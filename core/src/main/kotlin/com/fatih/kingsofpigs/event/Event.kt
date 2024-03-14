package com.fatih.kingsofpigs.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage

fun Stage.fireEvent(event : Event) = this.root.fire(event)

data class MapChangeEvent(val map : TiledMap) : Event()
class PlayerGetHitEvent : Event()
class PigGetHitEvent : Event()
