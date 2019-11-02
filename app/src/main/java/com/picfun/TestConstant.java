package com.picfun;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * @author Secret
 * @since 2019/10/9
 */
public class TestConstant {

//    @StringValue(value = "subscription")
    public static final String PURCHASE_SUBSCRIPTION = "subscription";

//    @StringValue(value = "subscription")
    public static final int PURCHASE_INAPP = 2;

    public static void showSnackBar(View view){
        Snackbar.make(view,"ss",5000).show();
    }

}
