package com.lhf.feign.hystrix.stream;

import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.binding.BindingTargetFactory;
import org.springframework.cloud.stream.binding.SubscribableChannelBindingTargetFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2018/1/8.
 * 注意topicName格式 ：j_(server-Name)_fallback_message
 */

public class FeignHystrixStreamChannelFactory {

    private SubscribableChannelBindingTargetFactory channelFactory;

    private BindingService bindingService;

    private MessageChannel inputChannel;

    private Map<String, MessageChannel> outputChannels = new HashMap<>();

    // 消息通道前缀
    private static final String CHANNEL_PREFIX = "j_";

    // 消息通道后缀
    private static final String CHANNEL_SUFFIX = "_fallback_message";

    public FeignHystrixStreamChannelFactory(BindingTargetFactory bindingTargetFactory, BindingService bindingService) {
        Assert.isTrue(bindingTargetFactory instanceof SubscribableChannelBindingTargetFactory
                , "bindingTargetFactory must not null");
        Assert.notNull(bindingService, "bindingService must not null");
        this.channelFactory = (SubscribableChannelBindingTargetFactory)bindingTargetFactory;
        this.bindingService = bindingService;
    }

    public MessageChannel getInputChannel(String serviceName) {
        if(null != inputChannel) {
            return inputChannel;
        }
        String channelName = getFallbackChannelName(serviceName);
        inputChannel = channelFactory.createInput(channelName);
        bindingService.bindConsumer(inputChannel, channelName);
        return inputChannel;
    }

    public MessageChannel getOutputChannel(String serviceName) {
        String channelName = getFallbackChannelName(serviceName);
        if(outputChannels.containsKey(channelName)) {
            return outputChannels.get(channelName);
        }

        MessageChannel output = null;
        synchronized (FeignHystrixStreamChannelFactory.class) {
            if(!outputChannels.containsKey(channelName)) {
                output = channelFactory.createOutput(channelName);
                bindingService.bindProducer(output, channelName);
                outputChannels.putIfAbsent(channelName, output);
            }
        }

        return output;
    }

    private String getFallbackChannelName(String serviceName) {
        return CHANNEL_PREFIX + serviceName + CHANNEL_SUFFIX;
    }
}
