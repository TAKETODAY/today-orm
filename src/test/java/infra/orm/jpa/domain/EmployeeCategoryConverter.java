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

package infra.orm.jpa.domain;

import jakarta.persistence.AttributeConverter;

public class EmployeeCategoryConverter implements AttributeConverter<EmployeeCategory, String> {

  @Override
  public String convertToDatabaseColumn(EmployeeCategory employeeCategory) {
    if (employeeCategory != null) {
      return employeeCategory.getName();
    }
    return null;
  }

  @Override
  public EmployeeCategory convertToEntityAttribute(String data) {
    if (data != null) {
      EmployeeCategory employeeCategory = new EmployeeCategory();
      employeeCategory.setName(data);
      return employeeCategory;
    }
    return null;
  }
}
