package audio.rabid.kards.gofish.ai.cardcounter

/**
 * This interface helps index into a 2-D array of A rows and B columns implemented as a 1-D array
 */
interface ArrayHelper<A, B> {

    val rows: List<A>
    val columns: List<B>

    val totalSize get() = rows.size * columns.size

    fun getIndex(a: A, b: B): Int = (rows.indexOf(a) * columns.size) + columns.indexOf(b)

    fun decodeIndex(index: Int): Pair<A, B> = Pair(rows[index / columns.size], columns[index % columns.size])
}
