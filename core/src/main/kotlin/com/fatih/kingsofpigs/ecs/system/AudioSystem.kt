package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.fatih.kingsofpigs.event.PigGetHitEvent
import com.fatih.kingsofpigs.event.PlayerGetHitEvent
import com.github.quillraven.fleks.IntervalSystem
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
                }.also { it.play() }
                return true
            }
            is PlayerGetHitEvent ->{
                val soundPath = "audio/player_hit${(1..2).random()}.wav"
                soundCache.getOrPut(soundPath){
                    Gdx.audio.newSound(Gdx.files.internal(soundPath))
                }.also { it.play() }
                return true
            }
            is PigGetHitEvent ->{
                val soundPath = "audio/giant${(1..5).random()}.wav"
                soundCache.getOrPut(soundPath){
                    Gdx.audio.newSound(Gdx.files.internal(soundPath))
                }.also { it.play() }
                return true
            }
        }
        return false
    }

}
