package audio.rabid.kards.gofish.ui

import audio.rabid.kards.gofish.ai.CardCounterAi
import audio.rabid.kards.gofish.models.Game
import java.lang.StringBuilder

object DebuggingUI: UI {
    override fun draw(game: Game) {
        println("ocean: ${game.ocean.size}")
        for (player in game.players) {
            println("${player.name.name}: Hand(${player.hand.size})[${player.hand.joinToString(", ") { it.shortName() }}]")
        }
        println("out: ${game.players.flatMap { it.books }.joinToString(", ") { it.rank.shortName() }}")
        println("=====================")
        val s = game.getGameInfo(game.players.first().name)
        CardCounterAi.Picker(s).debug()
        print("=====================\n\n")
    }
}
