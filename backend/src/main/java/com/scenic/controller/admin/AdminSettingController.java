package com.scenic.controller.admin;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.entity.ScenicInfo;
import com.scenic.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/setting")
@RequiredArgsConstructor
public class AdminSettingController {

    private final AdminService adminService;

    @GetMapping("/scenic")
    public Result<ScenicInfo> getScenicInfo() {
        try {
            ScenicInfo scenicInfo = adminService.getScenicInfo();
            return Result.success(scenicInfo);
        } catch (Exception e) {
            log.error("获取景区信息失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取景区信息失败");
        }
    }

    @PutMapping("/scenic")
    public Result<?> updateScenicInfo(@RequestBody ScenicInfo scenicInfo) {
        try {
            if (scenicInfo.getId() == null) {
                return Result.error(StatusCode.PARAM_ERROR, "景区ID不能为空");
            }
            adminService.updateScenicInfo(scenicInfo);
            return Result.success("景区信息更新成功", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新景区信息失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "更新景区信息失败");
        }
    }

    @PutMapping("/password")
    public Result<?> changePassword(
            HttpServletRequest request,
            @RequestBody Map<String, String> params) {
        try {
            Long adminId = (Long) request.getAttribute("userId");
            if (adminId == null) {
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

            adminService.changeAdminPassword(adminId, oldPassword.trim(), newPassword.trim());
            return Result.success("密码修改成功", null);

        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("修改管理员密码失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "修改密码失败");
        }
    }
}