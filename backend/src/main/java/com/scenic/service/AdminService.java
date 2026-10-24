package com.scenic.service;

import com.scenic.common.PageResult;
import com.scenic.entity.ScenicInfo;
import com.scenic.entity.ScenicSpot;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {

    /**
     * 管理员登录
     */
    Map<String, Object> adminLogin(String username, String password);


    // ========== 监控大屏 ==========
    Map<String, Object> getMonitorToday();
    List<Map<String, Object>> getMonitorSpots();
    Object getMonitorTrend(String type);
    List<Map<String, Object>> getHeatmapData();

    // ========== 数据分析 ==========
    Map<String, Object> getRevenueStats(String type, String startDate, String endDate);
    Map<String, Object> getTicketSales();
    Map<String, Object> getVisitorProfile();
    void exportExcel(String params, HttpServletResponse response);

    // ========== 景点管理 ==========
    Map<String, Object> getSpotList(Integer pageNo, Integer pageSize, String keyword, String category);
    void addSpot(ScenicSpot spot);
    void updateSpot(ScenicSpot spot);
    void deleteSpot(Long id);

    // ========== 订单管理 ==========
    PageResult<Map<String, Object>> getOrderList(Integer pageNo, Integer pageSize, String keyword, String status, String startDate, String endDate);
    void updateOrderStatus(Long orderId, String status);
    Map<String, Object> getOrderDetail(Long orderId);

    // ========== 景区设置 ==========
    ScenicInfo getScenicInfo();
    void updateScenicInfo(ScenicInfo scenicInfo);

    void changeAdminPassword(Long adminId, String oldPassword, String newPassword);
}