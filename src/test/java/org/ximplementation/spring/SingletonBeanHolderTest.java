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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * {@linkplain SingletonBeanHolder} unit tests.
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-29
 *
 */
public class SingletonBeanHolderTest
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
		SingletonBeanHolder beanHolder = new SingletonBeanHolder(
				applicationContext, "singletonBeanHolderTest.MyBean", true);

		assertNull(beanHolder.getSingletonBean());

		Object bean = beanHolder.getBean();

		assertNotNull(beanHolder.getSingletonBean());
		Assert.assertTrue(bean == beanHolder.getSingletonBean());
	}

	@Component
	public static class MyBean
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
}
