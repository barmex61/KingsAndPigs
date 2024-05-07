package com.fatih.kingsofpigs.ecs.system

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.fatih.kingsofpigs.KingOfPigs.Companion.UNIT_SCALE
import com.fatih.kingsofpigs.actor.FlipImage
import com.fatih.kingsofpigs.ecs.component.AiComponent
import com.fatih.kingsofpigs.ecs.component.AnimationComponent
import com.fatih.kingsofpigs.ecs.component.AnimationType
import com.fatih.kingsofpigs.ecs.component.DestroyableComponent
import com.fatih.kingsofpigs.ecs.component.DialogComponent
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.Item
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent.Companion.DEFAULT_MAX_HP
import com.fatih.kingsofpigs.ecs.component.LightComponent
import com.fatih.kingsofpigs.ecs.component.MeleeAttackComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent.Companion.DEFAULT_MOVE_SPEED
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.createBody
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.ecs.component.RangeAttackComponent
import com.fatih.kingsofpigs.ecs.component.SpawnComponent
import com.fatih.kingsofpigs.ecs.component.SpawnConfig
import com.fatih.kingsofpigs.ecs.component.StateComponent
import com.fatih.kingsofpigs.ecs.system.LightSystem.Companion.isLightsOn
import com.fatih.kingsofpigs.ecs.system.LightSystem.Companion.nightAmbientLight
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
import ktx.tiled.property
import ktx.tiled.shape
import ktx.tiled.width
import ktx.tiled.x
import ktx.tiled.y
import kotlin.experimental.or

@AllOf([SpawnComponent::class])
class EntitySpawnSystem (
    private val spawnComps : ComponentMapper<SpawnComponent>,
    private val box2dWorld : World,
    private val textureAtlas : TextureAtlas,
    private val rayHandler: RayHandler
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
                        this.flipX = spawnComponent.isFlipX
                    }
                }
                add<AnimationComponent>{
                    this.entityModel = this@run.entityModel
                    this.durationScaling = this@run.frameDurationScaling
                    nextAnimation(this@run.animationType,if (entityModel == EntityModel.DOOR) Animation.PlayMode.NORMAL else Animation.PlayMode.LOOP)
                }

                val physicComponent = add<PhysicComponent>{
                    val shape = Rectangle(
                        position.x , position.y  ,size.x  , size.y
                    )
                    this.body = createBody(box2dWorld,shape,categoryBit,maskBit,bodyType,physicScaling,physicOffset, isPortal = isPortal, aiCircle = aiCircleRadius)
                    this.bodyOffset.set(physicOffset)
                }

                if (speedScaling != 0f){
                    add<MoveComponent>{
                        speed = DEFAULT_MOVE_SPEED * speedScaling
                        aiMoveRadius = this@run.aiMoveRadius
                        aiCanMove = this@run.entityModel != EntityModel.PIG_LIGHT
                    }
                }
                if (entityModel == EntityModel.BOX){
                    add<DestroyableComponent>{
                        val itemList = listOf(Item.Diamond(),Item.Heart())
                        (0..(0..3).random()).forEach { _ ->
                            items.add(itemList[(0..1).random()])
                        }
                    }
                }
                if (attackScaling != 0f ){
                    if (!isRangeAttack){
                        add<MeleeAttackComponent>{
                            this.attackDamage = (MeleeAttackComponent.DEFAULT_ATTACK_DAMAGE * attackScaling).toInt()
                            this.critChance = this@run.critChance
                            this.attackRange = this@run.attackRange
                            this.critDamage = this@run.critDamage
                            this.attackFloatArray = this@run.attackFloatArray
                            this.attackFloatArrayMirror = this@run.attackFloatArrayMirror
                            this.entityModel = this@run.entityModel
                            this.isPlayer = entityModel == EntityModel.KING
                            this.destroyBodyTime = 0.15f
                        }
                    }else{
                        add<RangeAttackComponent>{
                            this.attackDamage = (MeleeAttackComponent.DEFAULT_ATTACK_DAMAGE * attackScaling).toInt()
                            this.critChance = this@run.critChance
                            this.attackRange = this@run.attackRange
                            this.critDamage = this@run.critDamage
                            this.attackFloatArray = this@run.attackFloatArray
                            this.attackFloatArrayMirror = this@run.attackFloatArrayMirror
                            if (this@run.entityModel == EntityModel.PIG_BOX){
                                this.image.drawable = TextureRegionDrawable(textureAtlas.findRegion("box/idle"))
                            }
                            this.entityModel = this@run.entityModel
                            this.imageScaling = this@run.attackImageScaling
                            this.attackImageOffset = this@run.attackImageOffset
                        }
                    }
                }

                if (lifeScaling != 0f){
                    if (entityModel != EntityModel.KING){
                        add<DialogComponent>()
                    }
                    add<LifeComponent>{
                        maxHp = DEFAULT_MAX_HP * lifeScaling
                        currentHp = maxHp
                        if (entityModel == EntityModel.KING){
                            currentHp = LifeComponent.playerHp
                            currentLife = LifeComponent.playerLife
                        }
                        canResurrect = this@run.canResurrect
                        resurrectionTime = this@run.resurrectionTime
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
                if (isLightsOn && entityModel == EntityModel.KING){
                    add<LightComponent>{
                        distance = 2f..6f
                        light = PointLight(rayHandler,64,LightComponent.lightColor,distance.endInclusive,0f,0f).apply {
                            attachToBody(physicComponent.body,physicComponent.bodyOffset.x,physicComponent.bodyOffset.y )
                        }
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
                        maskBit = Constants.ENEMY or Constants.ITEM or Constants.OBJECT or Constants.PORTAL or Constants.ATTACK_OBJECT or Constants.DESTROYABLE,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.225f,0.45f),
                        physicOffset = vec2(-0.45f,-0.1f),
                        attackScaling = 1.5f,
                        attackRange = 1.1f,
                        isRangeAttack = false,
                        critChance  = 35,
                        critDamage  = 2.5f,
                        attackFloatArray = floatArrayOf(- 0.6f , -1.1f , 0.7f , -1.55f , 1.8f, - 1.35f, 2.6f , - 0.6f, 2.63f , 0.4f, 1.8f, 1.3f, -0.6f , -1.1f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -1.1f , -0.7f , -1.55f , -1.8f, - 1.35f, -2.6f , - 0.6f, -2.63f , 0.4f, -1.8f, 1.3f, 0.6f , -1.1f),
                        lifeScaling = 1f,
                        resurrectionTime = 2.5f,
                        canResurrect = true,

                    )
                }
                EntityModel.BOX ->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        categoryBit = Constants.DESTROYABLE,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody
                    )
                }
                EntityModel.BOMB ->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.BOMB_OFF,
                        categoryBit = Constants.DESTROYABLE,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.25f,0.23f),
                        physicOffset = vec2(0f,-0.25f)
                    )
                }
                EntityModel.PIG_BOX->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 0.8f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.8f,0.85f),
                        physicOffset = vec2(0.1f,-0.1f),
                        attackFloatArray = floatArrayOf(-0.6f,0f,1.3f,1f),
                        attackFloatArrayMirror = floatArrayOf(-0.5f,0f,1.3f,1f),
                        attackScaling = 1.5f,
                        attackRange = 5f,
                        isRangeAttack = true,
                        critChance  = 33,
                        critDamage  = 1.5f,
                        lifeScaling = 1f,
                        aiCircleRadius = 5f,
                        aiTreePath = "ai/pig.tree",
                        aiMoveRadius = 4f,
                        )
                }
                EntityModel.PIG->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 0.8f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING  or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.4f,0.6f),
                        physicOffset = vec2(0.18f,-0.35f),
                        attackFloatArray = floatArrayOf(-0.6f , -0.3f , -1f , 0f , -1.1f, 0.4f, -1f , 0.8f, -0.7f , 1f,-0.6f , -0.3f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -0.3f , 1f , 0f , 1.1f, 0.4f, 1f , 0.8f, 0.7f , 1f,0.6f , -0.3f),
                        attackScaling = 1f,
                        attackRange = 1.5f,
                        isRangeAttack = false,
                        critChance  = 15,
                        critDamage  = 1.5f,
                        lifeScaling = 1f,
                        aiCircleRadius = 3f,
                        aiMoveRadius = 1.5f,
                        aiTreePath = "ai/pig.tree"
                        )
                }
                EntityModel.PIG_BOMB->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 0.8f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.55f,0.6f),
                        physicOffset = vec2(-0.2f,-0.3f),
                        attackFloatArray = floatArrayOf(-0.5f,0f,0.9f,0.9f),
                        attackFloatArrayMirror = floatArrayOf(-0.5f,0f,1.3f,1f),
                        attackScaling = 2f,
                        attackRange = 4f,
                        isRangeAttack = true,
                        critChance  = 25,
                        critDamage  = 1.7f,
                        lifeScaling = 1.2f,
                        aiCircleRadius = 4f,
                        attackImageScaling = vec2(3.8f,3.8f),
                        attackImageOffset = vec2(0f,0.2f),
                        aiTreePath = "ai/pig.tree",
                        aiMoveRadius = 1f
                        )
                }
                EntityModel.PIG_BOX_HIDE->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.LOOKING_OUT,
                        speedScaling = 0.8f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.8f,0.85f),
                        physicOffset = vec2(0f,-0.1f),
                        attackFloatArray = floatArrayOf(- 0.6f , -1.1f , 0.7f , -1.55f , 1.8f, - 1.35f, 2.6f , - 0.6f, 2.63f , 0.4f, 1.8f, 1.3f, -0.6f , -1.1f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -1.1f , -0.7f , -1.55f , -1.8f, - 1.35f, -2.6f , - 0.6f, -2.63f , 0.4f, -1.8f, 1.3f, 0.6f , -1.1f),
                        lifeScaling = 1f,
                        attackScaling = 1f,
                        frameDurationScaling = 3f,
                    )
                }
                EntityModel.CANNON->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        categoryBit = Constants.OBJECT,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING,
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
                        animationType = AnimationType.READY,
                        speedScaling = 0.8f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING  or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.StaticBody,
                        physicScaling = vec2(0.5f,0.85f),
                        physicOffset = vec2(0.1f,-0.1f),
                        attackFloatArray = floatArrayOf(-2.5f,0f,0.7f,0.7f),
                        attackFloatArrayMirror = floatArrayOf(-2.5f,0f,0.7f,0.7f),
                        attackScaling = 2f,
                        attackRange = 1f,
                        isRangeAttack = true,
                        attackImageScaling = vec2(4f,2.5f),
                        critChance  = 40,
                        critDamage  = 2f,
                        lifeScaling = 1f,
                        aiMoveRadius = 0f,
                        aiCircleRadius = 15f,
                        attackImageOffset = vec2(-0.45f,0.32f),
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.KING_PIG->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 0.8f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.4f,0.7f),
                        physicOffset = vec2(0.05f,-0.25f),
                        attackFloatArray = floatArrayOf(-0.6f , -0.3f , -1f , 0f , -1.1f, 0.4f, -1f , 0.8f, -0.7f , 1f,-0.6f , -0.3f),
                        attackFloatArrayMirror = floatArrayOf(0.6f , -0.3f , 1f , 0f , 1.1f, 0.4f, 1f , 0.8f, 0.7f , 1f,0.6f , -0.3f),
                        attackScaling = 1.3f,
                        attackRange = 1.8f,
                        isRangeAttack = false,
                        critChance  = 35,
                        critDamage  = 1.7f,
                        lifeScaling = 1.6f,
                        aiMoveRadius = 2.5f,
                        aiCircleRadius = 4f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.DEMON->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 0.65f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.15f,0.43f),
                        physicOffset = vec2(0f,-2.8f),
                        attackFloatArray = floatArrayOf(-7f , 0f , -6f , -3f ,0.6f,-1.5f,0.6f,3f),
                        attackFloatArrayMirror = floatArrayOf(7f , 0f , 6f , -3f ,-0.6f,-1.5f,-0.6f,3f),
                        attackScaling = 2.5f,
                        attackRange = 7f,
                        isRangeAttack = false,
                        critChance  = 40,
                        critDamage  = 3f,
                        lifeScaling = 4f,
                        frameDurationScaling = 0.8f,
                        aiMoveRadius = 20f,
                        aiCircleRadius = 20f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.GOLEM->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.IDLE,
                        speedScaling = 0.65f,
                        categoryBit = Constants.ENEMY,
                        maskBit = Constants.ENEMY or Constants.DESTROYABLE or Constants.OBJECT or Constants.KING or Constants.ATTACK_OBJECT,
                        bodyType = BodyType.DynamicBody,
                        physicScaling = vec2(0.25f,0.55f),
                        physicOffset = vec2(0.15f,-0.63f),
                        attackFloatArray = floatArrayOf(-7f , 0f , -6f , -3f ,0.6f,-1.5f,0.6f,3f),
                        attackFloatArrayMirror = floatArrayOf(7f , 0f , 6f , -3f ,-0.6f,-1.5f,-0.6f,3f),
                        attackScaling = 2f,
                        attackRange = 7f,
                        isRangeAttack = false,
                        critChance  = 40,
                        critDamage  = 3f,
                        lifeScaling = 5f,
                        frameDurationScaling = 0.8f,
                        aiMoveRadius = 20f,
                        aiCircleRadius = 20f,
                        aiTreePath = "ai/pig.tree"
                    )
                }
                EntityModel.DOOR->{
                    SpawnConfig(
                        entityModel = entityModel,
                        animationType = AnimationType.OPENING,
                        speedScaling = 0f,
                        categoryBit = Constants.OBJECT,
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
                    isLightsOn = event.map.property<Boolean>("hasLights")
                    if (isLightsOn){
                        nightAmbientLight = event.map.property<Color>("lightColor")
                    }
                    mapLayer.objects.forEach {mapObject->
                        if (mapLayer.name == RenderSystem.MapLayerType.ENTITY.layerName){
                            val name = mapObject.name ?: gdxError("There is no name for $mapObject")
                            val flipX = mapObject.properties.get("isFlipX",Boolean::class.java)?:false
                            world.entity {
                                add<SpawnComponent>{
                                    entityModel = EntityModel.valueOf(name.uppercase())
                                    position.set(mapObject.x * UNIT_SCALE , mapObject.y * UNIT_SCALE)
                                    size.set(mapObject.width * UNIT_SCALE , mapObject.height * UNIT_SCALE)
                                    isFlipX = flipX
                                }
                            }
                        }else{
                            val isPlatform = mapObject.name == "platform"
                            val isPortal = mapObject.name == "portal"
                            world.entity {
                                add<PhysicComponent>{
                                    body = createBody(box2dWorld,
                                        (mapObject.shape as Rectangle).apply {
                                            setSize(this.width * UNIT_SCALE , this.height * UNIT_SCALE)
                                            setPosition(x * UNIT_SCALE, y * UNIT_SCALE)
                                        },
                                        if (isPortal) Constants.PORTAL else Constants.OBJECT,
                                        Constants.KING or Constants.PORTAL or Constants.ITEM or Constants.OBJECT or Constants.ENEMY or Constants.ATTACK_OBJECT or Constants.DESTROYABLE,
                                        BodyType.StaticBody ,
                                        vec2(1f,1f),
                                        vec2(0f,0f),
                                        isPortal = isPortal,
                                        isCollision = true,
                                        isPlatform = isPlatform
                                    )
                                    if (isPortal){
                                        body.fixtureList.first().userData = mapObject.properties.get("toMap",String::class.java)
                                    }
                                }
                            }
                        }
                    }
                }

                return true
            }
        }
        return false
    }

}
