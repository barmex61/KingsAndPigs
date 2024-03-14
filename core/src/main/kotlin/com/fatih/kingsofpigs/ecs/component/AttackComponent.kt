package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import kotlin.experimental.or

enum class AttackState{
    READY,PREPARE,ATTACK,WAIT
}

class AttackComponent (
    var isRangeAttack : Boolean = false,
    var attackRange : Float = DEFAULT_ATTACK_RANGE,
    var attackDamage : Int = DEFAULT_ATTACK_DAMAGE,
    var critChance : Int = 20,
    var critDamage : Float = 2f,
    var attackState: AttackState = AttackState.READY,
    var doAttack : Boolean = false,
    val maxDestroyBodyTime : Float = MAX_DESTROY_DELAY,
    var destroyBodyTime : Float = maxDestroyBodyTime,
    var attackPolyLine : FloatArray = floatArrayOf(),
    var attackFloatArray : FloatArray = floatArrayOf(),
    var attackFloatArrayMirror : FloatArray = floatArrayOf(),
    var isPlayer : Boolean = false,
    var createAttackBody : Boolean = true
){
    lateinit var entityModel : EntityModel
    var attackBody : Body? = null
    var attackBodyImage : Image? = null
    lateinit var attackBodyType : BodyType


    val attackBodyMaskBit : Short
        get()  {
            return if (isPlayer) Constants.ENEMY or Constants.ITEM or Constants.ATTACK_OBJECT
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
        const val MAX_DESTROY_DELAY = 1.5f
        class AttackComponentListener(private val gameStage : Stage) : ComponentListener<AttackComponent>{
            override fun onComponentAdded(entity: Entity, component: AttackComponent) {
                if (component.attackBodyImage != null){
                    gameStage.addActor(component.attackBodyImage)
                }
            }

            override fun onComponentRemoved(entity: Entity, component: AttackComponent) {
                if (component.attackBodyImage != null){
                    gameStage.root.removeActor(component.attackBodyImage)
                }
            }
        }
    }

}
