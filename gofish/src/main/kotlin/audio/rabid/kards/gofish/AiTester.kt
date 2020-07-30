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

    class ScoreCard {
        private val wins: AtomicInteger = AtomicInteger(0)
        private val ties: AtomicInteger = AtomicInteger(0)
        private val losses: AtomicInteger = AtomicInteger(0)

        fun trackWin() = wins.incrementAndGet()
        fun trackTie() = ties.incrementAndGet()
        fun trackLoss() = losses.incrementAndGet()

        private val gamesPlayed: Int
            get() = wins.get() + ties.get() + losses.get()

        val winProbability: Double
            get() = wins.get().toDouble() / gamesPlayed.toDouble()

        val tieProbability: Double
            get() = ties.get().toDouble() / gamesPlayed.toDouble()

        val lossProbability: Double
            get() = losses.get().toDouble() / gamesPlayed.toDouble()

        // see: https://en.wikipedia.org/wiki/Elo_rating_system#Mathematical_details
        val score: Double get() = winProbability + 0.5 * tieProbability
    }

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
                val playerInfo = players.associateWith { aiBuilder.invoke(it, random) }
                val game = GoFishGame(playerInfo, gameOptions, random)
                val winners = game.play(null)
                for (player in players) {
                    when {
                        winners.size == 1 && winners.contains(player) -> scores[player]!!.trackWin()
                        winners.size > 1 && winners.contains(player) -> scores[player]!!.trackTie()
                        else -> scores[player]!!.trackLoss()
                    }
                }
            })
        }

        jobs.forEach { it.join() }
        val end = System.currentTimeMillis()
        val completeNames = players.associateWith { p -> aiBuilder.invoke(p, Random).javaClass.simpleName }
        val maxNameSize = completeNames.values.map { it.length }.max()!!
        for ((playerName, scorecard) in scores) {
            print(completeNames[playerName]!!.padStart(maxNameSize))
            print(": ")
            print(String.format("%7.4f", scorecard.winProbability))
            print(String.format("%7.4f", scorecard.tieProbability))
            print(String.format("%7.4f", scorecard.lossProbability))
            print(String.format("%7.4f", scorecard.score))
            println()
        }
        println("run time: ${(end - start) / 1000.0}s")
    }
}
