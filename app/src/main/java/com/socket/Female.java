package com.socket;

/**
 * Created by lqm on 2016/9/20.
 */
public class Female extends People {

    private NoticeCallBack callBack;


    public Female(String name, String sex, int age) {
        super(name, sex, age);
    }

    public void setCallBack(NoticeCallBack callBack) {
        this.callBack = callBack;
    }

    public void go() {
        System.out.println("执行go方法");
        //我要走了,通知另外一个人
        callBack.callback();
    }
}
