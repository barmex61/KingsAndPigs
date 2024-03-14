package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Qualifier
import ktx.actors.plusAssign
import ktx.math.random
import ktx.math.vec2

class FloatingTextComponent(
    var text : String = "",
    var isCrit : Boolean = false,
    val startPosition : Vector2 = vec2(),
    val targetPosition : Vector2 = vec2(),
    var remove : Boolean = false
) {

    lateinit var label : Label

    companion object{
        class FloatingTextComponentListener(
            @Qualifier("uiStage") private val uiStage : Stage
        ) : ComponentListener<FloatingTextComponent>{
            override fun onComponentAdded(entity: Entity, component: FloatingTextComponent) {
                component.targetPosition.set(
                    component.startPosition.x + (-3f..3f).random(),
                    component.startPosition.y + (2f..4f).random()
                )
                component.label += Actions.sequence(
                    fadeOut(2f, Interpolation.pow3OutInverse),
                    Actions.run {
                        component.remove = true
                    }
                )
                uiStage.addActor(component.label)
            }

            override fun onComponentRemoved(entity: Entity, component: FloatingTextComponent) {
                uiStage.root.removeActor(component.label)
            }
        }
    }
}
