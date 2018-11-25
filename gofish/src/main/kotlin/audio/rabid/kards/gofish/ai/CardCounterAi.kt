package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Decks
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit
import audio.rabid.kards.core.deck.utils.cartesianProduct
import audio.rabid.kards.gofish.models.*
import java.lang.StringBuilder

object CardCounterAi : MovePicker {

    private val DECK_SIZE = Decks.standard().size // 52
    private val POSSIBLE_SUITS = Suit.ALL.size    // 4
    private val POSSIBLE_RANKS = Rank.ALL.size    // 13

    override fun move(gameInfo: GameInfo): Move = Picker(gameInfo).getBestMove()

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
        }

        fun getProbability(playerName: PlayerName, rank: Rank): Double = probabilities.getProbability(playerName, rank)

        fun getBestMove(): Move {
            val (player, rank) = probabilities.getMaxProbability(gameInfo.myRanks, gameInfo.otherPlayerNames)
            return Move(askFor = rank, from = player)
        }

        fun debug() {
            probabilities.printTable(gameInfo.outstandingRanks)
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
                    val probability = unknownInstances.toDouble() * possibleInstancesInHand.toDouble() / allPossibleLocations.toDouble()
                    addProbability(playerName, rank, probability)
                }
            }
        }

        private fun possibleInstancesInHand(rank: Rank, playerName: PlayerName, handSize: Int): Int {
            // if they have 3 in their hand, they definitely don't have a fourth, because they'd have a book
            if (cardCounter.knownHandSize(playerName) == 3) return 0
            return gameInfo.cardsDrawnSinceNotHaving(playerName, rank)
                    ?: (handSize - cardCounter.knownHandSize(playerName))
        }

        private val GameInfo.completedBooks: Set<Rank> get() = players.flatMap { it.books }.toSet()

        private val GameInfo.oceanSize: Int get() =
            DECK_SIZE - (POSSIBLE_SUITS * completedBooks.size) - players.sumBy { it.handSize }

        private val GameInfo.playerNames: List<PlayerName> get() = players.map { it.playerName }

        private val GameInfo.otherPlayerNames: List<PlayerName> get() = playerNames - myPlayerName

        private val GameInfo.otherPlayers: List<GameInfo.PlayerInfo> get() =
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

    private interface ArrayHelper {

        val players: List<PlayerName>

        private val numColumns get() = POSSIBLE_RANKS

        fun getIndex(playerName: PlayerName, rank: Rank): Int =
                players.indexOf(playerName) * numColumns + Rank.ALL.indexOf(rank)

        fun decodeIndex(index: Int): Pair<PlayerName, Rank> =
                Pair(players[index / numColumns], Rank.ALL[index % numColumns])
    }

    private class CardCounter(override val players: List<PlayerName>): ArrayHelper {

        private val cardCountTable = IntArray(players.size * POSSIBLE_RANKS)

        private fun get(playerName: PlayerName, rank: Rank): Int = cardCountTable[getIndex(playerName, rank)]

        private fun set(playerName: PlayerName, rank: Rank, value: Int) {
            cardCountTable[getIndex(playerName, rank)] = value
        }

        fun setNone(playerName: PlayerName, rank: Rank) = set(playerName, rank, 0)

        fun setAtLeastOne(playerName: PlayerName, rank: Rank) {
            if (get(playerName, rank) == 0) set(playerName, rank, 1)
        }

        fun add(playerName: PlayerName, rank: Rank, count: Int) =
                set(playerName, rank, get(playerName, rank) + count)

        fun setExactly(playerName: PlayerName, rank: Rank, count: Int) = set(playerName, rank, count)

        fun knownInstancesOf(rank: Rank): Int = players.sumBy { get(it, rank) }

        fun knownHandSize(playerName: PlayerName) = Rank.ALL.sumBy { get(playerName, it) }

        fun generateBaseScores(): ProbabilityTracker = ProbabilityTracker(players, cardCountTable)
    }

    private class ProbabilityTracker(override val players: List<PlayerName>, cardCounts: IntArray): ArrayHelper {

        // convert integer number of cards to doubles for scoring
        private val probabilityTable = DoubleArray(cardCounts.size) { i -> cardCounts[i].toDouble() }

        fun addProbability(playerName: PlayerName, rank: Rank, probability: Double) {
            probabilityTable[getIndex(playerName, rank)] += probability
        }

        fun getMaxProbability(rankOptions: List<Rank>, playerOptions: List<PlayerName>): Pair<PlayerName, Rank> =
                playerOptions.cartesianProduct(rankOptions)
                    .map { i -> i to probabilityTable[getIndex(i.first, i.second)] }
                    .maxBy { (_, p) -> p }!!.first

        fun getProbability(playerName: PlayerName, rank: Rank): Double = probabilityTable[getIndex(playerName, rank)]

        fun printTable(oustanding: List<Rank>) {
            print(StringBuilder().apply {
                val maxNameLength = players.map { it.name.length }.max()!!
                append("".padStart(maxNameLength)).append(" \t")
                for (rank in Rank.ALL) {
                    append(rank.shortName().padStart(7))
                }
                appendln()
                append("deck".padStart(maxNameLength)).append(":\t")
                for (rank in Rank.ALL) {
                    if (oustanding.contains(rank)) {
                        val remaining = POSSIBLE_SUITS - players.sumByDouble { probabilityTable[getIndex(it, rank)] }
                        append(String.format("%7.2f", remaining))
                    } else {
                        append("".padStart(7))
                    }
                }
                appendln()
                for (player in players) {
                    append(player.name.padStart(maxNameLength))
                    append(":\t")
                    for (rank in Rank.ALL) {
                        append(String.format("%7.2f", probabilityTable[getIndex(player, rank)]))
                    }
                    appendln()
                }
            }.toString())
        }
    }
}
