import java.util.ArrayList;
import java.util.Arrays;

public abstract class SlidingMoveGen extends MoveGen {
	private static final long[] rookOccupancyMasks = new long[64];
	private static final long[] bishopOccupancyMasks = new long[64];

	private static final long[] rookMagics = {0x8000805021c000L, 0x2d40200110004000L, 0x2200201018820040L, 0x280080044300080L, 0x30800800821c0080L, 0x480010400801200L, 0xc00019810040122L, 0x100102280460100L, 0x2800040082682L, 0x20b0400042201008L, 0x5000801000200980L, 0xa100801002080084L, 0x2800800800400L, 0x2000200111804L, 0x1311000100460004L, 0x112800100204880L, 0x9208002984000L, 0xf008808020004000L, 0x8404220016008043L, 0x210008008008095L, 0x4210808008000400L, 0x223808024000200L, 0xc000040001104826L, 0x206000108a044L, 0x4100608080004000L, 0xb440022100430080L, 0x8020011010020400L, 0x901180080100284L, 0x8001100050900L, 0x420080800400L, 0x1080420400082110L, 0x804480208000c500L, 0x200400220800080L, 0x2050042000c04000L, 0x40110149002000L, 0x240800800801000L, 0x2045101000800L, 0x40801200801400L, 0x1a0024020001a8L, 0x204b40042000081L, 0x261846040088008L, 0x84420005007c000L, 0xa40823202420021L, 0x120141220018L, 0x104400800801800cL, 0x8211000400090002L, 0x2800080110040002L, 0x884050082460004L, 0x408801940002480L, 0x4820021004200L, 0x4800881142006200L, 0x2220080080b00580L, 0x20100c408001100L, 0x41009804000300L, 0x9058020108101400L, 0x82010402408200L, 0x80802230420102L, 0x8000210040005185L, 0x210420060491082L, 0x41002008051001L, 0x806001021040802L, 0x102000410050802L, 0x10c010880e0d1044L, 0x10a040a8840902L};

	private static final long[] bishopMagics = {0x808a01882811100L, 0x103100400818005L, 0x8018080100210480L, 0x1024040088001200L, 0x12121010300068L, 0x104024241000eL, 0x881015002200040L, 0x220050088800L, 0x20400682020200L, 0x200222022020L, 0x90802088008L, 0x2040282084e00060L, 0x200011140048840L, 0x408850108421000L, 0x840088280851L, 0x40011108a2100204L, 0x6008102020018200L, 0x10000847680490L, 0x84010214040409L, 0x220480a80a004000L, 0x8002802400a00000L, 0xc004100201420800L, 0x244010043441041L, 0x20204001010390c0L, 0x502404009088810L, 0x4004200010824984L, 0x1d00680910048420L, 0x8204040000401080L, 0x8000840000806009L, 0x200043003880a005L, 0x4007100880400L, 0x400200880a240912L, 0x32021280202008L, 0x284030420200414L, 0x8000108601502400L, 0x3000020080080080L, 0x50100200100a0050L, 0xa4080200202081L, 0x1030200041201L, 0x2204a208650040L, 0x19180864444080L, 0x1044100c000900L, 0x202428021006L, 0x11402011100800L, 0x412012000301L, 0x4401000408110c0L, 0x410500080ac0108L, 0x4108120099200200L, 0xa04044504702000L, 0x2001110310220000L, 0x20142220000L, 0x1204118084044004L, 0x860242c0102L, 0x1001002100c4038L, 0x40400808090c4020L, 0x2110841b12c20010L, 0x8020101480a0840L, 0x30004200846000L, 0x4000001100431080L, 0x1600202000840402L, 0x30080000450c402L, 0x8004011220080L, 0x401104222042401L, 0x1c00a0421007100L};

	private static final int[] rookShifts = {52, 53, 53, 53, 53, 53, 53, 52, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 52, 53, 53, 53, 53, 53, 53, 52};

	private static final int[] bishopShifts = {58, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59, 59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 58};

	private static long[][] rookMoves = new long[64][];
	private static long[][] bishopMoves = new long[64][];

	static {
		// fill occupancy mask arrays	
		long square = 1L;
		for(int i = 0; i < 64; i++) {
			rookOccupancyMasks[i] = genRookOccupancyMask(square);
			bishopOccupancyMasks[i] = genBishopOccupancyMask(square);
			square = square << 1;
		}
		genMagicMoves(true, rookOccupancyMasks, rookMagics, rookShifts, rookMoves);
		genMagicMoves(false, bishopOccupancyMasks, bishopMagics, bishopShifts, bishopMoves);
	}

	public long getRookMoves(int squareIndex) {
		long occupancy = board.getAllPieces() & rookOccupancyMasks[squareIndex];
		int index = (int)((rookMagics[squareIndex] * occupancy) >>> rookShifts[squareIndex]);
		return rookMoves[squareIndex][index];
	}

	public long getBishopMoves(int squareIndex) {
		long occupancy = board.getAllPieces() & bishopOccupancyMasks[squareIndex];
		int index = (int)((bishopMagics[squareIndex] * 0L) >>> bishopShifts[squareIndex]);
		return bishopMoves[squareIndex][index];
	}

	public static void genMagicMoves(boolean type, long[] occupancyMasks, long[] magics, int[] shifts, long[][] moves) {
		long square = 1L;

		for(int i = 0; i < 64; i++) {
			long occupancyMask = occupancyMasks[i];
			long[] variations = genOccupancyVariations(occupancyMask, getOccupancyIndexes(occupancyMask));
			long[] resultMoves = genResultMoves(type, variations, square);

			moves[i] = new long[(int)(1L << Long.bitCount(occupancyMask))];
			Arrays.fill(moves[i], 0L);

			for(int j = 0; j < variations.length; j++) {
				int moveIndex = (int)((magics[i] * variations[j]) >>> shifts[i]);
				if(moves[i][moveIndex] == 0L) {
					moves[i][moveIndex] = resultMoves[j];
				}
			}

			square = square << 1;
		}
	}

	public static long[] genOccupancyVariations(long occupancy, int[] occupancyIndexes) {
		long mask = 1L;
		long[] occupancyVariations = new long[(int)(1L << Long.bitCount(occupancy))];
		int index = 0;

		for(long i = 0L; i <= (int)(1L << Long.bitCount(occupancy))-1; i++) {
			long variation = i;
			long occupancyVariation = 0L;

			for(int j = 0; j < occupancyIndexes.length; j++) {
				if((variation & mask) != 0L) {
					occupancyVariation |= mask << occupancyIndexes[j];
				}
				variation = variation >>> 1;
			}
			occupancyVariations[index++] = occupancyVariation;
		}

		return occupancyVariations;
	}

	public static long[] genResultMoves(boolean type, long[] variations, long square) {
		// type = true: rook, false: bishop
		long[] moves = new long[variations.length];

		for(int i = 0; i < variations.length; i++) {
			long move = 0L;

			if(type) {
				// +8
				move |= genRayMove(true, 8, square, ~0L, variations[i]);
				// -8
				move |= genRayMove(false, 8, square, ~0L, variations[i]);
				// +1
				move |= genRayMove(true, 1, square, clearFileH, variations[i]);
				// -1 
				move |= genRayMove(false, 1, square, clearFileA, variations[i]);
			} else {
				// +7
				move |= genRayMove(true, 7, square, clearFileA, variations[i]);
				// -9
				move |= genRayMove(false, 9, square, clearFileA, variations[i]);
				// -7 
				move |= genRayMove(false, 7, square, clearFileH, variations[i]);
				// +9 
				move |= genRayMove(true, 9, square, clearFileH, variations[i]);
			}

			moves[i] = move;
		}
		return moves;
	}

	public static int[] getOccupancyIndexes(long occupancy) {
		long mask = 1L;
		int[] occupancyIndexes = new int[Long.bitCount(occupancy)];
		int index = 0;

		for(int i = 0; i < 64; i++) {
			if((occupancy & mask) != 0L) {
				occupancyIndexes[index++] = i;
			}
			occupancy = occupancy >>> 1;
		}
		return occupancyIndexes;
	}

	public static long shift(boolean direction, int amount, long mask) {
		// true: left, false: right
		if(direction) {
			return mask << amount;
		} else {
			return mask >>> amount;
		}
	}

	public static long genRayMask(boolean direction, int amount, long square, long clear) {
		long rayMask = 0L;

		long mask = shift(direction, amount, square & clear);
		while(mask != 0L) {
			rayMask |= mask;
			mask = shift(direction, amount, mask & clear);
		}

		return rayMask;
	}

	public static long genRayMove(boolean direction, int amount, long square, long clear, long variation) {
		long rayMove = 0L;

		long mask = shift(direction, amount, square & clear);
		while(mask != 0L) {
			rayMove |= mask;

			if((mask & variation) != 0L) {
				break;
			}
			mask = shift(direction, amount, mask & clear);
		}

		return rayMove;
	}

	public static long genRookOccupancyMask(long square) {
		long mask = 0L;

		// +8
		mask |= genRayMask(true, 8, square, ~0L);
		// -8
		mask |= genRayMask(false, 8, square, ~0L);
		// +1
		mask |= genRayMask(true, 1, square, clearFileH);
		// -1 
		mask |= genRayMask(false, 1, square, clearFileA);

		return clearEdges(square, mask);
	}

	public static long genBishopOccupancyMask(long square) {
		long mask = 0L;

		// +7
		mask |= genRayMask(true, 7, square, clearFileA);
		// -9
		mask |= genRayMask(false, 9, square, clearFileA);
		// -7 
		mask |= genRayMask(false, 7, square, clearFileH);
		// +9 
		mask |= genRayMask(true, 9, square, clearFileH);

		return clearEdges(square, mask);
	}

	public static long clearEdges(long square, long mask) {
		// clear edges (rank 1, rank 8, file A, and file H) on mask
		// if square is not on their respective edge

		if((maskRank1 & square) == 0L) {
			mask &= clearRank1;
		}

		if((maskRank8 & square) == 0L) {
			mask &= clearRank8;
		}

		if((maskFileA & square) == 0L) {
			mask &= clearFileA;
		}

		if((maskFileH & square) == 0L) {
			mask &= clearFileH;
		}
		return mask;
	}
}
