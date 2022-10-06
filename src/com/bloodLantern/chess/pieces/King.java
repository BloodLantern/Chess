package com.bloodLantern.chess.pieces;

import java.util.ArrayList;

import com.bloodLantern.chess.Chess;
import com.bloodLantern.chess.Tile;

/**
 * A King can move on its adjacent Tiles (also diagonals) but not on attacked
 * Tiles.
 *
 * @author BloodLantern
 */
public final class King extends Piece implements IfNotMoved {

	public static final char FEN_VALUE = 'K';

	private static King whiteKing;
	private static King blackKing;

	private boolean moved = false;

	/**
	 * @param white Whether the Piece should be white or black.
	 * @param tile  The Tile on which this Piece should be.
	 */
	public King(boolean white, Tile tile) {
		super(white, white ? Piece.TEXTURE_WHITE_KING : Piece.TEXTURE_BLACK_KING, tile);
		if (white)
			whiteKing = this;
		else
			blackKing = this;
	}

	/**
	 * Checks if a King piece movement is possible to {@code moveTo} from the
	 * current position.
	 *
	 * @param moveTo The tile to which this King is trying to move on.
	 * @return True if the movement should be executed. False otherwise.
	 */
	@Override
	public boolean checkMove(Tile moveTo) {
		if (getProtectedTiles().contains(moveTo))
			if (moveTo.getPiece() == null || moveTo.getPiece().isEnemy(white))
				if (moveTo.isEnemyProtected(white))
					return false;
				else {
					// If in check
					if (isInDanger()) {
						return tryMove(moveTo);
					} else
						return true;
				}
		return false;
	}

	@Override
	public ArrayList<Tile> getProtectedTiles() {
		// Remember that negative Y is on top
		ArrayList<Tile> list = new ArrayList<>();
		Tile[][] tiles = Chess.getInstance().getTiles();
		int row;
		int line;
		for (int i = -1; i <= 1; i++)
			for (int ii = -1; ii <= 1; ii++)
				if (i != 0 || ii != 0) {
					row = tile.getRow() + i;
					line = tile.getLine() + ii;
					// Check if inside the board
					if (row >= 0 && row <= 7 && line >= 0 && line <= 7)
						list.add(tiles[row][line]);
				}
		return list;
	}

	/**
	 * The King implementation of this method makes it call
	 * {@link #getProtectedTiles()} instead.
	 */
	@Override
	public ArrayList<Tile> getPotentiallyProtectedTiles() {
		return getProtectedTiles();
	}

	public boolean checkCastle(Rook rook) {
		if (moved || rook.isMoved())
			return false;
		Tile[][] tiles = Chess.getInstance().getTiles();
		int x = rook.getTile().getRow();
		while (x != tile.getRow()) {
			if (x < tile.getRow())
				x++;
			else
				x--;
			if (tiles[x][tile.getLine()].getPiece() != null)
				if (!tiles[x][tile.getLine()].getPiece().equals(this))
					return false;
			if (tiles[x][tile.getLine()].isEnemyProtected(white))
				return false;
		}
		return true;
	}

	/**
	 * Getter for the moved value.
	 *
	 * @return The moved to get.
	 */
	@Override
	public boolean isMoved() {
		return moved;
	}

	/**
	 * Setter for the moved value.
	 *
	 * @param moved The moved to set.
	 */
	@Override
	public void setMoved(boolean moved) {
		this.moved = moved;
	}

	/**
	 * Getter for the whiteKing value.
	 *
	 * @return The whiteKing to get.
	 */
	public static King getWhiteKing() {
		return whiteKing;
	}

	/**
	 * Getter for the blackKing value.
	 *
	 * @return The blackKing to get.
	 */
	public static King getBlackKing() {
		return blackKing;
	}
	
	/**
	 * Returns the enemy King of {@code piece}.
	 * 
	 * @param piece To check which King is the enemy one.
	 * @return The enemy King of {@code piece}.
	 */
	public static King getEnemyKing(Piece piece) {
		return !piece.isWhite() ? whiteKing : blackKing;
	}

	@Override
	public char getFENValue() {
		if (white)
			return FEN_VALUE;
		return Character.toLowerCase(FEN_VALUE);
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "King [moved=" + moved + ", " + super.toString() + "]";
	}

}
