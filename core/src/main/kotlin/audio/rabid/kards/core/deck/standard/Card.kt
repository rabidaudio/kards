package audio.rabid.kards.core.deck.standard

data class Card(val suit: Suit, val rank: Rank) {

    override fun toString(): String = "$rank of $suit"
}

infix fun Rank.of(suit: Suit) = Card(suit, this)
