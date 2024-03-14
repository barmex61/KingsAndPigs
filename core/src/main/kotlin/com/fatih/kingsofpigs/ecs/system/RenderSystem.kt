package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.KingOfPigs.Companion.UNIT_SCALE
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.Qualifier
import ktx.graphics.use
import ktx.tiled.forEachLayer

class RenderSystem (
    private val gameStage : Stage,
    @Qualifier("uiStage") private val uiStage: Stage
) : IntervalSystem() , EventListener {

    private val tiledMapTiledLayers = mutableListOf<TiledMapTileLayer>()
    private val gameCamera = gameStage.camera as OrthographicCamera
    private val mapRenderer = OrthogonalTiledMapRenderer(null,UNIT_SCALE,gameStage.batch)

    override fun onTick() {
        gameStage.act(deltaTime)
        uiStage.act(deltaTime)
        mapRenderer.setView(gameCamera)
        gameStage.batch.color = Color.WHITE
        gameStage.batch.use {
            tiledMapTiledLayers.forEach { tileLayer->
                mapRenderer.renderTileLayer(tileLayer)
            }
        }
        gameStage.draw()
        gameStage.batch.color = Color.WHITE
        uiStage.draw()
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is MapChangeEvent -> {
                tiledMapTiledLayers.clear()
                event.map.forEachLayer<TiledMapTileLayer> {tileLayer->
                    when(tileLayer.name){
                        MapLayerType.BACKGROUND.layerName , MapLayerType.FOREGROUND.layerName-> tiledMapTiledLayers.add(tileLayer)
                    }
                }
            }
        }
        return false
    }

    enum class MapLayerType{
        BACKGROUND,FOREGROUND,PORTALS,ENTITY,COLLISION;
        val layerName = this.name.lowercase()
    }
}
