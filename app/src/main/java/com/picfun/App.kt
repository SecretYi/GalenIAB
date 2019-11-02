package com.picfun

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.multidex.MultiDex

/**
 * @author Secret
 * @since 2019/10/14
 */
class App :Application(){

    override fun onCreate() {
        super.onCreate()

        Log.e("CJY==test","app==onCreate")
        Log.e("CJY==test==appName",packageName)
        Log.e("CJY==test","processName==" + getProcessName(this, Process.myPid()))

//        YouzanSDK.init(this,"190725",YouZanSDKX5Adapter())

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityPaused(activity: Activity?) {
                Log.e("CJY==lifeCircle",activity!!.javaClass.simpleName + "==onActivityPaused")
            }

            override fun onActivityResumed(activity: Activity?) {
                Log.e("CJY==lifeCircle",activity!!.javaClass.simpleName + "==onActivityResumed")
            }

            override fun onActivityStarted(activity: Activity?) {
                Log.e("CJY==lifeCircle",activity!!.javaClass.simpleName + "==onActivityStarted")
            }

            override fun onActivityDestroyed(activity: Activity?) {
                Log.e("CJY==lifeCircle",activity!!.javaClass.simpleName + "==onActivityDestroyed")
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
                Log.e("CJY==lifeCircle",activity!!.javaClass.simpleName + "==onActivitySaveInstanceState")
            }

            override fun onActivityStopped(activity: Activity?) {
                Log.e("CJY==lifeCircle",activity!!.javaClass.simpleName + "==onActivityStopped")
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                Log.e("CJY==lifeCircle",activity!!.javaClass.simpleName + "==onActivityCreated")
            }

        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    /**
     * @return null may be returned if the specified process not found
     */
    fun getProcessName(cxt: Context, pid: Int): String? {
        val am = cxt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return null
        for (procInfo in runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName
            }
        }
        return null
    }

}