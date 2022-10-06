package com.bloodLantern.chess;

import java.util.ArrayList;
import java.util.List;

import com.bloodLantern.chess.pieces.Piece;

/**
 *
 * @author BloodLantern
 */
public class Tile {

	/**
	 * The current piece on this tile. May be null if nothing on it.
	 */
	private Piece piece;

	/**
	 * Index of the row on which this Tile is.
	 */
	private final int row;

	/**
	 * Index of the line on which this Tile is.
	 */
	private final int line;

	/**
	 * Protection list of this Tile. Every Piece in this list is supposed to
	 * "protect" this Tile. Not protected if empty.
	 */
	private final List<Piece> protecting = new ArrayList<>();

	/**
	 * Potential protection list of this Tile. Every Piece in this list is supposed
	 * to "potentially protect" this Tile. Not potentially protected if empty.
	 *
	 * @see Piece#getPotentiallyProtectedTiles()
	 */
	private final List<Piece> potentiallyProtecting = new ArrayList<>();

	/**
	 * Constructs a Tile object assigned to a row and a line of the board.
	 *
	 * @throws IllegalArgumentException If {@code row} or {@code line} is greater
	 *                                  than 7 or negative.
	 */
	public Tile(int row, int line) {
		if (row > 7 || line > 7 || row < 0 || line < 0)
			throw new IllegalArgumentException("Cannot construct a Tile outside the chess board.");
		this.row = row;
		this.line = line;
	}

	/**
	 * Checks if this Tile is protected by at least one enemy Piece of
	 * {@code piece}.
	 *
	 * @param piece Used to know which side (black or white) is the enemy.
	 * @return True if this Tile is protected by at least one enemy of
	 *         {@code piece}.
	 */
	public boolean isEnemyProtected(boolean white) {
		if (getEnemyProtection(white).isEmpty())
			return false;
		return true;
	}

	/**
	 * Gets the enemy Pieces that are protecting this Tile. Enemy is defined by
	 * {@code piece}.
	 *
	 * @param piece The Piece of which the enemity should be checked.
	 * @return A List containing the Pieces that are enemies of {@code piece} and
	 *         are protecting this Tile.
	 */
	public List<Piece> getEnemyProtection(boolean white) {
		List<Piece> result = new ArrayList<>();
		for (Piece p : protecting)
			if (p.isEnemy(white))
				result.add(p);
		return result;
	}

	/**
	 * Checks if this Tile is potentially protected by at least one enemy Piece of
	 * {@code piece}.
	 *
	 * @param piece Used to know which side (black or white) is the enemy.
	 * @return True if this Tile is potentially protected by at least one enemy of
	 *         {@code piece}.
	 */
	public boolean isEnemyPotentiallyProtected(boolean white) {
		if (getEnemyPotentiallyProtection(white).isEmpty())
			return false;
		return true;
	}

	/**
	 * Gets the enemy Pieces that are potentially protecting this Tile. Enemy is
	 * defined by {@code piece}.
	 *
	 * @param piece Used to know which side (black or white) is the enemy.
	 * @return A List containing the Pieces that are enemies of {@code piece} and
	 *         are potentially protecting this Tile.
	 */
	public List<Piece> getEnemyPotentiallyProtection(boolean white) {
		List<Piece> result = new ArrayList<>();
		for (Piece p : potentiallyProtecting)
			if (p.isEnemy(white))
				result.add(p);
		return result;
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
	 * Setter for the piece value. Also sets {@code piece}'s Tile to this one.
	 *
	 * @param piece The piece to set.
	 * @param setXAndY Should this method also set the X and Y coordinates of this
	 *                 Piece according to {@code tile}'s position on the board ?
	 */
	public void setPiece(Piece piece, boolean setXAndY) {
		if (piece != null)
			if (piece.getTile() != this)
				piece.setTile(this, setXAndY);
		this.piece = piece;
	}

	/**
	 * Getter for the row value.
	 *
	 * @return The row to get.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Getter for the line value.
	 *
	 * @return The line to get.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Getter for the protecting value.
	 *
	 * @return A List of Pieces that are protecting this Tile.
	 */
	public List<Piece> getProtecting() {
		return protecting;
	}

	/**
	 * Getter for the potentiallyProtecting value.
	 *
	 * @return The potentiallyProtecting to get.
	 */
	public List<Piece> getPotentiallyProtecting() {
		return potentiallyProtecting;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "Tile [row=" + row + ", line=" + line + "]";
	}

}
