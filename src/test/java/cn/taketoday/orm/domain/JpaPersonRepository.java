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

package cn.taketoday.orm.domain;

import infra.stereotype.Repository;
import infra.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * JPA based implementation of the {@link PersonRepository} API.
 *
 * @author Sam Brannen
 * @since 4.08
 */
@Transactional
@Repository
public class JpaPersonRepository implements PersonRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Person findById(Long id) {
    return this.entityManager.find(Person.class, id);
  }

  @Override
  public Person findByName(String name) {
    return this.entityManager.createQuery("from person where name = :name", Person.class)
            .setParameter("name", name)
            .getSingleResult();
  }

  @Override
  public Person save(Person person) {
    this.entityManager.persist(person);
    return person;
  }

  @Override
  public void remove(Person person) {
    this.entityManager.remove(person);
  }

}
