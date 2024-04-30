package com.fatih.kingsofpigs.ui.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.ui.Labels
import com.fatih.kingsofpigs.ui.TextFields
import ktx.actors.plusAssign
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.textField

class PauseView(
    skin : Skin
) : KTable, Table(){

    var textField : TextField

    init {
        setFillParent(true)
        if (!skin.has("Pixmap", TextureRegionDrawable::class.java)){
            skin.add("Pixmap",TextureRegionDrawable(Texture(Pixmap(1,1,Pixmap.Format.RGBA8888).apply {
                this.drawPixel(0,0, Color.rgba8888(0.1f,0.1f,0.1f,0.7f))
            })))
        }
        background = skin.get("Pixmap",TextureRegionDrawable::class.java)
        textField = textField("Pause",TextFields.TITLE.name){
            it.expandX()
            this.style.fontColor = Color.RED
            this.alignment = Align.center
        }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.pauseView(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : PauseView.(S) -> Unit = {}
):PauseView = actor(PauseView(skin),init)
