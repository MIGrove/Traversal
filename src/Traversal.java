import javax.lang.model.element.NestingKind;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//	NB: As OOP, or more specifically objects, are not allowed in this project, every method has been made static.

public class Traversal {
	private static String filepathBoard, filepathMoves;
	private static String boardName;
	private static String[][] board;
	private static int[] currentPlayerPosition = new int[2];
	private static ArrayList<String> placementQueue = new ArrayList<>();
	private static int turn = 0;
	private static int rows, columns;
	private static String[] moverTypes = new String[] {
			"u", "d", "l", "r",
			"U", "D", "L", "R"
	};
	private static String[][] moverMap;

	//	constants
	private static final int ROW = 0;
	private static final int COLUMN = 1;
	
	public static void main(String[] args) {

		//	format for calling pieces is as follows:	board[row][column]	(row first!)

		/*
		moves:
			'h' --> left move
			'j' --> down move
			'k' --> up move
			'l' --> right move
			'x' --> quit command
		 */

		//filepathBoard = args[0];
		//filepathMoves = args[1];
		filepathBoard = "samples\\board_test.txt";
		filepathMoves = "samples\\moves_test.txt";

		//	initialisation
		readBoard();

		//	post-init.
		//readMoves();
		move('R');
		move('R');
		move('L');
		move('R');
		move('L');

		generateMoverMap();
		getMoverMapOnTurn(3);
	}

	private static void readBoard() {
		try {
			Scanner scanFile = new Scanner(new File(filepathBoard));

			boardName = scanFile.nextLine();
			rows = scanFile.nextInt();
			columns = scanFile.nextInt();
			board = new String[rows][columns];

			scanFile.nextLine();

			System.out.println(boardName);
			System.out.printf("rows: %d\tcolumns: %d\n", rows, columns);

			for(int i = 0; i < rows; i++) {
				String row = scanFile.nextLine();
				Scanner scanRow = new Scanner(row).useDelimiter("");

				for(int j = 0; j < columns; j++) {
					board[i][j] = scanRow.next();
				}
			}
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		displayBoard();
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

	private static void readMoves() {
		try {
			Scanner scanFile = new Scanner(new File(filepathMoves));

			if(scanFile.hasNext()) {
				String line = scanFile.nextLine();

				//	run each turn
				for(int i = 0; i < line.length(); i++) {
					char currentMove = line.charAt(i);

					switch(currentMove) {
						case 'h': move('L'); break;
						case 'l': move('R'); break;
						case 'j': move('D'); break;
						case 'k': move('U'); break;
						case 'x': quit(); break;
						default: invalidMove();
					}
				}
			}
			else {
				System.out.println("No moves in moves file!");
			}
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}
	}

	private static void refreshPlayerPosition() {
		int rowNum = -1, columnNum;

		for(String[] row : board) {
			columnNum = -1;
			rowNum++;

			for(String piece : row) {
				columnNum++;

				if (piece.equalsIgnoreCase("s")) {
					//	set row
					currentPlayerPosition[ROW] = rowNum;
					//	set column
					currentPlayerPosition[COLUMN] = columnNum;
				}
			}
		}
	}

	private static void wrapDestination(int[] destination, boolean playerPiece) {
		if(destination[ROW] < 0) {
			destination[ROW] = board.length - 1;
		}
		else if(destination[ROW] > board.length - 1) {
			destination[ROW] = 0;
		}

		//	prevents player pieces from being able to wrap around horizontally
		if(!playerPiece) {
			if(destination[COLUMN] < 0) {
				destination[COLUMN] = board[0].length - 1;
			}
			else if(destination[COLUMN] > board[0].length - 1) {
				destination[COLUMN] = 0;
			}
		}
	}

	private static void newWrapDestination(int[] destination, boolean playerPiece) {
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

	private static void move(char direction) {
		turn++;

		//	placementQueue item format:		turn;piece;row;column
		for(String s : placementQueue) {
			Scanner scanQueueItem = new Scanner(s).useDelimiter(";");

			int turnToPlace = scanQueueItem.nextInt();
			String piece = scanQueueItem.next();
			int row = scanQueueItem.nextInt();
			int column = scanQueueItem.nextInt();

			//	checking if the queue item is in the turn it is meant to be placed
			if(turnToPlace == turn) {

				System.out.println("next queue item reached");

				board[row][column - 1] = piece;
			}
		}

		//	first refresh player pos.
		refreshPlayerPosition();

		//	creating a string to hold the info. for the movers (so they move after the player)
		String moverInfo = "";

		//	setting destination coordinates
		int[] destinationPos = new int[2];

		switch(direction) {
			case 'L':
				destinationPos[ROW] = currentPlayerPosition[ROW];
				destinationPos[COLUMN] = currentPlayerPosition[COLUMN] - 1;
				moverInfo = "h";
				break;
			case 'R':
				destinationPos[ROW] = currentPlayerPosition[ROW];
				destinationPos[COLUMN] = currentPlayerPosition[COLUMN] + 1;
				moverInfo = "h";
				break;
			case 'D':
				destinationPos[COLUMN] = currentPlayerPosition[COLUMN];
				destinationPos[ROW] = currentPlayerPosition[ROW] + 1;
				moverInfo = "v";
				break;
			case 'U':
				destinationPos[COLUMN] = currentPlayerPosition[COLUMN];
				destinationPos[ROW] = currentPlayerPosition[ROW] - 1;
				moverInfo = "v";
				break;
		}
		//	finding the symbol of the destination
		String destinationSymbol = "";

		try {
			wrapDestination(destinationPos, true);
			destinationSymbol = board[destinationPos[ROW]][destinationPos[COLUMN]];
		}
		catch(ArrayIndexOutOfBoundsException aioobe) {
			invalidMove();
		}

		//	if destination is unoccupied
		switch(destinationSymbol) {
			case ".":	//	empty cell
				swapPiece(currentPlayerPosition, destinationPos);
				break;

			case "t":	//	target piece
			case "T":
				board[destinationPos[ROW]][destinationPos[COLUMN]] = ".";
				swapPiece(currentPlayerPosition, destinationPos);
				gameWin();
				break;

			case "x":	//	wall piece
			case "X":
				board[destinationPos[ROW]][destinationPos[COLUMN]] = ".";
				swapPiece(currentPlayerPosition, destinationPos);
				gameLose();
				break;
		}

		if(!moverInfo.equals("")) {
			shiftMovers(moverInfo);
		}

		displayBoard();
	}

	private static void generateMoverMap() {
		moverMap = new String[board.length][board[0].length];
		try {
			Scanner scanBoard = new Scanner(new File(filepathBoard));

			scanBoard.nextLine();
			scanBoard.nextLine();

			for(int i = 0; i < rows; i++) {
				String row = scanBoard.nextLine();
				Scanner scanRow = new Scanner(row).useDelimiter("");

				for(int j = 0; j < columns; j++) {
					String piece = scanRow.next();

					if(Arrays.asList(moverTypes).contains(piece)) {
						moverMap[i][j] = piece;
					}
					else {
						moverMap[i][j] = ".";
					}
				}
			}
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}
	}

	private static String[][] getMoverMapOnTurn(int requestedTurn) {
		String[][] newMoverMap = new String[rows][columns];
		String[][] mapOnlyMovers = new String[rows][columns];

		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				newMoverMap[i][j] = ".";
				mapOnlyMovers[i][j] = ".";
			}
		}

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
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		System.out.println("vertical moves: " + verticalMoves + "\nhorizontal movers: " + horizontalMoves);

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
			}
		}
		catch(FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		}

		System.out.println("\nmapOnlyMovers:");
		print2DArray(mapOnlyMovers);

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
						newWrapDestination(destination, false);
						break;
					case "D":
						destination[ROW] += verticalMoves;
						newWrapDestination(destination, false);
						break;
					case "L":
						destination[COLUMN] -= horizontalMoves;
						newWrapDestination(destination, false);
						break;
					case "R":
						destination[COLUMN] += horizontalMoves;
						newWrapDestination(destination, false);
						break;
				}
				if(Arrays.asList(moverTypes).contains(piece)) {
					newMoverMap[destination[ROW]][destination[COLUMN]] = piece;
				}
			}
		}

		System.out.println("newMoverMap");
		print2DArray(newMoverMap);

		return null;
	}

	private static void print2DArray(String[][] array) {
		for(String[] row : array) {
			for(String piece : row) {
				System.out.print(piece);
			}
			System.out.println();
		}
	}

	private static void shiftMovers(String playerDirection) {
		//	creating an array to hold the pieces that will be moved
		int[][][] toBeShifted = new int[rows * columns][2][2];
		int arrCounter = 0;
		int rowNum = -1;

		for(String[] row : board) {
			rowNum++;
			int columnNum = -1;

			for(String piece : row) {
				columnNum++;

				int[] origin = {rowNum, columnNum};
				int[] destination = origin.clone();

				//	vertical movers
				if(playerDirection.equalsIgnoreCase("v")) {
					switch(piece.toUpperCase()) {
						case "U":
							destination[ROW]--;
							break;
						case "D":
							destination[ROW]++;
							break;
					}
				}
				//	horizontal movers
				else if(playerDirection.equalsIgnoreCase("h")) {
					switch(piece.toUpperCase()) {
						case "L":
							destination[COLUMN]--;
							break;
						case "R":
							destination[COLUMN]++;
							break;
					}
				}

				if(!Arrays.equals(origin, destination)) {
					wrapDestination(destination, false);

					toBeShifted[arrCounter][0] = origin;
					toBeShifted[arrCounter][1] = destination;

					arrCounter++;
				}
			}
		}

		//	bulk swap all movers
		bulkShift(toBeShifted);
	}

	private static void bulkShift(int[][][] toBeShifted) {
		for(int[][] pair : toBeShifted) {
			swapPiece(pair[0], pair[1]);
		}
	}

	private static void swapPiece(int[] origin, int[] destination) {
		if(!Arrays.equals(origin, destination)) {
			if(getPieceAt(destination).equals(".")) {
				String temp = board[destination[ROW]][destination[COLUMN]];
				board[destination[ROW]][destination[COLUMN]] = board[origin[ROW]][origin[COLUMN]];
				board[origin[ROW]][origin[COLUMN]] = temp;
			}
			else {
				//	check if piece hit is another mover
				if(Arrays.asList(moverTypes).contains(getPieceAt(destination))) {
					System.out.println("hit another mover!");

					//	get direction of piece by subtracting origin from destination
					int[] direction = new int[origin.length];

					for(int i = 0; i < origin.length; i++) {
						direction[i] = destination[i] - origin[i];
					}

					System.out.println(Arrays.toString(direction));
				}
			}
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
