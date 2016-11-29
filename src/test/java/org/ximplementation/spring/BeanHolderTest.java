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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * {@linkplain BeanHolder} unit tests.
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-29
 *
 */
public class BeanHolderTest
{
	private ApplicationContext applicationContext;

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
	public void getBean()
	{
		// not peeling
		{
			Object beanA = new BeanHolder(applicationContext,
					"beanHolderTest.MyBeanA", false).getBean();
			Object beanB = new BeanHolder(applicationContext,
					"beanHolderTest.MyBeanB", false).getBean();
			Object beanC = new BeanHolder(applicationContext,
					"beanHolderTest.MyBeanCImple", false).getBean();

			assertNotNull(beanA);
			assertNotNull(beanB);
			assertNotNull(beanC);
			assertEquals(MyBeanA.class, beanA.getClass());
			assertTrue(beanB.getClass().getName()
					.indexOf("$$EnhancerByCGLIB$$") > 0);
			assertTrue(beanC instanceof java.lang.reflect.Proxy);
		}

		// peeling
		{
			Object beanA = new BeanHolder(applicationContext,
					"beanHolderTest.MyBeanA", true).getBean();
			Object beanB = new BeanHolder(applicationContext,
					"beanHolderTest.MyBeanB", true).getBean();
			Object beanC = new BeanHolder(applicationContext,
					"beanHolderTest.MyBeanCImple", true).getBean();

			assertNotNull(beanA);
			assertNotNull(beanB);
			assertNotNull(beanC);
			assertEquals(MyBeanA.class, beanA.getClass());
			assertEquals(MyBeanB.class, beanB.getClass());
			assertEquals(MyBeanCImple.class, beanC.getClass());
		}
	}

	@Test
	public void peelSpringJdkProxy()
	{
		Object proxy = applicationContext
				.getBean("beanHolderTest.MyBeanCImple");
		Object bean = new BeanHolder(null, null, true)
				.peelSpringJdkProxy((java.lang.reflect.Proxy) proxy);

		assertEquals(MyBeanCImple.class, bean.getClass());
	}

	@Test
	public void peelSpringCglibProxy()
	{
		Object proxy = applicationContext
				.getBean("beanHolderTest.MyBeanB");
		Object bean = new BeanHolder(null, null, true)
				.peelSpringCglibProxy((net.sf.cglib.proxy.Factory) proxy);

		assertEquals(MyBeanB.class, bean.getClass());
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

		@Pointcut("execution(* org.ximplementation.spring.BeanHolderTest$MyBeanB.handle(..))")
		private void testPointcut()
		{
		}

		@org.aspectj.lang.annotation.Before("testPointcut()")
		public void beforeAspect(JoinPoint jp) throws Throwable
		{
			System.out.println("Before aspect execute");
		}

		@Pointcut("execution(* org.ximplementation.spring.BeanHolderTest$MyBeanC.handle(..))")
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
