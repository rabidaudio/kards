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

    private lateinit var playerNames: List<PlayerName>
    private lateinit var myPlayerName: PlayerName
    private lateinit var cardCounter: CardCounter

    private val completedBooks = mutableSetOf<Rank>()

    protected val outstandingRanks get() = Rank.ALL - completedBooks
    protected fun getPlayerNames() = playerNames
    protected fun getCardCounter() = cardCounter

    override fun onGameStarted(
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

    override fun onTurnCompleted(turnResult: TurnResult, myHand: Set<Card>) {
        cardCounter.trackMove(turnResult)
        cardCounter.trackMyHand(myHand)
    }

    override fun move(turnInfo: TurnInfo): Move {
        cardCounter.trackMyHand(turnInfo.myHand)
        return pickMove(turnInfo)
    }

    abstract fun pickMove(turnInfo: TurnInfo): Move

    fun CardCounter.trackBooked(rank: Rank) {
        completedBooks.add(rank)
        for (playerName in playerNames) setNone(playerName, rank)
    }

    fun CardCounter.trackMove(turnResult: TurnResult) {
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

    fun CardCounter.trackMyHand(myHand: Set<Card>) {
        for (rank in Rank.ALL) {
            val numberInMyHand = myHand.count { it.matches(rank) }
            setExactly(myPlayerName, rank, numberInMyHand)
        }
    }
}
