package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.system.AttackSystem.Companion.ATTACK_POLYLINE
import com.fatih.kingsofpigs.ecs.system.AttackSystem.Companion.ATTACK_RECT
import com.github.quillraven.fleks.IntervalSystem
import ktx.graphics.use


class DebugSystem(
    private val box2dWorld : World,
    gameStage : Stage
) : IntervalSystem(enabled = true) {

    private val gameCamera = gameStage.camera as OrthographicCamera
    private var debugRenderer : Box2DDebugRenderer? = null
    private var shapeRenderer : ShapeRenderer? = null

    init {
        if (enabled){
            debugRenderer = Box2DDebugRenderer()
            shapeRenderer = ShapeRenderer().apply { setColor(Color.RED) }
        }
    }

    override fun onTick() {
        debugRenderer?.render(box2dWorld,gameCamera.combined)
        shapeRenderer?.use(ShapeRenderer.ShapeType.Line,gameCamera){
           it.rect(ATTACK_RECT.x, ATTACK_RECT.y, ATTACK_RECT.width, ATTACK_RECT.height)
           /* if (ATTACK_POLYLINE.vertices.isNotEmpty()){
                it.polyline(ATTACK_POLYLINE.vertices)
            } */
        }
    }
}
