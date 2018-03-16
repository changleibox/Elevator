/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.enums;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 方向
 */
public enum Direction {

    UP, DOWN;

    @Override
    public String toString() {
        return this == UP ? "上" : "下";
    }
}
