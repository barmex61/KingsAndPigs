package com.fatih.kingsofpigs.ecs.component

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import ktx.app.gdxError

val attackDialogKeys = listOf("dead","attack","loser")
val idleDialogKeys = listOf("hi","hello")
val hitDialogKeys = listOf("loser","wtf","no")
val alertDialogKeys = listOf("alert","question")

enum class DialogType{
    ATTACK,IDLE,ALERT,HIT;
    val atlasKey : String
    get() = when(this.name){
        "ATTACK" -> "dialogs/${attackDialogKeys.random()}"
        "IDLE" -> "dialogs/${idleDialogKeys.random()}"
        "ALERT" -> "dialogs/${alertDialogKeys.random()}"
        "HIT" -> "dialogs/${hitDialogKeys.random()}"
        else -> gdxError("Atlas key path not found for ${this.name}")
    }

}

data class DialogComponent(
    val dialogImage : Image = Image().apply { setSize(1.5f,0.6f) },
    var showDialog : Boolean = false,
    var dialogTimer : Float = 1f
){
    lateinit var  dialogType : DialogType
    companion object{
        class DialogComponentListener (private val gameStage : Stage): ComponentListener<DialogComponent>{
            override fun onComponentAdded(entity: Entity, component: DialogComponent) {
                gameStage.addActor(component.dialogImage)
            }

            override fun onComponentRemoved(entity: Entity, component: DialogComponent) {
                gameStage.root.removeActor(component.dialogImage)
            }
        }
    }
}
