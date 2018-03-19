/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by box on 2018/3/19.
 * <p>
 * 电梯里边的楼层
 */
public class InsideFloor extends Floor {

    private InsideFloor(int index) {
        super(index);
    }

    public static List<Floor> create(Integer... indexs) {
        List<Floor> floors = new ArrayList<>();
        for (int index : indexs) {
            floors.add(create(index));
        }
        return floors;
    }

    public static Floor create(int index) {
        return new InsideFloor(index);
    }
}
