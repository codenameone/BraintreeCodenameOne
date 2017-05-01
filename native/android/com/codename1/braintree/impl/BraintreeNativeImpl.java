package com.codename1.braintree.impl;

import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.DropInActivity;
import com.codename1.impl.android.AndroidNativeUtil;
import com.codename1.impl.android.IntentResultListener;
import android.content.Intent;
import android.app.Activity;
import com.codename1.io.Log;

public class BraintreeNativeImpl {
    public void showChargeUI(final String param) {
        final Activity act = AndroidNativeUtil.getActivity();
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {        
                DropInRequest dropInRequest = new DropInRequest()
                    .clientToken(param);
                AndroidNativeUtil.startActivityForResult(dropInRequest.getIntent(act), new IntentResultListener() {
                    public void onActivityResult (int requestCode, int resultCode, Intent data) {
                        if (resultCode == Activity.RESULT_OK) {
                          DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                          // use the result to update your UI and send the payment method nonce to your server
                          BraintreePaymentCallback.onPurchaseSuccess(result.getPaymentMethodNonce().getNonce());
                        } else if (resultCode == Activity.RESULT_CANCELED) {
                          // the user canceled
                          BraintreePaymentCallback.onPurchaseCancel();
                        } else {
                          // handle errors here, an exception may be available in
                          Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                          BraintreePaymentCallback.onPurchaseFail(error.toString());
                          Log.e(error);
                          Log.sendLog();
                        }
                    }
                });
            }
        });
    }

    public boolean isSupported() {
        return true;
    }

}
