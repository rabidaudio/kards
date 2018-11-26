package audio.rabid.kards.gofish.ui

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.models.TurnResult

interface UI {

    fun onGameStarted(playerNames: List<PlayerName>, bookedAtStart: Map<PlayerName, Set<Rank>>)

    fun onTurnCompleted(turnResult: TurnResult, scores: Map<PlayerName, Set<Rank>>)

    fun onGameEnded(winners: Set<PlayerName>, scores: Map<PlayerName, Set<Rank>>)
}
