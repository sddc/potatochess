package baked.potato;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class Game {
	private Board chessboard;
	private static final String initialPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private Search search;
	private Thread searchThread;

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
			command = preSplitCommand.split(" ");

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
					chessboard.tt.clear();
					break;
				case "position":
					int moveskip = 2;
					if(command[1].equals("startpos")) {
						chessboard = parseFen(initialPosition);
					} else if(command[1].equals("fen")) {
						String fen = "";
						for(int i = 2; i < 8; i++) {
							fen += command[i];
							if(i < 7) fen += " ";
						}
						chessboard = parseFen(fen);
						moveskip = 8;
					}

					for(int i = moveskip; i < command.length; i++) {
						Movelist ml = MoveGen.getMoves(chessboard, false);
						for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
							Move m = ml.moves[mIdx];

							if(m.toString().equals(command[i])) {
								chessboard.move(m);
								break;
							}
						}
					}
					break;
				case "go":
					int time = 0;
					int increment = 0;
					int movesToGo = 30;
					int depth = 0;

					for(int i = 1; i < command.length;) {
						switch(command[i]) {
							case "wtime":
								if(chessboard.getActiveColor() == Board.WHITE) {
									time = Integer.parseInt(command[i + 1]);
								}
								i += 2;
								break;
							case "btime":
								if(chessboard.getActiveColor() == Board.BLACK) {
									time = Integer.parseInt(command[i + 1]);
								}
								i += 2;
								break;
							case "winc":
								if(chessboard.getActiveColor() == Board.WHITE) {
									increment = Integer.parseInt(command[i + 1]);
								}
								i += 2;
								break;
							case "binc":
								if(chessboard.getActiveColor() == Board.BLACK) {
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
								i += 2;
								break;
							case "infinite":
								i++;
								break;
							default:
								break;
						}
					}

					time = (time / movesToGo) + increment;

					search = new Search(chessboard, depth, time);
					searchThread = new Thread(search);
					searchThread.start();

					break;
				case "stop":
					if(searchThread != null && searchThread.isAlive()) {
						search.stop();
						try {
							searchThread.join();
						} catch(InterruptedException ie) {

						}
					}

					break;
				case "moves":
					Movelist ml = MoveGen.getMoves(chessboard, false);
					for(int i = 0; i < ml.size(); i++) {
						Move m = ml.moves[i];

						if(chessboard.move(m)) {
							System.out.println(m);
						}

						chessboard.undoMove();
					}

					break;
				case "print":
					chessboard.print();
					break;
				case "move":
					if(command.length == 2) {
						if(command[1].length() == 4 || command[1].length() == 5) {
							boolean foundMove = false;

							ml = MoveGen.getMoves(chessboard, false);
							for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
								Move m = ml.moves[mIdx];

								if(m.toString().equals(command[1])) {
									chessboard.move(m);
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
					} catch(IllegalArgumentException e) {
						System.out.println("setboard failed: " + e.getMessage());
					}
					break;
				case "perft":
					depth = Integer.parseInt(command[1]);
					long elapsed = System.nanoTime();
					int nodes = perft(chessboard, depth);
					elapsed = System.nanoTime() - elapsed;
					System.out.printf("nodes: %d\n%.1f ms\n", nodes, elapsed * 1e-6);
					break;
				case "divide":
					divide(chessboard, Integer.parseInt(command[1]), true);
					break;
				case "quit":
				case "exit":
					if(searchThread != null && searchThread.isAlive()) {
						search.stop();
						try {
							searchThread.join();
						} catch(InterruptedException ie) {

						}
					}
					return;
				default:
					if(command[0].length() == 0) {
					} else {
						System.out.println("'" + command[0] + "' is not a valid command.");
					}
					break;
			}
		}
	}

	public static int perft(Board chessboard, int depth) {
		if(depth == 0) {
			return 1;
		}

		int nodes = 0;

		Movelist ml = MoveGen.getMoves(chessboard, true);
		for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
			Move m = ml.moves[mIdx];
			if(chessboard.move(m)) {
				nodes += perft(chessboard, depth-1);
			}
			chessboard.undoMove();
		}
		return nodes;
	}
	
	public static int divide(Board chessboard, int depth, boolean initial) {
		if(depth == 0) {
			return 1;
		}	

		int nodes = 0;
		ArrayList<String> results = null;
		if(initial) {
			results = new ArrayList<String>();
		}

		Movelist ml = MoveGen.getMoves(chessboard, false);
		for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
			Move m = ml.moves[mIdx];
			if(chessboard.move(m)) {
				nodes += divide(chessboard, depth - 1, false);

				if(initial) {
					results.add(new String(m.toString() + " " + nodes));
					nodes = 0;
				}
			}

			chessboard.undoMove();
		}

		if(initial) {
			Collections.sort(results);
			for(String r : results) {
				System.out.println(r);
			}
		}
		return nodes;
	}

	public static Board parseFen(String position) {
		long[] bitboards = new long[12];
		Arrays.fill(bitboards, 0L);

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

		int epSquare;
		if(fields[3].matches("^[a-h][1-8]$|^-$")) {
			epSquare = Square.stringToEnum(fields[3]).intValue;
		} else {
			throw new IllegalArgumentException("'" + fields[3] + "' is not a valid en passant target square");
		}

		int fiftyMove = Integer.parseInt(fields[4]);
		if(fiftyMove < 0) {
			throw new IllegalArgumentException("'" + fields[4] + "' is not a valid halfmove clock");
		}

		int fullMove = Integer.parseInt(fields[5]);
		if(fullMove < 1) {
			throw new IllegalArgumentException("'" + fields[5] + "' is not a valid fullmove number");
		}

		return new Board(bitboards, castleRights, epSquare, activeColor, fiftyMove, fullMove);
	}
}
