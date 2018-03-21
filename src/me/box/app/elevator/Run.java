/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator;

import me.box.app.elevator.control.Assembler;
import me.box.app.elevator.control.ElevatorManager;
import me.box.app.elevator.enums.Direction;

import java.util.Scanner;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 应用启动入口
 */
public class Run {

    public static void main(String[] args) {
        ElevatorManager manager = Assembler.install();
        manager.ride(-1, Direction.DOWN);
        manager.ride(2, Direction.DOWN);
        manager.ride(3, Direction.UP);
        manager.ride(4, Direction.UP);
        manager.ride(5, Direction.DOWN);
        manager.ride(6, Direction.UP);
        manager.ride(7, Direction.UP);
        manager.ride(8, Direction.DOWN);
        manager.ride(9, Direction.DOWN);
        manager.ride(10, Direction.UP);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextInt()) {
            manager.ride(scanner.nextInt());
        }
    }
}
