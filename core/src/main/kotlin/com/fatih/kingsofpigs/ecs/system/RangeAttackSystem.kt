package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.CANT_DEAL_DAMAGE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.DEAL_DAMAGE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.createBody
import com.fatih.kingsofpigs.ecs.component.RangeAttackComponent
import com.fatih.kingsofpigs.ecs.system.MeleeAttackSystem.Companion.addPos
import com.fatih.kingsofpigs.event.RangeAttackEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.box2d.circle
import ktx.math.component1
import ktx.math.component2
import ktx.math.minus
import ktx.math.vec2
import ktx.collections.map
import ktx.math.plus
import ktx.math.random

@AllOf([RangeAttackComponent::class])
class RangeAttackSystem(
    private val rangeAttackComps : ComponentMapper<RangeAttackComponent>,
    private val box2dWorld : World,
    private val physicComps: ComponentMapper<PhysicComponent>,
    private val animComps : ComponentMapper<AnimationComponent>,
    private val textureAtlas : TextureAtlas,
    private val gameStage : Stage,
    private val imageComps : ComponentMapper<ImageComponent>
) : IteratingSystem(){

    private val rangeAnimationCache = hashMapOf<String,Animation<TextureRegionDrawable>>()

    override fun onTickEntity(entity: Entity) {
        val rangeAttackComponent = rangeAttackComps[entity]
        rangeAttackComponent.run {
            if (attackBody != null){
                destroyBodyTime += deltaTime
                when{
                    destroyBodyTime <= maxDestroyBodyTime/4f && animationPath != startAnimPath->{
                        setAnimation(startAnimPath,animation(startAnimPath, playMode = PlayMode.LOOP))
                    }
                    destroyBodyTime >= maxDestroyBodyTime/4f && destroyBodyTime <= maxDestroyBodyTime*2f/3f && animationPath != resumeAnimPath ->{
                        setAnimation(resumeAnimPath,animation(resumeAnimPath, playMode = PlayMode.LOOP))
                    }
                    destroyBodyTime >= maxDestroyBodyTime*2f/3f && destroyBodyTime <= maxDestroyBodyTime && animationPath != endAnimPath ->{
                        setAnimation(endAnimPath,animation(endAnimPath, playMode = PlayMode.NORMAL, frameDuration = DEFAULT_FRAME_DURATION *2f))
                        attackBody!!.destroyFixture(attackBody!!.fixtureList.first())
                        attackBody!!.circle(2f){
                            userData = DEAL_DAMAGE
                            density = 50f
                            isSensor = true
                            filter.categoryBits = Constants.ATTACK_OBJECT
                            filter.maskBits = Constants.KING
                            filter.groupIndex = 1
                            attackBody!!.gravityScale = 0f
                            attackBody!!.setLinearVelocity(0f,0f)
                        }
                        if (entityModel == EntityModel.PIG_LIGHT){
                            image.setSize(4f,4f)
                        }
                        gameStage.fireEvent(RangeAttackEvent(true))
                    }
                }

                animDone = animationPath == endAnimPath && animation?.isAnimationFinished(animationTimer) == true
                animationTimer += deltaTime
                image.apply {
                    if (animation != null){
                        drawable = animation!!.getKeyFrame(animationTimer)
                    }
                    setOrigin(Align.center)
                    rotation = MathUtils.radiansToDegrees * attackBody!!.angle
                    setPosition(attackBody!!.position.x - width/2f + if (animationPath!= endAnimPath) attackImageOffset.x else 0f ,attackBody!!.position.y - height/2f + if (animationPath!= endAnimPath) attackImageOffset.y else 0f)
                }
                if (destroyBodyTime >= maxDestroyBodyTime || animDone ){
                    box2dWorld.destroyBody(attackBody)
                    attackBody = null
                    destroyBodyTime = 0f
                    image.isVisible = false
                }
            }
            if (attackState == AttackState.READY){
                return
            }
            if (attackState == AttackState.PREPARE){
                val physicComponent = physicComps[entity]
                val (posX,posY) = physicComponent.body.position
                val (offX,offY) = physicComponent.bodyOffset
                val pos = vec2(posX + offX + if(entityModel == EntityModel.PIG_LIGHT && imageComps[entity].image.flipX) 5f else 0f,posY + offY)
                attackPolyLine = attackFloatArray.copyOf().apply {
                    this[0] = this[0] + pos.x
                    this[1] = this[1] + pos.y
                }
                if (entityModel == EntityModel.PIG_LIGHT){
                    gameStage.fireEvent(RangeAttackEvent())
                }
                attackState = AttackState.ATTACK
            }
            if (attackState == AttackState.ATTACK){
                attackBody = createBody(
                    box2dWorld, getShape(attackPolyLine,entityModel), Constants.ATTACK_OBJECT, attackBodyMaskBit, BodyDef.BodyType.DynamicBody, vec2(1f, 1f),
                    vec2(0f, 0f),fixedRotation = fixedRotation, isAttackBody = true, entity = entity,
                    usData = if (entityModel == EntityModel.PIG_BOX) DEAL_DAMAGE else CANT_DEAL_DAMAGE
                )

                attackBody!!.applyLinearImpulse(attackImpulse,attackBody!!.worldCenter + (-1f..1f).random(),true)
                image.apply {
                    setOrigin(Align.center)
                    setSize(attackPolyLine[2]* imageScaling.x,attackPolyLine[3] * imageScaling.y)
                    setPosition(attackBody!!.position.x - width/2f + attackImageOffset.x ,attackBody!!.position.y - height/2f + attackImageOffset.y)
                    isVisible = true
                }
                attackState = AttackState.WAIT
            }
            if (attackState == AttackState.WAIT && animComps[entity].isAnimationDone){
                attackState = AttackState.READY
                doAttack = false
            }
        }
    }

    private fun getShape(attackPolyLine: FloatArray,entityModel: EntityModel) : Shape2D{
       return when(entityModel){
            EntityModel.PIG_BOX ->{
                Rectangle(attackPolyLine[0],attackPolyLine[1],attackPolyLine[2],attackPolyLine[3])
            }
           else -> Circle(attackPolyLine[0],attackPolyLine[1],attackPolyLine[2]/2f)
        }
    }

    private fun animation(animPath : String,frameDuration : Float = DEFAULT_FRAME_DURATION,playMode: PlayMode = PlayMode.LOOP) : Animation<TextureRegionDrawable> {
        return rangeAnimationCache.getOrPut(animPath){
            Animation(frameDuration,textureAtlas.findRegions(animPath).map {
                TextureRegionDrawable(it)
            },playMode)
        }
    }
}
