package com.scenic.controller.visitor;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.dto.RouteDTO;
import com.scenic.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/visitor/route")
@RequiredArgsConstructor
public class VisitorRouteController {

    private final VisitorService visitorService;

    @PostMapping("/generate")
    public Result<List<Long>> generateRoute(@RequestBody Map<String, Object> params) {
        try {
            List<Long> routeIds = visitorService.generateRoute(params);
            return Result.success(routeIds);
        } catch (Exception e) {
            log.error("生成路线失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "生成路线失败");
        }
    }

    @PostMapping("/save")
    public Result<Long> saveRoute(
            HttpServletRequest request,
            @Valid @RequestBody RouteDTO routeDTO) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            Long routeId = visitorService.saveRoute(userId, routeDTO);
            return Result.success("路线保存成功", routeId);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("保存路线失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "保存路线失败");
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getRouteList(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            List<Map<String, Object>> routes = visitorService.getRouteList(userId);
            return Result.success(routes);
        } catch (Exception e) {
            log.error("获取路线列表失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取路线列表失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteRoute(HttpServletRequest request, @PathVariable Long id) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            visitorService.deleteRoute(userId, id);
            return Result.success("路线已删除", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("删除路线失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "删除路线失败");
        }
    }
}