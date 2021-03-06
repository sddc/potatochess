package baked.potato;

import java.util.Arrays;

public class Search implements Runnable {
    public static final int INFINITY = 1000000;
    public static final int MAX_PLY = 64;
    public static final int MATE = 30000;
    public static final int MAX_MATE = MATE - MAX_PLY * 2;
    private int nodes = 0;
    private volatile boolean stop = false;
    private Board b;
    private int depth;
    private long stopTime;
    private boolean infinite;

    public Search(Board b, int depth, int time) {
        this.b = b;
        this.depth = depth <= 0 ? MAX_PLY : depth;
        if(time <= 0) {
            infinite = true;
        } else {
            infinite = false;
            stopTime = time * 1_000_000L;
        }
    }

    public void stop() {
        stop = true;
    }

    public void search(Board b, int depth) {
        b.resetPly();
        b.tt.incAge();

        Move bestMove = null;
        for(int d = 1; d <= depth; d++) {
            nodes = 0;
            long start = System.nanoTime();
            int bestScore = negamax(b, d, -INFINITY, INFINITY);
            if(stop) break;
            double elapsed = (System.nanoTime() - start) * 1e-6;
            double nps = nodes / (elapsed * 1e-3);

            System.out.printf("info depth %d score cp %d time %.0f nodes %d nps %.0f pv %s\n", d, bestScore, elapsed, nodes, nps, pv(b, d));
            bestMove = b.tt.get(b.getPositionKey()).bestMove;
        }

        System.out.println("bestmove " + bestMove);
    }

    private String pv(Board b, int depth) {
        TTEntry ttEntry = b.tt.get(b.getPositionKey());
        String result = "";
        int initialPly = b.getPly();
        for(int i = 0; i < depth && ttEntry != null; i++) {
            Move m = ttEntry.bestMove;

            Movelist ml = MoveGen.getMoves(b, false);
            if(Arrays.asList(ml.moves).contains(m)) {
                b.move(m);
                result += result.length() > 0 ? " " + m.toString() : m.toString();
                ttEntry = b.tt.get(b.getPositionKey());
            } else {
                break;
            }
        }

        while(b.getPly() > initialPly) {
            b.undoMove();
        }

        return result;
    }

    private int quiescence(Board b, int alpha, int beta) {
        if(!infinite && (System.nanoTime() >= stopTime)) {
            stop = true;
            return 0;
        }

        if(MoveGen.isKingInCheck(b, b.getActiveColor())) {
            return negamax(b, 1, alpha, beta);
        }

        nodes++;

        if(b.repetition() || b.getFiftyMove() >= 100) {
            return 0;
        }

        int standPat = Evaluation.score(b);
        if(standPat >= beta) return beta;
        if(alpha < standPat) alpha = standPat;

        Movelist ml = MoveGen.getMoves(b, true);
        MoveGen.sortMoves(ml, null);

        for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
            Move m = ml.moves[mIdx];

            if(b.move(m)) {
                int eval = -quiescence(b, -beta, -alpha);

                if(eval > alpha) {
                    alpha = eval;
                }
            }
            b.undoMove();
            if(stop) return 0;

            if(alpha >= beta) {
                return beta;
            }
        }

        return alpha;
    }

    private int negamax(Board b, int depth, int alpha, int beta) {
        if(!infinite && (System.nanoTime() >= stopTime)) {
            stop = true;
            return 0;
        }

        if(depth == 0) {
            return quiescence(b, alpha, beta);
        }

        nodes++;

        if(b.getPly() > 0 && (b.repetition() || b.getFiftyMove() >= 100)) {
            return 0;
        }

        int oldAlpha = alpha;

        Move pvMove = null;
        TTEntry ttEntry = b.tt.get(b.getPositionKey());
        if(ttEntry != null) {
            if(ttEntry.depth >= depth) {
                int score = mateScore(ttEntry.score, b.getPly(), false);

                if ((ttEntry.flag & TTEntry.flagPvNode) != 0) {
                    // exact
                    return score;
                } else if ((ttEntry.flag & TTEntry.flagCutNode) != 0) {
                    // lowerbound
                    alpha = Math.max(alpha, score);
                } else if ((ttEntry.flag & TTEntry.flagAllNode) != 0) {
                    // upperbound
                    beta = Math.min(beta, score);
                }

                if (alpha >= beta) {
                    return score;
                }
            } else if((ttEntry.flag & TTEntry.flagPvNode) != 0) {
                pvMove = ttEntry.bestMove;
            }
        }

        Movelist ml = MoveGen.getMoves(b, false);
        MoveGen.sortMoves(ml, pvMove);
        Move bestMove = null;
        int legalMoves = 0;
        int maxEval = -INFINITY;
        for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
            Move m = ml.moves[mIdx];

            if(b.move(m)) {
                legalMoves++;
                int eval = -negamax(b, depth - 1, -beta, -alpha);

                if(eval > maxEval) {
                    maxEval = eval;
                    bestMove = m;

                    if(maxEval > alpha) {
                        alpha = maxEval;
                    }
                }
            }
            b.undoMove();
            if(stop) return 0;

            if(alpha >= beta) {
                break;
            }
        }

        if(legalMoves == 0) {
            if(MoveGen.isKingInCheck(b, b.getActiveColor())) {
                return -MATE + b.getPly();
            } else {
                return 0;
            }
        }

        int flag;
        if(maxEval <= oldAlpha) {
            // upperbound
            flag = TTEntry.flagAllNode;
        } else if(maxEval >= beta) {
            // lowerbound
            flag = TTEntry.flagCutNode;
        } else {
            // exact
            flag = TTEntry.flagPvNode;
        }

        b.tt.put(b.getPositionKey(), bestMove, mateScore(maxEval, b.getPly(), true), depth, flag);

        return maxEval;
    }

    private static int mateScore(int score, int ply, boolean absToRel) {
        if (Math.abs(score) >= MAX_MATE) {
            if (absToRel) {
                return score < 0 ? score - ply : score + ply;
            } else {
                return score < 0 ? score + ply : score - ply;
            }
        }

        return score;
    }

    @Override
    public void run() {
        stopTime = System.nanoTime() + stopTime;
        search(b, depth);
    }
}
