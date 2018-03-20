/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

import me.box.app.elevator.enums.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by box on 2018/3/19.
 * <p>
 * 楼道里的Floor
 */
@SuppressWarnings("unused")
public class OutsideFloor extends Floor {

    private Direction[] directions;

    private OutsideFloor(int index, Direction[] directions) {
        super(index);
        this.directions = directions;
    }

    public Direction[] getDirections() {
        return directions;
    }

    public static List<OutsideFloor> createFloors(Integer... indexs) {
        List<Integer> tmpIndexs = new ArrayList<>(Arrays.asList(indexs));
        tmpIndexs.removeIf(item -> item == null || item == 0);
        if (tmpIndexs.isEmpty()) {
            throw new NullPointerException("请创建至少一层楼");
        }
        Integer min = Collections.min(tmpIndexs);
        Integer max = Collections.max(tmpIndexs);
        List<OutsideFloor> floors = new ArrayList<>();
        for (Integer index : tmpIndexs) {
            Direction[] directions = new Direction[0];
            if (min.equals(index)) {
                directions = new Direction[]{Direction.UP};
            } else if (max.equals(index)) {
                directions = new Direction[]{Direction.DOWN};
            } else if (!min.equals(max)) {
                directions = Direction.values();
            }
            floors.add(createFloor(index, directions));
        }
        return floors;
    }

    public static OutsideFloor createFloor(int index) {
        return new OutsideFloor(index, Direction.values());
    }

    private static OutsideFloor createFloor(int index, Direction[] directions) {
        return new OutsideFloor(index, directions);
    }
}
