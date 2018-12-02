package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.ai.TurnInfo
import audio.rabid.kards.gofish.models.Move

/**
 * Just pick the move with the highest score
 */
class SimpleCardCounterAI : CardCounterAi(), DebuggableAI {

    override fun pickMove(turnInfo: TurnInfo): Move {
        val scorer = turnInfo.getScorer()
        return turnInfo.possibleMoves.maxBy { scorer.getScore(it) }!!
    }

    override fun debug(turnInfo: TurnInfo) {
        val scorer = turnInfo.getScorer()
        val playerScores = getPlayerNames().associateWith { p ->
            Rank.ALL.associateWith { r -> scorer.getScore(Move(askFor = r, from = p)) }
        }
        ScorePrinter(outstandingRanks, playerScores) { String.format("%7.2f", it) }.print()
    }

    private fun TurnInfo.getScorer() = Scorer(getCardCounter(), oceanSize, allPlayerHandSizes)
}
