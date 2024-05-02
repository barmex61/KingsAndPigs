package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.DeadComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.event.GameOverEvent
import com.fatih.kingsofpigs.event.HpChangeEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadComps : ComponentMapper<DeadComponent>,
    private val lifeComps : ComponentMapper<LifeComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val gameStage : Stage
) : IteratingSystem(){

    override fun onTickEntity(entity: Entity) {
        val deadComponent = deadComps[entity]
        deadComponent.run {
            if (!canResurrect && remove ){
                world.remove(entity)
                if (entity in playerComps){
                    gameStage.fireEvent(GameOverEvent())
                }
                return
            }
            if (canResurrect){
                resurrectionTime -= deltaTime
                if (lifeComps[entity].currentLife> 0){
                    if (resurrectionTime <= 0f ){
                        configureEntity(entity){
                            deadComps.remove(it)
                            lifeComps[entity].apply {
                                currentHp = maxHp
                                damageTaken = 0f
                                gameStage.fireEvent(HpChangeEvent(currentHp/maxHp))
                            }
                        }
                    }
                }else{
                    if (resurrectionTime <= 0f){
                        canResurrect = false
                        remove = true
                    }
                }
            }

        }
    }
}
