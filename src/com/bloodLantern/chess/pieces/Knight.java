package com.bloodLantern.chess.pieces;

import java.util.ArrayList;

import com.bloodLantern.chess.Chess;
import com.bloodLantern.chess.Tile;

/**
 * A Knight can only move in a L shape but in every directions.
 *
 * @author BloodLantern
 */
public final class Knight extends Piece {

	public static final char FEN_VALUE = 'N';

	/**
	 * @param white Whether the Piece should be white or black.
	 * @param tile  The Tile on which this Piece should be.
	 */
	public Knight(boolean white, Tile tile) {
		super(white, white ? Piece.TEXTURE_WHITE_KNIGHT : Piece.TEXTURE_BLACK_KNIGHT, tile);
	}

	public Knight(Pawn pawn) {
		super(pawn, pawn.isWhite() ? Piece.TEXTURE_WHITE_KNIGHT : Piece.TEXTURE_BLACK_KNIGHT);
	}

	@Override
	public ArrayList<Tile> getProtectedTiles() {
		// Remember that negative Y is on top
		ArrayList<Tile> list = new ArrayList<>();
		Tile[][] tiles = Chess.getInstance().getTiles();
		int row;
		int line;
		boolean firstLoop = true;
		for (int i = 0; i < 2; i++) {
			for (int x = -1; x < 3; x += 2)
				for (int y = -1; y < 3; y += 2) {
					if (firstLoop) {
						row = this.tile.getRow() + x * 2;
						line = this.tile.getLine() + y;
					} else {
						row = this.tile.getRow() + x;
						line = this.tile.getLine() + y * 2;
					}
					if (row < 0 || row > 7 || line < 0 || line > 7)
						continue;
					list.add(tiles[row][line]);
				}
			firstLoop = false;
		}
		return list;
	}

	/**
	 * The Knight implementation of this method makes it call
	 * {@link #getProtectedTiles()} instead.
	 */
	@Override
	public ArrayList<Tile> getPotentiallyProtectedTiles() {
		return getProtectedTiles();
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
		return "Knight [" + super.toString() + "]";
	}

}
