# ximplementation-spring
Ximplementation-spring is a library that lets you use <i>ximplementation</i> in Spring, makes Spring support multiple dependency injection and more <i>ximplementation</i> features.

The class `org.ximplementation.spring.ImplementeeBeanCreationPostProcessor` is a `BeanPostProcessor` for creating <i>ximplementation</i> dependency beans in Spring.

Simply add the following content

`<bean class="org.ximplementation.spring.ImplementeeBeanCreationPostProcessor"/>`

to `applicationContext.xml`, then your Spring project will be able to support multiple dependency injection and more <i>ximplementation</i> features.

## Example
You can write Spring components like this:

```java

	@Component
	public class Controller
	{
		@Autowired
		private Service service;
		
		public String handle(Number number)
		{
			return this.service.handle(number);
		}
	}
	
	public interface Service
	{
		String handle(Number number);
	}
	
	@Component
	public class ServiceImplDefault implements Service
	{
		public String handle(Number number){...}
	}
	
	@Component
	public class ServiceImplAnother implements Service
	{
		@Validity("isValid")
		public String handle(Number number){...}
	
		public boolean isValid(Number number){ return number.intValue() > 0; }
	}
	
	@Component
	@Implementor(Service.class)
	public class ServiceImplInteger
	{
		@Implement
		public String handle(Integer number){...}
	}

```

The `Controller.service` will be injected successfully though there are two instances, and its `handle` method invocation will be delegated to `ServiceImplInteger` when the parameter type is `Integer`, to `ServiceImplAnother` when the parameter is greater than `0`, to `ServiceImplDefault` otherwise.