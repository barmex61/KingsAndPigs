package com.fatih.kingsofpigs.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.event.HpChangeEvent
import com.fatih.kingsofpigs.event.LifeChangeEvent
import com.fatih.kingsofpigs.event.PigGetHitEvent
import com.fatih.kingsofpigs.event.PlayerGitHitEvent
import com.fatih.kingsofpigs.event.ShowPortalDialogEvent
import com.fatih.kingsofpigs.input.KeyboardInputProcessor
import com.fatih.kingsofpigs.ui.Drawables
import com.fatih.kingsofpigs.ui.Labels
import com.fatih.kingsofpigs.ui.widget.AttackHud
import com.fatih.kingsofpigs.ui.widget.CharacterHud
import com.fatih.kingsofpigs.ui.widget.PlayerLifeHud
import com.fatih.kingsofpigs.ui.widget.attackHud
import com.fatih.kingsofpigs.ui.widget.characterHud
import com.fatih.kingsofpigs.ui.widget.playerLifeHud
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.actors.onTouchDown
import ktx.actors.onTouchEvent
import ktx.actors.onTouchUp
import ktx.actors.plusAssign
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.textArea
import ktx.scene2d.textField
import ktx.scene2d.touchpad

class GameView(
    skin: Skin,
    val isPhone : Boolean
) : KTable, Table() , EventListener{

    lateinit var inputProcessor: KeyboardInputProcessor
    private var playerHud : CharacterHud
    private var enemyHud : CharacterHud
    private var playerLifeHud : PlayerLifeHud
    private var dialogLabel: Label
    private var touchPad : Touchpad? = null
    private var attackHud : AttackHud? = null

    init {
        setFillParent(true)
        top().left()
        playerHud = characterHud (skin){
            it.expandX().left().padTop(6f).padLeft(6f)
        }
        enemyHud = characterHud(skin){
            this.alpha = 0f
            it.expandX().left().padTop(6f).padRight(50f)
        }
        row().left()
        playerLifeHud = playerLifeHud(skin){
            it.padTop(30f).padLeft(6f)
        }
        row().left()
        dialogLabel = label("",Labels.DIALOG.name){
            alpha = 0f
            this.wrap = true
            this.fontScaleX = 0.3f
            this.fontScaleY = 0.3f
            this.setAlignment(Align.center)
            it.width(100f).height(50f).colspan(3).padLeft(6f).center()
        }
        row()
        if (isPhone){
            touchPad = touchpad(10f,skin = skin){
                this.setSize(60f,60f)
                this.alpha = 0.25f
                it.padLeft(10f).expandX().bottom().colspan(1)
                onTouchEvent { event, x, y ->
                   this@GameView.inputProcessor.updatePlayerValues(
                       this@GameView.scaleValue(x,false),
                       this@GameView.scaleValue(y,true)
                   )
                }
                onTouchUp {
                    this@GameView.inputProcessor.updatePlayerValues(
                        0f,
                        0f
                    )
                    this.alpha = 0.25f
                }
                onTouchDown {
                    this.alpha = 1f
                }
            }
            attackHud = attackHud{
                it.padRight(10f).expandX().bottom().colspan(1)
                onTouchDown {
                    this.alpha = 1f
                }
                onTouchUp {
                    this.alpha = 0.25f
                }
            }
        }
    }

    private fun scaleValue(value: Float,sin:Boolean): Float {
        val clampedValue = value.coerceIn(0.6f, 2.5f)
        return if (sin) {
            if (clampedValue == 2.5f) 1f else 0f
        } else {
            2f * (clampedValue - 0.6f) / (2.5f - 0.6f) - 1f
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
            is ShowPortalDialogEvent ->{
                dialogLabel.clearActions()
                dialogLabel.addAction(
                    Actions.sequence(
                        fadeIn(2f, Interpolation.pow3Out),
                        fadeOut(1f, Interpolation.pow3Out))
                )
                return true
            }
        }
        return false
    }
}

fun <S>KWidget<S>.gameView(
    skin: Skin = Scene2DSkin.defaultSkin,
    isPhone : Boolean,
    init : GameView.(S) -> Unit = {}
) : GameView = actor(GameView(skin,isPhone),init)
