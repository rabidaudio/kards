package audio.rabid.kards.core.deck.standard

import audio.rabid.kards.core.deck.standard.Rank.Ace
import audio.rabid.kards.core.deck.standard.Rank.Queen
import audio.rabid.kards.core.deck.standard.Rank.Three
import audio.rabid.kards.core.deck.standard.Rank.Two
import audio.rabid.kards.core.deck.standard.Suit.Clubs
import audio.rabid.kards.core.deck.standard.Suit.Diamonds
import audio.rabid.kards.core.deck.standard.Suit.Hearts
import audio.rabid.kards.core.deck.standard.Suit.Spades
import audio.rabid.kards.core.utils.expectBlock
import com.winterbe.expekt.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.random.Random


object CardSetSpec : Spek({

    describe("CardSet") {

        val set by memoized {
            cardSetOf(
                Ace of Clubs,
                Two of Hearts,
                Three of Spades
            )
        }

        describe("constructor") {

            it("should contain a list of cards") {
                expect(set).to.have.size(3)
            }

            it("should be ordered") {
                expect(set.first()).to.equal(Ace of Clubs)
                expect(set.last()).to.equal(Three of Spades)
            }

            it("should have basic collection properties") {
                expect(set.iterator()).not.to.be.`null`
                expect(set.contains(Two of Hearts)).to.be.`true`
            }

            it("should not hold duplicates of the same card") {
                expectBlock {
                    cardSetOf(
                        Queen of Spades,
                        Queen of Spades
                    )
                }.to.`throw`<DuplicateCardException> {
                    expect(it.card).to.equal(Queen of Spades)
                }
            }

            it("should allow empty sets") {
                expect(cardSetOf()).to.be.empty
            }
        }

        describe("immutableCopy") {

            val copy by memoized { set.immutableCopy() }

            it("should allow immutable copies to be made") {
                expect(copy).to.have.size(3)
            }

            it("should have the same order") {
                expect(copy.toList()[0]).to.equal(Ace of Clubs)
                expect(copy.toList()[1]).to.equal(Two of Hearts)
                expect(copy.toList()[2]).to.equal(Three of Spades)
            }

            it("should not mutate with changes to the set") {
                expect(copy).to.have.size(3)
                set.drawOne()
                expect(copy).to.have.size(3)
            }
        }

        describe("drawing") {

            describe("drawOne") {

                it("should draw off the top") {
                    expect(set.drawOne()).to.equal(Ace of Clubs)
                    expect(set.drawOne()).to.equal(Two of Hearts)
                    expect(set.drawOne()).to.equal(Three of Spades)
                }

                it("should remove the drawn item from the list") {
                    set.drawOne()
                    expect(set).to.have.size(2)
                }

                it("should return null when there are no more cards to draw") {
                    expect(set.drawOne()).not.to.be.`null`
                    expect(set.drawOne()).not.to.be.`null`
                    expect(set.drawOne()).not.to.be.`null`
                    expect(set.drawOne()).to.be.`null`
                    expect(set).to.be.empty
                }
            }

            describe("drawOneFromBottom") {

                it("should draw off the bottom") {
                    expect(set.drawOneFromBottom()).to.equal(Three of Spades)
                    expect(set.drawOneFromBottom()).to.equal(Two of Hearts)
                    expect(set.drawOneFromBottom()).to.equal(Ace of Clubs)
                }

                it("should remove the drawn item from the list") {
                    set.drawOneFromBottom()
                    expect(set).to.have.size(2)
                }

                it("should return null when there are no more cards to draw") {
                    expect(set.drawOneFromBottom()).not.to.be.`null`
                    expect(set.drawOneFromBottom()).not.to.be.`null`
                    expect(set.drawOneFromBottom()).not.to.be.`null`
                    expect(set.drawOneFromBottom()).to.be.`null`
                    expect(set).to.be.empty
                }
            }

            describe("draw") {

                it("should allow drawing multiple cards off the top") {
                    val drawn = set.draw(2)
                    expect(drawn).not.to.be.`null`
                    expect(drawn).to.equal(cardSetOf(Ace of Clubs, Two of Hearts))
                }

                it("should allow drawing no cards") {
                    expect(set.draw(0)).to.equal(cardSetOf())
                }

                it("should allow drawing all the cards") {
                    val drawn = set.draw(3)
                    expect(drawn).to.have.size(3)
                    expect(set).to.have.size(0)
                }

                it("should return null if you try to draw more than the available cards") {
                    expect(set.draw(4)).to.be.`null`
                    expect(set).to.have.size(3)
                }
            }

            describe("drawFromPosition") {

                it("should allow drawing from anywhere in the deck") {
                    expect(set.drawFromPosition(1)).to.equal(Two of Hearts)
                    expect(set).to.have.size(2)
                }

                it("should error if you try to draw from a negative position") {
                    expectBlock { set.drawFromPosition(-1) }.to.`throw`<IllegalArgumentException>()
                }

                it("should return null for drawing from an empty set") {
                    expect(cardSetOf().drawFromPosition(0)).to.be.`null`
                }

                it("should return null if you try and draw from past the end") {
                    expect(set.drawFromPosition(4)).to.be.`null`
                }
            }

            describe("drawUntil") {

                it("should allow drawing from the top until a condition is met") {
                    val drawn = set.drawUntil { it?.rank == Two }
                    expect(drawn).to.equal(cardSetOf(Ace of Clubs))
                    expect(set.size).to.equal(2)
                }

                it("should be exclusive (exclude the card that the block returned false on)") {
                    val drawn = set.drawUntil { it?.rank == Two }
                    expect(drawn).not.to.contain(Two of Hearts)
                    expect(set).to.contain(Two of Hearts)
                }

                it("should return null to the block when all cards are drawn") {
                    val drawn = set.drawUntil { it == null }
                    expect(drawn).to.have.size(3)
                    expect(set).to.be.empty
                }

                it("should allow drawing no cards") {
                    val drawn = set.drawUntil { true }
                    expect(drawn).to.be.empty
                    expect(set).to.have.size(3)
                }
            }

            describe("drawAllWhere") {

                it("should allow drawing all cards that match the block") {
                    val reds = set.drawAllWhere { it.suit.isBlack }
                    expect(reds).to.equal(
                        cardSetOf(
                            Ace of Clubs,
                            Three of Spades
                        )
                    )
                    expect(set).to.have.size(1)
                }

                it("should allow drawing all cards") {
                    val all = set.drawAllWhere { true }
                    expect(all).to.have.size(3)
                    expect(set).to.have.size(0)
                }

                it("should allow drawing no cards") {
                    val all = set.drawAllWhere { false }
                    expect(all).to.have.size(0)
                    expect(set).to.have.size(3)
                }
            }
        }

        describe("placing") {
            describe("placeOnTop") {

                it("should allow adding cards to the top of the pile") {
                    set.placeOnTop(Queen of Spades)
                    expect(set.first()).to.equal(Queen of Spades)
                    expect(set).to.have.size(4)
                }

                it("should error if adding duplicate cards") {
                    expectBlock { set.placeOnTop(Three of Spades) }.to.`throw`<DuplicateCardException> {
                        expect(it.card).to.equal(Three of Spades)
                        expect(it.cardSet).to.equal(set)
                    }
                }
            }

            describe("placeOnBottom") {

                it("should allow adding cards to the bottom of the pile") {
                    set.placeOnBottom(Queen of Spades)
                    expect(set.last()).to.equal(Queen of Spades)
                    expect(set).to.have.size(4)
                }

                it("should error if adding duplicate cards") {
                    expectBlock { set.placeOnBottom(Three of Spades) }.to.`throw`<DuplicateCardException> {
                        expect(it.card).to.equal(Three of Spades)
                        expect(it.cardSet).to.equal(set)
                    }
                }

                it("should allow adding multiple cards to the bottom of the pile") {
                    set.placeOnBottom(cardSetOf(Queen of Spades, Queen of Diamonds))
                    expect(set.last()).to.equal(Queen of Diamonds)
                    expect(set).to.have.size(5)
                }

                it("should error if adding multiple duplicate cards") {
                    expectBlock {
                        set.placeOnBottom(cardSetOf(Queen of Spades, Queen of Diamonds, Three of Spades))
                    }.to.`throw`<DuplicateCardException> {
                        expect(it.card).to.equal(Three of Spades)
                        expect(it.cardSet).to.equal(set)
                    }
                }
            }

            describe("insertAtPosition") {

                it("should allow placing cards at arbitrary positions") {
                    set.insertAtPosition(2, Queen of Spades)
                    expect(set).to.equal(
                        cardSetOf(
                            Ace of Clubs,
                            Two of Hearts,
                            Queen of Spades,
                            Three of Spades
                        )
                    )
                }

                it("should allow placing cards at the beginning") {
                    set.insertAtPosition(0, Queen of Spades)
                    expect(set).to.equal(
                        cardSetOf(
                            Queen of Spades,
                            Ace of Clubs,
                            Two of Hearts,
                            Three of Spades
                        )
                    )
                }

                it("should allow placing cards at the end") {
                    set.insertAtPosition(3, Queen of Spades)
                    expect(set).to.equal(
                        cardSetOf(
                            Ace of Clubs,
                            Two of Hearts,
                            Three of Spades,
                            Queen of Spades
                        )
                    )
                }

                it("should error if adding duplicate cards") {
                    expectBlock {
                        set.insertAtPosition(2, Three of Spades)
                    }.to.`throw`<DuplicateCardException> {
                        expect(it.card).to.equal(Three of Spades)
                        expect(it.cardSet).to.equal(set)
                    }
                }

                it("should error if adding at negative positions") {
                    expectBlock {
                        set.insertAtPosition(-1, Queen of Spades)
                    }.to.`throw`<IllegalArgumentException>()
                }

                it("should error if adding past the bounds") {
                    expectBlock {
                        set.insertAtPosition(4, Queen of Spades)
                    }.to.`throw`<IndexOutOfBoundsException>()
                }
            }
        }

        describe("shuffle") {

            val random by memoized { Random(0) }

            it("should shuffle the set in place") {
                set.shuffle(random)
                expect(set).to.equal(
                    cardSetOf(
                        Two of Hearts,
                        Three of Spades,
                        Ace of Clubs
                    )
                )
                expect(set.containsAll(listOf(Ace of Clubs, Two of Hearts, Three of Spades)))
            }

            it("should have the same items after shuffling") {
                val items = set.immutableCopy()
                set.shuffle(random)
                expect(set).to.have.size(3)
                items.forEach { expect(set).to.contain(it) }
            }
        }
    }
})
