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
		MoveGen.setBoard(chessboard);
		moves = MoveGen.getMoves(activeColor);
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
									moves = MoveGen.getMoves(activeColor);
									// check if game is over
									if(moves.size() == 0) {
										if(MoveGen.isKingInCheck(activeColor)) {
											if(activeColor) {
												System.out.println("Checkmate. Black has won.");
											} else {
												System.out.println("Checkmate. White has won.");
											}
										} else {
											System.out.println("Game is a statemate");
										}
										return;
									}
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
						MoveGen.setBoard(chessboard);
						moves = MoveGen.getMoves(activeColor);
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

		ArrayList<Move> moves = MoveGen.getMoves(side);
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

		ArrayList<Move> moves = MoveGen.getMoves(side);
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
