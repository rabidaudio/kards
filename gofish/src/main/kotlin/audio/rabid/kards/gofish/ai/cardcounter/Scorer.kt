package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PlayerName

class Scorer(
    private val cardCounter: CardCounter,
    private val oceanSize: Int,
    private val allPlayerHandSizes: Map<PlayerName, Int>
) {

    fun getScore(move: Move): Double {
        val (rank, playerName) = move
        // the score is the known number of that card in that persons hand,
        // plus the probability of the unknown cards being that rank, weighted by unknown hand size
        val minNumber = cardCounter.getMin(playerName, rank).toDouble()
        val probabilityDistribution = Rank.ALL.sumByDouble { r -> getProbabilityOfAdditional(playerName, r) }
        if (probabilityDistribution == 0.0) return minNumber
        val unknownHandSize = getUnknownHandSize(playerName).toDouble()
        return minNumber + (getProbabilityOfAdditional(playerName, rank) / probabilityDistribution * unknownHandSize)
    }

    fun getProbabilityOfAdditional(playerName: PlayerName, rank: Rank): Double {
        // the probability they have one of those cards is the probability that any unknown card is that card,
        // weighted by the number of unknown cards in their hand
        val possibleLocationsInGame = oceanSize +
                allPlayerNames.sumBy { p -> cardCounter.getDifferential(p, rank) }
        val possibleLocationsInHand = cardCounter.getDifferential(playerName, rank)
        val knownInstancesInGame = cardCounter.players.sumBy { p -> cardCounter.getMin(p, rank) }
        val unknownInstancesInGame = 4 - knownInstancesInGame
        return possibleLocationsInHand.toDouble() *
                unknownInstancesInGame.toDouble() / possibleLocationsInGame.toDouble()
    }

    private val allPlayerNames get() = allPlayerHandSizes.keys

    private fun getUnknownHandSize(playerName: PlayerName) =
        allPlayerHandSizes[playerName]!! - Rank.ALL.sumBy { cardCounter.getMin(playerName, it) }
}
