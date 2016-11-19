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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.ximplementation.Implement;
import org.ximplementation.Implementor;
import org.ximplementation.Validity;

/**
 * {@linkplain ImplementeeBeanCreationPostProcessor} unit tests.
 * 
 * @author earthangry@gmail.com
 * @date 2016-8-16
 *
 */
public class ImplementeeBeanCreationPostProcessorTest
{
	private ApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception
	{
		applicationContext = new ClassPathXmlApplicationContext(
				"classpath:ImplementeeBeanCreationPostProcessorTest.xml");
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void testInjectionAndAop()
	{
		Controller controller = applicationContext.getBean(Controller.class);

		String re = controller.handle(new Byte((byte) 5));
		assertEquals(MyAspect.PREFIX + ServiceImpl0.MY_RE, re);

		re = controller.handle(ServiceImpl1.MY_NUMBER);
		assertEquals(MyAspect.PREFIX + ServiceImpl1.MY_RE, re);

		re = controller.handle(12345);
		assertEquals(MyAspect.PREFIX + ServiceImpl2.MY_RE, re);

		re = controller.handle(new Float(5.0F));
		assertEquals(MyAspect.PREFIX + ServiceImpl3.MY_RE, re);
	}

	@Test
	public void testOnlyOneDependentImplementeeBeanCreated()
	{
		Controller controller = applicationContext.getBean(Controller.class);

		Controller1 controller1 = applicationContext.getBean(Controller1.class);

		assertTrue(controller1.getService() == controller.getService());
	}

	@Test
	public void testNotInterfaceImplementeeBean()
	{
		TestNotInterfaceImplementeeBean.TNIController controller = applicationContext
				.getBean(TestNotInterfaceImplementeeBean.TNIController.class);

		assertTrue(controller.getService() instanceof CglibImplementee);
	}

	public static class TestNotInterfaceImplementeeBean
	{
		@Component
		public static class TNIController
		{
			private TNIService service;

			public TNIService getService()
			{
				return service;
			}

			@Autowired
			public void setService(TNIService service)
			{
				this.service = service;
			}
		}

		@Component
		public static class TNIService
		{

		}

		@Component
		public static class TNIServiceAnother extends TNIService
		{

		}
	}


	@Test
	public void testSetterMethodAutowired()
	{
		TestSetterMethodAutowired bean = applicationContext
				.getBean(TestSetterMethodAutowired.class);

		assertNotNull(bean.getService());
	}

	@Component
	public static class TestSetterMethodAutowired
	{
		private Service service;

		public Service getService()
		{
			return service;
		}

		@Autowired
		public void setService(Service service)
		{
			this.service = service;
		}
	}

	@Test
	public void testFieldAutowired()
	{
		TestFieldAutowired bean = applicationContext
				.getBean(TestFieldAutowired.class);

		assertNotNull(bean.getService());
	}

	@Component
	public static class TestFieldAutowired
	{
		@Autowired
		private Service service;

		public Service getService()
		{
			return service;
		}
	}

	@Test
	public void testNoXimplementationWhenOnlyOneInstance()
	{
		TestNoXimplementationWhenOnlyOneInstance.TNOController controller = applicationContext
				.getBean(
						TestNoXimplementationWhenOnlyOneInstance.TNOController.class);

		assertEquals(TestNoXimplementationWhenOnlyOneInstance.TNOService.class,
				controller.getService().getClass());

		assertEquals(
				TestNoXimplementationWhenOnlyOneInstance.TNOService1Impl.class,
				controller.getService1().getClass());
	}

	public static class TestNoXimplementationWhenOnlyOneInstance
	{
		@Component
		public static class TNOController
		{
			private TNOService service;

			private TNOService1 service1;

			public TNOService getService()
			{
				return service;
			}

			@Autowired
			public void setService(TNOService service)
			{
				this.service = service;
			}

			public TNOService1 getService1()
			{
				return service1;
			}

			@Autowired
			public void setService1(TNOService1 service1)
			{
				this.service1 = service1;
			}
		}

		@Component
		public static class TNOService
		{

		}

		public interface TNOService1
		{
		}

		@Component
		public static class TNOService1Impl implements TNOService1
		{
		}
	}

	@Test
	public void testPrototypeImplementorBean()
	{
		TestPrototypeImplementorBean.TPIController controller = applicationContext
				.getBean(TestPrototypeImplementorBean.TPIController.class);

		// singleton
		assertEquals(0, controller
				.getCount(TestPrototypeImplementorBean.TPIService1.TYPE));
		assertEquals(0, controller
				.getCount(TestPrototypeImplementorBean.TPIService1.TYPE));
		assertEquals(0, controller
				.getCount(TestPrototypeImplementorBean.TPIService1.TYPE));

		// prototype
		TestPrototypeImplementorBean.TPIService2.count = 0;
		assertEquals(1, controller
				.getCount(TestPrototypeImplementorBean.TPIService2.TYPE));
		assertEquals(2, controller
				.getCount(TestPrototypeImplementorBean.TPIService2.TYPE));
		assertEquals(3, controller
				.getCount(TestPrototypeImplementorBean.TPIService2.TYPE));
	}

	public static class TestPrototypeImplementorBean
	{
		@Component
		public static class TPIController
		{
			@Autowired
			private TPIService service;

			public TPIService getService()
			{
				return service;
			}

			public void setService(TPIService service)
			{
				this.service = service;
			}

			public int getCount(String type)
			{
				return this.service.getCount(type);
			}
		}

		public static interface TPIService
		{
			public int getCount(String type);
		}

		@Component
		public static class TPIService1 implements TPIService
		{
			public static final String TYPE = TPIService1.class.getName();

			private static int count = 0;

			public TPIService1()
			{
				super();
			}

			@Validity("isValid")
			@Override
			public int getCount(String type)
			{
				return count;
			}

			boolean isValid(String type)
			{
				return TYPE.equals(type);
			}
		}

		@Component
		@Scope("prototype")
		public static class TPIService2 implements TPIService
		{
			public static final String TYPE = TPIService2.class.getName();

			private static int count = 0;

			public TPIService2()
			{
				super();

				count += 1;
			}

			@Validity("isValid")
			@Override
			public int getCount(String type)
			{
				return count;
			}

			boolean isValid(String type)
			{
				return TYPE.equals(type);
			}
		}
	}

	@Test
	public void testPrototypeImplementorBean_AopOfJdkProxy()
	{
		// singleton
		TestPrototypeImplementorBean_AopOfJdkProxy.TPIController controller = applicationContext
				.getBean(
						TestPrototypeImplementorBean_AopOfJdkProxy.TPIController.class);

		TestPrototypeImplementorBean_AopOfJdkProxy.TPIService service = controller
				.getService();

		// singleton
		assertEquals(0, controller
				.getCount(
						TestPrototypeImplementorBean_AopOfJdkProxy.TPIService1.TYPE));
		assertEquals(0, controller
				.getCount(
						TestPrototypeImplementorBean_AopOfJdkProxy.TPIService1.TYPE));
		assertEquals(0, controller
				.getCount(
						TestPrototypeImplementorBean_AopOfJdkProxy.TPIService1.TYPE));

		// AOP Jdk Proxy
		TestPrototypeImplementorBean_AopOfJdkProxy.TPIService2.count = 0;
		assertEquals(1, controller.getCount(
				TestPrototypeImplementorBean_AopOfJdkProxy.TPIService2.TYPE));
		assertEquals(2, controller.getCount(
				TestPrototypeImplementorBean_AopOfJdkProxy.TPIService2.TYPE));
		assertEquals(3, controller.getCount(
				TestPrototypeImplementorBean_AopOfJdkProxy.TPIService2.TYPE));
	}

	public static class TestPrototypeImplementorBean_AopOfJdkProxy
	{
		@Component
		public static class TPIController
		{
			@Autowired
			private TPIService service;

			public TPIService getService()
			{
				return service;
			}

			public void setService(TPIService service)
			{
				this.service = service;
			}

			public int getCount(String type)
			{
				return this.service.getCount(type);
			}
		}

		public static interface TPIService
		{
			public int getCount(String type);
		}

		@Component
		public static class TPIService1 implements TPIService
		{
			public static final String TYPE = TPIService1.class.getName();

			private static int count = 0;

			public TPIService1()
			{
				super();
			}

			@Validity("isValid")
			@Override
			public int getCount(String type)
			{
				return count;
			}

			boolean isValid(String type)
			{
				return TYPE.equals(type);
			}
		}

		@Component
		@Scope("prototype")
		public static class TPIService2 implements TPIService
		{
			public static final String TYPE = TPIService2.class.getName();

			private static int count = 0;

			public TPIService2()
			{
				super();

				count += 1;
			}

			@Validity("isValid")
			@Override
			public int getCount(String type)
			{
				return count;
			}

			boolean isValid(String type)
			{
				return TYPE.equals(type);
			}
		}

		@Component
		@Aspect
		public static class TPIService3Aspect
		{
			@Pointcut("execution(* org.ximplementation.spring.ImplementeeBeanCreationPostProcessorTest$TestPrototypeImplementorBean_AopOfJdkProxy$TPIService.getCount(..))")
			private void testPointcut()
			{
			}

			@org.aspectj.lang.annotation.Before("testPointcut()")
			public void beforeAspect(JoinPoint jp) throws Throwable
			{
			}
		}
	}

	@Component
	public static class Controller
	{
		@Autowired
		private Service service;

		public Controller()
		{
			super();
		}

		public Controller(Service tservice)
		{
			super();
			this.service = tservice;
		}

		public Service getService()
		{
			return service;
		}

		public void setService(Service service)
		{
			this.service = service;
		}

		public String handle(Number number)
		{
			return this.service.handle(number);
		}
	}

	@Component
	public static class Controller1
	{
		@Autowired
		private Service service;

		public Controller1()
		{
			super();
		}

		public Service getService()
		{
			return service;
		}

		public void setService(Service service)
		{
			this.service = service;
		}
	}

	@Component
	public static interface Service
	{
		String handle(Number number);
	}

	@Component
	public static class ServiceImpl0 implements Service
	{
		public static final String MY_RE = ServiceImpl0.class.getName();

		@Override
		public String handle(Number number)
		{
			return MY_RE;
		}
	}

	@Component
	public static class ServiceImpl1 implements Service
	{
		public static final String MY_RE = ServiceImpl1.class.getName();

		public static final Number MY_NUMBER = new Double(1.0D);

		@Validity("isValid")
		@Override
		public String handle(Number number)
		{
			return MY_RE;
		}

		public boolean isValid(Number number)
		{
			return MY_NUMBER.equals(number);
		}
	}

	@Component
	@Implementor(Service.class)
	public static class ServiceImpl2
	{
		public static final String MY_RE = ServiceImpl2.class.getName();

		@Implement("handle")
		public String handle(Integer number)
		{
			return MY_RE;
		}
	}

	@Component
	@Implementor(Service.class)
	public static class ServiceImpl3
	{
		public static final String MY_RE = ServiceImpl3.class.getName();

		@Implement("handle")
		public String handle(Float number)
		{
			return MY_RE;
		}
	}

	@Component
	@Aspect
	public static class MyAspect
	{
		public static final String PREFIX = MyAspect.class.getSimpleName();

		@Pointcut("execution(* org.ximplementation.spring.ImplementeeBeanCreationPostProcessorTest$Service.handle(..))")
		private void testPointcut()
		{
		}

		@org.aspectj.lang.annotation.Before("testPointcut()")
		public void beforeAspect(JoinPoint jp) throws Throwable
		{
			System.out.println("Before aspect execute");
		}

		@org.aspectj.lang.annotation.Around("testPointcut()")
		public Object aroundAspect(ProceedingJoinPoint jp) throws Throwable
		{
			String re = (String) jp.proceed();

			return PREFIX + re;
		}
	}
}
