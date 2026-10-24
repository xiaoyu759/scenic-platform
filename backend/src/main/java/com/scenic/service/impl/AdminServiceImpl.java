package com.scenic.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.scenic.common.PageResult;
import com.scenic.common.StatusCode;
import com.scenic.entity.*;
import com.scenic.exception.BusinessException;
import com.scenic.mapper.AdminMapper;
import com.scenic.service.AdminService;
import com.scenic.utils.JwtUtil;
import com.scenic.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;


    // ==================== 认证 ====================

    @Override
    public Map<String, Object> adminLogin(String username, String password) {
        AdminUser admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!PasswordUtil.matches(password, admin.getPassword())) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "用户名或密码错误");
        }
        // 存量明文口令校验通过后原地升级为 BCrypt，老账号无需强制重置
        if (!PasswordUtil.isEncoded(admin.getPassword())) {
            admin.setPassword(PasswordUtil.encode(password));
            adminMapper.updatePassword(admin);
            log.info("管理员口令已升级为 BCrypt 存储: adminId={}", admin.getId());
        }
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", admin.getId());
        userInfo.put("username", admin.getUsername());
        userInfo.put("role", admin.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);

        log.info("管理员登录成功: username={}", username);
        return result;
    }


    // ==================== 监控大屏 ====================

    @Override
    public Map<String, Object> getMonitorToday() {
        LocalDate today = LocalDate.now();

        // 1. 获取今日入园人数
        Integer todayVisitors = adminMapper.getTodayPaidCount(today);
        if (todayVisitors == null) {
            todayVisitors = 0;
        }

        // 2. 获取当前在园人数（从 entry_record 表统计）
        Integer currentVisitors = adminMapper.getCurrentVisitors(today);
        if (currentVisitors == null) {
            currentVisitors = 0;
        }

        // 3. 获取最大承载量（从景区信息表获取）
        ScenicInfo scenicInfo = adminMapper.selectScenicInfo();
        int maxCapacity = scenicInfo != null && scenicInfo.getMaxCapacity() != null
                ? scenicInfo.getMaxCapacity() : 30000;

        // 4. 计算承载率
        BigDecimal capacityRate = BigDecimal.valueOf(currentVisitors)
                .divide(BigDecimal.valueOf(maxCapacity), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("todayVisitors", todayVisitors);
        result.put("currentVisitors", currentVisitors);
        result.put("capacityRate", capacityRate);

        log.info("今日数据: 入园={}, 在园={}, 承载率={}%", todayVisitors, currentVisitors, capacityRate);
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonitorSpots() {
        LocalDate today = LocalDate.now();

        // 获取所有开放景点
        List<ScenicSpot> spots = adminMapper.selectAllOpenSpots();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ScenicSpot spot : spots) {
            // 获取景点当前人数（从 entry_record 表统计）
            Integer currentCount = adminMapper.getSpotCurrentCount(spot.getId(), today);
            if (currentCount == null) {
                currentCount = 0;
            }

            // 计算拥挤度百分比
            BigDecimal rate = BigDecimal.valueOf(currentCount)
                    .divide(BigDecimal.valueOf(spot.getMaxCapacity()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);

            Map<String, Object> item = new HashMap<>();
            item.put("id", spot.getId());
            item.put("name", spot.getName());
            item.put("currentCount", currentCount);
            item.put("maxCapacity", spot.getMaxCapacity());
            item.put("rate", rate);

            result.add(item);
        }

        return result;
    }

    @Override
    @Cacheable(cacheNames = "monitor", key = "'trend:' + #type")
    public Object getMonitorTrend(String type) {
        boolean daily = "daily".equals(type);

        // 标签在内存中按区间顺序生成，查询只发一次，缺失的日期由 getOrDefault 补 0
        List<String> labels = new ArrayList<>();
        List<Map<String, Object>> rows;

        if (daily) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(6);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            for (int i = 6; i >= 0; i--) {
                labels.add(end.minusDays(i).format(formatter));
            }
            rows = adminMapper.getDailyPaidCounts(start, end);
        } else {
            LocalDate end = LocalDate.now().withDayOfMonth(1);
            LocalDate start = end.minusMonths(5);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            for (int i = 5; i >= 0; i--) {
                labels.add(end.minusMonths(i).format(formatter));
            }
            rows = adminMapper.getMonthlyOrderCounts(start, end.plusMonths(1));
        }

        Map<String, Integer> counted = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object label = row.get("label");
            Object cnt = row.get("cnt");
            if (label != null) {
                counted.put(label.toString(), cnt instanceof Number number ? number.intValue() : 0);
            }
        }

        List<Integer> counts = labels.stream()
                .map(label -> counted.getOrDefault(label, 0))
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("dates", labels);
        result.put("counts", counts);
        return result;
    }

    @Override
    @Cacheable(cacheNames = "monitor", key = "'heatmap'")
    public List<Map<String, Object>> getHeatmapData() {
        LocalDate today = LocalDate.now();

        // 获取所有开放景点
        List<ScenicSpot> spots = adminMapper.selectAllOpenSpots();

        // 一次聚合取回全部景点人数，替代按景点逐个 COUNT
        Map<Long, Integer> countBySpot = new HashMap<>();
        for (Map<String, Object> row : adminMapper.getSpotCurrentCounts(today)) {
            Object spotId = row.get("spotId");
            Object cnt = row.get("cnt");
            if (spotId instanceof Number id) {
                countBySpot.put(id.longValue(), cnt instanceof Number number ? number.intValue() : 0);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (ScenicSpot spot : spots) {
            // 获取景点当前人数
            int currentCount = countBySpot.getOrDefault(spot.getId(), 0);

            // 计算拥挤度比例；承载量缺失或非法时兜底，避免除零把强度算成 Infinity
            Integer capacity = spot.getMaxCapacity();
            double rate = (double) currentCount / (capacity == null || capacity <= 0 ? 200 : capacity);

            // 计算热力强度（0-1）
            double intensity = Math.min(rate * 1.2, 1.0);

            // 拥挤度等级
            String level;
            if (rate < 0.3) {
                level = "low";
            } else if (rate < 0.6) {
                level = "medium";
            } else if (rate < 0.8) {
                level = "high";
            } else {
                level = "extreme";
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", spot.getId());
            item.put("name", spot.getName());
            item.put("intensity", Math.round(intensity * 100) / 100.0);
            item.put("latitude", spot.getLatitude());
            item.put("longitude", spot.getLongitude());
            item.put("currentCount", currentCount);
            item.put("level", level);

            result.add(item);
        }

        return result;
    }

    // ==================== 数据分析 ====================

    @Override
    public Map<String, Object> getRevenueStats(String type, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        if ("daily".equals(type)) {
            List<String> dates = new ArrayList<>();
            List<BigDecimal> revenues = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

            // 处理日期范围
            LocalDate start;
            LocalDate end;
            try {
                start = startDate != null && !startDate.isEmpty() ? LocalDate.parse(startDate) : LocalDate.now().minusDays(7);
                end = endDate != null && !endDate.isEmpty() ? LocalDate.parse(endDate) : LocalDate.now();
            } catch (Exception e) {
                // 日期解析失败，使用默认7天
                start = LocalDate.now().minusDays(7);
                end = LocalDate.now();
            }

            // 确保开始日期不晚于结束日期
            if (start.isAfter(end)) {
                LocalDate temp = start;
                start = end;
                end = temp;
            }

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                dates.add(date.format(formatter));
                BigDecimal revenue = adminMapper.getDailyRevenue(date);
                revenues.add(revenue == null ? BigDecimal.ZERO : revenue);
            }

            result.put("dates", dates);
            result.put("revenues", revenues);

        } else {
            // 月度统计
            List<String> months = new ArrayList<>();
            List<BigDecimal> revenues = new ArrayList<>();

            // 默认最近6个月
            for (int i = 5; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusMonths(i);
                months.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM")));

                BigDecimal revenue = adminMapper.getMonthlyRevenue(date.getYear(), date.getMonthValue());
                revenues.add(revenue == null ? BigDecimal.ZERO : revenue);
            }

            result.put("dates", months);
            result.put("revenues", revenues);
        }

        return result;
    }

    @Override
    public Map<String, Object> getTicketSales() {
        List<Map<String, Object>> stats = adminMapper.getTicketSalesStats();

        // 如果查询结果为空，返回空数据（结构必须一致）
        if (stats == null || stats.isEmpty()) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("names", new ArrayList<>());
            emptyResult.put("values", new ArrayList<>());
            emptyResult.put("counts", new ArrayList<>());
            return emptyResult;
        }

        List<String> names = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 票种名称映射
        Map<String, String> typeNameMap = new HashMap<>();
        typeNameMap.put("single", "单次登岛票");
        typeNameMap.put("combo", "特呈岛套票");

        // 计算总数
        int total = 0;
        for (Map<String, Object> stat : stats) {
            Object countObj = stat.get("count");
            Integer count = countObj != null ? ((Number) countObj).intValue() : 0;
            total += count;
        }

        // 如果没有数据，返回空
        if (total == 0) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("names", new ArrayList<>());
            emptyResult.put("values", new ArrayList<>());
            emptyResult.put("counts", new ArrayList<>());
            return emptyResult;
        }

        for (Map<String, Object> stat : stats) {
            String ticketType = (String) stat.get("ticketType");
            Object countObj = stat.get("count");
            Integer count = countObj != null ? ((Number) countObj).intValue() : 0;

            String name = typeNameMap.getOrDefault(ticketType, ticketType);
            names.add(name);
            counts.add(count);
            values.add(Math.round((float) count / total * 100));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("names", names);
        result.put("values", values);
        result.put("counts", counts);
        return result;
    }

    @Override
    public Map<String, Object> getVisitorProfile() {
        // 年龄分布
        List<Map<String, Object>> ageData = adminMapper.getVisitorAgeDistribution();
        // 性别分布
        List<Map<String, Object>> genderData = adminMapper.getVisitorGenderDistribution();

        // 如果查询结果为空，使用默认数据
        if (ageData == null || ageData.isEmpty()) {
            ageData = getDefaultAgeData();
        }

        if (genderData == null || genderData.isEmpty()) {
            genderData = getDefaultGenderData();
        }

        // 格式化年龄数据（确保前端字段名一致：name + value）
        List<Map<String, Object>> formattedAge = new ArrayList<>();
        for (Map<String, Object> item : ageData) {
            Map<String, Object> ageItem = new HashMap<>();
            ageItem.put("name", item.get("ageGroup"));
            ageItem.put("value", item.get("value"));
            formattedAge.add(ageItem);
        }

        // 格式化性别数据（确保前端字段名一致：name + value）
        List<Map<String, Object>> formattedGender = new ArrayList<>();
        for (Map<String, Object> item : genderData) {
            Map<String, Object> genderItem = new HashMap<>();
            genderItem.put("name", item.get("genderName"));
            genderItem.put("value", item.get("value"));
            formattedGender.add(genderItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("age", formattedAge);
        result.put("gender", formattedGender);
        return result;
    }

    private List<Map<String, Object>> getDefaultAgeData() {
        List<Map<String, Object>> data = new ArrayList<>();
        String[] groups = {"18-25岁", "26-35岁", "36-50岁", "50岁以上"};
        int[] values = {25, 35, 25, 15};
        for (int i = 0; i < groups.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("ageGroup", groups[i]);
            item.put("value", values[i]);
            data.add(item);
        }
        return data;
    }

    private List<Map<String, Object>> getDefaultGenderData() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> male = new HashMap<>();
        male.put("genderName", "男性");
        male.put("value", 55);
        data.add(male);

        Map<String, Object> female = new HashMap<>();
        female.put("genderName", "女性");
        female.put("value", 45);
        data.add(female);
        return data;
    }

    @Override
    public void exportExcel(String params, HttpServletResponse response) {
        // TODO: 实现导出Excel
        log.info("导出Excel: params={}", params);

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=revenue_report.xlsx");
            // 实际导出逻辑待实现
        } catch (Exception e) {
            log.error("导出Excel失败: ", e);
        }
    }

    // ==================== 景点管理 ====================

    @Override
    public Map<String, Object> getSpotList(Integer pageNo, Integer pageSize, String keyword, String category) {
        PageHelper.startPage(pageNo, pageSize);
        List<ScenicSpot> spots = adminMapper.selectSpotListByPage(keyword, category);
        PageInfo<ScenicSpot> pageInfo = new PageInfo<>(spots);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("current", pageInfo.getPageNum());
        result.put("size", pageInfo.getPageSize());
        result.put("pages", pageInfo.getPages());
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "monitor", allEntries = true)
    public void addSpot(ScenicSpot spot) {
        // 设置默认值
        if (spot.getCurrentCount() == null) {
            spot.setCurrentCount(0);
        }
        if (spot.getStatus() == null) {
            spot.setStatus("open");
        }
        if (spot.getSortOrder() == null) {
            // 获取当前最大排序号
            List<ScenicSpot> allSpots = adminMapper.selectSpotListByPage(null, null);
            int maxOrder = allSpots.stream().mapToInt(ScenicSpot::getSortOrder).max().orElse(0);
            spot.setSortOrder(maxOrder + 1);
        }
        spot.setCreatedAt(LocalDateTime.now());
        spot.setUpdatedAt(LocalDateTime.now());

        int result = adminMapper.insertSpot(spot);
        if (result <= 0) {
            throw new RuntimeException("添加景点失败");
        }
        log.info("添加景点成功: name={}", spot.getName());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "monitor", allEntries = true)
    public void updateSpot(ScenicSpot spot) {
        ScenicSpot existing = adminMapper.selectSpotById(spot.getId());
        if (existing == null) {
            throw new RuntimeException("景点不存在");
        }
        spot.setUpdatedAt(LocalDateTime.now());
        int result = adminMapper.updateSpot(spot);
        if (result <= 0) {
            throw new RuntimeException("更新景点失败");
        }
        log.info("更新景点成功: id={}, name={}", spot.getId(), spot.getName());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "monitor", allEntries = true)
    public void deleteSpot(Long id) {
        ScenicSpot existing = adminMapper.selectSpotById(id);
        if (existing == null) {
            throw new RuntimeException("景点不存在");
        }
        int result = adminMapper.deleteSpot(id);
        if (result <= 0) {
            throw new RuntimeException("删除景点失败");
        }
        log.info("删除景点成功: id={}, name={}", id, existing.getName());
    }


    // ==================== 订单管理 ====================

    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("pending", "待使用");
        STATUS_MAP.put("paid", "已支付");
        STATUS_MAP.put("used", "已使用");
        STATUS_MAP.put("cancelled", "已取消");
    }

    @Override
    public PageResult<Map<String, Object>> getOrderList(Integer pageNo, Integer pageSize, String keyword, String status, String startDate, String endDate) {
        PageHelper.startPage(pageNo, pageSize);

        LocalDate start = null;
        LocalDate end = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (startDate != null && !startDate.isEmpty()) start = LocalDate.parse(startDate, formatter);
            if (endDate != null && !endDate.isEmpty()) end = LocalDate.parse(endDate, formatter);
        } catch (Exception e) {
            log.warn("日期解析失败: startDate={}, endDate={}", startDate, endDate);
        }

        List<TicketOrder> orders = adminMapper.selectOrderListByPage(keyword, status, start, end);
        PageInfo<TicketOrder> pageInfo = new PageInfo<>(orders);

        List<Map<String, Object>> records = new ArrayList<>();
        for (TicketOrder order : pageInfo.getList()) {
            List<TicketVisitor> visitors = adminMapper.selectTicketVisitorsByOrderId(order.getId());
            TicketVisitor visitor = visitors.isEmpty() ? null : visitors.get(0);

            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("ticketType", order.getTicketType());
            item.put("ticketName", "single".equals(order.getTicketType()) ? "单次登岛票" : "特呈岛套票");
            item.put("quantity", order.getQuantity());
            item.put("totalPrice", order.getTotalPrice());
            item.put("visitDate", order.getVisitDate());
            item.put("status", order.getStatus());
            item.put("statusText", STATUS_MAP.getOrDefault(order.getStatus(), order.getStatus()));
            item.put("createdAt", order.getCreatedAt());
            item.put("updatedAt", order.getUpdatedAt());

            if (visitor != null) {
                item.put("visitorName", visitor.getName());
                item.put("phone", visitor.getPhone());
                item.put("idCard", visitor.getIdCard());
            }

            records.add(item);
        }

        return PageResult.of(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                records
        );
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        if (!STATUS_MAP.containsKey(status)) {
            throw new RuntimeException("无效的状态值: " + status);
        }

        TicketOrder order = adminMapper.selectOrderById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        String currentStatus = order.getStatus();
        if ("cancelled".equals(currentStatus)) {
            throw new RuntimeException("已取消的订单不能修改状态");
        }
        if ("used".equals(currentStatus)) {
            throw new RuntimeException("已使用的订单不能修改状态");
        }
        if ("pending".equals(currentStatus) && "used".equals(status)) {
            throw new RuntimeException("待使用订单需先支付才能使用");
        }

        int result = adminMapper.updateOrderStatus(orderId, status);
        if (result <= 0) throw new RuntimeException("更新订单状态失败");

        log.info("订单状态更新成功: orderId={}, oldStatus={}, newStatus={}", orderId, currentStatus, status);
    }

    @Override
    public Map<String, Object> getOrderDetail(Long orderId) {
        TicketOrder order = adminMapper.selectOrderById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        List<TicketVisitor> visitors = adminMapper.selectTicketVisitorsByOrderId(orderId);

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", order.getId());
        detail.put("orderNo", order.getOrderNo());
        detail.put("visitorId", order.getVisitorId());
        detail.put("ticketType", order.getTicketType());
        detail.put("ticketName", "single".equals(order.getTicketType()) ? "单次登岛票" : "特呈岛套票");
        detail.put("quantity", order.getQuantity());
        detail.put("totalPrice", order.getTotalPrice());
        detail.put("visitDate", order.getVisitDate());
        detail.put("status", order.getStatus());
        detail.put("statusText", STATUS_MAP.getOrDefault(order.getStatus(), order.getStatus()));
        detail.put("createdAt", order.getCreatedAt());
        detail.put("updatedAt", order.getUpdatedAt());
        detail.put("visitors", visitors);

        return detail;
    }


// ==================== 景区设置 ====================

    @Override
    public ScenicInfo getScenicInfo() {
        ScenicInfo info = adminMapper.selectScenicInfo();
        if (info == null) {
            info = new ScenicInfo();
            info.setName("特呈岛生态景区");
            info.setDescription("特呈岛位于湛江湾内，距离市区仅3海里，乘船15-20分钟可达。是国家3A级旅游景区、全国乡村旅游重点村。");
            info.setAddress("广东省湛江市霞山区特呈岛");
            info.setPhone("0759-1234567");
            info.setBusinessHours("08:00 - 18:00");
            info.setMaxCapacity(30000);
        }
        return info;
    }

    @Override
    @Transactional
    public void updateScenicInfo(ScenicInfo scenicInfo) {
        ScenicInfo existing = adminMapper.selectScenicInfo();

        if (existing == null) {
            scenicInfo.setUpdatedAt(LocalDateTime.now());
            int result = adminMapper.insertScenicInfo(scenicInfo);
            if (result <= 0) throw new RuntimeException("保存景区信息失败");
            log.info("新增景区信息成功: name={}", scenicInfo.getName());
        } else {
            scenicInfo.setId(existing.getId());
            scenicInfo.setUpdatedAt(LocalDateTime.now());
            int result = adminMapper.updateScenicInfo(scenicInfo);
            if (result <= 0) throw new RuntimeException("更新景区信息失败");
            log.info("更新景区信息成功: id={}, name={}", existing.getId(), scenicInfo.getName());
        }
    }

    @Override
    @Transactional
    public void changeAdminPassword(Long adminId, String oldPassword, String newPassword) {
        AdminUser admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        if (!PasswordUtil.matches(oldPassword, admin.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (!PasswordUtil.isStrongPassword(newPassword)) {
            throw new BusinessException("新密码需至少 6 位且同时包含字母和数字");
        }
        admin.setPassword(PasswordUtil.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        int result = adminMapper.updatePassword(admin);
        if (result <= 0) {
            throw new RuntimeException("修改密码失败");
        }
        log.info("管理员密码修改成功: adminId={}", adminId);
    }
}