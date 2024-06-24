/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 */
package me.liwk.karhu.util.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class KarhuThreadManager {
    private static final List<ExecutorService> activeServices = new ArrayList<ExecutorService>();

    public static void shutdown() {
        activeServices.forEach(ExecutorService::shutdown);
        activeServices.clear();
    }

    public static ScheduledExecutorService createNewScheduledExecutor(String name) {
        return KarhuThreadManager.createNewScheduledExecutor(1, name);
    }

    public static ScheduledExecutorService createNewScheduledExecutor(int no, String name) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(no, new ThreadFactoryBuilder().setNameFormat(name).build());
        activeServices.add(service);
        return service;
    }

    public static ExecutorService createNewNormalExecutor(String name) {
        ExecutorService service = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat(name).build());
        activeServices.add(service);
        return service;
    }

    public static ExecutorService createNewExecutor(String name) {
        return KarhuThreadManager.createNewExecutor(1, name);
    }

    public static ExecutorService createNewExecutor(int no, String name) {
        ExecutorService service = Executors.newFixedThreadPool(no, new ThreadFactoryBuilder().setNameFormat(name).build());
        activeServices.add(service);
        return service;
    }

    public static ExecutorService createNewExecutor(int no) {
        ExecutorService service = Executors.newFixedThreadPool(no);
        activeServices.add(service);
        return service;
    }

    public static ExecutorService createNewExecutor() {
        return KarhuThreadManager.createNewExecutor(1);
    }
}

