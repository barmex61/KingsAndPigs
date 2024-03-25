package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.fatih.kingsofpigs.ecs.component.FloatingTextComponent
import com.fatih.kingsofpigs.ecs.component.Item.*
import com.fatih.kingsofpigs.ecs.component.ItemComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ui.Fonts
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.plus
import com.fatih.kingsofpigs.ui.get
import ktx.scene2d.Scene2DSkin

@AllOf([ItemComponent::class])
class ItemSystem (
    private val itemComps : ComponentMapper<ItemComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val lifeComps : ComponentMapper<LifeComponent>
): IteratingSystem(){

    private val hpLabelStyle = LabelStyle(Scene2DSkin.defaultSkin[Fonts.SEGOE_PRINT_GRADIENT_GREEN],null)

    override fun onTickEntity(entity: Entity) {
        val itemComponent = itemComps[entity]
        itemComponent.run{
            if (collideEntity == null) return
            val pos = physicComps[entity].body.position + physicComps[entity].bodyOffset
            collectItem(pos,collideEntity!!)
            world.remove(entity)
        }
    }

    private fun ItemComponent.collectItem(position: Vector2,entity: Entity){
        when(item){
            is Diamond ->{
                lifeComps[entity].currentLife = (lifeComps[entity].currentLife + (item as Diamond).extraLife).coerceAtMost(lifeComps[entity].maxLife)
            }
            is Heart ->{
                world.entity {
                    add<FloatingTextComponent>{
                        this.text = "+ ${(item as Heart).hp} HP"
                        this.startPosition.set(position)
                        label = Label(text + if (isCrit) " Crit!" else "",hpLabelStyle)
                    }
                }
            }
        }
    }
}
