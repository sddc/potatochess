import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class Game {
	private Board chessboard;
	private ArrayList<Move> moves;
	private static final String initialPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private boolean activeColor;

	public Game() {
		chessboard = parseFen(initialPosition);
		activeColor = chessboard.getActiveColor();
		moves = genValidMoves(activeColor);
		start();
	}

	public void start() {
		Scanner input = new Scanner(System.in);
		String[] command;
		String preSplitCommand;
		
		System.out.print("> ");
		while(input.hasNextLine()) {
			preSplitCommand = input.nextLine();
			command = preSplitCommand.split(" ");

			switch(command[0]) {
				case "moves":
					Collections.sort(moves);
					for(Move m : moves) {
						System.out.println(m.toString());
					}
					System.out.println(moves.size() + " moves");
					break;
				case "print":
					chessboard.print();
					if(activeColor == Board.WHITE) {
						System.out.println("Active color: White (uppercase)");
					} else {
						System.out.println("Active color: Black (lowercase)");
					}
					break;
				case "move":
					if(command.length == 2) {
						if(command[1].length() == 4) {
							boolean foundMove = false;

							for(Move m : moves) {
								if(m.toString().equals(command[1])) {
									chessboard.move(activeColor, m.fromSquare, m.toSquare);
									activeColor = chessboard.toggleActiveColor();
									moves = genValidMoves(activeColor);
									foundMove = true;
									break;
								}
							}

							if(!foundMove) {
								System.out.println("'" + command[1] + "' is not a valid move.");
							}
							break;
						} else {
							System.out.println("'" + command[1] + "' is not a valid move.");
							break;
						}
					} else {
						System.out.println("move command missing square coordinates.");
						break;
					}
				case "setboard":
					try {
						String position = preSplitCommand.substring(preSplitCommand.indexOf(' ') + 1,
								preSplitCommand.length());
						chessboard = parseFen(position);
						activeColor = chessboard.getActiveColor();
						moves = genValidMoves(activeColor);
					} catch(IllegalArgumentException e) {
						System.out.println("setboard failed: " + e.getMessage());
					}
					break;
				case "perft":
					System.out.println(perft(activeColor, Integer.parseInt(command[1])));
					break;
				case "divide":
					divide(activeColor, Integer.parseInt(command[1]), true);
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

	public int perft(boolean side, int depth) {
		if(depth == 0) {
			return 1;
		}	

		int Nodes = 0;

		ArrayList<Move> moves = genValidMoves(side);
		for(Move m : moves) {
			chessboard.move(side, m.fromSquare, m.toSquare);
			Nodes += perft(!side, depth-1);
			chessboard.undoMove(side);
		}
		return Nodes;
	}
	
	public int divide(boolean side, int depth, boolean initial) {
		if(depth == 0) {
			return 1;
		}	

		int Nodes = 0;
		ArrayList<String> results = null;
		if(initial) {
			results = new ArrayList<String>();
		}

		ArrayList<Move> moves = genValidMoves(side);
		for(Move m : moves) {
			chessboard.move(side, m.fromSquare, m.toSquare);
			Nodes += perft(!side, depth-1);
			chessboard.undoMove(side);

			if(initial) {
				results.add(new String(m.toString() + " " + Nodes));
				Nodes = 0;
			}
		}

		if(initial) {
			Collections.sort(results);
			for(String r : results) {
				System.out.println(r);
			}
		}
		return Nodes;
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
			j = pawnAttack & Move.genPawnAttack(side, Board.get1BitMask(i), chessboard.getSidePieces(side));
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = Move.genPawnPush(side, Board.get1BitMask(i), chessboard.getAllPieces());
			for(int k : Board.get1BitIndexes(j)) {
				validateAndAdd(side, i, k, moves);
			}
		}

		for(int i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = Move.genDoublePawnPush(side, Board.get1BitMask(i), chessboard.getAllPieces());
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
		genCastlingMoves(side, moves);

		return moves;
	}

	private void genCastlingMoves(boolean side, ArrayList<Move> moves) {
		long opponentAttacks = genAttackSquares(!side);
		// check if king in check
		if(kingInCheck(side, opponentAttacks)) {
			return;
		}

		// check if kingside castling is available
		if(chessboard.castlingAvailable(side, Board.KINGSIDE, opponentAttacks)) {
			if(side == Board.WHITE) {
				moves.add(new Move(Board.E1, Board.G1));
			} else {
				moves.add(new Move(Board.E8, Board.G8));
			}
		}

		// check if queenside castling is available
		if(chessboard.castlingAvailable(side, Board.QUEENSIDE, opponentAttacks)) {
			if(side == Board.WHITE) {
				moves.add(new Move(Board.E1, Board.C1));
			} else {
				moves.add(new Move(Board.E8, Board.C8));
			}
		}
	}


	private void validateAndAdd(boolean side, int fromSquare, int toSquare, ArrayList<Move> moves) {
		// make move then check if king in check
		// if not, add to moves then undo
		chessboard.move(side, fromSquare, toSquare);
		if(!kingInCheck(side, genAttackSquares(!side))) {
			moves.add(new Move(fromSquare, toSquare));
		}
		chessboard.undoMove(side);
	}

	private boolean kingInCheck(boolean side, long opponentAttacks) {
		if((opponentAttacks & chessboard.getKing(side)) != 0L) {
			return true;
		} else {
			return false;
		}
	}

	private Board parseFen(String position) {
		long[] bitboards = new long[12];
		Arrays.fill(bitboards, 0L);

		boolean[] moved = new boolean[6];
		Arrays.fill(moved, true);

		boolean lastMoveDoublePawnPush = false;
		int behindSquare = Board.EMPTY;
		boolean activeColor = Board.WHITE;

		String[] fields = position.split(" ");

		if(fields.length != 6) {
			throw new IllegalArgumentException("fields not equal to 6");
		}

		// split first field into ranks
		String[] ranks = fields[0].split("/"); 
		if(ranks.length != 8) {
			throw new IllegalArgumentException("ranks not equal to 8");
		}

		Map<Character, Integer> pieces = new HashMap<Character, Integer>();
		pieces.put('P', 0);
		pieces.put('R', 1);
		pieces.put('N', 2);
		pieces.put('B', 3);
		pieces.put('Q', 4);
		pieces.put('K', 5);
		pieces.put('p', 6);
		pieces.put('r', 7);
		pieces.put('n', 8);
		pieces.put('b', 9);
		pieces.put('q', 10);
		pieces.put('k', 11);
		long bitMask = 1L;

		// loop over ranks starting at rank 1
		for(int i = 7; i >= 0; i--) {
			int rankCount = 0;

			// loop over files starting at file A
			for(char piece : ranks[i].toCharArray()) {
				// if piece is a number(empty), shift by that amount
				if(piece >= '1' && piece <= '8') {
					int empty = Character.getNumericValue(piece);
					bitMask = bitMask << empty;
					rankCount += empty;
				} else {
					Integer idx = pieces.get(piece);
					if(idx == null) {
						throw new IllegalArgumentException("invalid piece '" + piece + "'");
					} else {
						bitboards[idx.intValue()] |= bitMask;
						bitMask = bitMask << 1;
						rankCount++;
					}
				}
			}

			if(rankCount != 8) {
				throw new IllegalArgumentException("rank " + i + " does not have correct number of pieces");
			}
		}

		if(fields[1].equals("w")) {
			// do nothing, activeColor initialized to correct value
		} else if(fields[1].equals("b")) {
			activeColor = Board.BLACK;
		} else {
			throw new IllegalArgumentException("'" + fields[1] + "' is not a valid active color");
		}

		if(fields[2].matches("^K?Q?k?q?$|^-$")) {
			for(char piece : fields[2].toCharArray()) {
				switch(piece) {
					case 'K':
						moved[0] = false;
						moved[2] = false;
						break;
					case 'Q':
						moved[3] = false;
						moved[0] = false;
						break;
					case 'k':
						moved[4] = false;
						moved[1] = false;
						break;
					case 'q':
						moved[5] = false;
						moved[1] = false;
						break;
					default:
						break;
				}
			}
		} else {
			throw new IllegalArgumentException("'" + fields[2] + "' is not a valid castling configuration");
		}

		if(fields[3].matches("^[a-h][1-8]$|^-$")) {
			if(fields[3].equals("-")) {
				// do nothing, behindSquare and lastMoveDoublePawnPush already initialized to correct values
			} else {
				for(int i = 0; i < Move.squareNames.length; i++) {
					if(Move.squareNames[i].equals(fields[3])) {
						behindSquare = i;
						lastMoveDoublePawnPush = true;
					}
				}
			}
		} else {
			throw new IllegalArgumentException("'" + fields[3] + "' is not a valid en passant target square");
		}

		// todo: halfmove clock, fullmove number

		return new Board(bitboards, moved, lastMoveDoublePawnPush, behindSquare, activeColor);
	}
		
}
