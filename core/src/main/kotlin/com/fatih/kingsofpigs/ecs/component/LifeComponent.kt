package com.fatih.kingsofpigs.ecs.component

data class LifeComponent (
    var maxLife : Float = DEFAULT_MAX_LIFE,
    var currentLife : Float = maxLife,
    var regeneration : Float = 1f,
    var damageTaken : Float = 0f,
    var canResurrect : Boolean = false,
    var resurrectionTime : Float = 0f,
    var isCrit : Boolean = false
){
    companion object{
        const val DEFAULT_MAX_LIFE = 20f
    }
}
