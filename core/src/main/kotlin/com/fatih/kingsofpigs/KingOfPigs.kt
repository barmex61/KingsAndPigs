package com.fatih.kingsofpigs


import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.fatih.kingsofpigs.screens.GameScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen


class KingOfPigs : KtxGame<KtxScreen>() {

    private val spriteBatch by lazy {
        SpriteBatch()
    }
    override fun create() {
        addScreen(GameScreen(spriteBatch))
        setScreen<GameScreen>()
    }

    companion object{
        const val UNIT_SCALE = 1/16f
    }
}
