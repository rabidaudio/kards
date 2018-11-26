package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.utils.cartesianProduct
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PlayerName

data class TurnInfo(
    val myHand: Set<Card>,
    val oceanSize: Int,
    val otherPlayerHandSizes: Map<PlayerName, Int>
) {

    val otherPlayerNames: Set<PlayerName>
        get() = otherPlayerHandSizes.keys

    val possibleMoves: List<Move>
        get() = otherPlayerNames.cartesianProduct(myRanks).map { (p, r) -> Move(askFor = r, from = p) }

    private val myRanks: Set<Rank>
        get() = myHand.map { it.rank }.toSet()
}
