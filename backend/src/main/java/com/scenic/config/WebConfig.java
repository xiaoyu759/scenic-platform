package com.scenic.config;

import com.scenic.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // ========== 游客端 - 不需要登录 ==========
                        "/api/visitor/login",
                        "/api/visitor/register",
                        "/api/visitor/home/info",
                        "/api/visitor/home/announcements",
                        "/api/visitor/spot/list",
                        "/api/visitor/spot/{id}",
                        "/api/visitor/spot/facilities",
                        "/api/visitor/ticket/types",
                        "/api/visitor/route/generate",     // 服务设施
                        // ========== 管理端 ==========
                        "/api/admin/login",
                        // ========== 测试 ==========
                        "/api/**/test"
                );
    }
}