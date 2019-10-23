package com.sim.android.appdetection;

public enum TrustedStores {

    STORE_GOOGLE("com.android.vending", "Google"),
    STORE_SAMSUNG("com.sec.android.app.samsungapps", "Samsung"),
    STORE_SAMSUNG_LOCAL("com.samsung.android.app.omcagent", "Samsung"),
    STORE_FACEBOOK("com.facebook.system","Facebook")
    ;

    private String key;
    private String value;

    TrustedStores(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
