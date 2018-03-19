/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

import me.box.app.elevator.enums.Direction;
import me.box.app.elevator.enums.Status;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 电梯
 */
@SuppressWarnings({"unused", "InfiniteLoopStatement"})
public class Elevator implements Runnable {

    private final Map<Integer, OutsideFloor> floorsMap;
    private Direction currentDirection;
    private Status status;
    private IntentFloor currentFloor;
    private final LinkedList<IntentFloor> targetFloors;
    private final LinkedList<IntentFloor> routeFloors;
    private boolean isAnalysisData = true;

    public Elevator(List<OutsideFloor> floors) {
        this.status = Status.AWAIT;
        this.currentDirection = Direction.UP;
        this.floorsMap = new LinkedHashMap<>();
        this.currentFloor = IntentFloor.createFloor(Collections.min(floors));
        this.targetFloors = new LinkedList<>();
        this.routeFloors = new LinkedList<>();
        floors.stream().sorted().forEach(floor -> floorsMap.put(floor.getIndex(), floor));
    }

    public List<Floor> getFloors() {
        return new ArrayList<>(floorsMap.values());
    }

    public Status getStatus() {
        return status;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
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
        addTargetFloor(index, null);
    }

    /**
     * 点击楼道里的电梯上下按钮
     *
     * @param index           楼层
     * @param intentDirection 上还是下
     */
    public void addTargetFloor(int index, Direction intentDirection) {
        synchronized (routeFloors) {
            isAnalysisData = true;
            if (index == currentFloor.getIndex()) {
                return;
            }
            if (intentDirection == null) {
                if (currentDirection == Direction.UP) {
                    if (index < currentFloor.getIndex()) {
                        intentDirection = Direction.DOWN;
                    } else {
                        intentDirection = Direction.UP;
                    }
                } else {
                    if (index > currentFloor.getIndex()) {
                        intentDirection = Direction.DOWN;
                    } else {
                        intentDirection = Direction.UP;
                    }
                }
            }
            if (!floorsMap.containsKey(index)) {
                System.out.println(String.format("不能到达%d楼", index));
                return;
            }
            IntentFloor intentFloor = IntentFloor.createFloor(index, intentDirection);
            if (targetFloors.contains(intentFloor)) {
                return;
            }
            this.targetFloors.add(intentFloor);

            List<IntentFloor> upFloors = new ArrayList<>();
            List<IntentFloor> downFloors = new ArrayList<>();
            for (IntentFloor targetFloor : targetFloors) {
                Direction direction = targetFloor.getIntentDirection();
                if (currentDirection == Direction.UP) {
                    if (direction == Direction.UP && targetFloor.compareTo(currentFloor) > 0) {
                        upFloors.add(targetFloor);
                    } else {
                        downFloors.add(targetFloor);
                    }
                } else {
                    if (direction == Direction.DOWN && targetFloor.compareTo(currentFloor) < 0) {
                        downFloors.add(targetFloor);
                    } else {
                        upFloors.add(targetFloor);
                    }
                }
            }
            upFloors.sort(Floor::compareTo);
            downFloors.sort(Comparator.reverseOrder());
            LinkedList<IntentFloor> floors = new LinkedList<>();
            floors.addAll(upFloors);
            floors.addAll(downFloors);
            targetFloors.clear();
            targetFloors.addAll(floors);

            routeFloors.clear();
            int upMaxFloor = 0;
            if (!upFloors.isEmpty()) {
                int upMinFloor = Collections.min(upFloors).getIndex();
                upMaxFloor = Collections.max(upFloors).getIndex();
                routeFloors.addAll(Stream.iterate(upMinFloor, item -> item + 1)
                        .limit(upMaxFloor - upMinFloor + 1)
                        .map(item -> IntentFloor.createFloor(item, Direction.UP))
                        .collect(Collectors.toList()));
            }
            if (!downFloors.isEmpty()) {
                int downMinFloor = Collections.min(downFloors).getIndex();
                int downMaxFloor = Collections.max(downFloors).getIndex();
                downMaxFloor = Math.max(upMaxFloor, downMaxFloor);
                if (downMaxFloor == upMaxFloor) {
                    downMaxFloor -= 1;
                }
                routeFloors.addAll(Stream.iterate(downMaxFloor, item -> item - 1)
                        .limit(downMaxFloor - downMinFloor + 1)
                        .map(item -> IntentFloor.createFloor(item, Direction.DOWN))
                        .collect(Collectors.toList()));
            }

            if (status == Status.AWAIT) {
                status = Status.RUNING;
                if (intentFloor.compareTo(currentFloor) < 0) {
                    currentDirection = Direction.DOWN;
                } else if (intentFloor.compareTo(currentFloor) > 0) {
                    currentDirection = Direction.UP;
                } else {
                    currentDirection = intentFloor.getIntentDirection();
                }
                System.out.println("电梯启动，方向" + currentDirection);
            }
            isAnalysisData = false;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (status == Status.AWAIT || isAnalysisData) {
                continue;
            }
            if (routeFloors.isEmpty()) {
                status = Status.AWAIT;
                currentDirection = currentFloor.getIntentDirection();
                handle(currentFloor);
                System.out.println("电梯停止");
            } else {
                handle(routeFloors.poll());
            }

            if (!routeFloors.isEmpty()) {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void handle(IntentFloor nextFloor) {
        StringBuilder builder = new StringBuilder("到达")
                .append(currentFloor)
                .append("，")
                .append("目标")
                .append(nextFloor);
        System.out.println(builder);
        if (targetFloors.contains(currentFloor)) {
            System.out.println("\33[31m开门\033[0m");
            targetFloors.remove(currentFloor);
        }
        if (currentFloor.compareTo(nextFloor) < 0) {
            currentDirection = Direction.UP;
        } else if (currentFloor.compareTo(nextFloor) > 0) {
            currentDirection = Direction.DOWN;
        }
        System.out.println("方向" + currentDirection);
        currentFloor = nextFloor;
    }
}
