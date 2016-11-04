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
