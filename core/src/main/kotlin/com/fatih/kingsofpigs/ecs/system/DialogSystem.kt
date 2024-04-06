package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.fatih.kingsofpigs.ecs.component.DialogComponent
import com.fatih.kingsofpigs.ecs.component.DialogType
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.plus
import ktx.math.random

@AllOf([DialogComponent::class])
class DialogSystem(
    private val dialogComps : ComponentMapper<DialogComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val textureAtlas: TextureAtlas
) : IteratingSystem(){

    private val dialogImageCache : MutableMap<String,TextureRegionDrawable> = mutableMapOf()

    override fun onTickEntity(entity: Entity) {
        dialogComps[entity].run {
            if (showDialog){
                dialogTimer -= deltaTime
                val physicComponent = physicComps[entity]
                dialogImage.apply {
                    setPosition(physicComponent.body.position.x - 0.8f ,physicComponent.body.position.y + 0.2f)
                    if (drawable == null){
                        drawable = getDialogDrawable(dialogType.atlasKey)
                    }
                }
                if (dialogTimer <= 0f){
                    dialogTimer = (1f..2f).random()
                    showDialog = false
                    dialogImage.drawable = null
                }
            }
        }
    }

    private fun getDialogDrawable(atlasKey : String): TextureRegionDrawable =
        dialogImageCache.getOrPut(atlasKey){
            TextureRegionDrawable(textureAtlas.findRegion(atlasKey))
        }
}
