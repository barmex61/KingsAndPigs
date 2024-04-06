package com.fatih.kingsofpigs.ui.widget

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup
import com.fatih.kingsofpigs.ui.Drawables
import ktx.scene2d.KGroup
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import ktx.scene2d.image
import com.fatih.kingsofpigs.ui.get
import ktx.actors.alpha
import ktx.actors.plusAssign

class PlayerLifeHud(
    val skin: Skin
) : KGroup,WidgetGroup(){

    private var extraLife : Int = 3
    private var hearts = mutableListOf<Image>()

    init {
        image(skin[Drawables.LIFE_BAR]){
            setSize(50f,25f)
        }
        (0..<extraLife).forEach { i->
            hearts.add(image(skin[Drawables.HEARTH]){
                setPosition(13f + i*8.5f,9.2f)
                setSize(7f,5.5f)
            })
        }
    }


    fun setExtraLife(extraLife : Int){
        if (extraLife < this.extraLife){
            val lastImage = hearts.lastOrNull()
            lastImage?.addAction(
                Actions.sequence(
                    fadeOut(1f, Interpolation.pow3Out),
                    Actions.run {
                        hearts.remove(lastImage)
                    }
                )
            )
        }else{
            val lastPositionX = hearts.last().x
            val image = image(skin[Drawables.HEARTH]){
                alpha = 0f
                setPosition(lastPositionX + 8.5f,9.2f)
                setSize(7f,5.5f)
                this += fadeIn(1f, Interpolation.pow3OutInverse)
            }
            hearts.add(image)

        }
    }
}
fun <S>KWidget<S>.playerLifeHud(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : PlayerLifeHud.(S) -> Unit
) : PlayerLifeHud = actor(PlayerLifeHud(skin),init)
