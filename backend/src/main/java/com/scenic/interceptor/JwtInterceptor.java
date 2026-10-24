package com.scenic.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scenic.common.Result;
import com.scenic.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        log.info("拦截请求: {}", path);

        // 1. 获取 Token
        String authHeader = request.getHeader("Authorization");
        log.info("收到的 Authorization: {}", authHeader);

        // 2. 检查 Token 是否存在
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "未登录或Token格式错误");
            return false;
        }

        // 3. 去除 "Bearer " 前缀
        String token = authHeader.substring(7);
        log.info("解析后的 Token: {}", token);

        // 4. 验证 Token 是否有效（打印验证结果）
        boolean isValid = jwtUtil.validateToken(token);
        log.info("Token 验证结果: {}", isValid);

        if (!isValid) {
            sendError(response, 401, "Token无效或已过期");
            return false;
        }

        // 5. 解析 Token 获取用户信息
        Claims claims = jwtUtil.parseToken(token);
        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        // 6. 将用户信息存入请求属性
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        log.info("用户认证通过: userId={}, username={}, role={}", userId, username, role);
        return true;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);

        Result<?> result = Result.error(code, message);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}