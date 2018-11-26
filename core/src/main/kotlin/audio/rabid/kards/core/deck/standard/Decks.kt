package audio.rabid.kards.core.deck.standard

import audio.rabid.kards.core.deck.utils.cartesianProduct

object Decks {

    fun standard() = Deck.create(Suit.ALL.cartesianProduct(Rank.ALL).map { (suit, rank) -> Card(suit, rank) })
}
