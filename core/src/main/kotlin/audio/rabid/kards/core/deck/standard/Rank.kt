package audio.rabid.kards.core.deck.standard

@SuppressWarnings("MagicNumber")
enum class Rank(val shortName: String, val defaultRank: Int) {
    Ace("A", 14),
    Two("2", 2),
    Three("3", 3),
    Four("4", 4),
    Five("5", 5),
    Six("6", 6),
    Seven("7", 7),
    Eight("8", 8),
    Nine("9", 9),
    Ten("10", 10),
    Jack("J", 11),
    Queen("Q", 12),
    King("K", 13);

    val isFace: Boolean
        get() = when (this) {
            Jack, Queen, King -> true
            else -> false
        }

    companion object {
        val ALL = values().toList()

        /**
         * In most games, the value of a rank is the value of the number for number cards, followed by Jack, Queen,
         * King, and finally Ace as the highest. This is the default comparator, but if your game uses a different
         * value, you are free to provide your own implementation.
         */
        val DefaultComparator = compareBy<Rank> { it.defaultRank }
    }
}
