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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ximplementation.support.Implementation;
import org.ximplementation.support.ImplementationResolver;
import org.ximplementation.support.ImplementorBeanFactory;
import org.ximplementation.support.SimpleImplementorBeanFactory;

/**
 * {@linkplain CglibImplementeeBeanInvocationHandler} unit tests.
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-2
 *
 */
public class CglibImplementeeBeanInvocationHandlerTest
{
	private ImplementationResolver implementationResolver;

	private CglibImplementeeBeanBuilder cglibImplementeeBeanBuilder;

	@Before
	public void setUp() throws Exception
	{
		this.implementationResolver = new ImplementationResolver();
		this.cglibImplementeeBeanBuilder = new CglibImplementeeBeanBuilder();
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void invokeTest()
	{
		Implementation<Implementee> implementation = this.implementationResolver
				.resolve(Implementee.class, Implementee.class,
						Implementor0.class, Implementor1.class);

		ImplementorBeanFactory implementorBeanFactory = SimpleImplementorBeanFactory
				.valueOf(new Implementee(), new Implementor0(),
						new Implementor1());

		Implementee implementee = this.cglibImplementeeBeanBuilder
				.build(implementation, implementorBeanFactory);

		Assert.assertFalse(implementee.equals(new Object()));
		Assert.assertNotNull(implementee.handle());
	}

	public static class Implementee
	{
		public static final String RE = Implementee.class.getName();

		public String handle()
		{
			return RE;
		}
	}

	public static class Implementor0 extends Implementee
	{
		public static final String RE = Implementor0.class.getName();

		@Override
		public String handle()
		{
			return RE;
		}
	}

	public static class Implementor1 extends Implementee
	{
		public static final String RE = Implementor1.class.getName();

		@Override
		public String handle()
		{
			return RE;
		}
	}

	public static Method getMethodByName(Class<?> clazz, String name)
	{
		for (Method method : clazz.getMethods())
		{
			if (method.getName().equals(name))
				return method;
		}

		for (Method method : clazz.getDeclaredMethods())
		{
			if (method.getName().equals(name))
				return method;
		}

		return null;
	}

	public static Method getMethodByName(Method[] methods, String name)
	{
		for (Method method : methods)
		{
			if (method.getName().equals(name))
				return method;
		}

		return null;
	}

	public static Method getMethodByNameAndType(Class<?> clazz, String name,
			Class<?>... paramTypes)
	{
		for (Method method : clazz.getMethods())
		{
			if (method.getName().equals(name)
					&& Arrays.equals(method.getParameterTypes(), paramTypes))
				return method;
		}

		for (Method method : clazz.getDeclaredMethods())
		{
			if (method.getName().equals(name)
					&& Arrays.equals(method.getParameterTypes(), paramTypes))
				return method;
		}

		return null;
	}

	public static Method getMethodByNameAndType(Method[] methods, String name,
			Class<?>... paramTypes)
	{
		for (Method method : methods)
		{
			if (method.getName().equals(name)
					&& Arrays.equals(method.getParameterTypes(), paramTypes))
				return method;
		}

		return null;
	}
}
