package com.fatih.kingsofpigs.state

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.AttackComponent
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.ImageComponent
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
    attackComps : ComponentMapper<AttackComponent> = world.mapper(),
    physicComps : ComponentMapper<PhysicComponent> = world.mapper()
) {

    private val animationComponent = animComps[entity]
    private val moveComponent = moveComps[entity]
    private val stateComponent = stateComps[entity]
    private val attackComponent = attackComps[entity]
    private val physicComponent = physicComps[entity]

    val wantsToRun : Boolean
        get() = moveComponent.cos != 0f

    val wantsToAttack : Boolean
        get() = attackComponent.doAttack && attackComponent.attackState == AttackState.READY && attackComponent.attackBody == null

    val doAttack : Boolean
        get() = attackComponent.doAttack

    val isJumping : Boolean
        get() = physicComponent.body.linearVelocity.y > 0.01f

    val isFalling : Boolean
        get() = physicComponent.body.linearVelocity.y < -0.01f

    val isAnimationDone : Boolean
        get() = animationComponent.isAnimationDone

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
