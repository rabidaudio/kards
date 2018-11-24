package audio.rabid.kards.gofish.ui

import audio.rabid.kards.gofish.models.Game
import java.lang.StringBuilder

object DebuggingUI: UI {
    override fun draw(game: Game) {
        println(StringBuilder().apply {
            append("ocean: ${game.ocean.size}\n")
            for (player in game.players) {
                append("${player.name.name}: Hand(${player.hand.size})[${player.hand.joinToString(", ") { it.shortName() }}]\n")
            }
            append("out: ${game.players.flatMap { it.books }.joinToString(", ") { it.rank.shortName() }}\n")
            append("=====================\n\n")
        }.toString())
    }
}
