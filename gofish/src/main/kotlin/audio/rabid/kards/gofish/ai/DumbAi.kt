package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult
import kotlin.random.Random

/**
 * This is a dumb GoFish player which just picks legal moves at random
 */
class DumbAi(private val random: Random) : MovePicker {

    override fun onGameStarted(
        playerNames: List<PlayerName>,
        myPlayerName: PlayerName,
        myHand: Set<Card>,
        bookedAtStart: Map<PlayerName, Set<Rank>>
    ) {}

    override fun onTurnCompleted(turnResult: TurnResult, myHand: Set<Card>) {}

    override fun move(turnInfo: TurnInfo): Move = turnInfo.possibleMoves.random(random)
}
