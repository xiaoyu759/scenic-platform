package com.scenic.controller.admin;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    /**
     * 管理员登录
     * POST /api/admin/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");

            // 参数校验
            if (username == null || username.trim().isEmpty()) {
                return Result.error(StatusCode.PARAM_ERROR, "用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error(StatusCode.PARAM_ERROR, "密码不能为空");
            }

            Map<String, Object> result = adminService.adminLogin(username.trim(), password.trim());
            return Result.success("登录成功", result);

        } catch (RuntimeException e) {
            log.warn("管理员登录失败: {}", e.getMessage());
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("管理员登录异常: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "登录失败，请稍后重试");
        }
    }
}