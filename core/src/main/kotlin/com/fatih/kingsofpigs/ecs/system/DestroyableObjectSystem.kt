package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.fatih.kingsofpigs.actor.FlipImage
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.DestroyableComponent
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.ItemComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.plus
import ktx.math.random
import ktx.math.vec2
import kotlin.experimental.or

@AllOf([DestroyableComponent::class])
class DestroyableObjectSystem(
    private val destroyableComps : ComponentMapper<DestroyableComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val textureAtlas : TextureAtlas,
    private val box2dWorld : World ,
    private val gameStage : Stage
) : IteratingSystem(){

    override fun onTickEntity(entity: Entity) {
        val destroyableComponent = destroyableComps[entity]
        destroyableComponent.run {
            if (destroy){
                destroyTimer -= deltaTime
                bodyList.forEachIndexed { index, body ->
                    val image = images[index]
                    image.isVisible = true
                    image.setPosition(body.position.x-image.width/2f,body.position.y -image.height/2f)
                }
                if (destroyTimer <= 0f){
                    world.remove(entity)
                }
            }
            if (createBodies){
                imageComps[entity].image.isVisible = false
                physicComps[entity].body.fixtureList.first().isSensor = true
                val bodyPos = physicComps[entity].body.position + physicComps[entity].bodyOffset
                destroyTimer -= deltaTime
                (0..3).forEach { i ->
                    val body = PhysicComponent.createBody(box2dWorld,Rectangle(bodyPos.x,bodyPos.y,0.5f,0.5f),
                        Constants.OBJECT,Constants.OBJECT ,
                        BodyDef.BodyType.DynamicBody,
                        vec2(1f,1f),
                        vec2(), fixedRotation = false, entity = entity
                    )
                    val image = Image(textureAtlas.findRegion("box/piece",i+1)).apply { setSize(0.5f,0.5f)}
                    images.add(image)
                    gameStage.addActor(image)
                    bodyList.add(body)
                    body.applyLinearImpulse(Vector2((-50f..50f).random(),(50f..100f).random()),body.worldCenter +(-0.5f..0.5f).random(),true)
                }

                destroy = true
                createBodies = false
                items.forEachIndexed { i, item->
                    world.entity {
                        add<ItemComponent>{
                            this.item = item
                            this.extraLife = 5f
                        }
                        add<PhysicComponent>{
                            body = PhysicComponent.createBody(box2dWorld,Rectangle(bodyPos.x,bodyPos.y,0.65f,0.6f),
                                Constants.ITEM,Constants.OBJECT or Constants.KING or Constants.ITEM,
                                BodyDef.BodyType.DynamicBody,
                                vec2(1f,1f),
                                vec2(0.08f,0f), fixedRotation = true, entity = entity, isItem = true
                            )
                            body.applyLinearImpulse(vec2(-100f + i * 50f,100f),body.worldCenter +(-0.5f..0.5f).random(),true)
                        }
                        add<ImageComponent>{
                            image = FlipImage().apply {
                                setSize(0.8f,0.7f)
                            }
                        }
                        add<AnimationComponent>{
                            println(item.javaClass.simpleName)
                            this.entityModel = EntityModel.valueOf(item.javaClass.simpleName.uppercase())
                            nextAnimation(AnimationType.IDLE)
                        }
                    }
                }
            }
        }
    }
}
