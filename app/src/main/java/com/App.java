package com;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

//import com.nercms.service.LongConnService;

/**
 * Created by lqm on 2016/9/19.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        startLongConn();

    }


//    public void startLongConn() {
//        quitLongConn();
//        Log.i("lqm", "长连接服务已开启");
//        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(this, LongConnService.class);
//        intent.setAction(LongConnService.ACTION);
//        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        long triggerAtTime = SystemClock.elapsedRealtime();
//        manager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, 60 * 1000, pendingIntent);
//    }


//    public void quitLongConn() {
//
//    }
}
