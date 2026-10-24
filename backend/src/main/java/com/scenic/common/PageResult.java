package com.scenic.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 分页结果封装
 * 用于返回分页数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 手动构建
     */
    public static <T> PageResult<T> of(Integer current, Integer size, Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setCurrent(current);
        result.setSize(size);
        result.setTotal(total);
        result.setRecords(records);
        // 计算总页数
        result.setPages((int) ((total + size - 1) / size));
        return result;
    }

    /**
     * 使用 PageHelper 的 PageInfo 构建
     */
    public static <T> PageResult<T> of(com.github.pagehelper.PageInfo<T> pageInfo) {
        return of(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getList()
        );
    }
}