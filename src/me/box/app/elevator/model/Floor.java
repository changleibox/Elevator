/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.model;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 楼层
 */
@SuppressWarnings("unused")
public class Floor implements Comparable<Floor> {

    private int index;

    Floor(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(Floor o) {
        return Integer.compare(index, o.index);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Floor && ((Floor) obj).index == index;
    }

    @Override
    public String toString() {
        return String.format("%d楼", index);
    }
}
