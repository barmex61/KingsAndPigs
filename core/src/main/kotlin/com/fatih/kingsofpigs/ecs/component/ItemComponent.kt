package com.fatih.kingsofpigs.ecs.component

import com.github.quillraven.fleks.Entity

sealed class Item {
    data class Diamond(val extraLife: Int = 1) : Item()
    data class Heart(val hp : Int = 1) : Item()
}

class ItemComponent(
    var extraLife : Float = 0f,
){
    lateinit var item : Item
    var collideEntity : Entity? = null
}
