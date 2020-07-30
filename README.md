# Kards

> Card games, written in Kotlin.

I love card games. Right now this project has a light card game builder module plus an
implementation of Go Fish. I started with Go Fish because it is a simple, pretty easily
solved game to start with before getting to harder games. I'm still using it to
experiment with game AIs, but eventually I want this project to serve several purposes:

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







Possible improved solution:

Calculate scores for each unknown player, determine their best move (move which maximizes books, rather than getting cards), then score each possible move by number of earned books - number of enemies earned books


Possible improved solution:

Monte Carlo tree search
alpha-beta pruning
minimax

https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning



alpha-beta pruning: end search with win or loss based on which player eventually wins that rank, not the whole game. This should reduce the search space

