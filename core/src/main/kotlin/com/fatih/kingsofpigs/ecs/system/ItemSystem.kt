package com.fatih.kingsofpigs.ecs.system

import com.fatih.kingsofpigs.ecs.component.ItemComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([ItemComponent::class])
class ItemSystem (
    private val itemComps : ComponentMapper<ItemComponent>
): IteratingSystem(){

    override fun onTickEntity(entity: Entity) {
        val itemComponent = itemComps[entity]
        itemComponent.run{
            if (collideEntity == null) return
            println("yesSirrrrSss")
            world.remove(entity)
        }
    }
}
