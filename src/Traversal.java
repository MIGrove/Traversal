import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//	NB: As OOP, or more specifically objects, are not allowed in this project, every method has been made static.

public class Traversal {
	private static int[] currentPlayerPosition = new int[2];
	private static int turn = 0, totalMoves;
	private static int rows, columns;
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
			".", "t", "T", "x",
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
		while(!gameOver && turn <= totalMoves) {
			String[][] map0 = generateBlankMap();
			String[][] map1 = generateMiscMapOnTurn(turn);
			String[][] map2 = generateMoverMapOnTurn(turn);
			String[][] map3 = generatePlayerMap(turn);

			board = mergeMaps(map0, map1, map2, map3);	//	error when adding map3 (map3 is empty)

			checkForConflicts();
			displayBoard();

			turn++;
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
			moves = new String[totalMoves];

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
	
	private static void displayBoard() {
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
	}

	private static void checkForConflicts() {
		//	will search for overlapping pieces and give suitable responses
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

	private static String[][] generateMiscMapOnTurn(int requestedTurn) {
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

	private static String[][] generateMoverMapOnTurn(int requestedTurn) {
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
						destination[COLUMN] -= horizontalMoves;
						wrapDestination(destination, false);
						break;
					case "R":
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
		System.out.println("You won!");
		quit();
	}

	private static void gameLose() {
		System.out.println("You lost!");
		quit();
	}

	private static void quit() {
		displayBoard();
		System.exit(0);
	}

	private static void invalidMove() {
		System.out.println("Invalid move!");
		quit();
	}
}
