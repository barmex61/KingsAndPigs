package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.github.quillraven.fleks.IntervalSystem

class PortalSystem (
    private val gameStage : Stage
): IntervalSystem() {

    private val tmxMapLoader = TmxMapLoader()

    fun changeMap(mapPath : String){
        gameStage.fireEvent(MapChangeEvent(tmxMapLoader.load(mapPath)))
    }

    override fun onTick() {

    }
}
