package audio.rabid.kards.gofish.models

data class PastMove(val player: PlayerName, val move: Move, val result: MoveResult, val nextPlayer: Boolean)
