package com.fatih.kingsofpigs.ecs.system

import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.DeadComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadComps : ComponentMapper<DeadComponent>,
    private val lifeComps : ComponentMapper<LifeComponent>,
    private val animComps : ComponentMapper<AnimationComponent>
) : IteratingSystem(){

    override fun onTickEntity(entity: Entity) {
        val deadComponent = deadComps[entity]
        deadComponent.run {
            if (!canResurrect && remove ){
                world.remove(entity)
                return
            }else if (canResurrect){
                resurrectionTime -= deltaTime
                if (resurrectionTime <= 0f ){
                    configureEntity(entity){
                        deadComps.remove(it)
                        lifeComps[entity].apply {
                            currentLife = maxLife
                            damageTaken = 0f
                        }
                    }
                }
            }
        }
    }
}
