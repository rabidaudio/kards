package audio.rabid.kards.gofish.ai.cardcounter.montecarlo

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit
import audio.rabid.kards.core.deck.utils.cartesianProduct
import audio.rabid.kards.gofish.ai.TurnInfo
import audio.rabid.kards.gofish.ai.cardcounter.CardCounter
import audio.rabid.kards.gofish.ai.cardcounter.CardCounterAi
import audio.rabid.kards.gofish.ai.cardcounter.Scorer
import audio.rabid.kards.gofish.ai.cardcounter.addExactly
import audio.rabid.kards.gofish.ai.cardcounter.addPossiblyOneMore
import audio.rabid.kards.gofish.ai.cardcounter.setAtLeastOne
import audio.rabid.kards.gofish.ai.cardcounter.setExactly
import audio.rabid.kards.gofish.ai.cardcounter.setNone
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult
import java.math.BigInteger
import kotlin.random.Random

/**
 * This algorithm considers the possible moves, selects a few possible real game states, and plays them out
 * assuming opponents pick high-optimal moves, and counts how many result in the player winning the book vs
 * of another player winning the book.
 *
 * The search space is enormous, with 1.1e43 possible game states (although the search space within a game should be
 * much smaller). We use a few heuristics to reduce the search space:
 *  - terminate a search at winning or losing the book in question over winning the match
 *  - assume the other players play optimally
 */
class MonteCarloTreeSearchAI(private val random: Random, private val iterationsPerMove: Int = 100) : CardCounterAi() {

    private lateinit var publicCardState: CardCounter

    override fun onGameStarted(
        playerNames: List<PlayerName>,
        myPlayerName: PlayerName,
        myHand: Set<Card>,
        bookedAtStart: Map<PlayerName, Set<Rank>>
    ) {
        super.onGameStarted(playerNames, myPlayerName, myHand, bookedAtStart)
        publicCardState = getCardCounter().clone()
    }

    override fun onTurnCompleted(turnResult: TurnResult, myHand: Set<Card>) {
        super.onTurnCompleted(turnResult, myHand)
        publicCardState.trackMove(turnResult)
    }

    override fun pickMove(turnInfo: TurnInfo): Move {
        val start = System.currentTimeMillis()
        val outstandingCards = getOutstandingCards(turnInfo)
        println("${countPossiblePermutations(outstandingCards)} possible moves")
        val selectedMove = turnInfo.possibleMoves.maxBy { move ->
            (0 until iterationsPerMove).sumBy {
                val possibleGame = generatePossibleGame(turnInfo, outstandingCards)
                val didWin = GameSimulation(publicCardState, turnInfo.myPlayerName, move, possibleGame).playOut()
                if (didWin) 1 else 0
            }
        }!!
        val end = System.currentTimeMillis()
        println("Took ${(end - start) / 1000.0}s searching moves")
        return selectedMove
    }

    private fun getOutstandingCards(turnInfo: TurnInfo): List<Rank> {
        val allRemainingCards = outstandingRanks.flatMap { it.repeated(4) }
        val myCards = turnInfo.myHand.map { it.rank }
        val knownOpponentCards = turnInfo.otherPlayerNames.flatMap { p ->
            outstandingRanks.flatMap { r -> r.repeated(getCardCounter().getMin(p, r)) }
        }
        return allRemainingCards - myCards - knownOpponentCards
    }

    private fun countPossiblePermutations(outstandingCards: List<Rank>): BigInteger {
        // see: https://www.mathwarehouse.com/probability/permutations-repeated-items.php
        val n = outstandingCards.size
        val parts = outstandingCards.distinct().map { r -> outstandingCards.count { it == r } }
        return n.factorial() / parts.fold(BigInteger.ONE) { acc, i -> acc * i.factorial() }
    }

    private fun possibleHandCombinations(outstandingCards: List<Rank>): BigInteger {
        // see: https://math.stackexchange.com/questions/41724/combination-problem-with-constraints

    }

    private fun combinations(n: Int, k: Int): BigInteger =
        n.factorial() / (k.factorial() * (n - k).factorial())

    private fun generatePossibleGame(turnInfo: TurnInfo, outstandingCards: List<Rank>): PossibleGame {
        // TODO! this is wrong, doesn't take into account max value for spaces
        val deck = outstandingCards.toMutableList() // copy
        deck.shuffle(random) // shuffle
        val myCards = turnInfo.myHand.map { it.rank }
        val playerHands = turnInfo.otherPlayerHandSizes.mapValues { (_, s) ->
            (0 until s).map { deck.removeAt(0) }
        } + Pair(turnInfo.myPlayerName, myCards)
        return PossibleGame(deck, playerHands)
    }

    private fun Int.factorial(): BigInteger {
        var v = BigInteger.ONE
        for (i in (2..this)) {
            v *= i.toBigInteger()
        }
        return v
    }
}

// unfortunately we don't want to use the existing GoFishGame implementation because we want to keep the search
// space small by abstracting cards into ranks (because we don't care about suit permutations)
data class PossibleGame(
    val ocean: List<Rank>,
    val players: Map<PlayerName, List<Rank>>
)

class GameSimulation(
    private val publicCardState: CardCounter,
    private val myPlayer: PlayerName,
    private val simulatedMove: Move,
    possibleGame: PossibleGame
) {
    private val players: List<PlayerName> = possibleGame.players.keys.toList()

    private var currentPlayerIndex = players.indexOf(myPlayer)

    private val currentPlayer get() = players[currentPlayerIndex]

    private val ocean = possibleGame.ocean.toMutableList()
    private val playerHands: Map<PlayerName, MutableList<Rank>>
            = possibleGame.players.mapValues { (_, hand) -> hand.toMutableList() }
    private val outstandingRanks: MutableSet<Rank>
            = (possibleGame.ocean + possibleGame.players.values.flatten()).toMutableSet()

    fun playOut(): Boolean {
        var move = simulatedMove
        while (true) {
            // play this move
            val (rank, _) = move
            val nextPlayer = execute(move)
            if (isBooked(rank)) {
                // this is the exit simulation condition, when one player wins the rank in question
                if (rank == simulatedMove.askFor) return currentPlayer == myPlayer
                outstandingRanks.remove(rank)
                for (player in players) publicCardState.setNone(player, rank)
            }
            if (nextPlayer) goToNextPlayer()
            // select move
            move = selectNextMove()
        }
    }

    private fun execute(move: Move): Boolean {
        val (rank, targetPlayer) = move
        // because they asked, they have at least one
        publicCardState.setAtLeastOne(currentPlayer, rank)
        // the target player either hand none or handed them over
        publicCardState.setNone(targetPlayer, rank)
        val countInTargetHand = playerHands[targetPlayer]!!.count { it == rank }
        if (countInTargetHand == 0) {
            // go fish
            if (ocean.isEmpty()) return true // game is over
            val drawn = ocean.removeAt(0)
            playerHands[currentPlayer]!!.add(drawn)
            return if (drawn == rank) {
                publicCardState.addExactly(currentPlayer, rank, 1)
                false
            } else {
                for (outstandingRank in outstandingRanks) {
                    publicCardState.addPossiblyOneMore(currentPlayer, outstandingRank)
                }
                true
            }
        } else {
            // got a match
            playerHands[targetPlayer]!!.removeAll { it == rank }
            playerHands[currentPlayer]!!.addAll(rank.repeated(countInTargetHand))
            publicCardState.addExactly(currentPlayer, rank, countInTargetHand)
            return true
        }
    }

    private fun selectNextMove(): Move {
        val playersCardState = publicCardState.clone()
        playersCardState.trackHand(currentPlayer, playerHands[currentPlayer]!!)
        val possibleMoves = (players - currentPlayer).cartesianProduct(outstandingRanks)
            .map { (p, r) -> Move(askFor = r, from = p) }
        val scorer = Scorer(playersCardState, ocean.size, playerHands.mapValues { (_, hand) -> hand.size })
        return possibleMoves.maxBy { scorer.getScore(it) }!!
    }

    private fun isBooked(rank: Rank): Boolean = playerHands[currentPlayer]!!.count { it == rank } == Suit.ALL.size

    private fun CardCounter.trackHand(playerName: PlayerName, hand: List<Rank>) {
        for (rank in outstandingRanks) setExactly(playerName, rank, hand.count { it == rank })
    }

    private fun goToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }
}

private fun <T> T.repeated(times: Int): List<T> = List(times) { this }
