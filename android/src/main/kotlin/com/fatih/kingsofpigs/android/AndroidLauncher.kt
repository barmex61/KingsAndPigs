package com.fatih.kingsofpigs.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.fatih.kingsofpigs.KingOfPigs
import com.fatih.kingsofpigs.utils.AdVisibilityListener
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() , AdVisibilityListener,OnUserEarnedRewardListener {

    private val SHOW_INTERSTITIAL_AD = 1
    private val SHOW_REWARDED_AD = 2

    private var rewardedInterstitialAd : RewardedInterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val adHandler by lazy {
        Handler(Looper.getMainLooper()){
            when(it.what){

                SHOW_INTERSTITIAL_AD ->{
                    rewardedInterstitialAd?.show(this,this)
                    true
                }
                SHOW_REWARDED_AD -> {
                    rewardedAd?.show(this,this)
                    true
                }
                else -> false
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ca-app-pub-3940256099942544/9214589741
        MobileAds.initialize(this) {
            loadInterstitialAd()
            loadRewardedAd()
        }
        initialize(KingOfPigs().apply { adVisibilityListener = this@AndroidLauncher }, AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
        })
    }

    private fun loadInterstitialAd() {
        RewardedInterstitialAd.load(this, "ca-app-pub-7923951045985903/4447121592",
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    rewardedInterstitialAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                        override fun onAdDismissedFullScreenContent() {
                            loadInterstitialAd()
                            super.onAdDismissedFullScreenContent()
                        }

                    }
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedInterstitialAd = null
                }
            })
    }

    private fun loadRewardedAd(){
        RewardedAd.load(this,"ca-app-pub-7923951045985903/2679261274", AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                rewardedAd!!.fullScreenContentCallback = object : FullScreenContentCallback(){

                    override fun onAdDismissedFullScreenContent() {
                        loadRewardedAd()
                        super.onAdDismissedFullScreenContent()
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                    }
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                println("rewarded failed ${adError.message}")
                rewardedAd = null
            }
        })
    }


    override fun onUserEarnedReward(p0: RewardItem) {
        println("reward USERRRR!!")
    }

    override fun showInterstitialAd(showAd: Boolean) {
        if (showAd) adHandler.sendEmptyMessage(SHOW_INTERSTITIAL_AD)
    }

    override fun showRewardedAd(showAd: Boolean) {
        if (showAd) adHandler.sendEmptyMessage(SHOW_REWARDED_AD)
    }
}
