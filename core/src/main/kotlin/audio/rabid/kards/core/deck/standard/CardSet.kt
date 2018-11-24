@file:Suppress("NOTHING_TO_INLINE")

package audio.rabid.kards.core.deck.standard

import java.util.*
import kotlin.random.Random

typealias Deck = CardSet

typealias Hand = CardSet

inline fun cardSetOf(vararg cards: Card): CardSet = CardSet(cards.toList())

inline fun deckOf(vararg cards: Card): Deck = Deck(cards.toList())

inline fun handOf(vararg cards: Card): Hand = Hand(cards.toList())

class DuplicateCardException(
        val card: Card, val cardSet: CardSet
) : IllegalStateException("Tried to add duplicate card $card to $cardSet")

class CardSet(cards: Collection<Card>): Collection<Card> {

    private var cards = cards.toMutableList()

    init {
        cards.groupBy { it }.values.find { it.size > 1 }?.let { duplicates ->
            throw DuplicateCardException(duplicates.first(), this)
        }
    }

    fun drawFromPosition(position: Int): Card? = when {
        isEmpty() -> null
        position < 0 -> throw IllegalArgumentException("Position cannot be negative (got $position)")
        position >= size -> null
        else -> cards.removeAt(position)
    }

    fun drawOne(): Card? = drawFromPosition(0)

    fun drawOneFromBottom(): Card? = drawFromPosition(size - 1)

    fun insertAtPosition(position: Int, card: Card) {
        when {
            position < 0 -> throw IllegalArgumentException("Position cannot be negative (got $position)")
            position > size ->
                throw IndexOutOfBoundsException("Position $position outside of CardSet bounds (size $size)")
            contains(card) -> throw DuplicateCardException(card, this)
            else -> cards.add(position, card)
        }
    }

    fun placeOnTop(card: Card) = insertAtPosition(0, card)

    fun placeOnBottom(card: Card) = insertAtPosition(size, card)

    fun placeOnBottom(cards: CardSet) = cards.forEach { placeOnBottom(it) }

    fun shuffle(random: Random) {
        cards.shuffle(random)
    }

    /**
     * If there are enough cards left, remove the requested number and return them. If there are not, return null and
     * do not mutate the deck
     */
    fun draw(count: Int): CardSet? = if (count <= size) CardSet((0 until count).mapNotNull { drawOne() }) else null

    fun drawUntil(block: (Card?) -> Boolean): CardSet {
        val drawn = mutableListOf<Card>()
        while (true) {
            val card = cards.firstOrNull()
            if (block.invoke(card)) break
            if (card != null) {
                drawn.add(card)
                cards.removeAt(0)
            }
        }
        return CardSet(drawn)
    }

    fun drawAllWhere(block: (Card) -> Boolean): CardSet {
        val drawn = cards.filter(block)
        cards.removeAll(drawn)
        return CardSet(drawn)
    }

    fun immutableCopy(): Set<Card> = LinkedHashSet(cards)

    override val size: Int get() = cards.size

    override fun contains(element: Card): Boolean = cards.contains(element)

    override fun containsAll(elements: Collection<Card>): Boolean = cards.containsAll(elements)

    override fun isEmpty(): Boolean = cards.isEmpty()

    override fun iterator(): Iterator<Card> = cards.iterator()

    override fun equals(other: Any?): Boolean = other is CardSet && immutableCopy() == other.immutableCopy()

    override fun hashCode(): Int = cards.hashCode()
}
