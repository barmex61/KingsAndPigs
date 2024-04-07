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
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val box2dWorld : World
): IntervalSystem() {

    private val tmxMapLoader = TmxMapLoader()
    private var currentMap : TiledMap? = null
    var changeMap : Boolean = false
    var portalPath : String = ""

    private fun changeMap(mapPath : String){
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

    companion object{
        val playerConfig = SpawnConfig(
        entityModel = EntityModel.KING,
        animationType = AnimationType.IDLE,
        speedScaling = 1f,
        categoryBit = Constants.KING,
        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.PORTAL or Constants.ATTACK_OBJECT or Constants.DESTROYABLE,
        bodyType = BodyDef.BodyType.DynamicBody,
        attackBodyType = BodyDef.BodyType.StaticBody,
        physicScaling = vec2(0.225f,0.45f),
        physicOffset = vec2(-0.45f,-0.1f),
        attackScaling = 2f,
        attackRange = 1.1f,
        isRangeAttack = false,
        critChance  = 50,
        critDamage  = 2f,
        attackFloatArray = floatArrayOf(- 0.6f , -1.1f , 0.7f , -1.55f , 1.8f, - 1.35f, 2.6f , - 0.6f, 2.63f , 0.4f, 1.8f, 1.3f, -0.6f , -1.1f),
        attackFloatArrayMirror = floatArrayOf(0.6f , -1.1f , -0.7f , -1.55f , -1.8f, - 1.35f, -2.6f , - 0.6f, -2.63f , 0.4f, -1.8f, 1.3f, 0.6f , -1.1f),
        lifeScaling = 2f,
        resurrectionTime = 4f,
        canResurrect = true,
        )
    }
}
