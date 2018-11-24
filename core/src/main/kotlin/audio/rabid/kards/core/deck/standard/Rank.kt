package audio.rabid.kards.core.deck.standard

enum class Rank {
    Ace,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Ten,
    Jack,
    Queen,
    King;

    val isFace: Boolean get() = when (this) {
        Jack, Queen, King -> true
        else -> false
    }

    fun shortName() = when (this) {
        Ace -> "A"
        Two -> "2"
        Three -> "3"
        Four -> "4"
        Five -> "5"
        Six -> "6"
        Seven -> "7"
        Eight -> "8"
        Nine -> "9"
        Ten -> "10"
        Jack -> "J"
        Queen -> "Q"
        King -> "K"
    }

    companion object {
        val ALL = values().toList()
    }

    /**
     * In most games, the value of a rank is the value of the number for number cards, followed by Jack, Queen, King,
     * and finally Ace as the highest. This is the default comparator, but if your game uses a different value, you are
     * free to provide your own implementation.
     */
    object DefaultComparator: Comparator<Rank> {
        override fun compare(o1: Rank, o2: Rank): Int = o1.toInt() - o2.toInt()

        private fun Rank.toInt() = when (this) {
            Two -> 2
            Three -> 3
            Four -> 4
            Five -> 5
            Six -> 6
            Seven -> 7
            Eight -> 8
            Nine -> 9
            Ten -> 10
            Jack -> 11
            Queen -> 12
            King -> 13
            Ace -> 14
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Collection<Rank>.sorted(comparator: Comparator<Rank> = Rank.DefaultComparator) = sortedWith(comparator)
