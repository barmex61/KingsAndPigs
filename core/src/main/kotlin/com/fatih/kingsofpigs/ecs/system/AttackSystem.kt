package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AttackComponent
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.createBody
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.component1
import ktx.math.component2
import ktx.math.minus
import ktx.math.vec2

@AllOf([AttackComponent::class])
class AttackSystem(
    private val attackComps : ComponentMapper<AttackComponent>,
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
                if (attackBodyImage != null){
                    attackBodyImage!!.run {
                        setPosition(attackBody!!.position.x - width/2f,attackBody!!.position.y - height/2f)
                        rotation = MathUtils.radiansToDegrees * attackBody!!.angle
                        setOrigin(Align.center)
                    }
                }
                if (destroyBodyTime <= 0f || attackBody!!.fixtureList.first().userData == PhysicComponent.CANT_DEAL_DAMAGE){
                    box2dWorld.destroyBody(attackBody)
                    attackBody = null
                    destroyBodyTime = maxDestroyBodyTime
                    attackBodyImage?.isVisible = false
                }
            }
            if (attackState == AttackState.READY){
                return
            }
            if (attackState == AttackState.PREPARE){
                val physicComponent = physicComps[entity]
                val (posX,posY) = physicComponent.body.position
                val (offX,offY) = physicComponent.bodyOffset
                val pos = vec2(posX + offX,posY + offY)
                if (!isRangeAttack){
                    attackPolyLine = if (!imageComps[entity].image.flipX) attackFloatArray.myPlus(pos) else attackFloatArrayMirror.myPlus(pos)
                    ATTACK_POLYLINE.vertices = attackPolyLine
                }else{
                    attackPolyLine = if(!imageComps[entity].image.flipX) {
                        attackFloatArray.copyOf().apply {
                            this[0] = this[0] + pos.x
                            this[1] = this[1] + pos.y
                        }
                    }else {
                        attackFloatArrayMirror.copyOf().apply {
                            this[0] = this[0] + pos.x
                            this[1] = this[1] + pos.y
                        }
                    }
                    attackBodyImage?.apply {
                        isVisible = true
                        setSize(attackPolyLine[2],attackPolyLine[3])
                        setScaling(Scaling.stretch)
                    }

                }
                attackState = AttackState.ATTACK
            }
            if (attackState == AttackState.ATTACK && createAttackBody){
                attackBody = createBody(
                    box2dWorld,if (!isRangeAttack) Polyline(attackPolyLine) else Rectangle(attackPolyLine[0],attackPolyLine[1],attackPolyLine[2],attackPolyLine[3]),Constants.ATTACK_OBJECT,attackBodyMaskBit,
                    attackBodyType, vec2(1f,1f), vec2(0f,0f), fixedRotation = false,
                    isAttackBody = true , entity = entity
                )
                attackBody?.applyLinearImpulse(Vector2(-50f*attackBody!!.mass,50f*attackBody!!.mass),attackBody!!.worldCenter - 0.2f,true)
                if (isRangeAttack) createAttackBody = false
                attackState = AttackState.WAIT
            }
            if (attackState == AttackState.WAIT && animComps[entity].isAnimationDone){
                attackState = AttackState.READY
                doAttack = false
            }

        }
    }

    private fun FloatArray.myPlus(pos:Vector2) : FloatArray {
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

    companion object{
        val ATTACK_POLYLINE = Polyline()
        val ATTACK_RECT = Rectangle()
    }
}
