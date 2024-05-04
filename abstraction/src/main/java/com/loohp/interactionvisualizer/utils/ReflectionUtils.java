/*
 * This file is part of InteractionVisualizer-Abstraction.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static Field findDeclaredField(Class<?> clazz, Class<?> fieldType, String... names) throws NoSuchFieldException {
        NoSuchFieldException exception = null;
        for (String name : names) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (field.getType().equals(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException e) {
                exception = e;
            }
        }
        if (exception == null) {
            throw new NoSuchFieldException();
        } else {
            throw exception;
        }
    }

}
