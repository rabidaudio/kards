package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Decks
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit
import audio.rabid.kards.gofish.ai.GameInfo
import audio.rabid.kards.gofish.ai.MovePicker
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PastMove
import audio.rabid.kards.gofish.models.PlayerName

object CardCounterAi : MovePicker {

    private val DECK_SIZE = Decks.standard().size // 52
    private val POSSIBLE_SUITS = Suit.ALL.size // 4

    override fun move(gameInfo: GameInfo): Move = Picker(
        gameInfo
    ).getBestMove()

    @SuppressWarnings("TooManyFunctions") // TODO
    class Picker(private val gameInfo: GameInfo) {

        private val cardCounter = CardCounter(gameInfo.playerNames).apply {
            // track all moves so far, which should give the minimum number of cards each player has
            for (pastMove in gameInfo.pastMoves) trackMove(pastMove)
            // then, for all completed books, clear out those ranks
            trackCompletedBooks()
            // fill in my hand, which is totally known
            trackMyHand()
        }

        private val probabilities = cardCounter.generateBaseScores().apply {
            // now fill in unknown cards with probabilities
            calculateProbabilities()
            verifyProbabilities()
        }

        fun getProbability(playerName: PlayerName, rank: Rank): Double = probabilities.getProbability(playerName, rank)

        fun getBestMove(): Move {
            val (player, rank) = probabilities.getMaxProbability(gameInfo.myRanks, gameInfo.otherPlayerNames)
            return Move(askFor = rank, from = player)
        }

        fun debug() {
            probabilities.printTable(gameInfo.outstandingRanks, this::probabilityInDeck)
        }

        private fun CardCounter.trackMove(pastMove: PastMove) {
            val (playerName, move, result, turnEnded, _) = pastMove
            // because they asked, we know they have at least one
            setAtLeastOne(playerName, move.askFor)
            // also the askee now has none, either because they didn't have any or they were taken
            setNone(move.from, move.askFor)
            when (result) {
                GoFish -> if (!turnEnded) {
                    // they drew that thing! so now they have one more
                    add(playerName, move.askFor, 1)
                }
                // otherwise, the askee gave all of theirs over to the asker
                is HandOver -> add(playerName, move.askFor, result.cards.size)
            }
        }

        private fun CardCounter.trackCompletedBooks() {
            for (rank in gameInfo.completedBooks) {
                for (player in gameInfo.playerNames) {
                    setNone(player, rank)
                }
            }
        }

        private fun CardCounter.trackMyHand() {
            for (rank in Rank.ALL) {
                val numberInMyHand = gameInfo.myHand.count { it.matches(rank) }
                setExactly(gameInfo.myPlayerName, rank, numberInMyHand)
            }
        }

        private fun ProbabilityTracker.calculateProbabilities() {
            for (rank in gameInfo.outstandingRanks) {
                // a card could exist in any deck position, plus in players' hands.
                // for a hand, it could be in any of the cards they've drawn since they last said they didn't have
                // it, or in any unknown cards in their hand if they haven't said yet
                val allPossibleLocations = gameInfo.oceanSize + gameInfo.otherPlayers.sumBy { (name, handSize) ->
                    possibleInstancesInHand(rank, name, handSize)
                }
                if (allPossibleLocations == 0) continue // avoid divide by zero, don't add any probability

                for ((playerName, handSize) in gameInfo.otherPlayers) {
                    // now we take the weighted probability: the number of unknown instances, multiplied by
                    // the probability it is in this player's hand (possible locations in hand / all possible locations)

                    val unknownInstances = POSSIBLE_SUITS - cardCounter.knownInstancesOf(rank)
                    val possibleInstancesInHand = possibleInstancesInHand(rank, playerName, handSize)
                    val probability = unknownInstances.toDouble() *
                            possibleInstancesInHand.toDouble() / allPossibleLocations.toDouble()
                    addProbability(playerName, rank, probability)
                }
            }
        }

        private fun ProbabilityTracker.verifyProbabilities() {
            // each rank column should sum to 4
            for (rank in columns) {
                val prob = rows.sumByDouble { getProbability(it, rank) } + probabilityInDeck(rank)
                prob.assertWithin(POSSIBLE_SUITS.toDouble(), 0.01) {
                    "rank probability for $rank didn't sum to 4"
                }
            }
            for ((name, handSize) in gameInfo.players) {
                val prob = columns.sumByDouble { getProbability(name, it) }
                prob.assertWithin(handSize.toDouble(), 0.01) {
                    "hand probabilities for $name expected $handSize but was $prob"
                }
            }
        }

        private inline fun Double.assertWithin(target: Double, delta: Double, lazyMessage: () -> String) {
            if (!(this >= (target - delta) && this <= (target + delta))) throw AssertionError(lazyMessage.invoke())
        }

        private fun possibleInstancesInHand(rank: Rank, playerName: PlayerName, handSize: Int): Int {
            // if they have 3 in their hand, they definitely don't have a fourth, because they'd have a book
            if (cardCounter.knownHandSize(playerName) == 3) return 0
            return gameInfo.cardsDrawnSinceNotHaving(playerName, rank)
                ?: (handSize - cardCounter.knownHandSize(playerName))
        }

        private fun probabilityInDeck(rank: Rank): Double {
            val allPossibleLocations = gameInfo.oceanSize + gameInfo.otherPlayers.sumBy { (name, handSize) ->
                possibleInstancesInHand(rank, name, handSize)
            }
            if (allPossibleLocations == 0) return 0.0
            val unknownInstances = POSSIBLE_SUITS - cardCounter.knownInstancesOf(rank)
            return unknownInstances.toDouble() * gameInfo.oceanSize.toDouble() / allPossibleLocations.toDouble()
        }

        private val GameInfo.completedBooks: Set<Rank> get() = players.flatMap { it.books }.toSet()

        private val GameInfo.oceanSize: Int
            get() =
                DECK_SIZE - (POSSIBLE_SUITS * completedBooks.size) - players.sumBy { it.handSize }

        private val GameInfo.playerNames: List<PlayerName> get() = players.map { it.playerName }

        private val GameInfo.otherPlayerNames: List<PlayerName> get() = playerNames - myPlayerName

        private val GameInfo.otherPlayers: List<GameInfo.PlayerInfo>
            get() =
                players.filter { it.playerName != myPlayerName }

        private val GameInfo.outstandingRanks get() = Rank.ALL - completedBooks

        private val GameInfo.myRanks get() = myHand.map { it.rank }.distinct()

        private fun GameInfo.cardsDrawnSinceNotHaving(playerName: PlayerName, rank: Rank): Int? {
            var drawn = 0
            for (pastMove in pastMoves.reversed()) {
                val (mover, move, result, turnEnded) = pastMove
                when {
                    mover == playerName -> {
                        // when it was their turn, they drew a card if they got a go fish
                        // (unless they drew that thing, which we've already counted)
                        if (result is GoFish && turnEnded) drawn += 1
                    }
                    move.from == playerName -> {
                        // if they were asked for the thing we are done,
                        // because that's the max number of cards they could have drawn.
                        // if they said yes, they lost all of them, and if they said no, they didn't have any
                        if (move.askFor == rank) return drawn
                    }
                }
            }
            return null
        }
    }
}
