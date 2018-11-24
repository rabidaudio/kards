package audio.rabid.kards.core.deck.standard

object Decks {

    fun standard() = Deck(Suit.ALL.cartesianProduct(Rank.ALL).map { (suit, rank) -> Card(suit, rank) })

    private fun <A, B> Collection<A>.cartesianProduct(other: Collection<B>): List<Pair<A, B>> =
            flatMap { a -> other.map { b -> a to b } }
}
