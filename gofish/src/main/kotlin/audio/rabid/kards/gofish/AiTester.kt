package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.DumbAi
import audio.rabid.kards.gofish.ai.MovePicker
import audio.rabid.kards.gofish.models.PlayerName
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking

class AiTester(
    private val aiUnderTest: MovePicker,
    private val otherPlayers: Int = 2,
    private val testRuns: Int = 10_000
) {

    private val seeds = Random(0)
    private val wins = AtomicInteger(0)
    private val ties = AtomicInteger(0)
    private val losses = AtomicInteger(0)

    private val cpuMaxingContext =
        newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors() + 3, "CPU")

    fun run() = runBlocking {
        val jobs = mutableListOf<Job>()

        println("starting run")
        val start = System.currentTimeMillis()

        repeat(testRuns) { i ->
            val random = Random(seeds.nextLong())

            jobs.add(launch(cpuMaxingContext) {
                if (i % 100 == 0) println("running game $i")
                val playerInfo = mapOf(PlayerName("smart") to aiUnderTest) +
                        (0 until otherPlayers).map { i -> PlayerName("dummy$i") to DumbAi(random) }.toMap()
                val game = GoFishGame(playerInfo, random)
                val winners = game.play(null)
                when {
                    winners.size == 1 && winners.contains(PlayerName("smart")) -> wins.incrementAndGet()
                    winners.size > 1 && winners.contains(PlayerName("smart")) -> ties.incrementAndGet()
                    else -> losses.incrementAndGet()
                }
            })
        }

        jobs.forEach { it.join() }
        val end = System.currentTimeMillis()
        println(
            "wins: ${wins.get()} (${wins.get() / testRuns.toDouble()})\t" +
                    "ties: ${ties.get()} (${ties.get() / testRuns.toDouble()})\t" +
                    "losses: ${losses.get()} (${losses.get() / testRuns.toDouble()})\t" +
                    "run time: ${(end - start) / 1000.0}s"
        )
    }
}
