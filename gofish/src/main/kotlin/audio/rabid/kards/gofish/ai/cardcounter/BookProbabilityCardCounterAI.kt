package audio.rabid.kards.gofish.ai.cardcounter

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit
import audio.rabid.kards.gofish.ai.TurnInfo
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult

/**
 * This AI looks at its probability of getting a book and subtracks its opponents probability of getting a book
 * if it were to reveal its hand.
 *
 * Note: Its a really bad algorithm. Often places worse than random move selection.
 */
class BookProbabilityCardCounterAI : CardCounterAi(), DebuggableAI {

    private lateinit var otherPlayersCardCounter: CardCounter

    override fun onGameStarted(
        playerNames: List<PlayerName>,
        myPlayerName: PlayerName,
        myHand: Set<Card>,
        bookedAtStart: Map<PlayerName, Set<Rank>>
    ) {
        super.onGameStarted(playerNames, myPlayerName, myHand, bookedAtStart)
        otherPlayersCardCounter = CardCounter(playerNames)
    }

    override fun onTurnCompleted(turnResult: TurnResult, myHand: Set<Card>) {
        super.onTurnCompleted(turnResult, myHand)
        otherPlayersCardCounter.trackMove(turnResult)
    }

    override fun pickMove(turnInfo: TurnInfo): Move {
        return turnInfo.possibleMoves.maxBy { expectedOutcome(turnInfo, it) }!!
    }

    private fun expectedOutcome(turnInfo: TurnInfo, move: Move): Double {
        val (rank, targetPlayer) = move
        val myCount = turnInfo.myHand.count { it.rank == rank }
        val (min, max) = getCardCounter().getMinMax(targetPlayer, rank)
        // we are going to exclude getting gofish and drawing what we asked for because it has low probability
        // (unless the ocean is small) and adds too much to complexity for now

        // If you get it, we won't count other players moves, because we will get to make another move.
        // So we only need to calculate other player's outcomes if we don't get it.

        return when {
            // we will definitely get the book
            myCount + min == Suit.ALL.size -> 1.0
            // we will definitely not get the book
            myCount + max < Suit.ALL.size -> 0.0
            else -> {
                // otherwise it is: (the probability we get the book * 1)
                //      - (the probability they get a book from us for asking for that) * 1 per player
                val scorer = Scorer(getCardCounter(), turnInfo.oceanSize, turnInfo.allPlayerHandSizes)
                val probabilityOfBook = scorer.getProbabilityOfAdditional(targetPlayer, rank)
                // if we don't get it, we reveal to the other players our hand
                probabilityOfBook - turnInfo.otherPlayerNames.sumByDouble { player ->
                    getOtherPlayerExpectedBooksOnMyGoFish(player, move, turnInfo)
                }
            }
        }
    }

    private fun getOtherPlayerExpectedBooksOnMyGoFish(
        player: PlayerName,
        move: Move,
        originalTurnInfo: TurnInfo
    ): Double {
        val myPlayerName = originalTurnInfo.myPlayerName
        val (rank, targetPlayer) = move
        // we revealed to the other players that we had at least one, and the target player had none
        val playerCardCounter = otherPlayersCardCounter.clone()
        playerCardCounter.setAtLeastOne(myPlayerName, rank)
        playerCardCounter.setNone(targetPlayer, rank)

        // I drew a card, so my hand increased by one and the ocean decreased by one
        val myCount = originalTurnInfo.myHand.size + 1
        val oceanSize = originalTurnInfo.oceanSize - 1

        // because we don't know the other player's hand, we will use our known min/max for their hand to calculate
        val (otherMin, otherMax) = playerCardCounter.getMinMax(player, rank)
        return when {
            // they will definitely get the book
            otherMin + myCount == Suit.ALL.size -> 1.0
            // the will definitely not get the book
            otherMax + myCount < Suit.ALL.size -> 0.0
            else -> {
                // to calculate hand sizes from the other player's perspective, we need to update ours
                val otherPlayerHandSizes = originalTurnInfo.otherPlayerHandSizes + Pair(myPlayerName, myCount)
                val scorer = Scorer(playerCardCounter, oceanSize, otherPlayerHandSizes)
                scorer.getProbabilityOfAdditional(myPlayerName, rank)
            }
        }
    }

    override fun debug(turnInfo: TurnInfo) {
        val playerScores = getPlayerNames().associateWith { p ->
            Rank.ALL.associateWith { r ->
                val move = Move(askFor = r, from = p)
                val score = if (turnInfo.possibleMoves.contains(move)) expectedOutcome(turnInfo, move) else null
                val (min, max) = getCardCounter().getMinMax(p, r)
                Triple(min, max, score)
            }
        }
        ScorePrinter(outstandingRanks, playerScores) { (min, max, score) ->
            (if (min == max) min.toString() else "$min..$max") +
                    (if (score != null) " (" + String.format("%5.2f", score) + ")" else "")
        }.print()
    }
}
