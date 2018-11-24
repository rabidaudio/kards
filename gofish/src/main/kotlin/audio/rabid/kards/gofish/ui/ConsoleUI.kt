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
                game.players.forEachIndexed { index, player -> println("${index + 1}: ${player.name.name}") }
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
                    print("Winner: ${winners.first().name} with $bookSize books")
                }
            }
            else -> {
                val lastMove = game.pastMoves.last()
                println("${lastMove.player.name}: Hey, ${lastMove.move.from.name}, " +
                        "got any ${lastMove.move.askFor.pluralize()}?")
                when (lastMove.result) {
                    GoFish -> {
                        println("${lastMove.move.from.name}: Go Fish!")
                        if (!lastMove.nextPlayer) {
                            println("${lastMove.player.name}: I drew a ${lastMove.move.askFor}! I go again!")
                        }
                    }
                    is HandOver -> println("${lastMove.move.from.name}: Ugh, I have ${lastMove.result.cards.size}!")
                }
            }
        }
    }
}
