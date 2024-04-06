package com.fatih.kingsofpigs.state

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.MeleeAttackComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.StateComponent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class PlayerEntity(
    world : World,
    entity: Entity,
    animComps : ComponentMapper<AnimationComponent> = world.mapper(),
    moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    stateComps : ComponentMapper<StateComponent> = world.mapper(),
    attackComps : ComponentMapper<MeleeAttackComponent> = world.mapper(),
    physicComps : ComponentMapper<PhysicComponent> = world.mapper(),
    lifeComps : ComponentMapper<LifeComponent> = world.mapper()
) {

    private val animationComponent = animComps[entity]
    private val moveComponent = moveComps[entity]
    private val attackComponent = attackComps[entity]
    private val stateComponent = stateComps[entity]
    private val physicComponent = physicComps[entity]
    private val lifeComponent = lifeComps[entity]

    val wantsToRun : Boolean
        get() = moveComponent.cos != 0f

    val wantsToAttack : Boolean
        get() = attackComponent.doAttack && attackComponent.attackState == AttackState.READY && attackComponent.attackBody == null

    val doAttack : Boolean
        get() = attackComponent.doAttack

    val isJumping : Boolean
        get() = physicComponent.body.linearVelocity.y > 0.5f

    val isFalling : Boolean
        get() = physicComponent.body.linearVelocity.y < -0.5f

    val isAnimationDone : Boolean
        get() = animationComponent.isAnimationDone

    val isDead : Boolean
        get() = lifeComponent.currentHp <= 0f

    var getHit : Boolean
        get() = lifeComponent.getHit
        set(value) {
            lifeComponent.getHit = value
        }

    fun animation(animationType: AnimationType, playMode: PlayMode = PlayMode.LOOP, frameDuration : Float = DEFAULT_FRAME_DURATION){
        animationComponent.nextAnimation(animationType, playMode, frameDuration)
    }

    fun changeState(state: PlayerState){
        stateComponent.currentState = state
    }

    fun startAttack(){
        attackComponent.startAttack()
    }

    fun root(enabled:Boolean){
        moveComponent.root = enabled
    }

    fun changePreviousState(){
        stateComponent.currentState = stateComponent.stateMachine.previousState
    }
}
