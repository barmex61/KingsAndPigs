package com.fatih.kingsofpigs.ecs.component

data class LifeComponent (
    var maxHp : Float = DEFAULT_MAX_HP,
    var currentHp : Float = maxHp,
    val maxLife : Int = DEFAULT_MAX_LIFE,
    var currentLife : Int = maxLife,
    var damageTaken : Float = 0f,
    var canResurrect : Boolean = false,
    var resurrectionTime : Float = 0f,
    var isCrit : Boolean = false,
    var getHit : Boolean = false
){
    companion object{
        const val DEFAULT_MAX_HP = 30f
        const val DEFAULT_MAX_LIFE = 3
    }
}
