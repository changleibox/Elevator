/*
 * Copyright © 2018 CHANGLEI. All rights reserved.
 */

package me.box.app.elevator.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by box on 2018/3/20.
 * <p>
 * 负责打印日志
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Logger {

    private static final Map<Level, String> COLORS = new HashMap<>();

    public enum Level {
        HEADER, INFO, DEBUG, WARNING, ERROR, CRITICAL, NOTSET, ENDC
    }

    static {
        COLORS.put(Level.HEADER, "\033[30m");
        COLORS.put(Level.INFO, "\033[96m");
        COLORS.put(Level.DEBUG, "\033[36m");
        COLORS.put(Level.WARNING, "\033[92m");
        COLORS.put(Level.ERROR, "\033[31m");
        COLORS.put(Level.CRITICAL, "\033[33m");
        COLORS.put(Level.NOTSET, "\033[0m");
        COLORS.put(Level.ENDC, "\033[0m");
    }

    public static void header(Object msg) {
        log(Level.HEADER, msg);
    }

    public static void info(Object msg) {
        log(Level.INFO, msg);
    }

    public static void debug(Object msg) {
        log(Level.DEBUG, msg);
    }

    public static void warning(Object msg) {
        log(Level.WARNING, msg);
    }

    public static void error(Object msg) {
        log(Level.ERROR, msg);
    }

    public static void critical(Object msg) {
        log(Level.CRITICAL, msg);
    }

    public static void notset(Object msg) {
        log(Level.NOTSET, msg);
    }

    public static void log(Level level, Object msg) {
        System.out.println(formatMsg(level, msg));
    }

    public static String formatMsg(Level level, Object msg) {
        if (msg != null && msg instanceof Collection) {
            msg = ((Collection) msg).toArray();
        }
        if (msg != null && msg.getClass().isArray()) {
            msg = Arrays.toString((Object[]) msg);
        }
        return String.format("%s%s: %s%s", COLORS.get(level), level.name(), msg, COLORS.get(Level.ENDC));
    }
}
