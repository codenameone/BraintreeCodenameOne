package com.codename1.braintree.impl;

import com.codename1.system.NativeInterface;

/**
 * @deprecated don't use the native interface directly
 */
public interface BraintreeNative extends NativeInterface {
    public void showChargeUI(String clientToken);
}
