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

import org.ximplementation.support.DefaultImplementeeMethodInvocationFactory;
import org.ximplementation.support.Implementation;
import org.ximplementation.support.ImplementeeBeanBuilder;
import org.ximplementation.support.ImplementeeMethodInvocationFactory;
import org.ximplementation.support.ImplementorBeanFactory;
import org.ximplementation.support.ProxyImplementeeBeanBuilder;
import org.ximplementation.support.ProxyImplementeeInvocationSupport;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * Implementee bean builder based on CGLIB.
 * <p>
 * It creating <i>implementee</i> bean of new CGLIB class and can work with
 * Spring AOP.
 * </p>
 * <p>
 * The JDK proxy <i>implementee</i> bean created by
 * {@linkplain ProxyImplementeeBeanBuilder} is not applicable in Spring, because
 * it can not work with Spring AOP.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-9-30
 *
 */
public class CglibImplementeeBeanBuilder implements ImplementeeBeanBuilder
{
	private ImplementeeMethodInvocationFactory implementeeMethodInvocationFactory;

	public CglibImplementeeBeanBuilder()
	{
		super();
		this.implementeeMethodInvocationFactory = new DefaultImplementeeMethodInvocationFactory();
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
	 * Build cglib based <i>implementee</i> bean.
	 * 
	 * @param implementation
	 * @param implementorBeanFactory
	 * @return
	 */
	protected Object doBuild(
			Implementation<?> implementation,
			ImplementorBeanFactory implementorBeanFactory)
	{
		InvocationHandler invocationHandler = new CglibImplementeeBeanInvocationHandler(
				implementation, implementorBeanFactory,
				implementeeMethodInvocationFactory);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(implementation.getImplementee());
		enhancer.setCallback(invocationHandler);

		return enhancer.create();
	}

	protected static class CglibImplementeeBeanInvocationHandler extends
			ProxyImplementeeInvocationSupport implements InvocationHandler
	{
		public CglibImplementeeBeanInvocationHandler()
		{
			super();
		}

		public CglibImplementeeBeanInvocationHandler(
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

			return super.invoke(method, args);
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [implementee="
					+ implementation.getClass().getName() + "]";
		}
	}
}
