package com.scenic.mapper;

import com.scenic.entity.ScenicSpot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScenicSpotMapper {

    /**
     * 获取所有开放的景点（按排序号升序）
     */
    @Select("SELECT * FROM scenic_spot WHERE status = 'open' ORDER BY sort_order")
    List<ScenicSpot> selectAllOpen();

    /**
     * 根据ID获取景点
     */
    @Select("SELECT * FROM scenic_spot WHERE id = #{id}")
    ScenicSpot selectById(@Param("id") Long id);

    /**
     * 根据分类获取景点
     */
    @Select("SELECT * FROM scenic_spot WHERE status = 'open' AND category = #{category} ORDER BY sort_order")
    List<ScenicSpot> selectByCategory(@Param("category") String category);
}