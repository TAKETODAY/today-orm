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

package infra.orm.jpa.persistenceunit;

import org.hibernate.tuple.CreationTimestampGeneration;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.sql.DataSource;

import infra.aot.hint.MemberCategory;
import infra.aot.hint.RuntimeHints;
import infra.aot.test.generate.TestGenerationContext;
import infra.context.ApplicationContextInitializer;
import infra.context.annotation.AnnotationConfigApplicationContext;
import infra.context.annotation.Bean;
import infra.context.aot.ApplicationContextAotGenerator;
import infra.context.support.GenericApplicationContext;
import infra.core.io.ResourceLoader;
import infra.core.test.tools.Compiled;
import infra.core.test.tools.TestCompiler;
import infra.orm.jpa.JpaVendorAdapter;
import infra.orm.jpa.LocalContainerEntityManagerFactoryBean;
import infra.orm.jpa.domain.DriversLicense;
import infra.orm.jpa.domain.Employee;
import infra.orm.jpa.domain.EmployeeCategoryConverter;
import infra.orm.jpa.domain.EmployeeId;
import infra.orm.jpa.domain.EmployeeKindConverter;
import infra.orm.jpa.domain.EmployeeLocation;
import infra.orm.jpa.domain.EmployeeLocationConverter;
import infra.orm.jpa.domain.Person;
import infra.orm.jpa.domain.PersonListener;
import infra.orm.jpa.vendor.Database;
import infra.orm.jpa.vendor.HibernateJpaVendorAdapter;

import static infra.aot.hint.predicate.RuntimeHintsPredicates.reflection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2023/6/28 21:58
 */
class PersistenceManagedTypesBeanRegistrationAotProcessorTests {

  @Test
  void processEntityManagerWithPackagesToScan() {
    GenericApplicationContext context = new AnnotationConfigApplicationContext();
    context.registerBean(JpaDomainConfiguration.class);
    compile(context, (initializer, compiled) -> {
      GenericApplicationContext freshApplicationContext = toFreshApplicationContext(
              initializer);
      PersistenceManagedTypes persistenceManagedTypes = freshApplicationContext.getBean(
              "persistenceManagedTypes", PersistenceManagedTypes.class);
      assertThat(persistenceManagedTypes.getManagedClassNames()).containsExactlyInAnyOrder(
              DriversLicense.class.getName(), Person.class.getName(), Employee.class.getName(),
              EmployeeLocationConverter.class.getName());
      assertThat(persistenceManagedTypes.getManagedPackages()).isEmpty();
      assertThat(freshApplicationContext.getBean(
              JpaDomainConfiguration.class).scanningInvoked).isFalse();
    });
  }

  @Test
  void contributeJpaHints() {
    GenericApplicationContext context = new AnnotationConfigApplicationContext();
    context.registerBean(JpaDomainConfiguration.class);
    contributeHints(context, hints -> {
      assertThat(reflection().onType(DriversLicense.class).withMemberCategories(MemberCategory.ACCESS_DECLARED_FIELDS)).accepts(hints);
      assertThat(reflection().onType(Person.class).withMemberCategories(MemberCategory.ACCESS_DECLARED_FIELDS)).accepts(hints);
      assertThat(reflection().onType(PersonListener.class).withMemberCategories(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS)).accepts(hints);
      assertThat(reflection().onType(Employee.class).withMemberCategories(MemberCategory.ACCESS_DECLARED_FIELDS)).accepts(hints);
      assertThat(reflection().onMethodInvocation(Employee.class, "preRemove")).accepts(hints);
      assertThat(reflection().onType(EmployeeId.class).withMemberCategories(MemberCategory.ACCESS_DECLARED_FIELDS)).accepts(hints);
      assertThat(reflection().onType(EmployeeLocationConverter.class).withMemberCategories(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
      assertThat(reflection().onType(EmployeeCategoryConverter.class).withMemberCategories(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
      assertThat(reflection().onType(EmployeeKindConverter.class).withMemberCategories(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
      assertThat(reflection().onType(EmployeeLocation.class).withMemberCategories(MemberCategory.ACCESS_DECLARED_FIELDS)).accepts(hints);
    });
  }

  @Test
  void contributeHibernateHints() {
    GenericApplicationContext context = new AnnotationConfigApplicationContext();
    context.registerBean(HibernateDomainConfiguration.class);
    contributeHints(context, hints ->
            assertThat(reflection().onType(CreationTimestampGeneration.class)
                    .withMemberCategories(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints));
  }

  @SuppressWarnings("unchecked")
  private void compile(GenericApplicationContext applicationContext,
          BiConsumer<ApplicationContextInitializer, Compiled> result) {
    ApplicationContextAotGenerator generator = new ApplicationContextAotGenerator();
    TestGenerationContext generationContext = new TestGenerationContext();
    generator.processAheadOfTime(applicationContext, generationContext);
    generationContext.writeGeneratedContent();
    TestCompiler.forSystem().with(generationContext).compile(compiled ->
            result.accept(compiled.getInstance(ApplicationContextInitializer.class), compiled));
  }

  private GenericApplicationContext toFreshApplicationContext(
          ApplicationContextInitializer initializer) {
    GenericApplicationContext freshApplicationContext = new GenericApplicationContext();
    initializer.initialize(freshApplicationContext);
    freshApplicationContext.refresh();
    return freshApplicationContext;
  }

  private void contributeHints(GenericApplicationContext applicationContext, Consumer<RuntimeHints> result) {
    ApplicationContextAotGenerator generator = new ApplicationContextAotGenerator();
    TestGenerationContext generationContext = new TestGenerationContext();
    generator.processAheadOfTime(applicationContext, generationContext);
    result.accept(generationContext.getRuntimeHints());
  }

  public static class JpaDomainConfiguration extends AbstractEntityManagerWithPackagesToScanConfiguration {

    @Override
    protected String packageToScan() {
      return "infra.orm.jpa.domain";
    }
  }

  public static class HibernateDomainConfiguration extends AbstractEntityManagerWithPackagesToScanConfiguration {

    @Override
    protected String packageToScan() {
      return "infra.orm.jpa.hibernate.domain";
    }
  }

  public abstract static class AbstractEntityManagerWithPackagesToScanConfiguration {

    protected boolean scanningInvoked;

    @Bean
    public DataSource mockDataSource() {
      return mock();
    }

    @Bean
    public HibernateJpaVendorAdapter jpaVendorAdapter() {
      HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
      jpaVendorAdapter.setDatabase(Database.HSQL);
      return jpaVendorAdapter;
    }

    @Bean
    public PersistenceManagedTypes persistenceManagedTypes(ResourceLoader resourceLoader) {
      this.scanningInvoked = true;
      return new PersistenceManagedTypesScanner(resourceLoader)
              .scan(packageToScan());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
            JpaVendorAdapter jpaVendorAdapter, PersistenceManagedTypes persistenceManagedTypes) {
      LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
      entityManagerFactoryBean.setDataSource(dataSource);
      entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);
      entityManagerFactoryBean.setManagedTypes(persistenceManagedTypes);
      return entityManagerFactoryBean;
    }

    protected abstract String packageToScan();

  }

}