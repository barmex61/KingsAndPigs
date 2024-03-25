package com.fatih.kingsofpigs.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.fatih.kingsofpigs.KingOfPigs.Companion.UNIT_SCALE
import com.fatih.kingsofpigs.ecs.component.AiComponent.Companion.AiComponentListener
import com.fatih.kingsofpigs.ecs.component.DestroyableComponent
import com.fatih.kingsofpigs.ecs.component.DestroyableComponent.Companion.DestroyableListener
import com.fatih.kingsofpigs.ecs.component.FloatingTextComponent.Companion.FloatingTextComponentListener
import com.fatih.kingsofpigs.ecs.component.ImageComponent.Companion.ImageComponentListener
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.PhysicComponentListener
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.ecs.component.RangeAttackComponent.Companion.RangeAttackComponentListener
import com.fatih.kingsofpigs.ecs.component.StateComponent.Companion.StateComponentListener
import com.fatih.kingsofpigs.ecs.system.AiSystem
import com.fatih.kingsofpigs.ecs.system.AnimationSystem
import com.fatih.kingsofpigs.ecs.system.MeleeAttackSystem
import com.fatih.kingsofpigs.ecs.system.AudioSystem
import com.fatih.kingsofpigs.ecs.system.CameraSystem
import com.fatih.kingsofpigs.ecs.system.DeadSystem
import com.fatih.kingsofpigs.ecs.system.DebugSystem
import com.fatih.kingsofpigs.ecs.system.DestroyableObjectSystem
import com.fatih.kingsofpigs.ecs.system.EntitySpawnSystem
import com.fatih.kingsofpigs.ecs.system.FloatingTextSystem
import com.fatih.kingsofpigs.ecs.system.ItemSystem
import com.fatih.kingsofpigs.ecs.system.LifeSystem
import com.fatih.kingsofpigs.ecs.system.MoveSystem
import com.fatih.kingsofpigs.ecs.system.PhysicSystem
import com.fatih.kingsofpigs.ecs.system.PortalSystem
import com.fatih.kingsofpigs.ecs.system.RangeAttackSystem
import com.fatih.kingsofpigs.ecs.system.RenderSystem
import com.fatih.kingsofpigs.ecs.system.StateSystem
import com.fatih.kingsofpigs.input.KeyboardInputProcessor
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.math.div
import ktx.math.times
import kotlin.reflect.KClass

class GameScreen(private val spriteBatch: SpriteBatch,private val changeScreen : (Class<out KtxScreen>) -> Unit) : KtxScreen {

    private val orthographicCamera = OrthographicCamera()
    private val gameViewport = ExtendViewport(16f,9f,orthographicCamera)
    private val uiViewport = ExtendViewport(320f,180f)
    private val gameStage = Stage(gameViewport,spriteBatch).apply { isDebugAll= true }
    private val box2dWorld = createWorld(Vector2(0f,-9.8f) * 1/UNIT_SCALE * 0.7f,false)
    private val textureAtlas = TextureAtlas(Gdx.files.internal("graphics/gameObject.atlas"))
    private val uiStage = Stage(uiViewport,spriteBatch)
    private var disposed : Boolean = false
    private val world = world {

        components {
            add<ImageComponentListener>()
            add<PhysicComponentListener>()
            add<StateComponentListener>()
            add<FloatingTextComponentListener>()
            add<AiComponentListener>()
            add<RangeAttackComponentListener>()
            add<DestroyableListener>()
        }
        injectables {
            add("uiStage",uiStage)
            add(gameStage)
            add(box2dWorld)
            add(textureAtlas)
        }
        systems {
            add<EntitySpawnSystem>()
            add<AnimationSystem>()
            add<MoveSystem>()
            add<PhysicSystem>()
            add<LifeSystem>()
            add<DeadSystem>()
            add<DestroyableObjectSystem>()
            add<ItemSystem>()
            add<MeleeAttackSystem>()
            add<RangeAttackSystem>()
            add<FloatingTextSystem>()
            add<AudioSystem>()
            add<StateSystem>()
            add<PortalSystem>()
            add<AiSystem>()
            add<CameraSystem>()
            add<RenderSystem>()
            add<DebugSystem>()
        }
    }

    init {
        world.systems.filterIsInstance<EventListener>().forEach { gameStage.addListener(it) }
        world.system<PortalSystem>().changeMap("map/map1.tmx")
        KeyboardInputProcessor(world, changeScreen = ::changeScreen)
    }

    private fun changeScreen(){
        changeScreen(UiScreen::class.java)
        if (!disposed){
            disposeSafely()
            disposed = true
        }
    }


    override fun render(delta: Float) {
        world.update(delta.coerceAtMost(0.25f))
        GdxAI.getTimepiece().update(delta.coerceAtMost(0.25f))
    }

    override fun resize(width: Int, height: Int) {
        gameStage.viewport.update(width,height,true)
        uiStage.viewport.update(width,height,true)
    }

    override fun dispose() {
        world.dispose()
        gameStage.disposeSafely()
        box2dWorld.disposeSafely()
    }

}
