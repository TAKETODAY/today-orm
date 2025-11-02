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

package infra.orm.jpa.support;

import org.junit.jupiter.api.Test;

import infra.orm.jpa.EntityManagerHolder;
import infra.orm.jpa.EntityManagerProxy;
import infra.transaction.support.TransactionSynchronizationManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Phillip Webb
 */
public class SharedEntityManagerFactoryTests {

  @Test
  public void testValidUsage() {
    Object o = new Object();

    EntityManager mockEm = mock(EntityManager.class);
    given(mockEm.isOpen()).willReturn(true);

    EntityManagerFactory mockEmf = mock(EntityManagerFactory.class);
    given(mockEmf.createEntityManager()).willReturn(mockEm);

    SharedEntityManagerBean proxyFactoryBean = new SharedEntityManagerBean();
    proxyFactoryBean.setEntityManagerFactory(mockEmf);
    proxyFactoryBean.afterPropertiesSet();

    assertThat(EntityManager.class.isAssignableFrom(proxyFactoryBean.getObjectType())).isTrue();
    assertThat(proxyFactoryBean.isSingleton()).isTrue();

    EntityManager proxy = proxyFactoryBean.getObject();
    assertThat(proxyFactoryBean.getObject()).isSameAs(proxy);
    assertThat(proxy.contains(o)).isFalse();

    boolean condition = proxy instanceof EntityManagerProxy;
    assertThat(condition).isTrue();
    EntityManagerProxy emProxy = (EntityManagerProxy) proxy;
    assertThatIllegalStateException().as("outside of transaction").isThrownBy(
            emProxy::getTargetEntityManager);

    TransactionSynchronizationManager.bindResource(mockEmf, new EntityManagerHolder(mockEm));
    try {
      assertThat(emProxy.getTargetEntityManager()).isSameAs(mockEm);
    }
    finally {
      TransactionSynchronizationManager.unbindResource(mockEmf);
    }

    assertThat(TransactionSynchronizationManager.getResourceMap().isEmpty()).isTrue();
    verify(mockEm).contains(o);
    verify(mockEm).close();
  }

}
