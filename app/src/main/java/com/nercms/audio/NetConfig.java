package com.nercms.audio;

import android.util.Log;

/*
 * UDP configure
 */
public class NetConfig {
	public static String SERVER_HOST = "192.168.1.182";// server ip
	public static final int SERVER_PORT = 12234;// server port
//	public static final int CLIENT_PORT = 12235;// client port
	public static void setServerHost(String ip) {
		Log.e("NetConfig", "重新设置的IP是" + ip);
		SERVER_HOST = ip;
	}
}
