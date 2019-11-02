package com.picfun.googlepurchase

import android.util.Log

/**
 * @author Secret
 * @since 2019/11/2
 */

object KKUtility {

    private const val TAG: String = "KKIab"

    @JvmField internal var isDebug:Boolean = false

    @JvmOverloads
    @JvmStatic
    fun logE(tag:String = TAG,msg:Any){
        if(isDebug){
            Log.e(tag,msg.toString())
        }
    }

}
