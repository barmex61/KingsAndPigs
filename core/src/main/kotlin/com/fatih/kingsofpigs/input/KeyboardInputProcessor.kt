package com.fatih.kingsofpigs.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
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
        val processor = multiplexer.processors.firstOrNull { it::class.java == inputProcessor::class.java }
        processor?.let {
            multiplexer.removeProcessor(it)
        }
        multiplexer.addProcessor(inputProcessor)
    }
}

fun removeProcessor(inputProcessor: InputProcessor){
    val inputMultiplexer = Gdx.input.inputProcessor as InputMultiplexer
    inputMultiplexer.removeProcessor(inputProcessor)
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

    var moveComponent : MoveComponent? = null
    var attackComponent : MeleeAttackComponent? = null

    private var playerSin : Float = 0f
    private var playerCos : Float = 0f

    private fun Int.isMovementKey() = this == UP || this == RIGHT || this == LEFT
    private fun Int.isAttackKey() = this == SPACE
    private fun Int.changeScreenKey() = this == C

    override fun keyDown(keycode: Int): Boolean {
        if (!keycode.isMovementKey() && !keycode.isAttackKey() && !keycode.changeScreenKey()) return false
        println(keycode)
        when(keycode){
            RIGHT -> playerCos+=1f
            LEFT-> playerCos-=1f
            UP -> playerSin+=1f
            C -> changeScreen()
            SPACE -> updateAttack()
        }
        updateMovement()
        return true
    }

    fun updatePlayerValues(x : Float,y : Float){
        playerCos = x
        playerSin = y
        updateMovement()
    }

    override fun keyUp(keycode: Int): Boolean {
        if(!keycode.isMovementKey() && !keycode.isAttackKey() && !keycode.changeScreenKey()) return false
        when(keycode){
            RIGHT ->  playerCos-=1f
            LEFT ->  playerCos+=1f
            UP ->  playerSin-=1f
        }
        updateMovement()
        return true
    }

    fun updateAttack(){
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
