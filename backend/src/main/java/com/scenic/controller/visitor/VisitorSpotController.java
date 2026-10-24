package com.scenic.controller.visitor;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.entity.ScenicSpot;
import com.scenic.service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/visitor/spot")
@RequiredArgsConstructor
public class VisitorSpotController {

    private final VisitorService visitorService;

    /**
     * 获取景点列表
     * GET /api/visitor/spot/list
     */
    @GetMapping("/list")
    public Result<List<ScenicSpot>> getSpotList() {
        try {
            List<ScenicSpot> spots = visitorService.getSpotList();
            return Result.success(spots);
        } catch (Exception e) {
            log.error("获取景点列表失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取景点列表失败");
        }
    }

    /**
     * 获取景点详情
     * GET /api/visitor/spot/{id}
     */
    @GetMapping("/{id}")
    public Result<ScenicSpot> getSpotDetail(@PathVariable Long id) {
        try {
            ScenicSpot spot = visitorService.getSpotDetail(id);
            return Result.success(spot);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("获取景点详情失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取景点详情失败");
        }
    }

    /**
     * 获取服务设施
     * GET /api/visitor/spot/facilities
     */
    @GetMapping("/facilities")
    public Result<Map<String, List<Map<String, Object>>>> getFacilities() {
        try {
            Map<String, List<Map<String, Object>>> facilities = visitorService.getFacilities();
            return Result.success(facilities);
        } catch (Exception e) {
            log.error("获取服务设施失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取服务设施失败");
        }
    }
}