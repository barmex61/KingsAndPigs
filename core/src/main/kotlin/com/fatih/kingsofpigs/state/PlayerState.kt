package com.fatih.kingsofpigs.state

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationType

enum class PlayerState : DefaultState{

    DOOR_OUT{
        override fun enter(entity: PlayerEntity) {
            entity.root(true)
            entity.animation(AnimationType.DOOR_OUT,PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 2f)
        }

        override fun update(entity: PlayerEntity) {
            super.update(entity)
            when{
                entity.isAnimationDone -> entity.changeState(IDLE)
            }
        }

        override fun exit(entity: PlayerEntity) {
            entity.root(false)
        }
    },
    IDLE{
        override fun enter(entity: PlayerEntity) {
            entity.animation(AnimationType.IDLE,PlayMode.LOOP, DEFAULT_FRAME_DURATION)
        }

        override fun update(entity: PlayerEntity) {
            super.update(entity)
            when{
                entity.wantsToAttack -> entity.changeState(ATTACK)
                entity.wantsToRun -> entity.changeState(RUN)
                entity.isFalling -> entity.changeState(FALL)
                entity.isJumping -> entity.changeState(JUMP)
            }
        }
    },

    RUN{
        override fun enter(entity: PlayerEntity) {
            entity.animation(AnimationType.RUN,PlayMode.LOOP, DEFAULT_FRAME_DURATION)
        }

        override fun update(entity: PlayerEntity) {
            super.update(entity)
            when{
                entity.wantsToAttack -> entity.changeState(ATTACK)
                !entity.wantsToRun -> entity.changeState(IDLE)
                entity.isFalling -> entity.changeState(FALL)
                entity.isJumping -> entity.changeState(JUMP)
            }
        }
    },
    ATTACK{
        override fun enter(entity: PlayerEntity) {
            entity.startAttack()
            entity.animation(AnimationType.ATTACK, PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 2f)
            entity.root(true)
        }

        override fun update(entity: PlayerEntity) {
            super.update(entity)
            when{
                !entity.doAttack -> entity.changePreviousState()
            }
        }

        override fun exit(entity: PlayerEntity) {
            entity.root(false)
        }
    },
    FALL{
        override fun enter(entity: PlayerEntity) {
            entity.animation(AnimationType.FALL, PlayMode.NORMAL, DEFAULT_FRAME_DURATION)
        }

        override fun update(entity: PlayerEntity) {
            super.update(entity)
            when{
                entity.wantsToAttack -> entity.changeState(ATTACK)
                !entity.isFalling -> entity.changeState(IDLE)
            }
        }
    },
    HIT{
        override fun enter(entity: PlayerEntity) {
            entity.animation(AnimationType.HIT, PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 2f)
        }

        override fun update(entity: PlayerEntity) {
            super.update(entity)
            if(entity.isAnimationDone) entity.changePreviousState()
        }
    },
    JUMP{
        override fun enter(entity: PlayerEntity) {
            entity.animation(AnimationType.JUMP, PlayMode.NORMAL, DEFAULT_FRAME_DURATION)
        }

        override fun update(entity: PlayerEntity) {
            super.update(entity)
            when{
                entity.wantsToAttack -> entity.changeState(ATTACK)
                !entity.isJumping -> entity.changeState(IDLE)
            }
        }
    },
    DEATH{
        override fun enter(entity: PlayerEntity) {
            entity.animation(AnimationType.DEAD, playMode = PlayMode.NORMAL, DEFAULT_FRAME_DURATION * 2f)
            entity.root(true)
        }

        override fun update(entity: PlayerEntity) {
            when{
                !entity.isDead  -> entity.changeState(RESURRECT)
            }
        }

    },
    RESURRECT{
        override fun enter(entity: PlayerEntity) {
            entity.animation(AnimationType.DEAD, playMode = PlayMode.REVERSED, DEFAULT_FRAME_DURATION * 2f)
        }

        override fun update(entity: PlayerEntity) {
            when{
                entity.isAnimationDone  -> entity.changeState(IDLE)
            }
        }

        override fun exit(entity: PlayerEntity) {
            entity.root(false)
        }
    }
}
