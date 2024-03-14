package com.fatih.kingsofpigs.state

import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram

interface DefaultState : State<PlayerEntity> {
    override fun enter(entity: PlayerEntity) {

    }

    override fun update(entity: PlayerEntity) {

    }

    override fun exit(entity: PlayerEntity) {

    }

    override fun onMessage(entity: PlayerEntity?, telegram: Telegram?) = false
}
