package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.gofish.ai.TurnInfo

interface DebuggableAi {

    fun debug(turnInfo: TurnInfo)
}
