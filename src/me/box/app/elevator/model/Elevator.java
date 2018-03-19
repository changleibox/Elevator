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
@SuppressWarnings({"unused", "InfiniteLoopStatement", "WeakerAccess"})
public class Elevator implements Runnable {

    private final Map<Integer, OutsideFloor> floorsMap;
    private final LinkedList<IntentFloor> targetFloors;
    private final LinkedList<IntentFloor> routeFloors;
    private Direction currentDirection;
    private IntentFloor currentFloor;
    private Status status;

    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;

    public Elevator(List<OutsideFloor> floors) {
        this.status = Status.AWAIT;
        this.floorsMap = new LinkedHashMap<>();
        this.currentFloor = IntentFloor.createFloor(3);
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
        if (!floorsMap.containsKey(index)) {
            System.out.println(String.format("不能到达%d楼", index));
            return;
        }

        stopTimer();

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

        if (status == Status.AWAIT) {
            status = Status.RUNING;
            System.out.println("\033[36m电梯启动，方向" + currentDirection + "\033[0m");
        }
        System.out.println(Arrays.toString(routeFloors.toArray()));
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Elevator.this.run();
            }
        };
        mTimer.schedule(mTimerTask, 500L);
    }

    @Override
    public void run() {
        while (status == Status.RUNING) {
            if (routeFloors.isEmpty()) {
                handle(currentFloor);
                stop();
            } else {
                currentFloor = handle(routeFloors.poll());
            }

            try {
                Thread.sleep(2000L); // 电梯运行两秒钟
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void stop() {
        stopTimer();
        status = Status.AWAIT;
        currentDirection = null;
        routeFloors.clear();
        targetFloors.clear();
        System.out.println("\33[31m电梯停止\033[0m");
    }

    private void stopTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        mTimer.purge();
    }

    private IntentFloor handle(IntentFloor nextFloor) {
        if (currentFloor.compareTo(nextFloor) < 0) {
            currentDirection = Direction.UP;
        } else if (currentFloor.compareTo(nextFloor) > 0) {
            currentDirection = Direction.DOWN;
        }
        StringBuilder builder = new StringBuilder("到达")
                .append(currentFloor)
                .append("方向")
                .append(currentDirection);
        if (targetFloors.contains(currentFloor)) {
            builder.append("\n");
            builder.append("\33[92m开门\033[0m");
            targetFloors.remove(currentFloor);
            System.out.println(builder);
            try {
                Thread.sleep(1000L); // 电梯开门一秒钟
            } catch (InterruptedException ignored) {
            }
        } else {
            System.out.println(builder);
        }
        return nextFloor;
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
        if (intentFloors != null && (indexOf = intentFloors.indexOf(currentFloor)) != -1) {
            List<IntentFloor> endFloors = intentFloors.subList(indexOf, intentFloors.size());
            sortedIntentFloors.addAll(endFloors);
            sortedIntentFloors.addAll(intentFloors.subList(0, indexOf));
            indexOf = endFloors.size();
        }
        Direction otherDirection = currentDirection == Direction.UP ? Direction.DOWN : Direction.UP;
        List<IntentFloor> otherFloors = floorMap.get(otherDirection);
        if (otherFloors != null && indexOf != -1) {
            sortedIntentFloors.addAll(indexOf, otherFloors);
        }
        if (!contains) {
            sortedIntentFloors.remove(currentFloor);
        }
        return new ArrayList<>(new LinkedHashSet<>(sortedIntentFloors));
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
        return new ArrayList<>(new LinkedHashSet<>(routeFloors));
    }

}
