package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.PlayerName

class ScorePrinter<T>(
    private val ranksInPlay: List<Rank>,
    private val playerScores: Map<PlayerName, Map<Rank, T>>,
    private val block: (T) -> String
) {

    private val maxFieldSize = playerScores.values.flatMap { it.values }.map { block.invoke(it).length }.max()!! + 4

    private val maxNameLength = playerScores.map { it.key.name.length }.max()!!

    fun print() {
        printLine("") { it.shortName.fixedSize() }
        for ((playerName, scores) in playerScores) {
            printLine(playerName.name) { rank ->
                (if (ranksInPlay.contains(rank)) block.invoke(scores[rank]!!) else "").fixedSize()
            }
        }
    }

    private fun printLine(name: String, values: (Rank) -> String) {
        print(name.padStart(maxNameLength))
        print(": \t")
        for (rank in Rank.ALL) {
            print(values.invoke(rank))
        }
        println()
    }

    private fun String.fixedSize() = padStart(maxFieldSize)
}
