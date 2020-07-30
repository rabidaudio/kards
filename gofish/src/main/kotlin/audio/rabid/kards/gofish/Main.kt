package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.DumbAi
import audio.rabid.kards.gofish.ai.cardcounter.SimpleCardCounterAI
import audio.rabid.kards.gofish.models.GameOptions
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.ui.ConsoleUI
import kotlin.random.Random

fun main() {
    // test()
    play()
}

fun test() {
    for (opponents in 1..5) {
        val players = (1..(opponents + 1)).map { PlayerName(it.toString()) }
        val options = GameOptions()
        AiTester(10_000, players, options) { name, random ->
            // SimpleCardCounterAI()
            when (name) {
                PlayerName("1") -> SimpleCardCounterAI()
                else -> DumbAi(random)
            }
        }.run()
    }
}

fun play() {
    val random = Random(0)

    val game = GoFishGame(
        playerInfo = mapOf(
            PlayerName("A") to SimpleCardCounterAI(),
            PlayerName("B") to SimpleCardCounterAI(), // DumbAi(random),
            PlayerName("C") to SimpleCardCounterAI() // DumbAi(random)
        ),
        gameOptions = GameOptions(debug = true),
        random = random
    )

    game.play(ConsoleUI)
}
