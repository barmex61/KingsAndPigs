package com.fatih.kingsofpigs.ui.widget


import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.ui.Drawables
import com.fatih.kingsofpigs.ui.Labels
import com.fatih.kingsofpigs.ui.TextFields
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import ktx.scene2d.image
import com.fatih.kingsofpigs.ui.get
import com.fatih.kingsofpigs.ui.view.UiView
import ktx.actors.alpha
import ktx.actors.minusAssign
import ktx.actors.onClick
import ktx.actors.plusAssign
import ktx.scene2d.label
import ktx.scene2d.textField

class BannerHud(
    skin: Skin,
    uiView: UiView
) : KGroup,WidgetGroup() {

    init {
        image(skin[Drawables.RED_BACKGROUND]).apply {
            setSize(250f,50f)
        }
        image(skin[Drawables.TITLE_BACKGROUND]).apply {
            setSize(240f,90f)
            setPosition(5f,-10f)
        }
        textField("Kings And Pigs",TextFields.TITLE.name){
            setPosition(51f,38f)
            alignment = Align.center
            style.fontColor =  Color(63f/255f,63f/255f,63/255f,1f)
        }
        image(skin[Drawables.ICON_CROWN]){
            setSize(35f,30f)
            setPosition(110f,10f)
        }
        image(skin[Drawables.BIG_BACKGROUND]){
            setPosition(0f,-215f)
            setSize(250f,200f)
        }
        label("PLAY",Labels.COLUMN.name){
            setPosition(35f,-100f)
            setAlignment(Align.center)
            setSize(190f,40f)
            onClick {
                this@BannerHud.apply {
                    this.clearActions()
                    this += fadeOut(1f, Interpolation.pow3OutInverse)
                    uiView.addAction(Actions.color(Color.CORAL,3F))
                    this += Actions.sequence(
                        Actions.moveBy(0f,-400f,1f, Interpolation.pow3OutInverse),
                        Actions.run {
                            uiView.changeScreen = true
                        }
                    )
                }
            }
        }
        label("HOW TO PLAY",Labels.COLUMN.name){
            setPosition(35f,-158f)
            setAlignment(Align.center)
            setSize(190f,40f)
            onClick {
                this@BannerHud.apply {
                    this.clearActions()
                    this += fadeOut(1f, Interpolation.pow3OutInverse)
                    this += Actions.sequence(
                        Actions.moveBy(0f,-400f,1f, Interpolation.pow3OutInverse),
                        Actions.run {
                            uiView -= uiView.bannerHud
                            uiView.apply {
                                htpHud = htpHud(skin,uiView){
                                    this.alpha = 0f
                                    this += fadeIn(1f, Interpolation.pow3OutInverse)
                                    this += Actions.moveBy(-300f,-110f,1f,Interpolation.pow3OutInverse)
                                }
                            }
                        }
                    )
                }
            }
        }
        image(skin[Drawables.ICON_LIGHTNING]){
            setSize(25f,30f)
            setPosition(25f,-96f)
        }

        image(skin[Drawables.ICON_MENU]){
            setSize(23f,25f)
            setPosition(25f,-150f)
        }
    }
}


fun <S>KWidget<S>.bannerHud(
    skin : Skin = Scene2DSkin.defaultSkin,
    uiView: UiView,
    init : BannerHud.(S) -> Unit = {}
) : BannerHud = actor(BannerHud(skin,uiView),init)
