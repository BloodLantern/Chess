package com.bloodLantern.chess;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import com.bloodLantern.chess.pieces.Bishop;
import com.bloodLantern.chess.pieces.IfNotMoved;
import com.bloodLantern.chess.pieces.King;
import com.bloodLantern.chess.pieces.Knight;
import com.bloodLantern.chess.pieces.Pawn;
import com.bloodLantern.chess.pieces.Piece;
import com.bloodLantern.chess.pieces.Queen;
import com.bloodLantern.chess.pieces.Rook;

/**
 * Main class of the chess game.
 *
 * @author BloodLantern
 */
public class Chess {

	/**
	 * Starting point of the program.
	 *
	 * @param args Additional String arguments.
	 */
	public static void main(String[] args) {
		// Default board position
		String board = "rnbqkbnr/pppppppp/////PPPPPPPP/RNBQKBNR w KQkq - 0 1";
		int runTestMoves = 0;
		for (String arg : args) {
			if (arg.equalsIgnoreCase("devmode"))
				devMode = true;
			else if (arg.equalsIgnoreCase("showpptiles"))
				showPotentiallyProtectedTiles = true;
			else if (arg.equalsIgnoreCase("notimer"))
				timer = false;
			else if (arg.startsWith("board="))
				board = arg.substring(6);
			else if (arg.startsWith("testMoves="))
				runTestMoves = Integer.valueOf(arg.substring(10));
		}
		new Chess(board, runTestMoves).start();
	}

	/**
	 * Whether to play against an AI or a player.
	 */
	private static boolean multiplayer = false;
	/**
	 * Whether the developper mode is on. If this is true then game turns will be
	 * deactivated.
	 */
	private static boolean devMode = false;
	/**
	 * Should the renderer show the potentially protected tiles in orange as
	 * declared in {@link Piece#getPotentiallyProtectedTiles()} ?
	 */
	private static boolean showPotentiallyProtectedTiles = false;
	/**
	 * Should the timer decrease over time ?
	 */
	public static boolean timer = true;

	/**
	 * Last timer update time (in ms).
	 */
	private long lastTimerUpdate;
	/**
	 * False if the game has ended.
	 */
	private boolean playing = false;
	/**
	 * Which player is the winner ? True for player 1 (white) and false for player 2
	 * (black). This field musn't be used if {@link #playing} is still true.
	 */
	private boolean winner = true;
	/**
	 * When a game ends, if this is true, there is a draw.
	 */
	private boolean draw = false;
	/**
	 * Which player should play now ? True for player 1 (white) and false for player
	 * 2 (black).
	 */
	private boolean turn = true;
	/**
	 * The play time left for the white player (in ms).
	 */
	private int timeLeftWhite = DEFAULT_TIME;
	/**
	 * The play time left for the black player (in ms).
	 */
	private int timeLeftBlack = DEFAULT_TIME;
	/**
	 * An Array of Tiles representing a Chess board.
	 */
	private final Tile[][] tiles = new Tile[8][8];
	/**
	 * The default rendering size of each Tile (in px).
	 */
	public static final int TILES_SIZE = 75;
	/**
	 * The default play time for each player (in ms).
	 */
	private static final int DEFAULT_TIME = 600000;
	/**
	 * The size of the game frame.
	 */
	private static final int FRAME_SIZE = 600;
	/**
	 * The width of the information frame.
	 */
	private static final int INFO_FRAME_WIDTH = 450;
	/**
	 * The height of the information frame.
	 */
	private static final int INFO_FRAME_HEIGHT = 200;
	/**
	 * The last created instance of the Chess class.
	 */
	private static Chess instance = null;
	/**
	 * The taken Pieces.
	 */
	public CapturedPieces capturedPieces = new CapturedPieces();
	/**
	 * The board Image.
	 */
	private static final Image BOARD = getBoardImage();
	/**
	 * Information frame.
	 */
	JFrame infoFrame = null;
	/**
	 * Main frame.
	 */
	public JPanel mainPanel = null;
	/**
	 * En passant position possibility. If this field is null then there isn't any
	 * possible en passant target.
	 */
	private Tile enPassant = null;

	/**
	 * The number of halfmoves since the last capture or pawn advance, used for the
	 * fifty-move rule.
	 */
	private float halfmoveClock = 0;
	/**
	 * The number of the full move. It starts at 1, and is incremented after Black's
	 * move.
	 */
	private int fullmoveNumber = 1;
	/**
	 * The AI used to play against the player.
	 */
	public AI ai;

	/**
	 * Used to get the board Image. This must be made in this method because of the
	 * exception thrown by this statement.
	 *
	 * @return The Image of the board.
	 */
	private static Image getBoardImage() {
		try {
			return ImageIO.read(new FileInputStream("board.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private class Board {
		private int possibleMoves;
		private int captures;
		private int castles;
		private int promotions;
		private int checks;
		private int checkmates;
		private int draws;

		/**
		 * @param possibleMoves
		 * @param captures
		 * @param castles
		 * @param promotions
		 * @param checks
		 * @param checkmates
		 * @param draws
		 */
		private Board(int possibleMoves, int captures, int castles, int promotions, int checks, int checkmates,
				int draws) {
			this.possibleMoves = possibleMoves;
			this.captures = captures;
			this.castles = castles;
			this.promotions = promotions;
			this.checks = checks;
			this.checkmates = checkmates;
			this.draws = draws;
		}

		private Board() {

		}

		private Board(int moves) {
			possibleMoves = moves;
		}

		private void add(Board board) {
			possibleMoves += board.possibleMoves;
			captures += board.captures;
			castles += board.castles;
			promotions += board.promotions;
			checks += board.checks;
			checkmates += board.checkmates;
			;
			draws += board.draws;
		}

		/**
		 * 
		 */
		@Override
		public String toString() {
			return "Board[possibleMoves=" + possibleMoves + ", captures=" + captures + ", castles=" + castles
					+ ", promotions=" + promotions + ", checks=" + checks + ", checkmates=" + checkmates + ", draws="
					+ draws + "]";
		}
	}

	/**
	 * Constructs a Chess object.
	 *
	 * @param fen          The FEN board to setup.
	 * @param runTestMoves If 0, doesn't run the movement tests. If greater than 0,
	 *                     run that test with that depth.
	 */
	public Chess(String fen, int runTestMoves) {
		if (!multiplayer)
			ai = new AI(false);
		instance = this;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		switch (JOptionPane.showConfirmDialog(null, "Play against an AI ?", "Select an option",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
		case JOptionPane.YES_OPTION -> multiplayer = false;
		case JOptionPane.NO_OPTION -> multiplayer = true;
		case JOptionPane.CANCEL_OPTION -> System.exit(0);
		}
		setupTiles();
		setupPiecesFromFEN(fen);
		setupFrames();
		computeTileProtection();
		if (runTestMoves > 0)
			moveGenerationTest(runTestMoves, runTestMoves);
	}

	Board moveGenerationTest(int depth, int startingDepth) {
		if (depth == 0)
			return new Board(1);
		long time = System.currentTimeMillis();

		List<Move> moves = getPossibleMoves(startingDepth == depth ? turn : depth % 2 == startingDepth % 2);
		Board board = new Board();

		for (Move move : moves) {
			move.makeMove(false);
			if (depth == 1) {
				if (move.isCapture())
					board.captures++;
				if (move.isCastle())
					board.castles++;
				if (move.isPromotion())
					board.promotions++;
				if (move.isCheck())
					board.checks++;
				if (move.isCheckmate())
					board.checkmates++;
				if (move.isDraw())
					board.draws++;
			}
			board.add(moveGenerationTest(depth - 1, startingDepth));
			move.unmakeMove(false);
		}

		System.out.println("Depth: " + depth + " ply  Result: " + board + "  Time: "
				+ (System.currentTimeMillis() - time) + " milliseconds");
		return board;
	}

	public List<Move> getPossibleMoves(boolean white) {
		List<Move> moves = new ArrayList<>();
		for (int x = 0; x < 8; x++)
			for (int y = 0; y < 8; y++)
				if (tiles[x][y].getPiece() != null)
					if (tiles[x][y].getPiece().isWhite() == white)
						moves.addAll(getPossibleMoves(tiles[x][y].getPiece()));
		return moves;
	}

	public List<Move> getPossibleMoves(Piece piece) {
		List<Move> moves = new ArrayList<>();
		if (piece instanceof Pawn pawn) {
			for (Tile t : pawn.getMovingTiles())
				if (pawn.checkMove(t))
					moves.add(new Move(piece, t));
			return moves;
		} else if (piece instanceof King king) {
			// Castling check
			for (int i = 2; i < 6; i += 4) {
				Tile tile = tiles[i][king.getTile().getLine()];
				if (tile.getLine() == piece.getTile().getLine())
					if (tile.getRow() + 2 == piece.getTile().getRow()
							|| tile.getRow() - 2 == piece.getTile().getRow()) {
						Piece piece1 = null;
						for (int i2 = 0; i2 < 14; i2 += 7) {
							piece1 = tiles[i2][king.getTile().getLine()].getPiece();
							if (piece1 instanceof Rook rook)
								if (king.checkCastle(rook)) {
									moves.add(new Move(king, tile));
								}
						}
					}
			}
		}
		for (Tile t : piece.getProtectedTiles())
			if (piece.checkMove(t))
				moves.add(new Move(piece, t));
		return moves;
	}

	/**
	 * Setups the Tile Array representing the chess board.
	 */
	private void setupTiles() {
		for (int i = 0; i < 8; i++)
			for (int ii = 0; ii < 8; ii++)
				tiles[i][ii] = new Tile(i, ii);
	}

	/**
	 * Setups the Pieces on the board.
	 *
	 * @param fen The FEN board to setup.
	 */
	private void setupPiecesFromFEN(String fen) {
		fen = fen.replaceAll("_", " ");
		for (Tile[] tiles : tiles)
			for (Tile tile : tiles)
				tile.setPiece(null, false);
		// Between 0 and 7 included
		int row = 0;
		// Between 0 and 7 included
		int line = 0;
		// Value between 1 and 6 included
		int field = 1;
		String halfmoveClock = "", fullmoveNumber = "";
		// Read the board String
		for (char c : fen.toCharArray()) {
			if (c == ' ')
				// We use an underscore instead of a space bacause the main method args are
				// already separated with spaces
				field++;
			else
				switch (field) {
				case 1:
					if (Character.getNumericValue(c) > 0 && Character.getNumericValue(c) <= 8)
						// Change row
						row += Character.getNumericValue(c);
					else
						switch (c) {
						case '/':
							// Change line
							row = 0;
							line++;
							break;
						case 'K':
							new King(true, tiles[row][line]).setMoved(true);
							row++;
							break;
						case 'k':
							new King(false, tiles[row][line]).setMoved(true);
							row++;
							break;
						case 'Q':
							new Queen(true, tiles[row][line]);
							row++;
							break;
						case 'q':
							new Queen(false, tiles[row][line]);
							row++;
							break;
						case 'B':
							new Bishop(true, tiles[row][line]);
							row++;
							break;
						case 'b':
							new Bishop(false, tiles[row][line]);
							row++;
							break;
						case 'N':
							new Knight(true, tiles[row][line]);
							row++;
							break;
						case 'n':
							new Knight(false, tiles[row][line]);
							row++;
							break;
						case 'R':
							new Rook(true, tiles[row][line]).setMoved(true);
							row++;
							break;
						case 'r':
							new Rook(false, tiles[row][line]).setMoved(true);
							row++;
							break;
						case 'P':
							new Pawn(true, tiles[row][line]).setMoved(true);
							row++;
							break;
						case 'p':
							new Pawn(false, tiles[row][line]).setMoved(true);
							row++;
							break;
						default:
							System.err.println("Unknown FEN character: '" + c + "'");
						}
					break;
				case 2:
					if (c == 'w')
						turn = true;
					else if (c == 'b')
						turn = false;
					break;
				case 3:
					if (c == '-') {
						// No castling possible
						for (Tile[] tiles : tiles)
							for (Tile tile : tiles)
								if (tile.getPiece() instanceof IfNotMoved inm && !(tile.getPiece() instanceof Pawn))
									inm.setMoved(true);
					} else {
						if (Character.isUpperCase(c)) {
							((IfNotMoved) tiles[4][7].getPiece()).setMoved(false);
							if (c == 'K')
								((IfNotMoved) tiles[7][7].getPiece()).setMoved(false);
							else if (c == 'Q')
								((IfNotMoved) tiles[0][7].getPiece()).setMoved(false);
						} else {
							((IfNotMoved) tiles[4][0].getPiece()).setMoved(false);
							if (c == 'k')
								((IfNotMoved) tiles[7][0].getPiece()).setMoved(false);
							else if (c == 'q')
								((IfNotMoved) tiles[0][0].getPiece()).setMoved(false);
						}
					}
					break;
				case 4:
					if (c == '-')
						enPassant = null;
					else {
						if (enPassant == null)
							enPassant = tiles[0][0];
						switch (c) {
						case 'a' -> enPassant = tiles[0][enPassant.getLine()];
						case 'b' -> enPassant = tiles[1][enPassant.getLine()];
						case 'c' -> enPassant = tiles[2][enPassant.getLine()];
						case 'd' -> enPassant = tiles[3][enPassant.getLine()];
						case 'e' -> enPassant = tiles[4][enPassant.getLine()];
						case 'f' -> enPassant = tiles[5][enPassant.getLine()];
						case 'g' -> enPassant = tiles[6][enPassant.getLine()];
						case 'h' -> enPassant = tiles[7][enPassant.getLine()];
						}
						switch (Character.getNumericValue(c)) {
						case 0 -> enPassant = tiles[enPassant.getRow()][0];
						case 1 -> enPassant = tiles[enPassant.getRow()][1];
						case 2 -> enPassant = tiles[enPassant.getRow()][2];
						case 3 -> enPassant = tiles[enPassant.getRow()][3];
						case 4 -> enPassant = tiles[enPassant.getRow()][4];
						case 5 -> enPassant = tiles[enPassant.getRow()][5];
						case 6 -> enPassant = tiles[enPassant.getRow()][6];
						case 7 -> enPassant = tiles[enPassant.getRow()][7];
						}
					}
					break;
				case 5:
					halfmoveClock += c;
					break;
				case 6:
					fullmoveNumber += c;
					break;
				default:
					System.err.println("A FEN field must be within 1 and 6 included. Current field: " + field);
				}
			if (!halfmoveClock.equals(""))
				this.halfmoveClock = Integer.parseInt(halfmoveClock);
			if (!fullmoveNumber.equals(""))
				this.fullmoveNumber = Integer.parseInt(fullmoveNumber);
		}
		// Set moved value of each Pawn
		for (Tile[] tiles : tiles)
			for (Tile tile : tiles)
				if (tile.getPiece() instanceof Pawn pawn)
					if (pawn.isWhite()) {
						if (tile.getLine() == 6)
							pawn.setMoved(false);
					} else {
						if (tile.getLine() == 1)
							pawn.setMoved(false);
					}
		if (mainPanel != null) {
			mainPanel.repaint();
		}
	}

	/**
	 * Converts the current board in a FEN String euivalent. May be used for saving.
	 *
	 * @return A FEN representation of the current board.
	 */
	private String generateFEN() {
		String output = "";

		// Field 1
		int space;
		for (int y = 0; y < 8; y++) {
			space = 0;
			for (int x = 0; x < 8; x++)
				if (tiles[x][y].getPiece() != null) {
					if (space > 0)
						output += space;
					output += tiles[x][y].getPiece().getFENValue();
					space = 0;
				} else
					space++;
			if (y < 7)
				output += "/";
		}
		output += " ";

		// Field 2
		if (turn)
			output += "w ";
		else
			output += "b ";

		// Field 3
		for (int y = 0; y < 14; y += 7)
			if (tiles[4][y].getPiece() instanceof King king)
				if (!king.isMoved()) {
					if (tiles[0][y].getPiece() instanceof Rook leftRook)
						if (!leftRook.isMoved())
							if (leftRook.isWhite())
								output += "Q";
							else
								output += "q";
					if (tiles[7][y].getPiece() instanceof Rook rightRook)
						if (!rightRook.isMoved())
							if (rightRook.isWhite())
								output += "K";
							else
								output += "k";
				}
		if (output.endsWith(" "))
			output += "-";
		output += " ";

		// Field 4
		if (enPassant == null)
			output += "- ";
		else {
			switch (enPassant.getRow()) {
			case 0 -> output += 'a';
			case 1 -> output += 'b';
			case 2 -> output += 'c';
			case 3 -> output += 'd';
			case 4 -> output += 'e';
			case 5 -> output += 'f';
			case 6 -> output += 'g';
			case 7 -> output += 'h';
			}
			output += enPassant.getLine() + " ";
		}

		// Fields 5 and 6
		output += (int) Math.floor(halfmoveClock) + " " + fullmoveNumber;
		return output;
	}

	private void setupFrames() {
		// Shows the game
		JFrame frame = new JFrame();
		final class BoardRenderer extends JPanel implements MouseListener, MouseMotionListener {

			/**
			 *
			 */
			private static final long serialVersionUID = -6945335859602682282L;

			private Piece moving;
			private Piece selected = null;

			private BoardRenderer() {
				addMouseListener(this);
				addMouseMotionListener(this);
			}

			@Override
			public void paintComponent(Graphics g) {
				// Clears last frame
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, FRAME_SIZE, FRAME_SIZE);
				// Draw the board
				g.drawImage(BOARD, 0, 0, null);
				// Draw the moving/attacking/castling overlays
				ArrayList<Piece> pieces = new ArrayList<>();
				for (Tile[] tiless : tiles)
					for (Tile tile : tiless) {
						if (selected != null)
							if (selected.checkMove(tile)) {
								if (tile.getPiece() == null) {
									if (tile.equals(enPassant) && selected instanceof Pawn)
										g.setColor(new Color(255, 0, 0, 150));
									else
										g.setColor(new Color(255, 255, 0, 150));
									g.fillRect(tile.getRow() * TILES_SIZE, tile.getLine() * TILES_SIZE, TILES_SIZE,
											TILES_SIZE);
								} else if (tile.getPiece().isEnemy(selected.isWhite())) {
									g.setColor(new Color(255, 0, 0, 150));
									g.fillRect(tile.getRow() * TILES_SIZE, tile.getLine() * TILES_SIZE, TILES_SIZE,
											TILES_SIZE);
								}
							} else {
								if (showPotentiallyProtectedTiles)
									if (selected.getPotentiallyProtectedTiles().contains(tile)) {
										g.setColor(new Color(255, 165, 0, 150));
										g.fillRect(tile.getRow() * TILES_SIZE, tile.getLine() * TILES_SIZE, TILES_SIZE,
												TILES_SIZE);
									}
								// Castling overlay
								if (selected instanceof King king) {
									// If on the same line
									if (tile.getLine() == selected.getTile().getLine())
										// IF one of the castling Tiles
										if (tile.getRow() + 2 == selected.getTile().getRow()
												|| tile.getRow() - 2 == selected.getTile().getRow()) {
											// Checking left Rook
											Piece rook = tiles[0][selected.getTile().getLine()].getPiece();
											if (rook instanceof Rook rook_) {
												if (king.checkCastle(rook_)) {
													g.setColor(new Color(220, 0, 255, 100));
													g.fillRect((selected.getTile().getRow() - 2) * TILES_SIZE,
															selected.getTile().getLine() * TILES_SIZE, TILES_SIZE,
															TILES_SIZE);
												}
											}
											// Checking right Rook
											rook = tiles[7][selected.getTile().getLine()].getPiece();
											if (rook instanceof Rook rook_) {
												if (king.checkCastle(rook_)) {
													g.setColor(new Color(220, 0, 255, 100));
													g.fillRect((selected.getTile().getRow() + 2) * TILES_SIZE,
															selected.getTile().getLine() * TILES_SIZE, TILES_SIZE,
															TILES_SIZE);
												}
											}
										}
								}
							}
						if (tile.getPiece() != null)
							pieces.add(tile.getPiece());
					}
				// Draw the Pieces
				for (Piece p : pieces)
					g.drawImage(p.getTexture(), p.getX(), p.getY(), TILES_SIZE, TILES_SIZE, null);
				// Print winner's name if the game ended
				if (!playing) {
					g.setFont(new Font("Arial", Font.BOLD, 30));
					g.setColor(Color.DARK_GRAY);
					if (!draw)
						g.drawString(winner ? "White wins!" : "Black wins!", 218, 310);
					else
						g.drawString("Draw!", 255, 310);
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (playing)
					if (e.getButton() == MouseEvent.BUTTON1) {
						Tile t = getTileAtPosition(e.getPoint());
						if (t != null)
							if (t.getPiece() != null) {
								// Only select and move the Piece if it is the right turn
								if (isTurn(t.getPiece().isWhite())) {
									selected = moving = t.getPiece();
									return;
								}
							} else {
								selected = null;
								repaint();
							}
						moving = null;
					}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (playing)
					if (moving != null) {
						Tile tile = getTileAtPosition(e.getPoint());
						if (tile != null)
							if (moving.checkMove(tile)) {
								// Moving
								new Move(moving, tile).finalizeMove();
								selected = null;
								moving = null;
								repaint();
								switchTurn();
								checkWin(true);
								if (!multiplayer && playing) {
									ai.chooseMove().finalizeMove();
									switchTurn();
									checkWin(true);
								}
							} else {
								// Castling
								if (moving instanceof King king)
									if (tile.getLine() == moving.getTile().getLine())
										if (tile.getRow() + 2 == moving.getTile().getRow()
												|| tile.getRow() - 2 == moving.getTile().getRow()) {
											Piece piece = null;
											// Know which side to castle
											if (e.getX() < moving.getTile().getRow() * TILES_SIZE)
												piece = tiles[0][tile.getLine()].getPiece();
											else
												piece = tiles[7][tile.getLine()].getPiece();
											if (piece instanceof Rook rook)
												if (king.checkCastle(rook)) {
													new Move(rook, tiles[tile.getRow()
															+ (king.getTile().getRow() - tile.getRow()) / 2][tile
																	.getLine()]).finalizeMove();
													new Move(king, tile).finalizeMove();
													king.setMoved(true);
													rook.setMoved(true);
													moving = null;
													selected = null;
													repaint();
													switchTurn();
													checkWin(true);
													if (!multiplayer && playing) {
														ai.chooseMove().finalizeMove();
														switchTurn();
														checkWin(true);
													}
													return;
												}
										}
								// Not moving or moving where the Piece can't
								moving.replaceToTilePosition();
								moving = null;
								repaint();
							}
						else {
							// If outside the board
							moving.replaceToTilePosition();
							moving = null;
							repaint();
						}
					}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (playing)
					if (moving != null) {
						moving.setX(e.getX() - TILES_SIZE / 2);
						moving.setY(e.getY() - TILES_SIZE / 2);
						repaint();
					}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		}
		frame.setResizable(false);
		frame.setSize(FRAME_SIZE + 16, FRAME_SIZE + 39);
		frame.setLocationRelativeTo(null);
		frame.add(mainPanel = new BoardRenderer());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setTitle("Chess");
		frame.setVisible(true);
		// Shows the taken Pieces as well as the timer and save/load buttons
		infoFrame = new JFrame();
		infoFrame.setResizable(false);
		infoFrame.setBounds(1260, 500, INFO_FRAME_WIDTH, INFO_FRAME_HEIGHT);
		JPanel container = new JPanel();
		container.setLayout(null);
		JPanel panel = new JPanel() {
			/**
			 *
			 */
			private static final long serialVersionUID = -7002691943991482626L;

			/**
			 *
			 */
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, INFO_FRAME_WIDTH, INFO_FRAME_HEIGHT);
				g.setColor(Color.BLACK);
				g.setFont(new Font("Arial", Font.PLAIN, 20));
				// Draw white pieces and count
				g.drawImage(Piece.TEXTURE_WHITE_PAWN, 0, 0, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getWhite().pawns), 25, 70);
				g.drawImage(Piece.TEXTURE_WHITE_ROOK, 45, 0, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getWhite().rooks), 70, 70);
				g.drawImage(Piece.TEXTURE_WHITE_KNIGHT, 90, 0, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getWhite().knights), 115, 70);
				g.drawImage(Piece.TEXTURE_WHITE_BISHOP, 135, 0, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getWhite().bishops), 160, 70);
				g.drawImage(Piece.TEXTURE_WHITE_QUEEN, 180, 0, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getWhite().queens), 205, 70);

				// Draw black pieces and count
				g.drawImage(Piece.TEXTURE_BLACK_PAWN, 0, 100, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getBlack().pawns), 25, 105);
				g.drawImage(Piece.TEXTURE_BLACK_ROOK, 45, 100, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getBlack().rooks), 70, 105);
				g.drawImage(Piece.TEXTURE_BLACK_KNIGHT, 90, 100, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getBlack().knights), 115, 105);
				g.drawImage(Piece.TEXTURE_BLACK_BISHOP, 135, 100, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getBlack().bishops), 160, 105);
				g.drawImage(Piece.TEXTURE_BLACK_QUEEN, 180, 100, 60, 60, null);
				g.drawString(String.valueOf(capturedPieces.getBlack().queens), 205, 105);

				// Draw the timer
				g.setFont(new Font("Arial", Font.PLAIN, 30));
				g.drawString(getTimer(true), 240, 40);
				g.drawString(getTimer(false), 240, 135);

				// Draw the turn count
				g.drawString("Turn " + fullmoveNumber, 240, 90);
			}
		};
		panel.setBounds(0, 0, 350, INFO_FRAME_HEIGHT);
		container.add(panel);
		JButton button = new JButton("Save");
		button.addActionListener((ActionEvent event) -> {
			timer = false;
			JFrame f = new JFrame("The current game FEN");
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.setSize(505, 100);
			f.setLayout(null);
			f.setResizable(false);
			f.setLocationRelativeTo(null);
			JFormattedTextField textField = new JFormattedTextField(generateFEN());
			textField.setBounds(10, 15, 470, 35);
			textField.setEditable(false);
			f.add(textField);
			f.addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {
				}

				@Override
				public void windowClosing(WindowEvent e) {
				}

				@Override
				public void windowClosed(WindowEvent e) {
					timer = true;
				}

				@Override
				public void windowIconified(WindowEvent e) {
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
				}

				@Override
				public void windowActivated(WindowEvent e) {
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
				}
			});
			f.setAlwaysOnTop(true);
			f.setVisible(true);
		});
		button.setBounds(350, 0, 84, INFO_FRAME_HEIGHT / 2 - 16);
		container.add(button);
		button = new JButton("Load");
		button.addActionListener((ActionEvent event) -> {
			timer = false;
			JFrame f = new JFrame("Enter an FEN to load");
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.setSize(605, 100);
			f.setLayout(null);
			f.setResizable(false);
			f.setLocationRelativeTo(null);
			JFormattedTextField textField = new JFormattedTextField();
			textField.setBounds(10, 15, 470, 35);
			f.add(textField);
			JButton confirmButton = new JButton("Confirm");
			confirmButton.setBounds(480, 15, 100, 35);
			confirmButton.addActionListener((ActionEvent e) -> {
				f.dispose();
				setupPiecesFromFEN(textField.getText());
				computeTileProtection();
			});
			f.add(confirmButton);
			f.addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {
				}

				@Override
				public void windowClosing(WindowEvent e) {
				}

				@Override
				public void windowClosed(WindowEvent e) {
					timer = true;
				}

				@Override
				public void windowIconified(WindowEvent e) {
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
				}

				@Override
				public void windowActivated(WindowEvent e) {
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
				}
			});
			f.setAlwaysOnTop(true);
			f.setVisible(true);
		});
		button.setBounds(350, INFO_FRAME_HEIGHT / 2 - 16, 84, INFO_FRAME_HEIGHT / 2 - 16);
		container.add(button);
		infoFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		infoFrame.setTitle("Informations");
		infoFrame.add(container);
		infoFrame.setVisible(true);
		frame.toFront();
	}

	/**
	 * 
	 */
	boolean checkWin(boolean stopGame) {
		boolean result = true;
		for (Tile[] tiles : tiles)
			for (Tile t : tiles) // CA MET PAS TRUE JCOMPRENDS PAS WTF
				if (t.getPiece() != null)
					if (turn == t.getPiece().isWhite())
						for (Tile[] tiles2 : Chess.this.tiles)
							for (Tile t2 : tiles2)
								if (t.getPiece().checkMove(t2))
									result = false;
		if (result) {
			for (Tile[] tiles : tiles)
				for (Tile t : tiles)
					if (t.getPiece() != null)
						if (turn == t.getPiece().isWhite())
							if (!t.getPiece().getKing().isInDanger())
								draw = true;
			winner = !turn;
			playing = !stopGame;
			return true;
		}
		return false;
	}

	/**
	 * Gets a String representation of {@code player}'s timer as said in
	 * {@link #turn} for its meaning.
	 *
	 * @param player The player timer to get.
	 * @return A String representation of the type {@code MM:SS.MS}, {@code M}
	 *         standing for minutes, {@code S} for seconds and {@code MS} for
	 *         milliseconds of {@code player}'s timer.
	 */
	private String getTimer(boolean player) {
		String result = "";
		result += player ? (int) Math.floor(timeLeftWhite / 60000) : (int) Math.floor(timeLeftBlack / 60000);
		result += ":";
		final int seconds = player ? (int) Math.floor((timeLeftWhite % 60000) / 1000)
				: (int) Math.floor((timeLeftBlack % 60000) / 1000);
		result += seconds < 10 ? "0" + seconds : seconds;
		result += "s";
		return result;
	}

	/**
	 * Tries to get the Tile positioned at the Point {@code p}. To do this, this
	 * method snaps {@code p}'s position to get the closest Tile on the board.
	 *
	 * @param p The Point from which the method should get the closest Tile on the
	 *          board.
	 * @return The closest Tile on the board from the Point {@code p}.
	 */
	public Tile getTileAtPosition(final Point p) {
		if (p.getX() < 0 || p.getY() < 0 || p.getX() > FRAME_SIZE || p.getY() > FRAME_SIZE)
			return null;
		return tiles[(int) Math.floor(p.getX() / TILES_SIZE)][(int) Math.floor(p.getY() / TILES_SIZE)];
	}

	/**
	 * Clears every Tile's {@link Tile#getProtecting() protecting list} and then
	 * calls their {@link Piece#refreshTileProtection() refreshTileProtection()}
	 * method.
	 */
	public void computeTileProtection() {
		for (Tile[] tiles : tiles)
			for (Tile tile : tiles) {
				tile.getProtecting().clear();
				tile.getPotentiallyProtecting().clear();
			}
		for (Tile[] tiles : tiles)
			for (Tile tile : tiles)
				if (tile.getPiece() != null)
					tile.getPiece().refreshTileProtection();
	}

	/**
	 * Starts the game by computing the Tile protection and beginning the timer.
	 */
	private void start() {
		computeTileProtection();
		playing = true;
		mainPanel.repaint();
		// Timer
		lastTimerUpdate = System.currentTimeMillis();
		if (timer)
			while (playing) {
				if (!timer) {
					lastTimerUpdate = System.currentTimeMillis();
					continue;
				}
				if (turn)
					// White turn
					timeLeftWhite -= System.currentTimeMillis() - lastTimerUpdate;
				else
					// Black turn
					timeLeftBlack -= System.currentTimeMillis() - lastTimerUpdate;
				lastTimerUpdate = System.currentTimeMillis();
				infoFrame.repaint();
				if (timeLeftWhite <= 0) {
					winner = false;
					playing = false;
					mainPanel.repaint();
				} else if (timeLeftBlack <= 0) {
					winner = true;
					playing = false;
					mainPanel.repaint();
				}
			}
	}

	/**
	 * Return true if {@link #devMode} is true. Else, this method will return true
	 * or false following these conditions:
	 * <ul>
	 * <li>If {@code whiteTurn} is true:
	 * <ul>
	 * <li>If {@code turn} is true, returns true.</li>
	 * <li>If {@code turn} is false, returns false.</li>
	 * </ul>
	 * <li>Else:</li>
	 * <ul>
	 * <li>If {@code turn} is true, returns false.</li>
	 * <li>If {@code turn} is false, returns true.</li>
	 * </ul>
	 * </ul>
	 *
	 * @param whiteTurn Whether to check for the white player turn or the black
	 *                  player turn (True for white and false for black).
	 * @return Following the conditions depicted over.
	 * @see #turn
	 * @see #devMode
	 */
	private boolean isTurn(boolean whiteTurn) {
		if (devMode)
			return true;
		else
			return whiteTurn ? turn : !turn;
	}

	/**
	 * Switches turns. If {@link #turn} was true, it becomes false and vice-versa.
	 */
	private void switchTurn() {
		if (turn)
			turn = false;
		else
			turn = true;
	}

	/**
	 * Returns the current instance of Chess. May be null.
	 *
	 * @return The last constructed instance of Chess. May be null if never
	 *         instantiated.
	 */
	public static Chess getInstance() {
		return instance;
	}

	/**
	 * Getter for the tiles value.
	 *
	 * @return The tiles to get.
	 */
	public Tile[][] getTiles() {
		return tiles;
	}

	/**
	 * Getter for the halfmoveClock value.
	 *
	 * @return The halfmoveClock to get.
	 */
	float getHalfmoveClock() {
		return halfmoveClock;
	}

	/**
	 * Setter for the halfmoveClock value.
	 *
	 * @param halfmoveClock The halfmoveClock to set.
	 */
	void setHalfmoveClock(int halfmoveClock) {
		this.halfmoveClock = halfmoveClock;
	}

	/**
	 * Increments halfmoveClock by 0.5.
	 */
	void incrementHalfmoveClock() {
		halfmoveClock += 0.5f;
	}

	/**
	 * Getter for the fullmoveNumber value.
	 *
	 * @return The fullmoveNumber to get.
	 */
	int getFullmoveNumber() {
		return fullmoveNumber;
	}

	/**
	 * Setter for the fullmoveNumber value.
	 *
	 * @param fullmoveNumber The fullmoveNumber to set.
	 */
	void setFullmoveNumber(int fullmoveNumber) {
		this.fullmoveNumber = fullmoveNumber;
	}

	public boolean getTurn() {
		return turn;
	}

	/**
	 * Getter for the enPassant value.
	 *
	 * @return The enPassant to get.
	 */
	public Tile getEnPassant() {
		return enPassant;
	}

	/**
	 * Setter for the enPassant value.
	 *
	 * @param enPassant The enPassant to set.
	 */
	public void setEnPassant(Tile enPassant) {
		this.enPassant = enPassant;
	}

	/**
	 * Setter for the draw value.
	 *
	 * @param draw The draw to set.
	 */
	public void setDraw(boolean draw) {
		this.draw = draw;
	}

	/**
	 * Setter for the playing value.
	 *
	 * @param playing The playing to set.
	 */
	void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public boolean getWinner() {
		return winner;
	}

	public boolean isDraw() {
		return draw;
	}

	public CapturedPieces getCapturedPieces() {
		return capturedPieces;
	}

}
