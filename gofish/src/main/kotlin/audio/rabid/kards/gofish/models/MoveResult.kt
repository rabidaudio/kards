package audio.rabid.kards.gofish.models

import audio.rabid.kards.core.deck.standard.Card

sealed class MoveResult

object GoFish: MoveResult()
data class HandOver(val cards: Set<Card>): MoveResult()
