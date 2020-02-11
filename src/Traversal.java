import princeton.In;
import java.io.File;

public class Traversal {
	private static String filenameBoard, filenameMoves;
	private static String boardName;
	private static char[][] board;
	
	public static void main(String[] args) {
		//filenameBoard = args[0];
		filenameBoard = "board_14.txt";
		filenameMoves = args[1];
		
		readBoardFile();
		displayBoard();
		
	}
	
	private static void readBoardFile() {
		In readFileBoard = new In(new File(filenameBoard));
		
		boardName = readFileBoard.readLine();
		int rows = readFileBoard.readInt();
		int columns = readFileBoard.readInt();
		
		board = new char[rows][columns];
				
		System.out.printf("\nrows: %d\tcolumns: %d\n\nreading", rows, columns);
		
		for (int r=0; r < rows && readFileBoard.hasNextLine(); r++) {
			for (int c=0; c < columns && readFileBoard.hasNextChar(); c++) {
				char element = readFileBoard.readChar();
				board[r][c] = element;
			}
			System.out.print('.');
			readFileBoard.readLine();
		}
		System.out.println("\nreading complete");
	}
	
	private static void displayBoard() {
		for (int r=0; r < board.length; r++) {
			for (int c=0; c < board[0].length; c++) {
				System.out.print(board[r][c]);
				
				/*
				 * this section here exists to fix the bug where the last character in the first line does not appear
				 * it seems that the issue actually lies in the readBoardFile() method, not this one
				 */
				
				//System.out.printf("\nr:%dc:%d ", r, c);
			}
			System.out.println();
		}
	}
}
