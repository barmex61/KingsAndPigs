package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.KingOfPigs.Companion.UNIT_SCALE
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.event.JumpEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([MoveComponent::class])
class MoveSystem(
    private val moveComps : ComponentMapper<MoveComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val gameStage : Stage
) : IteratingSystem(){

    override fun onTickEntity(entity: Entity) {
        val moveComponent = moveComps[entity]
        val physicComponent = physicComps[entity]
        val imageComponent = imageComps[entity]
        val velocity = physicComponent.body.linearVelocity
        moveComponent.run {
            if (velocity.y in -0.000001f..0.000001f){
                velocity.y = 0f
            }
            if (velocity.y != 0f ) {
                sin = 0f
            }
            val mass = physicComponent.body.mass
            physicComponent.impulse.set(
                if (!root) mass * cos * speed - velocity.x else 0f,
                if (!root) mass * sin * 1/UNIT_SCALE * 2.5F  else 0f
            )
            if (cos != 0f && !root){
                if (entity in playerComps) {
                    imageComponent.image.flipX = cos < 0f
                }else{
                    imageComponent.image.flipX = cos >= 0f
                }
            }
            if (entity in playerComps && velocity.y == 0f && sin == 1f && !root){
                gameStage.fireEvent(JumpEvent())
                sin = 0f
            }
        }
    }
}
