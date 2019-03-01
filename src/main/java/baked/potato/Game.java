package baked.potato;

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
	private Search search;
	private Thread searchThread;

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
						MoveGen.setBoard(chessboard);
					} else if(command[1].equals("fen")) {
						String fen = "";
						for(int i = 2; i < 8; i++) {
							fen += command[i];
							if(i < 7) fen += " ";
						}
						chessboard = parseFen(fen);
						MoveGen.setBoard(chessboard);
						moveskip = 8;
					}

					for(int i = moveskip; i < command.length; i++) {
						for(Move m : MoveGen.getMoves(chessboard.getActiveColor())) {
							if(m.toString().equals(command[i])) {
								chessboard.move(chessboard.getActiveColor(), m);
								chessboard.toggleActiveColor();
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

//					System.out.printf("time %d increment %d movestogo %d depth %d\n", time, increment, movesToGo, depth);
					time = (time / movesToGo) + increment;
//					System.out.println("calculated time: " + time);

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
					Collections.sort(moves);
					for(Move m : moves) {
						System.out.println(m.toString());
					}
					System.out.println(moves.size() + " moves");
					break;
				case "print":
					chessboard.print();
					System.out.println("Position Key: " + String.format("%016X", chessboard.getPositionKey()));
                    System.out.print("Score: ");
                    System.out.println(Evaluation.score(chessboard) / 100.0);
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
                case "bestmove":
//                    System.out.println(Search.getBestMove(chessboard, 7, activeColor));
                    break;
                case "cmove":
//                    Move bestMove = Search.getBestMove(chessboard, 3, activeColor);
//                    if(bestMove != null) {
//                        chessboard.move(activeColor, bestMove);
//                    }
//
//                    activeColor = chessboard.toggleActiveColor();
//                    moves = MoveGen.getMoves(activeColor);
//                    // check if game is over for opponent
//                    if(gameOver()) {
//                        return;
//                    }
                    break;
				case "search":
					//Search.search(chessboard, 10, chessboard.getActiveColor());
					break;
				case "move":
					if(command.length == 2) {
						if(command[1].length() == 4 || command[1].length() == 5) {
							boolean foundMove = false;

							for(Move m : moves) {
								if(m.toString().equals(command[1])) {
									chessboard.move(activeColor, m);
									activeColor = chessboard.toggleActiveColor();
									moves = MoveGen.getMoves(activeColor);

									// check if game is over for opponent
                                    if(gameOver()) {
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
					System.out.println("nodes: " + perft(chessboard, chessboard.getActiveColor(), Integer.parseInt(command[1])));
					break;
				case "divide":
					divide(chessboard, chessboard.getActiveColor(), Integer.parseInt(command[1]), true);
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

    private boolean gameOver() {
        if(moves.size() == 0) {
            if(MoveGen.isKingInCheck(activeColor)) {
                if(activeColor) {
                    System.out.println("Checkmate. Black has won.");
                } else {
                    System.out.println("Checkmate. White has won.");
                }
            } else {
                System.out.println("baked.potato.Game is a draw.");
            }

            return true;
        }
        
        return false;
    }

	public static int perft(Board chessboard, boolean side, int depth) {
		if(depth == 0) {
			return 1;
		}	

		int nodes = 0;

		ArrayList<Move> moves = MoveGen.getMoves(side);
		for(Move m : moves) {
			chessboard.move(side, m);
			nodes += perft(chessboard, chessboard.toggleActiveColor(), depth-1);
			chessboard.undoMove(chessboard.toggleActiveColor());
		}
		return nodes;
	}
	
	public static int divide(Board chessboard, boolean side, int depth, boolean initial) {
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
			Nodes += perft(chessboard, chessboard.toggleActiveColor(), depth-1);
			chessboard.undoMove(chessboard.toggleActiveColor());

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

	public static Board parseFen(String position) {
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
