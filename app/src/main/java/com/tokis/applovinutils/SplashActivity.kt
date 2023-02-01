package com.tokis.applovinutils

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.tokis.tokiapp.*
import com.tokis.applovinutils.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AdmodUtils.getInstance().initAdmob(this, 10000, true, true)
        AppOpenManager.getInstance().init(application, getString(R.string.test_ads_admob_app_open))
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ApplovinUtil.initApplovin(this,  true)
        loadInter();

//        ApplovinUtil.loadAndGetNativeAds(this@SplashActivity, "8aec97f172bce4a6",object : NativeAdCallback {
//            override fun onNativeAdLoaded() {
//
//            }
//
//            override fun onLoadedAndGetNativeAd(ad: MaxAd?, adView: MaxNativeAdView?) {
//            }
//
//            override fun onAdRevenuePaid(ad: MaxAd?) {
//
//            }
//
//            override fun onAdFail() {
//                loadInter();
//            }
//
//        })





        binding.btnNext.setOnClickListener {
          //  requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
//        IronSourceUtil.loadInterstitials(this,15000,object : InterstititialCallback {
//            override fun onInterstitialReady() {
//                binding.btnNext.visibility = View.VISIBLE
//                binding.progressBar.visibility = View.INVISIBLE
//            }
//
//
//
//            override fun onInterstitialClosed() {
//                val i = Intent(this@SplashActivity, MainActivity::class.java)
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(i)
//            }
//
//            override fun onInterstitialLoadFail() {
//                onInterstitialClosed()
//            }
//
//            override fun onInterstitialShowSucceed() {
//
//            }
//        })

    }

    fun loadInter(){
        ApplovinUtil.loadInterstitials(this, "46f6535dcdaa4adf", 0, object : InterstititialCallback {
            override fun onInterstitialReady() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))

            }

            override fun onInterstitialClosed() {


                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onInterstitialLoadFail(error: String) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onInterstitialShowSucceed() {

            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }
}