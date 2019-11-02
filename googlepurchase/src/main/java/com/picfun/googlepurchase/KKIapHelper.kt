package com.picfun.googlepurchase

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.picfun.googlepurchase.KKUtility.logE
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Secret
 * @since 2019/11/1
 */

object KKIapHelper : PurchasesUpdatedListener, ConsumeResponseListener {

    private var billingClient: BillingClient? = null
    private var iabListener:KKIapListener? = null
    private var isDebug: Boolean = false
    private var isReportEventToServer = false
    private var skuHashMap: ConcurrentHashMap<String, SkuDetails> = ConcurrentHashMap()
    private var consumeSkuList = ArrayList<String>()
    private var unConsumeSkuList = ArrayList<String>()
    private var subsSkuList = ArrayList<String>()
    private var purchaseList = ArrayList<Purchase>()
    private var isAutoConsume = true
    private lateinit var base64KEY:String

    fun setUpGoogleBillingClient(isDebug: Boolean
                                 ,isReportEventToServer:Boolean
                                 ,context: Context
                                 ,consumeSkuList: List<String>?
                                 ,unConsumeSkuList:List<String>?
                                 ,subsSkuList: List<String>?
                                 ,isAutoConsume:Boolean
                                 ,iabListener: KKIapListener?
                                 ,base64Key:String) {

        logE(msg = "setUp GoogleBillingClient start...")

        billingClient?.isReady.let {
            if(it == true){
                logE(msg = "BillingClient already ready")
                KKIapHelper.iabListener?.onGoogleConnectListener()
                return
            }
        }

        KKUtility.isDebug = isDebug
        this.isAutoConsume = isAutoConsume
        this.iabListener = iabListener
        this.base64KEY = base64Key
        this.isReportEventToServer = isReportEventToServer
        this.isDebug = isDebug
        consumeSkuList?.let {
            this.consumeSkuList.addAll(it)
        }
        unConsumeSkuList?.let {
            this.unConsumeSkuList.addAll(it)
        }
        subsSkuList?.let {
            this.subsSkuList.addAll(it)
        }

        if(null == billingClient){
            billingClient = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()
        }

        if(isReportEventToServer){
            ServerEvent.init(context)
        }

        connectGooglePlay(Runnable {
            queryInventory()
        })
    }

    private fun connectGooglePlay(executeOnSuccess:Runnable){

        logE(msg = "connect start...")

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                logE(msg = "connect end... ${billingResult.responseCode} ... ${billingResult.debugMessage}")

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    iabListener?.onGoogleConnectListener()
                    querySkuInfo(unConsumeSkuList,consumeSkuList,subsSkuList)
                    executeOnSuccess.run()
                }else{
                    iabListener?.onGoogleConnectErrorListener(billingResult.responseCode)
                }
            }

            override fun onBillingServiceDisconnected() {
                logE(msg = "disconnected...")
                iabListener?.onGoogleDisConnectListener()
            }
        })
    }

    private fun executeOrConnect(runnable: Runnable){
        billingClient?.apply {
            if(this.isReady){
                runnable.run()
            }else{
                connectGooglePlay(runnable)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        logE(msg = "onPurchasesUpdated responseCode is ${
        billingResult?.let { 
            when(it.responseCode){
                BillingClient.BillingResponseCode.OK -> "purchase success"
                BillingClient.BillingResponseCode.ERROR -> "purchase error"
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
                BillingClient.BillingResponseCode.USER_CANCELED -> "purchase cancel"
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "purchase already owned"
                else -> {it.responseCode.toString()}
            }
        }
        }")
        logE(msg = "onPurchasesUpdated purchases size is ${purchases?.size}")
        billingResult?.apply {
            when (this.responseCode) {
                BillingClient.BillingResponseCode.OK -> purchases?.apply {
                    purchaseList.clear()
                    this.forEach {
                        try {
                            if(Security.verifyPurchase(base64PublicKey = this@KKIapHelper.base64KEY,
                                            signedData = it.originalJson,signature = it.signature)){
                                logE(msg = "purchase item verity success")
                                logE(msg = "purchase item == $it")
                                purchaseList.add(it)

                                if(isReportEventToServer){
                                    ServerEvent.reportPaymentEvent(it)
                                }

                            }
                        }catch (e:Exception){
                            logE(msg = "purchase verify failed == ${e.message}")
                            iabListener?.onPurchaseItemFailed(resultCode = 0,msg = "purchase verify failed == ${e.message}")
                        }
                    }
                    handlePurchase(purchaseList)

                    iabListener?.onPurchaseUpdate(purchaseList)
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    iabListener?.onPurchaseItemFailed(resultCode = this.responseCode,msg = "USER_CANCELED")
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    iabListener?.onPurchaseItemFailed(resultCode = this.responseCode,msg = "ITEM_ALREADY_OWNED")
                }
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                    iabListener?.onPurchaseItemFailed(resultCode = this.responseCode,msg = "ITEM_UNAVAILABLE")
                }
                else -> {
                    iabListener?.onPurchaseItemFailed(resultCode = this.responseCode,msg = this.debugMessage)
                }
            }
        }
    }

    private fun handlePurchase(purchaseList:MutableList<Purchase>) {
        for(purchase in purchaseList){
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if(subsSkuList.contains(purchase.sku) || unConsumeSkuList.contains(purchase.sku)){
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .setDeveloperPayload(purchase.developerPayload)
                                .build()
                        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) {
                            iabListener?.onAcknowledgeItemResult(it.responseCode)
                            logE(msg = "acknowledge purchase item == ${it.responseCode}")
                        }
                    }
                }
                else if(consumeSkuList.contains(purchase.sku)){
                    if(isAutoConsume){
                        consumeAsync(purchase)
                    }
                }
            } else{
                iabListener?.onPurchaseStateError(purchase.purchaseState)
            }
        }
    }

    private fun querySkuInfo(consumeSkuList: List<String>?, unConsumeSkuList: List<String>?, subsSkuList: List<String>?) {

        logE(msg = "querySkuDetail start ==")

        val inAppSkuList = ArrayList<String>()
        consumeSkuList?.let {
            inAppSkuList.addAll(it)
        }
        unConsumeSkuList?.let {
            inAppSkuList.addAll(unConsumeSkuList)
        }
        if(inAppSkuList.size > 0){
            querySkuDetails(inAppSkuList, BillingClient.SkuType.INAPP)
        }
        subsSkuList?.apply {
            querySkuDetails(this, BillingClient.SkuType.SUBS)
        }

        logE(msg = "querySkuDetail end ==")

    }

    override fun onConsumeResponse(billingResult: BillingResult?, purchaseToken: String?) {
        logE(msg = "consumeAsync,response code is ${billingResult?.responseCode}")
        billingResult?.let {
            iabListener?.onConsumeAsyncResult(billingResult.responseCode)
        }
    }

    private fun querySkuDetails(skuList: List<String>, skuType: String) {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(skuType)
        billingClient?.querySkuDetailsAsync(params.build()) { _, skuDetails ->

            if(isReportEventToServer){
                ServerEvent.collectSkuDetails(skuDetails)
            }

            // Process the result.
            skuDetails.forEach {
                logE(msg = "exist sku ==  ${it.originalJson}")
                skuHashMap[it.sku] = it
            }
        }
    }

    /**
     * start Google purchase flow
     */
    fun startPurchaseFlowByGoogle(sku: String, isAutoConsume: Boolean = true, activity: Activity) {
        val purchaseRunnable = Runnable {
            if(isDebug){
                skuHashMap.forEach {
                    logE(msg = "hashMap exist sku == ${it.key}")
                }
            }
            this.isAutoConsume = isAutoConsume
            if (skuHashMap.containsKey(sku)) {
                val skuDetails = skuHashMap[sku]
                skuDetails?.apply {
                    logE(msg = "start purchase ${this.sku}")
                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                    val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build()
                    billingClient?.launchBillingFlow(activity, flowParams)
                }
            } else {
                iabListener?.onPurchaseItemFailed(resultCode = -1,msg = "skuHashMap don't contains this sku")
                logE(msg = "purchase error")
            }
        }

        executeOrConnect(purchaseRunnable)

    }

    /**
     * consume item
     */
    fun consumeAsync(purchase:Purchase) {
        executeOrConnect(Runnable {
            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient?.consumeAsync(consumeParams,this)
        })
    }

    /**
     * restore purchase
     */
    fun queryInventory(){
        val queryRunnable = Runnable {
            var purchaseList = ArrayList<Purchase>()
            billingClient?.apply {
                if(this.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode == BillingClient.BillingResponseCode.OK){
                    val subsResult = queryPurchases(BillingClient.SkuType.SUBS)
                    if(subsResult.responseCode == BillingClient.BillingResponseCode.OK){
                        purchaseList.addAll(subsResult.purchasesList)
                    }
                }
                val inAppResult = queryPurchases(BillingClient.SkuType.INAPP)
                if(inAppResult.responseCode == BillingClient.BillingResponseCode.OK){
                    purchaseList.addAll(inAppResult.purchasesList)
                }
            }
            logE(msg = "verify pre inventory itemSize  == ${purchaseList.size} ")
            purchaseList = purchaseList.filter {
                try {
                    Security.verifyPurchase(base64KEY,it.originalJson,it.signature)
                }catch (e:Exception){
                    logE(msg = "inventory verity failed == ${it.sku==e.message}")
                    false
                }
            } as ArrayList<Purchase>
            logE(msg = "verity end inventory itemSize  == ${purchaseList.size} ")
            if(isDebug){
                purchaseList.forEach {
                    logE(msg = "inventory item is  == $it")
                }
            }
            handlePurchase(purchaseList)
            iabListener?.onInventoryQueryFinish(purchaseList)
        }
        executeOrConnect(queryRunnable)
    }

    /**
     * when app exist,destroy connection
     */
    fun destroy(){
        billingClient?.apply {
            if(isReady){
                endConnection()
            }
        }
    }

}