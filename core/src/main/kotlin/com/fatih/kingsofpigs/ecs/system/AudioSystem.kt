package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.fatih.kingsofpigs.event.JumpEvent
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.fatih.kingsofpigs.event.MeleeAttackEvent
import com.fatih.kingsofpigs.event.PigGetHitEvent
import com.fatih.kingsofpigs.event.PlayerGetHitEvent
import com.fatih.kingsofpigs.event.RangeAttackEvent
import com.github.quillraven.fleks.IntervalSystem
import ktx.math.component1
import ktx.math.component2
import ktx.math.vec2
import ktx.tiled.property

class AudioSystem : IntervalSystem() , EventListener{

    private val musicCache = mutableMapOf<String,Music>()
    private val soundCache = mutableMapOf<String,Sound>()

    override fun onTick() {

    }

    override fun handle(event: Event): Boolean {
        when(event){
            is MapChangeEvent ->{
                val musicPath = event.map.property<String>("mapMusic")
                musicCache.getOrPut(musicPath){
                    Gdx.audio.newMusic(Gdx.files.internal(musicPath))
                }.also {
                    it.volume = 0.5f
                    it.play() }
                return true
            }
            is PlayerGetHitEvent ->{
                val soundPath = "audio/player_hit${(1..2).random()}.wav"
                playSound(soundPath)
                return true
            }
            is PigGetHitEvent ->{
                val soundPath = "audio/giant${(1..5).random()}.wav"
                playSound(soundPath)
                return true
            }
            is JumpEvent ->{
                val soundPath = "audio/jump${(1..2).random()}.wav"
                playSound(soundPath)
                return true
            }
            is MeleeAttackEvent ->{
                val onAir = event.onAir
                val soundPath =if (onAir) "audio/flyattack.ogg" else "audio/groundattack.ogg"
                playSound(soundPath)
                return true
            }
            is RangeAttackEvent ->{
                val explode = event.explode
                val list = listOf("audio/cannon_explosion.ogg","audio/bomb.ogg")
                val soundPath = if (explode) list.random() else "audio/cannon_fire.ogg"
                playSound(soundPath)
                return true
            }
        }
        return false
    }

    private fun playSound(soundPath : String){
        soundCache.getOrPut(soundPath){
            Gdx.audio.newSound(Gdx.files.internal(soundPath))
        }.also { it.play(0.5F) }
    }
}
