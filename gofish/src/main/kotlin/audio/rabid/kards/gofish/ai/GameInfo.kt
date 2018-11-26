package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.utils.cartesianProduct
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PastMove
import audio.rabid.kards.gofish.models.PlayerName

data class GameInfo(
    val myPlayerName: PlayerName,
    val myHand: Set<Card>,
    val players: List<PlayerInfo>,
    val initialBooks: Map<PlayerName, Set<Rank>>,
    val pastMoves: List<PastMove>
) {
    data class PlayerInfo(val playerName: PlayerName, val handSize: Int)

    val playerNames: List<PlayerName>
        get() = players.map { it.playerName }

    val otherPlayerNames: List<PlayerName>
        get() = playerNames - myPlayerName

    val otherPlayers: List<GameInfo.PlayerInfo>
        get() = players.filter { it.playerName != myPlayerName }

    private val myRanks: Set<Rank>
        get() = myHand.map { it.rank }.toSet()

    val possibleMoves: List<Move>
        get() = otherPlayerNames.cartesianProduct(myRanks).map { (p, r) -> Move(askFor = r, from = p) }

    val completedBooks: Set<Rank>
        get() = (initialBooks.flatMap { it.value } + pastMoves.mapNotNull { it.newBook }).toSet()

    val oceanSize: Int
        get() = 52 - (4 * completedBooks.size) - players.sumBy { it.handSize }
}
