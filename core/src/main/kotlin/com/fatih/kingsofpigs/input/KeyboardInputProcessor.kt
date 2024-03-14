package com.fatih.kingsofpigs.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.fatih.kingsofpigs.ecs.component.AttackComponent
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World

fun addProcessor(inputProcessor: InputProcessor){

    if (Gdx.input.inputProcessor == null){
        Gdx.input.inputProcessor = InputMultiplexer(inputProcessor)
    }else{
        (Gdx.input.inputProcessor as InputMultiplexer).addProcessor(inputProcessor)
    }
}

class KeyboardInputProcessor(
    private val world: World,
    private val moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    private val attackComps : ComponentMapper<AttackComponent> = world.mapper()
) : InputAdapter() {

    init {
        addProcessor(this)
    }

    private var moveComponent : MoveComponent? = null
    private var attackComponent : AttackComponent? = null

    private var playerSin : Float = 0f
    private var playerCos : Float = 0f

    private fun Int.isMovementKey() = this == W || this == D || this == A
    private fun Int.isAttackKey() = this == SPACE

    override fun keyDown(keycode: Int): Boolean {
        if (!keycode.isMovementKey() && !keycode.isAttackKey()) return false
        when(keycode){
            D -> playerCos = 1f
            A -> playerCos = -1f
            W -> playerSin = 1f
            SPACE -> updateAttack()
        }
        updateMovement()
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if(!keycode.isMovementKey() && !keycode.isAttackKey()) return false
        when(keycode){
            D ->  playerCos = if (Gdx.input.isKeyPressed(A)) -1f else 0f
            A ->  playerCos = if (Gdx.input.isKeyPressed(D)) 1f else 0f
            W ->  playerSin = 0f
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
            moveComponent = moveComps[world.family(allOf = arrayOf(PlayerComponent::class)).first()]
        }
        moveComponent!!.run {
            cos = playerCos
            sin = playerSin
        }

    }
}
