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

import org.springframework.aop.framework.AdvisedSupport;

/**
 * Utility for Spring proxy.
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-28
 *
 */
public class ProxyUtil
{
	/**
	 * The full class name of
	 * {@linkplain org.springframework.aop.framework.JdkDynamicAopProxy}.
	 */
	public static final String JdkDynamicAopProxyName = "org.springframework.aop.framework.JdkDynamicAopProxy";

	/**
	 * The {@linkplain org.springframework.aop.framework.AdvisedSupport advised}
	 * field name in
	 * {@linkplain org.springframework.aop.framework.JdkDynamicAopProxy} .
	 */
	public static final String JdkDynamicAopProxyAdvisedSupportFieldName = "advised";

	/**
	 * The full class name of
	 * {@linkplain org.springframework.aop.framework.Cglib2AopProxy}.
	 */
	public static final String Cglib2AopProxyName = "org.springframework.aop.framework.Cglib2AopProxy";

	/**
	 * The full class name of
	 * {@linkplain org.springframework.aop.framework.Cglib2AopProxy.DynamicAdvisedInterceptor}
	 * .
	 */
	public static final String Cglib2AopProxyDynamicAdvisedInterceptorName = "org.springframework.aop.framework.Cglib2AopProxy$DynamicAdvisedInterceptor";

	/**
	 * The {@linkplain org.springframework.aop.framework.AdvisedSupport advised}
	 * field name in
	 * {@linkplain org.springframework.aop.framework.Cglib2AopProxy.DynamicAdvisedInterceptor}
	 * .
	 */
	public static final String Cglib2AopProxyDynamicAdvisedInterceptorAdvisedSupportFieldName = "advised";

	private static volatile Field jdkProxyAdvisedField = null;

	private static volatile Field cglibProxyAdvisedField = null;

	/**
	 * Peel Spring JDK proxy object and returns the raw bean object.
	 * <p>
	 * The given proxy object should be created by
	 * {@linkplain {@linkplain org.springframework.aop.framework.JdkDynamicAopProxy}
	 * }.
	 * </p>
	 * 
	 * @param jdkProxy
	 * @return The no proxy raw bean object.
	 * @throws ProxyPeelingException
	 */
	public static Object peelSpringJdkProxy(java.lang.reflect.Proxy jdkProxy)
			throws ProxyPeelingException
	{
		java.lang.reflect.InvocationHandler invocationHandler = java.lang.reflect.Proxy
				.getInvocationHandler(jdkProxy);

		Class<?> invocationHandlerClass = invocationHandler.getClass();

		if (!JdkDynamicAopProxyName.equals(invocationHandlerClass.getName()))
			throw new ProxyPeelingException(
					"Peeling is not supported, Proxy [" + jdkProxy
							+ "] is not created by [" + JdkDynamicAopProxyName
							+ "]");

		try
		{
			if (jdkProxyAdvisedField == null)
			{
				jdkProxyAdvisedField = invocationHandlerClass
						.getDeclaredField(
								JdkDynamicAopProxyAdvisedSupportFieldName);

				if (!jdkProxyAdvisedField.isAccessible())
					jdkProxyAdvisedField.setAccessible(true);
			}

			AdvisedSupport advisedSupport = (AdvisedSupport) jdkProxyAdvisedField
					.get(invocationHandler);

			return advisedSupport.getTargetSource().getTarget();
		}
		catch (Exception e)
		{
			throw new ProxyPeelingException(e);
		}
	}

	/**
	 * Peel Spring CGLIB proxy object and returns the raw bean object.
	 * <p>
	 * The given proxy object should be created by
	 * {@linkplain org.springframework.aop.framework.Cglib2AopProxy}.
	 * </p>
	 * 
	 * @param cglibProxy
	 * @return The not proxied raw bean object
	 * @throws ProxyPeelingException
	 */
	public static Object peelSpringCglibProxy(
			net.sf.cglib.proxy.Factory cglibProxy)
			throws ProxyPeelingException
	{
		net.sf.cglib.proxy.Callback[] callbacks = cglibProxy.getCallbacks();

		if (callbacks == null || callbacks.length == 0)
			throw new ProxyPeelingException("Peeling is not supported, Proxy ["
					+ cglibProxy + "] may not be created by ["
					+ Cglib2AopProxyName + "], no callbacks is defined.");

		net.sf.cglib.proxy.Callback dynamicAdvisedInterceptor = null;

		for (net.sf.cglib.proxy.Callback callback : callbacks)
		{
			if (Cglib2AopProxyDynamicAdvisedInterceptorName
					.equals(callback.getClass().getName()))
			{
				dynamicAdvisedInterceptor = callback;
				break;
			}
		}

		if (dynamicAdvisedInterceptor == null)
			throw new ProxyPeelingException("Peeling is not supported, Proxy ["
					+ cglibProxy + "] may not be created by ["
					+ Cglib2AopProxyName + "], no ["
					+ Cglib2AopProxyDynamicAdvisedInterceptorName
					+ "] found in its callbacks.");

		try
		{
			if (cglibProxyAdvisedField == null)
			{
				cglibProxyAdvisedField = dynamicAdvisedInterceptor
						.getClass().getDeclaredField(
								Cglib2AopProxyDynamicAdvisedInterceptorAdvisedSupportFieldName);

				if (!cglibProxyAdvisedField.isAccessible())
					cglibProxyAdvisedField.setAccessible(true);
			}

			AdvisedSupport advisedSupport = (AdvisedSupport) cglibProxyAdvisedField
					.get(dynamicAdvisedInterceptor);

			return advisedSupport.getTargetSource().getTarget();
		}
		catch (Exception e)
		{
			throw new ProxyPeelingException(e);
		}
	}
}
