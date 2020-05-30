import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//	NB: As OOP, or more specifically objects, are not allowed in this project, every method has been made static.

public class Traversal {
	private static int turn = 0, quitTurn, totalMoves;
	private static int rows, columns;
	private static int playerOffset = 0;
	private static boolean gameOver = false;
	private static String filepathBoard, filepathMoves;
	private static String boardName;
	private static ArrayList<String> placementQueue = new ArrayList<>();
	private static String[][] board;
	private static String[] moves;
	private static String[] moverTypes = new String[] {
			"u", "d", "l", "r",
			"U", "D", "L", "R"
	};
	private static String[] miscTypes = new String[] {
			"t", "T", "x",
			"X","h", "H", "v",
			"V", "k", "p", "P"
	};

	//	constants
	private static final int ROW = 0;
	private static final int COLUMN = 1;

	//	format for calling pieces is as follows:	board[row][column]	(row first!)

	/*
	moves:
		'h' --> left move
		'j' --> down move
		'k' --> up move
		'l' --> right move
		'x' --> quit command
	 */

	public static void main(String[] args) {
		//filepathBoard = args[0];
		//filepathMoves = args[1];
		filepathBoard = "samples\\board_test.txt";
		filepathMoves = "samples\\moves_test.txt";

		//	initialisation
		initialise();

		//	post-init.
		while(!gameOver && turn <= totalMoves && turn != quitTurn) {
			String[][] map0 = generateBlankMap();
			String[][] map1 = generateMiscMap(turn);
			String[][] map2 = generateMoverMap(turn);
			String[][] map3 = generatePlayerMap(turn);

			board = mergeMaps(map0, map1, map2, map3);

			displayBoardText();
			checkForConflicts(false, map1, map2, map3);

			turn++;

			drawBoard();
		}
	}

	private static void initialise() {
		try {
			Scanner scanFile = new Scanner(new File(filepathBoard));
			boardName = scanFile.nextLine();
			rows = scanFile.nextInt();
			columns = scanFile.nextInt();
			board = new String[rows][columns];
			totalMoves = getTotalMoves();
			moves = new String[rows * columns];
			quitTurn = findQuit();

			try {
				Scanner scanMoves = new Scanner(new File(filepathMoves)).useDelimiter("");
				int i = 0;
				while(scanMoves.hasNext()) {
					moves[i] = scanMoves.next();
					i++;
				}
			}
			catch(FileNotFoundException fnfex) {
				fnfex.printStackTrace();
			}
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}
	}

	private static void drawBoard() {
		double[][][] grid = new double[rows][columns][2];
		double aspectRatio = 0.5625;
		double width = aspectRatio * 0.1;
		double height = 0.1;
		//double xValue = 1d / columns;
		double xValue = (0.5 - (width * columns / 2)) + (width / 2);
		//double yValue = 1 - (height / 2);
		double yValue = (0.5 + (height * rows / 2)) - (height / 2);

		StdDraw.setCanvasSize(1920, 1080);

		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				grid[i][j][0] = xValue + (j * width);
				grid[i][j][1] = yValue - (i * height);
			}
		}

		int i = 0;

		for(double[][] row : grid) {
			int j = 0;
			for(double[] cell : row) {
				StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_e.png", width, height);

				switch(board[i][j]) {
					case "s":
					case "S":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_s.png", width, height);
						break;
					case "x":
					case "X":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_x.png", width, height);
						break;
					case "U":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_uv.png", width, height);
						break;
					case "D":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_dv.png", width, height);
						break;
					case "L":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_lv.png", width, height);
						break;
					case "R":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_rv.png", width, height);
						break;
					case "u":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_uh.png", width, height);
						break;
					case "d":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_dh.png", width, height);
						break;
					case "l":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_lh.png", width, height);
						break;
					case "r":
						StdDraw.picture(cell[0], cell[1], "assets\\images\\tvl_rh.png", width, height);
						break;
				}
				j++;
			}
			i++;
		}

		double[] cornerUL = {grid[0][0][0] - (width / 2), grid[0][0][1] + (height / 2)};
		double[] cornerDL = {grid[0][0][0] - (width / 2), grid[rows - 1][0][1] - (height / 2)};
		double[] cornerDR = {grid[0][columns - 1][0] + (width / 2), grid[rows - 1][0][1] - (height / 2)};
		double[] cornerUR = {grid[0][columns - 1][0] + (width / 2), grid[0][0][1] + (height / 2)};

		StdDraw.line(cornerUL[0], cornerUL[1], cornerDL[0], cornerDL[1]);
		StdDraw.line(cornerDL[0], cornerDL[1], cornerDR[0], cornerDR[1]);
		StdDraw.line(cornerDR[0], cornerDR[1], cornerUR[0], cornerUR[1]);
		StdDraw.line(cornerUR[0], cornerUR[1], cornerUL[0], cornerUL[1]);

		StdDraw.show(0);

		System.out.println(Arrays.deepToString(grid));

	}

	private static int findQuit() {
		try {
			Scanner scanMoves = new Scanner(new File(filepathMoves)).useDelimiter("");
			int currentTurn = 0;

			while(scanMoves.hasNext()) {
				String move = scanMoves.next();

				if(move.equalsIgnoreCase("x")) {
					return (currentTurn + 1);
				}

				currentTurn++;
			}
			scanMoves.close();
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		return -1;
	}

	private static int getTotalMoves() {
		int count = 0;

		try {
			Scanner scanFile = new Scanner(new File(filepathMoves)).useDelimiter("");
			while(scanFile.hasNext()) {
				count++;
				scanFile.next();
			}
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		return count;
	}
	
	private static void displayBoardText() {
		System.out.println("\nTurn " + turn);
		for(String[] row : board) {
			for(String piece : row) {
				System.out.print(piece);
			}
			System.out.println();
		}
		System.out.println();
	}

	private static void wrapDestination(int[] destination, boolean playerPiece) {
		while(destination[ROW] < 0) {
			destination[ROW] += rows;
		}
		while(destination[ROW] > rows - 1) {
			destination[ROW] -= rows;
		}

		if(!playerPiece) {
			while(destination[COLUMN] < 0) {
				destination[COLUMN] += columns;
			}
			while(destination[COLUMN] > columns - 1) {
				destination[COLUMN] -= columns;
			}
		}
		else {
			if(destination[COLUMN] < 0) {
				destination[COLUMN] = 0;
				playerOffset++;
			}
			else if(destination[COLUMN] > columns - 1) {
				destination[COLUMN] = columns - 1;
				playerOffset++;
			}
		}
	}
	//	will search for overlapping pieces and give suitable responses
	private static void checkForConflicts(boolean verbose, String[][]... maps) {
		//	compare board to individual generated maps to see if all unique pieces are common (not including ".")
		for(String[][] map : maps) {
			for(int i = 0; i < rows; i++) {
				for(int j = 0; j < columns; j++) {
					if(!board[i][j].equalsIgnoreCase(".")) {
						if(!map[i][j].equals(board[i][j]) && !map[i][j].equals("?")) {
							/*
								Java doesn't support switch cases with a pair of booleans,
								so I made this hacky work-around. Seems to work.
							 */
							String[] conflictingPieces = {map[i][j], board[i][j]};

							boolean containsPlayer = contains(conflictingPieces, "s", "S");
							boolean containsTarget = contains(conflictingPieces, "t", "T");
							boolean containsWall = contains(conflictingPieces, "x", "X");
							boolean containsMover = contains(conflictingPieces, (Object[]) moverTypes);

							if(containsPlayer) {
								if(containsTarget) {
									gameWin();
								}
								else if(containsWall || containsMover) {
									gameLose();
								}
							}

							if(verbose) {
								System.out.printf("" +
												"conflict found at:\t(%d, %d)\n" +
												"conflicting pieces:\t\"%s\" and \"%s\"\n",
										i, j, map[i][j], board[i][j]);
							}
						}
					}
				}
			}
		}
	}

	private static boolean contains(Object[] array, Object... items) {
		boolean containsItem = false;

		for(Object item : items) {
			containsItem = Arrays.asList(array).contains(item) || containsItem;
		}

		return containsItem;
	}

	//	this method will load the given maps on top of each other in the order given
	private static String[][] mergeMaps(String[][]... maps) {
		String[][] mergedMap = new String[rows][columns];

		for(String[] row : mergedMap) {
			Arrays.fill(row, "?");
		}

		for(String[][] map : maps) {
			for(int i = 0; i < rows; i++) {
				for(int j = 0; j < columns; j++) {
					String piece = map[i][j];

					if(!piece.equals("?")) {
						mergedMap[i][j] = piece;
					}
				}
			}
		}

		return mergedMap;
	}

	private static String[][] generateBlankMap() {
		String[][] blankMap = new String[rows][columns];

		for(int row = 0; row < rows; row++) {
			for(int column = 0; column < columns; column++) {
				blankMap[row][column] = ".";
			}
		}

		return blankMap;
	}

	private static String[][] generateMiscMap(int requestedTurn) {
		String[][] miscMap = new String[rows][columns];

		try {
			Scanner scanBoard = new Scanner(new File(filepathBoard));

			scanBoard.nextLine();
			scanBoard.nextLine();

			for(int i = 0; i < rows; i++) {
				Scanner scanRow = new Scanner(scanBoard.nextLine()).useDelimiter("");

				for(int j = 0; j < columns; j++) {
					String piece = scanRow.next();

					if(Arrays.asList(miscTypes).contains(piece)) {
						miscMap[i][j] = piece;
					}
					else {
						miscMap[i][j] = "?";
					}
				}
				scanRow.close();
			}
			scanBoard.close();
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		return miscMap;
	}

	private static String[][] generateMoverMap(int requestedTurn) {
		String[][] newMoverMap = new String[rows][columns];
		String[][] mapOnlyMovers = new String[rows][columns];

		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				newMoverMap[i][j] = "?";
				mapOnlyMovers[i][j] = "?";
			}
		}

		int verticalMoves = getMovesAtTurn(requestedTurn, "vertical");
		int horizontalMoves = getMovesAtTurn(requestedTurn, "horizontal");

		try {
			Scanner scanBoard = new Scanner(new File(filepathBoard));
			scanBoard.nextLine();
			scanBoard.nextLine();

			for(int row = 0; row < rows; row++) {
				Scanner scanRow = new Scanner(scanBoard.nextLine()).useDelimiter("");

				for(int column = 0; column < columns; column++) {
					String piece = scanRow.next();

					if(Arrays.asList(moverTypes).contains(piece)) {
						mapOnlyMovers[row][column] = piece;
					}
				}
				scanRow.close();
			}
			scanBoard.close();
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		/*
		now that we have the number of moves of either vertical or horizontal (at the requested turn),
		that have happened, we can calculate where the movers will be at the requested turn.
		(this new map will eventually have the movers on it copied to the actual board).
		 */

		for(int row = 0; row < rows; row++) {
			for(int column = 0; column < columns; column++) {
				String piece = mapOnlyMovers[row][column];
				int[] destination = {row, column};

				switch(piece) {
					case "U":
						destination[ROW] -= verticalMoves;
						wrapDestination(destination, false);
						break;
					case "D":
						destination[ROW] += verticalMoves;
						wrapDestination(destination, false);
						break;
					case "L":
						destination[COLUMN] -= verticalMoves;
						wrapDestination(destination, false);
						break;
					case "R":
						destination[COLUMN] += verticalMoves;
						wrapDestination(destination, false);
						break;
					case "u":
						destination[ROW] -= horizontalMoves;
						wrapDestination(destination, false);
						break;
					case "d":
						destination[ROW] += horizontalMoves;
						wrapDestination(destination, false);
						break;
					case "l":
						destination[COLUMN] -= horizontalMoves;
						wrapDestination(destination, false);
						break;
					case "r":
						destination[COLUMN] += horizontalMoves;
						wrapDestination(destination, false);
						break;
				}
				if(Arrays.asList(moverTypes).contains(piece)) {
					newMoverMap[destination[ROW]][destination[COLUMN]] = piece;
				}
			}
		}

		return newMoverMap;
	}

	private static String[][] generatePlayerMap(int requestedTurn) {
		requestedTurn += playerOffset;

		String[][] playerMap = new String[rows][columns];
		int[] startLoc = new int[2];
		String pieceUsed = "!";

		try {
			Scanner scanBoard = new Scanner(new File(filepathBoard));
			scanBoard.nextLine();
			scanBoard.nextLine();

			for(int i = 0 ; i < rows; i++) {
				Scanner scanRow = new Scanner(scanBoard.nextLine()).useDelimiter("");

				for(int j = 0; j < columns; j++) {
					String piece = scanRow.next();

					if(piece.equalsIgnoreCase("s")) {
						pieceUsed = piece;
						startLoc[0] = i;
						startLoc[1] = j;
					}
				}
				scanRow.close();
			}
			scanBoard.close();
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		//	now find current location
		int[] currentLoc = startLoc.clone();
		int counter = 1;

		for(String move : moves) {
			if(counter <= requestedTurn) {
				switch(move) {
					case "h":	//	left
						currentLoc[COLUMN]--;
						break;
					case "l":	//	right
						currentLoc[COLUMN]++;
						break;
					case "j":	//	down
						currentLoc[ROW]++;
						break;
					case "k":	//	up
						currentLoc[ROW]--;
						break;
				}
			}
			counter++;
		}
		//	to fix, add offset that only affects the mapPlayer by adding to its internal turn
		wrapDestination(currentLoc, true);

		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				if(i == currentLoc[ROW] && j == currentLoc[COLUMN]) {
					playerMap[i][j] = pieceUsed;
				}
				else {
					playerMap[i][j] = "?";
				}
			}
		}

		return playerMap;
	}

	private static int getMovesAtTurn(int requestedTurn, String moveType) {
		int verticalMoves = 0, horizontalMoves = 0;

		try {
			Scanner scanMoves = new Scanner(new File(filepathMoves)).useDelimiter("");
			for(int i = 0; i < requestedTurn; i++) {
				char move = scanMoves.next().charAt(0);

				if(move == 'j' || move == 'k') {
					verticalMoves++;
				}
				else if(move == 'h' || move == 'l') {
					horizontalMoves++;
				}
			}
			scanMoves.close();
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		if(moveType.equalsIgnoreCase("horizontal")) {
			horizontalMoves -= playerOffset;
			return horizontalMoves;
		}
		else if(moveType.equalsIgnoreCase("vertical")) {
			return verticalMoves;
		}
		else {
			System.out.println("That is not a move type! Exiting...");
			System.exit(0);
			return -1;
		}
	}

	private static void print2DArray(String[][] array) {
		for(String[] row : array) {
			for(String piece : row) {
				System.out.print(piece);
			}
			System.out.println();
		}
	}

	private static void addToPlacementQueue(int turnToPlace, String pieceToPlace, int[] location) {
		placementQueue.add(turnToPlace + ";" + pieceToPlace + ";" + location[ROW] + ";" + location[COLUMN]);
	}

	private static String getPieceAt(int[] target) {
		try {
			return board[target[0]][target[1]];
		}
		catch(IndexOutOfBoundsException ioobe) {
			System.out.printf("error:\tthat piece (@ %d, %d) does not exist! exiting...\n", target[0], target[1]);
			System.exit(0);
			return null;
		}
	}

	private static String getPieceAt(int row, int column) {
		try {
			return board[row][column];
		}
		catch(IndexOutOfBoundsException ioobe) {
			System.out.printf("error:\tthat piece (@ %d, %d) does not exist! exiting...\n", row, column);
			System.exit(0);
			return null;
		}
	}

	private static void gameWin() {
		quit("You won!");
	}

	private static void gameLose() {
		quit("You lost!");
	}

	private static void quit(String quitMessage) {
		System.out.println(quitMessage);
		System.exit(0);
	}

	private static void invalidMove() {
		quit("Invalid move!");
	}
}
