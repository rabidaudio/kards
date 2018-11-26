package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.ai.GameInfo
import audio.rabid.kards.gofish.ai.MovePicker
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PastMove
import audio.rabid.kards.gofish.models.PlayerName

object CardCounterAi : MovePicker {

    override fun move(gameInfo: GameInfo): Move = Picker(gameInfo).getBestMove()

    class Picker(private val gameInfo: GameInfo) {

        companion object {
            private val Ranks = Rank.ALL
        }

        private val cardCounter = CardCounter(gameInfo.playerNames).apply {
            // for any initial books, remove those from the game
            for (rank in gameInfo.initialBooks.values.flatten()) trackBooked(rank)
            // track all moves so far, which should give the possible range of cards each player has
            for (pastMove in gameInfo.pastMoves) trackMove(pastMove)
            // fill in my hand, which is totally known
            trackMyHand()
        }

        fun getBestMove(): Move {
            return gameInfo.possibleMoves.maxBy { (r, p) -> getScore(p, r) }!!
        }

        fun getScore(playerName: PlayerName, rank: Rank): Double {
            // the score is the known number of that card in that persons hand,
            // plus the probability of the unknown cards being that rank, weighted by unknown hand size
            val minNumber = cardCounter.getMin(playerName, rank).toDouble()
            val probabilityDistribution = Ranks.sumByDouble { r -> getProbability(playerName, r) }
            if (probabilityDistribution == 0.0) return minNumber
            val unknownHandSize = gameInfo.getUnknownHandSize(playerName).toDouble()
            return minNumber + (getProbability(playerName, rank) / probabilityDistribution * unknownHandSize)
        }

        private fun getProbability(playerName: PlayerName, rank: Rank): Double {
            // the probability they have one of those cards is the probability that any unknown card is that card,
            // weighted by the number of unknown cards in their hand
            val possibleLocationsInGame = gameInfo.oceanSize +
                    gameInfo.otherPlayerNames.sumBy { p -> cardCounter.getDifferential(p, rank) }
            val possibleLocationsInHand = cardCounter.getDifferential(playerName, rank)
            val knownInstancesInGame = gameInfo.playerNames.sumBy { p -> cardCounter.getMin(p, rank) }
            val unknownInstancesInGame = 4 - knownInstancesInGame
            return possibleLocationsInHand.toDouble() *
                    unknownInstancesInGame.toDouble() / possibleLocationsInGame.toDouble()
        }

        fun debug() {
            val playerScores = gameInfo.playerNames.associateWith { p -> Ranks.associateWith { r -> getScore(p, r) } }
            ScorePrinter(gameInfo.outstandingRanks, playerScores).print()
        }

        private fun CardCounter.trackBooked(rank: Rank) {
            for (playerName in gameInfo.playerNames) setNone(playerName, rank)
        }

        private fun CardCounter.trackMove(pastMove: PastMove) {
            val (fromPlayer, move, result, turnEnded, newBook) = pastMove
            val (rank, toPlayer) = move
            // because they asked, we know they have at least one
            setAtLeastOne(fromPlayer, rank)
            // also the player asked now has none, either because they didn't have any or they were taken
            setNone(toPlayer, rank)
            when (result) {
                GoFish -> {
                    if (!turnEnded) {
                        // they drew that thing! so now they have one more
                        addExactly(fromPlayer, move.askFor, 1)
                    } else {
                        // they drew a card, so it could be anything except what they asked for
                        for (possiblyDrawnRank in (gameInfo.outstandingRanks - rank)) {
                            addPossiblyOneMore(fromPlayer, possiblyDrawnRank)
                        }
                    }
                }
                // otherwise, the asked player gave all of theirs over to the asking player
                is HandOver -> addExactly(fromPlayer, move.askFor, result.cards.size)
            }
            // if they booked a new rank, track it
            newBook?.let { trackBooked(it) }
        }

        private fun CardCounter.trackMyHand() {
            for (rank in Ranks) {
                val numberInMyHand = gameInfo.myHand.count { it.matches(rank) }
                setExactly(gameInfo.myPlayerName, rank, numberInMyHand)
            }
        }

        private fun GameInfo.getUnknownHandSize(playerName: PlayerName) =
            players.first { it.playerName == playerName }.unknownHandSize

        private val GameInfo.PlayerInfo.unknownHandSize
            get() = handSize - Ranks.sumBy { cardCounter.getMin(playerName, it) }

        private val GameInfo.outstandingRanks get() = Ranks - completedBooks
    }
}
