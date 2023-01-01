@file:Suppress("NOTHING_TO_INLINE")

package audio.rabid.kards.core.deck.standard

import kotlin.random.Random

typealias Deck = CardSet

typealias Hand = CardSet

inline fun cardSetOf(vararg cards: Card): CardSet = CardSet.create(cards.toMutableList())

inline fun deckOf(vararg cards: Card): Deck = Deck.create(cards.toList())

inline fun handOf(vararg cards: Card): Hand = Hand.create(cards.toList())

class DuplicateCardException(
    val card: Card,
    val cardSet: CardSet
) : IllegalStateException("Tried to add duplicate card $card to $cardSet")

class CardSet private constructor(private val cards: MutableList<Card>) : Collection<Card> by cards {

    companion object {
        fun create(cards: Collection<Card>) = CardSet(cards.toMutableList())
    }

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

    fun drawUntil(includeMatched: Boolean = true, block: (Card) -> Boolean): CardSet {
        val drawn = mutableListOf<Card>()
        while (true) {
            val card = cards.firstOrNull() ?: return CardSet(drawn)
            if (block.invoke(card)) {
                if (includeMatched) {
                    drawn.add(card)
                    cards.removeAt(0)
                }
                return CardSet(drawn)
            }
            drawn.add(card)
            cards.removeAt(0)
        }
    }

    fun drawAllWhere(block: (Card) -> Boolean): CardSet {
        val drawn = cards.filter(block)
        cards.removeAll(drawn)
        return create(drawn)
    }

    fun insertAtPosition(position: Int, card: Card) {
        when {
            position < 0 -> throw IllegalArgumentException("Position cannot be negative (got $position)")
            position > size ->
                throw IndexOutOfBoundsException("Position $position outside of CardSet bounds (size $size)")
            contains(card) -> throw DuplicateCardException(card, this)
            else -> cards.add(position, card)
        }
    }

    fun shuffle(random: Random) {
        cards.shuffle(random)
    }

    fun sort(comparator: Comparator<Card> = Card.DefaultComparator) {
        cards.sortWith(comparator)
    }

    fun immutableCopy(): Set<Card> = LinkedHashSet(cards)

    override fun equals(other: Any?): Boolean = other is CardSet && cards == other.cards

    override fun hashCode(): Int = cards.hashCode()
}

fun CardSet.drawOne(): Card? = drawFromPosition(0)

fun CardSet.drawOneFromBottom(): Card? = drawFromPosition(size - 1)

/**
 * If there are enough cards left, remove the requested number and return them. If there are not, return null and
 * do not mutate the deck
 */
fun CardSet.draw(count: Int): CardSet? =
    if (count <= size) CardSet.create((0 until count).mapNotNull { drawOne() }) else null

fun CardSet.placeOnTop(card: Card) = insertAtPosition(0, card)

fun CardSet.placeOnBottom(card: Card) = insertAtPosition(size, card)

fun CardSet.placeOnBottom(cards: CardSet) = cards.forEach { placeOnBottom(it) }
