package baked.potato;

import java.util.ArrayList;

public class Search {
    public static final int INFINITY = 1000000;
    public static final int MAX_PLY = 64;
    public static final int MATE = 30000;
    public static final int MAX_MATE = MATE - MAX_PLY;
    private static int nodes = 0;

    public static void search(Board b, int depth, boolean side) {
        for(int d = 1; d <= depth; d++) {
            long start = System.nanoTime();
            b.tt.incAge();
            nodes = 0;
            int bestScore = negamax(b, d, -INFINITY, INFINITY, side);
            double elapsed = (System.nanoTime() - start) * 1e-6;
            double nps = nodes / (elapsed * 1e-3);

            System.out.printf("info depth %d score cp %d time %.0f nodes %d nps %.0f pv %s\n", d, bestScore, elapsed, nodes, nps, pv(b, d));
        }

        Move bestMove = new Move(b.tt.get(b.getPositionKey()).bestMove);
        System.out.println("bestmove " + bestMove);
    }

    private static String pv(Board b, int depth) {
        TTEntry ttEntry = b.tt.get(b.getPositionKey());
        String result = "";
        int initialPly = b.getPly();
        for(int i = 0; i < depth && ttEntry != null; i++) {
            Move m = new Move(ttEntry.bestMove);

            ArrayList<Move> moves = MoveGen.getMoves(b.getActiveColor());
            if(moves.contains(m)) {
                b.move(b.getActiveColor(), m);
                b.toggleActiveColor();
                result += result.length() > 0 ? " " + m.toString() : m.toString();
                ttEntry = b.tt.get(b.getPositionKey());
            } else {
                break;
            }
        }

        while(b.getPly() > initialPly) {
            b.undoMove(b.toggleActiveColor());
        }

        return result;
    }

    private static int negamax(Board b, int depth, int alpha, int beta, boolean side) {
        int oldAlpha = alpha;
        nodes++;

        TTEntry ttEntry = b.tt.get(b.getPositionKey());
        if(ttEntry != null && ttEntry.depth >= depth) {
            int score = ttEntry.score;

            if(Math.abs(score) >= MAX_MATE) {
                score = score < 0 ? score + b.getPly() : score - b.getPly();
            }

            if((ttEntry.flag & TTEntry.flagPvNode) != 0) {
                return score;
            } else if((ttEntry.flag & TTEntry.flagCutNode) != 0) {
                alpha = Math.max(alpha, score);
            } else if((ttEntry.flag & TTEntry.flagAllNode) != 0) {
                beta = Math.min(beta, score);
            }

            if(alpha >= beta) {
                return score;
            }
        }

        int color = side ? -1 : 1;

        if(depth == 0) {
            return Evaluation.score(b) * color;
        }

        ArrayList<Move> moves = MoveGen.getMoves(side);
        if(moves.size() == 0) {
            if(MoveGen.isKingInCheck(side)) {
                return -MATE + b.getPly();
            } else {
                return 0;
            }
        }

        Move bestMove = null;
        for(Move m : moves) {
            b.move(side, m);
            int eval = -negamax(b, depth - 1, -beta, -alpha, b.toggleActiveColor());
            b.undoMove(b.toggleActiveColor());

            if(eval > alpha) {
                alpha = eval;
                bestMove = m;

                if(alpha >= beta) {
                    break;
                }
            }
        }

        int flag;
        if(alpha <= oldAlpha) {
            flag = TTEntry.flagAllNode;
        } else if(alpha >= beta) {
            flag = TTEntry.flagCutNode;
        } else {
            flag = TTEntry.flagPvNode;
        }

        if(Math.abs(alpha) >= MAX_MATE) {
            int rel_alpha = alpha < 0 ? alpha - b.getPly() : alpha + b.getPly();
            b.tt.put(b.getPositionKey(), bestMove != null ? bestMove.move : 0, rel_alpha, depth, flag);
        } else {
            b.tt.put(b.getPositionKey(), bestMove != null ? bestMove.move : 0, alpha, depth, flag);
        }

        return alpha;
    }
}
