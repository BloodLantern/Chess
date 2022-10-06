package com.bloodLantern.chess.pieces;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.bloodLantern.chess.AI;
import com.bloodLantern.chess.Chess;
import com.bloodLantern.chess.Tile;

/**
 * A Pawn can move forward by one if no Piece is on the Tile or two Tiles if it
 * has'nt move yet or can attack the forward left and right Tiles if enemy
 * Pieces stand on them. It also transforms into a Queen/Rook/Knight/Bishop
 * (chosen by the player) when reaching the enemy's last line.
 *
 * @author BloodLantern
 */
public final class Pawn extends Piece implements IfNotMoved {

	public static final char FEN_VALUE = 'P';

	private boolean moved = false;
	private int adaptiveDirection = 0;

	/**
	 * @param white Whether the Piece should be white or black.
	 * @param tile  The Tile on which this Piece should be.
	 */
	public Pawn(boolean white, Tile tile) {
		super(white, white ? Piece.TEXTURE_WHITE_PAWN : Piece.TEXTURE_BLACK_PAWN, tile);
	}

	/**
	 * Computes and sets {@code adaptiveDirection}. May be called more than once
	 * even if not needed.
	 *
	 * @return The newly computed value.
	 */
	public int computeAdaptiveDirection() {
		if (white)
			adaptiveDirection = tile.getLine() - 1;
		else
			adaptiveDirection = tile.getLine() + 1;
		return adaptiveDirection;
	}

	/**
	 * Asks the user to choose a transformation for his Pawn.
	 */
	public void transform(AI ai) {
		if (ai == null) {
			Chess.timer = false;
			JFrame frame = new JFrame();
			JPanel container = new JPanel();
			frame.setResizable(false);
			frame.setSize(300 + 16, 60 + 39);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setTitle("Transform your pawn");
			container.setBackground(Color.WHITE);
			container.setLayout(null);
			final class PieceButton extends JButton implements MouseListener, ActionListener {

				/**
				 *
				 */
				private static final long serialVersionUID = 1L;
				private Image texture;
				private boolean hovered = false;
				private boolean clicked = false;
				private Piece piece;

				public PieceButton(Piece piece) {
					setText("");
					this.texture = Piece.getPieceImage(piece);
					this.piece = piece;
					addMouseListener(this);
					addActionListener(this);
				}

				@Override
				public void paintComponent(Graphics g) {
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, getWidth(), getHeight());
					if (clicked) {
						g.setColor(new Color(0xC1, 0xC1, 0xC1, 100));
						g.fillRect(0, 0, getWidth(), getHeight());
					} else if (hovered) {
						g.setColor(new Color(0xD3, 0xD3, 0xD3, 100));
						g.fillRect(0, 0, getWidth(), getHeight());
					}
					g.drawImage(texture, 0, 0, 60, 60, null);
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					frame.dispose();
					transform(piece);
				}

				@Override
				public void mouseClicked(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					clicked = true;
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					clicked = false;
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					hovered = true;
				}

				@Override
				public void mouseExited(MouseEvent e) {
					hovered = false;
					clicked = false;
				}

			}
			PieceButton button = new PieceButton(new Queen(this));
			button.setBounds(0, 0, 60, 60);
			container.add(button);
			button = new PieceButton(new Rook(this));
			button.setBounds(60, 0, 60, 60);
			container.add(button);
			button = new PieceButton(new Knight(this));
			button.setBounds(120, 0, 60, 60);
			container.add(button);
			button = new PieceButton(new Bishop(this));
			button.setBounds(180, 0, 60, 60);
			container.add(button);
			button = new PieceButton(this);
			button.setBounds(240, 0, 60, 60);
			container.add(button);
			frame.add(container);
			frame.setVisible(true);
		} else {
			transform(ai.choosePromotion(this));
		}
	}

	/**
	 * Transforms this Pawn into another Piece. This method also turns the
	 * {@link Chess#timer} back on.
	 *
	 * @param piece The Piece to transform this Pawn into.
	 */
	private void transform(Piece piece) {
		if (!(piece instanceof Pawn)) {
			piece.setTile(tile, true);
			piece.refreshTileProtection();
			Chess.getInstance().mainPanel.repaint();
		}
		Chess.timer = true;
		Chess.getInstance().computeTileProtection();
	}

	/**
	 * Pawn implementation because it is the only Piece that moves on and protects
	 * different Tiles.
	 */
	@Override
	public boolean checkMove(Tile moveTo) {
		computeAdaptiveDirection();
		boolean pawnCheck = false;
		if (moveTo != null)
			if (moveTo.getPiece() instanceof King)
				return false;
			else {
				if (getProtectedTiles().contains(moveTo)) {
					if (moveTo.getPiece() != null) {
						if (moveTo.getPiece().isEnemy(white))
							pawnCheck = true;
					} else if (moveTo.equals(Chess.getInstance().getEnPassant()))
						pawnCheck = true;
				} else if (moveTo.getRow() == tile.getRow())
					if (moveTo.getPiece() == null)
						if (moveTo.getLine() - adaptiveDirection == 0)
							pawnCheck = true;
						else if (!moved)
							if (moveTo.getLine() - adaptiveDirection == -1 || moveTo.getLine() - adaptiveDirection == 1)
								if (Chess.getInstance().getTiles()[this.tile.getRow()][adaptiveDirection]
										.getPiece() == null)
									pawnCheck = true;
			}
		if (pawnCheck)
			// Piece check
			// If this Piece's King isn't attacked
			if (!getKing().isInDanger()) {
				// If this Piece's King won't be attacked
				if (getKing().getTile().isEnemyPotentiallyProtected(white)) {
					return tryMove(moveTo);
				} else
					return true;
				// If this Piece's King is in check this Piece may move to protect it
			} else {
				// If this Piece's King is in check by only one enemy Piece
				if (getKing().getTile().getEnemyProtection(white).size() == 1)
					// If this Piece can attack the attacking one
					if (getKing().getTile().getEnemyProtection(white).get(0).getTile().equals(moveTo))
						return tryMove(moveTo);
					else
						// If this Piece can move between its King and the Piece that attacks it
						for (Tile t : getKing().getTile().getEnemyProtection(white).get(0).getProtectedTiles())
							if (moveTo.equals(t))
								return tryMove(t);
			}
		return false;
	}

	public ArrayList<Tile> getMovingTiles() {
		computeAdaptiveDirection();
		ArrayList<Tile> list = new ArrayList<>();
		list.addAll(getProtectedTiles());
		list.add(Chess.getInstance().getTiles()[tile.getRow()][adaptiveDirection]);
		if (!moved)
			list.add(Chess.getInstance().getTiles()[tile.getRow()][white ? adaptiveDirection - 1
					: adaptiveDirection + 1]);
		return list;
	}

	@Override
	public ArrayList<Tile> getProtectedTiles() {
		// Remember that negative Y is on top
		computeAdaptiveDirection();
		ArrayList<Tile> list = new ArrayList<>();
		if (adaptiveDirection < 0 || adaptiveDirection > 7)
			return list;
		Tile[][] tiles = Chess.getInstance().getTiles();
		Tile tile = null;
		int row;
		// Checks the forward left and right Tiles
		for (int x = -1; x < 3; x += 2) {
			row = this.tile.getRow() + x;
			// Check if outside the board
			if (row < 0 || row > 7)
				continue;
			tile = tiles[row][adaptiveDirection];
			list.add(tile);
		}
		return list;
	}

	/**
	 * The Pawn implementation of this method makes it call
	 * {@link #getProtectedTiles()} instead.
	 */
	@Override
	public ArrayList<Tile> getPotentiallyProtectedTiles() {
		return getProtectedTiles();
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
		return "Pawn [moved=" + moved + ", " + super.toString() + "]";
	}

}
