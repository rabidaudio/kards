package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.DumbAi
import audio.rabid.kards.gofish.ai.cardcounter.CardCounterAi
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.ui.ConsoleUI
import kotlin.random.Random

fun main(args: Array<String>) {
//    AiTester(CardCounterAi()).run()

    val random = Random(0)

    val game = GoFishGame(
        playerInfo = mapOf(
            PlayerName("smartypants") to CardCounterAi(),
            PlayerName("dummy1") to DumbAi(random),
            PlayerName("dummy2") to DumbAi(random)
        ),
        random = random
    )

    game.play(ConsoleUI)
}
