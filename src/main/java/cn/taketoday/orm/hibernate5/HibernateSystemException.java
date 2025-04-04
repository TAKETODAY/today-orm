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

import org.hibernate.HibernateException;

import infra.dao.UncategorizedDataAccessException;
import infra.lang.Nullable;

/**
 * Hibernate-specific subclass of UncategorizedDataAccessException,
 * for Hibernate system errors that do not match any concrete
 * {@code infra.dao} exceptions.
 *
 * @author Juergen Hoeller
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @see SessionFactoryUtils#convertHibernateAccessException
 * @since 4.0
 */
public class HibernateSystemException extends UncategorizedDataAccessException {

  /**
   * Create a new HibernateSystemException,
   * wrapping an arbitrary HibernateException.
   *
   * @param cause the HibernateException thrown
   */
  public HibernateSystemException(@Nullable HibernateException cause) {
    super(cause != null ? cause.getMessage() : null, cause);
  }

}
