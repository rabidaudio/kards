package audio.rabid.kards.gofish.ui

import audio.rabid.kards.core.deck.standard.ui.pluralize
import audio.rabid.kards.gofish.models.Game
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver

object ConsoleUI : UI {

    override fun draw(game: Game) {
        when {
            game.pastMoves.isEmpty() -> {
                println("Let's get started! We have ${game.players.size} players:")
                for ((i, player) in game.players.withIndex()) {
                    println("${i + 1}: ${player.name.name}")
                }
                for (player in game.players) {
                    if (player.books.isNotEmpty()) {
                        println("${player.name.name} started with ${player.books.size} books! ${player.books.joinToString(", ") { it.rank.name }} are out")
                    }
                }
            }
            game.isOver -> {
                println("Game Over!")
                val winners = game.winners
                val bookSize = game.getPlayer(winners.first()).books.size
                if (winners.size > 1) {
                    println("We have a ${winners.size}-way tie with $bookSize books!")
                    val winnerNames = winners.joinToString(", ") { it.name }
                    println("Winners: $winnerNames")
                } else {
                    println("Winner: ${winners.first().name}")
                }
                val longestNameSize = game.players.map { it.name.name.length }.max()!!
                game.players.sortedByDescending { it.books.size }.forEach { player ->
                    println("${player.name.name.padStart(longestNameSize)}: ${player.books.size}")
                }
            }
            else -> {
                val lastMove = game.pastMoves.last()
                println("${lastMove.player.name}: Hey, ${lastMove.move.from.name}, " +
                        "got any ${lastMove.move.askFor.pluralize()}?")
                when (lastMove.result) {
                    GoFish -> {
                        println("\t${lastMove.move.from.name}: Go Fish!")
                        if (!lastMove.turnEnded) {
                            println("${lastMove.player.name}: I drew a ${lastMove.move.askFor}! I go again!")
                        }
                    }
                    is HandOver -> println("\t${lastMove.move.from.name}: Ugh, I have ${lastMove.result.cards.size}!")
                }
                lastMove.newBook?.let { rank -> println("${lastMove.player.name}: I put away the $rank book! " +
                        "My score is ${game.getPlayer(lastMove.player).books.size}") }
            }
        }
    }
}
