/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © Harry Yang & 2017 - 2023 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.orm.jpa.domain;

import jakarta.persistence.AttributeConverter;

public class EmployeeKindConverter implements AttributeConverter<EmployeeKind, String> {

	@Override
	public String convertToDatabaseColumn(EmployeeKind employeeKind) {
		if (employeeKind != null) {
			return employeeKind.getName();
		}
		return null;
	}

	@Override
	public EmployeeKind convertToEntityAttribute(String data) {
		if (data != null) {
			EmployeeKind employeeKind = new EmployeeKind();
			employeeKind.setName(data);
			return employeeKind;
		}
		return null;
	}
}
