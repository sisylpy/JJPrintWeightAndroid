package com.swolo.lpy.pysx.main.gp;


import java.util.concurrent.ThreadFactory;

import androidx.annotation.NonNull;

/**
 * Created by Administrator
 *
 * @author 猿史森林
 *         Date: 2017/11/2
 *         Class description:
 */
public class ThreadFactoryBuilder implements ThreadFactory {

    private String name;
    private int counter;

    public ThreadFactoryBuilder(String name) {
        this.name = name;
        counter = 1;
    }

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        Thread thread = new Thread(runnable, name);
        thread.setName("ThreadFactoryBuilder_" + name  + counter);
        return thread;
    }
}
