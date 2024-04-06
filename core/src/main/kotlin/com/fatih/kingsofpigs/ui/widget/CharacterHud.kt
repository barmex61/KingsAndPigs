package com.fatih.kingsofpigs.ui.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.fatih.kingsofpigs.ui.Drawables
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import com.fatih.kingsofpigs.ui.get
import ktx.actors.plusAssign
import ktx.scene2d.image

class CharacterHud (
    val skin: Skin
): KGroup,WidgetGroup() {

    private val background = Image(skin[Drawables.STATUS_BAR])
    private val hpBar : Image
    var charImage : Image

    init {
        this += background
        hpBar = image(skin[Drawables.HP_BAR]){
            setPosition(28f,23f)
        }
        image(skin[Drawables.MP_BAR]){
            setPosition(28f,17f)
        }
        charImage = image(skin[Drawables.KING]){
            setPosition(5f,6f)
            setSize(20f,20f)
        }
    }

    fun setCharacterImageDrawable(drawables: Drawables){
        charImage.drawable = skin[drawables]

    }


    fun setHpPercentage(percentage: Float,isEnemy : Boolean = false){
        if (isEnemy){
            this.clearActions()
            this += Actions.sequence(
                fadeIn(1f, Interpolation.pow3OutInverse),
                delay(1f,fadeOut(1f, Interpolation.pow3OutInverse))
            )
        }
        hpBar.clearActions()
        hpBar += Actions.scaleTo(percentage,1f,1f, Interpolation.pow3OutInverse)
    }

    override fun getPrefWidth() = background.drawable.minWidth

    override fun getPrefHeight() = background.drawable.minHeight
}
fun <S>KWidget<S>.characterHud(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : CharacterHud.(S) -> Unit = {}
) : CharacterHud = actor(CharacterHud(skin),init)
