package com.scenic.controller.admin;

import com.scenic.common.PageResult;
import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminService adminService;

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> getOrderList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            PageResult<Map<String, Object>> result = adminService.getOrderList(pageNo, pageSize, keyword, status, startDate, endDate);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单列表失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取订单列表失败");
        }
    }

    @PutMapping("/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String status = params.get("status");
            if (status == null || status.trim().isEmpty()) {
                return Result.error(StatusCode.PARAM_ERROR, "状态不能为空");
            }
            adminService.updateOrderStatus(id, status.trim());
            return Result.success("订单状态更新成功", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新订单状态失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "更新订单状态失败");
        }
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        try {
            Map<String, Object> orderDetail = adminService.getOrderDetail(id);
            return Result.success(orderDetail);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("获取订单详情失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取订单详情失败");
        }
    }
}