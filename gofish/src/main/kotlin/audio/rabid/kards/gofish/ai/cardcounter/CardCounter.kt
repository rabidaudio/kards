package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.PlayerName

class CardCounter(players: List<PlayerName>) : ArrayHelper<PlayerName, Rank> {

    override val rows: List<PlayerName> = players
    override val columns: List<Rank> get() = Rank.ALL

    private val cardCountTable = IntArray(totalSize)

    private fun get(playerName: PlayerName, rank: Rank): Int = cardCountTable[getIndex(playerName, rank)]

    private fun set(playerName: PlayerName, rank: Rank, value: Int) {
        cardCountTable[getIndex(playerName, rank)] = value
    }

    fun setNone(playerName: PlayerName, rank: Rank) = set(playerName, rank, 0)

    fun setAtLeastOne(playerName: PlayerName, rank: Rank) {
        if (get(playerName, rank) == 0) set(playerName, rank, 1)
    }

    fun add(playerName: PlayerName, rank: Rank, count: Int) =
        set(playerName, rank, get(playerName, rank) + count)

    fun setExactly(playerName: PlayerName, rank: Rank, count: Int) = set(playerName, rank, count)

    fun knownInstancesOf(rank: Rank): Int = rows.sumBy { get(it, rank) }

    fun knownHandSize(playerName: PlayerName) = columns.sumBy { get(playerName, it) }

    fun generateBaseScores(): ProbabilityTracker = ProbabilityTracker(rows, cardCountTable) // TODO
}
