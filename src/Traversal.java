import princeton.In;
import java.io.File;

public class Traversal {
	private static String filenameBoard, filenameMoves;
	private static String boardName;
	private static char[][] board;
	
	public static void main(String[] args) {
		filenameBoard = args[0];
		filenameMoves = args[1];
		
		readBoardFile();
	}
	
	private static void readBoardFile() {
		In readFileBoard = new In(new File(filenameBoard));
		
		boardName = readFileBoard.readLine();
		int rows = readFileBoard.readInt();
		int columns = readFileBoard.readInt();
		
		board = new char[rows][columns];
				
		System.out.printf("\nrows: %d\tcolumns: %d\n\n", rows, columns);
		
		for (int r=0; r < rows && readFileBoard.hasNextLine(); r++) {
			for (int c=0; c < columns && readFileBoard.hasNextChar(); c++) {
				System.out.printf("reading [%d][%d]\n", r, c);
				char element = readFileBoard.readChar();
				board[r][c] = element;
			}
			readFileBoard.readLine();
		}
		System.out.println("reading complete");
	}
}
