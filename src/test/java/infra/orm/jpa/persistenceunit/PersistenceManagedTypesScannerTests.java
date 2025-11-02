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

import org.junit.jupiter.api.Test;

import infra.orm.jpa.domain.DriversLicense;
import infra.orm.jpa.domain.Employee;
import infra.orm.jpa.domain.EmployeeLocationConverter;
import infra.orm.jpa.domain.Person;
import infra.orm.jpa.domain2.entity.User;
import infra.core.io.ClassPathResource;
import infra.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/10/31 10:26
 */
class PersistenceManagedTypesScannerTests {

  private final PersistenceManagedTypesScanner scanner = new PersistenceManagedTypesScanner(new DefaultResourceLoader());

  @Test
  void scanPackageWithOnlyEntities() {
    PersistenceManagedTypes managedTypes = this.scanner.scan("infra.orm.jpa.domain");
    assertThat(managedTypes.getManagedClassNames()).containsExactlyInAnyOrder(
            Person.class.getName(), DriversLicense.class.getName(), Employee.class.getName(),
            EmployeeLocationConverter.class.getName());
    assertThat(managedTypes.getManagedPackages()).isEmpty();
  }

  @Test
  void scanPackageWithEntitiesAndManagedPackages() {
    PersistenceManagedTypes managedTypes = this.scanner.scan("infra.orm.jpa.domain2");
    assertThat(managedTypes.getManagedClassNames()).containsExactlyInAnyOrder(User.class.getName());
    assertThat(managedTypes.getManagedPackages()).containsExactlyInAnyOrder("infra.orm.jpa.domain2");
  }

  @Test
  void scanPackageUsesIndexIfPresent() {
    DefaultResourceLoader resourceLoader = new DefaultResourceLoader(
            infra.context.testfixture.index.CandidateComponentsTestClassLoader.index(getClass().getClassLoader(),
                    new ClassPathResource("test-today.components", getClass())));
    PersistenceManagedTypes managedTypes = new PersistenceManagedTypesScanner(resourceLoader).scan("com.example");
    assertThat(managedTypes.getManagedClassNames()).containsExactlyInAnyOrder(
            "com.example.domain.Person", "com.example.domain.Address");
    assertThat(managedTypes.getManagedPackages()).containsExactlyInAnyOrder(
            "com.example.domain");

  }

}