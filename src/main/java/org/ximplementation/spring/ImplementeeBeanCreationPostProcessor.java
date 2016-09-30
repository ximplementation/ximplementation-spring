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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.ximplementation.support.ImplementeeBeanBuilder;
import org.ximplementation.support.ImplementorManager;
import org.ximplementation.support.PreparedImplementorBeanFactory;

/**
 * A {@linkplain BeanPostProcessor} for creating dependency beans based on
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
 * to your {@code applicationContext.xml}, Spring will be able to support
 * multiple dependency injection and more <i>ximplementation</i> features.
 * </p>
 * <p>
 * For example :
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
 * <b> Attention ： </b>
 * </p>
 * <p>
 * Tis class will only handle dependencies matching all of the following
 * conditions:
 * </p>
 * <ul>
 * <li>The injected field or setter method must be annotated with
 * {@linkplain Autowired} or {@code javax.inject.Inject};</li>
 * <li>The injected field or setter method must NOT be annotated with
 * {@linkplain Qualifier} or {@code javax.inject.Named}.</li>
 * <li>There are more than one <i>implementor</i>s in the Spring context.</li>
 * </ul>
 * 
 * @author earthangry@gmail.com
 * @date 2016-8-16
 *
 */
public class ImplementeeBeanCreationPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements PriorityOrdered, BeanFactoryAware
{
	private ImplementorManager implementorManager = new ImplementorManager();

	private ImplementationResolver implementationResolver = new ImplementationResolver();

	private ImplementeeBeanBuilder implementeeBeanBuilder = new CglibImplementeeBeanBuilder();

	private ImplementeeBeanNameGenerator implementeeBeanNameGenerator = new ClassNameImplementeeBeanNameGenerator();

	/** order, must be before AutowiredAnnotationBeanPostProcessor */
	private int order = Ordered.LOWEST_PRECEDENCE - 3;

	private ConfigurableListableBeanFactory beanFactory;

	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	private final Set<Class<? extends Annotation>> qualifierAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	private List<PreparedImplementorBeanFactory> preparedImplementorBeanFactories = new ArrayList<PreparedImplementorBeanFactory>();

	private Map<Class<?>, Object> initializedImplementeeBeans = new HashMap<Class<?>, Object>();

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

	public ImplementeeBeanNameGenerator getImplementeeBeanNameGenerator()
	{
		return implementeeBeanNameGenerator;
	}

	public void setImplementeeBeanNameGenerator(
			ImplementeeBeanNameGenerator implementeeBeanNameGenerator)
	{
		this.implementeeBeanNameGenerator = implementeeBeanNameGenerator;
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

			// only handle multiple implementors
			if (implementors == null || implementors.size() <= 1)
				continue;

			Object implementeeBean = getInitializedImplementeebean(
					propertyType);

			if (implementeeBean == null)
			{
				Implementation<?> implementation = this.implementationResolver
						.resolve(propertyType, implementors);

				PreparedImplementorBeanFactory preparedImplementorBeanFactory = new PreparedImplementorBeanFactory(
						implementors);

				this.preparedImplementorBeanFactories
						.add(preparedImplementorBeanFactory);

				implementeeBean = this.implementeeBeanBuilder
						.build(implementation, preparedImplementorBeanFactory);

				implementeeBean = this.beanFactory.initializeBean(
						implementeeBean, generateImplementeeBeanName(
								propertyType, implementeeBean));

				setInitializedImplementeebean(propertyType, implementeeBean);

				this.beanFactory.registerResolvableDependency(propertyType,
					implementeeBean);
			}
		}

		return pvs;
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
		Method method = pd.getWriteMethod();

		if (method != null)
		{
			// must not be staitc or qualified
			if (Modifier.isStatic(method.getModifiers()) || isQualified(method))
				return null;

			Class<?> paramType = method.getParameterTypes()[0];

			// must be interface
			if (!paramType.isInterface())
				return null;

			Annotation autowiredAnno = findAutowiredAnnotation(method);

			// must be autowired annotated
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

		// must not be staitc or qualified, must be interface
		if (Modifier.isStatic(field.getModifiers()) || isQualified(field) || !field.getType().isInterface())
			return null;

		Annotation autowiredAnno = findAutowiredAnnotation(field);

		// must be autowired annotated
		return (autowiredAnno == null ? null : field.getType());
	}

	/**
	 * Generate the name of <i>implementee</i> bean.
	 * 
	 * @param implementee
	 * @param implementeeBean
	 * @return
	 */
	protected String generateImplementeeBeanName(Class<?> implementee,
			Object implementeeBean)
	{
		return this.implementeeBeanNameGenerator.generate(implementee,
				implementeeBean);
	}

	/**
	 * Get the <i>implementee</i> bean if initialized previous, {@code null}
	 * otherwise.
	 * 
	 * @param implementee
	 * @return
	 */
	protected Object getInitializedImplementeebean(Class<?> implementee)
	{
		return this.initializedImplementeeBeans.get(implementee);
	}

	/**
	 * Set <i>implementee</i> bean for using by {{
	 * {@link #getInitializedImplementeebean(Class)}.
	 * 
	 * @param implementee
	 * @param implementeeBean
	 */
	protected void setInitializedImplementeebean(Class<?> implementee,
			Object implementeeBean)
	{
		this.initializedImplementeeBeans.put(implementee, implementeeBean);
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
	 * Inflate {@linkplain ImplementorManager}。
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
