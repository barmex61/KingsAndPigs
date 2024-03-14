package com.fatih.kingsofpigs.ecs.system

import com.fatih.kingsofpigs.ecs.component.StateComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([StateComponent::class])
class StateSystem(
    private val stateComps : ComponentMapper<StateComponent>
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val stateComponent = stateComps[entity]
        stateComponent.run {
            stateMachine.update()
            if (currentState != stateMachine.currentState){
                stateMachine.changeState(currentState)
            }
        }
    }
}
