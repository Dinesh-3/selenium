// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.grid.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Config {

  Set<String> getSectionNames();

  Set<String> getOptions(String section);

  Optional<List<String>> getAll(String section, String option);

  default Optional<String> get(String section, String option) {
    return getAll(section, option).map(items -> items.isEmpty() ? null : items.get(0));
  }

  default Optional<Integer> getInt(String section, String option) {
    return get(section, option).map(Integer::parseInt);
  }

  default Optional<Boolean> getBool(String section, String option) {
    return get(section, option).map(Boolean::parseBoolean);
  }

  default <X> Object getClass(String section, String option, Class<X> typeOfClass, String defaultClazz) {
    String clazz = get(section, option).orElse(defaultClazz);

    try {
      Class<?> ClassClazz = Class.forName(clazz);
      Method create = ClassClazz.getMethod("create", Config.class);

      if (!Modifier.isStatic(create.getModifiers())) {
        throw new IllegalArgumentException(String.format(
            "Class %s's `create(Config)` method must be static", clazz));
      }

      if (!typeOfClass.isAssignableFrom(create.getReturnType())) {
        throw new IllegalArgumentException(String.format(
            "Class %s's `create(Config)` method must be static", clazz));
      }

      return create.invoke(null, this);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(String.format(
          "Class %s must have a static `create(Config)` method", clazz));
    } catch (ReflectiveOperationException e) {
      throw new IllegalArgumentException("Unable to find class: " + clazz, e);
    }
  }
}
