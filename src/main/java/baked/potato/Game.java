package baked.potato;

import java.util.*;

public class Game {
	private Board chessboard;
	private static final String initialPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private Search search;
	private Thread searchThread;

	private static Map<Character, Integer> pieces;

	static {
		pieces = new HashMap<>();
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
	}

	public Game() {
		chessboard = parseFen(initialPosition);
		chessboard.print();
		start();
	}

	public void start() {
		Scanner input = new Scanner(System.in);
		String[] command;
		String preSplitCommand;

		while(input.hasNextLine()) {
			preSplitCommand = input.nextLine().trim();
			command = preSplitCommand.split("\\s+");

			switch(command[0]) {
				case "uci":
					System.out.println("id name Potatochess");
					System.out.println("id author Sean D");
					System.out.println("uciok");
					break;
				case "isready":
					System.out.println("readyok");
					break;
				case "ucinewgame":
					if(safeToRun()) chessboard.tt.clear();
					break;
				case "position":
					position(command);
					break;
				case "go":
					go(command);
					break;
				case "stop":
					if(!stop()) System.out.println("No search is running");
					break;
				case "print":
					if(safeToRun()) chessboard.print();
					break;
				case "perft":
				case "divide":
					if(!safeToRun()) break;
					try {
						long elapsed = System.nanoTime();
						divide(chessboard, Integer.parseInt(command[1]));
						elapsed = System.nanoTime() - elapsed;
						System.out.printf("%.1f ms\n", elapsed * 1e-6);
					} catch(Exception e) {
						System.out.println("missing or invalid depth");
					}
					break;
				case "quit":
				case "exit":
					stop();
					return;
				default:
					if(command[0].length() != 0) {
						System.out.println("'" + command[0] + "' is not a valid command.");
					}
					break;
			}
		}
	}

	private boolean safeToRun() {
		if(searchThread != null && searchThread.isAlive()) {
			System.out.println("Cannot run command while a search is running");
			return false;
		}

		return true;
	}

	private void position(String[] command) {
		if(!safeToRun()) return;

		if(command.length < 2) {
			System.out.println("Position invalid. Missing startpos or fen");
			return;
		}

		Board b = null;
		int moveskip = 2;
		if(command[1].equals("startpos")) {
			chessboard = parseFen(initialPosition);
		} else if(command[1].equals("fen")) {
			if(command.length < 8) {
				System.out.println("Position invalid. Invalid fen: fields not equal to 6");
				return;
			}
			String fen = "";
			for(int i = 2; i < 8; i++) {
				fen += command[i];
				if(i < 7) fen += " ";
			}

			try {
				b = parseFen(fen);
			} catch(IllegalArgumentException e) {
				System.out.println("Position invalid. parseFen failed: " + e.getMessage());
				return;
			}

			moveskip = 8;
		} else {
			System.out.println("Position invalid. Expected startpos or fen. '" + command[1] + "' not recognized.");
			return;
		}

		if(command.length > moveskip) {
			if(!command[moveskip].equals("moves")) {
				System.out.println("Position invalid. Expected moves. '" + command[1] + "' not recognized.");
			}

			for (int i = moveskip + 1; i < command.length; i++) {
				Movelist ml = MoveGen.getMoves(chessboard, false);
				boolean foundMove = false;

				for (int mIdx = 0; mIdx < ml.size(); mIdx++) {
					Move m = ml.moves[mIdx];

					if (m.toString().equals(command[i])) {
						chessboard.move(m);
						foundMove = true;
						break;
					}
				}

				if (!foundMove) {
					System.out.println("Position invalid. Move '" + command[i] + "' not valid for position");
					return;
				}
			}
		}

		if(b != null) {
			chessboard = b;
		}
	}

	private void go(String[] command) {
		if(!safeToRun()) return;

		int time = 0;
		int increment = 0;
		int movesToGo = 40;
		int depth = Search.MAX_PLY;

		loop:
		for(int i = 1; i < command.length;) {
			try {
				switch (command[i]) {
					case "wtime":
						if (chessboard.getActiveColor() == Board.WHITE) {
							time = Integer.parseInt(command[i + 1]);
						}
						i += 2;
						break;
					case "btime":
						if (chessboard.getActiveColor() == Board.BLACK) {
							time = Integer.parseInt(command[i + 1]);
						}
						i += 2;
						break;
					case "winc":
						if (chessboard.getActiveColor() == Board.WHITE) {
							increment = Integer.parseInt(command[i + 1]);
						}
						i += 2;
						break;
					case "binc":
						if (chessboard.getActiveColor() == Board.BLACK) {
							increment = Integer.parseInt(command[i + 1]);
						}
						i += 2;
						break;
					case "movestogo":
						movesToGo = Integer.parseInt(command[i + 1]);
						i += 2;
						break;
					case "depth":
						depth = Integer.parseInt(command[i + 1]);
						i += 2;
						break;
					case "movetime":
						time = Integer.parseInt(command[i + 1]);
						movesToGo = 1;
						increment = 0;
						depth = Search.MAX_PLY;
						break loop;
					case "infinite":
						time = 0;
						increment = 0;
						depth = Search.MAX_PLY;
						break loop;
					default:
						System.out.println("Go invalid. '" + command[i] + "' not recognized");
						return;
				}

				if(time < 0 || increment < 0 || movesToGo < 0) {
					System.out.println("Go invalid. " + command[i - 1] + " < 0");
					return;
				} else if(depth <= 0) {
					System.out.println("invalid depth: " + depth);
					return;
				}
			} catch(Exception e) {
				System.out.println("Go invalid. Missing or invalid integer for '" + command[i] + "'");
				return;
			}
		}

		time = (time / movesToGo) + increment;

		search = new Search(chessboard, depth, time);
		searchThread = new Thread(search);
		searchThread.start();
	}

	private boolean stop() {
		if(searchThread != null && searchThread.isAlive()) {
			search.stop();
			try {
				searchThread.join();
			} catch(InterruptedException ie) {

			}

			return true;
		}

		return false;
	}

	public static int perft(Board chessboard, int depth) {
		if(depth <= 0) {
			return 1;
		}

		int nodes = 0;

		Movelist ml = MoveGen.getMoves(chessboard, false);
		for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
			Move m = ml.moves[mIdx];
			if(chessboard.move(m)) {
				nodes += perft(chessboard, depth-1);
			}
			chessboard.undoMove();
		}
		return nodes;
	}
	
	public static void divide(Board chessboard, int depth) {
		if(depth <= 0) {
			return;
		}	

		int nodes = 0;
		List<String> results = new ArrayList<>();

		Movelist ml = MoveGen.getMoves(chessboard, false);
		for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
			Move m = ml.moves[mIdx];
			if(chessboard.move(m)) {
				int moveNodes = perft(chessboard, depth - 1);

				results.add(m.toString() + " " + moveNodes);
				nodes += moveNodes;
			}

			chessboard.undoMove();
		}

		Collections.sort(results);
		for(String r : results) {
			System.out.println(r);
		}
		System.out.println(results.size() + " moves " + nodes + " nodes");
	}

	public static Board parseFen(String position) {
		String[] fields = position.trim().split("\\s+");

		if(fields.length != 6) {
			throw new IllegalArgumentException("fields not equal to 6");
		}

		boolean activeColor;
		if(fields[1].equals("w")) {
			activeColor = Board.WHITE;
		} else if(fields[1].equals("b")) {
			activeColor = Board.BLACK;
		} else {
			throw new IllegalArgumentException("'" + fields[1] + "' is not a valid active color");
		}

		int epSquare;
		if(fields[3].matches("^[a-h][3|6]$|^-$")) {
			epSquare = Square.stringToEnum(fields[3]).intValue;
		} else {
			throw new IllegalArgumentException("'" + fields[3] + "' is not a valid en passant square");
		}

		int fiftyMove;
		try {
			fiftyMove = Integer.parseInt(fields[4]);
			if(fiftyMove < 0) {
				throw new Exception();
			}
		} catch(Exception e) {
			throw new IllegalArgumentException("'" + fields[4] + "' is not a valid halfmove clock");
		}

		int fullMove;
		try {
			fullMove = Integer.parseInt(fields[5]);
			if (fullMove < 1) {
				throw new Exception();
			}
		} catch(Exception e) {
			throw new IllegalArgumentException("'" + fields[5] + "' is not a valid fullmove number");
		}

		// assume no castle rights
		long castleRights = 0x8100000000000081L;
		if(fields[2].matches("^K?Q?k?q?$|^-$")) {
			for(char piece : fields[2].toCharArray()) {
				switch(piece) {
					case 'K':
						castleRights &= ~0x80L;
						break;
					case 'Q':
						castleRights &= ~0x1L;
						break;
					case 'k':
						castleRights &= ~0x8000000000000000L;
						break;
					case 'q':
						castleRights &= ~0x100000000000000L;
						break;
					default:
						break;
				}
			}
		} else {
			throw new IllegalArgumentException("'" + fields[2] + "' is not a valid castling configuration");
		}

		// split first field into ranks
		String[] ranks = fields[0].split("/");
		if(ranks.length != 8) {
			throw new IllegalArgumentException("ranks not equal to 8");
		}

		long[] bitboards = new long[12];
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
						bitboards[idx] |= bitMask;
						bitMask = bitMask << 1;
						rankCount++;
					}
				}
			}

			if(rankCount != 8) {
				throw new IllegalArgumentException("rank " + (8 - i) + " does not have correct number of pieces");
			}
		}

		// check if there are kings
		if(Long.bitCount(bitboards[Piece.WHITE_KING.intValue]) != 1 || Long.bitCount(bitboards[Piece.BLACK_KING.intValue]) != 1) {
			throw new IllegalArgumentException("invalid amount of kings");
		}

		// check if kings are next to each other
		if((KingMoveGen.kingMoves(bitboards[Piece.WHITE_KING.intValue]) & bitboards[Piece.BLACK_KING.intValue]) != 0) {
			throw new IllegalArgumentException("kings next to each other");
		}

		// check ep passant
		if(epSquare != Square.NO_SQ.intValue) {
			long epSquareMask = 1L << epSquare + (activeColor ? -8 : 8);

			if(activeColor && (epSquareMask & bitboards[Piece.BLACK_PAWN.intValue]) == 0 ||
			!activeColor && (epSquareMask & bitboards[Piece.WHITE_PAWN.intValue]) == 0) {
				throw new IllegalArgumentException("ep square invalid: no pawn to capture en passant");
			}

			long sidePawns = ((epSquareMask & Mask.clearFileH) << 1) | ((epSquareMask & Mask.clearFileA) >>> 1);

			if (activeColor && (sidePawns & bitboards[Piece.WHITE_PAWN.intValue]) == 0 ||
					!activeColor && (sidePawns & bitboards[Piece.BLACK_PAWN.intValue]) == 0) {
				epSquare = Square.NO_SQ.intValue;
			}
		}

		// check castle rights
		if((castleRights & Board.WKS_CASTLE_MASK) == 0 &&
				Long.bitCount((bitboards[Piece.WHITE_KING.intValue] | bitboards[Piece.WHITE_ROOK.intValue]) & Board.WKS_CASTLE_MASK) != 2) {
			throw new IllegalArgumentException("white kingside castle not valid: king or rook not on initial squares");
		}

		if((castleRights & Board.WQS_CASTLE_MASK) == 0 &&
				Long.bitCount((bitboards[Piece.WHITE_KING.intValue] | bitboards[Piece.WHITE_ROOK.intValue]) & Board.WQS_CASTLE_MASK) != 2) {
			throw new IllegalArgumentException("white queenside castle not valid: king or rook not on initial squares");
		}

		if((castleRights & Board.BKS_CASTLE_MASK) == 0 &&
				Long.bitCount((bitboards[Piece.BLACK_KING.intValue] | bitboards[Piece.BLACK_ROOK.intValue]) & Board.BKS_CASTLE_MASK) != 2) {
			throw new IllegalArgumentException("black kingside castle not valid: king or rook not on initial squares");
		}

		if((castleRights & Board.BQS_CASTLE_MASK) == 0 &&
				Long.bitCount((bitboards[Piece.BLACK_KING.intValue] | bitboards[Piece.BLACK_ROOK.intValue]) & Board.BQS_CASTLE_MASK) != 2) {
			throw new IllegalArgumentException("black queenside castle not valid: king or rook not on initial squares");
		}

		Board b = new Board(bitboards, castleRights, epSquare, activeColor, fiftyMove, fullMove);
		// check if previous activeColor was not in check
		if(MoveGen.isKingInCheck(b, !activeColor)) {
			throw new IllegalArgumentException("side not to move is in check");
		}

		return b;
	}
}
