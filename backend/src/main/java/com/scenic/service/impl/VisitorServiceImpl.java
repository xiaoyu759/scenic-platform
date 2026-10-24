package com.scenic.service.impl;

import cn.hutool.core.util.IdUtil;
import com.scenic.common.StatusCode;
import com.scenic.dto.*;
import com.scenic.entity.*;
import com.scenic.exception.BusinessException;
import com.scenic.mapper.VisitorMapper;
import com.scenic.mapper.ScenicSpotMapper;
import com.scenic.service.VisitorService;
import com.scenic.utils.JwtUtil;
import com.scenic.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    private final VisitorMapper visitorMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final JwtUtil jwtUtil;

    /** 门票单价，取自 application.yml 的 app.single-ticket-price */
    @Value("${app.single-ticket-price:60}")
    private BigDecimal singleTicketPrice;

    /** 套票单价，取自 application.yml 的 app.combo-ticket-price */
    @Value("${app.combo-ticket-price:168}")
    private BigDecimal comboPrice;

    // ==================== 用户模块 ====================

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        Visitor visitor = visitorMapper.selectByUsername(loginDTO.getUsername());
        if (visitor == null) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!PasswordUtil.matches(loginDTO.getPassword(), visitor.getPassword())) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "用户名或密码错误");
        }
        // 存量明文口令校验通过后原地升级为 BCrypt，老账号无需强制重置
        if (!PasswordUtil.isEncoded(visitor.getPassword())) {
            visitor.setPassword(PasswordUtil.encode(loginDTO.getPassword()));
            visitorMapper.updatePassword(visitor);
            log.info("游客口令已升级为 BCrypt 存储: userId={}", visitor.getId());
        }

        String token = jwtUtil.generateVisitorToken(visitor.getId(), visitor.getUsername());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", visitor.getId());
        userInfo.put("username", visitor.getUsername());
        userInfo.put("nickname", visitor.getNickname());
        userInfo.put("phone", visitor.getPhone());
        userInfo.put("email", visitor.getEmail());
        userInfo.put("avatar", visitor.getAvatar());
        userInfo.put("gender", visitor.getGender());
        userInfo.put("age", visitor.getAge());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);
        return result;
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        if (visitorMapper.countByUsername(registerDTO.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        if (visitorMapper.countByPhone(registerDTO.getPhone()) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        if (!PasswordUtil.isStrongPassword(registerDTO.getPassword())) {
            throw new BusinessException("密码需至少 6 位且同时包含字母和数字");
        }

        Visitor visitor = new Visitor();
        visitor.setUsername(registerDTO.getUsername());
        visitor.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        visitor.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        visitor.setPhone(registerDTO.getPhone());
        visitor.setEmail(registerDTO.getEmail());
        visitor.setGender(0);
        visitor.setAge(0);

        visitorMapper.insert(visitor);
        log.info("用户注册成功: userId={}, username={}", visitor.getId(), visitor.getUsername());
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        Visitor visitor = visitorMapper.selectById(userId);
        if (visitor == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", visitor.getId());
        userInfo.put("username", visitor.getUsername());
        userInfo.put("nickname", visitor.getNickname());
        userInfo.put("phone", visitor.getPhone());
        userInfo.put("email", visitor.getEmail());
        userInfo.put("avatar", visitor.getAvatar());
        userInfo.put("gender", visitor.getGender());
        userInfo.put("age", visitor.getAge());
        return userInfo;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UpdateUserDTO updateUserDTO) {
        Visitor visitor = visitorMapper.selectById(userId);
        if (visitor == null) {
            throw new RuntimeException("用户不存在");
        }

        if (updateUserDTO.getNickname() != null) {
            visitor.setNickname(updateUserDTO.getNickname());
        }
        if (updateUserDTO.getPhone() != null) {
            Visitor existVisitor = visitorMapper.selectByPhone(updateUserDTO.getPhone());
            if (existVisitor != null && !existVisitor.getId().equals(userId)) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
            visitor.setPhone(updateUserDTO.getPhone());
        }
        if (updateUserDTO.getEmail() != null) {
            visitor.setEmail(updateUserDTO.getEmail());
        }
        if (updateUserDTO.getGender() != null) {
            visitor.setGender(updateUserDTO.getGender());
        }
        if (updateUserDTO.getAge() != null) {
            visitor.setAge(updateUserDTO.getAge());
        }
        if (updateUserDTO.getAvatar() != null) {
            visitor.setAvatar(updateUserDTO.getAvatar());
        }

        visitorMapper.updateById(visitor);
        log.info("用户信息更新成功: userId={}", userId);
    }

    @Override
    public void logout(String token) {
        log.info("退出登录: token={}", token != null ? token.substring(0, Math.min(20, token.length())) : "null");
    }

    // ==================== 首页模块 ====================

    @Override
    public Map<String, Object> getHomeInfo() {
        Map<String, Object> homeInfo = new HashMap<>();
        homeInfo.put("name", "特呈岛生态景区");
        homeInfo.put("description", "特呈岛位于湛江湾内，距离市区仅3海里，乘船15-20分钟可达。" +
                "是国家3A级旅游景区、全国乡村旅游重点村。岛上拥有500年古红树林、天然海水温泉、红姜石海滩等独特资源，" +
                "疍家渔村文化浓厚。这里依山傍海，四季如春，被誉为\"湛江明珠\"、\"海上桃源\"，是休闲度假、亲子游玩、康养健身的理想胜地。");
        homeInfo.put("address", "广东省湛江市霞山区特呈岛");
        homeInfo.put("phone", "0759-1234567");
        homeInfo.put("businessHours", "08:00 - 18:00");
        return homeInfo;
    }

    @Override
    public List<Map<String, Object>> getAnnouncements() {
        List<Map<String, Object>> announcements = new ArrayList<>();

        Map<String, Object> ann1 = new HashMap<>();
        ann1.put("id", 1);
        ann1.put("title", "特呈岛红树林最佳观赏季");
        ann1.put("content", "每年11月至次年3月是观赏红树林的最佳季节，白鹭成群，景色壮丽。");
        ann1.put("time", "2026-01-15");
        announcements.add(ann1);

        Map<String, Object> ann2 = new HashMap<>();
        ann2.put("id", 2);
        ann2.put("title", "春节特惠活动");
        ann2.put("content", "春节期间套票8折优惠，限时抢购，欢迎广大游客前来游玩。");
        ann2.put("time", "2026-01-10");
        announcements.add(ann2);

        Map<String, Object> ann3 = new HashMap<>();
        ann3.put("id", 3);
        ann3.put("title", "温泉度假村升级通知");
        ann3.put("content", "温泉度假村已完成升级改造，新增多个温泉泡池，欢迎体验。");
        ann3.put("time", "2026-01-05");
        announcements.add(ann3);

        return announcements;
    }

    // ==================== 景点模块 ====================

    @Override
    public List<ScenicSpot> getSpotList() {
        return scenicSpotMapper.selectAllOpen();
    }

    @Override
    public ScenicSpot getSpotDetail(Long id) {
        ScenicSpot spot = scenicSpotMapper.selectById(id);
        if (spot == null) {
            throw new RuntimeException("景点不存在");
        }
        return spot;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getFacilities() {
        Map<String, List<Map<String, Object>>> facilities = new HashMap<>();

        List<Map<String, Object>> toilets = new ArrayList<>();
        Map<String, Object> toilet1 = new HashMap<>();
        toilet1.put("name", "码头游客中心");
        toilet1.put("location", "码头区域");
        toilets.add(toilet1);

        Map<String, Object> toilet2 = new HashMap<>();
        toilet2.put("name", "红树林栈道入口");
        toilet2.put("location", "红树林栈道");
        toilets.add(toilet2);

        Map<String, Object> toilet3 = new HashMap<>();
        toilet3.put("name", "温泉度假村");
        toilet3.put("location", "温泉度假村");
        toilets.add(toilet3);
        facilities.put("toilets", toilets);

        List<Map<String, Object>> restaurants = new ArrayList<>();
        Map<String, Object> restaurant1 = new HashMap<>();
        restaurant1.put("name", "渔村文化村海鲜大排档");
        restaurant1.put("location", "渔村文化村");
        restaurants.add(restaurant1);

        Map<String, Object> restaurant2 = new HashMap<>();
        restaurant2.put("name", "温泉度假村餐厅");
        restaurant2.put("location", "温泉度假村");
        restaurants.add(restaurant2);

        Map<String, Object> restaurant3 = new HashMap<>();
        restaurant3.put("name", "码头美食街");
        restaurant3.put("location", "码头区域");
        restaurants.add(restaurant3);
        facilities.put("restaurants", restaurants);

        List<Map<String, Object>> parkings = new ArrayList<>();
        Map<String, Object> parking1 = new HashMap<>();
        parking1.put("name", "码头停车场");
        parking1.put("capacity", "500个车位");
        parkings.add(parking1);

        Map<String, Object> parking2 = new HashMap<>();
        parking2.put("name", "温泉度假村停车场");
        parking2.put("capacity", "200个车位");
        parkings.add(parking2);
        facilities.put("parkings", parkings);

        return facilities;
    }

    // ==================== 购票模块 ====================

    @Override
    public Map<String, Object> getTicketTypes() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> single = new HashMap<>();
        single.put("name", "单次登岛票");
        single.put("price", 60);
        single.put("desc", "含往返船票 + 岛上观光车（单次）");
        result.put("single", single);

        Map<String, Object> combo = new HashMap<>();
        combo.put("name", "特呈岛套票");
        combo.put("price", 168);
        combo.put("desc", "含往返船票 + 观光车无限次 + 温泉门票 + 赶海体验");
        result.put("combo", combo);

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(Long userId, OrderDTO orderDTO) {
        // 票种、数量、手机号、身份证等已由 OrderDTO 上的 Bean Validation 在进入本方法前拦截，
        // 这里只补服务端必须自己保证的规则：不允许预约过去的日期
        if (orderDTO.getVisitDate() != null && orderDTO.getVisitDate().isBefore(LocalDate.now())) {
            throw new BusinessException("游览日期不能早于今天");
        }

        BigDecimal price = "single".equals(orderDTO.getTicketType()) ? singleTicketPrice : comboPrice;
        BigDecimal totalPrice = price.multiply(new BigDecimal(orderDTO.getQuantity()));

        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        TicketOrder order = new TicketOrder();
        order.setOrderNo(orderNo);
        order.setVisitorId(userId);
        order.setTicketType(orderDTO.getTicketType());
        order.setQuantity(orderDTO.getQuantity());
        order.setTotalPrice(totalPrice);
        order.setVisitDate(orderDTO.getVisitDate());
        order.setStatus("pending");

        visitorMapper.insertTicketOrder(order);

        TicketVisitor ticketVisitor = new TicketVisitor();
        ticketVisitor.setOrderId(order.getId());
        ticketVisitor.setName(orderDTO.getName());
        ticketVisitor.setPhone(orderDTO.getPhone());
        ticketVisitor.setIdCard(orderDTO.getIdCard());

        visitorMapper.insertTicketVisitor(ticketVisitor);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", orderNo);
        result.put("totalPrice", totalPrice);
        return result;
    }

    @Override
    public List<Map<String, Object>> getOrderList(Long userId, String status) {
        List<TicketOrder> orders;
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            orders = visitorMapper.selectOrdersByVisitorIdAndStatus(userId, status);
        } else {
            orders = visitorMapper.selectOrdersByVisitorId(userId);
        }

        return orders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("ticketName", order.getTicketType().equals("single") ? "单次登岛票" : "特呈岛套票");
            map.put("quantity", order.getQuantity());
            map.put("totalAmount", order.getTotalPrice());
            map.put("visitDate", order.getVisitDate());
            map.put("status", order.getStatus());
            map.put("createTime", order.getCreatedAt());

            List<TicketVisitor> visitors = visitorMapper.selectTicketVisitorsByOrderId(order.getId());
            if (!visitors.isEmpty()) {
                TicketVisitor tv = visitors.get(0);
                map.put("name", tv.getName());
                map.put("phone", tv.getPhone());
                map.put("idCard", tv.getIdCard());
            }
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        TicketOrder order = visitorMapper.selectOrderByIdAndVisitor(orderId, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getStatus().equals("pending")) {
            throw new RuntimeException("该订单无法取消");
        }
        visitorMapper.updateOrderStatus(order.getId(), "cancelled");
    }

    @Override
    @Transactional
    public void useTicket(Long userId, Long orderId) {
        TicketOrder order = visitorMapper.selectOrderByIdAndVisitor(orderId, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"pending".equals(order.getStatus()) && !"paid".equals(order.getStatus())) {
            throw new RuntimeException("该订单无法使用");
        }
        int result = visitorMapper.updateOrderStatus(orderId, "used");
        if (result <= 0) {
            throw new RuntimeException("使用门票失败");
        }
        log.info("游客使用门票成功: userId={}, orderId={}", userId, orderId);
    }

    // ==================== 路线模块 ====================

    /** 默认游览时长预算（分钟） */
    private static final int DEFAULT_ROUTE_DURATION = 240;

    /** 景区内步行速度（公里/小时），用于把地理距离折算成通行耗时 */
    private static final double WALKING_SPEED_KMH = 4.0;

    /** 地球半径（公里） */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /** 经纬度缺失时的兜底通行距离（公里） */
    private static final double FALLBACK_DISTANCE_KM = 0.3;

    @Override
    public List<Long> generateRoute(Map<String, Object> params) {
        int duration = parseDuration(params);
        List<ScenicSpot> spots = scenicSpotMapper.selectAllOpen();
        List<Long> routeIds = new ArrayList<>();
        if (spots.isEmpty()) {
            return routeIds;
        }

        // 以 sort_order 最小者（景区入口 / 码头）作为起点
        ScenicSpot current = spots.stream()
                .min(Comparator.comparingInt(s -> s.getSortOrder() == null ? Integer.MAX_VALUE : s.getSortOrder()))
                .orElse(spots.get(0));

        int totalTime = stayMinutes(current);
        if (totalTime > duration) {
            // 预算连入口景点的停留时间都不够，不存在可行路线
            return routeIds;
        }
        routeIds.add(current.getId());
        Set<Long> visited = new HashSet<>();
        visited.add(current.getId());

        // 最近邻贪心：每一步都在「剩余预算内可达」的未访问景点中取地理距离最近的一个，
        // 通行耗时按步行速度折算后一并计入预算（原实现只累计停留时间，会超出用户设定的时长）
        while (true) {
            ScenicSpot next = null;
            double nearest = Double.MAX_VALUE;
            int travelCost = 0;

            for (ScenicSpot candidate : spots) {
                if (visited.contains(candidate.getId())) {
                    continue;
                }
                double distance = distanceKm(current, candidate);
                int travel = travelMinutes(distance);
                if (totalTime + travel + stayMinutes(candidate) > duration) {
                    continue;
                }
                if (distance < nearest) {
                    nearest = distance;
                    travelCost = travel;
                    next = candidate;
                }
            }

            if (next == null) {
                break;
            }
            routeIds.add(next.getId());
            visited.add(next.getId());
            totalTime += travelCost + stayMinutes(next);
            current = next;
        }

        log.info("路线生成完成: 时长预算={}分钟, 纳入景点={}个, 预计总耗时={}分钟",
                duration, routeIds.size(), totalTime);
        return routeIds;
    }

    /**
     * 解析时长预算，兼容 JSON 反序列化出的 Integer / Long / Double
     */
    private int parseDuration(Map<String, Object> params) {
        Object raw = params == null ? null : params.get("duration");
        if (raw instanceof Number number && number.intValue() > 0) {
            return number.intValue();
        }
        return DEFAULT_ROUTE_DURATION;
    }

    private int stayMinutes(ScenicSpot spot) {
        return spot.getStayTime() == null || spot.getStayTime() <= 0 ? 30 : spot.getStayTime();
    }

    private int travelMinutes(double distanceKm) {
        return (int) Math.ceil(distanceKm / WALKING_SPEED_KMH * 60);
    }

    /**
     * Haversine 球面距离（公里）。任一端点经纬度缺失时退回默认通行距离，
     * 保证景点坐标不全时路线仍可生成而不是直接失败。
     */
    private double distanceKm(ScenicSpot from, ScenicSpot to) {
        if (from.getLatitude() == null || from.getLongitude() == null
                || to.getLatitude() == null || to.getLongitude() == null) {
            return FALLBACK_DISTANCE_KM;
        }
        double lat1 = Math.toRadians(from.getLatitude().doubleValue());
        double lat2 = Math.toRadians(to.getLatitude().doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLng = Math.toRadians(to.getLongitude().doubleValue() - from.getLongitude().doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    @Override
    @Transactional
    public Long saveRoute(Long userId, RouteDTO routeDTO) {
        List<ScenicSpot> spots = scenicSpotMapper.selectAllOpen();
        Map<Long, ScenicSpot> spotMap = spots.stream().collect(Collectors.toMap(ScenicSpot::getId, s -> s));

        int totalTime = 0;
        BigDecimal totalDistance = BigDecimal.ZERO;

        for (int i = 0; i < routeDTO.getSpotIds().size(); i++) {
            Long spotId = routeDTO.getSpotIds().get(i);
            ScenicSpot spot = spotMap.get(spotId);
            if (spot == null) continue;

            Integer stayTime = routeDTO.getStayTimes() != null && i < routeDTO.getStayTimes().size()
                    ? routeDTO.getStayTimes().get(i) : spot.getStayTime();
            totalTime += stayTime;

            if (i > 0) {
                totalDistance = totalDistance.add(new BigDecimal("0.3"));
            }
        }

        VisitorRoute route = new VisitorRoute();
        route.setVisitorId(userId);
        route.setName(routeDTO.getName() != null ? routeDTO.getName() :
                "我的路线_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        route.setTotalTime(totalTime);
        route.setTotalDistance(totalDistance);

        visitorMapper.insertVisitorRoute(route);

        for (int i = 0; i < routeDTO.getSpotIds().size(); i++) {
            Long spotId = routeDTO.getSpotIds().get(i);
            ScenicSpot spot = spotMap.get(spotId);
            if (spot == null) continue;

            RouteSpot routeSpot = new RouteSpot();
            routeSpot.setRouteId(route.getId());
            routeSpot.setSpotId(spotId);
            routeSpot.setSortOrder(i + 1);

            Integer stayTime = routeDTO.getStayTimes() != null && i < routeDTO.getStayTimes().size()
                    ? routeDTO.getStayTimes().get(i) : spot.getStayTime();
            routeSpot.setStayTime(stayTime);

            visitorMapper.insertRouteSpot(routeSpot);
        }

        return route.getId();
    }

    @Override
    public List<Map<String, Object>> getRouteList(Long userId) {
        List<VisitorRoute> routes = visitorMapper.selectRoutesByVisitorId(userId);
        return routes.stream().map(route -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", route.getId());
            map.put("name", route.getName());
            map.put("totalTime", route.getTotalTime());  // 保持 totalTime
            map.put("totalDistance", route.getTotalDistance());
            map.put("createdAt", route.getCreatedAt());  // 保持 createdAt

            // ⭐ 新增：获取路线关联的景点列表
            List<RouteSpot> routeSpots = visitorMapper.selectRouteSpotsByRouteId(route.getId());
            List<Map<String, Object>> spotList = new ArrayList<>();
            for (RouteSpot rs : routeSpots) {
                ScenicSpot spot = scenicSpotMapper.selectById(rs.getSpotId());
                if (spot != null) {
                    Map<String, Object> spotInfo = new HashMap<>();
                    spotInfo.put("id", spot.getId());
                    spotInfo.put("name", spot.getName());
                    spotInfo.put("stayTime", rs.getStayTime());
                    spotList.add(spotInfo);
                }
            }
            map.put("spots", spotList);
            map.put("spotCount", spotList.size());

            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRoute(Long userId, Long routeId) {
        VisitorRoute route = visitorMapper.selectRouteByIdAndVisitor(routeId, userId);
        if (route == null) {
            throw new RuntimeException("路线不存在");
        }
        visitorMapper.deleteRouteSpotsByRouteId(routeId);
        visitorMapper.deleteRouteByIdAndVisitor(routeId, userId);
    }


    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Visitor visitor = visitorMapper.selectById(userId);
        if (visitor == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!PasswordUtil.matches(oldPassword, visitor.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (!PasswordUtil.isStrongPassword(newPassword)) {
            throw new BusinessException("新密码需至少 6 位且同时包含字母和数字");
        }
        visitor.setPassword(PasswordUtil.encode(newPassword));
        visitor.setUpdatedAt(LocalDateTime.now());
        int result = visitorMapper.updatePassword(visitor);
        if (result <= 0) {
            throw new RuntimeException("修改密码失败");
        }
        log.info("用户密码修改成功: userId={}", userId);
    }
}