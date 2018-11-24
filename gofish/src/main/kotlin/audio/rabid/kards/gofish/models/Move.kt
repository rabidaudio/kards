package audio.rabid.kards.gofish.models

import audio.rabid.kards.core.deck.standard.Rank

data class Move(val askFor: Rank, val from: PlayerName)
