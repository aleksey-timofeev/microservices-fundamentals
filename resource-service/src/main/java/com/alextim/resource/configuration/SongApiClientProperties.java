package com.alextim.resource.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "song-service.rest-api")
public class SongApiClientProperties {
    @NotBlank
    private String baseUrl;

    private TimeoutProperties timeouts = new TimeoutProperties();
}
