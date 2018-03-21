/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.control;

import me.box.app.elevator.enums.Direction;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 电梯控制器
 */
@SuppressWarnings("unused")
public class ElevatorManager {

    private final Elevator mElevator;

    ElevatorManager(Elevator elevator) {
        this.mElevator = elevator;
    }

    /**
     * 乘坐电梯
     */
    public void ride(int index, Direction direction) {
        mElevator.addTargetFloor(index, direction);
    }

    /**
     * 乘坐电梯
     */
    public void ride(int index) {
        mElevator.addTargetFloor(index);
    }

}
