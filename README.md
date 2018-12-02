# Kards

> Card games, written in Kotlin.

I love card games. Right now this project has a light card game builder module plus an
implementation of Go Fish. I'm still using it to experiment with game AIs, but eventually
I want this project to serve several purposes:

- A general library for creating card games in Kotlin
- A collection of implementations of common card games, with interfaces to be usable both
  with human and AI players, and independent of UI
- Tools for creating card game AI and pitting them against one another

## TODO

- Go Fish
  - [X] Refactor AI for performance and clarity
  - [ ] Be smarter about selecting moves - don't just pick the highest scoring move,
    be selective about what you reveal to other players
  - [ ] add interface to allow real players to play
  - [ ] train neural net against ai to see if it can find a better solution
  - [ ] support game variants (play until all books dealt, etc)

- Add more games
  - [ ] betting games, e.g. Poker?
  - [ ] Trick-taking games, e.g. Hearts, Spades

## Links

- [Go fish AI stats](https://docs.google.com/spreadsheets/d/12TJxPpsdHSiXgNui7Ahn_Z2JR-y0w9TD7Vdj-O5krVY/edit?usp=sharing)
