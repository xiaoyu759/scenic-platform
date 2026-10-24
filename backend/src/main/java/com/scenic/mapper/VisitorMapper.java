package com.scenic.mapper;

import com.scenic.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VisitorMapper {

    // ========== VisitorMapper ==========
    @Select("SELECT * FROM visitor WHERE username = #{username} AND deleted = 0")
    Visitor selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM visitor WHERE phone = #{phone} AND deleted = 0")
    Visitor selectByPhone(@Param("phone") String phone);

    @Select("SELECT COUNT(*) FROM visitor WHERE username = #{username} AND deleted = 0")
    int countByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) FROM visitor WHERE phone = #{phone} AND deleted = 0")
    int countByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM visitor WHERE id = #{id} AND deleted = 0")
    Visitor selectById(@Param("id") Long id);

    int insert(Visitor visitor);
    int updateById(Visitor visitor);

    int updatePassword(Visitor visitor);


    // ========== TicketOrderMapper ==========
    int insertTicketOrder(TicketOrder order);
    List<TicketOrder> selectOrdersByVisitorId(@Param("visitorId") Long visitorId);
    TicketOrder selectOrderByIdAndVisitor(@Param("id") Long id, @Param("visitorId") Long visitorId);
    int updateOrderStatus(@Param("id") Long id, @Param("status") String status);
    List<TicketOrder> selectOrdersByVisitorIdAndStatus(@Param("visitorId") Long visitorId, @Param("status") String status);


    // ========== TicketVisitorMapper ==========
    int insertTicketVisitor(TicketVisitor ticketVisitor);
    List<TicketVisitor> selectTicketVisitorsByOrderId(@Param("orderId") Long orderId);


    // ========== VisitorRouteMapper ==========
    int insertVisitorRoute(VisitorRoute route);
    List<VisitorRoute> selectRoutesByVisitorId(@Param("visitorId") Long visitorId);
    VisitorRoute selectRouteByIdAndVisitor(@Param("id") Long id, @Param("visitorId") Long visitorId);
    int deleteRouteByIdAndVisitor(@Param("id") Long id, @Param("visitorId") Long visitorId);


    // ========== RouteSpotMapper ==========
    int insertRouteSpot(RouteSpot routeSpot);
    List<RouteSpot> selectRouteSpotsByRouteId(@Param("routeId") Long routeId);
    int deleteRouteSpotsByRouteId(@Param("routeId") Long routeId);
}