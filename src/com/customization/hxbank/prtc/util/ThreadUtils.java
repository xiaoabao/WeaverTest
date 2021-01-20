package com.customization.hxbank.prtc.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 多线程相关工具类
 * Created by YeShengtao on 2020/9/27 12:08
 */
public class ThreadUtils {

    private static ThreadPoolExecutor executor = null;

    /**
     * 单例模式返回ThreadPoolExecutor - 5, 5, 0L
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getPoolExecutor() {
        if (executor != null) {
            return executor;
        } else {
            synchronized (ThreadUtils.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(5, 5, 0L,
                            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
                }
            }
        }
        return executor;
    }

}
