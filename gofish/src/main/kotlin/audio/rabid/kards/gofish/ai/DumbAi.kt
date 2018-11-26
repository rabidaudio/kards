package audio.rabid.kards.gofish.ai

import audio.rabid.kards.gofish.models.Move
import kotlin.random.Random

/**
 * This is a dumb GoFish player which just picks legal moves at random
 */
class DumbAi(private val random: Random) : MovePicker {

    override fun move(gameInfo: GameInfo): Move = gameInfo.possibleMoves.random(random)
}
