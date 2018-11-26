package audio.rabid.kards.gofish.ui

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.ui.pluralize
import audio.rabid.kards.gofish.models.Game
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult

object ConsoleUI : UI {

    override fun onGameStarted(playerNames: List<PlayerName>, bookedAtStart: Map<PlayerName, Set<Rank>>) {
        println("Let's get started! We have ${playerNames.size} players:")
        for ((i, player) in playerNames.withIndex()) {
            println("${i + 1}: ${player.name}")
        }
        for ((player, books) in bookedAtStart) {
            if (books.isEmpty()) continue
            val removedBooks = books.joinToString(", ")
            println("${player.name} started with ${books.size} books! $removedBooks are out")
        }
    }

    override fun onTurnCompleted(turnResult: TurnResult, scores: Map<PlayerName, Set<Rank>>) {
        val player = turnResult.player.name
        val target = turnResult.move.from.name
        println("$player: Hey, $target, got any ${turnResult.move.askFor.pluralize()}?")
        when (turnResult.result) {
            GoFish -> {
                println("\t$target: Go Fish!")
                if (!turnResult.turnEnded) {
                    println("$player: I drew a ${turnResult.move.askFor}! I go again!")
                }
            }
            is HandOver -> println("\t$target: Ugh, I have ${turnResult.result.cards.size}!")
        }
        turnResult.newBook?.let { rank ->
            val score = scores[turnResult.player]!!.size
            println("$player: I put away the $rank book! My score is $score")
        }
    }

    override fun onGameEnded(winners: Set<PlayerName>, scores: Map<PlayerName, Set<Rank>>) {
        println("Game Over!")
        val bookSize = scores[winners.first()]!!.size
        if (winners.size > 1) {
            println("We have a ${winners.size}-way tie with $bookSize books!")
            val winnerNames = winners.joinToString(", ") { it.name }
            println("Winners: $winnerNames")
        } else {
            println("Winner: ${winners.first().name}")
        }
        val longestNameSize = scores.keys.map { it.name.length }.max()!!
        scores.entries.sortedByDescending { it.value.size }.forEach { (player, books) ->
            println("${player.name.padStart(longestNameSize)}: ${books.size}")
        }
    }
}
