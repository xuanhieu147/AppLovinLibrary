package com.tokis.applovinutils

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.tokis.tokiapp.*

class MainActivity : AppCompatActivity() {
    lateinit var bannerContainer: ViewGroup
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnLoad = findViewById<Button>(R.id.btn_load_inter)
        val btnCallback2 = findViewById<Button>(R.id.btn_show_inter_callback2)
        val btnLoadAndShow = findViewById<Button>(R.id.btn_load_show_inter_callback2)
        val nativeAds = findViewById<LinearLayout>(R.id.nativead)


        val btnReward = findViewById<Button>(R.id.btn_show_reward)
        bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)

        btnLoad.setOnClickListener {
            ApplovinUtil.loadInterstitials(this,"46f6535dcdaa4adf",12000, object :InterstititialCallback {
                override fun onInterstitialReady() {
                    Toast.makeText(this@MainActivity,"load success",Toast.LENGTH_SHORT).show()
                }

                override fun onInterstitialClosed() {

                }

                override fun onInterstitialLoadFail(error: String) {
                    Toast.makeText(this@MainActivity,"load fail ${error.toString()}",Toast.LENGTH_SHORT).show()

                }

                override fun onInterstitialShowSucceed() {

                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                    TODO("Not yet implemented")
                }
            })

        }
        btnCallback2.setOnClickListener {
            ApplovinUtil.showInterstitialsWithDialogCheckTime(
                this,
                1500,
                object : InterstititialCallback {
                    override fun onInterstitialShowSucceed() {

                    }

                    override fun onAdRevenuePaid(ad: com.applovin.mediation.MaxAd?) {
                    }

                    override fun onInterstitialReady() {

                    }

                    override fun onInterstitialClosed() {
                        startActivity(Intent(this@MainActivity, MainActivity3::class.java))
                    }

                    override fun onInterstitialLoadFail(error: String) {
                        onInterstitialClosed()
                    }
                })
        }

        btnLoadAndShow.setOnClickListener(){

        }

        btnReward.setOnClickListener {
            ApplovinUtil.loadReward(this,"c10d259dcb47378d",15000, object :RewardCallback{
                override fun onRewardReady() {
                    ApplovinUtil.showRewardWithDialogCheckTime(this@MainActivity,1500, object :RewardCallback{
                        override fun onRewardReady() {

                        }

                        override fun onRewardClosed() {
                            Toast.makeText(this@MainActivity,"onRewardClosed",Toast.LENGTH_SHORT).show()
                        }

                        override fun onRewardLoadFail(error: String) {
                        }

                        override fun onRewardShowSucceed() {
                        }

                        override fun onUserRewarded() {
                            Toast.makeText(this@MainActivity,"onUserRewarded",Toast.LENGTH_SHORT).show()
                        }

                        override fun onRewardedVideoStarted() {
                        }

                        override fun onRewardedVideoCompleted() {
                            Toast.makeText(this@MainActivity,"onRewardedVideoCompleted",Toast.LENGTH_SHORT).show()
                        }

                        override fun onAdRevenuePaid(ad: MaxAd?) {
                        }
                    })
                }

                override fun onRewardClosed() {
                }

                override fun onRewardLoadFail(error: String) {
                }

                override fun onRewardShowSucceed() {
                }

                override fun onUserRewarded() {
                }

                override fun onRewardedVideoStarted() {
                }

                override fun onRewardedVideoCompleted() {
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }
            })
        }
        btnLoadAndShow.setOnClickListener {
            ApplovinUtil.loadAndShowInterstitialsWithDialogCheckTime(this,"46f6535dcdaa4adf",0,object : InterstititialCallback {
                override fun onInterstitialReady() {

                }

                override fun onInterstitialClosed() {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))


                }

                override fun onInterstitialLoadFail(error: String) {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))


                }

                override fun onInterstitialShowSucceed() {

                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                    TODO("Not yet implemented")
                }
            })
        }

        ApplovinUtil.loadAndShowNativeAds(this@MainActivity, "8aec97f172bce4a6", nativeAds,GoogleENative.UNIFIED_MEDIUM, object : NativeAdCallback {
                override fun onNativeAdLoaded() {
                    Toast.makeText(this@MainActivity,"onNativeAdLoaded",Toast.LENGTH_SHORT).show()
                }

            override fun onLoadedAndGetNativeAd(ad: MaxAd?, adView: MaxNativeAdView?) {

            }

            override  fun onAdFail() {
                    Toast.makeText(this@MainActivity,"onAdFail",Toast.LENGTH_SHORT).show()
                }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })

//        AdmodUtils.getInstance().loadNativeAds(this@MainActivity,
//            getString(R.string.test_ads_admob_native_id), nativeAds,
//            GoogleENative.UNIFIED_MEDIUM, object : NativeAdCallback {
//           override fun onNativeAdLoaded() {}
//            override  fun onAdFail() {}
//        })


    }

    //    override fun onPause() {
//        if(this::bannerContainer.isInitialized){
//            IronSourceUtil.destroyBanner(bannerContainer)
//        }
//        super.onPause()
//    }
    override fun onResume() {
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
//        ApplovinUtil.showBanner(this, bannerContainer, "5c902521afdeef72", object : BannerCallback{
//            override fun onBannerLoadFail(error: String) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onBannerShowSucceed() {
//                TODO("Not yet implemented")
//            }
//
//            override fun onAdRevenuePaid(ad: MaxAd?) {
//                TODO("Not yet implemented")
//            }
//        })
        super.onResume()
    }
}