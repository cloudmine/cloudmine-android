package com.cloudmine.api.rest;

import android.util.Base64;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class AndroidBase64Encoder implements Base64Encoder{

    @Override
    public String encode(String toEncode) {
        return Base64.encodeToString(toEncode.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
    }
}
