package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.MeleeAttackComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.createBody
import com.fatih.kingsofpigs.ecs.component.RangeAttackComponent
import com.fatih.kingsofpigs.event.MeleeAttackEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.component1
import ktx.math.component2
import ktx.math.minus
import ktx.math.vec2

@AllOf([MeleeAttackComponent::class])
class MeleeAttackSystem(
    private val attackComps : ComponentMapper<MeleeAttackComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val animComps : ComponentMapper<AnimationComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val box2dWorld : World,
    private val gameStage : Stage
) : IteratingSystem(){


    override fun onTickEntity(entity: Entity) {
        val attackComponent = attackComps[entity]
        attackComponent.run {
            if (attackBody != null){
                destroyBodyTime -= deltaTime
                if (destroyBodyTime <= 0f ){
                    box2dWorld.destroyBody(attackBody)
                    attackBody = null
                    destroyBodyTime = maxDestroyBodyTime
                }
            }

            if (attackState == AttackState.READY){
                return
            }
            if (attackState == AttackState.PREPARE ){
                val physicComponent = physicComps[entity]
                val (posX,posY) = physicComponent.body.position
                val (offX,offY) = physicComponent.bodyOffset
                val pos = vec2(posX + offX,posY + offY)
                attackPolyLine = if (!imageComps[entity].image.flipX) attackFloatArray.addPos(pos) else attackFloatArrayMirror.addPos(pos)
                if (physicComponent.body.linearVelocity.x != 0f){
                    (attackPolyLine.indices).forEach {
                        if (it % 2 == 0){
                            attackPolyLine[it] += physicComponent.body.linearVelocity.x * 0.06f
                        }
                    }
                }
                gameStage.fireEvent(MeleeAttackEvent(physicComponent.body.linearVelocity.y > 0.1f))
                //WATTACK_POLYLINE.vertices = attackPolyLine
                attackState = AttackState.ATTACK
            }
            if (attackState == AttackState.ATTACK ){
                attackBody = createBody(
                    box2dWorld,Polyline(attackPolyLine),Constants.ATTACK_OBJECT,attackBodyMaskBit,
                    BodyDef.BodyType.DynamicBody, vec2(1f,1f), vec2(0f,0f), fixedRotation = false,
                    isAttackBody = true , entity = entity
                )
                attackState = AttackState.WAIT
            }
            if (attackState == AttackState.WAIT && animComps[entity].isAnimationDone){
                attackState = AttackState.READY
                doAttack = false
            }

        }
    }

    companion object{
        val ATTACK_POLYLINE = Polyline()
        val ATTACK_RECT = Rectangle()
        fun FloatArray.addPos(pos:Vector2) : FloatArray {
            val floatArray = this.copyOf()
            forEachIndexed { index, fl ->
                if (index % 2 == 0 ){
                    floatArray[index] = fl + pos.x
                }else{
                    floatArray[index] = fl + pos.y
                }
            }
            return floatArray
        }
    }
}
