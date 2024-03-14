package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.actor.FlipImage
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity

class ImageComponent {

    lateinit var image : FlipImage
    var entityModel = EntityModel.UNDEFINED

    companion object{
        var playerFlipX : Boolean = false
        class ImageComponentListener(private val gameStage : Stage) : ComponentListener<ImageComponent>{
            override fun onComponentAdded(entity: Entity, component: ImageComponent) {
                gameStage.addActor(component.image)
            }

            override fun onComponentRemoved(entity: Entity, component: ImageComponent) {
                gameStage.root.removeActor(component.image)
            }
        }
    }
}
