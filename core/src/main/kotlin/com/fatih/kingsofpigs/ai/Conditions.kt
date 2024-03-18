package com.fatih.kingsofpigs.ai

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task

abstract class Conditions : LeafTask<PigEntity>(){
    val entity : PigEntity
        get() = `object` as PigEntity

    abstract fun condition() : Boolean

    override fun copyTo(task: Task<PigEntity>): Task<PigEntity> = task

    override fun execute(): Status {
        return if (condition()){
            Status.SUCCEEDED
        }else{
            Status.FAILED
        }
    }
}

class IsJumping : Conditions(){
    override fun condition() = entity.isJumping

}

class IsFalling : Conditions(){
    override fun condition(): Boolean = entity.isFalling
}

class IsMeleeAttack : Conditions(){
    override fun condition(): Boolean = entity.isMeleeAttack
}

class IsRangeAttack : Conditions(){
    override fun condition() : Boolean = entity.isRangeAttack
}

class IsEnemyNearby : Conditions(){
    override fun condition(): Boolean = entity.isEnemyNearby
}

class CanAttack : Conditions(){
    override fun condition(): Boolean = entity.canAttack
}

class IsPigBox : Conditions(){
    override fun condition(): Boolean = entity.isPigBox
}

class IsPigBomb : Conditions(){
    override fun condition() : Boolean = entity.isPigBomb
}

class CanMove : Conditions(){
    override fun condition(): Boolean = entity.canMove
}

class NotInRange : Conditions(){
    override fun condition(): Boolean = !entity.isEnemyNearby
}

class IsDead : Conditions(){
    override fun condition(): Boolean = entity.isDead
}

class IsPigLight : Conditions(){
    override fun condition() = entity.isPigLight
}
