package com.tokis.tokiapp

import com.applovin.mediation.MaxAd

interface InterstititialCallback {
    fun onInterstitialReady()
    fun onInterstitialClosed()
    fun onInterstitialLoadFail(error:String)
    fun onInterstitialShowSucceed()
    fun onAdRevenuePaid(ad: MaxAd?)

}