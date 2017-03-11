package com.socket;

/**
 * Created by lqm on 2016/9/20.
 */
public class People {

    private String sex;
    private String name;
    private int age;

    public People(String name, String sex, int age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }


    @Override
    public String toString() {
        return name;
    }
}
