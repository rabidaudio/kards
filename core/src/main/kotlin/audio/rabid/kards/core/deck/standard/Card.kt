package audio.rabid.kards.core.deck.standard

data class Card(val suit: Suit, val rank: Rank) {

    override fun toString(): String = "$rank of $suit"

    fun shortName() = "${rank.shortName()}${suit.shortName()}"

    fun matches(rank: Rank): Boolean = this.rank == rank

    fun matches(suit: Suit): Boolean = this.suit == suit

    companion object {
        val DefaultComparator = compareBy(Rank.DefaultComparator, Card::rank).then(compareBy { it.suit })
    }
}

infix fun Rank.of(suit: Suit) = Card(suit, this)
