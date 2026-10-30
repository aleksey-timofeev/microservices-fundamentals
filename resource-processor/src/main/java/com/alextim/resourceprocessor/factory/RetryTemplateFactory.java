package com.alextim.resourceprocessor.factory;

import com.alextim.resourceprocessor.configuration.RetryProperties;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

public class RetryTemplateFactory {
    public RetryTemplate create(
            RetryProperties properties,
            RetryPolicy retryPolicy
    ) {
        var retryTemplate = new RetryTemplate();

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(createBackOffPolicy(properties));

        return retryTemplate;

    }

    private BackOffPolicy createBackOffPolicy(RetryProperties properties) {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();

        backOffPolicy.setInitialInterval(properties.getInitialInterval().toMillis());
        backOffPolicy.setMaxInterval(properties.getMaxInterval().toMillis());
        backOffPolicy.setMultiplier(properties.getMultiplier());

        return backOffPolicy;
    }
}
