package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.PlayerName

class ScorePrinter(
    private val ranksInPlay: List<Rank>,
    private val playerScores: Map<PlayerName, Map<Rank, Double>>
) {

    private val maxNameLength = playerScores.map { it.key.name.length }.max()!!

    fun print() {
        printLine("") { it.shortName.fixedSize() }
        for ((playerName, scores) in playerScores) {
            printLine(playerName.name, scores.values.sum().fixedSize()) { rank ->
                if (ranksInPlay.contains(rank)) scores[rank]!!.fixedSize() else "".fixedSize()
            }
        }
    }

    private fun printLine(name: String, finalColumnValue: String? = null, values: (Rank) -> String) {
        print(name.padStart(maxNameLength))
        print(": \t")
        for (rank in Rank.ALL) {
            print(values.invoke(rank))
        }
        finalColumnValue?.let { print(it) }
        println()
    }

    private fun Double.fixedSize() = String.format("%7.2f", this)

    private fun String.fixedSize() = padStart(7)
}