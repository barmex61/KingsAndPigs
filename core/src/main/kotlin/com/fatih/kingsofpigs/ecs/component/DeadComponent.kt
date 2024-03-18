package com.fatih.kingsofpigs.ecs.component

data class DeadComponent(
    var resurrectionTime : Float = 0f,
    var canResurrect : Boolean = false,
    var remove : Boolean = false,
)
