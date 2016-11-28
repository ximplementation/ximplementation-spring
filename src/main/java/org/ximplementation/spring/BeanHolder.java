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
 * Bean holder.
 * <p>
 * Each call on {@linkplain #getBean()} method will call the
 * {@linkplain #getBeanFactory()}'s {@linkplain BeanFactory#getBean(String)}
 * method to return a bean, and it will peel proxy object if
 * {@linkplain #isPeeling()} is {@code true}.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-10
 *
 */
public class BeanHolder
{
	private BeanFactory beanFactory;

	private String beanName;

	private boolean peeling;

	/**
	 * Create an instance.
	 * 
	 * @param beanFactory
	 *            The Spring BeanFactory used for getting <i>implementor</i>
	 *            bean.
	 * @param beanName
	 *            The holden <i>implementor</i> bean name.
	 * @param peel
	 *            If peeling proxy bean to return the raw bean, {@code true} if
	 *            yes, {@code false} if no.
	 */
	public BeanHolder(BeanFactory beanFactory,
			String beanName, boolean peeling)
	{
		super();
		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.peeling = peeling;
	}

	/**
	 * Get the {@linkplain BeanFactory}.
	 * 
	 * @return
	 */
	public BeanFactory getBeanFactory()
	{
		return beanFactory;
	}

	/**
	 * Set the {@linkplain BeanFactory}.
	 * 
	 * @param beanFactory
	 */
	protected void setBeanFactory(BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;
	}

	/**
	 * Get the holden bean name.
	 * 
	 * @return
	 */
	public String getBeanName()
	{
		return beanName;
	}

	/**
	 * Set the holden bean name.
	 * 
	 * @param beanName
	 */
	protected void setBeanName(String beanName)
	{
		this.beanName = beanName;
	}

	/**
	 * Return if {@linkplain #getBean()} should peel proxy bean to return the
	 * raw bean.
	 * 
	 * @return {@code true} if yes, {@code false} if no.
	 */
	public boolean isPeeling()
	{
		return peeling;
	}

	/**
	 * Set if {@linkplain #getBean()} should peel proxy bean to return the raw
	 * bean.
	 * 
	 * @param peeling
	 */
	protected void setPeeling(boolean peeling)
	{
		this.peeling = peeling;
	}

	/**
	 * Get the holden bean.
	 * <p>
	 * It will try to peeling proxy bean and return the raw bean if
	 * {@linkplain #isPeeling()} is {@code true}.
	 * </p>
	 * 
	 * @return
	 */
	public Object getBean()
	{
		Object bean = this.beanFactory.getBean(this.beanName);

		if (!peeling)
			return bean;

		if (bean instanceof java.lang.reflect.Proxy)
			bean = peelSpringJdkProxy((java.lang.reflect.Proxy) bean);
		else if (bean instanceof net.sf.cglib.proxy.Proxy)
			bean = peelSpringCglibProxy((net.sf.cglib.proxy.Proxy) bean);

		return bean;
	}

	/**
	 * Peel Spring JDK proxy object.
	 * 
	 * @param jdkProxy
	 * @return The not proxied raw bean object
	 * @throws ProxyPeelingException
	 */
	protected Object peelSpringJdkProxy(java.lang.reflect.Proxy jdkProxy)
			throws ProxyPeelingException
	{
		return ProxyUtil.peelSpringJdkProxy(jdkProxy);
	}

	/**
	 * Peel Spring CGLIB proxy object.
	 * 
	 * @param cglibProxy
	 * @return The not proxied raw bean object
	 * @throws ProxyPeelingException
	 */
	protected Object peelSpringCglibProxy(net.sf.cglib.proxy.Proxy cglibProxy)
			throws ProxyPeelingException
	{
		return ProxyUtil.peelSpringCglibProxy(cglibProxy);
	}
}
