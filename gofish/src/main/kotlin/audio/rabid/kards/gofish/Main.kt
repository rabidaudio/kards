package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.DumbAi
import audio.rabid.kards.gofish.ai.cardcounter.CardCounterAi
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.ui.CompositeUI
import audio.rabid.kards.gofish.ui.ConsoleUI
import audio.rabid.kards.gofish.ui.DebuggingUI
import kotlin.random.Random

fun main(args: Array<String>) {
    test()
}

fun test() {
    val players = listOf(PlayerName("1"), PlayerName("2"), PlayerName("3"))
    AiTester(10_000, players) { name, random ->
        CardCounterAi
//        when (name) {
//            PlayerName("1") -> CardCounterAi
//            else -> DumbAi(random)
//        }
    }.run()
}

fun play() {
    val random = Random(0)

    val game = GoFishGame(
        playerInfo = mapOf(
            PlayerName("smartypants") to CardCounterAi,
            PlayerName("dummy1") to DumbAi(random),
            PlayerName("dummy2") to DumbAi(random)
        ),
        random = random
    )

    game.play(CompositeUI(listOf(ConsoleUI, DebuggingUI)))
}