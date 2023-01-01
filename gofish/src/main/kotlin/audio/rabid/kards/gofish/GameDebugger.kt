package audio.rabid.kards.gofish

import audio.rabid.kards.gofish.ai.cardcounter.CardCounterAi
import audio.rabid.kards.gofish.models.GameState

fun GameState.debug() {
    println("ocean: ${ocean.size}")
    for (player in players) {
        val hand = player.hand.joinToString(", ") { it.shortName }
        println("${player.name.name}: Hand(${player.hand.size})[$hand]")
    }
    println("out: ${players.flatMap { it.books }.joinToString(", ") { it.rank.shortName }}")
    println("=====================")
    players.first().let { player -> (player.movePicker as? CardCounterAi)?.debug(getTurnInfo(player.name)) }
    print("=====================\n\n")
}
