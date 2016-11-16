package org.ximplementation.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ximplementation.support.ImplementorManager;

/**
 * Bean name managed {@linkplain ImplementorManager}.
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-16
 *
 */
public class BeanNameImplementorManager extends ImplementorManager
{
	private Map<Class<?>, String[]> implementorBeanNamesMap = new HashMap<Class<?>, String[]>();

	public BeanNameImplementorManager()
	{
		super();
	}

	public BeanNameImplementorManager(
			Map<Class<?>, Set<Class<?>>> implementorsMap)
	{
		super(implementorsMap);
	}

	/**
	 * Get <i>implementor</i> bean names map.
	 * 
	 * @return
	 */
	public Map<Class<?>, String[]> getImplementorBeanNamesMap()
	{
		return implementorBeanNamesMap;
	}

	/**
	 * Set <i>implementor</i> bean names map.
	 * 
	 * @param implementorBeanNamesMap
	 */
	public void setImplementorBeanNamesMap(
			Map<Class<?>, String[]> implementorBeanNamesMap)
	{
		this.implementorBeanNamesMap = implementorBeanNamesMap;
	}

	/**
	 * Set given <i>implementor</i> bean names.
	 * 
	 * @param implementor
	 * @param implementorBeanName
	 */
	public void setImplementorBeanNames(Class<?> implementor,
			String... implementorBeanName)
	{
		this.implementorBeanNamesMap.put(implementor, implementorBeanName);
	}

	/**
	 * Get given <i>implementor</i> bean names.
	 * 
	 * @param implementor
	 * @return
	 */
	public String[] getImplementorBeanNames(Class<?> implementor)
	{
		return this.implementorBeanNamesMap.get(implementor);
	}
}
