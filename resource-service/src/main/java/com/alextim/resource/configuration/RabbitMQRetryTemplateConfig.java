package com.alextim.resource.configuration;

import com.alextim.resource.factory.RetryTemplateFactory;
import com.alextim.resource.exception.TransientPublishException;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;

@Configuration
public class RabbitMQRetryTemplateConfig {
    @Bean
    public RetryTemplate rabbitMQRetryTemplate(
            RetryTemplateFactory retryTemplateFactory,
            RetryProperties retryProperties,
            @Qualifier("rabbitMQRetryPolicy") RetryPolicy retryPolicy
    ) {
        return retryTemplateFactory.create(
                retryProperties,
                retryPolicy);
    }

    @Bean
    public RetryPolicy rabbitMQRetryPolicy(RetryProperties properties) {
        return new SimpleRetryPolicy(
                properties.getMaxAttempts(),
                Map.of(
                        TransientPublishException.class, true,
                        AmqpConnectException.class, true,
                        AmqpIOException.class, true,
                        AmqpTimeoutException.class, true
                ),
                true
        );
    }
}
