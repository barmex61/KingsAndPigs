package com.fatih.kingsofpigs.ui.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fatih.kingsofpigs.ui.Drawables
import com.fatih.kingsofpigs.ui.TextFields
import com.fatih.kingsofpigs.ui.get
import com.fatih.kingsofpigs.ui.view.UiView
import ktx.actors.alpha
import ktx.actors.minusAssign
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import ktx.scene2d.image
import ktx.scene2d.textField

class HtpHud(
    skin: Skin,
    uiView : UiView
) : KGroup,WidgetGroup(){

    init {
        image(skin[Drawables.BIG_BACKGROUND]).apply {
            setSize(350f,275f)
        }
        image(skin[Drawables.BUTTON_LEFT]).apply {
            setSize(35f,35f)
            setPosition(25f,220f)
            onClick {
                this@HtpHud.apply {
                    this.clearActions()
                    this += fadeOut(1f, Interpolation.pow3OutInverse)
                    this += Actions.sequence(
                        Actions.moveBy(0f,-400f,1f, Interpolation.pow3OutInverse),
                        Actions.run {
                            uiView -= uiView.htpHud
                            uiView.apply {
                                bannerHud = bannerHud(skin,uiView){
                                    this.alpha = 0f
                                    this += Actions.fadeIn(1f, Interpolation.pow3OutInverse)
                                    this += Actions.moveBy(-250f,110f,1f,Interpolation.pow3OutInverse)
                                }
                            }
                        }
                    )
                }
            }
        }
        textField("HOW TO PLAY",TextFields.TITLE.name){
            it.setPosition(100f,230f)
            setSize(180f,30f)
        }
        image(skin[Drawables.BUTTON_R]).apply {
            setSize(35f,35f)
            setPosition(40f,160f)
        }
        textField("Restart",TextFields.TITLE.name){
            it.setPosition(90f,165f)
            setSize(80f,30f)
        }
        image(skin[Drawables.BUTTON_P]).apply {
            setSize(35f,35f)
            setPosition(40f,110f)
        }
        textField("Pause",TextFields.TITLE.name){
            it.setPosition(90f,115f)
            setSize(80f,30f)
        }
        image(skin[Drawables.BUTTON_SPACE]).apply {
            setSize(100f,30f)
            setPosition(30f,30f)
        }
        textField("Attack",TextFields.TITLE.name){
            it.setPosition(45f,70f)
            setSize(80f,30f)
        }
        image(skin[Drawables.BUTTON_LEFT]).apply {
            setSize(35f,35f)
            setPosition(210f,80f)
        }
        image(skin[Drawables.BUTTON_RIGHT]).apply {
            setSize(35f,35f)
            setPosition(290f,80f)
        }
        image(skin[Drawables.BUTTON_UP]).apply {
            setSize(35f,35f)
            setPosition(250f,120f)
        }
        image(skin[Drawables.BUTTON_DOWN]).apply {
            setSize(35f,35f)
            setPosition(250f,80f)
        }
        textField("Movement",TextFields.TITLE.name){
            it.setPosition(220f,160f)
            setSize(120f,30f)
        }
    }
}

fun <S> KWidget<S>.htpHud(
    skin: Skin = Scene2DSkin.defaultSkin,
    uiView: UiView,
    init : HtpHud.(S) -> Unit = {}
) : HtpHud = actor(HtpHud(skin,uiView),init)
