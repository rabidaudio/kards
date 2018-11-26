package audio.rabid.kards.gofish

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.CardSet
import audio.rabid.kards.core.deck.standard.Decks
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit
import audio.rabid.kards.core.deck.standard.drawOne
import audio.rabid.kards.core.deck.standard.handOf
import audio.rabid.kards.core.deck.standard.placeOnBottom
import audio.rabid.kards.gofish.ai.MovePicker
import audio.rabid.kards.gofish.models.Book
import audio.rabid.kards.gofish.models.Game
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.MoveResult
import audio.rabid.kards.gofish.models.Player
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult
import audio.rabid.kards.gofish.ui.UI
import kotlin.random.Random

internal class GoFishGame(playerInfo: Map<PlayerName, MovePicker>, private val random: Random) {

    private val game: Game = run {
        // start with a 52 card deck and shuffle it
        val deck = Decks.standard().apply { shuffle(random) }
        // for each player, draw 7 cards into their hand
        val players = playerInfo.map { (name, picker) -> Player(name, picker, handOf()) }
        for (i in (1..7)) {
            for (player in players) {
                player.hand.placeOnBottom(deck.drawOne() ?: throw IllegalArgumentException("Too many players!"))
            }
        }
        // sort each player's hand so it's easier to see
        for (player in players) player.hand.sort()
        Game(deck, players)
    }

    fun play(ui: UI? = null): Set<PlayerName> {
        val initialBooks = bookAll()
        ui?.onGameStarted(game.playerNames, initialBooks)
        for (player in game.players) player.movePicker.gameStarted(game.playerNames, player.name, initialBooks)
        while (!game.isOver) {
            val turnResult = step()
            ui?.onTurnCompleted(turnResult, game.scores)
            game.debug()
            for (player in game.players) player.movePicker.afterTurn(turnResult)
        }
        ui?.onGameEnded(game.winners, game.scores)
        return game.winners
    }

    private fun step(): TurnResult {
        val move = game.currentPlayer.movePicker.move(game.getTurnInfo(game.currentPlayerName))
        val result = move.run()
        val nextPlayer = when (result) {
            GoFish -> {
                val drawn = game.ocean.drawOne()
                if (drawn != null) game.currentPlayer.hand.placeOnBottom(drawn)
                // if they drew what they asked for they go again
                drawn?.rank != move.askFor
            }
            is HandOver -> {
                game.currentPlayer.hand.placeOnBottom(CardSet.create(result.cards))
                // they got a match, so they go again
                false
            }
        }
        // have the player book cards from hand
        val book = game.currentPlayer.book(move.askFor)
        // sort each player's hand so it's easier to see
        game.currentPlayer.hand.sort()
        val turnResult = TurnResult(game.currentPlayerName, move, result, nextPlayer, book?.rank)
        if (nextPlayer) game.currentPlayerName = game.nextPlayerName
        return turnResult
    }

    private fun bookAll(): Map<PlayerName, Set<Rank>> {
        return game.players.associate { player ->
            player.name to Rank.ALL.mapNotNull { player.book(it) }.map { it.rank }.toSet()
        }
    }

    private fun Player.book(rank: Rank): Book? {
        val booked = Suit.ALL.all { suit -> hand.contains(Card(suit, rank)) }
        if (!booked) return null
        val book = Book(hand.drawAllWhere { it.rank == rank })
        books.add(book)
        return book
    }

    private fun Move.run(): MoveResult {
        if (!isLegal) throw IllegalStateException("Illegal move by player ${game.currentPlayerName}")
        if (!targetPlayer.hasCardsOfRank(askFor)) return GoFish
        val passedCards = targetPlayer.hand.drawAllWhere { it.rank == askFor }
        return HandOver(passedCards.immutableCopy())
    }

    private val Move.targetPlayer get() = game.getPlayer(from)

    private fun Player.hasCardsOfRank(rank: Rank): Boolean = hand.any { it.rank == rank }

    private val Move.isLegal: Boolean
        get() = game.currentPlayer.hand.any { it.rank == askFor } &&
                game.players.any { it.name == from } &&
                game.currentPlayerName != from
}
