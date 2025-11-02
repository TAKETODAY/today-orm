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
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmployeeLocationConverter implements AttributeConverter<EmployeeLocation, String> {

  @Override
  public String convertToDatabaseColumn(EmployeeLocation employeeLocation) {
    if (employeeLocation != null) {
      return employeeLocation.getLocation();
    }
    return null;
  }

  @Override
  public EmployeeLocation convertToEntityAttribute(String data) {
    if (data != null) {
      EmployeeLocation employeeLocation = new EmployeeLocation();
      employeeLocation.setLocation(data);
      return employeeLocation;
    }
    return null;
  }
}