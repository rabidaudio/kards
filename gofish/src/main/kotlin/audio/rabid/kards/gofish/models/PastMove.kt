package audio.rabid.kards.gofish.models

import audio.rabid.kards.core.deck.standard.Rank

data class PastMove(
        val player: PlayerName,
        val move: Move,
        val result: MoveResult,
        val turnEnded: Boolean,
        val newBook: Rank?
)
