package com.fatih.kingsofpigs


import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.fatih.kingsofpigs.screens.GameScreen
import com.fatih.kingsofpigs.screens.UiScreen
import com.fatih.kingsofpigs.ui.disposeSkin
import com.fatih.kingsofpigs.ui.loadSkin
import com.fatih.kingsofpigs.utils.AdVisibilityListener
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.gdxError


class KingOfPigs : KtxGame<KtxScreen>() {

    var adVisibilityListener: AdVisibilityListener? = null
    private val spriteBatch by lazy {
        SpriteBatch()
    }
    override fun create() {
        loadSkin()
        addScreen(UiScreen(spriteBatch,adVisibilityListener, changeScreen = ::changeScreen))
        setScreen<UiScreen>()
    }

    private fun changeScreen(screenType : Class<out KtxScreen>){
        removeScreen((currentScreen as KtxScreen)::class.java)
        when(screenType){
            UiScreen::class.java ->{
                addScreen(UiScreen(spriteBatch,adVisibilityListener, ::changeScreen))
                setScreen<UiScreen>()
            }
            GameScreen::class.java ->{
                addScreen(GameScreen(spriteBatch,adVisibilityListener,::changeScreen))
                setScreen<GameScreen>()
            }
            else -> gdxError("No screen type $screenType found in game")
        }

    }

    override fun dispose() {
        disposeSkin()
        super.dispose()
    }

    companion object{
        const val UNIT_SCALE = 1/16f
    }
}
