package com.alextim.resourceprocessor.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "resource-service.rest-api")
public class ResourceApiClientProperties {
    @NotBlank
    private String baseUrl;

    private TimeoutProperties timeouts = new TimeoutProperties();
}
