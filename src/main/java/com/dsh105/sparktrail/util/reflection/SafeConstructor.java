/*
 * This file is part of SparkTrail 3.
 *
 * SparkTrail 3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SparkTrail 3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SparkTrail 3.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of EchoPet.
 *
 * EchoPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EchoPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EchoPet.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dsh105.sparktrail.util.reflection;

import com.dsh105.dshutils.logger.ConsoleLogger;
import com.dsh105.dshutils.logger.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SafeConstructor<T> {

      private Constructor<T> constructor;
      private Class[] params;

      public SafeConstructor(Constructor constructor) {
            setConstructor(constructor);
      }

      public SafeConstructor(Class<?> coreClass, Class<?>... params) {
            try {
                  Constructor constructor = coreClass.getConstructor(params);
                  setConstructor(constructor);
            } catch (NoSuchMethodException e) {
                  ConsoleLogger.log(Logger.LogLevel.WARNING, "No such constructor!");
            }
      }

      protected void setConstructor(Constructor constructor) {
            if (constructor == null) {
                  throw new UnsupportedOperationException("Cannot create a new constructor!");
            }
            this.constructor = constructor;
            this.params = constructor.getParameterTypes();
      }

      public Constructor getConstructor() {
            return this.constructor;
      }

      public T newInstance(Object... params) {
            try {
                  return (T) this.getConstructor().newInstance(params);
            } catch (InstantiationException e) {
                  throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                  throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                  throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                  throw new RuntimeException(e);
            }
      }
}
