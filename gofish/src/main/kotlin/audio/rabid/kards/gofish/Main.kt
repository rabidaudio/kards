package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.DumbAi
import audio.rabid.kards.gofish.ai.cardcounter.BookProbabilityCardCounterAI
import audio.rabid.kards.gofish.ai.cardcounter.SimpleCardCounterAI
import audio.rabid.kards.gofish.ai.cardcounter.montecarlo.MonteCarloTreeSearchAI
import audio.rabid.kards.gofish.models.GameOptions
import audio.rabid.kards.gofish.models.PlayerName
import audio.rabid.kards.gofish.ui.ConsoleUI
import kotlin.random.Random

fun main(args: Array<String>) {
//    test()
    play()
}

fun test() {
    val opponents = 3
    val players = (1..(opponents + 1)).map { PlayerName(it.toString()) }
    val options = GameOptions()
    AiTester(10_000, players, options) { name, random ->
        when (name) {
            PlayerName("1") -> MonteCarloTreeSearchAI(random, iterationsPerMove = 100)
            else -> SimpleCardCounterAI()
        }
    }.run()
}

fun play() {
    val random = Random(0)

    val game = GoFishGame(
        playerInfo = mapOf(
            PlayerName("A") to MonteCarloTreeSearchAI(random),
            PlayerName("B") to DumbAi(random),
            PlayerName("C") to DumbAi(random)
        ),
        gameOptions = GameOptions(debug = true),
        random = random
    )

    game.play(ConsoleUI)
}
