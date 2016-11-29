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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ximplementation.support.EditableImplementorBeanFactory;

/**
 * Bean holder supported {@linkplain EditableImplementorBeanFactory}.
 * <p>
 * It can add {@linkplain BeanHolder} objects for supporting Spring beans, and
 * they will be unpacked in {@linkplain #getImplementorBeans(Class)}.
 * </p>
 * 
 * @author earthangry@gmail.com
 * @date 2016-11-10
 * @see BeanHolder
 */
public class EditableImplementorBeanHolderFactory
		extends EditableImplementorBeanFactory
{
	public EditableImplementorBeanHolderFactory()
	{
		super();
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
			if (bean instanceof BeanHolder)
				re.add(((BeanHolder) bean).getBean());
			else
				re.add(bean);
		}

		return (Collection<T>) re;
	}
}
