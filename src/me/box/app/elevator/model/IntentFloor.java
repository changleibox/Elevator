/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

import me.box.app.elevator.enums.Direction;

/**
 * Created by box on 2018/3/19.
 * <p>
 * 有上下方向的楼层
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class IntentFloor extends Floor {

    private Direction intentDirection;

    private IntentFloor(int index, Direction intentDirection) {
        super(index);
        this.intentDirection = intentDirection;
    }

    public Direction getIntentDirection() {
        return intentDirection;
    }

    public void setIntentDirection(Direction intentDirection) {
        this.intentDirection = intentDirection;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && ((IntentFloor) obj).getIntentDirection() == intentDirection;
    }

    public static IntentFloor createFloor(Floor floor) {
        return createFloor(floor.getIndex());
    }

    public static IntentFloor createFloor(int index) {
        return createFloor(index, Direction.UP);
    }

    public static IntentFloor createFloor(int index, Direction direction) {
        return new IntentFloor(index, direction);
    }
}
