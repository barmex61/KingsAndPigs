package com.fatih.kingsofpigs.ecs.component

data class MoveComponent (
    var cos : Float = 0f,
    var sin : Float = 0f,
    var speed : Float = DEFAULT_MOVE_SPEED,
    var root : Boolean = false,
    var aiMoveRadius : Float = 3f,
    var aiCanMove : Boolean = true
){
    companion object{
        const val DEFAULT_MOVE_SPEED = 1f
    }
}
