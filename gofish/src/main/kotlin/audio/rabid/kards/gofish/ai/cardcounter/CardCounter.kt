package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.PlayerName

class CardCounter(val players: List<PlayerName>) : ArrayHelper<PlayerName, Rank> {

    companion object {
        private val POSSIBLE_RANGE = 0..3
    }

    override val rows: List<PlayerName> = players
    override val columns: List<Rank> get() = Rank.ALL

    private val cardCountTable: Array<IntRange> = Array(totalSize) { POSSIBLE_RANGE }

    private fun get(playerName: PlayerName, rank: Rank): IntRange = cardCountTable[getIndex(playerName, rank)]

    private fun set(playerName: PlayerName, rank: Rank, range: IntRange) {
        cardCountTable[getIndex(playerName, rank)] = range.clampTo(POSSIBLE_RANGE)
    }

    fun getMin(playerName: PlayerName, rank: Rank): Int = get(playerName, rank).first

    fun getMax(playerName: PlayerName, rank: Rank): Int = get(playerName, rank).endInclusive

    fun setMin(playerName: PlayerName, rank: Rank, min: Int) =
        set(playerName, rank, min..getMax(playerName, rank))

    fun setMax(playerName: PlayerName, rank: Rank, max: Int) =
        set(playerName, rank, getMin(playerName, rank)..max)

    private fun IntRange.clampTo(outerRange: IntRange): IntRange =
        first.coerceAtLeast(outerRange.first)..endInclusive.coerceAtMost(outerRange.endInclusive)

    fun clone(): CardCounter = CardCounter(players).also { copy ->
        for ((index, value) in this.cardCountTable.withIndex()) {
            copy.cardCountTable[index] = value
        }
    }
}

fun CardCounter.setNone(playerName: PlayerName, rank: Rank) = setExactly(playerName, rank, 0)

fun CardCounter.setAtLeastOne(playerName: PlayerName, rank: Rank) {
    if (getMin(playerName, rank) == 0) setMin(playerName, rank, 1)
}

fun CardCounter.addExactly(playerName: PlayerName, rank: Rank, count: Int) {
    setMin(playerName, rank, getMin(playerName, rank) + count)
    setMax(playerName, rank, getMax(playerName, rank) + count)
}

fun CardCounter.setExactly(playerName: PlayerName, rank: Rank, count: Int) {
    setMin(playerName, rank, count)
    setMax(playerName, rank, count)
}

fun CardCounter.addPossiblyOneMore(playerName: PlayerName, rank: Rank) =
    setMax(playerName, rank, getMax(playerName, rank) + 1)

fun CardCounter.getMinMax(playerName: PlayerName, rank: Rank): Pair<Int, Int> =
    getMin(playerName, rank) to getMax(playerName, rank)

fun CardCounter.getDifferential(playerName: PlayerName, rank: Rank) =
    getMax(playerName, rank) - getMin(playerName, rank)
