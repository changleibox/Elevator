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
 * Created by box on 2018/3/16.
 * <p>
 * 楼层
 */
@SuppressWarnings("unused")
public class Floor implements Comparable<Floor> {

    private int index;
    private Direction[] directions;
    private Direction intentDirection;

    private Floor(int index, Direction[] directions) {
        this.index = index;
        this.directions = directions;
    }

    public int getIndex() {
        return index;
    }

    public Direction[] getDirections() {
        return directions;
    }

    public Direction getIntentDirection() {
        return intentDirection;
    }

    public void setIntentDirection(Direction intentDirection) {
        this.intentDirection = intentDirection;
    }

    @Override
    public int compareTo(Floor o) {
        return Integer.compare(index, o.index);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Floor && ((Floor) obj).index == index;
    }

    @Override
    public String toString() {
        return String.format("%d楼", index);
    }

    public static List<Floor> create(Integer... indexs) {
        int min = Collections.min(Arrays.asList(indexs));
        int max = Collections.max(Arrays.asList(indexs));
        List<Floor> floors = new ArrayList<>();
        for (int index : indexs) {
            Direction[] directions = new Direction[0];
            if (min == index) {
                directions = new Direction[]{Direction.UP};
            } else if (max == index) {
                directions = new Direction[]{Direction.DOWN};
            } else if (min != max) {
                directions = Direction.values();
            }
            floors.add(create(index, directions));
        }
        return floors;
    }

    private static Floor create(int index, Direction[] directions) {
        return new Floor(index, directions);
    }
}
