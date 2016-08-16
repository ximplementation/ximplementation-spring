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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.ximplementation.support.Implementation;
import org.ximplementation.support.ImplementationResolver;
import org.ximplementation.support.ImplementorManager;
import org.ximplementation.support.PreparedImplementorBeanFactory;
import org.ximplementation.support.ProxyImplementeeBeanBuilder;

/**
 * 代理接口实例{@linkplain BeanPostProcessor}。
 * <p>
 * 此类用于支持Spring中<i>ximplementation</i>依赖代理实例的创建。
 * </p>
 * <p>
 * 在Spring配置文件中添加了：
 * </p>
 * <p>
 * &lt;bean
 * class="org.ximplementation.spring.ProxyImplementeeBeanCreationPostProcessor"
 * /&gt;
 * </p>
 * <p>
 * 内容之后，Spring将能够支持存在多个实例的依赖注入，并使其具备<i>ximplementation</i>特性。
 * </p>
 * <p>
 * 例如：
 * </p>
 * 
 * <pre>
 * &#64;Component
 * public class Controller
 * {
 * 	<b><i>
 * 	&#64;Autowired
 * 	private Service service;
 * 	</i></b>
 * 
 * 	public String handle(Number number)
 * 	{
 * 		return this.service.handle(number);
 * 	}
 * }
 * 
 * public interface Service
 * {
 * 	String handle(Number number);
 * }
 * 
 * &#64;Component
 * public class ServiceImplDefault implements Service
 * {
 * 	public String handle(Number number){...}
 * }
 * 
 * &#64;Component
 * public class ServiceImplAnother implements Service
 * {
 * 	&#64;Validity("isValid")
 * 	public String handle(Number number){...}
 * 
 * 	public boolean isValid(Number number){ return number.intValue() > 0; }
 * }
 * 
 * &#64;Component
 * &#64;Implementor(Service.class)
 * public class ServiceImplInteger
 * {
 * 	&#64;Implement("handle")
 * 	public String handle(Integer number){...}
 * }
 * </pre>
 * <p>
 * 注意，此类对于依赖代理实例的创建有如下限制条件：
 * </p>
 * <ul>
 * <li>字段/写方法有{@linkplain Autowired}（或者{@code javax.inject.Inject}）注解</li>
 * <li>字段/写方法无{@linkplain Qualifier}（或者{@code javax.inject.Named}）注解</li>
 * <li>字段/写方法的类型是接口</li>
 * </ul>
 * 
 * @author earthangry@gmail.com
 * @date 2016年8月16日
 *
 */
public class ProxyImplementeeBeanCreationPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements PriorityOrdered, BeanFactoryAware
{
	private ImplementorManager implementorManager = new ImplementorManager();

	private ImplementationResolver implementationResolver = new ImplementationResolver();

	private ProxyImplementeeBeanBuilder proxyImplementeeBeanBuilder = new ProxyImplementeeBeanBuilder();

	private List<PreparedImplementorBeanFactory> preparedImplementorBeanFactories = new ArrayList<PreparedImplementorBeanFactory>();

	/** 执行顺序，默认值为排在AutowiredAnnotationBeanPostProcessor之前 */
	private int order = Ordered.LOWEST_PRECEDENCE - 3;

	private ConfigurableListableBeanFactory beanFactory;

	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	private final Set<Class<? extends Annotation>> qualifierAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	@SuppressWarnings("unchecked")
	public ProxyImplementeeBeanCreationPostProcessor()
	{
		super();

		this.autowiredAnnotationTypes.add(Autowired.class);
		ClassLoader cl = ProxyImplementeeBeanCreationPostProcessor.class.getClassLoader();
		try
		{
			this.autowiredAnnotationTypes.add((Class<? extends Annotation>) cl.loadClass("javax.inject.Inject"));
		}
		catch (ClassNotFoundException ex)
		{
		}

		this.qualifierAnnotationTypes.add(Qualifier.class);
		try
		{
			this.qualifierAnnotationTypes.add((Class<? extends Annotation>) cl.loadClass("javax.inject.Named"));
		}
		catch (ClassNotFoundException ex)
		{
		}
	}

	public ImplementorManager getImplementorManager()
	{
		return implementorManager;
	}

	public void setImplementorManager(ImplementorManager implementorManager)
	{
		this.implementorManager = implementorManager;
	}

	public ImplementationResolver getImplementationResolver()
	{
		return implementationResolver;
	}

	public void setImplementationResolver(
			ImplementationResolver implementationResolver)
	{
		this.implementationResolver = implementationResolver;
	}

	public ProxyImplementeeBeanBuilder getProxyImplementeeBeanBuilder()
	{
		return proxyImplementeeBeanBuilder;
	}

	public void setProxyImplementeeBeanBuilder(
			ProxyImplementeeBeanBuilder proxyImplementeeBeanBuilder)
	{
		this.proxyImplementeeBeanBuilder = proxyImplementeeBeanBuilder;
	}

	public Set<Class<? extends Annotation>> getAutowiredAnnotationTypes()
	{
		return autowiredAnnotationTypes;
	}

	public Set<Class<? extends Annotation>> getQualifierAnnotationTypes()
	{
		return qualifierAnnotationTypes;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException
	{
		if (!(beanFactory instanceof ConfigurableListableBeanFactory))
			throw new IllegalArgumentException("ConfigurableListableBeanFactory required");

		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
		this.inflateImplementorManager(implementorManager, this.beanFactory);
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName)
			throws BeansException
	{
		for (PreparedImplementorBeanFactory PreparedImplementorBeanFactory : this.preparedImplementorBeanFactories)
		{
			PreparedImplementorBeanFactory.addImplementorBean(bean);
		}

		return super.postProcessAfterInstantiation(bean, beanName);
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException
	{
		if (pds == null || pds.length == 0)
			return pvs;

		Class<?> beanClass = bean.getClass();

		for (PropertyDescriptor pd : pds)
		{
			Class<?> propertyType = getPropertyTypeForXImplementation(beanClass, pd);

			if (propertyType == null)
				continue;

			Set<Class<?>> implementors = this.implementorManager.getImplementors(propertyType);

			if (implementors == null || implementors.isEmpty())
				continue;

			Implementation implementation = this.implementationResolver
					.resolve(propertyType, implementors);

			PreparedImplementorBeanFactory preparedImplementorBeanFactory = new PreparedImplementorBeanFactory(
					implementors);

			this.preparedImplementorBeanFactories
					.add(preparedImplementorBeanFactory);

			Object interfaceBean = this.proxyImplementeeBeanBuilder
					.build(implementation, preparedImplementorBeanFactory);

			this.beanFactory.registerResolvableDependency(propertyType, interfaceBean);
		}

		return pvs;
	}

	/**
	 * 获取属性类型。
	 * 
	 * @param beanClass
	 * @param pd
	 * @return
	 */
	protected Class<?> getPropertyTypeForXImplementation(Class<?> beanClass, PropertyDescriptor pd)
	{
		Method method = pd.getWriteMethod();

		if (method != null)
		{
			Class<?> paramType = method.getParameterTypes()[0];

			// 静态、添加了DI限定名、非接口的Method不应处理
			if (Modifier.isStatic(method.getModifiers()) || isQualified(method) || !paramType.isInterface())
				return null;

			Annotation autowiredAnno = findAutowiredAnnotation(method);

			if (autowiredAnno != null)
				return paramType;
		}

		String propertyName = pd.getName();
		Field field = null;

		try
		{
			field = beanClass.getDeclaredField(propertyName);
		}
		catch (NoSuchFieldException e)
		{
			return null;
		}

		// 静态、添加了DI限定名、非接口的Field不应处理
		if (Modifier.isStatic(field.getModifiers()) || isQualified(field) || !field.getType().isInterface())
			return null;

		Annotation autowiredAnno = findAutowiredAnnotation(field);

		if (autowiredAnno != null)
			return field.getType();

		return null;
	}

	/**
	 * 判断元素是否添加了qualifier注解。
	 * 
	 * @param annotatedElement
	 * @return
	 */
	protected boolean isQualified(AnnotatedElement annotatedElement)
	{
		for (Class<? extends Annotation> annotationClass : this.qualifierAnnotationTypes)
		{
			if (annotatedElement.isAnnotationPresent(annotationClass))
				return true;
		}

		return false;
	}

	/**
	 * 填充{@linkplain ImplementorManager}。
	 * 
	 * @param implementorManager
	 * @param beanFactory
	 * @throws BeansException
	 */
	protected void inflateImplementorManager(ImplementorManager implementorManager,
			ConfigurableListableBeanFactory beanFactory) throws BeansException
	{
		String[] allBeanNames = this.beanFactory.getBeanDefinitionNames();

		Class<?>[] allBeanClasses = new Class<?>[allBeanNames.length];

		for (int i = 0; i < allBeanNames.length; i++)
		{
			String eleBeanName = allBeanNames[i];
			Class<?> eleBeanClass;

			BeanDefinition eleBeanDefinition = this.beanFactory.getBeanDefinition(eleBeanName);
			String eleBeanClassName = eleBeanDefinition.getBeanClassName();

			try
			{
				eleBeanClass = Class.forName(eleBeanClassName);
			}
			catch (ClassNotFoundException e)
			{
				throw new CannotLoadBeanClassException(eleBeanDefinition.getResourceDescription(), eleBeanName,
						eleBeanClassName, e);
			}

			allBeanClasses[i] = eleBeanClass;
		}

		implementorManager.addImplementor(allBeanClasses);
	}

	/**
	 * Copy of [org.springframework.beans.factory.annotation.
	 * AutowiredAnnotationBeanPostProcessor.findAutowiredAnnotation]
	 */
	private Annotation findAutowiredAnnotation(AccessibleObject ao)
	{
		for (Class<? extends Annotation> type : this.autowiredAnnotationTypes)
		{
			Annotation annotation = ao.getAnnotation(type);
			if (annotation != null)
			{
				return annotation;
			}
		}
		return null;
	}
}