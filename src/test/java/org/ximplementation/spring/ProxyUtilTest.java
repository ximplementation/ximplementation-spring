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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import net.sf.cglib.proxy.Enhancer;

/**
 * {@linkplain ProxyUtil} unit tests.
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-29
 *
 */
public class ProxyUtilTest
{
	private ApplicationContext applicationContext;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception
	{
		applicationContext = new ClassPathXmlApplicationContext(
				"classpath:applicationContext.xml");
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void peelSpringJdkProxy()
	{
		Object proxy = applicationContext
				.getBean("proxyUtilTest.MyBeanCImple");
		Object bean = ProxyUtil
				.peelSpringJdkProxy((java.lang.reflect.Proxy) proxy);

		assertEquals(MyBeanCImple.class, bean.getClass());
	}

	@Test
	public void peelSpringJdkProxy_throwForNotJdkDynamicAopProxy()
	{
		java.lang.reflect.InvocationHandler invocationHandler = new java.lang.reflect.InvocationHandler()
		{
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable
			{
				return null;
			}
		};

		java.lang.reflect.Proxy proxy = (java.lang.reflect.Proxy) java.lang.reflect.Proxy
				.newProxyInstance(getClass().getClassLoader(),
						new Class<?>[] { MyBeanC.class }, invocationHandler);

		expectedException.expect(ProxyPeelingException.class);
		expectedException.expectMessage(
				"] is not created by [" + ProxyUtil.JdkDynamicAopProxyName
						+ "]");

		ProxyUtil.peelSpringJdkProxy(proxy);
	}

	@Test
	public void peelSpringCglibProxy()
	{
		Object proxy = applicationContext
				.getBean("proxyUtilTest.MyBeanB");
		Object bean = ProxyUtil
				.peelSpringCglibProxy((net.sf.cglib.proxy.Factory) proxy);

		assertEquals(MyBeanB.class, bean.getClass());
	}

	@Test
	public void peelSpringCglibProxy_throwForNotCglibAopProxy()
	{
		net.sf.cglib.proxy.InvocationHandler invocationHandler = new net.sf.cglib.proxy.InvocationHandler()
		{
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable
			{
				return null;
			}
		};

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(MyBeanB.class);
		enhancer.setCallback(invocationHandler);

		Object proxy = enhancer.create();

		expectedException.expect(ProxyPeelingException.class);
		expectedException.expectMessage("] may not be created by ["
				+ ProxyUtil.Cglib2AopProxyName + "], no ["
				+ ProxyUtil.Cglib2AopProxyDynamicAdvisedInterceptorName
				+ "] found in its callbacks.");

		ProxyUtil.peelSpringCglibProxy((net.sf.cglib.proxy.Factory) proxy);
	}

	@Component
	public static class MyBeanA
	{
		public void handle()
		{
		}

		@Override
		public String toString()
		{
			return getClass().getName();
		}
	}

	@Component
	public static class MyBeanB
	{
		public void handle()
		{
		}

		@Override
		public String toString()
		{
			return getClass().getName();
		}
	}

	@Component
	public static interface MyBeanC
	{
		public void handle();
	}

	@Component
	public static class MyBeanCImple implements MyBeanC
	{
		@Override
		public void handle()
		{
		}
	}

	@Component
	@Aspect
	public static class MyAspect
	{
		public static final String PREFIX = MyAspect.class.getSimpleName();

		@Pointcut("execution(* org.ximplementation.spring.ProxyUtilTest$MyBeanB.handle(..))")
		private void testPointcut()
		{
		}

		@org.aspectj.lang.annotation.Before("testPointcut()")
		public void beforeAspect(JoinPoint jp) throws Throwable
		{
			System.out.println("Before aspect execute");
		}

		@Pointcut("execution(* org.ximplementation.spring.ProxyUtilTest$MyBeanC.handle(..))")
		private void testPointcut1()
		{
		}

		@org.aspectj.lang.annotation.Before("testPointcut1()")
		public void beforeAspect1(JoinPoint jp) throws Throwable
		{
			System.out.println("Before aspect execute");
		}
	}
}
