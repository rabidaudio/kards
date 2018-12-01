package audio.rabid.kards.gofish.models

sealed class Variant {
    object TargetPlayersTurnOnGoFish : Variant()
    object BookPairs : Variant()
    object ScoreByRank : Variant()
    object PassOnlyOneCard : Variant()
    object EndTurnRegardlessOnGoFish : Variant()
}

sealed class EndGameRule {
    object Default : EndGameRule()
    object PlayUntilAllBooked : EndGameRule()
    data class PlayUntilScore(val scoreLimit: Int) : EndGameRule()
    object EmptyHandWinner : EndGameRule()
}
