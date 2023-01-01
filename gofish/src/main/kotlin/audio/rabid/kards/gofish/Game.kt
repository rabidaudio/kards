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
import audio.rabid.kards.gofish.models.GameState
import audio.rabid.kards.gofish.models.GameOptions
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.MoveResult
import audio.rabid.kards.gofish.models.Player
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult
import audio.rabid.kards.gofish.ui.UI
import kotlin.random.Random

internal class Game(
    playerInfo: Map<PlayerName, MovePicker>,
    val gameOptions: GameOptions,
    private val random: Random
) {

    private val gameState: GameState = run {
        // start with a 52 card deck and shuffle it
        val deck = Decks.standard().apply { shuffle(random) }
        // for each player, draw 7 cards into their hand
        val players = playerInfo.map { (name, picker) -> Player(name, picker, handOf()) }
        for (i in (1..gameOptions.startingHandSize)) {
            for (player in players) {
                player.hand.placeOnBottom(deck.drawOne() ?: throw IllegalArgumentException("Too many players!"))
            }
        }
        // sort each player's hand so it's easier to see
        for (player in players) player.hand.sort()
        GameState(deck, players)
    }

    fun play(ui: UI? = null): Set<PlayerName> {
        val initialBooks = bookAll()
        ui?.onGameStarted(gameState.playerNames, initialBooks)
        for (player in gameState.players) {
            player.movePicker.gameStarted(gameState.playerNames, player.name, player.hand.immutableCopy(), initialBooks)
        }
        if (gameOptions.debug) gameState.debug()
        while (!gameState.isOver) {
            val turnResult = step()
            ui?.onTurnCompleted(turnResult, gameState.scores)
            for (player in gameState.players) player.movePicker.afterTurn(turnResult, player.hand.immutableCopy())
            if (gameOptions.debug) gameState.debug()
        }
        ui?.onGameEnded(gameState.winners, gameState.scores)
        return gameState.winners
    }

    private fun step(): TurnResult {
        val move = gameState.currentPlayer.movePicker.move(gameState.getTurnInfo(gameState.currentPlayerName))
        val result = move.run()
        val nextPlayer = when (result) {
            GoFish -> {
                val drawn = gameState.ocean.drawOne()
                if (drawn != null) gameState.currentPlayer.hand.placeOnBottom(drawn)
                // if they drew what they asked for they go again
                drawn?.rank != move.askFor
            }
            is HandOver -> {
                gameState.currentPlayer.hand.placeOnBottom(CardSet.create(result.cards))
                // they got a match, so they go again
                false
            }
        }
        // have the player book cards from hand
        val book = gameState.currentPlayer.book(move.askFor)
        // sort each player's hand so it's easier to see
        gameState.currentPlayer.hand.sort()
        val turnResult = TurnResult(gameState.currentPlayerName, move, result, nextPlayer, book?.rank)
        if (nextPlayer) gameState.currentPlayerName = gameState.nextPlayerName
        return turnResult
    }

    private fun bookAll(): Map<PlayerName, Set<Rank>> {
        return gameState.players.associate { player ->
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
        if (!isLegal) throw IllegalStateException("Illegal move by player ${gameState.currentPlayerName}")
        if (!targetPlayer.hasCardsOfRank(askFor)) return GoFish
        val passedCards = targetPlayer.hand.drawAllWhere { it.rank == askFor }
        return HandOver(passedCards.immutableCopy())
    }

    private val Move.targetPlayer get() = gameState.getPlayer(from)

    private fun Player.hasCardsOfRank(rank: Rank): Boolean = hand.any { it.rank == rank }

    private val Move.isLegal: Boolean
        get() = gameState.currentPlayer.hasCardsOfRank(askFor) &&
                gameState.players.any { it.name == from } &&
                gameState.currentPlayerName != from
}
