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
import com.fatih.kingsofpigs.ui.widget.bannerHud


class UiView(
    val uiSkin : Skin
) : KTable, Table(){

    private lateinit var bannerHud : BannerHud
    var changeScreen : Boolean = false

    init {
        initialize()
    }

    fun initialize(){
        setFillParent(true)
        center()
        background = uiSkin[Drawables.BIG_BACKGROUND]
        bannerHud =  bannerHud(uiView = this){
            it.padRight(250f).padBottom(215f)
        }
        bannerHud.addAction(
            Actions.sequence(
                Actions.moveBy(0f,200f,0.05f, Interpolation.pow3OutInverse),
                fadeIn(1f, Interpolation.smooth2),
                Actions.moveBy(0f,-200f ,1f, Interpolation.pow3OutInverse),
            )
        )
    }
}


fun <S>KWidget<S>.uiView(
    skin : Skin = Scene2DSkin.defaultSkin,
    init : UiView.(S) -> Unit = {}
) : UiView = actor(UiView(skin),init)
