package com.fatih.kingsofpigs.actor

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Image

class FlipImage (var flipX : Boolean = false,var setZ : Boolean = false): Image() {

    override fun draw(batch: Batch?, parentAlpha: Float) {
        if (setZ) {
            zIndex = 0
        }
        if (flipX){
            drawable.draw(batch,x+width,y,-width,height)
        }else{
            super.draw(batch, parentAlpha)
        }
    }

}
