package com.socket;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lqm on 2016/9/19.
 */
public class KeepAlive implements Serializable {

    private static final long serialVersionUID = -2813120366138988480L;

    @Override
    public String toString() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}
