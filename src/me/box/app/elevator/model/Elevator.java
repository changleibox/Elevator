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

import static me.box.app.elevator.common.Constant.TIME_APPLICATION_DELAY;
import static me.box.app.elevator.common.Constant.TIME_DOOR_OPEN;
import static me.box.app.elevator.common.Constant.TIME_ELEVALOR_RUN;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 电梯
 */
@SuppressWarnings({"unused", "InfiniteLoopStatement", "WeakerAccess"})
public class Elevator {

    private final Map<Integer, OutsideFloor> floorsMap;
    private final LinkedList<IntentFloor> targetFloors;
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
            Logger.warning(String.format("不能到达%d楼", index));
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

        if (status == Status.AWAIT) {
            status = Status.RUNING;
            Logger.debug("电梯启动，方向" + currentDirection);
        }
        mTimer.schedule(mTimerTask = new ElevatorTask(targetFloors), TIME_APPLICATION_DELAY);
    }

    public void stop() {
        stopTimer();
        status = Status.AWAIT;
        currentDirection = null;
        targetFloors.clear();
        Logger.error("电梯停止");
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
            builder.append("---->").append("开门");
            targetFloors.remove(currentFloor);
            Logger.error(builder);
            Threads.sleep(TIME_DOOR_OPEN); // 电梯开门一秒钟
        } else {
            Logger.debug(builder);
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

    private class ElevatorTask extends TimerTask {

        private final LinkedList<IntentFloor> mRouteFloors;
        private List<IntentFloor> mTargetFloors;

        ElevatorTask(List<IntentFloor> targetFloors) {
            this.mTargetFloors = targetFloors;
            this.mRouteFloors = new LinkedList<>();
        }

        @Override
        public void run() {
            List<IntentFloor> intentFloors = handleUpDownFloors(mTargetFloors);

            mTargetFloors.clear();
            mTargetFloors.addAll(intentFloors);

            mRouteFloors.clear();
            mRouteFloors.addAll(handleRouteFloors(intentFloors));

            Logger.notset(mRouteFloors);

            while (status == Status.RUNING) {
                if (mRouteFloors.isEmpty()) {
                    handle(currentFloor);
                    stop();
                } else {
                    currentFloor = handle(mRouteFloors.poll());
                }

                Threads.sleep(TIME_ELEVALOR_RUN); // 电梯运行两秒钟
            }
        }

        @Override
        public boolean cancel() {
            boolean cancel = super.cancel();
            mTargetFloors.clear();
            mRouteFloors.clear();
            return cancel;
        }
    }

}
