package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.gofish.models.PastMove
import audio.rabid.kards.gofish.models.PlayerName

typealias ReadOnlyHand = Set<Card>

typealias ReadOnlyBook = Set<Card>

data class GameInfo(
        val myHand: ReadOnlyHand,
        val myBooks: Set<ReadOnlyBook>,
        val deckSize: Int,
        val pastMoves: List<PastMove>,
        val otherPlayers: List<OtherPlayerInfo>
) {
    data class OtherPlayerInfo(val playerName: PlayerName, val handSize: Int, val books: Set<ReadOnlyBook>)
}
