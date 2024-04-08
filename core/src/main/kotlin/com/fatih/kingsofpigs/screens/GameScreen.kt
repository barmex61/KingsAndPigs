package com.fatih.kingsofpigs.screens

import box2dLight.Light
import box2dLight.RayHandler
import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.fatih.kingsofpigs.KingOfPigs.Companion.UNIT_SCALE
import com.fatih.kingsofpigs.ecs.component.AiComponent.Companion.AiComponentListener
import com.fatih.kingsofpigs.ecs.component.DestroyableComponent.Companion.DestroyableListener
import com.fatih.kingsofpigs.ecs.component.DialogComponent.Companion.DialogComponentListener
import com.fatih.kingsofpigs.ecs.component.FloatingTextComponent.Companion.FloatingTextComponentListener
import com.fatih.kingsofpigs.ecs.component.ImageComponent.Companion.ImageComponentListener
import com.fatih.kingsofpigs.ecs.component.LightComponent
import com.fatih.kingsofpigs.ecs.component.LightComponent.Companion.LightComponentListener
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.PhysicComponentListener
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
import com.fatih.kingsofpigs.ecs.system.DialogSystem
import com.fatih.kingsofpigs.ecs.system.EntitySpawnSystem
import com.fatih.kingsofpigs.ecs.system.FloatingTextSystem
import com.fatih.kingsofpigs.ecs.system.ItemSystem
import com.fatih.kingsofpigs.ecs.system.LifeSystem
import com.fatih.kingsofpigs.ecs.system.LightSystem
import com.fatih.kingsofpigs.ecs.system.MoveSystem
import com.fatih.kingsofpigs.ecs.system.PhysicSystem
import com.fatih.kingsofpigs.ecs.system.PortalSystem
import com.fatih.kingsofpigs.ecs.system.RangeAttackSystem
import com.fatih.kingsofpigs.ecs.system.RenderSystem
import com.fatih.kingsofpigs.ecs.system.StateSystem
import com.fatih.kingsofpigs.input.KeyboardInputProcessor
import com.fatih.kingsofpigs.input.addProcessor
import com.fatih.kingsofpigs.ui.view.GameView
import com.fatih.kingsofpigs.ui.view.gameView
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.math.div
import ktx.math.times
import ktx.scene2d.actors

class GameScreen(spriteBatch: SpriteBatch,private val changeScreen : (Class<out KtxScreen>) -> Unit) : KtxScreen {

    private val orthographicCamera = OrthographicCamera()
    private val gameViewport = ExtendViewport(16f,9f,orthographicCamera)
    private val uiViewport = ExtendViewport(320f,180f)
    private val gameStage = Stage(gameViewport,spriteBatch)
    private val box2dWorld = createWorld(Vector2(0f,-9.8f) * 1/UNIT_SCALE * 0.7f,false)
    private val textureAtlas = TextureAtlas(Gdx.files.internal("graphics/gameObject.atlas"))
    private val uiStage = Stage(uiViewport,spriteBatch)
    private var disposed : Boolean = false
    private var gameView: GameView
    private var rayHandler = RayHandler(box2dWorld).apply {
        RayHandler.useDiffuseLight(true)
        Light.setGlobalContactFilter(Constants.LIGHT,-1,Constants.OBJECT)
        setAmbientLight(Color.WHITE)
    }
    private val world = world {

        components {
            add<ImageComponentListener>()
            add<PhysicComponentListener>()
            add<StateComponentListener>()
            add<FloatingTextComponentListener>()
            add<AiComponentListener>()
            add<RangeAttackComponentListener>()
            add<DestroyableListener>()
            add<DialogComponentListener>()
            add<LightComponentListener>()
        }
        injectables {
            add("uiStage",uiStage)
            add(gameStage)
            add(box2dWorld)
            add(textureAtlas)
            add(rayHandler)
        }
        systems {
            add<EntitySpawnSystem>()
            add<AnimationSystem>()
            add<MoveSystem>()
            add<PhysicSystem>()
            add<LightSystem>()
            add<DialogSystem>()
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
        uiStage.actors {
            gameView =  gameView(isPhone = Gdx.app.type == ApplicationType.Android || Gdx.app.type == ApplicationType.iOS){
                gameStage.addListener(this)
            }
        }
        addProcessor(uiStage)
        world.systems.filterIsInstance<EventListener>().forEach { gameStage.addListener(it) }
        world.system<PortalSystem>().apply {
            changeMap = true
            portalPath = "map/map5.tmx"
        }
        val kbInputProcessor = KeyboardInputProcessor(world, changeScreen = ::changeScreen)
        world.system<PhysicSystem>().inputProcessor = kbInputProcessor
        gameView.inputProcessor = kbInputProcessor
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
        rayHandler.useCustomViewport(gameStage.viewport.screenX,gameStage.viewport.screenY,gameStage.viewport.screenWidth,gameStage.viewport.screenHeight)
    }

    override fun dispose() {
        world.dispose()
        gameStage.disposeSafely()
        box2dWorld.disposeSafely()
        rayHandler.disposeSafely()
        textureAtlas.disposeSafely()
    }

}
