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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.ximplementation.Implement;
import org.ximplementation.Implementor;
import org.ximplementation.Validity;

/**
 * {@linkplain ProxyImplementeeBeanCreationPostProcessor}单元测试类。
 * 
 * @author earthangry@gmail.com
 * @date 2016年8月16日
 *
 */
public class ProxyImplementeeBeanCreationPostProcessorTest
{
	private ApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception
	{
		applicationContext = new ClassPathXmlApplicationContext(
				"classpath:ProxyImplementeeBeanCreationPostProcessorTest.xml");
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void test()
	{
		TController controller = applicationContext.getBean(TController.class);

		{
			String re = controller.handle(new Byte((byte) 5));

			Assert.assertEquals(TService0.MY_RE, re);
		}

		{
			String re = controller.handle(TService1.MY_NUMBER);

			Assert.assertEquals(TService1.MY_RE, re);
		}

		{
			String re = controller.handle(12345);

			Assert.assertEquals(TService2.MY_RE, re);
		}

		{
			String re = controller.handle(new Float(5.0F));

			Assert.assertEquals(TService3.MY_RE, re);
		}
	}

	@Component
	public static class TController
	{
		@Autowired
		private TService tservice;

		public TController()
		{
			super();
		}

		public TController(TService tservice)
		{
			super();
			this.tservice = tservice;
		}

		public TService getTservice()
		{
			return tservice;
		}

		public void setTservice(TService tservice)
		{
			this.tservice = tservice;
		}

		public String handle(Number number)
		{
			return this.tservice.handle(number);
		}
	}

	@Component
	public static interface TService
	{
		String handle(Number number);
	}

	@Component
	public static class TService0 implements TService
	{
		public static final String MY_RE = "RE-TService0";

		@Override
		public String handle(Number number)
		{
			return MY_RE;
		}
	}

	@Component
	public static class TService1 implements TService
	{
		public static final String MY_RE = "RE-TService1";

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
	@Implementor(TService.class)
	public static class TService2
	{
		public static final String MY_RE = "RE-TService2";

		@Implement("handle")
		public String handle(Integer number)
		{
			return MY_RE;
		}
	}

	@Component
	@Implementor(TService.class)
	public static class TService3
	{
		public static final String MY_RE = "RE-TService3";

		@Implement("handle")
		public String handle(Float number)
		{
			return MY_RE;
		}
	}

	@Component
	@Aspect
	public static class TAspect
	{
		@Pointcut("execution(* *..*.TService.*(..))")
		private void testPointcut()
		{
		}

		@org.aspectj.lang.annotation.Before("testPointcut()")
		public void testAspect(JoinPoint jp) throws Throwable
		{
			System.out.println("-------------test aspect---------------");
		}
	}
}
