package audio.rabid.kards.core.deck.utils

fun <A, B> Collection<A>.cartesianProduct(other: Collection<B>): List<Pair<A, B>> {
    return flatMap { a -> other.map { b -> a to b } }
}

//fun <A, B> Collection<A>.cartesianProduct(other: Collection<B>): Iterable<Pair<A, B>> {
//    return object : Iterable<Pair<A, B>> {
//        override fun iterator(): Iterator<Pair<A, B>> {
//            return iterator {
//                for (a in this@cartesianProduct) {
//                    for (b in other) {
//                        yield(a to b)
//                    }
//                }
//            }
//        }
//    }
//}
