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


    override fun onTick() {
        super.onTick()
        if (isLightsOn){
            if (ambientTransitionTime >= 2f){
                ambientTransitionTime = 0f
                if (ambientColorFrom == dayAmbientLight){
                    ambientColorFrom = nightAmbientLight
                    ambientColorTo = dayAmbientLight
                }else{
                    ambientColorFrom = dayAmbientLight
                    ambientColorTo = nightAmbientLight
                }
            }else{
                setAmbientColor()
                ambientTransitionTime += deltaTime * 0.6f
            }
            rayHandler.setAmbientLight(ambientColor)
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
        private val nightAmbientLight = Color.BLACK
        var isLightsOn : Boolean = false
        var ambientColor = Color.WHITE

    }
}
