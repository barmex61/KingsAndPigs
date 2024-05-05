package com.fatih.kingsofpigs.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.event.ShowRewardedAdViewEvent
import com.fatih.kingsofpigs.ui.Drawables
import com.fatih.kingsofpigs.ui.Labels
import com.fatih.kingsofpigs.ui.TextFields
import ktx.actors.onClick
import ktx.scene2d.KGroup
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textField
import com.fatih.kingsofpigs.ui.get
import com.fatih.kingsofpigs.ui.widget.RewardedAdHud
import com.fatih.kingsofpigs.ui.widget.bannerHud
import com.fatih.kingsofpigs.ui.widget.rewardedAdHud
import com.fatih.kingsofpigs.utils.AdVisibilityListener

class RewardedAdView(
    adVisibilityListener: AdVisibilityListener,
    gameStage: Stage
) : KTable, Table() ,EventListener{

    private var rewardedAdHud : RewardedAdHud

    init {
        setFillParent(true)
        rewardedAdHud =  rewardedAdHud(adVisibilityListener = adVisibilityListener, gameStage = gameStage, setVisibility = {
            this.isVisible = !this.isVisible
        })
        this.isVisible = false
    }

    override fun handle(event: Event?): Boolean {
        return when(event){
            is ShowRewardedAdViewEvent -> {
                this.isVisible = event.showView
                true
            }
            else-> false
        }
    }
}

@Scene2dDsl
fun <S>KWidget<S>.rewardedAdView(
    adVisibilityListener: AdVisibilityListener,
    gameStage: Stage,
    init : RewardedAdView.(S) -> Unit = {}
) : RewardedAdView = actor(RewardedAdView(adVisibilityListener,gameStage),init)
