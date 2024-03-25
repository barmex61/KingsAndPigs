package com.fatih.kingsofpigs.ui.view

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.kingsofpigs.ui.Drawables
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import com.fatih.kingsofpigs.ui.get
import com.fatih.kingsofpigs.ui.widget.BannerHud
import com.fatih.kingsofpigs.ui.widget.bannerHud
import ktx.actors.alpha

class UiView(
    skin : Skin
) : KTable, Table(){

    private val bannerHud : BannerHud

    init {
        setFillParent(true)
        center()
        background = skin[Drawables.BIG_BACKGROUND]
        bannerHud =  bannerHud{
            it.padRight(250f).padBottom(200f)
        }

    }
}


fun <S>KWidget<S>.uiView(
    skin : Skin = Scene2DSkin.defaultSkin,
    init : UiView.(S) -> Unit = {}
) : UiView = actor(UiView(skin),init)
