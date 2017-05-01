package com.codename1.braintree.impl;

import com.codename1.braintree.Purchase;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.JSONParser;
import com.codename1.io.NetworkManager;
import com.codename1.ui.Component;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

/**
 * This class receives callback from native off of the EDT!
 * 
 * @deprecated this class is an implementation detail used internally
 */
public class BraintreePaymentCallback {
    public static Purchase.Callback cb;
    public static void onPurchaseSuccess(String nonce) {
        Display.getInstance().callSerially(() -> {
            cb.onPurchaseSuccess(nonce);
        });
    }

    public static void onPurchaseFail(String errorMessage) {
        Display.getInstance().callSerially(() -> {
            cb.onPurchaseFail(errorMessage);
        });
    }

    public static void onPurchaseCancel() {
        Display.getInstance().callSerially(() -> {
            cb.onPurchaseCancel();
        });
    }
}
