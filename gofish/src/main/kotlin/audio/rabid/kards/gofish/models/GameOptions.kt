package audio.rabid.kards.gofish.models

data class GameOptions(
    val startingHandSize: Int = 7,
    val debug: Boolean = false
//    val variants: List<Variant> = emptyList(),
//    val endGameRule: EndGameRule = EndGameRule.Default
)
