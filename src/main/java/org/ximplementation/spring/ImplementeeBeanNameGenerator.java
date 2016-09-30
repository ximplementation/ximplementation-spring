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

/**
 * Generator of <i>implementee</i> bean name in Spring.
 * 
 * @author earthangry@gmail.com
 * @date 2016-9-30
 *
 */
public interface ImplementeeBeanNameGenerator
{
	/**
	 * Generate the name of <i>implementee</i> bean.
	 * 
	 * @param implementee
	 * @param implementeeBean
	 * @return
	 */
	String generate(Class<?> implementee, Object implementeeBean);
}
