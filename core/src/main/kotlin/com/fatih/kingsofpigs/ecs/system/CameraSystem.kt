package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.random
import ktx.tiled.height
import ktx.tiled.totalHeight
import ktx.tiled.width

@AllOf([PlayerComponent::class])
class CameraSystem (
    gameStage : Stage,
    private val imageComps : ComponentMapper<ImageComponent>
) : IteratingSystem() , EventListener{

    private val gameCamera = gameStage.camera as OrthographicCamera
    private var maxWidth = 0f
    private var maxHeight = 0f
    private var shakeTimer = 0.3f
    var shakeCamera = false

    override fun onTickEntity(entity: Entity) {

        val imageComponent = imageComps[entity]
        if (shakeCamera){
            shakeTimer -= deltaTime
            if (shakeTimer <= 0f){
                shakeTimer = 0.3f
                shakeCamera = false
            }
        }
        val posX = imageComponent.image.x + imageComponent.image.width/2f + if(shakeCamera) (-3f..3f).random() else 0f
        val posY = imageComponent.image.y + imageComponent.image.height/2f +if (shakeCamera) (-3f..3f).random() else 0f
        gameCamera.position.set(
            posX.coerceIn(
                MathUtils.lerp(gameCamera.position.x,gameCamera.viewportWidth/2f,deltaTime ),
                MathUtils.lerp(gameCamera.position.x,posX + 6f + gameCamera.viewportWidth/2f,deltaTime ),
            ),
            posY.coerceIn(
               MathUtils.lerp(gameCamera.position.y,gameCamera.viewportHeight/2f,deltaTime *2F),
                MathUtils.lerp(gameCamera.position.y,posY + 6f + gameCamera.viewportHeight/2f,deltaTime*2f),
            ),
            gameCamera.position.z
        )
        gameCamera.update()
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is MapChangeEvent ->{
                maxWidth = event.map.width.toFloat()
                maxHeight = event.map.height.toFloat()
                return true
            }
        }
        return false
    }
}
