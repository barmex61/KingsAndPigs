package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.fatih.kingsofpigs.event.GameOverEvent
import com.fatih.kingsofpigs.event.VictoryEvent
import com.fatih.kingsofpigs.event.JumpEvent
import com.fatih.kingsofpigs.event.MapChangeEvent
import com.fatih.kingsofpigs.event.MeleeAttackEvent
import com.fatih.kingsofpigs.event.PigGetHitEvent
import com.fatih.kingsofpigs.event.PlayerGitHitEvent
import com.fatih.kingsofpigs.event.RangeAttackEvent
import com.fatih.kingsofpigs.event.StopAudioEvent
import com.github.quillraven.fleks.IntervalSystem
import ktx.tiled.property

class AudioSystem : IntervalSystem() , EventListener{

    private val musicCache = mutableMapOf<String,Music>()
    private val soundCache = mutableMapOf<String,Sound>()
    private var lastAudioPath = "audio/mapmusic.mp3"
    private val soundPaths = listOf(
        "audio/bomb.ogg",
        "audio/cannon_explosion.ogg",
        "audio/cannon_fire.ogg",
        "audio/flyattack.ogg",
        "audio/giant1.wav",
        "audio/giant2.wav",
        "audio/giant3.wav",
        "audio/giant4.wav",
        "audio/giant5.wav",
        "audio/groundattack.ogg",
        "audio/jump1.wav",
        "audio/jump2.wav",
        "audio/player_hit1.wav",
        "audio/player_hit2.wav",
        "audio/demon_laught.mp3"
    )
    var changeScreen : () -> Unit = {}

    init {
        soundPaths.forEach {
            soundCache[it] = Gdx.audio.newSound(Gdx.files.internal(it))
        }
    }

    override fun onTick() {

    }
    private fun stopMusic(){
        musicCache.values.forEach {
            it.stop()
        }
    }

    override fun handle(event: Event): Boolean {
        when(event){
            is StopAudioEvent ->{
                stopMusic()
            }
            is VictoryEvent ->{
                stopMusic()
                musicCache.getOrPut("audio/victory.mp3") {
                    Gdx.audio.newMusic(Gdx.files.internal("audio/victory.mp3"))
                }.apply {
                    play()
                    setOnCompletionListener {
                        changeScreen()
                    }
                }
            }
            is GameOverEvent -> {
                stopMusic()
                musicCache.getOrPut("audio/gameover.mp3") {
                    Gdx.audio.newMusic(Gdx.files.internal("audio/gameover.mp3"))
                }.apply {
                    play()
                    setOnCompletionListener {
                        changeScreen()
                    }
                }
            }
            is MapChangeEvent ->{
                val musicPath = event.map.property<String>("mapMusic")
                if(lastAudioPath != musicPath) stopMusic()
                lastAudioPath = musicPath
                musicCache.getOrPut(musicPath){
                    Gdx.audio.newMusic(Gdx.files.internal(musicPath))
                }.also {
                    it.volume = 0.35f
                    it.play() }
                if (musicPath == "audio/demon_boss.mp3") {
                    soundCache["audio/demon_laught.mp3"]!!.play(0.8f)
                }
                return true
            }
            is PlayerGitHitEvent ->{
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
