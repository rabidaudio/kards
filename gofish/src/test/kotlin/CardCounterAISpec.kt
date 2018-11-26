package audio.rabid.kards.gofish.ai

import audio.rabid.kards.core.deck.standard.Rank.Ace
import audio.rabid.kards.core.deck.standard.Rank.Queen
import audio.rabid.kards.core.deck.standard.Rank.Three
import audio.rabid.kards.core.deck.standard.Rank.Two
import audio.rabid.kards.core.deck.standard.Suit.Clubs
import audio.rabid.kards.core.deck.standard.Suit.Hearts
import audio.rabid.kards.core.deck.standard.Suit.Spades
import audio.rabid.kards.core.deck.standard.of
import audio.rabid.kards.core.utils.within
import audio.rabid.kards.gofish.ai.cardcounter.CardCounterAi
import audio.rabid.kards.gofish.models.GoFish
import audio.rabid.kards.gofish.models.HandOver
import audio.rabid.kards.gofish.models.Move
import audio.rabid.kards.gofish.models.PastMove
import audio.rabid.kards.gofish.models.PlayerName
import com.winterbe.expekt.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object CardCounterAISpec : Spek({

    describe("CardCounterAI.Picker") {

        val A = PlayerName("a")
        val B = PlayerName("b")
        val C = PlayerName("c")

        context("the beginning of the game") {
            // each player has 3 cards
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 3, emptySet())
                ),
                pastMoves = emptyList()
            )

            val picker = CardCounterAi.Picker(game)

            it("should know where the cards in it's hand are") {
                expect(picker.getProbability(A, Ace)).to.equal(1.0)
                expect(picker.getProbability(A, Two)).to.equal(1.0)
                expect(picker.getProbability(A, Three)).to.equal(1.0)
            }

            it("should know it does not have other cards in it's hand") {
                expect(picker.getProbability(A, Queen)).to.equal(0.0)
            }

            it("should know the probability of the other player having its missing cards") {
                val probability =
                        /* the outstanding number of aces */ 3.0 *
                        /* the probability it's in his hand */ (
                        /* the unknown spots in his hand */ 3.0 /
                        /* the unknown cards */ (52.0 - 3.0)
                        )
                expect(picker.getProbability(B, Ace)).to.be.within(2).of(probability)
            }

            it("should know the probability of the other player having unknown cards") {
                val probability =
                        /* the outstanding number of queens */ 4.0 *
                        /* the probability it's in his hand */ (
                        /* the unknown spots in his hand */ 3.0 /
                        /* the unknown cards */ (52.0 - 3.0)
                        )
                expect(picker.getProbability(B, Queen)).to.be.within(2).of(probability)
            }
        }


        context("the after they ask for something") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 4, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Queen, A), GoFish, true, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least one queen") {
                expect(picker.getProbability(B, Queen)).to.be.at.least(1.0)
            }
        }

        context("after winning something") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 2, emptySet()),
                    GameInfo.PlayerInfo(B, 4, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Ace, A), HandOver(setOf(Ace of Clubs)), false, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least 2 Aces") {
                expect(picker.getProbability(B, Ace)).to.be.at.least(2.0)
            }
        }

        context("the after they ask another player for something") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 4, emptySet()),
                    GameInfo.PlayerInfo(C, 3, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Queen, C), GoFish, true, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least one queen") {
                expect(picker.getProbability(B, Queen)).to.be.at.least(1.0)
            }

            it("should know that C has no queens") {
                expect(picker.getProbability(C, Queen)).to.equal(0.0)
            }
        }

        context("when drawing the card after go fish") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 4, emptySet()),
                    GameInfo.PlayerInfo(C, 3, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Queen, C), GoFish, false, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least two queens") {
                expect(picker.getProbability(B, Queen)).to.be.at.least(2.0)
            }

            it("should know that C has no queens") {
                expect(picker.getProbability(C, Queen)).to.equal(0.0)
            }
        }

        context("when asking someone else for what i have and go fish") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 4, emptySet()),
                    GameInfo.PlayerInfo(C, 3, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Ace, C), GoFish, true, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least one ace") {
                expect(picker.getProbability(B, Ace)).to.be.at.least(1.0)
            }

            it("should know that C has no aces") {
                expect(picker.getProbability(C, Ace)).to.equal(0.0)
            }

            it("should know the probability of B having more aces") {
                val probability =
                        /* the outstanding number of aces */ 2.0 *
                        /* the probability it's in his hand */ (
                        /* the unknown spots in his hand */ 3.0 /
                        /* the unknown cards */ (
                        /* the total in play*/ 52.0 -
                        /* the ones in my hand*/ 3.0 -
                        /* the known ace in Bs hand */ 1.0
                        )
                        )
                expect(picker.getProbability(B, Ace)).to.be.within(2).of(1.0 + probability)
            }
        }

        context("when asking someone else for what i have and win") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 4, emptySet()),
                    GameInfo.PlayerInfo(C, 2, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Ace, C), HandOver(setOf(Ace of Hearts)), false, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least two aces") {
                expect(picker.getProbability(B, Ace)).to.be.at.least(2.0)
            }

            it("should know that C has no aces") {
                expect(picker.getProbability(C, Ace)).to.equal(0.0)
            }

            it("should know the probability of B having more aces") {
                val probability =
                        /* the outstanding number of aces */ 1.0 *
                        /* the probability it's in his hand */ (
                        /* the unknown spots in his hand */ 2.0 /
                        /* the unknown cards */ (
                        /* the total in play */ 52.0 -
                        /* the ones in my hand */ 3.0 -
                        /* the known ace in Bs hand */ 2.0
                        )
                        )
                expect(picker.getProbability(B, Ace)).to.be.within(2).of(2.0 + probability)
            }
        }

        context("when asking someone else for what i have and draw on go fish") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 4, emptySet()),
                    GameInfo.PlayerInfo(C, 3, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Ace, C), GoFish, false, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least two aces") {
                expect(picker.getProbability(B, Ace)).to.be.at.least(2.0)
            }

            it("should know that C has no aces") {
                expect(picker.getProbability(C, Ace)).to.equal(0.0)
            }

            it("should know the probability of B having more aces") {
                val probability =
                        /* the outstanding number of aces */ 1.0 *
                        /* the probability it's in his hand */ (
                        /* the unknown spots in his hand */ 2.0 /
                        /* the unknown cards */ (
                        /* the total in play*/ 52.0 -
                        /* the ones in my hand*/ 3.0 -
                        /* the known aces in Bs hand */ 2.0
                        )
                        )
                expect(picker.getProbability(B, Ace)).to.be.within(2).of(2.0 + probability)
            }
        }

        context("when asking someone else for what i have and win multiple") {
            val game = GameInfo(
                myPlayerName = A,
                myHand = setOf(Ace of Clubs, Two of Clubs, Three of Clubs),
                players = listOf(
                    GameInfo.PlayerInfo(A, 3, emptySet()),
                    GameInfo.PlayerInfo(B, 5, emptySet()),
                    GameInfo.PlayerInfo(C, 1, emptySet())
                ),
                pastMoves = listOf(
                    PastMove(B, Move(Ace, C), HandOver(setOf(Ace of Hearts, Ace of Spades)), false, null)
                )
            )

            val picker = CardCounterAi.Picker(game)

            it("should know that B has at least three aces") {
                expect(picker.getProbability(B, Ace)).to.be.at.least(3.0)
            }

            it("should know that C has no aces") {
                expect(picker.getProbability(C, Ace)).to.equal(0.0)
            }

            it("should know the probability of B having more aces is zero because I have the remaining ace") {
                expect(picker.getProbability(B, Ace)).to.equal(3.0)
            }
        }
    }
})
