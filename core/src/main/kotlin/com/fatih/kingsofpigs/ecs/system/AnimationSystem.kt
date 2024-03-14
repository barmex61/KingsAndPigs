package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.DEFAULT_FRAME_DURATION
import com.fatih.kingsofpigs.ecs.component.AnimationComponent.Companion.EMPTY_ANIMATION
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.collections.map

@AllOf([AnimationComponent::class])
class AnimationSystem(
    private val animComps : ComponentMapper<AnimationComponent>,
    private val textureAtlas : TextureAtlas,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>
) : IteratingSystem() {

    private val animationCache = mutableMapOf<String,Animation<TextureRegionDrawable>>()

    override fun onTickEntity(entity: Entity) {
        val animationComponent = animComps[entity]
        val imageComponent = imageComps[entity]
        animationComponent.run {
            if (nextAnimation != EMPTY_ANIMATION){
                animation = getAnimation(nextAnimation).apply {
                    this.frameDuration = this@run.frameDuration
                    this.playMode = this@run.playMode
                }
                animationTimer = 0f
                nextAnimation = EMPTY_ANIMATION
            }
            animationTimer += deltaTime
            imageComponent.image.drawable = animation.getKeyFrame(animationTimer)
            if (entityModel == EntityModel.DOOR && animationType == AnimationType.OPENING && isAnimationDone){
                nextAnimation(AnimationType.CLOSING,PlayMode.NORMAL, DEFAULT_FRAME_DURATION )
            }
        }
    }

    private fun getAnimation(animPath : String) : Animation<TextureRegionDrawable>{
        return animationCache.getOrPut(animPath){
            Animation(DEFAULT_FRAME_DURATION,textureAtlas.findRegions(animPath).map {
                TextureRegionDrawable(it)
            },PlayMode.LOOP)
        }
    }
}
