package com.fatih.kingsofpigs.ecs.system

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.kingsofpigs.ecs.component.AiComponent
import com.fatih.kingsofpigs.ecs.component.DestroyableComponent
import com.fatih.kingsofpigs.ecs.component.EntityModel
import com.fatih.kingsofpigs.ecs.component.ImageComponent
import com.fatih.kingsofpigs.ecs.component.ItemComponent
import com.fatih.kingsofpigs.ecs.component.LifeComponent
import com.fatih.kingsofpigs.ecs.component.MeleeAttackComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.AI_CIRCLE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.CANT_DEAL_DAMAGE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.DEAL_DAMAGE
import com.fatih.kingsofpigs.ecs.component.PhysicComponent.Companion.PLATFORM_FIXTURE
import com.fatih.kingsofpigs.ecs.component.RangeAttackComponent
import com.fatih.kingsofpigs.event.ShowPortalDialogEvent
import com.fatih.kingsofpigs.event.fireEvent
import com.fatih.kingsofpigs.input.KeyboardInputProcessor
import com.fatih.kingsofpigs.utils.Constants
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import ktx.math.component1
import ktx.math.component2
import kotlin.math.abs

@AllOf([PhysicComponent::class,ImageComponent::class])
class PhysicSystem (
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val imageComps : ComponentMapper<ImageComponent>,
    private val meleeAttackComps : ComponentMapper<MeleeAttackComponent>,
    private val rangeAttackComps : ComponentMapper<RangeAttackComponent>,
    private val lifeComps : ComponentMapper<LifeComponent>,
    private val aiComps : ComponentMapper<AiComponent>,
    private val destroyableComps : ComponentMapper<DestroyableComponent>,
    private val itemComps : ComponentMapper<ItemComponent>,
    private val gameStage : Stage,
    private val box2dWorld: World,
): IteratingSystem(interval = Fixed(1/300f)) , ContactListener{

    init {
        box2dWorld.setContactListener(this)
    }

    lateinit var inputProcessor: KeyboardInputProcessor

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
                if (abs(body.linearVelocity.x) > 6f) impulse.x = 0f
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
    private fun Fixture.isPlayer() = this.filterData.categoryBits == Constants.KING
    private fun Fixture.canDealDamage() = this.userData == DEAL_DAMAGE
    private fun Fixture.cantDealDamage() = this.userData == CANT_DEAL_DAMAGE
    private fun Fixture.isAiCircle() = this.userData == AI_CIRCLE
    private fun Fixture.isDestroyable() = this.filterData.categoryBits == Constants.DESTROYABLE
    private fun Fixture.isItem() = this.filterData.categoryBits == Constants.ITEM
    private fun Fixture.isPortal() = this.filterData.categoryBits == Constants.PORTAL

    private fun LifeComponent.dealDamage(attackEntity: Entity) {
        if (attackEntity in meleeAttackComps){
            meleeAttackComps[attackEntity].run {
                val isCrit = (1..100).random() <= critChance
                this@dealDamage.isCrit = isCrit
                damageTaken =  attackDamage * if (isCrit) critDamage else 1f
            }
        }else {
            rangeAttackComps[attackEntity].run {
                if (attackBody != null && entityModel == EntityModel.PIG_BOX && attackBody!!.linearVelocity.len2() < 50f){
                    0f
                }else{
                    val isCrit = (1..100).random() <= critChance
                    this@dealDamage.isCrit = isCrit
                    damageTaken = attackDamage * if (isCrit) critDamage else 1f
                }
            }
        }
    }

    override fun beginContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val fixtureB = contact.fixtureB
        if (fixtureA.isPortal() && fixtureB.isPlayer()){
            if (world.family(allOf = arrayOf(LifeComponent::class)).numEntities == 1){
                inputProcessor.attackComponent = null
                inputProcessor.moveComponent = null
                world.system<PortalSystem>().changeMap = true
                world.system<PortalSystem>().portalPath = (fixtureA.userData as String)
            }else{
                gameStage.fireEvent(ShowPortalDialogEvent("You need to kill all enemies first"))
            }
        }
        if (fixtureB.isPortal() && fixtureA.isPlayer()){
            if (world.family(allOf = arrayOf(LifeComponent::class)).numEntities == 1){
                inputProcessor.attackComponent = null
                inputProcessor.moveComponent = null
                world.system<PortalSystem>().changeMap = true
                world.system<PortalSystem>().portalPath = (fixtureB.userData as String)
            }else{
                gameStage.fireEvent(ShowPortalDialogEvent("You need to kill all enemies first"))
            }
        }

        if (fixtureA.isPlayer() && fixtureB.isAttackObject() && fixtureB.cantDealDamage() && fixtureB.entity() in rangeAttackComps && rangeAttackComps[fixtureB.entity()].entityModel == EntityModel.PIG_LIGHT){
            rangeAttackComps[fixtureB.entity()].destroyBodyTime = rangeAttackComps[fixtureB.entity()].maxDestroyBodyTime*2f/3f
        }
        if (fixtureB.isPlayer() && fixtureA.isAttackObject() && fixtureA.cantDealDamage() && fixtureA.entity() in rangeAttackComps && rangeAttackComps[fixtureA.entity()].entityModel == EntityModel.PIG_LIGHT){
            rangeAttackComps[fixtureA.entity()].destroyBodyTime = rangeAttackComps[fixtureA.entity()].maxDestroyBodyTime*2f/3f
        }

        if (fixtureA.isPlayer() && fixtureB.isItem()){
            itemComps[fixtureB.entity()].collideEntity = fixtureA.entity()
        }
        if (fixtureB.isPlayer() && fixtureA.isItem()){
            itemComps[fixtureA.entity()].collideEntity = fixtureB.entity()
        }
        if (fixtureA.isAttackObject() && fixtureB.isDestroyable()){
            destroyableComps[fixtureB.entity()].run {
                if (!destroy){
                    createBodies = true
                }
            }
        }
        if (fixtureB.isAttackObject() && fixtureA.isDestroyable()){
            destroyableComps[fixtureA.entity()].run {
                if (!destroy){
                    createBodies = true
                }
            }
        }
        if (fixtureA.isAiCircle() && fixtureB.filterData.categoryBits == Constants.KING){
            aiComps[fixtureA.entity()].nearbyEntities += fixtureB.entity()
        }
        if (fixtureB.isAiCircle() && fixtureA.filterData.categoryBits == Constants.KING){
            aiComps[fixtureB.entity()].nearbyEntities += fixtureA.entity()
        }

        if (fixtureA.isAttackObject() && fixtureA.canDealDamage()){
            if (fixtureB.isEnemy() || fixtureB.isPlayer()){
                fixtureA.userData = CANT_DEAL_DAMAGE
                if (fixtureA.filterData.groupIndex == 1.toShort()) fixtureA.isSensor = true
                lifeComps[fixtureB.entity()].dealDamage(fixtureA.entity())
            }
        }
        if (fixtureB.isAttackObject() && fixtureB.canDealDamage()){
            if (fixtureA.isEnemy() || fixtureA.isPlayer()){
                fixtureB.userData = CANT_DEAL_DAMAGE
                if (fixtureB.filterData.groupIndex == 1.toShort()) fixtureB.isSensor = true
                lifeComps[fixtureA.entity()].dealDamage(fixtureB.entity())
            }
        }
    }

    override fun endContact(contact: Contact) {
        val fixtureA = contact.fixtureA
        val fixtureB = contact.fixtureB
        if (fixtureA.isAiCircle() && fixtureB.filterData.categoryBits == Constants.KING){
            aiComps.getOrNull(fixtureA.entity())?.nearbyEntities?.remove(fixtureB.entity())
        }
        if (fixtureB.isAiCircle() && fixtureA.filterData.categoryBits == Constants.KING){
            aiComps.getOrNull(fixtureB.entity())?.nearbyEntities?.remove(fixtureA.entity())
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
