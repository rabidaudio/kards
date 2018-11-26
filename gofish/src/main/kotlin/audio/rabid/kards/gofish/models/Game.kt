package audio.rabid.kards.gofish.models

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.ai.TurnInfo

data class Game(
    val ocean: Ocean,
    val players: List<Player>,
    var currentPlayerName: PlayerName = players.first().name
) {

    init {
        if (players.isEmpty()) throw IllegalStateException("No Players!")
    }

    fun getPlayer(name: PlayerName) = players.first { it.name == name }

    val isOver: Boolean get() = ocean.isEmpty() || players.any { it.hand.isEmpty() }

    val currentPlayer: Player get() = getPlayer(currentPlayerName)

    val winners: Set<PlayerName>
        get() = players.mapNotNull { player -> player.name.takeIf { player.books.size == highestBookSize } }.toSet()

    private val highestBookSize: Int get() = players.map { it.books.size }.max()!!

    val scores: Map<PlayerName, Set<Rank>>
        get() = players.associate { p -> p.name to p.books.map(Book::rank).toSet() }

    fun getTurnInfo(playerName: PlayerName): TurnInfo = TurnInfo(
        myHand = getPlayer(playerName).hand.immutableCopy(),
        oceanSize = ocean.size,
        otherPlayerHandSizes = players.filter { it.name != playerName }.associate { it.name to it.hand.size }
    )

    val playerNames: List<PlayerName> = players.map(Player::name)

    val nextPlayerName: PlayerName get() = players[(players.indexOf(currentPlayer) + 1) % players.size].name
}
