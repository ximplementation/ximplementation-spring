# Manual

## Requirement
JDK : 1.6+

Spring Framework : 3.0+

cglib : 2.1+

ximplementation : 0.1.0+

## Integration
The [ImplementeeBeanCreationPostProcessor](apidocs/org/ximplementation/spring/ImplementeeBeanCreationPostProcessor.html) is a Spring `BeanPostProcessor`, it is the core class for integrating <i>ximplementation</i> into Spring.

Simply add the following content

`<bean class="org.ximplementation.spring.ImplementeeBeanCreationPostProcessor"/>`

to your `applicationContext.xml`, then you will be able to write <i>ximplementation</i> featured beans in your Spring project.

## Dependency creation
The [ImplementeeBeanCreationPostProcessor](apidocs/org/ximplementation/spring/ImplementeeBeanCreationPostProcessor.html) will only handle dependencies matching all of the following conditions: 
* The injected setter method or field is annotated with `org.springframework.beans.factory.annotation.Autowired` or `javax.inject.Inject`;
* The injected setter method or field is NOT annotated with `org.springframework.beans.factory.annotation.Qualifier` nor `javax.inject.Named` nor `javax.annotation.Resource`. 
* There are more than one <i>implementor</i>s for the injected type in the Spring context.

If matched, it will use [CglibImplementeeBeanBuilder](apidocs/org/ximplementation/spring/CglibImplementeeBeanBuilder.html) to create CGLIB <i>implementee</i> beans for dependency injection.

Note that it will create only one dependency bean for a injected type in the whole Spring context, and the bean is only used for dependency injection, you can not get it through `BeanFactory.getBean(...)` methods.


## Dependency invocation

## Spring AOP compatibility