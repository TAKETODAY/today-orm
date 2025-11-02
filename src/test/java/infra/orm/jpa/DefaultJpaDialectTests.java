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

import org.junit.jupiter.api.Test;

import infra.transaction.TransactionDefinition;
import infra.transaction.TransactionException;
import infra.transaction.support.DefaultTransactionDefinition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @author Costin Leau
 * @author Phillip Webb
 */
public class DefaultJpaDialectTests {

  private JpaDialect dialect = new DefaultJpaDialect();

  @Test
  public void testDefaultTransactionDefinition() throws Exception {
    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
    definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    assertThatExceptionOfType(TransactionException.class).isThrownBy(() ->
            dialect.beginTransaction(null, definition));
  }

  @Test
  public void testDefaultBeginTransaction() throws Exception {
    TransactionDefinition definition = new DefaultTransactionDefinition();
    EntityManager entityManager = mock(EntityManager.class);
    EntityTransaction entityTx = mock(EntityTransaction.class);

    given(entityManager.getTransaction()).willReturn(entityTx);

    dialect.beginTransaction(entityManager, definition);
  }

  @Test
  public void testTranslateException() {
    OptimisticLockException ex = new OptimisticLockException();
    assertThat(dialect.translateExceptionIfPossible(ex).getCause()).isEqualTo(EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(ex).getCause());
  }
}
