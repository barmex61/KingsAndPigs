package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity

class DestroyableComponent(
    var destroy: Boolean = false,
    var destroyTimer : Float = 2f,
    var createBodies : Boolean = false,
    var images : MutableList<Image> = mutableListOf()
) {
    var items : MutableList<Item> = mutableListOf()
    var bodyList : MutableList<Body> = mutableListOf()
    companion object{
        class DestroyableListener(private val world:World,private val gameStage : Stage) : ComponentListener<DestroyableComponent>{
            override fun onComponentAdded(entity: Entity, component: DestroyableComponent) {

            }

            override fun onComponentRemoved(entity: Entity, component: DestroyableComponent) {
                if (component.bodyList.isNotEmpty()){
                    component.bodyList.forEach {
                        world.destroyBody(it)
                    }
                }
                component.images.forEach {
                    gameStage.root.removeActor(it)
                }
            }
        }
    }
}
