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

package cn.taketoday.annotation.config.jpa.test;

import infra.beans.factory.annotation.Autowired;
import infra.beans.factory.config.ConfigurableBeanFactory;
import jakarta.persistence.PostLoad;

public class CityListener {

  private ConfigurableBeanFactory beanFactory;

  public CityListener() {
  }

  @Autowired
  public CityListener(ConfigurableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @PostLoad
  public void postLoad(City city) {
    if (this.beanFactory != null) {
      this.beanFactory.registerSingleton(City.class.getName(), city);
    }
  }

}
