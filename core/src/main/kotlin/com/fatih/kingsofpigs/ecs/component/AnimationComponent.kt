package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

enum class AnimationType{
    IDLE,READY,LOOKING_OUT,HIT,BOMB_OFF,RUN,SHOOT,PREPARE,DEAD,ATTACK,FALL,JUMP,DOOR_OUT,DOOR_IN,OPENING,CLOSING,THROWING_BOX,PICKING_BOX,THROWING_BOMB;
    val animationName = this.name.lowercase()
}

class AnimationComponent (
    var entityModel: EntityModel = EntityModel.UNDEFINED,
    var nextAnimation : String = EMPTY_ANIMATION,
    var playMode: PlayMode = PlayMode.LOOP,
    var frameDuration : Float = DEFAULT_FRAME_DURATION,
    var animationTimer : Float = 0f,
    var animationType: AnimationType = AnimationType.IDLE,
    var durationScaling : Float = 1f
) {

    lateinit var animation : Animation<TextureRegionDrawable>

    val isAnimationDone : Boolean
        get() = animation.isAnimationFinished(animationTimer)


    fun nextAnimation(animationType:AnimationType,playMode: PlayMode = PlayMode.LOOP,frameDuration: Float = DEFAULT_FRAME_DURATION){

        this.animationType = animationType
        this.frameDuration = frameDuration * durationScaling
        this.playMode = playMode
        this.animationTimer = 0f
        nextAnimation = "${entityModel.entityName}/${animationType.animationName}"
    }

    companion object{
        const val DEFAULT_FRAME_DURATION = 0.07f
        const val EMPTY_ANIMATION = ""
    }
}
