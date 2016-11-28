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

import org.springframework.beans.factory.BeanFactory;

/**
 * Singleton bean holder.
 * <p>
 * It get the bean from the underline {@linkplain #getBeanFactory()} only the
 * first call to {@linkplain #getBean()}, cache it, and returns the same bean
 * for all afterwards calls.
 * </p>
 * <p>
 * Note that this class is thread-safe and can be accessed by multiple threads.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-25
 *
 */
public class SingletonBeanHolder extends BeanHolder
{
	private volatile Object singletonBean;

	public SingletonBeanHolder(BeanFactory beanFactory, String beanName,
			boolean peeling)
	{
		super(beanFactory, beanName, peeling);
	}

	@Override
	public Object getBean()
	{
		if (this.singletonBean != null)
			return this.singletonBean;
		else
		{
			Object bean = super.getBean();
			this.singletonBean = bean;

			return bean;
		}
	}
}
