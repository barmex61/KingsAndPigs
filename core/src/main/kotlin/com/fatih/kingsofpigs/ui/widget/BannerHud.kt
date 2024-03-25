package com.fatih.kingsofpigs.ui.widget

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.ui.Drawables
import com.fatih.kingsofpigs.ui.TextFields
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import ktx.scene2d.image
import com.fatih.kingsofpigs.ui.get
import ktx.scene2d.textField

class BannerHud(
    skin: Skin
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
        }
        image(skin[Drawables.ICON_CROWN]){
            setSize(35f,30f)
            setPosition(110f,10f)
        }
    }
}

fun <S>KWidget<S>.bannerHud(
    skin : Skin = Scene2DSkin.defaultSkin,
    init : BannerHud.(S) -> Unit = {}
) : BannerHud = actor(BannerHud(skin),init)
