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

import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * {@linkplain PreparedImplementorBeanHolderFactory} unit tests.
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-28
 *
 */
public class PreparedImplementorBeanHolderFactoryTest
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

	@SuppressWarnings("unchecked")
	@Test
	public void getImplementorBeansTest()
	{
		PreparedImplementorBeanHolderFactory factory = new PreparedImplementorBeanHolderFactory();
		factory.prepare(MyBeanA.class);
		factory.prepare(
				MyBeanB.class);
		
		BeanHolder beanHolderA0 = new BeanHolder(applicationContext,
				"preparedImplementorBeanHolderFactoryTest.MyBeanA",
				true);
		BeanHolder beanHolderA1 = new BeanHolder(applicationContext,
				"preparedImplementorBeanHolderFactoryTest.MyBeanA",
				true);
		BeanHolder beanHolderB = new BeanHolder(applicationContext,
				"preparedImplementorBeanHolderFactoryTest.MyBeanB",
				true);

		factory.add(MyBeanA.class, beanHolderA0);
		factory.add(MyBeanA.class, beanHolderA1);
		factory.add(MyBeanB.class, beanHolderB);
		
		Collection<MyBeanA> beanAs = factory.getImplementorBeans(MyBeanA.class);
		Collection<MyBeanB> beanBs = factory.getImplementorBeans(MyBeanB.class);

		assertThat(beanAs,
				Matchers.containsInAnyOrder(
						Matchers.hasToString(MyBeanA.class.getName()),
						Matchers.hasToString(MyBeanA.class.getName())));

		assertThat(beanBs, Matchers
				.contains(Matchers.hasToString(MyBeanB.class.getName())));
	}

	@Component
	public static class MyBeanA
	{
		@Override
		public String toString()
		{
			return getClass().getName();
		}
	}

	@Component
	public static class MyBeanB
	{
		@Override
		public String toString()
		{
			return getClass().getName();
		}
	}
}
