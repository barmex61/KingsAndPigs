package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import com.fatih.kingsofpigs.ecs.component.AiComponent
import com.fatih.kingsofpigs.ecs.component.AttackComponent
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.MoveComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.AI_CIRCLE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.CANT_DEAL_DAMAGE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.DEAL_DAMAGE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.PLATFORM_FIXTURE
import com.fatih.kingsofpigs.ecs.component.PlayerComponent
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.component1
import ktx.math.component2

@AllOf([PhysicComponent::class,ImageComponent::class])
class PhysicSystem (
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val attackComps : ComponentMapper<AttackComponent>,
    private val lifeComps : ComponentMapper<LifeComponent>,
    private val aiComps : ComponentMapper<AiComponent>,
    private val box2dWorld: World,
): IteratingSystem(interval = Fixed(1/60f)) , ContactListener{

    init {
        box2dWorld.setContactListener(this)
    }

    override fun onUpdate() {
        if (box2dWorld.autoClearForces){
            box2dWorld.autoClearForces = false
        }
        super.onUpdate()
        box2dWorld.clearForces()
    }

    override fun onTick() {
        super.onTick()
        box2dWorld.step(deltaTime,6,2)
    }

    override fun onTickEntity(entity: Entity) {
        physicComps[entity].run {
            previousPos.set(body.position)
            if (!impulse.isZero){
                body.applyLinearImpulse(impulse,body.worldCenter,true)
                impulse.setZero()
            }
        }
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        val physicComponent = physicComps[entity]
        val imageComponent = imageComps[entity]
        val physicOffset = physicComponent.bodyOffset
        val (prevX,prevY) = physicComponent.previousPos
        val (bodyX,bodyY) = physicComponent.body.position
        val width = imageComponent.image.width
        val height = imageComponent.image.height
        val flipX = imageComponent.image.flipX
        imageComponent.image.setPosition(
            MathUtils.lerp(prevX,bodyX,alpha) - width/2f + if (flipX) physicOffset.x *2f else 0f ,
            MathUtils.lerp(prevY,bodyY,alpha) - height/2f
        )
    }

    private fun Fixture.entity() = this.body.userData as Entity
    private fun Fixture.isAttackObject() = this.filterData.categoryBits == Constants.ATTACK_OBJECT
    private fun Fixture.isEnemy() = this.filterData.categoryBits == Constants.ENEMY
    private fun Fixture.canDealDamage() = this.userData == DEAL_DAMAGE
    private fun Fixture.isAiCircle() = this.userData == AI_CIRCLE


    override fun beginContact(contact: Contact) {
        if (contact.fixtureA.isAiCircle() && contact.fixtureB.filterData.categoryBits == Constants.KING){
            aiComps[contact.fixtureA.entity()].nearbyEntities += contact.fixtureB.entity()
        }
        if (contact.fixtureB.isAiCircle() && contact.fixtureA.filterData.categoryBits == Constants.KING){
            aiComps[contact.fixtureB.entity()].nearbyEntities += contact.fixtureA.entity()
        }

        if (contact.fixtureA.isAttackObject() && contact.fixtureA.canDealDamage()){
            if (contact.fixtureB.isEnemy()){
                contact.fixtureA.userData = CANT_DEAL_DAMAGE
                lifeComps[contact.fixtureB.entity()].apply {
                    damageTaken = attackComps[contact.fixtureA.entity()].run {
                        isCrit = (1..100).random() <= critChance
                        attackDamage * if (isCrit) critDamage else 1f
                    }
                }
            }
        }
        if (contact.fixtureB.isAttackObject() && contact.fixtureB.canDealDamage()){
            if (contact.fixtureA.isEnemy()){
                contact.fixtureB.userData = CANT_DEAL_DAMAGE
                lifeComps[contact.fixtureA.entity()].apply {
                    damageTaken = attackComps[contact.fixtureB.entity()].run {
                        isCrit = (1..100).random() <= critChance
                        attackDamage * if (isCrit) critDamage else 1f
                    }
                }
            }
        }
    }

    override fun endContact(contact: Contact) {
        if (contact.fixtureA.isAiCircle() && contact.fixtureB.filterData.categoryBits == Constants.KING){
            aiComps.getOrNull(contact.fixtureA.entity())?.nearbyEntities?.remove(contact.fixtureB.entity())
        }
        if (contact.fixtureB.isAiCircle() && contact.fixtureA.filterData.categoryBits == Constants.KING){
            aiComps.getOrNull(contact.fixtureB.entity())?.nearbyEntities?.remove(contact.fixtureA.entity())
        }
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold?) {

        if (contact.fixtureA.userData == PLATFORM_FIXTURE){
            physicComps.getOrNull(contact.fixtureB.entity())?.run {
                contact.isEnabled = body.linearVelocity.y <= 0.0001f
            }
        }
        if (contact.fixtureB.userData == PLATFORM_FIXTURE){
            physicComps.getOrNull(contact.fixtureA.entity())?.run {
                contact.isEnabled = body.linearVelocity.y <= 0.0001f
            }
        }
    }
    override fun postSolve(contact: Contact, impulse: ContactImpulse) = Unit
}
