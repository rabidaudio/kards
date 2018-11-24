package audio.rabid.kards.gofish

import audio.rabid.kards.core.deck.standard.*
import audio.rabid.kards.gofish.ai.MovePicker
import audio.rabid.kards.gofish.models.*
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.ui.UI
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.random.Random

internal class GoFishGame(playerInfo: Map<PlayerName, MovePicker>, private val random: Random) {

    private val game: Game = run {
        // start with a 52 card deck and shuffle it
        val deck = Decks.standard().apply { shuffle(random) }
        // for each player, draw 7 cards into their hand
        val players = playerInfo.map { (name, picker) ->
            val hand = deck.draw(7) ?: throw IllegalArgumentException("Too many players!")
            Player(name, picker, hand)
        }
        Game(deck, players)
    }

    fun play(ui: UI? = null): Set<PlayerName> {
        ui?.draw(game)
        while (!game.isOver) {
            step()
            ui?.draw(game)
        }
        return game.winners
    }

    private fun step() {
        val move = game.currentPlayer.movePicker.move(game.gameInfo)
        val result = move.run()
        val nextPlayer = when (result) {
            GoFish -> {
                val drawn = game.ocean.drawOne()
                if (drawn != null) {
                    game.currentPlayer.hand.placeOnBottom(drawn)
                    // if they drew what they asked for they go again
                    drawn.rank != move.askFor
                } else {
                    true
                }
            }
            is HandOver -> {
                game.currentPlayer.hand.placeOnBottom(CardSet(result.cards))
                // they got a match, so they go again
                false
            }
        }
        game.pastMoves.add(PastMove(game.currentPlayerName, move, result, nextPlayer))
        // have players book cards from hand
        game.players.forEach { player ->
            player.books.addAll(player.hand.books())
        }
        if (nextPlayer) game.currentPlayerName = game.nextPlayerName
    }

    private fun Hand.books(): List<Book> = groupBy { it.rank }.mapNotNull { (rank, cards) ->
        return@mapNotNull if (cards.toSet().isCompleteBook) drawAllWhere { it.rank == rank } else null
    }

    private val Set<Card>.isCompleteBook get() = !isEmpty()
            && all { it.rank == first().rank }
            && Suit.values().all { suit -> any { it.suit == suit } }

    private fun Move.run(): MoveResult {
        if (!isLegal) throw IllegalStateException("Illegal move by player ${game.currentPlayerName}")
        if (!targetPlayer.hasCardsOfRank(askFor)) return GoFish
        val passedCards = targetPlayer.hand.drawAllWhere { it.rank == askFor }
        return HandOver(passedCards.immutableCopy())
    }

    private val Move.targetPlayer get() = game.getPlayer(from)

    private fun Player.hasCardsOfRank(rank: Rank): Boolean = hand.any { it.rank == rank }

    private val Move.isLegal: Boolean get() =
        game.currentPlayer.hand.any { it.rank == askFor }
                && game.players.any { it.name == from }
                && game.currentPlayerName != from
}
