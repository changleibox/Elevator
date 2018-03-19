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
    private final LinkedList<IntentFloor> targetFloors;
    private final LinkedList<IntentFloor> routeFloors;
    private Direction currentDirection;
    private IntentFloor currentFloor;
    private Status status;
    private boolean isAnalysisData = true;

    public Elevator(List<OutsideFloor> floors) {
        this.status = Status.AWAIT;
        this.floorsMap = new LinkedHashMap<>();
        this.currentFloor = IntentFloor.createFloor(2);
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
            if (!floorsMap.containsKey(index)) {
                System.out.println(String.format("不能到达%d楼", index));
                return;
            }
            isAnalysisData = true;
            int currentIndex = currentFloor.getIndex();
            if (targetFloors.isEmpty()) {
                currentDirection = index < currentIndex ? Direction.DOWN : Direction.UP;
                currentFloor.setIntentDirection(currentDirection);
            }
            if (intentDirection == null) {
                if ((currentDirection == Direction.UP && index < currentIndex)
                        || (currentDirection == Direction.DOWN && index > currentIndex)) {
                    intentDirection = Direction.DOWN;
                } else {
                    intentDirection = Direction.UP;
                }
            }
            IntentFloor intentFloor = IntentFloor.createFloor(index, intentDirection);
            if (!targetFloors.contains(intentFloor)) {
                targetFloors.add(intentFloor);
            }

            List<IntentFloor> intentFloors = handleUpDownFloors(targetFloors);

            targetFloors.clear();
            targetFloors.addAll(intentFloors);

            routeFloors.clear();
            routeFloors.addAll(handleRouteFloors(intentFloors));

            start(intentFloor);
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
                handle(currentFloor);
                status = Status.AWAIT;
                currentDirection = null;
                System.out.println("电梯停止");
            } else {
                currentFloor = handle(routeFloors.poll());
            }

            try {
                Thread.sleep(2000L);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private IntentFloor handle(IntentFloor nextFloor) {
        if (currentFloor.compareTo(nextFloor) < 0) {
            currentDirection = Direction.UP;
        } else if (currentFloor.compareTo(nextFloor) > 0) {
            currentDirection = Direction.DOWN;
        }
        System.out.println("到达" + currentFloor + "---" + "方向" + currentDirection);
        if (targetFloors.contains(currentFloor)) {
            System.out.println("\33[31m开门\033[0m");
            targetFloors.remove(currentFloor);
        }
        return nextFloor;
    }

    private void start(IntentFloor intentFloor) {
        if (status == Status.AWAIT) {
            status = Status.RUNING;
            System.out.println("电梯启动，方向" + currentDirection);
        }
    }

    private List<IntentFloor> handleUpDownFloors(List<IntentFloor> targetFloors) {
        List<IntentFloor> tmpTargetFloors = new ArrayList<>(targetFloors);
        boolean contains = tmpTargetFloors.contains(currentFloor);
        if (!contains) {
            tmpTargetFloors.add(currentFloor);
        }
        Map<Direction, List<IntentFloor>> floorMap = new HashMap<>();
        for (IntentFloor targetFloor : tmpTargetFloors) {
            Direction direction = targetFloor.getIntentDirection();
            List<IntentFloor> intentFloors = floorMap.get(direction);
            if (intentFloors == null) {
                intentFloors = new LinkedList<>();
            }
            intentFloors.add(targetFloor);
            floorMap.put(direction, intentFloors);
        }
        for (Direction direction : floorMap.keySet()) {
            List<IntentFloor> intentFloors = floorMap.get(direction);
            if (direction == Direction.UP) {
                intentFloors.sort(Floor::compareTo);
            } else {
                intentFloors.sort(Comparator.reverseOrder());
            }
            floorMap.put(direction, intentFloors);
        }
        List<IntentFloor> sortedIntentFloors = new LinkedList<>();
        int indexOf = 0;
        List<IntentFloor> intentFloors = floorMap.get(currentDirection);
        if (intentFloors != null) {
            indexOf = intentFloors.indexOf(currentFloor);
            List<IntentFloor> endFloors = intentFloors.subList(indexOf, intentFloors.size());
            sortedIntentFloors.addAll(endFloors);
            sortedIntentFloors.addAll(intentFloors.subList(0, indexOf));
            indexOf = endFloors.size();
        }
        Direction otherDirection = currentDirection == Direction.UP ? Direction.DOWN : Direction.UP;
        List<IntentFloor> otherFloors = floorMap.get(otherDirection);
        if (otherFloors != null) {
            sortedIntentFloors.addAll(indexOf, otherFloors);
        }
        if (!contains) {
            sortedIntentFloors.remove(currentFloor);
        }
        return sortedIntentFloors;
    }

    private List<IntentFloor> handleRouteFloors(List<IntentFloor> targetFloors) {
        List<IntentFloor> tmpTargetFloors = new ArrayList<>(targetFloors);
        tmpTargetFloors.remove(currentFloor);
        tmpTargetFloors.add(0, currentFloor);
        int size = tmpTargetFloors.size();
        List<IntentFloor> routeFloors = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            IntentFloor floor = tmpTargetFloors.get(i);
            if (i == size - 1) {
                routeFloors.add(floor);
                break;
            }
            int index = floor.getIndex();
            int nextIndex = tmpTargetFloors.get(i + 1).getIndex();
            if (index < nextIndex) {
                routeFloors.addAll(Stream.iterate(index, item -> item + 1)
                        .limit(nextIndex - index)
                        .filter(item -> item != 0)
                        .map(item -> IntentFloor.createFloor(item, Direction.UP))
                        .collect(Collectors.toList()));
            } else {
                routeFloors.addAll(Stream.iterate(index, item -> item - 1)
                        .limit(index - nextIndex)
                        .filter(item -> item != 0)
                        .map(item -> IntentFloor.createFloor(item, Direction.DOWN))
                        .collect(Collectors.toList()));
            }
        }
        routeFloors.remove(currentFloor);
        return routeFloors;
    }

}
