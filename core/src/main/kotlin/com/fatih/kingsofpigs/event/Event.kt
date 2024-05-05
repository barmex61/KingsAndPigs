package com.fatih.kingsofpigs.event

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.EntityModel

fun Stage.fireEvent(event : Event) = this.root.fire(event)

data class MapChangeEvent(val map : TiledMap) : Event()
class PlayerGitHitEvent (val hpPercentage: Float): Event()
class PigGetHitEvent(val hpPercentage : Float,val entityModel: EntityModel) : Event()
class HpChangeEvent(val hpPercentage: Float) : Event()
class LifeChangeEvent(val extraLife : Int) : Event()
class JumpEvent : Event()
class MeleeAttackEvent(val onAir : Boolean) : Event()
class RangeAttackEvent(val explode : Boolean = false) : Event()
class ShowPortalDialogEvent(val dialog : String) : Event()
class StopAudioEvent : Event()
class VictoryEvent : Event()
class GameOverEvent : Event()
class ShowRewardedAdViewEvent(val showView : Boolean) : Event()
class DoNotWantToContinueEvent : Event()
class ContinueGameEvent : Event()
