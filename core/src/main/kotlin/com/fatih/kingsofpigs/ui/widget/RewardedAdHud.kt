package com.fatih.kingsofpigs.ui.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.event.ContinueGameEvent
import com.fatih.kingsofpigs.event.DoNotWantToContinueEvent
import com.fatih.kingsofpigs.event.GameOverEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.fatih.kingsofpigs.ui.Drawables
import com.fatih.kingsofpigs.ui.Fonts
import com.fatih.kingsofpigs.ui.Labels
import com.fatih.kingsofpigs.ui.TextFields
import com.fatih.kingsofpigs.ui.get
import com.fatih.kingsofpigs.utils.AdVisibilityListener
import ktx.actors.alpha
import ktx.actors.minusAssign
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.style.skin

class RewardedAdHud(
    skin: Skin = Scene2DSkin.defaultSkin,
    adVisibilityListener: AdVisibilityListener,
    gameStage : Stage,
    setVisibility : () -> Unit
) : KGroup, WidgetGroup() {
    init {

        label("      You are dead !" +
            "                 Would you like to continue to the game ? ", Labels.AD.name){
            setPosition(-60f,-15f)
            setSize(120f,60f)
            this.setAlignment(Align.center)
            this.wrap = true
        }
        label("Yes", Labels.AD.name){
            setAlignment(Align.center)
            setSize(40f,20f)
            setPosition(-45f,-40f)
            onClick {
                println("show inue")
                adVisibilityListener.showRewardedAd(true)
                gameStage.fireEvent(ContinueGameEvent())
                setVisibility()
            }
        }
        label("No", Labels.AD.name){
            setAlignment(Align.center)
            setSize(40f,20f)
            setPosition(5f,-40f)
            onClick {
                println("do not continue")
                gameStage.fireEvent(DoNotWantToContinueEvent())
                setVisibility()
            }

        }
    }
}
@Scene2dDsl
fun <S>KWidget<S>.rewardedAdHud(
    adVisibilityListener: AdVisibilityListener,
    gameStage: Stage,
    setVisibility: () -> Unit = {},
    init : RewardedAdHud.(S) -> Unit = {}
):RewardedAdHud = actor(RewardedAdHud(adVisibilityListener = adVisibilityListener, gameStage = gameStage, setVisibility = setVisibility),init)
