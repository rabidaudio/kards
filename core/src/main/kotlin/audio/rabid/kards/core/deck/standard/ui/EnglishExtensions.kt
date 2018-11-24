package audio.rabid.kards.core.deck.standard.ui

import audio.rabid.kards.core.deck.standard.Rank
import audio.rabid.kards.core.deck.standard.Suit

fun Rank.pluralize() = when (this) {
    Rank.Six -> "${name}es"
    else -> "${name}s"
}

fun Suit.singularize() = name.removeSuffix("s")
