package audio.rabid.kards.gofish.ai

import audio.rabid.kards.gofish.models.Move

interface MovePicker {

    fun move(gameInfo: GameInfo): Move
}
