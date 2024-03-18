package com.fatih.kingsofpigs.ai

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute
import com.badlogic.gdx.ai.utils.random.FloatDistribution
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.EntityModel
import ktx.math.random
import ktx.math.vec2

abstract class Actions : LeafTask<PigEntity>(){

    val entity : PigEntity
        get() = `object` as PigEntity

    override fun copyTo(task: Task<PigEntity>): Task<PigEntity> = task

}


class Idle(
    @JvmField
    @TaskAttribute(name = "duration")
    val duration : FloatDistribution? = null
) : Actions(){
    private var currentDuration : Float = duration?.nextFloat()?:2f

    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.root(true)
            currentDuration = duration?.nextFloat()?:2f
            entity.animation(AnimationType.IDLE)
            return Status.RUNNING
        }
        currentDuration -= GdxAI.getTimepiece().deltaTime
        if (entity.isEnemyNearby && entity.canAttack){
            entity.root(false)
            return Status.SUCCEEDED
        }
        if (currentDuration <= 0f){
            entity.root(false)
            return Status.SUCCEEDED
        }
        if (entity.isDead){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class Jump : Actions(){
    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.animation(AnimationType.JUMP)
            return Status.RUNNING
        }
        if (!entity.isJumping){
            return Status.FAILED
        }
        if (entity.isDead){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}
class Fall : Actions(){

    override fun execute(): Status {

        if (status != Status.RUNNING){
            entity.animation(AnimationType.FALL)
            return Status.RUNNING
        }
        if (!entity.isFalling){
            return Status.FAILED
        }
        if (entity.isDead){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class ThrowBox : Actions(){
    override fun execute(): Status {
        if (status != Status.RUNNING ){
            entity.setRangeAttackImpulse()
            entity.root(true)
            entity.animation(AnimationType.THROWING_BOX,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION *2f )
            return Status.RUNNING
        }
        if (entity.animKeyFrame == 3)  entity.startRangeAttack()
        if (entity.animationDone){
            entity.root(false)
            return Status.SUCCEEDED
        }
        if (!entity.isEnemyNearby){
            entity.root(false)
            return Status.SUCCEEDED
        }
        if (entity.isDead){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}
class ThrowBomb : Actions(){
    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.root(true)
            entity.setRangeAttackImpulse()
            entity.animation(AnimationType.THROWING_BOMB,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION *3f )
            return Status.RUNNING
        }
        if (entity.animKeyFrame == 3) entity.startRangeAttack()
        if (entity.animationDone){
            entity.root(false)
            return Status.SUCCEEDED
        }
        if (!entity.isEnemyNearby){
            entity.root(false)
            return Status.SUCCEEDED
        }
        if (entity.isDead){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class Focus : Actions(){
    private var jumpTimer = 0f
    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.root(false)
            jumpTimer = 0f
            return Status.RUNNING
        }
        if (!entity.isEnemyNearby){
            return Status.FAILED
        }
        if (!entity.inMeleeRange()){
            entity.goInMeleeRange()
            if (entity.animationType != AnimationType.RUN && entity.isMoving){
                entity.animation(AnimationType.RUN, frameDuration = DEFAULT_FRAME_DURATION *2f)
            }
        }else{
            return Status.SUCCEEDED
        }
        jumpTimer += GdxAI.getTimepiece().deltaTime
        if (entity.cantMove() && jumpTimer >= 1f){
            jumpTimer = 0f
            entity.jump()
        }
        if (entity.isDead){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class MeleeAttack : Actions(){
    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.root(true)
            entity.animation(AnimationType.ATTACK, playMode = PlayMode.NORMAL, frameDuration = DEFAULT_FRAME_DURATION *2f)
            entity.startMeleeAttack()
            return Status.RUNNING
        }
        if (entity.animationDone){
            entity.animation(AnimationType.IDLE)
            return Status.SUCCEEDED
        }
        if (entity.isDead){
            return Status.FAILED
        }
        return Status.RUNNING
    }
}

class Wander : Actions(){
    private val spawnPosition = vec2()
    private val targetPosition = vec2()
    private var wanderTimer = 1.5f
    private var jumpTimer = 0f
    override fun execute(): Status {
        if (status != Status.RUNNING){
            jumpTimer = 0f
            wanderTimer = 1.5f
            if (spawnPosition.isZero){
                spawnPosition.set(entity.position)
            }
            targetPosition.set(
                spawnPosition.x + (-entity.aiMoveRadius..entity.aiMoveRadius).random(),
                spawnPosition.y + (-entity.aiMoveRadius..entity.aiMoveRadius).random(),
            )
            entity.animation(AnimationType.RUN, frameDuration = DEFAULT_FRAME_DURATION *2f)
            entity.moveTo(targetPosition)
            return Status.RUNNING
        }
        jumpTimer += GdxAI.getTimepiece().deltaTime
        if (entity.inRange(targetPosition)){
            return Status.SUCCEEDED
        }
        if (entity.cantMove() && jumpTimer >= 0.5f){
            jumpTimer = 0f
            entity.jump()
        }
        if (wanderTimer <= 0f){
            wanderTimer = 1.5f
            return Status.SUCCEEDED
        }
        if (entity.isDead){
            return Status.FAILED
        }
        wanderTimer -= GdxAI.getTimepiece().deltaTime
        return Status.RUNNING
    }
}

class FireCannon : Actions(){
    override fun execute(): Status {

        if (status != Status.RUNNING){
            entity.setRangeAttackImpulse()
            entity.animation(AnimationType.PREPARE, playMode = PlayMode.NORMAL, frameDuration = DEFAULT_FRAME_DURATION * 3f)
            return Status.RUNNING
        }

        if (entity.animationDone && entity.animationType == AnimationType.PREPARE){
            entity.animation(AnimationType.ATTACK,PlayMode.NORMAL, DEFAULT_FRAME_DURATION *3f)
        }
        if (entity.animationDone && entity.animationType == AnimationType.ATTACK){
            entity.startRangeAttack()
            entity.startCannonAnimation()
            return Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}

class Dead : Actions(){
    override fun execute(): Status {

        if (status != Status.RUNNING){
            if (entity.entityModel != EntityModel.KING_PIG && entity.entityModel != EntityModel.PIG_BOX && entity.entityModel != EntityModel.PIG){
                entity.scaleImage(1.15f)
            }
            entity.root(true)
            entity.animation(AnimationType.DEAD,PlayMode.NORMAL, DEFAULT_FRAME_DURATION*2f)
            return Status.RUNNING
        }
        if (entity.animationDone){
            entity.remove(true)
            return Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}



