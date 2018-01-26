package com.lhf.stream.kafka.enhance;

import org.springframework.cloud.stream.binding.SubscribableChannelBindingTargetFactory;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.messaging.SubscribableChannel;

/**
 * Created on 2018/1/25.
 */

public class ChannelBindingTargetFactoryWapper extends SubscribableChannelBindingTargetFactory {

    private SubscribableChannelBindingTargetFactory channelBindingTargetFactory;

    private BindingServiceProperties bindingServiceProperties;

    public ChannelBindingTargetFactoryWapper(
            SubscribableChannelBindingTargetFactory channelBindingTargetFactory,
            BindingServiceProperties bindingServiceProperties) {
        super(null);
        this.channelBindingTargetFactory = channelBindingTargetFactory;
        this.bindingServiceProperties = bindingServiceProperties;
    }

    @Override
    public SubscribableChannel createInput(String name) {
        SubscribableChannel channel = channelBindingTargetFactory.createInput(name);
        return new MessageChannelWapper(channel, getBindingProperties(name));
    }

    @Override
    public SubscribableChannel createOutput(String name) {
        SubscribableChannel channel = channelBindingTargetFactory.createOutput(name);
        return new MessageChannelWapper(channel, getBindingProperties(name));
    }

    private BindingProperties getBindingProperties(String name) {
        return bindingServiceProperties.getBindings().get(name);
    }
}
