package com.fatih.kingsofpigs.ui


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import ktx.assets.disposeSafely
import ktx.scene2d.Scene2DSkin
import ktx.style.imageButton
import ktx.style.label
import ktx.style.set
import ktx.style.skin
import ktx.style.textField
import ktx.style.touchpad

enum class Fonts(val scale : Float = 1f){
    SEGOE_PRINT_GRADIENT_GREEN(0.4F),
    SEGOE_PRINT_GRADIENT_RED(0.4F),
    CORBEL(0.3F),
    SYLFAEN(0.7F);
    val atlasKey = this.name.lowercase()
    val fontPath = "ui/${atlasKey}.fnt"
}

enum class Labels{
    COLUMN,DIALOG,AD;
}

enum class TextFields{
    TITLE
}
enum class ImageButtons{
    ATTACK;
    val skinKey: String = this.name.lowercase()
}

enum class Drawables{
    KING,KING_PIG,GOLEM,DEMON,LIFE_BAR,HEARTH,PIG,ICON_ATTACK,TOUCHPAD_BG,TOUCHPAD_KNOB,PIG_BOMB,PIG_LIGHT,PIG_BOX,BUTTON_R,BUTTON_P,MP_BAR,HP_BAR,STATUS_BAR,BUTTON_LEFT,BUTTON_RIGHT,BUTTON_DOWN,BUTTON_UP,BUTTON_SPACE,CIRCLE_GRADIENT_BACKGROUND,RECTANGLE_GRADIENT_BACKGROUND,BIG_BACKGROUND,BLUE_BACKGROUND,RED_BACKGROUND,TITLE_BACKGROUND,ICON_CROWN,ICON_LEFT,ICON_RIGHT,ICON_UP,ICON_LIGHTNING,ICON_MENU,ICON_SCORE;
    val atlasKey = this.name.lowercase()
}

operator fun Skin.get(fonts : Fonts) : BitmapFont = this.getFont(fonts.atlasKey)
operator fun Skin.get(drawables : Drawables) : Drawable = this.getDrawable(drawables.atlasKey)

fun loadSkin(){
    Scene2DSkin.defaultSkin = skin(TextureAtlas("ui/uiObject.atlas")){skin->
        Fonts.entries.forEach {font->
            loadBitmapFont(font,skin)
        }
        textField(TextFields.TITLE.name){
            font = skin[Fonts.SYLFAEN]
            fontColor = Color(63f/255f,63f/255f,63/255f,1f)
        }
        label(Labels.COLUMN.name){
            background = skin[Drawables.BLUE_BACKGROUND]
            font = skin[Fonts.SYLFAEN]
            fontColor = Color(67f/255f,54f/255f,29f/255f,1f)
        }
        label(Labels.DIALOG.name){
            background = skin[Drawables.BIG_BACKGROUND]
            font = skin[Fonts.SYLFAEN]
            fontColor = Color(67f/255f,54f/255f,29f/255f,1f)
        }
        label(Labels.AD.name){
            background = skin[Drawables.BLUE_BACKGROUND]
            font = skin[Fonts.CORBEL]
            fontColor = Color(67f/255f,54f/255f,29f/255f,1f)
        }
        imageButton(ImageButtons.ATTACK.skinKey) {
            imageUp = skin[Drawables.ICON_ATTACK]
            imageDown = skin[Drawables.ICON_ATTACK]
        }
        touchpad{
            background = skin[Drawables.TOUCHPAD_BG].apply {
                minWidth = 60f
                minHeight = 60f
            }
            knob = skin[Drawables.TOUCHPAD_KNOB].apply {
                minWidth = 25f
                minHeight = 25f
            }
        }
    }
}

private fun loadBitmapFont(font: Fonts,skin: Skin){
    skin[font.atlasKey] = BitmapFont(Gdx.files.internal(font.fontPath),skin.getRegion(font.atlasKey)).apply {
        data.setScale(font.scale)
        data.markupEnabled = true
    }
}

fun disposeSkin(){
    Scene2DSkin.defaultSkin.disposeSafely()
}
