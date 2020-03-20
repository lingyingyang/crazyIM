package com.crazymakercircle.cocurrent;

import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class CallbackTaskScheduler extends Thread {
    private static CallbackTaskScheduler scheduler = new CallbackTaskScheduler();
    // 任务队列
    private ConcurrentLinkedQueue<CallbackTask> taskQueue = new ConcurrentLinkedQueue<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    ListeningExecutorService pool = MoreExecutors.listeningDecorator(executorService);

    private CallbackTaskScheduler() {
        this.start();
    }

    /**
     * 添加任务
     */
    public static <R> void add(CallbackTask<R> task) {
        scheduler.taskQueue.add(task);
    }

    @Override
    public void run() {
        while (true) {
            handleTask();// 处理任务
            // 线程休眠时间
            long sleepTime = 200;
            threadSleep(sleepTime);
        }
    }

    private void threadSleep(long time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 处理任务队列，检查其中是否有任务
     */
    private void handleTask() {
        try {
            while (taskQueue.peek() != null) {
                CallbackTask task = taskQueue.poll();
                handleTask(task);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 执行任务操作
     */
    private <R> void handleTask(CallbackTask<R> task) {

        ListenableFuture<R> future = pool.submit(task::execute);

        Futures.addCallback(future, new FutureCallback<R>() {
            public void onSuccess(R r) {
                task.onBack(r);
            }
            public void onFailure(Throwable t) {
                task.onException(t);
            }
        }, pool);
    }
}
