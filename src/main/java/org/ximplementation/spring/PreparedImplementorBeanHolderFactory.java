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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.beans.factory.BeanFactory;
import org.ximplementation.support.Implementation;
import org.ximplementation.support.PreparedImplementorBeanFactory;

/**
 * Bean holder supported {@linkplain PreparedImplementorBeanFactory}.
 * <p>
 * It can add {@linkplain ImplementorBeanHolder} objects for supporting Spring
 * prototype bean, and they will be unpacked in
 * {@linkplain #getImplementorBeans(Class)}.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-10
 *
 */
public class PreparedImplementorBeanHolderFactory
		extends PreparedImplementorBeanFactory
{
	public PreparedImplementorBeanHolderFactory(
			Implementation<?> implementation)
	{
		super(implementation);
	}

	public PreparedImplementorBeanHolderFactory(Set<Class<?>> implementors)
	{
		super(implementors);
	}

	/**
	 * Return if the given {@linkplain ImplementorBeanHolder} is acceptable.
	 * 
	 * @param implementorBeanHolder
	 *            The {@linkplain ImplementorBeanHolder} to be checked.
	 * @return {@code true} if yes, {@code false} if no.
	 */
	public boolean accept(ImplementorBeanHolder implementorBeanHolder)
	{
		return super.accept(implementorBeanHolder.getImplementor());
	}

	/**
	 * Add an {@linkplain ImplementorBeanHolder}.
	 * 
	 * @param implementorBeanHolder
	 *            The {@linkplain ImplementorBeanHolder} to be added.
	 * @return {@code true} if the {@linkplain ImplementorBeanHolder} is
	 *         acceptable, {@code false} if not.
	 */
	public boolean addImplementorBeanHolder(
			ImplementorBeanHolder implementorBeanHolder)
	{
		List<Object> implementorBeans = getImplementorBeansList(
				implementorBeanHolder.getImplementor());

		if (implementorBeans == null)
			return false;

		implementorBeans.add(implementorBeanHolder);

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> getImplementorBeans(Class<T> implementor)
	{
		List<Object> implementorBeans = getImplementorBeansList(implementor);

		if (implementorBeans == null)
			return null;

		List<Object> re = new ArrayList<Object>(implementorBeans.size());

		for (Object bean : implementorBeans)
		{
			if (bean instanceof ImplementorBeanHolder)
				re.add(((ImplementorBeanHolder) bean).getBean());
			else
				re.add(bean);
		}

		return (Collection<T>) re;
	}

	/**
	 * Holder for <i>implementor</i> bean.
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
	public static class ImplementorBeanHolder
	{
		public static final String JdkDynamicAopProxyName = "org.springframework.aop.framework.JdkDynamicAopProxy";
		
		protected static volatile Field springJdkProxyAdvisedField = null;

		private Class<?> implementor;

		private BeanFactory beanFactory;

		private String beanName;

		private boolean peel;

		public ImplementorBeanHolder()
		{
			super();
		}

		public ImplementorBeanHolder(Class<?> implementor,
				BeanFactory beanFactory, String beanName, boolean peel)
		{
			super();
			this.implementor = implementor;
			this.beanFactory = beanFactory;
			this.beanName = beanName;
			this.peel = peel;
		}

		/**
		 * Get the holding <i>implementor</i>.
		 * 
		 * @return
		 */
		public Class<?> getImplementor()
		{
			return implementor;
		}

		/**
		 * Set the holding <i>implementor</i>.
		 * 
		 * @param implementor
		 */
		public void setImplementor(Class<?> implementor)
		{
			this.implementor = implementor;
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
		public void setBeanFactory(BeanFactory beanFactory)
		{
			this.beanFactory = beanFactory;
		}

		/**
		 * Get the holding <i>implementor</i> bean name.
		 * 
		 * @return
		 */
		public String getBeanName()
		{
			return beanName;
		}

		/**
		 * Set the holding <i>implementor</i> bean name.
		 * 
		 * @param beanName
		 */
		public void setBeanName(String beanName)
		{
			this.beanName = beanName;
		}

		/**
		 * Return if {@linkplain #getBean()} should peel proxy bean to return
		 * the raw bean.
		 * 
		 * @return {@code true} if yes, {@code false} if no.
		 */
		public boolean isPeel()
		{
			return peel;
		}

		/**
		 * Set if {@linkplain #getBean()} should peel proxy bean to return the
		 * raw bean.
		 * 
		 * @param peel
		 *            {@code true} if yes, {@code false} if no.
		 */
		public void setPeel(boolean peel)
		{
			this.peel = peel;
		}

		/**
		 * Get bean from the underline {@linkplain #getBeanFactory()
		 * BeanFactory}.
		 * 
		 * @return
		 */
		public Object getBean()
		{
			Object bean = this.beanFactory.getBean(this.beanName);

			if (!peel)
				return bean;

			if (bean instanceof Proxy)
				bean = peelSpringJdkProxy((Proxy) bean);

			return bean;
		}

		/**
		 * Peel Spring JDK proxy object.
		 * 
		 * @param springProxy
		 * @return The not proxied raw bean object
		 * @throws PeelSpringProxyException
		 */
		protected Object peelSpringJdkProxy(Proxy springProxy)
				throws PeelSpringProxyException
		{
			InvocationHandler invocationHandler = Proxy
					.getInvocationHandler(springProxy);

			Class<?> invocationHandlerClass = invocationHandler.getClass();

			if (!JdkDynamicAopProxyName
					.equals(invocationHandlerClass.getName()))
				throw new PeelSpringProxyException(
						"Peeling is not supported, Proxy [" + springProxy
								+ "] is not created by ["
								+ JdkDynamicAopProxyName + "]");

			try
			{
				if (springJdkProxyAdvisedField == null)
				{
					springJdkProxyAdvisedField = invocationHandlerClass
							.getDeclaredField("advised");

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
	}
}
