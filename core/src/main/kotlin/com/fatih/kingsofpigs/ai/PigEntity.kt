package com.fatih.kingsofpigs.ai

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.math.Vector2
import com.fatih.kingsofpigs.ecs.component.AiComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.AttackState
import com.fatih.kingsofpigs.ecs.component.DeadComponent
import com.fatih.kingsofpigs.ecs.component.DialogComponent
import com.fatih.kingsofpigs.ecs.component.DialogType
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.MeleeAttackComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.RangeAttackComponent
import com.fatih.kingsofpigs.screens.GameScreen
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.math.plus
import ktx.math.random
import kotlin.math.pow
import kotlin.math.sqrt

class PigEntity(
    private val world:World,
    val entity: Entity,
    moveComps : ComponentMapper<MoveComponent> = world.mapper(),
    private val physicComps : ComponentMapper<PhysicComponent> = world.mapper(),
    aiComps : ComponentMapper<AiComponent> = world.mapper(),
    meleeAttackComps : ComponentMapper<MeleeAttackComponent> = world.mapper(),
    private val animComps : ComponentMapper<AnimationComponent> = world.mapper(),
    rangeAttackComps : ComponentMapper<RangeAttackComponent> = world.mapper(),
    imageComps : ComponentMapper<ImageComponent> = world.mapper(),
    lifeComps : ComponentMapper<LifeComponent> = world.mapper(),
    private val deadComps : ComponentMapper<DeadComponent> = world.mapper(),
    private val dialogComps : ComponentMapper<DialogComponent> = world.mapper()
) {

    private val physicComponent = physicComps[entity]
    private val dialogComponent = dialogComps[entity]
    private val moveComponent = moveComps[entity]
    private val imageComponent = imageComps[entity]
    private val aiComponent = aiComps[entity]
    private val animationComponent = animComps[entity]
    private val lifeComponent = lifeComps[entity]
    val entityModel = animationComponent.entityModel
    private val meleeAttackComponent = meleeAttackComps.getOrNull(entity)
    private val rangeAttackComponent = rangeAttackComps.getOrNull(entity)
    private val previousPosition = physicComponent.body.position.cpy()

    val isPigBomb : Boolean
        get() = entityModel == EntityModel.PIG_BOMB

    val isPigBox : Boolean
        get() = entityModel == EntityModel.PIG_BOX

    val isPigLight : Boolean
        get() = entityModel == EntityModel.PIG_LIGHT

    var isGetHit : Boolean
        get() {
            return when(entityModel){
                EntityModel.PIG_BOMB,EntityModel.PIG_BOX -> false
                else -> lifeComponent.getHit
            }
        }
        set(value) {
            lifeComponent.getHit = value
        }

    val isMeleeAttack : Boolean
        get() = meleeAttackComponent != null

    val isRangeAttack : Boolean
        get() = rangeAttackComponent != null

    val position : Vector2
        get() = physicComponent.body.position

    val aiMoveRadius : Float
        get() = moveComponent.aiMoveRadius

    val isDead : Boolean
        get() = lifeComponent.currentHp <= 0f

    val canAttack : Boolean
        get()  {
            return if (rangeAttackComponent != null){
                !rangeAttackComponent.doAttack && rangeAttackComponent.attackState == AttackState.READY && rangeAttackComponent.attackBody == null && isEnemyNearby
            }else{
                !meleeAttackComponent!!.doAttack && meleeAttackComponent.attackState == AttackState.READY && meleeAttackComponent.attackBody == null && isEnemyNearby
            }
        }

    val isEnemyNearby : Boolean
        get() = aiComponent.nearbyEntities.size > 0

    val animationDone : Boolean
        get() = animationComponent.isAnimationDone

    val animKeyFrame : Int
        get() = animationComponent.animation.getKeyFrameIndex(animationComponent.animationTimer)

    val canMove : Boolean
        get() = moveComponent.aiCanMove

    val isMoving : Boolean
        get() = moveComponent.cos != 0f

    val animationType : AnimationType
        get() = animationComponent.animationType


    fun startRangeAttack(){
        rangeAttackComponent!!.run {
            doAttack = true
            startAttack()
        }
    }


    fun scaleImage(scale : Float){
        imageComponent.image.setSize(imageComponent.image.width*scale,imageComponent.image.height*scale)
    }

    fun startMeleeAttack(){
        val playerEntity = aiComponent.nearbyEntities.firstOrNull()
        playerEntity?.let {
            val diffX = physicComps[it].body.position.x - physicComponent.body.position.x
            turnTowardsThePlayer(diffX)
        }
        meleeAttackComponent!!.doAttack = true
        meleeAttackComponent.startAttack()
    }

    fun remove(enabled: Boolean){
        deadComps[entity].remove = enabled
    }

    fun animation(animationType: AnimationType, playMode : PlayMode = PlayMode.LOOP, frameDuration : Float = DEFAULT_FRAME_DURATION){
        animationComponent.nextAnimation(animationType, playMode, frameDuration)
    }

    fun root(enabled:Boolean){
        moveComponent.root = enabled
    }

    private fun turnTowardsThePlayer(diffX : Float){
        imageComponent.image.flipX = diffX > 0f
    }

    fun showDialog(dialogType: DialogType){
        val random = (0f..1f).random()
        if (random <= 0.2f){
            dialogComponent.showDialog = true
            dialogComponent.dialogType = dialogType
        }
    }

    fun setRangeAttackImpulse(){
        val playerEntity = aiComponent.nearbyEntities.firstOrNull()
        if (playerEntity != null){
            val diffX = physicComps[playerEntity].body.position.x - physicComponent.body.position.x
            val diffY = physicComps[playerEntity].body.position.y - physicComponent.body.position.y
            if (entityModel != EntityModel.PIG_LIGHT) turnTowardsThePlayer(diffX)
            rangeAttackComponent!!.attackImpulse.set(
                (diffX * 100f).coerceIn(-300f,if (entityModel != EntityModel.PIG_LIGHT) 300f else {if (imageComponent.image.flipX) 300f else -300f} ),
                (diffY * 100f).coerceAtLeast(-50f),
            )
        }
    }

    fun moveTo(targetPosition : Vector2){
        moveComponent.cos = targetPosition.x.compareTo(position.x).toFloat()
    }

    fun inRange(targetPosition: Vector2) :Boolean {
        val diff = distance(targetPosition,physicComponent.body.position)
        return aiComponent.nearbyEntities.isNotEmpty() || diff <= 1f
    }

    fun inMeleeRange() : Boolean {
        val playerPhysics = physicComps[aiComponent.nearbyEntities.first()]
        val playerPos = playerPhysics.body.position + playerPhysics.bodyOffset
        val position = position + physicComponent.bodyOffset
        val distance = distance(playerPos,position)
        return distance <= meleeAttackComponent!!.attackRange
    }

    fun goInMeleeRange() {
        val playerPos = physicComps[aiComponent.nearbyEntities.first()].body.position
        moveTo(playerPos)
    }

    private fun distance(vector1: Vector2, vector2: Vector2): Float {
        val deltaX = vector2.x - vector1.x
        val deltaY = vector2.y - vector1.y
        return sqrt(deltaX.pow(2) + deltaY.pow(2))
    }

    fun jump(){
        moveComponent.sin = 1f
    }
    val root : Boolean
        get() = moveComponent.root

    fun cantMove(): Boolean{
        val cantMove : Boolean = distance(physicComponent.body.position,previousPosition) <= 0.001f
        previousPosition.set(physicComponent.body.position)
        return cantMove && physicComponent.body.linearVelocity.x < 0.001f
    }

    fun startCannonAnimation() {
        world.family(allOf = arrayOf(AnimationComponent::class)).forEach {
            if (animComps[it].entityModel == EntityModel.CANNON){
                animComps[it].nextAnimation(AnimationType.SHOOT,PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 2f)
            }
        }
    }

}
