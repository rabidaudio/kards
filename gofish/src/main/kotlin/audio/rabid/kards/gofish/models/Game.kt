package audio.rabid.kards.gofish.models

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.ai.GameInfo

data class Game(
    val ocean: Ocean,
    val players: List<Player>,
    var currentPlayerName: PlayerName = players.first().name,
    val pastMoves: MutableList<PastMove> = mutableListOf()
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

    fun getGameInfo(playerName: PlayerName): GameInfo = GameInfo(
        myPlayerName = playerName,
        myHand = getPlayer(playerName).hand.immutableCopy(),
        pastMoves = pastMoves,
        initialBooks = initialBooks,
        players = players.map { it.info }
    )

    val nextPlayerName: PlayerName get() = players[(players.indexOf(currentPlayer) + 1) % players.size].name

    private val allBooks: List<Pair<PlayerName, Book>> = players.flatMap { p -> p.books.map { p.name to it } }

    private val initialBooks: Map<PlayerName, Set<Rank>> = allBooks
        .filter { (_, b) -> !pastMoves.any { b.rank == it.newBook } }
        .groupBy { (n, _) -> n }
        .mapValues { (_, p) -> p.map { (_, b) -> b.rank }.toSet() }
}
