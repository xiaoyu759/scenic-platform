package com.scenic.mapper;

import com.scenic.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

    // ========== AdminUserMapper ==========
    AdminUser selectByUsername(@Param("username") String username);


    // ========== ScenicInfoMapper ==========
    ScenicInfo selectScenicInfo();
    int insertScenicInfo(ScenicInfo scenicInfo);
    int updateScenicInfo(ScenicInfo scenicInfo);


    // ========== EntryRecordMapper ==========
    Integer getCurrentVisitors(@Param("date") LocalDate date);
    Integer getSpotCurrentCount(@Param("spotId") Long spotId, @Param("date") LocalDate date);


    // ========== ScenicSpotMapper ==========
    List<ScenicSpot> selectAllOpenSpots();
    ScenicSpot selectSpotById(@Param("id") Long id);


    // ========== TicketOrderMapper ==========
    Integer getTodayPaidCount(@Param("date") LocalDate date);
    Integer getMonthlyOrderCount(@Param("year") int year, @Param("month") int month);

    /**
     * 区间内每日有效订单量聚合，替代「近 N 天逐日 COUNT」循环
     *
     * @return [{ label: "MM-dd", cnt: 数量 }]
     */
    List<Map<String, Object>> getDailyPaidCounts(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 区间内每月订单量聚合，替代「近 N 月逐月 COUNT」循环
     *
     * @return [{ label: "yyyy-MM", cnt: 数量 }]
     */
    List<Map<String, Object>> getMonthlyOrderCounts(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * 所有景点的当日实时人数聚合，替代「按景点逐个 COUNT」
     *
     * @return [{ spotId: 景点ID, cnt: 人数 }]
     */
    List<Map<String, Object>> getSpotCurrentCounts(@Param("date") LocalDate date);
    List<TicketOrder> selectOrderListByPage(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    TicketOrder selectOrderById(@Param("id") Long id);
    int updateOrderStatus(@Param("id") Long id, @Param("status") String status);
    List<TicketVisitor> selectTicketVisitorsByOrderId(@Param("orderId") Long orderId);

    // ==================== 数据分析 ====================
    java.math.BigDecimal getDailyRevenue(@Param("date") LocalDate date);
    java.math.BigDecimal getMonthlyRevenue(@Param("year") int year, @Param("month") int month);
    List<Map<String, Object>> getTicketSalesStats();
    List<Map<String, Object>> getVisitorAgeDistribution();
    List<Map<String, Object>> getVisitorGenderDistribution();


    // ==================== 景点管理 ====================
    List<ScenicSpot> selectSpotListByPage(@Param("keyword") String keyword, @Param("category") String category);
    Long countSpotList(@Param("keyword") String keyword, @Param("category") String category);
    int insertSpot(ScenicSpot spot);
    int updateSpot(ScenicSpot spot);
    int deleteSpot(@Param("id") Long id);

    AdminUser selectById(@Param("id") Long id);
    int updatePassword(AdminUser admin);
}