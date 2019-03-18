package baked.potato;

import org.junit.Test;

import static org.junit.Assert.*;

public class BoardTest {

    @Test
    public void getPositionKey() {
        Board cb = Game.parseFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        long expectedPosKey = 0;
        expectedPosKey ^= Zobrist.randSquare[Piece.WHITE_KING.intValue][Square.E1.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.BLACK_KING.intValue][Square.E8.intValue];
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/4P3/8/8/R3K2R b KQkq e3 0 1");
        expectedPosKey = 0;
        expectedPosKey ^= Zobrist.randSquare[Piece.WHITE_KING.intValue][Square.E1.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.A1.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.H1.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.WHITE_PAWN.intValue][Square.E4.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.BLACK_KING.intValue][Square.E8.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.A8.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.H8.intValue];
        expectedPosKey ^= Zobrist.randCastle[0] ^ Zobrist.randCastle[1] ^ Zobrist.randCastle[2] ^ Zobrist.randCastle[3];
        expectedPosKey ^= Zobrist.randSide;
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("4k3/8/8/3Pp3/8/8/8/4K3 w - e6 0 1");
        expectedPosKey = 0;
        expectedPosKey ^= Zobrist.randSquare[Piece.WHITE_KING.intValue][Square.E1.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.WHITE_PAWN.intValue][Square.D5.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.BLACK_PAWN.intValue][Square.E5.intValue];
        expectedPosKey ^= Zobrist.randSquare[Piece.BLACK_KING.intValue][Square.E8.intValue];
        expectedPosKey ^= Zobrist.randEp[Square.E6.intValue];
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("4k3/8/8/8/8/5N2/8/4K3 b - - 0 1");
        expectedPosKey = cb.getPositionKey();
        cb = Game.parseFen("4k3/8/8/8/8/8/8/4K1N1 w - - 0 1");
        cb.move(new Move(Square.G1, Square.F3));
        assertEquals(expectedPosKey, cb.getPositionKey());


        cb = Game.parseFen("4k3/8/8/2r5/8/8/8/4K3 w - - 0 1");
        expectedPosKey = cb.getPositionKey();
        cb = Game.parseFen("2r1k3/8/8/2B5/8/8/8/4K3 b - - 0 1");
        Move m = new Move(Square.C8, Square.C5);
        cb.move(m);
        assertEquals(expectedPosKey, cb.getPositionKey());


        cb = Game.parseFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        m = new Move(Square.E2, Square.E4);
        cb.move(m);
        expectedPosKey = Game.parseFen("4k3/8/8/8/4P3/8/8/4K3 b - e3 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        m = new Move(Square.E8, Square.E7);
        cb.move(m);
        expectedPosKey = Game.parseFen("8/4k3/8/8/4P3/8/8/4K3 w - - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("4k3/8/8/8/4Pp2/8/8/4K3 b - e3 0 1");
        m = new Move(Square.F4, Square.E3);
        m.move |= Move.EP_FLAG;
        cb.move(m);
        expectedPosKey = Game.parseFen("4k3/8/8/8/8/4p3/8/4K3 w - - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        m = new Move(Square.E1, Square.G1);
        m.move |= Move.CASTLE_FLAG;
        cb.move(m);
        expectedPosKey = Game.parseFen("r3k2r/8/8/8/8/8/8/R4RK1 b kq - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1");
        m = new Move(Square.E8, Square.C8);
        m.move |= Move.CASTLE_FLAG;
        cb.move(m);
        expectedPosKey = Game.parseFen("2kr3r/8/8/8/8/8/8/R3K2R w KQ - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        m = new Move(Square.H1, Square.H2);
        cb.move(m);
        expectedPosKey = Game.parseFen("r3k2r/8/8/8/8/8/7R/R3K3 b Qkq - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1");
        m = new Move(Square.A8, Square.A7);
        cb.move(m);
        expectedPosKey = Game.parseFen("4k2r/r7/8/8/8/8/8/R3K2R w KQk - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        m = new Move(Square.E1, Square.E2);
        cb.move(m);
        expectedPosKey = Game.parseFen("r3k2r/8/8/8/8/8/4K3/R6R b kq - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        m = new Move(Square.E2, Square.E4);
        cb.move(m);
        expectedPosKey = Game.parseFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 1");
        m = new Move(Square.E5, Square.D6);
        m.move |= Move.EP_FLAG;
        cb.move(m);
        expectedPosKey = Game.parseFen("rnbqkbnr/1pp1pppp/p2P4/8/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("4k3/2P5/8/8/8/8/8/4K3 w - - 0 1");
        m = new Move(Square.C7, Square.C8);
        m.move |= Move.PROMOTION_FLAG | Move.QUEEN_PROMO_TYPE;
        cb.move(m);
        expectedPosKey = Game.parseFen("2Q1k3/8/8/8/8/8/8/4K3 b - - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        // Kiwipete position
        cb = Game.parseFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        expectedPosKey = cb.getPositionKey();
        int expectedPerftResult = 4085603;
        int perftResult = Game.perft(cb, 4);
        assertEquals(expectedPerftResult, perftResult);
        assertEquals(expectedPosKey, cb.getPositionKey());
    }

    @Test
    public void testTT() {
        Board cb = Game.parseFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        assertEquals(null, cb.tt.get(cb.getPositionKey()));

        cb.tt.put(cb.getPositionKey(), null, 0, 0, 0); // no flag
        assertEquals(null, cb.tt.get(cb.getPositionKey()));

        cb.tt.put(cb.getPositionKey(), null, 0, 0, TTEntry.flagPvNode);
        assertNotEquals(null, cb.tt.get(cb.getPositionKey()));

        cb.tt.incAge();
        cb.tt.put(cb.getPositionKey(), null, 0, 1, TTEntry.flagAllNode);
        assertNotEquals(null, cb.tt.get(cb.getPositionKey()));

        cb.tt.put(cb.getPositionKey(), null, 0, 2, TTEntry.flagCutNode);
        assertEquals(1, cb.tt.get(cb.getPositionKey()).age);
        assertEquals(2, cb.tt.get(cb.getPositionKey()).depth);
        assertEquals(TTEntry.flagCutNode, cb.tt.get(cb.getPositionKey()).flag);

        cb.tt.clear();
        assertEquals(null, cb.tt.get(cb.getPositionKey()));
    }
}