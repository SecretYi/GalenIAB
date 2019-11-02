package com.picfun.googlepurchase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import org.json.JSONObject;

import java.io.Closeable;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import static com.picfun.googlepurchase.KKUtility.logE;

/**
 * @author Secret
 * @since 2019/10/9
 */
public class ServerEvent {

    private static final SimpleDateFormat sTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final String SERVER_URL = "http://verify.gameinlife.io/subscribe/android/order";
    private static boolean isInit;
    private static ArrayList<SkuDetails> sSkuDetailsList = new ArrayList<>();
    private static String appName = "";
    private static volatile String AdID = "";
    private static String deviceId = "";
    private static String country = "";
    private static String language = "";

    /**
     * invoke this method to collect all purchase sku info
     */
    static void collectSkuDetails(List<SkuDetails> skuDetails){
        sSkuDetailsList.addAll(skuDetails);
    }

    /**
     * initial method to collect the device info,the earlier the better
     */
    public static void init(Context context) {
        isInit = true;
        getGPAdId(context);
        appName = getAppName(context);
        country = getCountry(context);
        deviceId = getDeviceId(context);
        language = getLanguage(context);
    }

    /**
     * report the Google Payment to Server
     */
    static void reportPaymentEvent(Purchase purchase) {
        logE("report purchase to server start...");
        Executors.newSingleThreadExecutor().execute(() -> {
            OutputStreamWriter osw;
            HttpURLConnection connection = null;
            try {
                connection = getHttpUrlConnection();
                String reportJson = getReportJson(purchase);
                logE(String.format("json==%s", reportJson));
                osw = new OutputStreamWriter(connection.getOutputStream());
                osw.write(reportJson);
                osw.flush();
                closeStream(osw);
                connection.connect();
                logE(connection.getResponseCode() + connection.getResponseMessage());
                recordReportErrorEvent(connection.getResponseCode());
            } catch (Exception e) {
                recordReportErrorEvent(e.getMessage());
                logE(String.format("connect error : %s", e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static SkuDetails querySkuDetail(Purchase purchase){
        if(sSkuDetailsList.size() == 0){
            return null;
        }
        for (SkuDetails skuDetail :
                sSkuDetailsList) {
            if (skuDetail.getSku().equalsIgnoreCase(purchase.getSku())) {
                return skuDetail;
            }
        }
        return null;
    }

    /**
     * record error event to analyze
     */
    private static void recordReportErrorEvent(Object o) {

    }

    private static HttpURLConnection getHttpUrlConnection() {
        if (BuildConfig.DEBUG && !isInit) {
            throw new RuntimeException("you should be invoke init function before invoke this function");
        }
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setReadTimeout(50000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("accept", "application/json");
            return httpURLConnection;
        } catch (Exception e) {
            logE(e.getMessage());
        }
        return null;
    }

    private static String getReportJson(Purchase purchase) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("adId", AdID);
            jsonObject.put("appName", appName);
            jsonObject.put("appVersion", BuildConfig.VERSION_NAME);
            jsonObject.put("country", country);
            jsonObject.put("deviceId", deviceId);
            jsonObject.put("environment", BuildConfig.DEBUG ? "sandbox" : "release");
            jsonObject.put("language", language);
            jsonObject.put("orderDistinctId", purchase.getOrderId());
            jsonObject.put("orderProductId", purchase.getSku());
            jsonObject.put("orderTime", sTimeFormat.format(purchase.getPurchaseTime()));
            jsonObject.put("orderToken", purchase.getPurchaseToken());
            jsonObject.put("orderType", getPurchaseType(purchase));
            jsonObject.put("orderPrice",getPurchasePrice(purchase));
            jsonObject.put("orderCurrency",getPurchaseCurrency(purchase));
            jsonObject.put("osVersion", Build.VERSION.RELEASE);
            jsonObject.put("packageName", purchase.getPackageName());
            return jsonObject.toString();
        } catch (Exception e) {
            logE(String.format("get report json error : %s", e.getMessage()));
        }
        return "";
    }

    @SuppressLint("HardwareIds")
    private static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (TextUtils.isEmpty(deviceId)) {
                WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifi != null) {
                    WifiInfo info = wifi.getConnectionInfo();
                    deviceId = info.getMacAddress().replace(":", "");
                }
            }
        } catch (Exception e) {
            logE(e.getMessage());
        }
        return deviceId;
    }

    private static String getCountry(Context context) {
        String country = context.getResources().getConfiguration().locale.getCountry();
        return TextUtils.isEmpty(country) ? "" : country;
    }

    private static String getLanguage(Context context) {
        String language = context.getResources().getConfiguration().locale.getLanguage();
        return TextUtils.isEmpty(language) ? "" : language;
    }

    private static void getGPAdId(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Class<?> advertisingIdClient = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
                Method method = advertisingIdClient.getMethod("getAdvertisingIdInfo",Context.class);
                Object infoObject = method.invoke(null,context);
                Class<?>[] classes = advertisingIdClient.getDeclaredClasses();
                for (Class<?> aClass : classes) {
                    if ("com.google.android.gms.ads.identifier.AdvertisingIdClient.Info".equalsIgnoreCase(
                            aClass.getCanonicalName()
                    )) {
                        Method method1 = aClass.getMethod("getId");
                        AdID = (String) method1.invoke(infoObject);
                    }
                }
                logE("adId ==" + AdID);
//              adId = AdvertisingIdClient.getAdvertisingIdInfo(context).getId();
            } catch (Exception e) {
                logE(String.format("IOException when trying to get Advertising Id Info: %s", e.getMessage()));
            }
        });
    }



    private static void closeStream(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static long getPurchasePrice(Purchase purchase){
        SkuDetails skuDetails = querySkuDetail(purchase);
        if(null != skuDetails){
            return skuDetails.getPriceAmountMicros();
        }
        return -1L;
    }

    private static String getPurchaseCurrency(Purchase purchase){
        SkuDetails skuDetails = querySkuDetail(purchase);
        if(null != skuDetails){
            return skuDetails.getPriceCurrencyCode();
        }
        return "";
    }

    private static int getPurchaseType(Purchase purchase) {
       SkuDetails skuDetails = querySkuDetail(purchase);
       if(null != skuDetails){
           return skuDetails.getType().equalsIgnoreCase(BillingClient.SkuType.INAPP)?0:1;
       }
       return -1;
    }

    private static String getAppName(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
           logE("getAppName failed ==" + e.getMessage());
        }

        return "";
    }

}
