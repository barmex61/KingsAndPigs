package com.fatih.kingsofpigs.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.fatih.kingsofpigs.KingOfPigs
import com.fatih.kingsofpigs.utils.AdVisibilityListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() , AdVisibilityListener,OnUserEarnedRewardListener {

    private lateinit var adView : AdView
    private val SHOW_AD = 1
    private val HIDE_AD = 0
    private var rewardedInterstitialAd : RewardedInterstitialAd? = null
    private val adHandler = Handler(Looper.getMainLooper()){
        when(it.what){
            SHOW_AD -> {
                println("showad")
                rewardedInterstitialAd!!.show(this,this)
                val adRequest =AdRequest.Builder().build();
                adView.loadAd(adRequest)
                adView.visibility = View.VISIBLE
                true
            }
            HIDE_AD -> {
                val adRequest =AdRequest.Builder().build();
                adView.loadAd(adRequest)
                adView.visibility = View.INVISIBLE
                true
            }
            else -> false
        }
    }

    override fun onUserEarnedReward(p0: RewardItem) {
        println("reward USERRRR!!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ca-app-pub-3940256099942544/9214589741
        MobileAds.initialize(this) {
            loadAd()
        }

        val layout =RelativeLayout(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        val gameView = initializeForView(KingOfPigs(this), AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
        })
        adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"

        val adRequest =AdRequest.Builder().build();
        adView.loadAd(adRequest)
        adView.visibility = View.VISIBLE
        layout.addView(gameView);
        val adParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
        			RelativeLayout.LayoutParams.WRAP_CONTENT)
        adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        adParams.addRule(RelativeLayout.ALIGN_PARENT_END)
        adParams.addRule(RelativeLayout.ALIGN_PARENT_START)

        layout.addView(adView, adParams)

        setContentView(layout)

    }

    private fun loadAd() {
        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379",
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    println("ad was loaded interstitial")
                    rewardedInterstitialAd = ad
                    rewardedInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){
                        override fun onAdClicked() {
                            println("clickecd")
                            super.onAdClicked()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            println("dismiss")
                            super.onAdDismissedFullScreenContent()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            println("onfailed")
                            super.onAdFailedToShowFullScreenContent(p0)
                        }

                        override fun onAdShowedFullScreenContent() {
                            println("shown")
                            super.onAdShowedFullScreenContent()
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    println(adError.toString())
                    rewardedInterstitialAd = null
                }
            })
    }

    override fun setVisibility(isVisible: Boolean) {
       if (isVisible) adHandler.sendEmptyMessage(SHOW_AD) else adHandler.sendEmptyMessage(HIDE_AD)
    }
}
