package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.fatih.kingsofpigs.ecs.component.DeadComponent
import com.fatih.kingsofpigs.ecs.component.FloatingTextComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.event.PigGetHitEvent
import com.fatih.kingsofpigs.event.PlayerGetHitEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.fatih.kingsofpigs.ui.Fonts
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf
import ktx.math.plus
import ktx.scene2d.Scene2DSkin
import com.fatih.kingsofpigs.ui.get

@AllOf([LifeComponent::class])
@NoneOf([DeadComponent::class])
class LifeSystem (
    private val lifeComps : ComponentMapper<LifeComponent>,
    private val deadComps : ComponentMapper<DeadComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val gameStage : Stage
): IteratingSystem(){

    private val damageLabelStyle = LabelStyle(Scene2DSkin.defaultSkin[Fonts.SEGOE_PRINT_GRADIENT_RED].apply { data.setScale(0.3f) },null)

    override fun onTickEntity(entity: Entity) {
        val lifeComponent = lifeComps[entity]
        lifeComponent.run {

            if (currentHp > 0f){
                currentHp = (currentHp + regeneration * deltaTime).coerceAtMost(maxHp)
                if (damageTaken > 0f){
                    createFloatingText(damageTaken.toInt().toString(),isCrit,physicComps[entity].body.position,physicComps[entity].bodyOffset)
                    if (entity in playerComps){
                        gameStage.fireEvent(PlayerGetHitEvent())
                        world.system<CameraSystem>().shakeCamera = true
                    }else{
                        gameStage.fireEvent(PigGetHitEvent())
                    }
                    currentHp -= damageTaken
                    damageTaken = 0f
                }
            }else{
                if (entity !in deadComps){
                    configureEntity(entity){
                        deadComps.add(entity){
                            this.canResurrect = this@run.canResurrect
                            this.resurrectionTime = this@run.resurrectionTime
                        }
                    }
                }
            }
        }
    }

    private fun createFloatingText(damageText : String,isCrit : Boolean,position : Vector2,offset : Vector2){
        world.entity {
            add<FloatingTextComponent>{
                this.text = damageText
                this.isCrit = isCrit
                this.startPosition.set(position + offset)
                label = Label(text + if (isCrit) " Crit!" else "",damageLabelStyle)
            }
        }
    }
}
