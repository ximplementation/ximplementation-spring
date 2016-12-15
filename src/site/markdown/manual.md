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

If one is matched, it will create an <i>implementee</i> bean for dependency injection as the following steps :

1. Find all <i>implementor</i>s of the dependency <i>implementee</i> in the Spring context, then build an `Implementation` intance;
2. Create an [EditableImplementorBeanHolderFactory](apidocs/org/ximplementation/spring/EditableImplementorBeanHolderFactory.html) for getting the <i>implementor</i> beans in  the Spring context when the <i>implementee method</i>s is invoking. The `EditableImplementorBeanHolderFactory` will be initialized with [BeanHolder](apidocs/org/ximplementation/spring/BeanHolder.html)s and [SingletonBeanHolder](apidocs/org/ximplementation/spring/SingletonBeanHolder.html)s which each holds an <i>implementor</i> bean name, and the `BeanHolder.getBean()` method will be called to get the actual <i>implementor</i> bean in `EditableImplementorBeanHolderFactory.getImplementorBeans(Class<?>)` invocation. 
The `BeanHolder` is used for holding prototype Spring beans, the `SingletonBeanHolder` is used for holding singleton Spring beans, and they will do peeling for returing the raw bean in the `getBean()` method.
3. Use the [CglibImplementeeBeanBuilder](apidocs/org/ximplementation/spring/CglibImplementeeBeanBuilder.html) with the `Implementation` and the `EditableImplementorBeanHolderFactory` above to create  an CGLIB <i>implementee</i> bean.
4. Initalize the CGLIB <i>implementee</i> bean by calling `org.springframework.beans.factory.config.AutowireCapableBeanFactory.initializeBean(Object, String)` method, all `BeanPostProcessor`s will process it for applying Spring AOP.
5. Register the CGLIB <i>implementee</i> bean by calling `org.springframework.beans.factory.config.ConfigurableListableBeanFactory.registerResolvableDependency(Class, Object)` method.

That's ok, the subsequent `org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor` will use the CGLIB <i>implementee</i> bean for dependency injection.

Note that the `ImplementeeBeanCreationPostProcessor ` will create only one CGLIB <i>implementee</i> bean for a injected type in the whole Spring context, and the bean is only used for dependency injection, you can not get it through `BeanFactory.getBean(...)` methods.

## Spring AOP compatibility
The [CglibImplementeeBeanBuilder](apidocs/org/ximplementation/spring/CglibImplementeeBeanBuilder.html) creates beans which is sub class of <i>implementee</i>s, this work well if Spring AOP is JDK Proxy , but can not work if Spring AOP is CGLIB. So, the [ImplementeeBeanCreationPostProcessor](apidocs/org/ximplementation/spring/ImplementeeBeanCreationPostProcessor.html) can work for `interface` and `class` <i>implementee</i>s if Spring AOP will not applied to them, but only can work for `interface` <i>implementee</i>s if Spring AOP will applied to them and only JDK Proxy AOP.