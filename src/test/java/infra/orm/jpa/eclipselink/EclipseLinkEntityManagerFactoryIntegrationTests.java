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

package infra.orm.jpa.eclipselink;

import org.eclipse.persistence.jpa.JpaEntityManager;
import org.junit.jupiter.api.Test;

import infra.orm.jpa.AbstractContainerEntityManagerFactoryIntegrationTests;
import infra.orm.jpa.EntityManagerFactoryInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EclipseLink-specific JPA tests.
 *
 * @author Juergen Hoeller
 */
public class EclipseLinkEntityManagerFactoryIntegrationTests extends AbstractContainerEntityManagerFactoryIntegrationTests {

  @Test
  public void testCanCastNativeEntityManagerFactoryToEclipseLinkEntityManagerFactoryImpl() {
    EntityManagerFactoryInfo emfi = (EntityManagerFactoryInfo) entityManagerFactory;
    assertThat(emfi.getNativeEntityManagerFactory().getClass().getName().endsWith("EntityManagerFactoryImpl")).isTrue();
  }

  @Test
  public void testCanCastSharedEntityManagerProxyToEclipseLinkEntityManager() {
    boolean condition = sharedEntityManager instanceof JpaEntityManager;
    assertThat(condition).isTrue();
    JpaEntityManager eclipselinkEntityManager = (JpaEntityManager) sharedEntityManager;
    assertThat(eclipselinkEntityManager.getActiveSession()).isNotNull();
  }

}
