[英文](README.md) | [中文](README_zh-CN.md)

# ximplementation-spring
Ximplementation-spring是一个将<i>ximplementation</i>整合至Spring的库，使Spring能够支持多实例依赖注入和更多<i>ximplementation</i>特性。

类`org.ximplementation.spring.ImplementeeBeanCreationPostProcessor`是一个用于创建<i>ximplementation</i>依赖实例的`BeanPostProcessor`。

简单地将如下内容

`<bean class="org.ximplementation.spring.ImplementeeBeanCreationPostProcessor"/>`

添加至`applicationContext.xml`中，你的Spring工程将能够支持多实例依赖注入和更多<i>ximplementation</i>特性。

## 示例
你可以像下面这样编写Spring组件：

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

虽然有多个`Service`实例，变量`Controller.service`仍将会被成功注入。对于它的`handle`方法调用，如果参数类型是`Integer`，将被路由至`ServiceImplInteger`，如果参数值大于`0`，将被路由至`ServiceImplAnother`，否则，将被路由至`ServiceImplDefault`。