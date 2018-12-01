package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.gofish.ai.TurnInfo
import audio.rabid.kards.gofish.models.Move

/**
 * Just pick the move with the highest score
 */
class SimpleCardCounterAI : CardCounterAi() {
    override fun pickMove(turnInfo: TurnInfo, scorer: Scorer): Move =
        turnInfo.possibleMoves.maxBy { (r, p) -> scorer.getScore(p, r) }!!
}
