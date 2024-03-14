package com.fatih.kingsofpigs.ai

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.fatih.kingsofpigs.ecs.component.AiComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.AttackComponent
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class PigEntity(
    world:World,
    val entity: Entity,
    moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    physicComps : ComponentMapper<PhysicComponent> = world.mapper(),
    attackComps : ComponentMapper<AttackComponent> = world.mapper(),
    aiComps : ComponentMapper<AiComponent> = world.mapper(),
    animComps : ComponentMapper<AnimationComponent> = world.mapper()
) {
    private val physicComponent = physicComps[entity]
    private val moveComponent = moveComps[entity]
    private val attackComponent = attackComps[entity]
    private val aiComponent = aiComps[entity]
    private val animationComponent = animComps[entity]
    private val entityModel = attackComponent.entityModel

    val isPigBomb : Boolean
        get() = entityModel == EntityModel.PIG_BOMB

    val isPigBox : Boolean
        get() = entityModel == EntityModel.PIG_BOX

    val isJumping : Boolean
        get() = physicComponent.body.linearVelocity.y > 0.001f

    val isFalling : Boolean
        get() = physicComponent.body.linearVelocity.y < -0.001f

    val isMeleeAttack : Boolean
        get() = !attackComponent.isRangeAttack

    val isRangeAttack : Boolean
        get() = attackComponent.isRangeAttack

    val canAttack : Boolean
        get() = !attackComponent.doAttack && attackComponent.attackState == AttackState.READY && attackComponent.attackBody == null

    val isEnemyNearby : Boolean
        get() = aiComponent.nearbyEntities.size > 0

    val animationDone : Boolean
        get() = animationComponent.isAnimationDone

    var createAttackBody : Boolean = attackComponent.createAttackBody
        get() = attackComponent.createAttackBody
        set(value)  {
            attackComponent.createAttackBody = value
            field = value
        }

    val animKeyFrame : Int
        get() = animationComponent.animation.getKeyFrameIndex(animationComponent.animationTimer)

    fun startAttack(){
        attackComponent.run {
            attackComponent.doAttack = true
            attackComponent.startAttack()
        }
    }


    fun animation(animationType: AnimationType, playMode : PlayMode = PlayMode.LOOP, frameDuration : Float = DEFAULT_FRAME_DURATION){
        val animType = when (animationComponent.entityModel) {
            EntityModel.PIG_BOX_HIDE -> AnimationType.LOOKING_OUT
            EntityModel.PIG_LIGHT -> AnimationType.LIGHT_READY
            else -> animationType
        }
        if (animationComponent.animationType != animType){
            animationComponent.nextAnimation(animType, playMode, frameDuration)
        }
    }
}
