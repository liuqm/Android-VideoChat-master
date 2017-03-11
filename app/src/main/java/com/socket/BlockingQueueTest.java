package com.socket;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTest {

    public static BlockingQueue<String> bqueue = new ArrayBlockingQueue<String>(20);

    public static void main(String[] args) throws InterruptedException {

//        for (int i = 0; i < 30; i++) {
//            //将指定元素添加到此队列中
//            bqueue.put("" + i);
//            System.out.println("向阻塞队列中添加了元素:" + i);
//            if (i > 18) {
//                //从队列中获取队头元素，并将其移出队列
//                System.out.println("从阻塞队列中移除元素：" + bqueue.take());
//            }
//        }
//        System.out.println("程序到此运行结束，即将退出----");

        new Thread(new Consumer()).start();
        new Thread(new Producer()).start();
    }


    static class Consumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    bqueue.take();
                    System.out.println("往阻塞队列获取一个元素:" + bqueue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    static class Producer implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    bqueue.put("1");
                    System.out.println("往阻塞队列添加一个元素：" + bqueue.size());
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}  