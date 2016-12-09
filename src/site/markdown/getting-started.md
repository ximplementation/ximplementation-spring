#Getting started
The class `org.ximplementation.spring.ImplementeeBeanCreationPostProcessor` is a `BeanPostProcessor` for creating <i>ximplementation</i> dependency beans in Spring, makes Spring support multiple dependency injection and more <i>ximplementation</i> features.

## First
Add the following content

`<bean class="org.ximplementation.spring.ImplementeeBeanCreationPostProcessor"/>`

to your `applicationContext.xml`.

## Second
Write Spring components with multiple dependencies and <i>ximplementation</i> style:

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

The `Controller.service` will be injected successfully though there are two instances, and its `handle` method invocation will be delegated to `ServiceImplInteger` when the parameter type is `Integer`, to `ServiceImplAnother` when the parameter is greater than `0`, to `ServiceImplDefault` otherwise.
