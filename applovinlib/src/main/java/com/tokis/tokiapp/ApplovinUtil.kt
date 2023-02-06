package com.tokis.tokiapp

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkUtils
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object ApplovinUtil : LifecycleObserver {
    var enableAds = true
    var isInterstitialAdShowing = false
    private var banner: MaxAdView? = null
    var lastTimeInterstitialShowed: Long = 0L
    var lastTimeCallInterstitial: Long = 0L
    var isLoadInterstitialFailed = false
    public lateinit var interstitialAd: MaxInterstitialAd
    public lateinit var rewardAd: MaxRewardedAd

    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private var nativeAd: MaxAd? = null

    fun initApplovin(activity: Activity, enableAds: Boolean) {
        this.enableAds = enableAds
        AppLovinSdk.getInstance(activity).setMediationProvider("max")
        AppLovinSdk.getInstance(activity).initializeSdk { }

    }

    val TAG: String = "IronSourceUtil"


    //Only use for splash interstitial
    fun loadInterstitials(activity: AppCompatActivity, idAd: String, timeout: Long, callback: InterstititialCallback) {
        interstitialAd = MaxInterstitialAd(idAd, activity)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                callback.onInterstitialReady()
                isLoadInterstitialFailed = false
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                callback.onInterstitialLoadFail(error.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                callback.onInterstitialLoadFail(error.toString())
            }

        })

        // Load the first ad
        interstitialAd.loadAd()

        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if ((!interstitialAd.isReady()) && (!isInterstitialAdShowing)) {
                callback.onInterstitialLoadFail("!IronSource.isInterstitialReady()")
            }
        }

    }


    @MainThread
    fun showInterstitialsWithDialogCheckTime(
        activity: AppCompatActivity,
        dialogShowTime: Long,
        callback: InterstititialCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        if (interstitialAd == null) {
            callback.onInterstitialLoadFail("null")
            return
        }

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }

        if (System.currentTimeMillis() - 1000 < lastTimeCallInterstitial) {
            return
        }
        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onInterstitialLoadFail("\"isNetworkConnected\"")
            return
        }

        interstitialAd.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false
                    callback.onInterstitialReady()
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {
                TODO("Not yet implemented")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                isLoadInterstitialFailed = true
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialLoadFail(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
            }
        })


        if (interstitialAd.isReady()) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    val dialog = DialogLoading()
                    dialog.setCancelable(false)
                    dialog.show(activity.supportFragmentManager, "TAG")
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show(activity.supportFragmentManager, "TAG")
                    }
                    delay(dialogShowTime)
                    dialog.dialog?.let {
                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                            it.dismiss()
                        }
                    }
                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    interstitialAd.showAd()
                }
            }
        } else {
            activity.lifecycleScope.launch(Dispatchers.Main) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
                isLoadInterstitialFailed = true
            }
        }
    }

    @MainThread
    fun loadAndShowInterstitialsWithDialogCheckTime(
        activity: AppCompatActivity,
        idAd: String,
        dialogShowTime: Long,
        callback: InterstititialCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        val dialog = DialogLoading()
        dialog.isCancelable = false
//        dialog.show(activity.supportFragmentManager, "TAG")

        interstitialAd = MaxInterstitialAd(idAd, activity)
        interstitialAd.loadAd()

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Log.e("isAppResumeEnabled", "2" + AppOpenManager.getInstance().isAppResumeEnabled)
                }
            }
        }

        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds || !isNetworkConnected(activity)) {
            Log.e("isNetworkConnected", "1" + AppOpenManager.getInstance().isAppResumeEnabled)
            dialog.dialog?.let {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                    it.dismiss()
                }
            }

            Log.e("isNetworkConnected", "2" + AppOpenManager.getInstance().isAppResumeEnabled)

            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
                Log.e("isNetworkConnected", "3" + AppOpenManager.getInstance().isAppResumeEnabled)

                Log.e("isAppResumeEnabled", "3" + AppOpenManager.getInstance().isAppResumeEnabled)
            }
            Log.e("isNetworkConnected", "4" + AppOpenManager.getInstance().isAppResumeEnabled)

            isInterstitialAdShowing = false
            Log.e("isNetworkConnected", "5" + AppOpenManager.getInstance().isAppResumeEnabled)

            callback.onInterstitialLoadFail("isNetworkConnected")
            return
        }

        interstitialAd.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })
        interstitialAd.setListener(object : MaxAdListener {


            override fun onAdLoaded(ad: MaxAd?) {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    if (interstitialAd.isReady()) {
                        dialog.dialog?.dismiss()
                        interstitialAd.showAd();
                    }
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                dialog.dialog?.let {
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                        it.dismiss()
                    }
                }

                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Log.e("isAppResumeEnabled", "6" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                callback.onInterstitialShowSucceed()

                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {

                dialog.dialog?.let {
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                        it.dismiss()
                    }
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e("isAppResumeEnabled", "5" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                isInterstitialAdShowing = false

                callback.onInterstitialClosed()
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {

                    dialog.dialog?.let {
                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                            it.dismiss()
                        }
                    }
                    isLoadInterstitialFailed = true
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                        Log.e("isAppResumeEnabled", "4" + AppOpenManager.getInstance().isAppResumeEnabled)

                    }
                    isInterstitialAdShowing = false
                    callback.onInterstitialLoadFail(error.toString())
                }
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e("isAppResumeEnabled", "7" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                isInterstitialAdShowing = false
                callback.onInterstitialClosed()
                dialog.dialog?.let {
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                        it.dismiss()
                    }
                }

            }
        })




        if (interstitialAd.isReady()) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    val dialog1 = DialogLoading()
                    dialog1.isCancelable = false
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog1))
                    if (!activity.isFinishing) {
                        dialog1.show(activity.supportFragmentManager, "TAG")
                    }
                    delay(dialogShowTime)
                    dialog1.dialog?.let {
                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                            dialog1.dismiss()
                        }
                    }
                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    if (interstitialAd.isReady()) {
                        interstitialAd.showAd();
                    }
                }
            }
        } else {
            if (dialogShowTime > 0) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show(activity.supportFragmentManager, "TAG")
                    }
                }
            }

        }
    }


    fun showBanner(
        activity: AppCompatActivity, bannerContainer: ViewGroup, idAd: String,
        callback: BannerCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onBannerLoadFail("")
            return
        }

        bannerContainer.removeAllViews()
        banner = MaxAdView(idAd, activity)

        val width = ViewGroup.LayoutParams.MATCH_PARENT

        // Get the adaptive banner height.
        val heightDp = MaxAdFormat.BANNER.getAdaptiveSize(activity).height
        val heightPx = AppLovinSdkUtils.dpToPx(activity, heightDp)

        banner?.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        banner?.setExtraParameter("adaptive_banner", "true")

        val tagView: View =
            activity.getLayoutInflater().inflate(R.layout.banner_shimmer_layout, null, false)
        bannerContainer.addView(tagView, 0)
        bannerContainer.addView(banner, 1)
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmerAnimation()

        banner?.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })

        banner?.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                shimmerFrameLayout.stopShimmerAnimation()
                bannerContainer.removeView(tagView)
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                callback.onBannerShowSucceed()
            }

            override fun onAdHidden(ad: MaxAd?) {
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                bannerContainer.removeAllViews()
                callback.onBannerLoadFail(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                callback.onBannerLoadFail(error.toString())

            }

            override fun onAdExpanded(ad: MaxAd?) {
            }

            override fun onAdCollapsed(ad: MaxAd?) {
            }

        })

        banner?.loadAd()

    }


    @MainThread
    fun showRewardWithDialogCheckTime(
        activity: AppCompatActivity,
        dialogShowTime: Long,
        callback: RewardCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onRewardClosed()
            return
        }

        if (rewardAd == null) {
            callback.onRewardLoadFail("null")
            return
        }

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }

        if (System.currentTimeMillis() - 1000 < lastTimeCallInterstitial) {
            return
        }
        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onRewardLoadFail("\"isNetworkConnected\"")
            return
        }

        rewardAd.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })
        rewardAd.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false
                    callback.onRewardReady()
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onRewardShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                isLoadInterstitialFailed = true
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardLoadFail(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
                callback.onUserRewarded()
            }

            override fun onRewardedVideoStarted(ad: MaxAd?) {
                callback.onRewardedVideoStarted()
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                callback.onRewardedVideoCompleted()
            }
        })


        if (rewardAd.isReady()) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    val dialog = DialogLoading()
                    dialog.setCancelable(false)
                    dialog.show(activity.supportFragmentManager, "TAG")
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.dialog?.show()
                    }
                    delay(dialogShowTime)
                    dialog.dialog?.let {
                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && it.isShowing()) {
                            it.dismiss()
                        }
                    }

                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    rewardAd.showAd()
                }
            }
        } else {
            activity.lifecycleScope.launch(Dispatchers.Main) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
                isInterstitialAdShowing = false
                isLoadInterstitialFailed = true
            }
        }
    }


    fun loadReward(activity: AppCompatActivity, idAd: String, timeout: Long, callback: RewardCallback) {

        rewardAd = MaxRewardedAd.getInstance(idAd, activity)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onRewardClosed()
            return
        }

        rewardAd.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                isLoadInterstitialFailed = false
                callback.onRewardReady()
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                callback.onRewardShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                callback.onRewardClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                callback.onRewardLoadFail(error.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                callback.onRewardLoadFail(error.toString())
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
                callback.onUserRewarded()
            }

            override fun onRewardedVideoStarted(ad: MaxAd?) {
                callback.onRewardedVideoStarted()
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                callback.onRewardedVideoCompleted()
            }

        })

        // Load the first ad
        rewardAd.loadAd()

        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if ((!rewardAd.isReady()) && (!isInterstitialAdShowing)) {
                callback.onRewardLoadFail("!IronSource.isInterstitialReady()")
            }
        }

    }


//    fun loadAndShowRewardsAds(placementId: String, callback: RewardVideoCallback) {
//        IronSource.setRewardedVideoListener(object : RewardedVideoListener {
//            override fun onRewardedVideoAdOpened() {
//
//            }
//
//            override fun onRewardedVideoAdClosed() {
//                callback.onRewardClosed()
//            }
//
//            override fun onRewardedVideoAvailabilityChanged(p0: Boolean) {
//
//            }
//
//            override fun onRewardedVideoAdStarted() {
//
//            }
//
//            override fun onRewardedVideoAdEnded() {
//
//            }
//
//            override fun onRewardedVideoAdRewarded(p0: Placement?) {
//                callback.onRewardEarned()
//            }
//
//            override fun onRewardedVideoAdShowFailed(p0: IronSourceError?) {
//                callback.onRewardFailed()
//            }
//
//            override fun onRewardedVideoAdClicked(p0: Placement?) {
//
//            }
//        })
//        if (IronSource.isRewardedVideoAvailable()) {
//            IronSource.showRewardedVideo(placementId)
//        } else {
//            callback.onRewardNotAvailable()
//        }
//    }

    fun loadAndGetNativeAds(activity: Activity, idAd: String, adCallback: NativeAdCallback) {

        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail()
        }

        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
        nativeAdLoader.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                adCallback.onAdRevenuePaid(ad)
            }
        })
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {

            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd?) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }
                nativeAd = ad
                adCallback.onLoadedAndGetNativeAd(nativeAd, nativeAdView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                adCallback.onAdFail()
                // We recommend retrying with exponentially higher delays up to a maximum delay
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                // Optional click callback
            }
        })
        nativeAdLoader.loadAd()
    }

    fun showNativeAds(activity: Activity, nativeAdView: MaxNativeAdView?, nativeAdContainer: ViewGroup, adCallback: NativeAdCallback) {

        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail()
            return
        }
        nativeAdContainer.removeAllViews()
        nativeAdContainer.addView(nativeAdView)
    }

    fun loadAndShowNativeAds(activity: Activity, idAd: String, nativeAdContainer: ViewGroup, size: GoogleENative, adCallback: NativeAdCallback) {

        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail()
            return
        }

        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
        val tagView: View
        if (size === GoogleENative.UNIFIED_MEDIUM) {
            tagView = activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            tagView = activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        nativeAdContainer.addView(tagView, 0)
        val shimmerFrameLayout: ShimmerFrameLayout = tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmerAnimation()

        nativeAdLoader.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                adCallback.onAdRevenuePaid(ad)
            }
        })
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {

            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }

                // Save ad for cleanup.
                nativeAd = ad

                // Add ad view to view.
                shimmerFrameLayout.stopShimmerAnimation()
                nativeAdContainer.removeAllViews()
                nativeAdContainer.addView(nativeAdView)
                adCallback.onNativeAdLoaded()

            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                shimmerFrameLayout.stopShimmerAnimation()
                nativeAdContainer.removeAllViews()
                adCallback.onAdFail()
                // We recommend retrying with exponentially higher delays up to a maximum delay
            }

            override fun onNativeAdClicked(ad: MaxAd) {
                // Optional click callback
            }
        })
        nativeAdLoader.loadAd()
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var vau = cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        Log.e("isNetworkConnected", "0" + vau)
        return vau
    }
}