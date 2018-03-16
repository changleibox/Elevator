/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.control;

import me.box.app.elevator.enums.Direction;
import me.box.app.elevator.model.Elevator;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 电梯控制器
 */
public class Manager {

    private final Elevator mElevator;
    private final Thread mThread;

    public Manager(Elevator elevator) {
        this.mElevator = elevator;
        this.mThread = new Thread(elevator);
    }

    /**
     * 乘坐电梯
     */
    public void ride(int index, Direction direction) {
        mElevator.addTargetFloor(index, direction);
    }

    public void start() {
        if (!mThread.isAlive()) {
            this.mThread.start();
        }
    }

}
