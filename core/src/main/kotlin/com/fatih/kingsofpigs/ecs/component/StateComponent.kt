package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.fatih.kingsofpigs.state.PlayerEntity
import com.fatih.kingsofpigs.state.PlayerState
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class StateComponent (
    var currentState : PlayerState = PlayerState.DOOR_OUT,
    val stateMachine: DefaultStateMachine<PlayerEntity,PlayerState> = DefaultStateMachine()
) {

    companion object{
        class StateComponentListener(private val world: World) : ComponentListener<StateComponent>{
            override fun onComponentAdded(entity: Entity, component: StateComponent) {
                component.stateMachine.owner = PlayerEntity(world, entity)
            }

            override fun onComponentRemoved(entity: Entity, component: StateComponent) = Unit
        }
    }
}
