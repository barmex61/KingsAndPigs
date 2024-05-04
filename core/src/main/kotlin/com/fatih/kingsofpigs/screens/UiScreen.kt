package com.fatih.kingsofpigs.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.fatih.kingsofpigs.input.addProcessor
import com.fatih.kingsofpigs.input.removeProcessor
import com.fatih.kingsofpigs.ui.view.UiView
import com.fatih.kingsofpigs.ui.view.uiView
import com.fatih.kingsofpigs.utils.AdVisibilityListener
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.scene2d.actors

class UiScreen(spriteBatch: SpriteBatch,val adVisibilityListener: AdVisibilityListener?,private val changeScreen : (Class<out KtxScreen>) -> Unit) : KtxScreen{

    private val uiCamera = OrthographicCamera()
    private val uiViewPort = ExtendViewport(960f,540f,uiCamera)
    private val uiStage = Stage(uiViewPort,spriteBatch)
    private var disposed : Boolean = false
    private var uiView : UiView? = null

    override fun show() {
        uiStage.actors {
            uiView = uiView()
        }
        addProcessor(uiStage)
    }

    private fun changeScreen(){
        changeScreen(GameScreen::class.java)
        if (!disposed){
            disposeSafely()
            disposed = true
        }
    }

    override fun render(delta: Float) {
        uiStage.act(delta)
        uiStage.draw()
        if (uiView?.changeScreen == true){
            uiView?.clearActions()
            changeScreen()
            uiView?.changeScreen = false
            removeProcessor(uiStage)
        }
    }

    override fun resize(width: Int, height: Int) {
        uiStage.viewport.update(width,height,true)
        uiView?.clearActions()
        uiView?.clear()
        uiView?.initialize()
    }

    override fun dispose() {
        uiStage.disposeSafely()
    }
}
