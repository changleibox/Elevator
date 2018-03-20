/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

import me.box.app.elevator.enums.Direction;
import me.box.app.elevator.enums.Status;
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
    private TimerTask mTimerTask;

    public Elevator(List<OutsideFloor> floors) {
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
        synchronized (mLock) {
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
            mTimer.schedule(mTimerTask = new ElevatorTask(this), TIME_APPLICATION_DELAY);
        }
    }

    public void stop() {
        synchronized (mLock) {
            stopTimer();
            status = Status.AWAIT;
            currentDirection = null;
            targetFloors.clear();
            Logger.error("电梯停止");
        }
    }

    void removeFloor(IntentFloor floor) {
        synchronized (mLock) {
            targetFloors.remove(floor);
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

}
