public class Board {
	Square[][] grid = {
		{Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD},
		{Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD},

		{Square.OFFBOARD, Square.B_ROOK, Square.B_KNIGHT, Square.B_BISHOP, Square.B_QUEEN, Square.B_KING, Square.B_BISHOP, Square.B_KNIGHT, Square.B_ROOK, Square.OFFBOARD},
		{Square.OFFBOARD, Square.B_PAWN, Square.B_PAWN, Square.B_PAWN, Square.B_PAWN, Square.B_PAWN, Square.B_PAWN, Square.B_PAWN, Square.B_PAWN, Square.OFFBOARD},

		{Square.OFFBOARD, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.OFFBOARD},
		{Square.OFFBOARD, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.OFFBOARD},
		{Square.OFFBOARD, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.OFFBOARD},
		{Square.OFFBOARD, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.EMPTY, Square.OFFBOARD},

		{Square.OFFBOARD, Square.W_PAWN, Square.W_PAWN, Square.W_PAWN, Square.W_PAWN, Square.W_PAWN, Square.W_PAWN, Square.W_PAWN, Square.W_PAWN, Square.OFFBOARD},
		{Square.OFFBOARD, Square.W_ROOK, Square.W_KNIGHT, Square.W_BISHOP, Square.W_QUEEN, Square.W_KING, Square.W_BISHOP, Square.W_KNIGHT, Square.W_ROOK, Square.OFFBOARD},

		{Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD},
		{Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD, Square.OFFBOARD}
	};

	void print() {
		for(int i = 0; i < grid.length; i++) {
			for(int j = 0; j < grid[i].length; j++) {
				switch(grid[i][j]) {
					case W_PAWN: System.out.print("p, ");
						     break;
					case W_ROOK: System.out.print("r, ");
						     break;
					case W_KNIGHT: System.out.print("n, ");
						       break;
					case W_BISHOP: System.out.print("b, ");
						       break;
					case W_QUEEN: System.out.print("q, ");
						      break;
					case W_KING: System.out.print("k, ");
						     break;
					case B_PAWN: System.out.print("P, ");
						     break;
					case B_ROOK: System.out.print("R, ");
						     break;
					case B_KNIGHT: System.out.print("N, ");
						       break;
					case B_BISHOP: System.out.print("B, ");
						       break;
					case B_QUEEN: System.out.print("Q, ");
						      break;
					case B_KING: System.out.print("K, ");
						     break;
					default: System.out.print("_, ");
						 break;
				}
			}
			System.out.print("\n");
		}
	}
}
