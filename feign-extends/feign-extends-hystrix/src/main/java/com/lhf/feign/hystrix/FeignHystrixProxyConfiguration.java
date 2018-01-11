package com.lhf.feign.hystrix;

import com.lhf.feign.hystrix.stream.FallbackMessageResolver;
import com.lhf.feign.hystrix.stream.FeignHystrixStreamChannelFactory;
import com.lhf.feign.hystrix.stream.FeignHystrixStreamInitializer;
import com.lhf.feign.hystrix.template.HystrixFallbackTemplate;
import com.lhf.feign.hystrix.template.MessageToServiceTemplate;
import com.lhf.feign.hystrix.template.RetryInJVMTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.binding.BindingTargetFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

/**
 * Created on 2018/1/6.
 */

@Configuration
public class FeignHystrixProxyConfiguration {

    @Bean
    public HystrixFallbackTemplate.Default getTemplateDefault() {
        return new HystrixFallbackTemplate.Default();
    }

    @Bean
    public RetryInJVMTemplate getFallbackRetry() {
        return new RetryInJVMTemplate();
    }

    @Configuration
    @ConditionalOnClass(FeignHystrixStreamInitializer.class)
    @EnableBinding
    public static class FeignHystrixStreamConfiguration {

        @Value("${spring.application.name}")
        private String serviceName;

        @Bean
        public FeignHystrixStreamChannelFactory getChannelFactory(BindingTargetFactory bindingTargetFactory,
                                                                  BindingService bindingService) {
            return new FeignHystrixStreamChannelFactory(bindingTargetFactory, bindingService);
        }

        @Bean
        public FallbackMessageResolver getMessageResolver(ApplicationContext applicationContext) {
            return new FallbackMessageResolver(applicationContext);
        }

        @Bean
        public FeignHystrixStreamInitializer importFeignHystrixStreamInitializer(
                @Lazy BinderAwareChannelResolver binderAwareChannelResolver,
                @Lazy MessageHandlerMethodFactory messageHandlerMethodFactory,
                FeignHystrixStreamChannelFactory channelFactory,
                FallbackMessageResolver messageResolver) {
            return new FeignHystrixStreamInitializer(
                    binderAwareChannelResolver,
                    messageHandlerMethodFactory,
                    channelFactory,
                    messageResolver,
                    serviceName);
        }

        @Bean
        public MessageToServiceTemplate getFallbackByMessage(
                FallbackMessageResolver messageResolver,
                FeignHystrixStreamChannelFactory channelFactory) {
            return new MessageToServiceTemplate(messageResolver, channelFactory);
        }
    }

}
