/*
 * Copyright 2017 - 2024 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see [https://www.gnu.org/licenses/]
 */

package cn.taketoday.orm.jpa;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import infra.lang.Nullable;
import infra.logging.Logger;
import infra.logging.LoggerFactory;
import infra.transaction.support.TransactionSynchronizationManager;
import infra.util.ClassUtils;
import infra.util.CollectionUtils;
import infra.util.ConcurrentReferenceHashMap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TransactionRequiredException;

/**
 * Delegate for creating a shareable JPA {@link EntityManager}
 * reference for a given {@link EntityManagerFactory}.
 *
 * <p>A shared EntityManager will behave just like an EntityManager fetched from
 * an application server's JNDI environment, as defined by the JPA specification.
 * It will delegate all calls to the current transactional EntityManager, if any;
 * otherwise it will fall back to a newly created EntityManager per operation.
 *
 * <p>For a behavioral definition of such a shared transactional EntityManager,
 * see {@link jakarta.persistence.PersistenceContextType#TRANSACTION} and its
 * discussion in the JPA spec document. This is also the default being used
 * for the annotation-based {@link jakarta.persistence.PersistenceContext#type()}.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Oliver Gierke
 * @author Mark Paluch
 * @see jakarta.persistence.PersistenceContext
 * @see jakarta.persistence.PersistenceContextType#TRANSACTION
 * @see cn.taketoday.orm.jpa.JpaTransactionManager
 * @see ExtendedEntityManagerCreator
 * @since 4.0
 */
public abstract class SharedEntityManagerCreator {

  private static final Class<?>[] NO_ENTITY_MANAGER_INTERFACES = new Class<?>[0];

  private static final ConcurrentReferenceHashMap<Class<?>, Class<?>[]>
          cachedQueryInterfaces = new ConcurrentReferenceHashMap<>(4);

  private static final Set<String> transactionRequiringMethods = Set.of(
          "joinTransaction",
          "flush",
          "persist",
          "merge",
          "remove",
          "refresh");

  private static final Set<String> queryTerminatingMethods = Set.of(
          "execute",  // jakarta.persistence.StoredProcedureQuery.execute()
          "executeUpdate", // jakarta.persistence.Query.executeUpdate()
          "getSingleResult",  // jakarta.persistence.Query.getSingleResult()
          "getResultStream",  // jakarta.persistence.Query.getResultStream()
          "getResultList",  // jakarta.persistence.Query.getResultList()
          "list",  // org.hibernate.query.Query.list()
          "scroll",  // org.hibernate.query.Query.scroll()
          "stream",  // org.hibernate.query.Query.stream()
          "uniqueResult",  // org.hibernate.query.Query.uniqueResult()
          "uniqueResultOptional"  // org.hibernate.query.Query.uniqueResultOptional()
  );

  /**
   * Create a transactional EntityManager proxy for the given EntityManagerFactory.
   *
   * @param emf the EntityManagerFactory to delegate to.
   * @return a shareable transaction EntityManager proxy
   */
  public static EntityManager createSharedEntityManager(EntityManagerFactory emf) {
    return createSharedEntityManager(emf, null, true);
  }

  /**
   * Create a transactional EntityManager proxy for the given EntityManagerFactory.
   *
   * @param emf the EntityManagerFactory to delegate to.
   * @param properties the properties to be passed into the
   * {@code createEntityManager} call (may be {@code null})
   * @return a shareable transaction EntityManager proxy
   */
  public static EntityManager createSharedEntityManager(EntityManagerFactory emf, @Nullable Map<?, ?> properties) {
    return createSharedEntityManager(emf, properties, true);
  }

  /**
   * Create a transactional EntityManager proxy for the given EntityManagerFactory.
   *
   * @param emf the EntityManagerFactory to delegate to.
   * @param properties the properties to be passed into the
   * {@code createEntityManager} call (may be {@code null})
   * @param synchronizedWithTransaction whether to automatically join ongoing
   * transactions (according to the JPA 2.1 SynchronizationType rules)
   * @return a shareable transaction EntityManager proxy
   */
  public static EntityManager createSharedEntityManager(
          EntityManagerFactory emf, @Nullable Map<?, ?> properties, boolean synchronizedWithTransaction) {

    Class<?> emIfc = emf instanceof EntityManagerFactoryInfo info ?
            info.getEntityManagerInterface() : EntityManager.class;
    return createSharedEntityManager(emf, properties, synchronizedWithTransaction,
            (emIfc == null ? NO_ENTITY_MANAGER_INTERFACES : new Class<?>[] { emIfc }));
  }

  /**
   * Create a transactional EntityManager proxy for the given EntityManagerFactory.
   *
   * @param emf the EntityManagerFactory to obtain EntityManagers from as needed
   * @param properties the properties to be passed into the
   * {@code createEntityManager} call (may be {@code null})
   * @param entityManagerInterfaces the interfaces to be implemented by the
   * EntityManager. Allows the addition or specification of proprietary interfaces.
   * @return a shareable transactional EntityManager proxy
   */
  public static EntityManager createSharedEntityManager(
          EntityManagerFactory emf, @Nullable Map<?, ?> properties, Class<?>... entityManagerInterfaces) {

    return createSharedEntityManager(emf, properties, true, entityManagerInterfaces);
  }

  /**
   * Create a transactional EntityManager proxy for the given EntityManagerFactory.
   *
   * @param emf the EntityManagerFactory to obtain EntityManagers from as needed
   * @param properties the properties to be passed into the
   * {@code createEntityManager} call (may be {@code null})
   * @param synchronizedWithTransaction whether to automatically join ongoing
   * transactions (according to the JPA 2.1 SynchronizationType rules)
   * @param entityManagerInterfaces the interfaces to be implemented by the
   * EntityManager. Allows the addition or specification of proprietary interfaces.
   * @return a shareable transactional EntityManager proxy
   */
  public static EntityManager createSharedEntityManager(EntityManagerFactory emf, @Nullable Map<?, ?> properties,
          boolean synchronizedWithTransaction, Class<?>... entityManagerInterfaces) {
    ClassLoader cl = null;
    if (emf instanceof EntityManagerFactoryInfo info) {
      cl = info.getBeanClassLoader();
    }
    Class<?>[] ifcs = new Class<?>[entityManagerInterfaces.length + 1];
    System.arraycopy(entityManagerInterfaces, 0, ifcs, 0, entityManagerInterfaces.length);
    ifcs[entityManagerInterfaces.length] = EntityManagerProxy.class;
    return (EntityManager) Proxy.newProxyInstance(
            (cl != null ? cl : SharedEntityManagerCreator.class.getClassLoader()),
            ifcs, new SharedEntityManagerInvocationHandler(emf, properties, synchronizedWithTransaction));
  }

  /**
   * Invocation handler that delegates all calls to the current
   * transactional EntityManager, if any; else, it will fall back
   * to a newly created EntityManager per operation.
   */
  @SuppressWarnings("serial")
  private static class SharedEntityManagerInvocationHandler implements InvocationHandler, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SharedEntityManagerInvocationHandler.class);

    private final EntityManagerFactory targetFactory;

    @Nullable
    private final Map<?, ?> properties;

    private final boolean synchronizedWithTransaction;

    @Nullable
    private transient volatile ClassLoader proxyClassLoader;

    public SharedEntityManagerInvocationHandler(
            EntityManagerFactory target, @Nullable Map<?, ?> properties, boolean synchronizedWithTransaction) {

      this.targetFactory = target;
      this.properties = properties;
      this.synchronizedWithTransaction = synchronizedWithTransaction;
      initProxyClassLoader();
    }

    private void initProxyClassLoader() {
      if (this.targetFactory instanceof EntityManagerFactoryInfo) {
        this.proxyClassLoader = ((EntityManagerFactoryInfo) this.targetFactory).getBeanClassLoader();
      }
      else {
        this.proxyClassLoader = this.targetFactory.getClass().getClassLoader();
      }
    }

    @Override
    @Nullable
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // Invocation on EntityManager interface coming in...

      switch (method.getName()) {
        case "equals":
          // Only consider equal when proxies are identical.
          return (proxy == args[0]);
        case "hashCode":
          // Use hashCode of EntityManager proxy.
          return hashCode();
        case "toString":
          // Deliver toString without touching a target EntityManager.
          return "Shared EntityManager proxy for target factory [" + this.targetFactory + "]";
        case "getEntityManagerFactory":
          // JPA 2.0: return EntityManagerFactory without creating an EntityManager.
          return this.targetFactory;
        case "getCriteriaBuilder":
        case "getMetamodel":
          // JPA 2.0: return EntityManagerFactory's CriteriaBuilder/Metamodel (avoid creation of EntityManager)
          try {
            return EntityManagerFactory.class.getMethod(method.getName()).invoke(this.targetFactory);
          }
          catch (InvocationTargetException ex) {
            throw ex.getTargetException();
          }
        case "unwrap":
          // JPA 2.0: handle unwrap method - could be a proxy match.
          Class<?> targetClass = (Class<?>) args[0];
          if (targetClass != null && targetClass.isInstance(proxy)) {
            return proxy;
          }
          break;
        case "isOpen":
          // Handle isOpen method: always return true.
          return true;
        case "close":
          // Handle close method: suppress, not valid.
          return null;
        case "getTransaction":
          throw new IllegalStateException(
                  "Not allowed to create transaction on shared EntityManager - " +
                          "use Framework transactions or EJB CMT instead");
      }

      // Determine current EntityManager: either the transactional one
      // managed by the factory or a temporary one for the given invocation.
      EntityManager target = EntityManagerFactoryUtils.doGetTransactionalEntityManager(
              this.targetFactory, this.properties, this.synchronizedWithTransaction);

      switch (method.getName()) {
        case "getTargetEntityManager" -> {
          // Handle EntityManagerProxy interface.
          if (target == null) {
            throw new IllegalStateException("No transactional EntityManager available");
          }
          return target;
        }
        case "unwrap" -> {
          Class<?> targetClass = (Class<?>) args[0];
          if (targetClass == null) {
            return (target != null ? target : proxy);
          }
          // We need a transactional target now.
          if (target == null) {
            throw new IllegalStateException("No transactional EntityManager available");
          }
        }
        // Still perform unwrap call on target EntityManager.
      }

      if (transactionRequiringMethods.contains(method.getName())) {
        // We need a transactional target now, according to the JPA spec.
        // Otherwise, the operation would get accepted but remain unflushed...
        if (target == null || (!TransactionSynchronizationManager.isActualTransactionActive() &&
                !target.getTransaction().isActive())) {
          throw new TransactionRequiredException("No EntityManager with actual transaction available " +
                  "for current thread - cannot reliably process '" + method.getName() + "' call");
        }
      }

      // Regular EntityManager operations.
      boolean isNewEm = false;
      if (target == null) {
        logger.debug("Creating new EntityManager for shared EntityManager invocation");
        target = CollectionUtils.isNotEmpty(this.properties) ?
                this.targetFactory.createEntityManager(this.properties) :
                this.targetFactory.createEntityManager();
        isNewEm = true;
      }

      // Invoke method on current EntityManager.
      try {
        Object result = method.invoke(target, args);
        if (result instanceof Query query) {
          if (isNewEm) {
            Class<?>[] ifcs = cachedQueryInterfaces.computeIfAbsent(query.getClass(), key ->
                    ClassUtils.getAllInterfacesForClass(key, this.proxyClassLoader));
            result = Proxy.newProxyInstance(this.proxyClassLoader, ifcs,
                    new DeferredQueryInvocationHandler(query, target));
            isNewEm = false;
          }
          else {
            EntityManagerFactoryUtils.applyTransactionTimeout(query, this.targetFactory);
          }
        }
        return result;
      }
      catch (InvocationTargetException ex) {
        throw ex.getTargetException();
      }
      finally {
        if (isNewEm) {
          EntityManagerFactoryUtils.closeEntityManager(target);
        }
      }
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      // Rely on default serialization, just initialize state after deserialization.
      ois.defaultReadObject();
      // Initialize transient fields.
      initProxyClassLoader();
    }
  }

  /**
   * Invocation handler that handles deferred Query objects created by
   * non-transactional createQuery invocations on a shared EntityManager.
   * <p>Includes deferred output parameter access for JPA 2.1 StoredProcedureQuery,
   * retrieving the corresponding values for all registered parameters on query
   * termination and returning the locally cached values for subsequent access.
   */
  private static class DeferredQueryInvocationHandler implements InvocationHandler {

    private final Query target;

    @Nullable
    private EntityManager entityManager;

    @Nullable
    private Map<Object, Object> outputParameters;

    public DeferredQueryInvocationHandler(Query target, EntityManager entityManager) {
      this.target = target;
      this.entityManager = entityManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // Invocation on Query interface coming in...

      switch (method.getName()) {
        case "equals":
          // Only consider equal when proxies are identical.
          return (proxy == args[0]);
        case "hashCode":
          // Use hashCode of EntityManager proxy.
          return hashCode();
        case "unwrap":
          // Handle JPA 2.0 unwrap method - could be a proxy match.
          Class<?> targetClass = (Class<?>) args[0];
          if (targetClass == null) {
            return this.target;
          }
          else if (targetClass.isInstance(proxy)) {
            return proxy;
          }
          break;
        case "getOutputParameterValue":
          if (this.entityManager == null) {
            Object key = args[0];
            if (this.outputParameters == null || !this.outputParameters.containsKey(key)) {
              throw new IllegalArgumentException("OUT/INOUT parameter not available: " + key);
            }
            Object value = this.outputParameters.get(key);
            if (value instanceof IllegalArgumentException) {
              throw (IllegalArgumentException) value;
            }
            return value;
          }
          break;
      }

      // Invoke method on actual Query object.
      try {
        Object retVal = method.invoke(this.target, args);
        if (method.getName().equals("registerStoredProcedureParameter")
                && args.length == 3
                && (args[2] == ParameterMode.OUT || args[2] == ParameterMode.INOUT)) {
          if (this.outputParameters == null) {
            this.outputParameters = new LinkedHashMap<>();
          }
          this.outputParameters.put(args[0], null);
        }
        return retVal == this.target ? proxy : retVal;
      }
      catch (InvocationTargetException ex) {
        throw ex.getTargetException();
      }
      finally {
        if (queryTerminatingMethods.contains(method.getName())) {
          // Actual execution of the query: close the EntityManager right
          // afterwards, since that was the only reason we kept it open.
          if (this.outputParameters != null && this.target instanceof StoredProcedureQuery storedProc) {
            for (Map.Entry<Object, Object> entry : this.outputParameters.entrySet()) {
              try {
                Object key = entry.getKey();
                if (key instanceof Integer) {
                  entry.setValue(storedProc.getOutputParameterValue((Integer) key));
                }
                else {
                  entry.setValue(storedProc.getOutputParameterValue(key.toString()));
                }
              }
              catch (RuntimeException ex) {
                entry.setValue(ex);
              }
            }
          }
          EntityManagerFactoryUtils.closeEntityManager(this.entityManager);
          this.entityManager = null;
        }
      }
    }
  }

}
