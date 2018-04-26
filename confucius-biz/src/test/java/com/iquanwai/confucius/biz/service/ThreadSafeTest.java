package com.iquanwai.confucius.biz.service;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nethunder on 2017/4/7.
 */
public class ThreadSafeTest {
    public static AtomicInteger count = new AtomicInteger(0);
    public static final Object lock = new Object();
    private void add() throws InterruptedException {
        synchronized (lock) {
            Thread.sleep(2);
            count.addAndGet(1);
            System.out.println(count+"+");
        }
    }

    private void dis() throws InterruptedException {
        synchronized (lock){
            Thread.sleep(2);
            count.addAndGet(-1);
            System.out.println(count+"-");
        }
    }
    @Test
    public void test() throws InterruptedException {
        for(int i=0;i<1000; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        add();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        for(int i=0;i<1000; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        dis();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        Thread.sleep(15000);
        System.out.println(count);
    }

}


