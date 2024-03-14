package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import ktx.app.gdxError
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.polygon
import ktx.math.vec2

class PhysicComponent {

    val previousPos : Vector2 = vec2()
    val impulse : Vector2 = vec2()
    val bodyOffset : Vector2 = vec2()
    lateinit var body : Body

    companion object{

        const val ENTITY_COLLISION_FIXTURE = "Entity collision fixture"
        const val DEAL_DAMAGE = "Deal damage"
        const val CANT_DEAL_DAMAGE = "Cant deal damage"
        const val PLATFORM_FIXTURE = "Platform fixture"
        const val AI_CIRCLE = "Ai circle"

        fun createBody(
            box2dWorld: World,
            shape : Shape2D,
            categoryBit : Short,
            maskBit : Short,
            bodyType: BodyType,
            physicScaling : Vector2,
            physicOffset : Vector2,
            isPortal : Boolean = false,
            isCollision : Boolean = false,
            isAttackBody : Boolean = false,
            fixedRotation : Boolean = true,
            isPlatform : Boolean = false,
            aiCircle : Float = 0f,
            entity: Entity? = null
        ) : Body {
            val body = box2dWorld.body(bodyType){
                linearDamping = 2f
                this.fixedRotation = fixedRotation
                if (entity != null) userData = entity
                if (isAttackBody){
                    when(shape){
                        is Rectangle ->{
                            this.position.set(shape.x + shape.width/2f,shape.y + shape.height/2f)
                            box(shape.width,shape.height){
                                isSensor = false
                                filter.categoryBits = categoryBit
                                filter.maskBits = maskBit
                                density = 10f
                                userData = DEAL_DAMAGE
                            }
                        }
                        is Polyline ->{
                            polygon(shape.vertices){
                                isSensor = false
                                filter.categoryBits = categoryBit
                                filter.maskBits = maskBit
                                userData = DEAL_DAMAGE
                            }
                        }
                    }
                }
                else {
                    when(shape){
                        is Rectangle ->{
                            this.position.set(shape.x + shape.width/2f,shape.y + shape.height/2f)
                            val size = vec2(shape.width * physicScaling.x ,shape.height * physicScaling.y )
                            polygon(floatArrayOf(
                                -size.x/2f + physicOffset.x,-size.y/2f + physicOffset.y,
                                size.x/2f + physicOffset.x , -size.y/2f+ physicOffset.y,
                                size.x /2f+ physicOffset.x , size.y/2f + physicOffset.y,
                                -size.x /2f+ physicOffset.x , size.y/2f + physicOffset.y
                            )){
                                isSensor = isPortal
                                filter.categoryBits = categoryBit
                                filter.maskBits = maskBit
                                userData = if (isPlatform) PLATFORM_FIXTURE else ENTITY_COLLISION_FIXTURE
                                density = 9f
                            }
                            if (aiCircle != 0f){
                                circle(aiCircle){
                                    isSensor = true
                                    userData = AI_CIRCLE
                                    filter.categoryBits = categoryBit
                                    filter.maskBits = Constants.KING
                                }
                            }
                        }
                        else -> gdxError("Shape is not supported $shape")
                    }
                }
            }
            return body
        }

        class PhysicComponentListener(private val box2dWorld:World) : ComponentListener<PhysicComponent>{
            override fun onComponentAdded(entity: Entity, component: PhysicComponent) {
                component.body.userData = entity
            }

            override fun onComponentRemoved(entity: Entity, component: PhysicComponent) {
                box2dWorld.destroyBody(component.body)
                component.body.userData = null
            }
        }
    }
}
