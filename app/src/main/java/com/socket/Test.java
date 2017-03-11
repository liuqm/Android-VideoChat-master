package com.socket;

/**
 * Created by lqm on 2016/9/20.
 */
public class Test {
    public static void main(String[] args) {
        Thread t=Thread.currentThread();
        System.out.println("t.isInterrupted():"+t.isInterrupted());
        t.interrupt();
        System.out.println("调用t.interrupt()之后，"+t.isInterrupted());
        try {
            Thread.sleep(2000);
            System.out.println("was NOT interrupted");
        } catch (InterruptedException e) {
            System.out.println("was interrupted");
        }
        //跑出异常后，会清除中断标志，这里会返回false
        System.out.println("t.isInterrupted()=" + t.isInterrupted());
    }
}
