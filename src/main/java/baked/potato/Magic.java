package baked.potato;

public class Magic {
	private static final long[] rookOccupancyMasks;
	private static final long[] bishopOccupancyMasks;

	// using best magics from https://www.chessprogramming.org/Best_Magics_so_far

	private static final long[] rookMagics = {
		0x008000805021c000L, 0x2d40200110004000L, 0x2200201018820040L, 0x0280080044300080L, 0x30800800821c0080L, 0x0480010400801200L, 0x0c00019810040122L, 0x0100102280460100L,
		0x0002800040082682L, 0x20b0400042201008L, 0x5000801000200980L, 0xa100801002080084L, 0x0002800800800400L, 0x0002000200111804L, 0x1311000100460004L, 0x0112800100204880L,
		0x0009208002984000L, 0xf008808020004000L, 0x8404220016008043L, 0x0210008008008095L, 0x4210808008000400L, 0x0223808024000200L, 0xc000040001104826L, 0x000206000108a044L,
		0x4100608080004000L, 0xb440022100430080L, 0x8020011010020400L, 0x0901180080100284L, 0x0008001100050900L, 0x0000420080800400L, 0x1080420400082110L, 0x804480208000c500L,
		0x0200400220800080L, 0x2050042000c04000L, 0x0040110149002000L, 0x0240800800801000L, 0x0002045101000800L, 0x0040801200801400L, 0x001a0024020001a8L, 0x0204b40042000081L,
		0x0261846040088008L, 0x084420005007c000L, 0x0a40823202420021L, 0x0000120141220018L, 0x104400800801800cL, 0x8211000400090002L, 0x2800080110040002L, 0x0884050082460004L,
		0x48FFFE99FECFAA00L, 0x48FFFE99FECFAA00L, 0x497FFFADFF9C2E00L, 0x613FFFDDFFCE9200L, 0xffffffe9ffe7ce00L, 0xfffffff5fff3e600L, 0x0003ff95e5e6a4c0L, 0x510FFFF5F63C96A0L,
		0xEBFFFFB9FF9FC526L, 0x61FFFEDDFEEDAEAEL, 0x53BFFFEDFFDEB1A2L, 0x127FFFB9FFDFB5F6L, 0x411FFFDDFFDBF4D6L, 0x0102000410050802L, 0x0003ffef27eebe74L, 0x7645FFFECBFEA79EL
	};

	private static final int[] rookShifts = {
		12, 11, 11, 11, 11, 11, 11, 12,
		11, 10, 10, 10, 10, 10, 10, 11,
		11, 10, 10, 10, 10, 10, 10, 11,
		11, 10, 10, 10, 10, 10, 10, 11,
		11, 10, 10, 10, 10, 10, 10, 11,
		11, 10, 10, 10, 10, 10, 10, 11,
		10, 9, 9, 9, 9, 9, 9, 10,
		11, 10, 10, 10, 10, 11, 10, 11
	};

	private static final long[] bishopMagics = {
		0xffedf9fd7cfcffffL, 0xfc0962854a77f576L, 0x8018080100210480L, 0x1024040088001200L, 0x0012121010300068L, 0x000104024241000eL, 0xfc0a66c64a7ef576L, 0x7ffdfdfcbd79ffffL,
		0xfc0846a64a34fff6L, 0xfc087a874a3cf7f6L, 0x0000090802088008L, 0x2040282084e00060L, 0x0200011140048840L, 0x0408850108421000L, 0xfc0864ae59b4ff76L, 0x3c0860af4b35ff76L,
		0x73C01AF56CF4CFFBL, 0x41A01CFAD64AAFFCL, 0x0084010214040409L, 0x220480a80a004000L, 0x8002802400a00000L, 0xc004100201420800L, 0x7c0c028f5b34ff76L, 0xfc0a028e5ab4df76L,
		0x0502404009088810L, 0x4004200010824984L, 0x1d00680910048420L, 0x8204040000401080L, 0x8000840000806009L, 0x200043003880a005L, 0x0004007100880400L, 0x400200880a240912L,
		0x0032021280202008L, 0x0284030420200414L, 0x8000108601502400L, 0x3000020080080080L, 0x50100200100a0050L, 0x00a4080200202081L, 0x0001030200041201L, 0x002204a208650040L,
		0xDCEFD9B54BFCC09FL, 0xF95FFA765AFD602BL, 0x0000202428021006L, 0x0011402011100800L, 0x0000412012000301L, 0x04401000408110c0L, 0x43ff9a5cf4ca0c01L, 0x4BFFCD8E7C587601L,
		0xfc0ff2865334f576L, 0xfc0bf6ce5924f576L, 0x0000020142220000L, 0x1204118084044004L, 0x00000860242c0102L, 0x01001002100c4038L, 0xc3ffb7dc36ca8c89L, 0xc3ff8a54f4ca2c89L,
		0xfffffcfcfd79edffL, 0xfc0863fccb147576L, 0x4000001100431080L, 0x1600202000840402L, 0x030080000450c402L, 0x0008004011220080L, 0xfc087e8e4bb2f736L, 0x43ff9e4ef4ca2c89L
	};

	private static final int[] bishopShifts = {
		5, 4, 5, 5, 5, 5, 4, 5,
		4, 4, 5, 5, 5, 5, 4, 4,
		4, 4, 7, 7, 7, 7, 4, 4,
		5, 5, 7, 9, 9, 7, 5, 5,
		5, 5, 7, 9, 9, 7, 5, 5,
		4, 4, 7, 7, 7, 7, 4, 4,
		4, 4, 5, 5, 5, 5, 4, 4,
		5, 4, 5, 5, 5, 5, 4, 5
	};

	private static long[][] rookMoves = new long[64][];
	private static long[][] bishopMoves = new long[64][];

	static {
		// fill occupancy mask arrays
		rookOccupancyMasks = genRookOccupancyMask();
		bishopOccupancyMasks = genBishopOccupancyMask();

		genMagicMoves(true, rookOccupancyMasks, rookMagics, rookShifts, rookMoves);
		genMagicMoves(false, bishopOccupancyMasks, bishopMagics, bishopShifts, bishopMoves);
	}

	public static long getRookMoves(int squareIndex, long occupancy) {
		occupancy &= rookOccupancyMasks[squareIndex];
		int index = (int)((rookMagics[squareIndex] * occupancy) >>> (64 - rookShifts[squareIndex]));
		return rookMoves[squareIndex][index];
	}

	public static long getBishopMoves(int squareIndex, long occupancy) {
		occupancy &= bishopOccupancyMasks[squareIndex];
		int index = (int)((bishopMagics[squareIndex] * occupancy) >>> (64 - bishopShifts[squareIndex]));
		return bishopMoves[squareIndex][index];
	}

	private static void genMagicMoves(boolean rook, long[] occMasks, long[] magics, int[] shifts, long[][] moves) {
		for(int sq = 0; sq < 64; sq++) {
			moves[sq] = new long[1 << shifts[sq]];

			long[] occPerms = genCombos(occMasks[sq]);
			for(int c = 0; c < occPerms.length; c++) {
				long move = 0;
				if(rook) {
					move |= genMove(sq, occPerms[c], Mask.rank[Mask.rankIdx[sq]]);
					move |= genMove(sq, occPerms[c], Mask.file[Mask.fileIdx[sq]]);
				} else {
					move |= genMove(sq, occPerms[c], Mask.diag[Mask.diagIdx[sq]]);
					move |= genMove(sq, occPerms[c], Mask.antiDiag[Mask.antiDiagIdx[sq]]);
				}

				int index = (int)((magics[sq] * occPerms[c]) >>> (64 - shifts[sq]));
				moves[sq][index] = move;
			}
		}
	}

	private static long genMove(int sq, long occ, long mask) {
		// https://www.chessprogramming.org/Hyperbola_Quintessence
		long sqMask = 1L << sq;
		occ &= mask;
		return ((occ - sqMask) ^ Long.reverse(Long.reverse(occ) - Long.reverse(sqMask))) & mask;
	}

	private static long[] genCombos(long x) {
		int numBits = Long.bitCount(x);
		if(numBits == 0) return null;
		long[] combos = new long[1 << numBits];
		long[] setBits = new long[numBits];

		int index = 0;
		long temp = x;
		while(temp != 0) {
			long setBit = Long.lowestOneBit(temp);
			setBits[index++] = setBit;
			temp &= ~setBit;
		}

		for(int bitCombo = 0; bitCombo < (1 << numBits); bitCombo++) {
			long c = 0;

			long mask = 1;
			for(int bitPosition = 0; bitPosition < numBits; bitPosition++) {
				if((bitCombo & mask) != 0) {
					c |= setBits[bitPosition];
				}

				mask <<= 1;
			}

			combos[bitCombo] = c;
		}

		return combos;
	}

	private static long[] genRookOccupancyMask() {
		long[] masks = new long[64];

		for(int i = 0; i < 64; i++) {
			// & clears bits on file a and file h
			masks[i] |= Mask.rank[i / 8] & 0x7E7E7E7E7E7E7E7EL;
			// & clears bits on rank 1 and rank 8
			masks[i] |= Mask.file[i % 8] & 0xFFFFFFFFFFFF00L;
			// clear bit i
			masks[i] &= ~(1L << i);
		}

		return masks;
	}

	private static long[] genBishopOccupancyMask() {
		long[] masks = new long[64];

		for(int i = 0; i < 64; i++) {
			masks[i] |= Mask.diag[Mask.diagIdx[i]] | Mask.antiDiag[Mask.antiDiagIdx[i]];
			// clear bit i and border bits
			masks[i] &= ~(1L << i | 0xFF818181818181FFL);
		}

		return masks;
	}
}
