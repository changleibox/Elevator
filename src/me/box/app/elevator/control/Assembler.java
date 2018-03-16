/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.control;

import me.box.app.elevator.model.Elevator;
import me.box.app.elevator.model.Floor;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 组装电梯
 */
public class Assembler {

    /**
     * 组装电梯
     *
     * @return 组装好的电梯
     */
    public static Elevator assemble() {
        return new Elevator(Floor.create(-1, -2, 1, 3, 4, 5, 6, 7, 8, 9, 10));
    }
}
