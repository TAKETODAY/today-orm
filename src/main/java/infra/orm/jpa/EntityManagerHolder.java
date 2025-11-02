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

package infra.orm.jpa;

import org.jspecify.annotations.Nullable;

import infra.lang.Assert;
import infra.orm.hibernate5.SessionHolder;
import infra.transaction.SavepointManager;
import infra.transaction.support.ResourceHolderSupport;
import jakarta.persistence.EntityManager;

/**
 * Resource holder wrapping a JPA {@link EntityManager}.
 * {@link JpaTransactionManager} binds instances of this class to the thread,
 * for a given {@link jakarta.persistence.EntityManagerFactory}.
 *
 * <p>Also serves as a base class for {@link SessionHolder},
 * as of 5.1.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @see JpaTransactionManager
 * @see EntityManagerFactoryUtils
 * @since 4.0
 */
public class EntityManagerHolder extends ResourceHolderSupport {

  @Nullable
  private final EntityManager entityManager;

  private boolean transactionActive;

  @Nullable
  private SavepointManager savepointManager;

  public EntityManagerHolder(@Nullable EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public EntityManager getEntityManager() {
    Assert.state(this.entityManager != null, "No EntityManager available");
    return this.entityManager;
  }

  protected void setTransactionActive(boolean transactionActive) {
    this.transactionActive = transactionActive;
  }

  protected boolean isTransactionActive() {
    return this.transactionActive;
  }

  protected void setSavepointManager(@Nullable SavepointManager savepointManager) {
    this.savepointManager = savepointManager;
  }

  @Nullable
  protected SavepointManager getSavepointManager() {
    return this.savepointManager;
  }

  @Override
  public void clear() {
    super.clear();
    this.transactionActive = false;
    this.savepointManager = null;
  }

}
