package com.fatih.kingsofpigs.ui.view


import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.fatih.kingsofpigs.ui.Drawables
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import com.fatih.kingsofpigs.ui.get
import com.fatih.kingsofpigs.ui.widget.BannerHud
import com.fatih.kingsofpigs.ui.widget.HtpHud
import com.fatih.kingsofpigs.ui.widget.bannerHud
import ktx.actors.alpha
import ktx.actors.plusAssign


class UiView(
    val uiSkin : Skin
) : KTable, Table(){

    lateinit var bannerHud : BannerHud
    lateinit var htpHud: HtpHud
    var changeScreen : Boolean = false

    init {
        initialize()
    }

    fun initialize(){
        setFillParent(true)
        center()
        background = uiSkin[Drawables.RECTANGLE_GRADIENT_BACKGROUND]
        bannerHud =  bannerHud(uiView = this){
            it.padRight(250f).padBottom(215f)
            this.alpha = 0f
            this += fadeIn(1f, Interpolation.smooth2)
        }
    }
}


fun <S>KWidget<S>.uiView(
    skin : Skin = Scene2DSkin.defaultSkin,
    init : UiView.(S) -> Unit = {}
) : UiView = actor(UiView(skin),init)
