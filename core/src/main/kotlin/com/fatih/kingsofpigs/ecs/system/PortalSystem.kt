package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent.Companion.playerHp
import com.fatih.kingsofpigs.ecs.component.LifeComponent.Companion.playerLife
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.ecs.component.SpawnConfig
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.IntervalSystem
import ktx.assets.disposeSafely
import ktx.math.vec2
import kotlin.experimental.or

class PortalSystem (
    private val gameStage : Stage,
    private val lifeComps : ComponentMapper<LifeComponent>
): IntervalSystem() {

    private val tmxMapLoader = TmxMapLoader()
    private var currentMap : TiledMap? = null
    var changeMap : Boolean = false
    var portalPath : String = ""

    private fun changeMap(mapPath : String){
        val playerEntity = world.family(anyOf = arrayOf(PlayerComponent::class)).firstOrNull()
        playerEntity?.let {
            playerLife = lifeComps[it].currentLife
            playerHp = lifeComps[it].currentHp
        }
        world.removeAll()
        currentMap?.disposeSafely()
        currentMap = tmxMapLoader.load(mapPath)
        gameStage.fireEvent(MapChangeEvent(currentMap!!))
    }

    override fun onTick() {
        if (changeMap){
            changeMap(portalPath)
            changeMap = false
        }
    }

}
