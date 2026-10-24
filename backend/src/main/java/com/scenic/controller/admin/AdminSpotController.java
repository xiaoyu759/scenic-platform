package com.scenic.controller.admin;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.entity.ScenicSpot;
import com.scenic.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/spot")
@RequiredArgsConstructor
public class AdminSpotController {

    private final AdminService adminService;

    @GetMapping("/list")
    public Result<Map<String, Object>> getSpotManageList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        try {
            Map<String, Object> result = adminService.getSpotList(pageNo, pageSize, keyword, category);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取景点列表失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取数据失败");
        }
    }

    @PostMapping("/add")
    public Result<?> addSpot(@RequestBody ScenicSpot spot) {
        try {
            // 参数校验
            if (spot.getName() == null || spot.getName().trim().isEmpty()) {
                return Result.error(StatusCode.PARAM_ERROR, "景点名称不能为空");
            }
            adminService.addSpot(spot);
            return Result.success("添加成功", spot.getId());
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("添加景点失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "添加失败");
        }
    }

    @PutMapping("/update")
    public Result<?> updateSpot(@RequestBody ScenicSpot spot) {
        try {
            if (spot.getId() == null) {
                return Result.error(StatusCode.PARAM_ERROR, "景点ID不能为空");
            }
            adminService.updateSpot(spot);
            return Result.success("更新成功", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("更新景点失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteSpot(@PathVariable Long id) {
        try {
            adminService.deleteSpot(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("删除景点失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "删除失败");
        }
    }
}