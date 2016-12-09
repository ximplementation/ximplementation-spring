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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.ximplementation.support.CachedImplementeeMethodInvocationFactory;
import org.ximplementation.support.Implementation;
import org.ximplementation.support.ImplementeeBeanBuilder;
import org.ximplementation.support.ImplementeeMethodInvocationFactory;
import org.ximplementation.support.ImplementorBeanFactory;
import org.ximplementation.support.ProxyImplementeeInvocationSupport;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * Implementee bean builder based on CGLIB.
 * <p>
 * It creating <i>implementee</i> bean of its sub class generated by CGLIB, with
 * {@linkplain CglibImplementeeInvocationHandler} as its invocation handler.
 * </p>
 * <p>
 * Note that the <i>implementee</i> bean also implements the
 * {@linkplain CglibImplementee} interface for token.
 * </p>
 * <p>
 * The <i>implementee</i> bean it created is a sub class of the
 * <i>implementee</i> but not a JDK {@linkplain Proxy}, so can work well with
 * Spring AOP when using JDK {@linkplain Proxy}. Note that this still have
 * problem when Spring AOP using CGLIB.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-9-30
 * @see CglibImplementee
 *
 */
public class CglibImplementeeBeanBuilder implements ImplementeeBeanBuilder
{
	private ImplementeeMethodInvocationFactory implementeeMethodInvocationFactory;

	public CglibImplementeeBeanBuilder()
	{
		super();
		this.implementeeMethodInvocationFactory = new CachedImplementeeMethodInvocationFactory();
	}

	public ImplementeeMethodInvocationFactory getImplementeeMethodInvocationFactory()
	{
		return implementeeMethodInvocationFactory;
	}

	public void setImplementeeMethodInvocationFactory(
			ImplementeeMethodInvocationFactory implementeeMethodInvocationFactory)
	{
		this.implementeeMethodInvocationFactory = implementeeMethodInvocationFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T build(Implementation<T> implementation,
			ImplementorBeanFactory implementorBeanFactory)
	{
		return (T) doBuild(implementation, implementorBeanFactory);
	}

	/**
	 * Build CGLIB <i>implementee</i> bean.
	 * 
	 * @param implementation
	 * @param implementorBeanFactory
	 * @return
	 */
	protected Object doBuild(
			Implementation<?> implementation,
			ImplementorBeanFactory implementorBeanFactory)
	{
		InvocationHandler invocationHandler = new CglibImplementeeInvocationHandler(
				implementation, implementorBeanFactory,
				implementeeMethodInvocationFactory);

		Enhancer enhancer = new Enhancer();
		enhancer.setInterfaces(new Class[] { CglibImplementee.class });
		enhancer.setSuperclass(implementation.getImplementee());
		enhancer.setCallback(invocationHandler);

		return enhancer.create();
	}

	/**
	 * The {@linkplain InvocationHandler} for CGLIB <i>implementee</i> bean.
	 * <p>
	 * Note that for all invocation on <i>implementee method</i>s which declared
	 * in {@linkplain Object}, it will call the handler itself's methods.
	 * </p>
	 * 
	 * @author earthangry@gmail.com
	 * @date 2016-9-30
	 *
	 */
	public static class CglibImplementeeInvocationHandler extends
			ProxyImplementeeInvocationSupport implements InvocationHandler
	{
		public CglibImplementeeInvocationHandler()
		{
			super();
		}

		public CglibImplementeeInvocationHandler(
				Implementation<?> implementation,
				ImplementorBeanFactory implementorBeanFactory,
				ImplementeeMethodInvocationFactory implementeeMethodInvocationFactory)
		{
			super(implementation, implementorBeanFactory,
					implementeeMethodInvocationFactory);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable
		{
			if (Object.class.equals(method.getDeclaringClass()))
				return method.invoke(this, args);

			return invoke(method, args);
		}
	}
}
