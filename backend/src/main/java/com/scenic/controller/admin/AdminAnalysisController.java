package com.scenic.controller.admin;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.service.AdminService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/analysis")
@RequiredArgsConstructor
public class AdminAnalysisController {

    private final AdminService adminService;

    /**
     * 营收统计
     * GET /api/admin/analysis/revenue
     * @param type daily-日营收 monthly-月营收
     */
    @GetMapping("/revenue")
    public Result<Object> getRevenueStats(
            @RequestParam(defaultValue = "daily") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Object data = adminService.getRevenueStats(type, startDate, endDate);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取营收统计失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }

    /**
     * 票种销售统计
     * GET /api/admin/analysis/ticket
     */
    @GetMapping("/ticket")
    public Result<Object> getTicketSales() {
        try {
            Object data = adminService.getTicketSales();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取票种销售统计失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }

    /**
     * 游客画像
     * GET /api/admin/analysis/visitor
     */
    @GetMapping("/visitor")
    public Result<Object> getVisitorProfile() {
        try {
            Object data = adminService.getVisitorProfile();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取游客画像失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }

    /**
     * 导出Excel
     * GET /api/admin/analysis/export
     */
    @GetMapping("/export")
    public void exportExcel(@RequestParam(required = false) String params, HttpServletResponse response) {
        try {
            adminService.exportExcel(params, response);
        } catch (Exception e) {
            log.error("导出Excel失败: ", e);
        }
    }
}