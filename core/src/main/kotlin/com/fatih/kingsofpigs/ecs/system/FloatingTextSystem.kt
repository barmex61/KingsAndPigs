package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.FloatingTextComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.Qualifier
import ktx.math.vec2

@AllOf([FloatingTextComponent::class])
class FloatingTextSystem (
    private val floatingTextComps : ComponentMapper<FloatingTextComponent>,
    @Qualifier("uiStage") private val uiStage :Stage,
    private val gameStage : Stage
): IteratingSystem() {

    private val uiPosition = vec2()

    override fun onTickEntity(entity: Entity) {
        val floatingTextComponent = floatingTextComps[entity]
        floatingTextComponent.run {
            if (remove) world.remove(entity)
            uiPosition.set(startPosition)
            gameStage.viewport.project(uiPosition)
            uiStage.viewport.unproject(uiPosition)
            startPosition.set(
                MathUtils.lerp(startPosition.x,targetPosition.x,deltaTime),
                MathUtils.lerp(startPosition.y,targetPosition.y,deltaTime),
            )
            label.setPosition(uiPosition.x,uiStage.viewport.worldHeight - uiPosition.y)
        }
    }

}
