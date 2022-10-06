package com.bloodLantern.chess.pieces;

/**
 * Interface used for Pawns, Kings and Rooks to reperesent Pieces that can do
 * another action if hasn't move yet.
 *
 * @author BloodLantern
 */
public interface IfNotMoved {

	/**
	 * Getter for the moved value.
	 *
	 * @return The moved to get.
	 */
	public boolean isMoved();

	/**
	 * Setter for the moved value.
	 *
	 * @param moved The moved to set.
	 */
	public void setMoved(boolean moved);

}
