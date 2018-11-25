package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.PastMove
import audio.rabid.kards.gofish.models.PlayerName

typealias ReadOnlyHand = Set<Card>

data class GameInfo(
        val myPlayerName: PlayerName,
        val myHand: ReadOnlyHand,
        val players: List<PlayerInfo>,
        val pastMoves: List<PastMove>
) {

    data class PlayerInfo(val playerName: PlayerName, val handSize: Int, val books: Set<Rank>)
}
