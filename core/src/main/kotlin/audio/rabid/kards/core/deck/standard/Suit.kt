package audio.rabid.kards.core.deck.standard

enum class Suit(val shortName: String) {
    Spades("♠"), Diamonds("♦"), Clubs("♣"), Hearts("♥");

    val isRed: Boolean get() = this == Diamonds || this == Hearts

    val isBlack: Boolean get() = !isRed

    companion object {
        val ALL = Suit.values().toList()
    }
}
