/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

import me.box.app.elevator.enums.Direction;
import me.box.app.elevator.enums.Status;

import java.util.*;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 电梯
 */
@SuppressWarnings({"unused", "InfiniteLoopStatement"})
public class Elevator implements Runnable {

    private final Map<Integer, Floor> floorsMap;
    private Direction direction;
    private Status status;
    private Floor currentFloor;
    private final LinkedList<Floor> targetFloors;

    public Elevator(List<Floor> floors) {
        this.status = Status.AWAIT;
        this.direction = Direction.UP;
        this.floorsMap = new LinkedHashMap<>();
        this.currentFloor = Collections.min(floors);
        this.targetFloors = new LinkedList<>();
        floors.stream().sorted().forEach(floor -> floorsMap.put(floor.getIndex(), floor));
    }

    public List<Floor> getFloors() {
        return new ArrayList<>(floorsMap.values());
    }

    public Direction getDirection() {
        return direction;
    }

    public Status getStatus() {
        return status;
    }

    public Floor getCurrentFloor() {
        return currentFloor;
    }

    /**
     * 点击电梯里边儿面板楼层
     *
     * @param index 楼层
     */
    public void addTargetFloor(int index) {
        addTargetFloor(index, index < currentFloor.getIndex() ? Direction.DOWN : Direction.UP);
    }

    /**
     * 点击楼道里的电梯上下按钮
     *
     * @param index           楼层
     * @param intentDirection 上还是下
     */
    public void addTargetFloor(int index, Direction intentDirection) {
        synchronized (targetFloors) {
            if (index == currentFloor.getIndex()) {
                return;
            }
            Floor floor = floorsMap.get(index);
            if (floor == null) {
                System.out.println(String.format("不能到达%d楼", index));
                return;
            }
            floor.setIntentDirection(intentDirection);
            if (targetFloors.contains(floor)) {
                return;
            }
            this.targetFloors.add(floor);

            List<Floor> upFloors = new ArrayList<>();
            List<Floor> downFloors = new ArrayList<>();
            for (Floor targetFloor : targetFloors) {
                if (direction == Direction.UP) {
                    if (targetFloor.getIntentDirection() == Direction.UP && targetFloor.compareTo(currentFloor) > 0) {
                        upFloors.add(targetFloor);
                    } else {
                        downFloors.add(targetFloor);
                    }
                } else {
                    if (targetFloor.getIntentDirection() == Direction.DOWN && targetFloor.compareTo(currentFloor) < 0) {
                        downFloors.add(targetFloor);
                    } else {
                        upFloors.add(targetFloor);
                    }
                }
            }
            upFloors.sort(Floor::compareTo);
            downFloors.sort(Comparator.reverseOrder());
            LinkedList<Floor> floors = new LinkedList<>();
            floors.addAll(upFloors);
            floors.addAll(downFloors);
            targetFloors.clear();
            targetFloors.addAll(floors);

            if (status == Status.AWAIT) {
                status = Status.RUNING;
                if (floor.compareTo(currentFloor) < 0) {
                    direction = Direction.DOWN;
                } else if (floor.compareTo(currentFloor) > 0) {
                    direction = Direction.UP;
                } else {
                    direction = floor.getIntentDirection();
                }
                System.out.println("电梯启动，方向" + direction);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            if (status == Status.AWAIT || targetFloors.isEmpty()) {
                continue;
            }
            Floor nextFloor = targetFloors.poll();
            StringBuilder builder = new StringBuilder("到达")
                    .append(currentFloor)
                    .append("，")
                    .append("目标")
                    .append(nextFloor);
            System.out.println(builder);
            if (currentFloor.compareTo(nextFloor) < 0) {
                direction = Direction.UP;
            } else if (currentFloor.compareTo(nextFloor) > 0) {
                direction = Direction.DOWN;
            }
            System.out.println("方向" + direction);
            currentFloor = nextFloor;

            if (targetFloors.isEmpty()) {
                status = Status.AWAIT;
                direction = currentFloor.getIntentDirection();
                System.out.println(String.format("到达%s", currentFloor));
                System.out.println("电梯停止");
                System.out.println("方向" + direction);
            } else {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
