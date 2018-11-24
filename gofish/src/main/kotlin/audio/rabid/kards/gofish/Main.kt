package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.DumbAi
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.ui.ConsoleUI
import kotlin.random.Random

fun main(args: Array<String>) {

    val random = Random(2000)
    val game = GoFishGame(
            playerInfo = mapOf(
                    PlayerName("one") to DumbAi(random),
                    PlayerName("two") to DumbAi(random),
                    PlayerName("three") to DumbAi(random)
            ),
            random = random
    )

    game.play(ConsoleUI)
}
