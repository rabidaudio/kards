package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.PastMove
import audio.rabid.kards.gofish.models.PlayerName

typealias ReadOnlyHand = Set<Card>

data class GameInfo(
        val myPlayerName: PlayerName,
        val myHand: ReadOnlyHand,
        val myBooks: Set<Rank>,
        val deckSize: Int,
        val pastMoves: List<PastMove>,
        val otherPlayers: List<OtherPlayerInfo>
) {
    data class OtherPlayerInfo(val playerName: PlayerName, val handSize: Int, val books: Set<Rank>)
}
