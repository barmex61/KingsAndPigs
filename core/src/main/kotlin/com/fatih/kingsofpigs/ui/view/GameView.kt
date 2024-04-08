package com.fatih.kingsofpigs.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import com.badlogic.gdx.utils.Align
import com.fatih.kingsofpigs.ecs.component.EntityModel
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
                it.expandX().left().padLeft(30f).bottom().colspan(1)
                this.addListener(object : DragListener(){

                    override fun touchDragged(
                        event: InputEvent?,
                        x: Float,
                        y: Float,
                        pointer: Int
                    ) {
                        val posX = if (knobPercentX > 0.5f) 1f else if (knobPercentX < -0.5f) -1f else 0f
                        val posY = if (knobPercentY > 0.7f) 1f else 0f
                        this@GameView.inputProcessor.updatePlayerValues(
                           posX,posY
                        )
                        super.touchDragged(event, x, y, pointer)
                    }

                    override fun dragStart(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                        this@touchpad.alpha = 1f
                        super.dragStart(event, x, y, pointer)
                    }

                    override fun dragStop(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                        this@GameView.inputProcessor.updatePlayerValues(
                            0f,
                            0f
                        )
                        this@touchpad.alpha = 0.25f
                        super.dragStop(event, x, y, pointer)
                    }
                })

            }
            attackHud = attackHud{
                it.expandX().right().padRight(90f).bottom().colspan(1)
                this.alpha = 0.25f
                addListener(object : ClickListener(){
                    override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                        this@attackHud.alpha = 1f
                        this@GameView.inputProcessor.updateAttack()
                        return super.touchDown(event, x, y, pointer, button)
                    }

                    override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                        this@attackHud.alpha = 0.25f
                        super.touchUp(event, x, y, pointer, button)
                    }
                })
            }
        }
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is PlayerGitHitEvent -> {
                playerHud.setHpPercentage(event.hpPercentage)
                return true
            }
            is PigGetHitEvent ->{
                enemyHud.setCharacterImageDrawable(Drawables.valueOf(if (event.entityModel == EntityModel.DEMON) EntityModel.KING.name else event.entityModel.name))
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
                dialogLabel.setText(event.dialog)
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
