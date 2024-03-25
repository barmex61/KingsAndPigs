package com.fatih.kingsofpigs.ui


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.assets.disposeSafely
import ktx.scene2d.Scene2DSkin
import ktx.style.label
import ktx.style.set
import ktx.style.skin
import ktx.style.textButton
import ktx.style.textField

enum class Fonts(val scale : Float = 1f){
    SEGOE_PRINT_GRADIENT_GREEN(1F),
    SEGOE_PRINT_GRADIENT_RED(1F),
    SYLFAEN(1F);
    val atlasKey = this.name.lowercase()
    val fontPath = "ui/${atlasKey}.fnt"
}

enum class Labels{
    TITLE
}

enum class TextButtons{
    COLUMN
}
enum class TextFields{
    TITLE
}

enum class Drawables{
    BIG_BACKGROUND,BLUE_BACKGROUND,RED_BACKGROUND,TITLE_BACKGROUND,FONT1,ICON_CROWN,ICON_LEFT,ICON_RIGHT,ICON_UP,ICON_LIGHTNING,ICON_MENU,ICON_SCORE;
    val atlasKey = this.name.lowercase()
}

operator fun Skin.get(fonts : Fonts) : BitmapFont = this.getFont(fonts.atlasKey)
operator fun Skin.get(drawables : Drawables) : Drawable = this.getDrawable(drawables.atlasKey)

fun loadSkin(){
    Scene2DSkin.defaultSkin = skin(TextureAtlas("ui/uiObject.atlas")){skin->
        Fonts.entries.forEach {font->
            skin[font.atlasKey] = BitmapFont(Gdx.files.internal(font.fontPath),skin.getRegion(font.atlasKey)).apply {
                data.setScale(font.scale)
                data.markupEnabled = true
            }
        }
        textField(TextFields.TITLE.name){
            font = skin[Fonts.SYLFAEN]
            fontColor = Color(0.988f,0.49f,0.44f,1f)
            font.data.setScale(0.7f)
        }

    }
}

fun disposeSkin(){
    Scene2DSkin.defaultSkin.disposeSafely()
}
