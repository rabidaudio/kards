package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.utils.cartesianProduct
import audio.rabid.kards.gofish.models.PlayerName

class ProbabilityTracker(players: List<PlayerName>, cardCounts: IntArray) : ArrayHelper<PlayerName, Rank> {

    override val rows: List<PlayerName> = players
    override val columns: List<Rank> get() = Rank.ALL

    // convert integer number of cards to doubles for scoring
    private val probabilityTable = DoubleArray(cardCounts.size) { i -> cardCounts[i].toDouble() }

    fun addProbability(playerName: PlayerName, rank: Rank, probability: Double) {
        probabilityTable[getIndex(playerName, rank)] += probability
    }

    fun getMaxProbability(rankOptions: List<Rank>, playerOptions: List<PlayerName>): Pair<PlayerName, Rank> =
        playerOptions.cartesianProduct(rankOptions)
            .map { i -> i to probabilityTable[getIndex(i.first, i.second)] }
            .maxBy { (_, p) -> p }!!.first

    fun getProbability(playerName: PlayerName, rank: Rank): Double = probabilityTable[getIndex(playerName, rank)]

    @SuppressWarnings("ComplexMethod") // TODO
    fun printTable(oustanding: List<Rank>, probabilityInDeck: (Rank) -> Double) {
        print(StringBuilder().apply {
            val maxNameLength = rows.map { it.name.length }.max()!!
            append("".padStart(maxNameLength)).append(" \t")
            for (rank in columns) {
                append(rank.shortName.padStart(7))
            }
            appendln()
            append("deck".padStart(maxNameLength)).append(":\t")
            for (rank in columns) {
                if (oustanding.contains(rank)) {
                    append(String.format("%7.2f", probabilityInDeck(rank)))
                } else {
                    append("".padStart(7))
                }
            }
            append(String.format("%7.2f", columns.sumByDouble(probabilityInDeck)))
            appendln()
            for (player in rows) {
                append(player.name.padStart(maxNameLength))
                append(":\t")
                for (rank in columns) {
                    if (oustanding.contains(rank)) {
                        append(String.format("%7.2f", probabilityTable[getIndex(player, rank)]))
                    } else {
                        append("".padStart(7))
                    }
                }
                append(String.format("%7.2f", columns.sumByDouble { probabilityTable[getIndex(player, it)] }))
                appendln()
            }
            append("".padStart(maxNameLength)).append(" \t")
            for (rank in columns) {
                if (oustanding.contains(rank)) {
                    val p = probabilityInDeck(rank) + rows.sumByDouble { probabilityTable[getIndex(it, rank)] }
                    append(String.format("%7.2f", p))
                } else {
                    append("".padStart(7))
                }
            }
            appendln()
        }.toString())
    }
}
