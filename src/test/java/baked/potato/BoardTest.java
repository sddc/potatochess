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
        expectedPosKey ^= Zobrist.randCastle[0];
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
        expectedPosKey ^= Zobrist.randCastle[15];
        expectedPosKey ^= Zobrist.randSide;
        expectedPosKey ^= Zobrist.randEp[Square.E3.intValue];
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("4k3/8/8/8/8/5N2/8/4K3 b - - 0 1");
        expectedPosKey = cb.getPositionKey();
        cb = Game.parseFen("4k3/8/8/8/8/8/8/4K1N1 w - - 0 1");
        cb.move(cb.getActiveColor(), new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT));
        cb.toggleActiveColor();
        assertEquals(expectedPosKey, cb.getPositionKey());


        cb = Game.parseFen("4k3/8/8/2r5/8/8/8/4K3 w - - 0 1");
        expectedPosKey = cb.getPositionKey();
        cb = Game.parseFen("2r1k3/8/8/2B5/8/8/8/4K3 b - - 0 1");
        Move m = new Move(Square.C8, Square.C5, Piece.BLACK_ROOK);
        m.setFlag(Flag.CAPTURE);
        m.setCapturePieceType(Piece.WHITE_BISHOP);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        assertEquals(expectedPosKey, cb.getPositionKey());


        cb = Game.parseFen("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1");
        m = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
        m.setFlag(Flag.DOUBLE_PAWN_PUSH);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("4k3/8/8/8/4P3/8/8/4K3 b - e3 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        m = new Move(Square.E8, Square.E7, Piece.BLACK_KING);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("8/4k3/8/8/4P3/8/8/4K3 w - - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("4k3/8/8/8/4Pp2/8/8/4K3 b - e3 0 1");
        m = new Move(Square.F4, Square.E3, Piece.BLACK_PAWN);
        m.setFlag(Flag.EP_CAPTURE);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("4k3/8/8/8/8/4p3/8/4K3 w - - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        m = new Move(Square.E1, Square.G1, Piece.WHITE_KING);
        m.setFlag(Flag.CASTLE);
        m.setCastleType(true);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("r3k2r/8/8/8/8/8/8/R4RK1 b kq - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1");
        m = new Move(Square.E8, Square.C8, Piece.BLACK_KING);
        m.setFlag(Flag.CASTLE);
        m.setCastleType(false);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("2kr3r/8/8/8/8/8/8/R3K2R w KQ - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        m = new Move(Square.H1, Square.H2, Piece.WHITE_ROOK);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("r3k2r/8/8/8/8/8/7R/R3K3 b Qkq - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1");
        m = new Move(Square.A8, Square.A7, Piece.BLACK_ROOK);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("4k2r/r7/8/8/8/8/8/R3K2R w KQk - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        m = new Move(Square.E1, Square.E2, Piece.WHITE_KING);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("r3k2r/8/8/8/8/8/4K3/R6R b kq - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        cb = Game.parseFen("4k3/2P5/8/8/8/8/8/4K3 w - - 0 1");
        m = new Move(Square.C7, Square.C8, Piece.WHITE_PAWN);
        m.setFlag(Flag.PROMOTION);
        m.setPromotionType(Piece.WHITE_QUEEN);
        cb.move(cb.getActiveColor(), m);
        cb.toggleActiveColor();
        expectedPosKey = Game.parseFen("2Q1k3/8/8/8/8/8/8/4K3 b - - 0 1").getPositionKey();
        assertEquals(expectedPosKey, cb.getPositionKey());

        // Kiwipete position
        cb = Game.parseFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        MoveGen.setBoard(cb);
        expectedPosKey = cb.getPositionKey();
        int expectedPerftResult = 4085603;
        int perftResult = Game.perft(cb, cb.getActiveColor(), 4);
        assertEquals(expectedPerftResult, perftResult);
        assertEquals(expectedPosKey, cb.getPositionKey());
    }
}