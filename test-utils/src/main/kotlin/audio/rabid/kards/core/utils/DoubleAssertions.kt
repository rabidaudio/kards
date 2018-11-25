package audio.rabid.kards.core.utils

import com.winterbe.expekt.ExpectDouble

class ExpectWithinDouble(private val expectDouble: ExpectDouble, private val delta: Double) {

    fun of(actual: Double) = expectDouble.closeTo(actual, delta)
}

fun ExpectDouble.within(decimalPlaces: Int): ExpectWithinDouble = ExpectWithinDouble(this, Math.pow(10.0, -1.0 * decimalPlaces))
