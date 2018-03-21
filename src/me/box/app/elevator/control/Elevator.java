/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.control;

import me.box.app.elevator.enums.Direction;
import me.box.app.elevator.enums.Status;
import me.box.app.elevator.model.IntentFloor;
import me.box.app.elevator.model.OutsideFloor;
import me.box.app.elevator.util.Logger;

import java.util.*;

import static me.box.app.elevator.common.Constant.TIME_APPLICATION_DELAY;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 电梯
 */
public class Elevator {

    private final Object mLock = new Object();

    private final Map<Integer, OutsideFloor> floorsMap;
    private final LinkedList<IntentFloor> targetFloors;
    private Direction currentDirection;
    private IntentFloor currentFloor;
    private Status status;

    private final Timer mTimer = new Timer();
    private ElevatorTask mTimerTask;

    Elevator(List<OutsideFloor> floors) {
        this.status = Status.AWAIT;
        this.floorsMap = new LinkedHashMap<>();
        this.currentFloor = IntentFloor.createFloor(3);
        this.targetFloors = new LinkedList<>();
        floors.stream().sorted().forEach(floor -> floorsMap.put(floor.getIndex(), floor));
    }

    public Status getStatus() {
        return status;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public IntentFloor getCurrentFloor() {
        return currentFloor;
    }

    public List<IntentFloor> getTargetFloors() {
        return Collections.unmodifiableList(targetFloors);
    }

    void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    void setCurrentFloor(IntentFloor currentFloor) {
        this.currentFloor = currentFloor;
    }

    void removeFloor(IntentFloor floor) {
        synchronized (mLock) {
            targetFloors.remove(floor);
        }
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
        synchronized (mLock) {
            IntentFloor intentFloor = handleNewFloor(index, intentDirection);
            if (intentFloor == null) {
                return;
            }
            Logger.notset("新加楼层", "新加入" + intentFloor);

            List<IntentFloor> orgTargetFloors = null;
            List<IntentFloor> routeFloors = null;
            if (mTimerTask != null) {
                orgTargetFloors = mTimerTask.getTargetFloors();
                routeFloors = mTimerTask.getRouteFloors();
            }
            stopTimer();

            targetFloors.add(intentFloor);

            if (status == Status.AWAIT) {
                status = Status.RUNING;
                Logger.error("电梯状态", "电梯启动，方向" + currentFloor);
            }
            if (routeFloors != null && routeFloors.contains(intentFloor)) {
                orgTargetFloors = new ArrayList<>(orgTargetFloors);
                orgTargetFloors.add(intentFloor);
                mTimerTask = new ElevatorTask(this, orgTargetFloors, routeFloors);
            } else {
                mTimerTask = new ElevatorTask(this);
            }
            mTimer.schedule(mTimerTask, TIME_APPLICATION_DELAY);
        }
    }

    public void stop() {
        synchronized (mLock) {
            stopTimer();
            status = Status.AWAIT;
            currentDirection = null;
            targetFloors.clear();
            Logger.error("电梯状态", "电梯停止");
        }
    }

    private void stopTimer() {
        synchronized (mLock) {
            if (mTimerTask != null) {
                mTimerTask.cancel();
                mTimerTask = null;
            }
            mTimer.purge();
        }
    }

    private boolean isInvalidIntentFloor(int index, Direction intentDirection) {
        OutsideFloor outsideFloor = floorsMap.get(index);
        if (outsideFloor == null) {
            Logger.warning(String.format("不能到达%d楼", index));
            return true;
        }
        return !outsideFloor.containsDirection(intentDirection);
    }

    private IntentFloor handleNewFloor(int index, Direction intentDirection) {
        if (isInvalidIntentFloor(index, intentDirection)) {
            Logger.warning(String.format("不能到达%d楼", index));
            return null;
        }
        IntentFloor intentFloor = IntentFloor.createFloor(index, intentDirection);
        if (targetFloors.contains(intentFloor)) {
            return null;
        }

        int currentIndex = currentFloor.getIndex();
        if (targetFloors.isEmpty()) {
            currentDirection = index < currentIndex ? Direction.DOWN : Direction.UP;
            currentFloor.setIntentDirection(currentDirection);
        }

        return intentFloor;
    }

}
