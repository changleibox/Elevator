/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.factory;

import java.util.concurrent.ThreadFactory;

/**
 * Created by box on 2018/3/16.
 * <p>
 * 创建线程
 */
public class SimpleThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }
}
