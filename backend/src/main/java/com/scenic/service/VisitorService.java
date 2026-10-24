package com.scenic.service;

import com.scenic.dto.*;
import com.scenic.entity.ScenicSpot;

import java.util.List;
import java.util.Map;

public interface VisitorService {

    // 登录
    Map<String, Object> login(LoginDTO loginDTO);

    // 注册
    void register(RegisterDTO registerDTO);

    // 获取用户信息
    Map<String, Object> getUserInfo(Long userId);

    // 更新用户信息
    void updateUserInfo(Long userId, UpdateUserDTO updateUserDTO);
    void changePassword(Long userId, String oldPassword, String newPassword);

    // 退出登录
    void logout(String token);

    // ========== 首页模块 ==========
    Map<String, Object> getHomeInfo();
    List<Map<String, Object>> getAnnouncements();

    // ========== 景点模块 ==========
    List<ScenicSpot> getSpotList();
    ScenicSpot getSpotDetail(Long id);
    Map<String, List<Map<String, Object>>> getFacilities();

    // ========== 购票模块 ==========
    Map<String, Object> getTicketTypes();
    void useTicket(Long userId, Long orderId);
    Map<String, Object> createOrder(Long userId, OrderDTO orderDTO);
    List<Map<String, Object>> getOrderList(Long userId, String status);
    void cancelOrder(Long userId, Long orderId);

    // ========== 路线模块 ==========
    List<Long> generateRoute(Map<String, Object> params);
    Long saveRoute(Long userId, RouteDTO routeDTO);
    List<Map<String, Object>> getRouteList(Long userId);
    void deleteRoute(Long userId, Long routeId);
}