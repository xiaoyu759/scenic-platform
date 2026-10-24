package com.scenic.controller.visitor;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.dto.LoginDTO;
import com.scenic.dto.RegisterDTO;
import com.scenic.dto.UpdateUserDTO;
import com.scenic.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/visitor")
@RequiredArgsConstructor
public class VisitorUserController {

    private final VisitorService visitorService;

    /**
     * 游客登录
     * POST /api/visitor/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> result = visitorService.login(loginDTO);
            return Result.success("登录成功", result);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        }
    }

    /**
     * 游客注册
     * POST /api/visitor/register
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            visitorService.register(registerDTO);
            return Result.success("注册成功", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        }
    }

    /**
     * 获取用户信息（需要登录）
     * GET /api/visitor/user/info
     */
    @GetMapping("/user/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            Map<String, Object> userInfo = visitorService.getUserInfo(userId);
            return Result.success(userInfo);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * 更新用户信息（需要登录）
     * PUT /api/visitor/user/info
     */
    @PutMapping("/user/info")
    public Result<?> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            visitorService.updateUserInfo(userId, updateUserDTO);
            return Result.success("更新成功", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        }
    }

    /**
     * 退出登录（需要登录）
     * POST /api/visitor/logout
     */
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                visitorService.logout(token);
            }
            return Result.success("退出成功", null);
        } catch (Exception e) {
            log.error("退出登录异常: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "退出失败");
        }
    }

    @PutMapping("/user/password")
    public Result<?> changePassword(
            HttpServletRequest request,
            @RequestBody Map<String, String> params) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error(StatusCode.PARAM_ERROR, "原密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error(StatusCode.PARAM_ERROR, "新密码不能为空");
            }
            if (newPassword.length() < 6) {
                return Result.error(StatusCode.PARAM_ERROR, "新密码长度至少6位");
            }

            visitorService.changePassword(userId, oldPassword.trim(), newPassword.trim());
            return Result.success("密码修改成功", null);

        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("修改用户密码失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "修改密码失败");
        }
    }
}