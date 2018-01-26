# spring-cloud-extends
对spring cloud组件的扩展 后续持续增加 <br> 

### feign-extends-hystrix [模板化fallback]
`@FeignClient`的原生的fallback需要一个继承自FeignClient的实现类。假如接口有10个方法，就需要实现这10个方法，然而其实可能只需要对某个方法专门编码
实现fallback，其他的可能只用打印个log就可以了。</br>
`FeignHystrixProxy`注解的虚类，可以不用任何编码，通过template对接口下每个方法进行代理实现模板化的fallback
```maven
<dependency>
    <groupId>com.lhf</groupId>
    <artifactId>feign-extends-hystrix</artifactId>
    <version>${version}</version>
</dependency>
```
###### ----------------
在mainApplication上加`@EnableFeignHystrixProxy` 并带上需要自动扫描的fallback包路径，自动创建需要FeignHystrixProxy的fallbak类。
对于未编码实现的fallback方法执行模板方法(打印日志，或者重试，或者其他操作) </br>
[打印日志的demo](https://github.com/lhf87/spring-cloud-extends/blob/master/feign-extends/feign-extends-hystrix/src/test/java/com/lhf/test/feign/app1/controller/FeignController.java)

### feign-extends-hystrix-stream-support [fallback的stream支持]
```maven
<dependency>
    <groupId>com.lhf</groupId>
    <artifactId>feign-extends-hystrix-stream-support</artifactId>
    <version>${version}</version>
</dependency>
```
Provider和Consumer端引入上面两个module，可以使用`MessageToServiceTemplate`将失败的内容发到消息中间件，然后消费端在异常重启后接收并消费feign的调用。
(需要在业务上实现幂等) </br>
[mq-fallback的demo](https://github.com/lhf87/spring-cloud-extends/tree/master/feign-extends/feign-extends-hystrix/src/test/java/com/lhf/test/feign)
(启动app1，调用`http://localhost:8111/hello-stream`，启动app2 会消费app1该方法里的feign调用)

### stream-extends-kafka [stream的kakfa组件扩展]
消息发送是异步的，不是每条消息都会立刻发送，会将消息存到buffer后续一次性发送。常规的使用方法都没有等到发送回调，业务方法就完结了，即使消息发送异常也没处理，默认的发送成功后也没有打印发送成功日志。</br>
现在暂时只做了ProducerListener，监听发送端消息回调,处理回调(打印日志) </br>
后续增加失败的重试
