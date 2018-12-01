package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.ai.MovePicker
import audio.rabid.kards.gofish.ai.TurnInfo
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult

abstract class CardCounterAi : MovePicker {

    companion object {
        private val Ranks = Rank.ALL
    }

    private lateinit var playerNames: List<PlayerName>
    private lateinit var myPlayerName: PlayerName
    private lateinit var cardCounter: CardCounter

    private val completedBooks = mutableSetOf<Rank>()

    private val outstandingRanks get() = Ranks - completedBooks

    override fun gameStarted(
        playerNames: List<PlayerName>,
        myPlayerName: PlayerName,
        myHand: Set<Card>,
        bookedAtStart: Map<PlayerName, Set<Rank>>
    ) {
        this.playerNames = playerNames
        this.myPlayerName = myPlayerName
        cardCounter = CardCounter(playerNames).apply {
            // for any initial books, remove those from the game
            for (rank in bookedAtStart.values.flatten()) trackBooked(rank)
        }
    }

    override fun afterTurn(turnResult: TurnResult, myHand: Set<Card>) {
        cardCounter.trackMove(turnResult)
        cardCounter.trackMyHand(myHand)
    }

    override fun move(turnInfo: TurnInfo): Move {
        cardCounter.trackMyHand(turnInfo.myHand)
        val scorer = Scorer(turnInfo)
        return pickMove(turnInfo, scorer)
    }

    abstract fun pickMove(turnInfo: TurnInfo, scorer: Scorer): Move

    private fun CardCounter.trackBooked(rank: Rank) {
        completedBooks.add(rank)
        for (playerName in playerNames) setNone(playerName, rank)
    }

    private fun CardCounter.trackMove(turnResult: TurnResult) {
        val (fromPlayer, move, result, turnEnded, newBook) = turnResult
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
                    for (possiblyDrawnRank in (outstandingRanks - rank)) {
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

    private fun CardCounter.trackMyHand(myHand: Set<Card>) {
        for (rank in Ranks) {
            val numberInMyHand = myHand.count { it.matches(rank) }
            setExactly(myPlayerName, rank, numberInMyHand)
        }
    }

    fun debug(turnInfo: TurnInfo) {
        val playerScores = playerNames.associateWith { p ->
            Ranks.associateWith { r -> Scorer(turnInfo).getScore(p, r) }
        }
        ScorePrinter(outstandingRanks, playerScores).print()
    }

    inner class Scorer(private val turnInfo: TurnInfo) {

        fun getScore(playerName: PlayerName, rank: Rank): Double {
            // the score is the known number of that card in that persons hand,
            // plus the probability of the unknown cards being that rank, weighted by unknown hand size
            val minNumber = cardCounter.getMin(playerName, rank).toDouble()
            val probabilityDistribution = Ranks.sumByDouble { r -> getProbability(playerName, r) }
            if (probabilityDistribution == 0.0) return minNumber
            val unknownHandSize = turnInfo.getUnknownHandSize(playerName).toDouble()
            return minNumber + (getProbability(playerName, rank) / probabilityDistribution * unknownHandSize)
        }

        private fun getProbability(playerName: PlayerName, rank: Rank): Double {
            // the probability they have one of those cards is the probability that any unknown card is that card,
            // weighted by the number of unknown cards in their hand
            val possibleLocationsInGame = turnInfo.oceanSize +
                    turnInfo.otherPlayerNames.sumBy { p -> cardCounter.getDifferential(p, rank) }
            val possibleLocationsInHand = cardCounter.getDifferential(playerName, rank)
            val knownInstancesInGame = playerNames.sumBy { p -> cardCounter.getMin(p, rank) }
            val unknownInstancesInGame = 4 - knownInstancesInGame
            return possibleLocationsInHand.toDouble() *
                    unknownInstancesInGame.toDouble() / possibleLocationsInGame.toDouble()
        }

        private fun TurnInfo.getHandSize(playerName: PlayerName) =
            if (playerName == myPlayerName) myHand.size else otherPlayerHandSizes[playerName]!!

        private fun TurnInfo.getUnknownHandSize(playerName: PlayerName) =
            getHandSize(playerName) - Ranks.sumBy { cardCounter.getMin(playerName, it) }
    }
}
