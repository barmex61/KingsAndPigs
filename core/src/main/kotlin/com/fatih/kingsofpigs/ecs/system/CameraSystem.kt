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
import ktx.tiled.height
import ktx.tiled.width

@AllOf([PlayerComponent::class])
class CameraSystem (
    gameStage : Stage,
    private val imageComps : ComponentMapper<ImageComponent>
) : IteratingSystem() , EventListener{

    private val gameCamera = gameStage.camera as OrthographicCamera
    private var maxWidth = 0f
    private var maxHeight = 0f

    override fun onTickEntity(entity: Entity) {

        val imageComponent = imageComps[entity]
        gameCamera.position.set(
            (imageComponent.image.x + imageComponent.image.width/2f).coerceIn(
                MathUtils.lerp(gameCamera.position.x,gameCamera.viewportWidth/2f,deltaTime ),
                MathUtils.lerp(gameCamera.position.x,maxWidth + gameCamera.viewportWidth/2f,deltaTime ),
            ),
            (imageComponent.image.y + imageComponent.image.height/2f).coerceIn(
               MathUtils.lerp(gameCamera.position.y,gameCamera.viewportHeight/2f,deltaTime *2F),
                MathUtils.lerp(gameCamera.position.y,maxHeight+gameCamera.viewportHeight/2f,deltaTime*2f),
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
