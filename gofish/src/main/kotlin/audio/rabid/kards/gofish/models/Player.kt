package audio.rabid.kards.gofish.models

import audio.rabid.kards.core.deck.standard.Hand
import audio.rabid.kards.gofish.ai.GameInfo
import audio.rabid.kards.gofish.ai.MovePicker

data class Player(
    val name: PlayerName,
    val movePicker: MovePicker,
    val hand: Hand,
    val books: MutableSet<Book> = mutableSetOf()
) {
    val info: GameInfo.PlayerInfo
        get() = GameInfo.PlayerInfo(name, hand.size)
}
