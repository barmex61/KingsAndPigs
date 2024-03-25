package com.fatih.kingsofpigs.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.MeleeAttackComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World

fun addProcessor(inputProcessor: InputProcessor){

    if (Gdx.input.inputProcessor == null){
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor)
    }else{
        val multiplexer = (Gdx.input.inputProcessor as InputMultiplexer)
        val processor = multiplexer.processors.first { it::class.java == inputProcessor::class.java }
        processor?.let {
            multiplexer.removeProcessor(it)
        }
        multiplexer.addProcessor(inputProcessor)
    }
}

class KeyboardInputProcessor(
    private val world: World,
    private val moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    private val attackComps : ComponentMapper<MeleeAttackComponent> = world.mapper(),
    var changeScreen : () -> Unit
) : InputAdapter() {

    init {
        addProcessor(this)
    }

    private var moveComponent : MoveComponent? = null
    private var attackComponent : MeleeAttackComponent? = null

    private var playerSin : Float = 0f
    private var playerCos : Float = 0f

    private fun Int.isMovementKey() = this == W || this == D || this == A
    private fun Int.isAttackKey() = this == SPACE
    private fun Int.changeScreenKey() = this == C

    override fun keyDown(keycode: Int): Boolean {
        if (!keycode.isMovementKey() && !keycode.isAttackKey() && !keycode.changeScreenKey()) return false
        when(keycode){
            D -> playerCos+=1f
            A -> playerCos-=1f
            W -> playerSin+=1f
            C -> changeScreen()
            SPACE -> updateAttack()
        }
        updateMovement()
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if(!keycode.isMovementKey() && !keycode.isAttackKey() && !keycode.changeScreenKey()) return false
        when(keycode){
            D ->  playerCos-=1f
            A ->  playerCos+=1f
            W ->  playerSin-=1f
        }
        updateMovement()
        return true
    }

    private fun updateAttack(){
        if (attackComponent == null){
            attackComponent = attackComps[world.family(allOf = arrayOf(PlayerComponent::class)).first()]
        }
        if (attackComponent!!.attackState == AttackState.READY && attackComponent!!.attackBody == null) {
            attackComponent!!.doAttack = true
        }
    }

    private fun updateMovement(){
        if (moveComponent == null){
            val family = world.family(allOf = arrayOf(PlayerComponent::class))
            if (family.isNotEmpty){
                moveComponent = moveComps[world.family(allOf = arrayOf(PlayerComponent::class)).first()]
            }
        }
        moveComponent?.run {
            cos = playerCos
            sin = playerSin
        }

    }
}
