package audio.rabid.kards.gofish.ui

import audio.rabid.kards.core.deck.standard.ui.pluralize
import audio.rabid.kards.gofish.models.Game
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.PastMove

object ConsoleUI : UI {

    override fun draw(game: Game) = Drawer(game).draw()

    class Drawer(private val game: Game) {

        fun draw() {
            when {
                game.pastMoves.isEmpty() -> drawGameStart()
                game.isOver -> drawGameEnd()
                else -> drawMove(game.pastMoves.last())
            }
        }

        private fun drawGameStart() {
            println("Let's get started! We have ${game.players.size} players:")
            for ((i, player) in game.players.withIndex()) {
                println("${i + 1}: ${player.name.name}")
            }
            for (player in game.players) {
                if (player.books.isNotEmpty()) {
                    val removedBooks = player.books.joinToString(", ") { it.rank.name }
                    println("${player.name.name} started with ${player.books.size} books! $removedBooks are out")
                }
            }
        }

        private fun drawMove(lastMove: PastMove) {
            val player = lastMove.player.name
            val target = lastMove.move.from.name
            println("$player: Hey, $target, got any ${lastMove.move.askFor.pluralize()}?")
            when (lastMove.result) {
                GoFish -> {
                    println("\t$target: Go Fish!")
                    if (!lastMove.turnEnded) {
                        println("$player: I drew a ${lastMove.move.askFor}! I go again!")
                    }
                }
                is HandOver -> println("\t$target: Ugh, I have ${lastMove.result.cards.size}!")
            }
            lastMove.newBook?.let { rank ->
                val score = game.getPlayer(lastMove.player).books.size
                println("$player: I put away the $rank book! My score is $score")
            }
        }

        private fun drawGameEnd() {
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
    }
}
