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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.ximplementation.support.Implementation;
import org.ximplementation.support.ImplementationResolver;
import org.ximplementation.support.ImplementeeBeanBuilder;
import org.ximplementation.support.ImplementorManager;

/**
 * A {@code BeanPostProcessor} for creating dependency beans based on
 * <i>ximplementation</i>.
 * <p>
 * After adding the following configuration
 * </p>
 * <p>
 * <code>
 * &lt;bean
 * class="org.ximplementation.spring.ImplementeeBeanCreationPostProcessor"
 * /&gt;
 * </code>
 * </p>
 * <p>
 * to {@code applicationContext.xml}, your Spring project will be able to
 * support multiple dependency injection and more <i>ximplementation</i>
 * features.
 * </p>
 * <p>
 * This {@code BeanPostProcessor} will only handle dependencies matching all of
 * the following conditions:
 * </p>
 * <ul>
 * <li>The injected setter method or field is annotated with
 * {@linkplain Autowired} or {@code javax.inject.Inject};</li>
 * <li>The injected setter method or field is NOT annotated with
 * {@linkplain Qualifier} or {@code javax.inject.Named}.</li>
 * <li>There are more than one <i>implementor</i>s for the injected type in the
 * Spring context.</li>
 * </ul>
 * <p>
 * If matched, it will use {@linkplain CglibImplementeeBeanBuilder} to create
 * CGLIB <i>implementee</i> beans for dependency injection.
 * </p>
 * <p>
 * Examples :
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
 * 	&#64;Implement
 * 	public String handle(Integer number){...}
 * }
 * </pre>
 * <p>
 * The {@code Controller.service} will be injected successfully though there are
 * more than one instances, and its {@code handle} method invocation will be
 * delegated to {@code ServiceImplInteger} when the parameter type is
 * {@code Integer}, to {@code ServiceImplAnother} when the parameter is greater
 * than 0, to {@code ServiceImplDefault} otherwise.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-8-16
 *
 */
public class ImplementeeBeanCreationPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements PriorityOrdered, BeanFactoryAware
{
	private ImplementorManager implementorManager = new ImplementorManager();

	private Map<Class<?>, List<String>> implementorBeanNamesMap = new HashMap<Class<?>, List<String>>();

	private ImplementationResolver implementationResolver = new ImplementationResolver();

	private ImplementeeBeanBuilder implementeeBeanBuilder = new CglibImplementeeBeanBuilder();

	/** order, must be before AutowiredAnnotationBeanPostProcessor */
	private int order = Ordered.HIGHEST_PRECEDENCE;

	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * stores implementee beans created for specified type in
	 * {@link #postProcessPropertyValues(PropertyValues, PropertyDescriptor[], Object, String)}
	 * , used for all afterwards dependency injections.
	 */
	private ConcurrentHashMap<Class<?>, Object> initializedImplementeeBeans = new ConcurrentHashMap<Class<?>, Object>();

	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	private final Set<Class<? extends Annotation>> qualifierAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	@SuppressWarnings("unchecked")
	public ImplementeeBeanCreationPostProcessor()
	{
		super();

		this.autowiredAnnotationTypes.add(Autowired.class);
		ClassLoader cl = ImplementeeBeanCreationPostProcessor.class.getClassLoader();
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

	public ImplementeeBeanBuilder getImplementeeBeanBuilder()
	{
		return implementeeBeanBuilder;
	}

	public void setImplementeeBeanBuilder(
			ImplementeeBeanBuilder implementeeBeanBuilder)
	{
		this.implementeeBeanBuilder = implementeeBeanBuilder;
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
		this.initImplementorManagerAndImplementorBeanNamesMap();
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName)
			throws BeansException
	{
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

			// synchronization for this.implementorManager is not necessary, see
			// #initImplementorManagerAndImplementorBeanNamesMap() doc
			Set<Class<?>> implementors = this.implementorManager
					.get(propertyType);
			
			// only handle multiple implementors
			if (implementors == null || implementors.size() <= 1)
				continue;
			
			Object implementeeBean = this.initializedImplementeeBeans
					.get(propertyType);

			if (implementeeBean == null)
			{
				Implementation<?> implementation = this.implementationResolver
						.resolve(propertyType, implementors);

				PreparedImplementorBeanHolderFactory preparedImplementorBeanHolderFactory = new PreparedImplementorBeanHolderFactory(
						implementation);

				implementeeBean = this.implementeeBeanBuilder.build(
						implementation, preparedImplementorBeanHolderFactory);

				// AOP will be applied to this implementee bean, so the
				// ImplementorBeanFactory must return the raw implementor
				// beans, this is done in
				// #initPreparedImplementorBeanHolderFactory(...)
				implementeeBean = this.beanFactory
						.initializeBean(implementeeBean, generateImplementeeBeanName(propertyType));

				Object previous = this.initializedImplementeeBeans.putIfAbsent(propertyType, implementeeBean);
				
				// put by my thread, then do initialization
				if (previous == null || implementeeBean == previous)
				{
					initPreparedImplementorBeanHolderFactory(
							preparedImplementorBeanHolderFactory);

					this.beanFactory.registerResolvableDependency(propertyType,
							implementeeBean);
				}
				else
					implementeeBean = previous;
			}
		}

		return pvs;
	}

	/**
	 * Init {@linkplain #implementorManager} and
	 * {@linkplain #implementorBeanNamesMap}.
	 * <p>
	 * Synchronization for {@linkplain #implementorManager} and
	 * {@linkplain #implementorBeanNamesMap} are not necessary, because they are
	 * initialized in #setBeanFactory(BeanFactory) which happens before any
	 * other actions.
	 * </p>
	 * 
	 * @throws BeansException
	 */
	protected void initImplementorManagerAndImplementorBeanNamesMap()
			throws BeansException
	{
		String[] allBeanNames = beanFactory.getBeanDefinitionNames();
	
		for (int i = 0; i < allBeanNames.length; i++)
		{
			String beanName = allBeanNames[i];
	
			Class<?> beanClass = null;

			BeanDefinition beanDefinition = this.beanFactory
					.getBeanDefinition(beanName);
			String beanClassName = beanDefinition.getBeanClassName();
	
			try
			{
				beanClass = Class.forName(beanClassName);
			}
			catch (ClassNotFoundException e)
			{
				throw new CannotLoadBeanClassException(
						beanDefinition.getResourceDescription(), beanName,
						beanClassName, e);
			}

			List<String> implementorBeanNames = this.implementorBeanNamesMap
					.get(beanClass);
			if (implementorBeanNames == null)
			{
				implementorBeanNames = new ArrayList<String>();
				this.implementorBeanNamesMap.put(beanClass,
						implementorBeanNames);
			}
			implementorBeanNames.add(beanName);

			// Add itself, fix missing itself as an implementor when auto wired
			// class is not abstract
			this.implementorManager.addFor(beanClass, beanClass);
			this.implementorManager.add(beanClass);
		}
	}

	/**
	 * Init {@linkplain PreparedImplementorBeanHolderFactory}.
	 * 
	 * @param preparedImplementorBeanHolderFactory
	 */
	protected void initPreparedImplementorBeanHolderFactory(
			PreparedImplementorBeanHolderFactory preparedImplementorBeanHolderFactory)
	{
		Set<Class<?>> implementors = preparedImplementorBeanHolderFactory
				.getAllImplementors();

		for (Class<?> implementor : implementors)
		{
			// synchronization for this.implementorBeanNamesMap is not
			// necessary, see
			// #initImplementorManagerAndImplementorBeanNamesMap() doc
			List<String> implementorBeanNames = this.implementorBeanNamesMap
					.get(implementor);

			for (String implementorBeanName : implementorBeanNames)
			{
				BeanDefinition beanDefinition = this.beanFactory
						.getBeanDefinition(implementorBeanName);

				BeanHolder implementorBeanHolder = null;

				if (beanDefinition.isPrototype())
				{
					implementorBeanHolder = new BeanHolder(
							this.beanFactory, implementorBeanName,
							true);
				}
				else
				{
					implementorBeanHolder = new SingletonBeanHolder(
							this.beanFactory, implementorBeanName, true);
				}

				preparedImplementorBeanHolderFactory.add(implementor,
						implementorBeanHolder);
			}
		}
	}

	/**
	 * Returns property type for <i>ximplementation</i>, {@code null} if not
	 * valid.
	 * 
	 * @param beanClass
	 * @param pd
	 * @return
	 */
	protected Class<?> getPropertyTypeForXImplementation(Class<?> beanClass, PropertyDescriptor pd)
	{
		// write method
		Method writeMethod = pd.getWriteMethod();
		if (writeMethod != null && !isQualified(writeMethod))
		{
			Annotation autowiredAnno = findAutowiredAnnotation(writeMethod);
	
			if (autowiredAnno != null)
				return writeMethod.getParameterTypes()[0];
		}
	
		// Field
		Field field = findFieldByName(beanClass, pd.getName());
	
		if (field == null || isQualified(field))
			return null;
	
		Annotation autowiredAnno = findAutowiredAnnotation(field);
	
		return (autowiredAnno == null ? null : field.getType());
	}

	/**
	 * Generate the name of <i>implementee</i> bean.
	 * 
	 * @param implementee
	 * @return
	 */
	protected String generateImplementeeBeanName(Class<?> implementee)
	{
		return implementee.getClass().getName();
	}

	/**
	 * Returns if the element is qualified.
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
	 * Find {@code Field} by name.
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 */
	protected Field findFieldByName(Class<?> clazz, String fieldName)
	{
		while (clazz != null && !Object.class.equals(clazz))
		{
			Field field = null;

			try
			{
				field = clazz.getDeclaredField(fieldName);
			}
			catch (NoSuchFieldException e)
			{
			}

			if (field != null)
				return field;

			clazz = clazz.getSuperclass();
		}

		return null;
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
