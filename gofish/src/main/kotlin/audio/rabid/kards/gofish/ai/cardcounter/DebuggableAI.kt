package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.gofish.ai.TurnInfo

interface DebuggableAI {

    fun debug(turnInfo: TurnInfo)
}
