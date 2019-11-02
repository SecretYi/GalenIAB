package com.picfun.googlepurchase

import com.android.billingclient.api.Purchase

/**
 * @author Secret
 * @since 2019/11/2
 */
interface KKIapListener {

    fun onGoogleConnectListener()

    fun onGoogleDisConnectListener()

    fun onGoogleConnectErrorListener(code: Int)

    fun onInventoryQueryFinish(ownedPurchaseList:MutableList<Purchase>)

    fun onPurchaseUpdate(purchaseList:MutableList<Purchase>)

    fun onPurchaseStateError(resultCode:Int)

    fun onPurchaseItemFailed(resultCode:Int,msg:String)

    fun onConsumeAsyncResult(resultCode: Int)

    fun onAcknowledgeItemResult(resultCode: Int)

}