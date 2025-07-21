package com.swolo.lpy.pysx.main.gp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Administrator
 *
 * @author 猿史森林
 *         Date: 2017/11/28
 *         Class description:
 */
public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        
        // 程序启动时初始化默认设置
        initDefaultSettings();
    }

    /**
     * 初始化默认设置
     */
    private void initDefaultSettings() {
        try {
            SharedPreferences prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE);
            
            // 检查是否已经设置过屏幕方向
            if (!prefs.contains("screen_orientation")) {
                // 如果没有设置过，默认设置为竖屏（0）
                prefs.edit().putInt("screen_orientation", 0).apply();
                Log.d("App", "程序启动：初始化默认屏幕方向为竖屏");
            } else {
                Log.d("App", "程序启动：屏幕方向设置已存在，值为: " + prefs.getInt("screen_orientation", 0));
            }
        } catch (Exception e) {
            Log.e("App", "初始化默认设置失败", e);
        }
    }

    public static Context getContext() {
        return mContext;
    }
}
