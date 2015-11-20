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
					if(chessboard.lastMoveDPP()) {
						System.out.println("Last move double pawn push.");
						System.out.println("en passant target square: " + chessboard.getEpTargetSquare().toString());
					}
					break;
				case "move":
					if(command.length == 2) {
						if(command[1].length() == 4) {
							boolean foundMove = false;

							for(Move m : moves) {
								if(m.toString().equals(command[1])) {
									chessboard.move(activeColor, m);
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
					System.out.println("nodes: " + perft(activeColor, Integer.parseInt(command[1])));
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
			chessboard.move(side, m);
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
			chessboard.move(side, m);
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
		Piece rook;
		Piece bishop;
		Piece queen;

		if(side == Board.WHITE) {
			rook = Piece.WHITE_ROOK;
			bishop = Piece.WHITE_BISHOP;
			queen = Piece.WHITE_QUEEN;
		} else {
			rook = Piece.BLACK_ROOK;
			bishop = Piece.BLACK_BISHOP;
			queen = Piece.BLACK_QUEEN;
		}

		attacks |= MoveGen.genPawnAttack(side, chessboard.getPawns(side), chessboard.getSidePieces(side));

		for(Square i : Board.get1BitIndexes(chessboard.getRooks(side))) {
			attacks |= MoveGen.genSlidingPieceMoves(rook, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
		}

		for(Square i : Board.get1BitIndexes(chessboard.getBishops(side))) {
			attacks |= MoveGen.genSlidingPieceMoves(bishop, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
		}

		for(Square i : Board.get1BitIndexes(chessboard.getQueen(side))) {
			attacks |= MoveGen.genSlidingPieceMoves(queen, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
		}

		for(Square i : Board.get1BitIndexes(chessboard.getKnights(side))) {
			attacks |= MoveGen.knightMoves[i.intValue] & ~chessboard.getSidePieces(side);
		}

		for(Square i : Board.get1BitIndexes(chessboard.getKing(side))) {
			attacks |= MoveGen.kingMoves[i.intValue] & ~chessboard.getSidePieces(side);
		}
		return attacks;
	}

	private ArrayList<Move> genValidMoves(boolean side) {
		long j;
		long opponentPieces;
		Piece pawn, rook, knight, bishop, queen, king;
		ArrayList<Move> moves = new ArrayList<Move>();
		Move m;
		Piece type;
		boolean epCapture = false;
		Square epCaptureSquare = null;

		if(side == Board.WHITE) {
			pawn = Piece.WHITE_PAWN;
			rook = Piece.WHITE_ROOK;
			knight = Piece.WHITE_KNIGHT;
			bishop = Piece.WHITE_BISHOP;
			queen = Piece.WHITE_QUEEN;
			king = Piece.WHITE_KING;
		} else {
			pawn = Piece.BLACK_PAWN;
			rook = Piece.BLACK_ROOK;
			knight = Piece.BLACK_KNIGHT;
			bishop = Piece.BLACK_BISHOP;
			queen = Piece.BLACK_QUEEN;
			king = Piece.BLACK_KING;
		}

		if(chessboard.lastMoveDPP()) {
			epCapture = true;
			epCaptureSquare = chessboard.getEpTargetSquare();
			// add en passant square
			opponentPieces = chessboard.getSidePieces(!side);
			opponentPieces |= Board.get1BitMask(epCaptureSquare);
		} else {
			opponentPieces = chessboard.getSidePieces(!side);
		}

		// pawn attack
		for(Square i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = opponentPieces & MoveGen.genPawnAttack(side, Board.get1BitMask(i), chessboard.getSidePieces(side));
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, pawn);

				if(epCapture && (k == epCaptureSquare)) {
					m.setFlag(Flag.EP_CAPTURE);
					if(side == Board.WHITE) {
						m.setCapturePieceType(Piece.BLACK_PAWN);
					} else {
						m.setCapturePieceType(Piece.WHITE_PAWN);
					}
					validateAndAdd(side, m, moves);
				} else {
					if(((Board.get1BitMask(k) & Board.maskRank1) != 0) || 
					((Board.get1BitMask(k) & Board.maskRank8) != 0)) {
						Move[] promotion ={
							new Move(i, k, pawn),
							new Move(i, k, pawn),
							new Move(i, k, pawn),
							new Move(i, k, pawn)
						};
						promotion[0].setPromotionType(queen);
						promotion[1].setPromotionType(rook);
						promotion[2].setPromotionType(knight);
						promotion[3].setPromotionType(bishop);
						for(Move p : promotion) {
							type = chessboard.getPieceType(k);
							p.setCapturePieceType(type);
							p.setFlag(Flag.CAPTURE);
							p.setFlag(Flag.PROMOTION);
							validateAndAdd(side, p, moves);
						}
					} else {
						type = chessboard.getPieceType(k);
						m.setFlag(Flag.CAPTURE);
						m.setCapturePieceType(type);
						validateAndAdd(side, m, moves);
					}
				}

			}
		}

		// pawn push
		for(Square i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = MoveGen.genPawnPush(side, Board.get1BitMask(i), chessboard.getAllPieces());
			for(Square k : Board.get1BitIndexes(j)) {
				if(((Board.get1BitMask(k) & Board.maskRank1) != 0) || 
				((Board.get1BitMask(k) & Board.maskRank8) != 0)) {
					Move[] promotion ={
						new Move(i, k, pawn),
						new Move(i, k, pawn),
						new Move(i, k, pawn),
						new Move(i, k, pawn)
					};
					promotion[0].setPromotionType(queen);
					promotion[1].setPromotionType(rook);
					promotion[2].setPromotionType(knight);
					promotion[3].setPromotionType(bishop);
					for(Move p : promotion) {
						p.setFlag(Flag.PROMOTION);
						validateAndAdd(side, p, moves);
					}
				} else {
					m = new Move(i, k, pawn);
					validateAndAdd(side, m, moves);
				}
			}
		}

		// double pawn push
		for(Square i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = MoveGen.genDoublePawnPush(side, Board.get1BitMask(i), chessboard.getAllPieces());
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, pawn);
				m.setFlag(Flag.DOUBLE_PAWN_PUSH);
				validateAndAdd(side, m, moves);
			}
		}

		// rooks
		for(Square i : Board.get1BitIndexes(chessboard.getRooks(side))) {
			j = MoveGen.genSlidingPieceMoves(rook, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, rook);
				type = chessboard.getPieceType(k);
				if(type != Piece.EMPTY) {
					m.setFlag(Flag.CAPTURE);
					m.setCapturePieceType(type);
				}
				validateAndAdd(side, m, moves);
			}
		}

		// bishops
		for(Square i : Board.get1BitIndexes(chessboard.getBishops(side))) {
			j = MoveGen.genSlidingPieceMoves(bishop, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, bishop);
				type = chessboard.getPieceType(k);
				if(type != Piece.EMPTY) {
					m.setFlag(Flag.CAPTURE);
					m.setCapturePieceType(type);
				}
				validateAndAdd(side, m, moves);
			}
		}

		// queen
		for(Square i : Board.get1BitIndexes(chessboard.getQueen(side))) {
			j = MoveGen.genSlidingPieceMoves(queen, i, chessboard.getAllPieces(), chessboard.getSidePieces(side));
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, queen);
				type = chessboard.getPieceType(k);
				if(type != Piece.EMPTY) {
					m.setFlag(Flag.CAPTURE);
					m.setCapturePieceType(type);
				}
				validateAndAdd(side, m, moves);
			}
		}

		// knight
		for(Square i : Board.get1BitIndexes(chessboard.getKnights(side))) {
			j = MoveGen.knightMoves[i.intValue] & ~chessboard.getSidePieces(side);
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, knight);
				type = chessboard.getPieceType(k);
				if(type != Piece.EMPTY) {
					m.setFlag(Flag.CAPTURE);
					m.setCapturePieceType(type);
				}
				validateAndAdd(side, m, moves);
			}
		}

		// king
		for(Square i : Board.get1BitIndexes(chessboard.getKing(side))) {
			j = MoveGen.kingMoves[i.intValue] & ~chessboard.getSidePieces(side);
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, king);
				type = chessboard.getPieceType(k);
				if(type != Piece.EMPTY) {
					m.setFlag(Flag.CAPTURE);
					m.setCapturePieceType(type);
				}
				validateAndAdd(side, m, moves);
			}
		}
		genCastlingMoves(side, moves);

		return moves;
	}

	private void genCastlingMoves(boolean side, ArrayList<Move> moves) {
		long opponentAttacks = genAttackSquares(!side);
		Move m;
		// check if king in check
		if(kingInCheck(side, opponentAttacks)) {
			return;
		}

		// check if kingside castling is available
		if(chessboard.castlingAvailable(side, Board.KINGSIDE, opponentAttacks)) {
			if(side == Board.WHITE) {
				m = new Move(Square.E1, Square.G1, Piece.WHITE_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.KINGSIDE);
				moves.add(m);
			} else {
				m = new Move(Square.E8, Square.G8, Piece.BLACK_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.KINGSIDE);
				moves.add(m);
			}
		}

		// check if queenside castling is available
		if(chessboard.castlingAvailable(side, Board.QUEENSIDE, opponentAttacks)) {
			if(side == Board.WHITE) {
				m = new Move(Square.E1, Square.C1, Piece.WHITE_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.QUEENSIDE);
				moves.add(m);
			} else {
				m = new Move(Square.E8, Square.C8, Piece.BLACK_KING);
				m.setFlag(Flag.CASTLE);
				m.setCastleType(Board.QUEENSIDE);
				moves.add(m);
			}
		}
	}


	private void validateAndAdd(boolean side, Move move, ArrayList<Move> moves) {
		// make move then check if king in check
		// if not, add to moves then undo
		chessboard.move(side, move);
		if(!kingInCheck(side, genAttackSquares(!side))) {
			moves.add(move);
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

		boolean[] castleStatus = new boolean[4];
		Arrays.fill(castleStatus, false);

		boolean lastMoveDoublePawnPush = false;
		Square epTargetSquare = null;
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
						castleStatus[0] = true;
						break;
					case 'Q':
						castleStatus[1] = true;
						break;
					case 'k':
						castleStatus[2] = true;
						break;
					case 'q':
						castleStatus[3] = true;
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
				// do nothing, epTargetSquare and lastMoveDoublePawnPush already initialized to correct values
			} else {
				epTargetSquare = Square.stringToEnum(fields[3]);
				lastMoveDoublePawnPush = true;
			}
		} else {
			throw new IllegalArgumentException("'" + fields[3] + "' is not a valid en passant target square");
		}

		// todo: halfmove clock, fullmove number

		return new Board(bitboards, castleStatus, lastMoveDoublePawnPush, epTargetSquare, activeColor);
	}
		
}
