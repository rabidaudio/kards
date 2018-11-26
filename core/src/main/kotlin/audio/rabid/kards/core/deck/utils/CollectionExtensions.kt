package audio.rabid.kards.core.deck.utils

fun <A, B> Collection<A>.cartesianProduct(other: Collection<B>): List<Pair<A, B>> =
    flatMap { a -> other.map { b -> a to b } }
