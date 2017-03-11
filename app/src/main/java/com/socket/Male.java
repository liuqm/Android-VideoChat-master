package com.socket;

/**
 * Created by lqm on 2016/9/20.
 */
public class Male extends People implements NoticeCallBack{


    public Male(String name, String sex, int age) {
        super(name, sex, age);
    }

    @Override
    public void callback() {
        System.out.println("通知");
    }
}
