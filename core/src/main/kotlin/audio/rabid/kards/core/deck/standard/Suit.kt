package audio.rabid.kards.core.deck.standard

enum class Suit {
    Spades, Diamonds, Clubs, Hearts;

    val isRed: Boolean get() = this == Diamonds || this == Hearts

    val isBlack: Boolean get() = !isRed

    fun shortName() = when (this) {
        Spades -> "♠"
        Diamonds -> "♦"
        Clubs -> "♣"
        Hearts -> "♥"
    }

    companion object {
        val ALL = Suit.values().toList()
    }
}
