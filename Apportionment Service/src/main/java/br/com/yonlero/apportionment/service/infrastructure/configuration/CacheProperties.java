package br.com.yonlero.apportionment.service.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(value = "spring.redis")
public class CacheProperties {
    private String host;
    private Integer port;
    private String password;
}