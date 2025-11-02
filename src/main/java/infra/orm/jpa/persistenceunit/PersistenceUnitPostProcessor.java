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

import infra.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * Callback interface for post-processing a JPA PersistenceUnitInfo.
 * Implementations can be registered with a DefaultPersistenceUnitManager
 * or via a LocalContainerEntityManagerFactoryBean.
 *
 * @author Juergen Hoeller
 * @see DefaultPersistenceUnitManager#setPersistenceUnitPostProcessors
 * @see LocalContainerEntityManagerFactoryBean#setPersistenceUnitPostProcessors
 * @since 4.0
 */
public interface PersistenceUnitPostProcessor {

  /**
   * Post-process the given PersistenceUnitInfo, for example registering
   * further entity classes and jar files.
   *
   * @param pui the chosen PersistenceUnitInfo, as read from {@code persistence.xml}.
   * Passed in as MutablePersistenceUnitInfo.
   */
  void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui);

}
