package com.fatih.kingsofpigs.ui.view

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.fatih.kingsofpigs.event.HpChangeEvent
import com.fatih.kingsofpigs.event.LifeChangeEvent
import com.fatih.kingsofpigs.event.PigGetHitEvent
import com.fatih.kingsofpigs.event.PlayerGitHitEvent
import com.fatih.kingsofpigs.ui.Drawables
import com.fatih.kingsofpigs.ui.widget.CharacterHud
import com.fatih.kingsofpigs.ui.widget.PlayerLifeHud
import com.fatih.kingsofpigs.ui.widget.characterHud
import com.fatih.kingsofpigs.ui.widget.playerLifeHud
import ktx.actors.alpha
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor

class GameView(
    skin: Skin
) : KTable, Table() , EventListener{

    private var playerHud : CharacterHud
    private var enemyHud : CharacterHud
    private var playerLifeHud : PlayerLifeHud

    init {
        setFillParent(true)
        top().left()
        playerHud = characterHud (skin){
            it.padTop(6f).padLeft(6f)
        }
        enemyHud = characterHud(skin){
            this.alpha = 0f
            it.expandX().padTop(6f).padRight(50f)

        }
        row().left()
        playerLifeHud = playerLifeHud(skin){
            it.padTop(30f).padLeft(6f)
        }
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is PlayerGitHitEvent -> {
                playerHud.setHpPercentage(event.hpPercentage)
                return true
            }
            is PigGetHitEvent ->{
                enemyHud.setCharacterImageDrawable(Drawables.valueOf(event.entityModel.name))
                enemyHud.setHpPercentage(event.hpPercentage,true)
                return true
            }
            is LifeChangeEvent ->{
                playerLifeHud.setExtraLife(event.extraLife)
            }
            is HpChangeEvent ->{
                playerHud.setHpPercentage(event.hpPercentage)
                return true
            }
        }
        return false
    }
}

fun <S>KWidget<S>.gameView(
    skin: Skin = Scene2DSkin.defaultSkin,
    init : GameView.(S) -> Unit = {}
) : GameView = actor(GameView(skin),init)
