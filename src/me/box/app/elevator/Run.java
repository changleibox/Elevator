/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator;

import me.box.app.elevator.control.Assembler;
import me.box.app.elevator.control.Manager;
import me.box.app.elevator.enums.Direction;
import me.box.app.elevator.model.Elevator;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 应用启动入口
 */
public class Run {

    public static void main(String[] args) {
        // Stream.iterate(3, item -> item + 1).limit(10).forEach(System.out::println);

        Elevator elevator = Assembler.assemble();
        Manager manager = new Manager(elevator);
        manager.ride(3, Direction.UP);
        manager.ride(4, Direction.UP);
        manager.ride(5, Direction.DOWN);
        manager.ride(6, Direction.UP);
        manager.ride(7, Direction.UP);
        manager.ride(8, Direction.DOWN);
        manager.start();
        // new Thread(() -> {
        //     Manager manager = new Manager(elevator);
        //     manager.start();
        //     manager.ride(3, Direction.UP);
        //     manager.ride(4, Direction.UP);
        //     manager.ride(5, Direction.DOWN);
        //     manager.ride(6, Direction.UP);
        //     manager.ride(7, Direction.UP);
        //     manager.ride(8, Direction.DOWN);
        //     // try {
        //     //     Thread.sleep(2000L);
        //     // } catch (InterruptedException e) {
        //     //     e.printStackTrace();
        //     // }
        //     // manager.ride(1, Direction.UP);
        // }).start();
    }
}
