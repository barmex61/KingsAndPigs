package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import ktx.math.vec2
import kotlin.experimental.or



class RangeAttackComponent(
    var attackRange : Float = 1f,
    var attackDamage : Int = 1,
    var critChance : Int = 20,
    var critDamage : Float = 2f,
    var attackState: AttackState = AttackState.READY,
    var doAttack : Boolean = false,
    val maxDestroyBodyTime : Float = 3f,
    var destroyBodyTime : Float = 0f,
    var attackPolyLine : FloatArray = floatArrayOf(),
    var attackFloatArray : FloatArray = floatArrayOf(),
    var attackFloatArrayMirror : FloatArray = floatArrayOf(),
    var imageScaling : Vector2 = vec2(),
    var attackImageOffset : Vector2 = vec2(),
    var animationTimer : Float = 0f,
    var animationPath : String = "",
    var startAnimPath : String = "",
    var resumeAnimPath : String = "",
    var endAnimPath : String = "",
    var animDone : Boolean = false,
    var attackImpulse : Vector2 = vec2()
) {
    var attackBody : Body? = null
    var animation : Animation<TextureRegionDrawable>? = null
    val image : Image = Image()
    lateinit var entityModel : EntityModel

    fun setAnimation(animPath : String,animation: Animation<TextureRegionDrawable>){
        this.animationPath = animPath
        this.animation = animation
        this.animationTimer = 0f
    }


    val fixedRotation : Boolean
        get() {
            return entityModel != EntityModel.PIG_BOX
        }

    val attackBodyMaskBit : Short
        get() = Constants.KING or Constants.ATTACK_OBJECT or Constants.OBJECT


    fun startAttack(){
        if (attackState == AttackState.READY && doAttack ){
            attackState = AttackState.PREPARE
        }
    }

    companion object{
        class RangeAttackComponentListener(
            private val gameStage : Stage,
            private val box2dWorld: World) :
            ComponentListener<RangeAttackComponent> {
            override fun onComponentAdded(entity: Entity, component: RangeAttackComponent) {
                gameStage.addActor(component.image)
                when(component.entityModel){
                    EntityModel.PIG_BOMB->{
                        component.startAnimPath = "bomb/bomb_off"
                        component.resumeAnimPath = "bomb/bomb_on"
                        component.endAnimPath = "bomb/bomb_explode"
                    }
                    EntityModel.PIG_LIGHT->{
                        component.startAnimPath = "cannon/cannon_ball"
                        component.resumeAnimPath = "cannon/cannon_ball"
                        component.endAnimPath = "bomb/bomb_explode"
                    }
                    else -> Unit
                }
            }

            override fun onComponentRemoved(entity: Entity, component: RangeAttackComponent) {
                gameStage.root.removeActor(component.image)
                component.attackBody?.let {
                    box2dWorld.destroyBody(it)
                }
                component.attackBody = null

            }
        }
    }
}
