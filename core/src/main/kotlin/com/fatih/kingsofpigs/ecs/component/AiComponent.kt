package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser
import com.fatih.kingsofpigs.ai.PigEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

class AiComponent (
    val nearbyEntities : MutableSet<Entity> = mutableSetOf(),
    var treePath : String = ""
){
    lateinit var behaviorTree : BehaviorTree<PigEntity>

    companion object{
        class AiComponentListener(private val world: World) : ComponentListener<AiComponent>{
            private val behaviorTreeParser = BehaviorTreeParser<PigEntity>()
            override fun onComponentAdded(entity: Entity, component: AiComponent) {
                component.behaviorTree = behaviorTreeParser.parse(Gdx.files.internal(component.treePath), PigEntity(world, entity))
            }

            override fun onComponentRemoved(entity: Entity, component: AiComponent) {

            }
        }
    }
}
