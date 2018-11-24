package audio.rabid.kards.core.utils

class ExpectThrow<T>(
        private val block: () -> T,
        private val words: MutableList<String> = mutableListOf(),
        private var negative: Boolean = false
) {

    val to: ExpectThrow<T> get() {
        words.add("to")
        return this
    }

    val not: ExpectThrow<T> get() = this.apply {
        words.add("not")
        negative = !negative
    }

    val `throw`: Unit get() = `throw`(Throwable::class.java)

    fun <E: Throwable> `throw`(clazz: Class<E>) {
        words.add("throw")
        val res = try {
            block.invoke()
        } catch (e: Throwable) {
            if (negative || !clazz.isAssignableFrom(e.javaClass)) {
                throw AssertionError("Expected block ${words.joinToString(" ")} ${clazz.simpleName} but it threw ${e.javaClass.simpleName}", e)
            }
            return
        }
        if (negative) return
        throw AssertionError("Expected block ${words.joinToString(" ")} ${clazz.simpleName} but it returned $res")
    }

    fun <E: Throwable> `throw`(clazz: Class<E>, exceptionBlock: (E) -> Unit) {
        words.add("throw")
        val res =  try {
            block.invoke()
        } catch (e: Throwable) {
            if (negative || !clazz.isAssignableFrom(e.javaClass)) {
                throw AssertionError("Expected block ${words.joinToString(" ")} ${clazz.simpleName} but it threw ${e.javaClass.simpleName}", e)
            }
            @Suppress("UNCHECKED_CAST")
            exceptionBlock.invoke(e as E)
            return
        }
        if (negative) return
        throw AssertionError("Expected block ${words.joinToString(" ")} ${clazz.simpleName} but it returned $res")
    }

    inline fun <reified E: Throwable>`throw`() = `throw`(E::class.java)

    inline fun <reified E: Throwable>`throw`(noinline exceptionBlock: (E) -> Unit) = `throw`(E::class.java, exceptionBlock)
}

fun <T> expectBlock(block: () -> T) = ExpectThrow(block)
