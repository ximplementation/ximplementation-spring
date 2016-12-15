/**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *  
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *  
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */

package org.ximplementation.spring;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Unit tests support class.
 * 
 * @author earthangry@gmail.com
 * @date 2016-12-15
 *
 */
public abstract class AbstractTestSupport
{
	public static Method getMethodByName(Class<?> clazz, String name)
	{
		return doGetMethod(clazz, true, name);
	}

	public static Method getMethodByNameAndType(Class<?> clazz, String name,
			Class<?>... paramTypes)
	{
		return doGetMethod(clazz, false, name, paramTypes);
	}

	protected static Method doGetMethod(Class<?> clazz, boolean onlyName,
			String name, Class<?>... paramTypes)
	{
		Method[] myMethods = clazz.getDeclaredMethods();

		for (Method myMethod : myMethods)
		{
			if (onlyName)
			{
				if (myMethod.getName().equals(name))
					return myMethod;
			}
			else
			{
				if (myMethod.getName().equals(name) && Arrays.equals(paramTypes,
						myMethod.getParameterTypes()))
					return myMethod;
			}
		}
		
		Method method = null;

		// methods in super class
		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null)
			method = doGetMethod(superClass, onlyName, name, paramTypes);
		
		if(method != null)
			return method;

		// methods in super interfaces
		Class<?>[] superInterfaces = clazz.getInterfaces();
		if (superInterfaces != null)
		{
			for (Class<?> superInterface : superInterfaces)
			{
				method = doGetMethod(superInterface, onlyName, name,
						paramTypes);

				if (method != null)
					return method;
			}
		}

		return null;
	}
}
