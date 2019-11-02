package com.picfun.abreak

import android.app.Activity

import android.os.Bundle
import com.android.billingclient.api.Purchase
import com.picfun.PIXEL_ART_NO_AD_SKU
import com.picfun.PIXEL_ART_PREMIUMS_SKU
import com.picfun.googlepurchase.KKIapHelper
import com.picfun.googlepurchase.KKIapListener
import com.picfun.googlepurchase.KKUtility
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity(), KKIapListener {

    override fun onAcknowledgeItemResult(resultCode: Int) {
        KKUtility.logE(tag = tag,msg = "onAcknowledgeItemResult == $resultCode")
    }

    private val tag = "kkiapListenr=="

    override fun onGoogleConnectListener() {
        KKUtility.logE(tag = tag,msg = "onGoogleConnectListener")
    }

    override fun onGoogleDisConnectListener() {
        KKUtility.logE(tag = tag,msg = "onGoogleDisConnectListener")
    }

    override fun onGoogleConnectErrorListener(code: Int) {
        KKUtility.logE(tag = tag,msg = "onGoogleConnectErrorListener")
    }

    override fun onInventoryQueryFinish(ownedPurchaseList: MutableList<Purchase>) {
        ownedPurchaseList.forEach {
            KKUtility.logE(tag = tag,msg = "onInventoryQueryFinish == ${it.sku}")
            KKIapHelper.consumeAsync(it)
        }
    }

    override fun onPurchaseUpdate(purchaseList: MutableList<Purchase>) {
        purchaseList.forEach {
            KKUtility.logE(tag = tag,msg = "onPurchaseUpdate == ${it.sku}")
        }
    }

    override fun onPurchaseStateError(resultCode: Int) {
        KKUtility.logE(tag = tag,msg = "onPurchaseStateError == $resultCode")
    }

    override fun onPurchaseItemFailed(resultCode: Int, msg: String) {
        KKUtility.logE(tag = tag,msg = "onPurchaseItemFailed == $resultCode == $msg")
    }

    override fun onConsumeAsyncResult(resultCode: Int) {
        KKUtility.logE(tag = tag,msg = "onConsumeAsyncResult == $resultCode")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        circle_iv.setImageDrawable(ColorDrawable())
        purchase_item.setOnClickListener {
            KKIapHelper.startPurchaseFlowByGoogle(sku = PIXEL_ART_PREMIUMS_SKU, activity = this)
        }
        connect.setOnClickListener {
            KKIapHelper.destroy()
        }
        circle_iv.setOnClickListener {
            KKIapHelper.setUpGoogleBillingClient(isDebug = true,
                    isReportEventToServer = true,
                    context = this,
                    iabListener = this,
                    consumeSkuList = null,
                    unConsumeSkuList = listOf(PIXEL_ART_NO_AD_SKU, PIXEL_ART_PREMIUMS_SKU),
                    subsSkuList = null,
                    isAutoConsume = true,
                    base64Key = "")
        }

    }
}