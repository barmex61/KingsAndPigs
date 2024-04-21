package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.physics.box2d.Body
import com.fatih.kingsofpigs.utils.Constants
import kotlin.experimental.or

enum class AttackState{
    READY,PREPARE,ATTACK,WAIT
}

class MeleeAttackComponent (
    var attackRange : Float = DEFAULT_ATTACK_RANGE,
    var attackDamage : Int = DEFAULT_ATTACK_DAMAGE,
    var critChance : Int = 20,
    var critDamage : Float = 2f,
    var attackState: AttackState = AttackState.READY,
    var doAttack : Boolean = false,
    val maxDestroyBodyTime : Float = 0.15f,
    var destroyBodyTime : Float = maxDestroyBodyTime,
    var attackPolyLine : FloatArray = floatArrayOf(),
    var attackFloatArray : FloatArray = floatArrayOf(),
    var attackFloatArrayMirror : FloatArray = floatArrayOf(),
    var isPlayer : Boolean = false

){
    lateinit var entityModel : EntityModel
    var attackBody : Body? = null

    val attackBodyMaskBit : Short
        get()  {
            return if (isPlayer) Constants.ENEMY or Constants.ITEM or Constants.ATTACK_OBJECT or Constants.OBJECT or Constants.DESTROYABLE
            else Constants.KING or Constants.ATTACK_OBJECT or Constants.OBJECT
        }

    fun startAttack(){
        if (attackState == AttackState.READY && doAttack ){
            attackState = AttackState.PREPARE
        }
    }

    companion object{
        const val DEFAULT_ATTACK_RANGE = 1f
        const val DEFAULT_ATTACK_DAMAGE = 4
    }

}
