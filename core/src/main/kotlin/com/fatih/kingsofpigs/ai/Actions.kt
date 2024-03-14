package com.fatih.kingsofpigs.ai

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute
import com.badlogic.gdx.ai.utils.random.FloatDistribution
import com.badlogic.gdx.graphics.g2d.Animation
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationType

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
    override fun start() {
        //println("start idle ${entity.entity.id}")
    }
    override fun execute(): Status {
        //println("execute idle  ${entity.entity.id}")
        if (status != Status.RUNNING){
            currentDuration = duration?.nextFloat()?:2f
            entity.animation(AnimationType.IDLE)
            return Status.RUNNING
        }
        if (entity.isEnemyNearby){
            return Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}

class Jump : Actions(){
    override fun start() {
        //println("start jump ${entity.entity.id}")
    }

    override fun execute(): Status {
        //println("execute jump  ${entity.entity.id}")
        if (status != Status.RUNNING){
            return Status.RUNNING
        }
        if (!entity.isJumping){
            Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}
class Fall : Actions(){
    override fun start() {
        //println("start fall ${entity.entity.id}")
    }
    override fun execute(): Status {
        // println("execute fall  ${entity.entity.id}")
        if (status != Status.RUNNING){
            return Status.RUNNING
        }
        if (!entity.isFalling){
            return Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}

class ThrowBox : Actions(){

    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.animation(AnimationType.THROWING_BOX,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION *2f )
            entity.startAttack()
            return Status.RUNNING
        }
        entity.createAttackBody = entity.animKeyFrame == 3
        if (entity.animationDone){
            return Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}
class ThrowBomb : Actions(){
    override fun execute(): Status {
        if (status != Status.RUNNING){
            entity.animation(AnimationType.THROWING_BOMB,Animation.PlayMode.NORMAL, DEFAULT_FRAME_DURATION *3f )
            entity.startAttack()
            return Status.RUNNING
        }
        entity.createAttackBody = entity.animKeyFrame == 3
        if (entity.animationDone){
            return Status.SUCCEEDED
        }
        return Status.RUNNING
    }
}

class MeleeAttack : Actions(){
    override fun execute(): Status {
        if (status != Status.RUNNING){

            return Status.RUNNING
        }
        return Status.RUNNING
    }
}


