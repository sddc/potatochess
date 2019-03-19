# potatochess
Chess engine written in Java. Still a work in progress =]

## Engine Features
* Magic Bitboards for Sliding Piece Move Generation
* Negamax with Alpha Beta Pruning
* Iterative Deepening
* Transposition Table
* MVV/LVA
* Quiescence Search
* Piece-Square Tables
* UCI Protocol

## Build
Run the following command in the project root
```
./gradlew shadowJar
```
to build a jar file in this directory
```
build/libs/potatochess-all.jar
```
Install the engine on a UCI compatible GUI such as Arena or Cutechess.
