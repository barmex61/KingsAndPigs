package com.fatih.kingsofpigs.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.fatih.kingsofpigs.ui.view.uiView
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.scene2d.actors

class UiScreen(private val spriteBatch: SpriteBatch,private val changeScreen : (Class<out KtxScreen>) -> Unit) : KtxScreen{

    private val uiCamera = OrthographicCamera()
    private val uiViewPort = ExtendViewport(960f,540f,uiCamera)
    private val uiStage = Stage(uiViewPort,spriteBatch).apply { isDebugAll = true }
    private var disposed : Boolean = false


    override fun show() {
        uiStage.actors {
            uiView()
        }
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)){
            changeScreen()
        }
    }

    override fun resize(width: Int, height: Int) {
        uiStage.viewport.update(width,height,true)
    }

    override fun dispose() {
        uiStage.disposeSafely()
    }
}
