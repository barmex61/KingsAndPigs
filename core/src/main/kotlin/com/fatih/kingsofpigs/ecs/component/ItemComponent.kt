package com.fatih.kingsofpigs.ecs.component

import com.github.quillraven.fleks.Entity

class ItemComponent(
    var itemModel : EntityModel = EntityModel.DIAMOND,
    var extraLife : Float = 0f,
){
    var collideEntity : Entity? = null
}
