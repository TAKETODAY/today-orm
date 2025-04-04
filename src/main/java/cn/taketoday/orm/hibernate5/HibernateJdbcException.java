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

package cn.taketoday.orm.hibernate5;

import org.hibernate.JDBCException;

import java.sql.SQLException;

import infra.dao.UncategorizedDataAccessException;

/**
 * Hibernate-specific subclass of UncategorizedDataAccessException,
 * for JDBC exceptions that Hibernate wrapped.
 *
 * @author Juergen Hoeller
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @see SessionFactoryUtils#convertHibernateAccessException
 * @since 4.0
 */
public class HibernateJdbcException extends UncategorizedDataAccessException {

  public HibernateJdbcException(JDBCException ex) {
    super("JDBC exception on Hibernate data access: SQLException for SQL [%s]; SQL state [%s]; error code [%d]; %s"
            .formatted(ex.getSQL(), ex.getSQLState(), ex.getErrorCode(), ex.getMessage()), ex);
  }

  /**
   * Return the underlying SQLException.
   */
  public SQLException getSQLException() {
    return ((JDBCException) getCause()).getSQLException();
  }

  /**
   * Return the SQL that led to the problem.
   */
  public String getSql() {
    return ((JDBCException) getCause()).getSQL();
  }

}
