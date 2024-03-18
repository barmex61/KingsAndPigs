package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.math.vec2

enum class EntityModel {
    UNDEFINED,PIG,KING,KING_PIG,PIG_BOX,PIG_BOMB,PIG_BOX_HIDE,PIG_LIGHT,BOX,BOMB,CANNON,DOOR;
    val entityName = this.name.lowercase()
}

class SpawnConfig(
    val size : Vector2 = vec2(),
    val position: Vector2 = vec2(),
    val entityModel: EntityModel,
    val animationType : AnimationType,
    val speedScaling : Float = 0f,
    val categoryBit : Short,
    val maskBit : Short,
    val bodyType: BodyType,
    val isPortal : Boolean = false,
    val physicOffset : Vector2 = vec2(),
    val physicScaling : Vector2 = vec2(1f,1f),
    val attackScaling : Float = 0f,
    val attackRange : Float = 1f,
    val isRangeAttack : Boolean = false,
    val critChance : Int = 20,
    val critDamage : Float = 1f,
    var attackFloatArray : FloatArray = floatArrayOf(),
    var attackFloatArrayMirror: FloatArray = floatArrayOf(),
    val attackBodyType: BodyType = BodyType.StaticBody,
    val lifeScaling : Float = 0f,
    val regeneration : Float = 1f,
    val resurrectionTime : Float = 0f,
    val canResurrect : Boolean = false,
    val aiCircleRadius : Float = 0f,
    val aiTreePath : String = "",
    val frameDurationScaling : Float = 1f,
    val attackImageScaling : Vector2 = vec2(1f,1f),
    val attackImageOffset : Vector2 = vec2(),
    val aiMoveRadius : Float = 2f
)

data class SpawnComponent (
    val size : Vector2 = vec2(),
    val position : Vector2 = vec2(),
    var entityModel : EntityModel = EntityModel.UNDEFINED
)

