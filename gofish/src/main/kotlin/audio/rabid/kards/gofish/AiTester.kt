package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.MovePicker
import audio.rabid.kards.gofish.models.GameOptions
import audio.rabid.kards.gofish.models.PlayerName
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AiTester(
    private val testRuns: Int,
    private val players: List<PlayerName>,
    private val gameOptions: GameOptions,
    seed: Int = 0,
    private val aiBuilder: (PlayerName, Random) -> MovePicker
) {

    data class ScoreCard(
        val wins: AtomicInteger = AtomicInteger(0),
        val ties: AtomicInteger = AtomicInteger(0),
        val losses: AtomicInteger = AtomicInteger(0)
    )

    private val scores = players.associateWith { ScoreCard() }

    private val gameSeeds = Random(seed)

    private val cpuMaxingContext =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 3).asCoroutineDispatcher()

    fun run() = runBlocking {
        val jobs = mutableListOf<Job>()

        println("starting run")
        val start = System.currentTimeMillis()

        repeat(testRuns) { i ->
            val random = Random(gameSeeds.nextLong())

            jobs.add(launch(cpuMaxingContext) {
                if (i % 100 == 0) println("running game $i")
                val playerInfo = players.associateWith { aiBuilder(it, random) }
                val game = GoFishGame(playerInfo, gameOptions, random)
                val winners = game.play(null)
                for (player in players) {
                    when {
                        winners.size == 1 && winners.contains(player) -> scores.getValue(player).wins.incrementAndGet()
                        winners.size > 1 && winners.contains(player) -> scores.getValue(player).ties.incrementAndGet()
                        else -> scores.getValue(player).losses.incrementAndGet()
                    }
                }
            })
        }

        jobs.forEach { it.join() }
        val end = System.currentTimeMillis()
        val maxNameSize = players.map { it.name.length }.max()!!
        for ((playerName, scorecard) in scores) {
            print(playerName.name.padStart(maxNameSize))
            print(": ")
            print(String.format("%7d", scorecard.wins.get()))
            print(String.format("%7.4f", scorecard.wins.get().toDouble() / testRuns.toDouble()))
            print(String.format("%7d", scorecard.ties.get()))
            print(String.format("%7.4f", scorecard.ties.get().toDouble() / testRuns.toDouble()))
            print(String.format("%7d", scorecard.losses.get()))
            print(String.format("%7.4f", scorecard.losses.get().toDouble() / testRuns.toDouble()))
            println()
        }
        println("run time: ${(end - start) / 1000.0}s")
    }
}
