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

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

/**
 * Person entity listener.
 *
 * @author Sam Brannen
 * @since 4.08
 */
public class PersonListener {

  public static final List<String> methodsInvoked = new ArrayList<>();

  @PostLoad
  public void postLoad(Person person) {
    methodsInvoked.add("@PostLoad: " + person.getName());
  }

  @PrePersist
  public void prePersist(Person person) {
    methodsInvoked.add("@PrePersist: " + person.getName());
  }

  @PostPersist
  public void postPersist(Person person) {
    methodsInvoked.add("@PostPersist: " + person.getName());
  }

  @PreUpdate
  public void preUpdate(Person person) {
    methodsInvoked.add("@PreUpdate: " + person.getName());
  }

  @PostUpdate
  public void postUpdate(Person person) {
    methodsInvoked.add("@PostUpdate: " + person.getName());
  }

  @PreRemove
  public void preRemove(Person person) {
    methodsInvoked.add("@PreRemove: " + person.getName());
  }

  @PostRemove
  public void postRemove(Person person) {
    methodsInvoked.add("@PostRemove: " + person.getName());
  }

}
