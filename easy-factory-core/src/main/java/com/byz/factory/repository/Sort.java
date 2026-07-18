package com.byz.factory.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 排序条件封装
 *
 * @author 苏政
 */
public class Sort implements Serializable {

    public enum Direction {
        ASC, DESC
    }

    private final List<Order> orders;

    public Sort(List<Order> orders) {
        this.orders = Collections.unmodifiableList(new ArrayList<>(orders));
    }

    public Sort(Order... orders) {
        this(Arrays.asList(orders));
    }

    public List<Order> getOrders() {
        return orders;
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    /**
     * 创建按单字段升序
     */
    public static Sort by(String property) {
        return new Sort(new Order(property, Direction.ASC));
    }

    /**
     * 创建按单字段排序
     */
    public static Sort by(String property, Direction direction) {
        return new Sort(new Order(property, direction));
    }

    /**
     * 排序条目
     */
    public record Order(String getProperty, Direction getDirection) implements Serializable {
    }

}
