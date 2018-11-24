package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit
import audio.rabid.kards.core.deck.utils.cartesianProduct
import audio.rabid.kards.gofish.models.*
import java.lang.StringBuilder

class CardCounterAi : MovePicker {

    override fun move(gameInfo: GameInfo): Move = Picker(gameInfo).getBestMove()

    private class Picker(val gameInfo: GameInfo) {

        private val cardCounter = CardCounter(gameInfo.allPlayerNames)

        private val probabilities by lazy { cardCounter.generateBaseScores() }

        fun getBestMove(): Move {
            // track all moves so far, which should give the minimum number of cards each player has
            for (pastMove in gameInfo.pastMoves) trackMove(pastMove)
            // fill in my hand, which is totally known
            trackMyHand()
            // now fill in unknown cards with probabilities
            calculateProbabilities()
            probabilities.printTable()
            val (player, rank) = probabilities.getMaxProbability(gameInfo.myRanks, gameInfo.otherPlayerNames)
            return Move(askFor = rank, from = player)
        }

        private fun trackMove(pastMove: PastMove) {
            val (playerName, move, result, turnEnded, newBook) = pastMove
            // because they asked, we know they have at least one
            cardCounter.setAtLeastOne(playerName, move.askFor)
            // also the askee now has none, either because they didn't have any or they were taken
            cardCounter.setNone(move.from, move.askFor)
            when (result) {
                GoFish -> if (!turnEnded) {
                    // they drew that thing! so now they have one more
                    cardCounter.add(playerName, move.askFor, 1)
                }
                // otherwise, the askee gave all of theirs over to the asker
                is HandOver -> cardCounter.add(playerName, move.askFor, result.cards.size)
            }
            // then, for all new books, clear out those ranks
            newBook?.let { trackNewBook(it) }
        }

        private fun trackNewBook(rank: Rank) {
            for (player in gameInfo.allPlayerNames) cardCounter.setNone(player, rank)
        }

        private fun trackMyHand() {
            for (rank in Rank.ALL) {
                val numberInMyHand = gameInfo.myHand.count { it.matches(rank) }
                cardCounter.setExactly(gameInfo.myPlayerName, rank, numberInMyHand)
            }
        }

        private fun calculateProbabilities() {
            for (rank in gameInfo.outstandingRanks) {
                for ((playerName, handSize) in gameInfo.otherPlayers) {
                    // the cards in this player's hand that could possibly be that rank cannot be more than the unknown
                    // number of cards in their hand. It also cannot be more than the number of cards they've drawn since
                    // they last didn't have them
                    val unknownCardsInHand = handSize - cardCounter.knownHandSize(playerName)
                    val cardsDrawnSinceNotHavingRank = gameInfo.cardsDrawnSinceNotHaving(playerName, rank)
                    val possibleInstances = if (cardsDrawnSinceNotHavingRank == null) {
                        unknownCardsInHand
                    } else {
                        minOf(unknownCardsInHand, cardsDrawnSinceNotHavingRank)
                    }
                    if (possibleInstances == 0) continue // avoid divide by zero
                    val unknownInstances = Suit.ALL.size - cardCounter.knownInstancesOf(rank)
                    val probability = unknownInstances.toDouble() / possibleInstances.toDouble()
                    probabilities.addProbability(playerName, rank, probability)
                }
            }
        }

        private val GameInfo.otherPlayerNames get() = otherPlayers.map { it.playerName }

        private val GameInfo.allPlayerNames get() = otherPlayerNames + myPlayerName

        private val GameInfo.outstandingRanks get() = Rank.ALL - myBooks - otherPlayers.flatMap { it.books }

        private val GameInfo.myRanks get() = myHand.map { it.rank }.distinct()

        private fun GameInfo.cardsDrawnSinceNotHaving(playerName: PlayerName, rank: Rank): Int? {
            var drawn = 0
            for (pastMove in pastMoves) {
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

        private val numColumns get() = Rank.ALL.size

        fun getIndex(playerName: PlayerName, rank: Rank): Int =
                players.indexOf(playerName) * numColumns + Rank.ALL.indexOf(rank)

        fun decodeIndex(index: Int): Pair<PlayerName, Rank> =
                Pair(players[index / numColumns], Rank.ALL[index % numColumns])
    }

    private class CardCounter(override val players: List<PlayerName>): ArrayHelper {

        private val cardCounts = IntArray(players.size * Rank.ALL.size)

        private fun get(playerName: PlayerName, rank: Rank): Int = cardCounts[getIndex(playerName, rank)]

        private fun set(playerName: PlayerName, rank: Rank, value: Int) {
            cardCounts[getIndex(playerName, rank)] = value
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

        fun generateBaseScores(): ProbabilityTracker = ProbabilityTracker(players, cardCounts)
    }

    private class ProbabilityTracker(override val players: List<PlayerName>, cardCounts: IntArray): ArrayHelper {

        // convert integer number of cards to doubles for scoring
        private val probabilities = DoubleArray(cardCounts.size) { i -> cardCounts[i].toDouble() }

        fun addProbability(playerName: PlayerName, rank: Rank, probability: Double) {
            probabilities[getIndex(playerName, rank)] += probability
        }

        fun getMaxProbability(rankOptions: List<Rank>, playerOptions: List<PlayerName>): Pair<PlayerName, Rank> =
                playerOptions.cartesianProduct(rankOptions)
                    .map { i -> i to probabilities[getIndex(i.first, i.second)] }
                    .maxBy { (_, p) -> p }!!.first

        fun printTable() {
            print(StringBuilder().apply {
                val maxNameLength = players.map { it.name.length }.max()!!
                append("".padStart(maxNameLength))
                append(" \t")
                for (rank in Rank.ALL) {
                    append(rank.shortName().padStart(7))
                }
                appendln()
                for (player in players) {
                    append(player.name.padStart(maxNameLength))
                    append(":\t")
                    for (rank in Rank.ALL) {
                        append(String.format("%7.2f", probabilities[getIndex(player, rank)]))
                    }
                    appendln()
                }
            }.toString())
        }
    }
}
