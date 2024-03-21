package com.fatih.kingsofpigs.ecs.system

import com.fatih.kingsofpigs.ecs.component.AiComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([AiComponent::class])
class AiSystem(
    private val aiComps : ComponentMapper<AiComponent>
) : IteratingSystem(){

    override fun onTickEntity(entity: Entity) {
        val aiComponent = aiComps[entity]
        aiComponent.run {
            behaviorTree.step()
        }
    }
}
