import java.util.ArrayList;
import java.util.Scanner;

public class Game {
	private Board chessboard;
	private boolean activeColor;
	private ArrayList<Move> moves;
	private boolean whiteKingInCheck = false;
	private boolean blackKingInCheck = false;

	public Game() {
		chessboard = new Board();
		activeColor = Board.WHITE;
		moves = genValidMoves(activeColor);
		start();
	}

	public void start() {
		Scanner input = new Scanner(System.in);
		String[] command;
		
		System.out.print("> ");
		while(input.hasNextLine()) {
			command = input.nextLine().split(" ");

			switch(command[0]) {
				case "moves":
					break;
				case "print":
					chessboard.print();
					if(activeColor == Board.WHITE) {
						System.out.println("Active color: White (uppercase)");
					} else {
						System.out.println("Active color: Black (lowercase)");
					}
					break;
				case "quit":
				case "exit":
					return;
				default:
					if(command[0].length() == 0) {
					} else {
						System.out.println("'" + command[0] + "' is not a valid command.");
					}
					break;
			}
			System.out.print("> ");
		}
	}

	private long genAttackSquares(boolean side) {
		long attacks = 0L;
		int rook;
		int bishop;
		int queen;

		if(side == Board.WHITE) {
			rook = Board.WHITE_ROOK;
			bishop = Board.WHITE_BISHOP;
			queen = Board.WHITE_QUEEN;
		} else {
			rook = Board.BLACK_ROOK;
			bishop = Board.BLACK_BISHOP;
			queen = Board.BLACK_QUEEN;
		}

		attacks |= Move.genPawnAttack(side, chessboard.getPawns(side), chessboard.getSidePieces(side));

		for(int i : Board.get1BitIndexes(chessboard.getRooks(side))) {
			attacks |= Move.genSlidingPieceMoves(rook, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
		}

		for(int i : Board.get1BitIndexes(chessboard.getBishops(side))) {
			attacks |= Move.genSlidingPieceMoves(bishop, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
		}

		for(int i : Board.get1BitIndexes(chessboard.getQueen(side))) {
			attacks |= Move.genSlidingPieceMoves(queen, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
		}

		for(int i : Board.get1BitIndexes(chessboard.getKnights(side))) {
			attacks |= Move.knightMoves[i] & ~chessboard.getSidePieces(side);
		}

		for(int i : Board.get1BitIndexes(chessboard.getKing(side))) {
			attacks |= Move.kingMoves[i] & ~chessboard.getSidePieces(side);
		}
		return attacks;
	}

	private ArrayList<Move> genValidMoves(boolean side) {
		long j;
		long pawnAttack;
		int rook;
		int bishop;
		int queen;
		ArrayList<Move> moves = new ArrayList<Move>();
		Move m;

		if(side == Board.WHITE) {
			rook = Board.WHITE_ROOK;
			bishop = Board.WHITE_BISHOP;
			queen = Board.WHITE_QUEEN;
		} else {
			rook = Board.BLACK_ROOK;
			bishop = Board.BLACK_BISHOP;
			queen = Board.BLACK_QUEEN;
		}

		if(chessboard.lastMoveDPP()) {
			pawnAttack = Board.get1BitMask(chessboard.getBehindSquare());
			pawnAttack |= chessboard.getSidePieces(!side);
		} else {
			pawnAttack = chessboard.getSidePieces(!side);
		}

		for(int i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = pawnAttack & Move.genPawnAttack(side, i, chessboard.getSidePieces(side));
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = Move.genPawnPush(side, i, chessboard.getAllPieces());
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = Move.genDoublePawnPush(side, i, chessboard.getAllPieces());
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getRooks(side))) {
			j = Move.genSlidingPieceMoves(rook, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getBishops(side))) {
			j = Move.genSlidingPieceMoves(bishop, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getQueen(side))) {
			j = Move.genSlidingPieceMoves(queen, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getKnights(side))) {
			j = Move.knightMoves[i] & ~chessboard.getSidePieces(side);
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getKing(side))) {
			j = Move.kingMoves[i] & ~chessboard.getSidePieces(side);
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}
		castling(side);

		return moves;
	}

	private void castling(boolean side) {
		long opponentAttacks = genAttackSquares(!side);
		// check if king in check
		if( ((side == Board.WHITE) && whiteKingInCheck) ||
		    ((side == Board.BLACK) && blackKingInCheck)
		) {
			return;
		}

		// check if kingside castling is available
		if(chessboard.castlingAvailable(side, Board.KINGSIDE, opponentAttacks)) {
			// make the move. rook moves implicitly. validateAndAdd
			// will check if king is attacked after move
			if(side == Board.WHITE) {
				validateAndAdd(side, Board.E1, Board.G1, moves);
			} else {
				validateAndAdd(side, Board.E8, Board.G8, moves);
			}
		}

		// check if queenside castling is available
		if(chessboard.castlingAvailable(side, Board.QUEENSIDE, opponentAttacks)) {
			// make the move. rook moves implicitly. validateAndAdd
			// will check if king is attacked after move
			if(side == Board.WHITE) {
				validateAndAdd(side, Board.E1, Board.C1, moves);
			} else {
				validateAndAdd(side, Board.E8, Board.C8, moves);
			}
		}
	}


	private void validateAndAdd(boolean side, int fromSquare, int toSquare, ArrayList<Move> moves) {
		chessboard.move(side, fromSquare, toSquare);
		if((genAttackSquares(!side) & chessboard.getKing(side)) == 0L) {
			moves.add(new Move(fromSquare, toSquare));
		}
		chessboard.undoMove(side);
	}

		
}
