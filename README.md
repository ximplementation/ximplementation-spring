# ximplementation-spring
Ximplementation-spring lets you use ximplementation in Spring.

The class `org.ximplementation.spring.ProxyImplementeeBeanCreationPostProcessor` is a Spring `BeanPostProcessor` for creating ximplementation beans.

Simply add the following content

`<bean class="org.ximplementation.spring.ProxyImplementeeBeanCreationPostProcessor"/>`

to you `applicationContext.xml`, then your Spring project will support multiple dependency injection and more ximplementation features.

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
		@Implement("handle")
		public String handle(Integer number){...}
	}

```

The `Controller.service` will be injected successfully though there are two instances, and its `handle` method invocation will be delegated to `ServiceImplAnother` when the parameter is greater than `0`, to `ServiceImplInteger` when the parameter type is `Integer`, and to `ServiceImplDefault` otherwise.