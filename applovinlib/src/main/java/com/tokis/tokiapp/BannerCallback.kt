package com.tokis.tokiapp

import com.applovin.mediation.MaxAd

interface BannerCallback {
    fun onBannerLoadFail(error:String)
    fun onBannerShowSucceed()
    fun onAdRevenuePaid(ad: MaxAd?)

}