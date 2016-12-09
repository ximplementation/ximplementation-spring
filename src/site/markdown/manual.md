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


## Dependency invocation

## Spring AOP compatibility