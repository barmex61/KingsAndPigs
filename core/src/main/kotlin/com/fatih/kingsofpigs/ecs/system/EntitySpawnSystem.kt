package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.fatih.kingsofpigs.KingOfPigs.Companion.UNIT_SCALE
import com.fatih.kingsofpigs.actor.FlipImage
import com.fatih.kingsofpigs.ecs.component.AiComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.AttackComponent
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent.Companion.DEFAULT_MAX_LIFE
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent.Companion.DEFAULT_MOVE_SPEED
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.createBody
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.ecs.component.SpawnComponent
import com.fatih.kingsofpigs.ecs.component.SpawnConfig
import com.fatih.kingsofpigs.ecs.component.StateComponent
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.app.gdxError
import ktx.math.vec2
import ktx.tiled.forEachLayer
import ktx.tiled.height
import ktx.tiled.shape
import ktx.tiled.width
import ktx.tiled.x
import ktx.tiled.y
import kotlin.experimental.or

@AllOf([SpawnComponent::class])
class EntitySpawnSystem (
    private val spawnComps : ComponentMapper<SpawnComponent>,
    private val box2dWorld : World,
    private val textureAtlas : TextureAtlas
): IteratingSystem() , EventListener {

    private val spawnConfigCache = mutableMapOf<EntityModel,SpawnConfig>()

    override fun onTickEntity(entity: Entity) {
        val spawnComponent = spawnComps[entity]
        val spawnConfig = getSpawnConfig(spawnComponent.entityModel).apply {
            size.set(spawnComponent.size)
            position.set(spawnComponent.position)
        }
        spawnConfig.run {
            world.entity {
                add<ImageComponent>{
                    this.entityModel = this@run.entityModel
                    image = FlipImage().apply {
                        setSize(size.x,size.y)
                        setPosition(position.x,position.y)
                        setScaling(Scaling.fill)
                        setZ = entityModel == EntityModel.DOOR
                    }
                }
                add<AnimationComponent>{
                    this.entityModel = this@run.entityModel
                    this.durationScaling = this@run.frameDurationScaling
                    nextAnimation(this@run.animationType,if (entityModel == EntityModel.DOOR) Animation.PlayMode.NORMAL else Animation.PlayMode.LOOP)
                }
                add<PhysicComponent>{
                    val shape = Rectangle(
                        position.x , position.y  ,size.x  , size.y
                    )
                    this.body = createBody(box2dWorld,shape,categoryBit,maskBit,bodyType,physicScaling,physicOffset, isPortal = isPortal, aiCircle = aiCircleRadius)
                    this.bodyOffset.set(physicOffset)
                }
                if (speedScaling != 0f){
                    add<MoveComponent>{
                        speed = DEFAULT_MOVE_SPEED * speedScaling
                    }
                }
                if (attackScaling != 0f){
                    add<AttackComponent>{
                        this.attackDamage = (AttackComponent.DEFAULT_ATTACK_DAMAGE * attackScaling).toInt()
                        this.critChance = this@run.critChance
                        this.attackRange = this@run.attackRange
                        this.critDamage = this@run.critDamage
                        this.isRangeAttack = this@run.isRangeAttack
                        this.attackFloatArray = this@run.attackFloatArray
                        this.attackFloatArrayMirror = this@run.attackFloatArrayMirror
                        this.entityModel = this@run.entityModel
                        this.isPlayer = entityModel == EntityModel.KING
                        this.attackBodyType = this@run.attackBodyType
                        this.attackBodyImage = if (this@run.attackBodyImageDrawable != null) Image(this@run.attackBodyImageDrawable).apply { isVisible = false }else null
                    }
                }
                if (lifeScaling != 0f){
                    add<LifeComponent>{
                        maxLife = DEFAULT_MAX_LIFE * lifeScaling
                        currentLife = maxLife
                        canResurrect = this@run.canResurrect
                        resurrectionTime = this@run.resurrectionTime
                        regeneration = this@run.regeneration
                    }
                }
                if (entityModel == EntityModel.KING){
                    add<PlayerComponent>()
                    add<StateComponent>()
                }
                if (aiTreePath.isNotEmpty()){
                    add<AiComponent>{
                        this.treePath = aiTreePath
                    }
                }
            }
        }
        world.remove(entity)
    }
    private fun getSpawnConfig(entityModel: EntityModel) : SpawnConfig {
        return spawnConfigCache.getOrPut(entityModel){
            when(entityModel){
                EntityModel.KING ->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 1f,
                        categoryBit = Constants.KING,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.PORTAL or Constants.ATTACK_OBJECT ,
                        bodyType = BodyType.DynamicBody,
                        attackBodyType = BodyType.StaticBody,
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
                        regeneration = 2f,
                        resurrectionTime = 4f,
                        canResurrect = true,
                    )
                }
                EntityModel.BOX ->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        categoryBit = Constants.ITEM,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING ,
                        bodyType = BodyType.StaticBody
                    )
                }
                EntityModel.BOMB ->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.BOMB_OFF,
                        categoryBit = Constants.ITEM,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING,
                        bodyType = BodyType.StaticBody,
                        physicScaling = vec2(0.25f,0.23f),
                        physicOffset = vec2(0f,-0.25f)
                    )
                }
                EntityModel.PIG_BOX->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 1f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        attackBodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.8f,0.85f),
                        physicOffset = vec2(0.1f,-0.1f),
                        attackFloatArray = floatArrayOf(-0.5f,0f,1.3f,1f),
                        attackFloatArrayMirror = floatArrayOf(-0.5f,0f,1.3f,1f),
                        attackScaling = 1.5f,
                        attackRange = 5f,
                        isRangeAttack = true,
                        critChance  = 30,
                        critDamage  = 2f,
                        lifeScaling = 1f,
                        regeneration = 1f,
                        aiCircleRadius = 7f,
                        aiTreePath = "ai/pig.tree",
                        attackBodyImageDrawable = TextureRegionDrawable(textureAtlas.findRegion("box/idle"))
                    )
                }
                EntityModel.PIG->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 1f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING  or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        attackBodyType = BodyType.StaticBody,
                        physicScaling = vec2(0.4f,0.6f),
                        physicOffset = vec2(0.18f,-0.35f),
                        attackFloatArray = floatArrayOf(-0.6f , -1.1f , 0.7f , -1.55f , 1.8f, - 1.35f, 2.6f , - 0.6f, 2.63f , 0.4f, 1.8f, 1.3f, -0.6f , -1.1f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -1.1f , -0.7f , -1.55f , -1.8f, - 1.35f, -2.6f , - 0.6f, -2.63f , 0.4f, -1.8f, 1.3f, 0.6f , -1.1f),
                        attackScaling = 1f,
                        attackRange = 1f,
                        isRangeAttack = false,
                        critChance  = 10,
                        critDamage  = 1.5f,
                        lifeScaling = 1f,
                        regeneration = 1f,
                        aiCircleRadius = 4f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.PIG_BOMB->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 1f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        attackBodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.55f,0.6f),
                        physicOffset = vec2(-0.2f,-0.3f),
                        attackFloatArray = floatArrayOf(-0.5f,0f,1.3f,1f),
                        attackBodyImageDrawable = TextureRegionDrawable(textureAtlas.findRegion("bomb/bomb_on")),
                        attackFloatArrayMirror = floatArrayOf(-0.5f,0f,1.3f,1f),
                        attackScaling = 2f,
                        attackRange = 4f,
                        isRangeAttack = true,
                        critChance  = 30,
                        critDamage  = 2f,
                        lifeScaling = 1f,
                        regeneration = 1f,
                        aiCircleRadius = 5f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.PIG_BOX_HIDE->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.LOOKING_OUT,
                        speedScaling = 1f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.8f,0.85f),
                        physicOffset = vec2(0f,-0.1f),
                        attackFloatArray = floatArrayOf(- 0.6f , -1.1f , 0.7f , -1.55f , 1.8f, - 1.35f, 2.6f , - 0.6f, 2.63f , 0.4f, 1.8f, 1.3f, -0.6f , -1.1f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -1.1f , -0.7f , -1.55f , -1.8f, - 1.35f, -2.6f , - 0.6f, -2.63f , 0.4f, -1.8f, 1.3f, 0.6f , -1.1f),
                        lifeScaling = 1f,
                        regeneration = 1f,
                        attackScaling = 1f,
                        frameDurationScaling = 3f,
                        aiCircleRadius = 4f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.CANNON->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        categoryBit = Constants.ITEM,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING,
                        bodyType = BodyType.StaticBody,
                        physicScaling = vec2(0.5f,0.65f),
                        physicOffset = vec2(0.4f,-0.1f),
                        attackScaling = 2f,
                        attackRange = 7f,
                        isRangeAttack = true,
                        critChance  = 50,
                        critDamage  = 2f
                    )
                }
                EntityModel.PIG_LIGHT->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.LIGHT_READY,
                        speedScaling = 1f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING  or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.5f,0.85f),
                        physicOffset = vec2(0.1f,-0.1f),
                        attackFloatArray = floatArrayOf(- 0.6f , -1.1f , 0.7f , -1.55f , 1.8f, - 1.35f, 2.6f , - 0.6f, 2.63f , 0.4f, 1.8f, 1.3f, -0.6f , -1.1f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -1.1f , -0.7f , -1.55f , -1.8f, - 1.35f, -2.6f , - 0.6f, -2.63f , 0.4f, -1.8f, 1.3f, 0.6f , -1.1f),
                        attackScaling = 1f,
                        attackRange = 1f,
                        isRangeAttack = false,
                        critChance  = 20,
                        critDamage  = 2f,
                        lifeScaling = 1f,
                        regeneration = 1f,
                        aiCircleRadius = 4f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.KING_PIG->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 1f,
                        categoryBit = Constants.ENEMY,
                        attackBodyType = BodyType.StaticBody,
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.4f,0.7f),
                        physicOffset = vec2(0.05f,-0.25f),
                        attackFloatArray = floatArrayOf(- 0.6f , -1.1f , 0.7f , -1.55f , 1.8f, - 1.35f, 2.6f , - 0.6f, 2.63f , 0.4f, 1.8f, 1.3f, -0.6f , -1.1f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -1.1f , -0.7f , -1.55f , -1.8f, - 1.35f, -2.6f , - 0.6f, -2.63f , 0.4f, -1.8f, 1.3f, 0.6f , -1.1f),
                        attackScaling = 2f,
                        attackRange = 1.5f,
                        isRangeAttack = false,
                        critChance  = 50,
                        critDamage  = 2f,
                        lifeScaling = 2f,
                        regeneration = 2f,
                        aiCircleRadius = 4f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.DOOR->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.OPENING,
                        speedScaling = 0f,
                        categoryBit = Constants.PORTAL,
                        maskBit = Constants.KING,
                        bodyType = BodyType.StaticBody,
                        isPortal = true,
                        frameDurationScaling = 2f
                    )
                }
                else -> gdxError("Error")
            }
        }
    }

    override fun handle(event: Event?): Boolean {
        when(event){
            is MapChangeEvent ->{
                event.map.forEachLayer<MapLayer> {mapLayer->
                    mapLayer.objects.forEach {mapObject->
                        if (mapLayer.name != RenderSystem.MapLayerType.COLLISION.layerName){
                            val name = mapObject.name ?: gdxError("There is no name for $mapObject")
                            world.entity {
                                add<SpawnComponent>{
                                    entityModel = EntityModel.valueOf(name.uppercase())
                                    position.set(mapObject.x * UNIT_SCALE , mapObject.y * UNIT_SCALE)
                                    size.set(mapObject.width * UNIT_SCALE , mapObject.height * UNIT_SCALE)
                                }
                            }
                        }else{
                            val isPlatform = mapObject.name == "platform"
                            createBody(box2dWorld,
                                (mapObject.shape as Rectangle).apply {
                                   setSize(this.width * UNIT_SCALE , this.height * UNIT_SCALE)
                                   setPosition(x * UNIT_SCALE, y * UNIT_SCALE)
                                },
                                Constants.OBJECT,
                                Constants.KING or Constants.PORTAL or Constants.ITEM or Constants.OBJECT or Constants.ENEMY or Constants.ATTACK_OBJECT,BodyType.StaticBody ,
                                vec2(1f,1f),
                                vec2(0f,0f),
                                isPortal = false,
                                isCollision = true,
                                isPlatform = isPlatform
                            )
                        }
                    }
                }

                return true
            }
        }
        return false
    }

}
