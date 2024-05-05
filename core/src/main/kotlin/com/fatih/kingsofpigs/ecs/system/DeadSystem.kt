package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.DeadComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.event.ContinueGameEvent
import com.fatih.kingsofpigs.event.DoNotWantToContinueEvent
import com.fatih.kingsofpigs.event.GameOverEvent
import com.fatih.kingsofpigs.event.HpChangeEvent
import com.fatih.kingsofpigs.event.LifeChangeEvent
import com.fatih.kingsofpigs.event.ShowRewardedAdViewEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.fatih.kingsofpigs.utils.AdVisibilityListener
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadComps : ComponentMapper<DeadComponent>,
    private val lifeComps : ComponentMapper<LifeComponent>,
    private val playerComps : ComponentMapper<PlayerComponent>,
    private val gameStage : Stage
) : IteratingSystem(),EventListener{

    var adVisibilityListener : AdVisibilityListener? = null
    var deadTimer = 5f
    private var endGame : Boolean = false
    private var continueGame : Boolean = false

    override fun onTickEntity(entity: Entity) {
        val deadComponent = deadComps[entity]
        deadComponent.run {
            if (!canResurrect && remove ){
                world.remove(entity)
                if (entity in playerComps){
                    gameStage.fireEvent(GameOverEvent())
                }
                return
            }
            if (canResurrect){
                resurrectionTime -= deltaTime
                if (lifeComps[entity].currentLife> 0){
                    if (resurrectionTime <= 0f ){
                        configureEntity(entity){
                            deadComps.remove(it)
                            lifeComps[entity].apply {
                                currentHp = maxHp
                                damageTaken = 0f
                                gameStage.fireEvent(HpChangeEvent(currentHp/maxHp))
                            }
                        }
                    }
                }else{

                    if (deadTimer == 5f){
                        println("show")
                        gameStage.fireEvent(ShowRewardedAdViewEvent(true))
                    }

                    if (deadTimer <= 0f || endGame){
                        gameStage.fireEvent(ShowRewardedAdViewEvent(false))
                        deadTimer = 5f
                        endGame = false
                        canResurrect = false
                        remove = true
                    }
                    deadTimer -= deltaTime
                    if (continueGame){
                        lifeComps[entity].currentLife = (lifeComps[entity].currentLife +1 ).coerceAtMost(1)
                        gameStage.fireEvent(LifeChangeEvent(lifeComps[entity].currentLife))
                        resurrectionTime = 0f
                        continueGame = false
                        deadTimer = 5f
                    }
                }
            }

        }
    }

    override fun handle(event: Event): Boolean {
        return when(event){
            is DoNotWantToContinueEvent ->{
                endGame = true
                true
            }
            is ContinueGameEvent ->{
                continueGame = true
                true
            }
            else -> false
        }
    }
}
