package com.scenic.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置类
 * 从 application.yml 读取 jwt 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT 签名密钥
     */
    private String secret;

    /**
     * 游客 Token 过期时间（毫秒）
     */
    private Long expiration;

    /**
     * 管理员 Token 过期时间（毫秒）
     */
    private Long adminExpiration;
}