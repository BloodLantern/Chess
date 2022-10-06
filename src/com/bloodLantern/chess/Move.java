package com.bloodLantern.chess;

import com.bloodLantern.chess.pieces.IfNotMoved;
import com.bloodLantern.chess.pieces.King;
import com.bloodLantern.chess.pieces.Pawn;
import com.bloodLantern.chess.pieces.Piece;

/**
 * A Move object is used to represent a movement of a Piece to a Tile. It may be
 * used to execute or simulate a movement using either
 * {@link #makeMove(boolean)} or {@link #unmakeMove(boolean)}. Remember that
 * those methods don't repaint the main rendering frame.
 *
 * @author BloodLantern
 */
public class Move {

	private Piece piece;
	private Tile moveFrom;
	private Tile moveTo;
	private Piece moveToPiece;
	private Tile enPassantTile;
	private Piece enPassantPiece;
	private Piece promotionPiece;
	private int score;

	/**
	 * 
	 * @param piece  The Piece to move.
	 * @param moveTo The ending Tile of the movement.
	 */
	public Move(Piece piece, Tile moveTo) {
		this.piece = piece;
		moveFrom = piece.getTile();
		this.moveTo = moveTo;
		moveToPiece = moveTo.getPiece();
		Chess chess = Chess.getInstance();
		if (moveTo.equals(chess.getEnPassant()))
			if (piece.isWhite())
				enPassantPiece = chess.getTiles()[moveTo.getRow()][moveTo.getLine() + 1].getPiece();
			else
				enPassantPiece = chess.getTiles()[moveTo.getRow()][moveTo.getLine() - 1].getPiece();
		if (enPassantPiece != null)
			enPassantTile = enPassantPiece.getTile();
	}

	/**
	 * Executing this method will cause the movement to be executed, that means
	 * {@link #piece}'s {@link Piece#getTile() tile} will be set from
	 * {@link #moveFrom} to {@link #moveTo}. This method also calls
	 * {@link Chess#computeTileProtection()}.
	 */
	public void makeMove(boolean setXAndY) {
		moveTo.setPiece(piece, setXAndY);
		if (isEnPassant())
			enPassantTile.setPiece(null, false);
		Chess.getInstance().computeTileProtection();
	}

	/**
	 * The opposite operation of {@link #makeMove(boolean)}: executing this method
	 * will cause the movement to be cancelled, that means {@link #piece}'s
	 * {@link Piece#getTile() tile} will be set from {@link #moveTo} to
	 * {@link #moveFrom}. This method also calls
	 * {@link Chess#computeTileProtection()}.
	 */
	public void unmakeMove(boolean setXAndY) {
		moveFrom.setPiece(piece, setXAndY);
		moveTo.setPiece(moveToPiece, false);
		if (isEnPassant())
			enPassantTile.setPiece(enPassantPiece, false);
		Chess.getInstance().computeTileProtection();
	}

	/**
	 * Finalizes this Move. That means this method will first call
	 * {@link #makeMove(boolean)} if {@link #madeMove()} returns false and then sets
	 * the {@code moved} field of {@code piece} to true if it is an instance of
	 * {@link IfNotMoved}. It will eventually transform {@code piece} if it is an
	 * instance of {@link Pawn} and reached the opposite side.
	 */
	public void finalizeMove() {
		if (!madeMove())
			makeMove(true);
		Chess chess = Chess.getInstance();
		// Refreshes the fifty-move rule
		if (moveToPiece == null)
			chess.incrementHalfmoveClock();
		else
			chess.setHalfmoveClock(0);
		// Refreshes the captured pieces count
		if (moveToPiece != null)
			chess.getCapturedPieces().add(moveToPiece);
		// Checks and execute en passant before resetting it
		if (piece instanceof Pawn) {
			chess.setHalfmoveClock(0);
			if (moveTo.equals(chess.getEnPassant()))
				// Execute en passant
				if (piece.isWhite())
					chess.getTiles()[moveTo.getRow()][moveTo.getLine() + 1].setPiece(null, false);
				else
					chess.getTiles()[moveTo.getRow()][moveTo.getLine() - 1].setPiece(null, false);
			chess.computeTileProtection();
		}
		if (chess.getHalfmoveClock() >= 50) {
			chess.setDraw(true);
			chess.setPlaying(false);
		}
		// Reset en passant
		chess.setEnPassant(null);
		if (piece instanceof Pawn pawn)
			if (moveTo.getLine() == 0 || moveTo.getLine() == 7)
				// Transform Pawn
				if (chess.ai != null)
					if (chess.ai.isWhite() == chess.getTurn())
						pawn.transform(chess.ai);
					else
						pawn.transform(null);
				else
					pawn.transform(null);
			else if (moveTo.getLine() - moveFrom.getLine() == 2)
				// Setup en passant
				chess.setEnPassant(chess.getTiles()[moveTo.getRow()][moveFrom.getLine() + 1]);
			else if (moveTo.getLine() - moveFrom.getLine() == -2)
				// Setup en passant
				chess.setEnPassant(chess.getTiles()[moveTo.getRow()][moveFrom.getLine() - 1]);
		if (piece instanceof IfNotMoved inm)
			if (!inm.isMoved())
				inm.setMoved(true);
		if (!chess.getTurn())
			chess.setFullmoveNumber(chess.getFullmoveNumber() + 1);
	}

	/**
	 * Checks whether this move has been made or not. This method will check what
	 * method was lastly called within {@link #makeMove(boolean)} and
	 * {@link #unmakeMove(boolean)}.
	 *
	 * @return True if the move has been made. False if nothing changed.
	 */
	public boolean madeMove() {
		if (piece.getTile().equals(moveFrom))
			return false;
		return true;
	}

	public boolean isCapture() {
		if (moveToPiece != null)
			if (moveToPiece.isEnemy(piece.isWhite()))
				return true;
		return false;
	}

	public boolean isCastle() {
		if (piece instanceof King)
			if (moveTo.getRow() - moveFrom.getRow() == -2 || moveTo.getRow() - moveFrom.getRow() == 2)
				return true;
		return false;
	}

	public boolean isPromotion() {
		if (piece instanceof Pawn)
			if (moveTo.getLine() == 0 || moveTo.getLine() == 7)
				return true;
		return false;
	}

	public boolean isCheck() {
		boolean madeMove = false;
		if (madeMove())
			madeMove = true;
		else
			makeMove(false);
		if (King.getEnemyKing(piece).isInDanger()) {
			if (!madeMove)
				unmakeMove(false);
			return true;
		}
		if (!madeMove)
			unmakeMove(false);
		return false;
	}

	public boolean isCheckmate() {
		boolean madeMove = false;
		if (madeMove())
			madeMove = true;
		else
			makeMove(false);
		if (Chess.getInstance().checkWin(false) && !Chess.getInstance().isDraw()) {
			if (!madeMove)
				unmakeMove(false);
			return true;
		}
		if (!madeMove)
			unmakeMove(false);
		return false;
	}

	public boolean isDraw() {
		boolean madeMove = false;
		if (madeMove())
			madeMove = true;
		else
			makeMove(false);
		if (Chess.getInstance().checkWin(false) && Chess.getInstance().isDraw()) {
			if (!madeMove)
				unmakeMove(false);
			return true;
		}
		if (!madeMove)
			unmakeMove(false);
		return false;
	}
	
	public boolean isEnPassant() {
		return enPassantPiece != null && piece instanceof Pawn;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "Move [piece=" + piece + ", moveTo=" + moveTo + ", moveToPiece=" + moveToPiece + "]";
	}

	/**
	 * Getter for the piece value.
	 * 
	 * @return The piece to get.
	 */
	public Piece getPiece() {
		return piece;
	}

	/**
	 * Setter for the piece value.
	 * 
	 * @param piece The piece to set.
	 */
	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	/**
	 * Getter for the moveFrom value.
	 * 
	 * @return The moveFrom to get.
	 */
	public Tile getMoveFrom() {
		return moveFrom;
	}

	/**
	 * Setter for the moveFrom value.
	 * 
	 * @param moveFrom The moveFrom to set.
	 */
	public void setMoveFrom(Tile moveFrom) {
		this.moveFrom = moveFrom;
	}

	/**
	 * Getter for the moveTo value.
	 * 
	 * @return The moveTo to get.
	 */
	public Tile getMoveTo() {
		return moveTo;
	}

	/**
	 * Setter for the moveTo value.
	 * 
	 * @param moveTo The moveTo to set.
	 */
	public void setMoveTo(Tile moveTo) {
		this.moveTo = moveTo;
	}

	/**
	 * Getter for the moveToPiece value.
	 * 
	 * @return The moveToPiece to get.
	 */
	public Piece getMoveToPiece() {
		return moveToPiece;
	}

	/**
	 * Setter for the moveToPiece value.
	 * 
	 * @param moveToPiece The moveToPiece to set.
	 */
	public void setMoveToPiece(Piece moveToPiece) {
		this.moveToPiece = moveToPiece;
	}

	/**
	 * Getter for the promotionPiece value.
	 * 
	 * @return The promotionPiece to get.
	 */
	public Piece getPromotionPiece() {
		return promotionPiece;
	}

	/**
	 * Setter for the promotionPiece value.
	 * 
	 * @param promotionPiece The promotionPiece to set.
	 */
	public void setPromotionPiece(Piece promotionPiece) {
		this.promotionPiece = promotionPiece;
	}

	/**
	 * Getter for the score value.
	 * 
	 * @return The score to get.
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Setter for the score value.
	 * 
	 * @param score The score to set.
	 */
	public void setScore(int score) {
		this.score = score;
	}

}
