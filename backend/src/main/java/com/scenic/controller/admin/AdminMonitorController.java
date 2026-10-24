package com.scenic.controller.admin;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/monitor")
@RequiredArgsConstructor
public class AdminMonitorController {

    private final AdminService adminService;

    /**
     * 今日数据总览
     * GET /api/admin/monitor/today
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getMonitorToday() {
        try {
            Map<String, Object> data = adminService.getMonitorToday();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取今日数据失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }

    /**
     * 景点实时数据
     * GET /api/admin/monitor/spots
     */
    @GetMapping("/spots")
    public Result<List<Map<String, Object>>> getMonitorSpots() {
        try {
            List<Map<String, Object>> spots = adminService.getMonitorSpots();
            return Result.success(spots);
        } catch (Exception e) {
            log.error("获取景点实时数据失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }

    /**
     * 入园趋势
     * GET /api/admin/monitor/trend
     * @param type daily-日趋势 monthly-月趋势
     */
    @GetMapping("/trend")
    public Result<Object> getMonitorTrend(@RequestParam(defaultValue = "daily") String type) {
        try {
            Object trend = adminService.getMonitorTrend(type);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取趋势数据失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }

    /**
     * 热力图数据
     * GET /api/admin/monitor/heatmap
     */
    @GetMapping("/heatmap")
    public Result<List<Map<String, Object>>> getHeatmapData() {
        try {
            List<Map<String, Object>> data = adminService.getHeatmapData();
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取热力图数据失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }
}