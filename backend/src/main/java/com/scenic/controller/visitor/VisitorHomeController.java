package com.scenic.controller.visitor;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.service.VisitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/visitor/home")
@RequiredArgsConstructor
public class VisitorHomeController {

    private final VisitorService visitorService;

    /**
     * 获取首页信息
     * GET /api/visitor/home/info
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getHomeInfo() {
        try {
            Map<String, Object> homeInfo = visitorService.getHomeInfo();
            return Result.success(homeInfo);
        } catch (Exception e) {
            log.error("获取首页信息失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取首页信息失败");
        }
    }

    /**
     * 获取公告列表
     * GET /api/visitor/home/announcements
     */
    @GetMapping("/announcements")
    public Result<List<Map<String, Object>>> getAnnouncements() {
        try {
            List<Map<String, Object>> announcements = visitorService.getAnnouncements();
            return Result.success(announcements);
        } catch (Exception e) {
            log.error("获取公告失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取公告失败");
        }
    }
}