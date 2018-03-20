/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

import me.box.app.elevator.enums.Direction;
import me.box.app.elevator.enums.Status;
import me.box.app.elevator.util.Logger;
import me.box.app.elevator.util.Threads;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.box.app.elevator.common.Constant.TIME_DOOR_OPEN;
import static me.box.app.elevator.common.Constant.TIME_ELEVALOR_RUN;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 电梯运行过程
 */
public class ElevatorTask extends TimerTask {

    private final LinkedList<IntentFloor> mRouteFloors;
    private final List<IntentFloor> mTargetFloors;
    private final Elevator mElevator;

    ElevatorTask(Elevator elevator) {
        this.mElevator = elevator;
        this.mTargetFloors = new LinkedList<>();
        this.mRouteFloors = new LinkedList<>();

        List<IntentFloor> intentFloors = handleUpDownFloors(elevator.getTargetFloors());

        mTargetFloors.addAll(intentFloors);
        mRouteFloors.addAll(handleRouteFloors(intentFloors));
        Logger.notset(mRouteFloors);
    }

    @Override
    public void run() {
        while (mElevator.getStatus() == Status.RUNING) {
            IntentFloor currentFloor;
            if (mRouteFloors.isEmpty()) {
                currentFloor = handle(mElevator.getCurrentFloor());
                mElevator.stop();
            } else {
                currentFloor = handle(mRouteFloors.poll());
                mElevator.setCurrentDirection(currentFloor.getIntentDirection());
            }
            mElevator.setCurrentFloor(currentFloor);

            Threads.sleep(TIME_ELEVALOR_RUN); // 电梯运行两秒钟
        }
    }

    private IntentFloor handle(IntentFloor currentFloor) {
        StringBuilder builder = new StringBuilder("到达")
                .append(currentFloor)
                .append("方向")
                .append(currentFloor.getIntentDirection());
        if (mTargetFloors.contains(currentFloor)) {
            builder.append("---->").append("开门");
            mTargetFloors.remove(currentFloor);
            Logger.error(builder);
            Threads.sleep(TIME_DOOR_OPEN); // 电梯开门一秒钟
        } else {
            Logger.debug(builder);
        }
        return currentFloor;
    }

    private List<IntentFloor> handleUpDownFloors(List<IntentFloor> targetFloors) {
        IntentFloor currentFloor = mElevator.getCurrentFloor();
        Direction currentDirection = mElevator.getCurrentDirection();
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
        IntentFloor currentFloor = mElevator.getCurrentFloor();
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
        return new ArrayList<>(new LinkedHashSet<>(routeFloors));
    }
}