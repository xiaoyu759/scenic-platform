package com.scenic.controller.visitor;

import com.scenic.common.Result;
import com.scenic.common.StatusCode;
import com.scenic.dto.OrderDTO;
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
@RequestMapping("/api/visitor/ticket")
@RequiredArgsConstructor
public class VisitorTicketController {

    private final VisitorService visitorService;

    @GetMapping("/types")
    public Result<Map<String, Object>> getTicketTypes() {
        try {
            Map<String, Object> ticketTypes = visitorService.getTicketTypes();
            return Result.success(ticketTypes);
        } catch (Exception e) {
            log.error("获取票种失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取票种失败");
        }
    }

    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(
            HttpServletRequest request,
            @Valid @RequestBody OrderDTO orderDTO) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            Map<String, Object> result = visitorService.createOrder(userId, orderDTO);
            return Result.success("订单创建成功", result);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("创建订单失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "创建订单失败");
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getOrderList(
            HttpServletRequest request,
            @RequestParam(required = false) String status) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            List<Map<String, Object>> orders = visitorService.getOrderList(userId, status);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取订单列表失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "获取订单列表失败");
        }
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancelOrder(HttpServletRequest request, @PathVariable Long id) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            visitorService.cancelOrder(userId, id);
            return Result.success("订单已取消", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("取消订单失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "取消订单失败");
        }
    }

    /**
     * 游客使用门票（核销）
     * PUT /api/visitor/ticket/use/{id}
     */
    @PutMapping("/use/{id}")
    public Result<?> useTicket(HttpServletRequest request, @PathVariable Long id) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(StatusCode.UNAUTHORIZED, "未登录");
            }
            visitorService.useTicket(userId, id);
            return Result.success("门票已使用", null);
        } catch (RuntimeException e) {
            return Result.error(StatusCode.PARAM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("使用门票失败: ", e);
            return Result.error(StatusCode.SERVER_ERROR, "使用门票失败");
        }
    }
}