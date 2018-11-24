package audio.rabid.kards.gofish.ui

import audio.rabid.kards.gofish.models.Game

class CompositeUI(private val children: List<UI>): UI {
    override fun draw(game: Game) = children.forEach { it.draw(game) }
}
