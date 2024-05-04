package com.fatih.kingsofpigs.ecs.system

import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.environment.AmbientCubemap
import com.badlogic.gdx.math.Interpolation
import com.fatih.kingsofpigs.ecs.component.LightComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.IteratingSystem

@AllOf([LightComponent::class])
class LightSystem(
    private val rayHandler: RayHandler,
    private val lightComps: ComponentMapper<LightComponent>
) : IteratingSystem(){

    private var ambientTransitionTime = 0f
    private var ambientColorFrom = dayAmbientLight
    private var ambientColorTo = nightAmbientLight
    var ambientColor = Color(0f,0f,0f,0f)
    var currentNightAmbient = nightAmbientLight
    private var alreadySet = false

    override fun onTick() {
        super.onTick()
        if (isLightsOn){
            alreadySet = false
            if (ambientTransitionTime >= 1f){
                ambientTransitionTime = 0f
                if (ambientColorFrom == dayAmbientLight){
                    ambientColorFrom = nightAmbientLight
                    ambientColorTo = dayAmbientLight
                }else{
                    ambientColorFrom = dayAmbientLight
                    ambientColorTo = nightAmbientLight
                }
            }else{
                if (currentNightAmbient != nightAmbientLight){
                    ambientColorTo = nightAmbientLight
                    currentNightAmbient = nightAmbientLight
                }
                setAmbientColor()
                ambientTransitionTime += deltaTime * 0.5F
            }
            rayHandler.setAmbientLight(ambientColor)
        }else{
            if (!alreadySet){
                rayHandler.setAmbientLight(Color.WHITE)
                alreadySet = true
            }
        }
    }


    override fun onTickEntity(entity: Entity) {
        val lightComponent = lightComps[entity]
        lightComponent.run {
            distanceTime = (distanceTime + distanceDirection * deltaTime).coerceIn(0f,1f)
            if (distanceTime == 0f || distanceTime == 1f){
                distanceDirection *= -1
            }
            light.distance = interpolation.apply(distance.start,distance.endInclusive,distanceTime)
        }
    }

    private fun setAmbientColor(){
        ambientColor.set(
            interpolation.apply(ambientColorFrom.r,ambientColorTo.r,ambientTransitionTime),
            interpolation.apply(ambientColorFrom.g,ambientColorTo.g,ambientTransitionTime),
            interpolation.apply(ambientColorFrom.b,ambientColorTo.b,ambientTransitionTime),
            interpolation.apply(ambientColorFrom.a,ambientColorTo.a,ambientTransitionTime),
        )
    }

    companion object{
        private val interpolation = Interpolation.smoother
        private val dayAmbientLight = Color.WHITE
        var nightAmbientLight = Color.WHITE
        var isLightsOn : Boolean = false
    }
}
