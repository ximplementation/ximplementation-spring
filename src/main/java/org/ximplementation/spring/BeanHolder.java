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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.beans.factory.BeanFactory;

/**
 * Bean holder.
 * <p>
 * Each call on {@linkplain #getBean()} method will call the
 * {@linkplain #getBeanFactory()}'s {@linkplain BeanFactory#getBean(String)}
 * method to return a bean.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-10
 *
 */
public class BeanHolder
{
	protected static final String JdkDynamicAopProxyName = "org.springframework.aop.framework.JdkDynamicAopProxy";

	protected static final String Cglib2AopProxyName = "org.springframework.aop.framework.Cglib2AopProxy";

	protected static final String JdkDynamicAopProxyAdvisedSupportFieldName = "advised";

	protected static final String Cglib2AopProxyAdvisedSupportFieldName = "advised";

	protected static volatile Field springJdkProxyAdvisedField = null;

	protected static volatile Field cglib2AopProxyNameAdvisedField = null;

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
	 * Get the holden bean name.
	 * 
	 * @return
	 */
	public String getBeanName()
	{
		return beanName;
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

		// JDK Proxy
		if (bean instanceof Proxy)
			bean = peelSpringJdkProxy((Proxy) bean);
		// CGLIB Proxy
		else if (bean instanceof net.sf.cglib.proxy.Proxy)
			bean = peelSpringCglibProxy((net.sf.cglib.proxy.Proxy) bean);

		return bean;
	}

	/**
	 * Peel Spring JDK proxy object.
	 * 
	 * @param jdkProxy
	 * @return The not proxied raw bean object
	 * @throws PeelSpringProxyException
	 */
	protected Object peelSpringJdkProxy(Proxy jdkProxy)
			throws PeelSpringProxyException
	{
		InvocationHandler invocationHandler = Proxy
				.getInvocationHandler(jdkProxy);

		Class<?> invocationHandlerClass = invocationHandler.getClass();

		if (!JdkDynamicAopProxyName.equals(invocationHandlerClass.getName()))
			throw new PeelSpringProxyException(
					"Peeling is not supported, Proxy [" + jdkProxy
							+ "] is not created by [" + JdkDynamicAopProxyName
							+ "]");

		try
		{
			if (springJdkProxyAdvisedField == null)
			{
				springJdkProxyAdvisedField = invocationHandlerClass
						.getDeclaredField(
								JdkDynamicAopProxyAdvisedSupportFieldName);

				if (!springJdkProxyAdvisedField.isAccessible())
					springJdkProxyAdvisedField.setAccessible(true);
			}

			AdvisedSupport advisedSupport = (AdvisedSupport) springJdkProxyAdvisedField
					.get(invocationHandler);

			return advisedSupport.getTargetSource().getTarget();
		}
		catch (Exception e)
		{
			throw new PeelSpringProxyException(e);
		}
	}

	/**
	 * Peel Spring CGLIB proxy object.
	 * 
	 * @param cglibProxy
	 * @return The not proxied raw bean object
	 * @throws PeelSpringProxyException
	 */
	protected Object peelSpringCglibProxy(net.sf.cglib.proxy.Proxy cglibProxy)
			throws PeelSpringProxyException
	{
		net.sf.cglib.proxy.InvocationHandler invocationHandler = net.sf.cglib.proxy.Proxy
				.getInvocationHandler(cglibProxy);

		Class<?> invocationHandlerClass = invocationHandler.getClass();

		if (!Cglib2AopProxyName.equals(invocationHandlerClass.getName()))
			throw new PeelSpringProxyException(
					"Peeling is not supported, Proxy [" + cglibProxy
							+ "] is not created by [" + Cglib2AopProxyName
							+ "]");

		try
		{
			if (cglib2AopProxyNameAdvisedField == null)
			{
				cglib2AopProxyNameAdvisedField = invocationHandlerClass
						.getDeclaredField(
								Cglib2AopProxyAdvisedSupportFieldName);

				if (!cglib2AopProxyNameAdvisedField.isAccessible())
					cglib2AopProxyNameAdvisedField.setAccessible(true);
			}

			AdvisedSupport advisedSupport = (AdvisedSupport) cglib2AopProxyNameAdvisedField
					.get(invocationHandler);

			return advisedSupport.getTargetSource().getTarget();
		}
		catch (Exception e)
		{
			throw new PeelSpringProxyException(e);
		}
	}
}
