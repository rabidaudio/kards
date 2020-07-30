package audio.rabid.kards.gofish.models

import audio.rabid.kards.core.deck.standard.Card
import audio.rabid.kards.core.deck.standard.CardSet
import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit

data class Book(private val cardSet: CardSet) {
    companion object {
        fun isValid(cards: Set<Card>): Boolean {
            return cards.isNotEmpty() &&
                    cards.all { it.matches(cards.first().rank) } &&
                    Suit.ALL.all { suit -> cards.any { it.matches(suit) } }
        }
    }

    init {
        if (!isValid(cardSet.immutableCopy())) {
            throw IllegalArgumentException("Not a valid Book: ${cardSet.immutableCopy()}")
        }
    }

    val rank: Rank = cardSet.first().rank
}
