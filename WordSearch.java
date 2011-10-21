package wordSearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Point;

public class WordSearch {

	/**
	 * @param args
	 */
	static int m_size = 5;

	public static void main(String[] args) {
		// Create a game grid
		wordArray wa = new wordArray(m_size);
		
		// Initialize grid with some words
		wa.initialize(1509);
		wa.printGrid();
		
		for (int i = 13; i < 1700; i+=17 ) {
			wa.initialize(i);
		
			wa.printGrid();
		}
		
		for (int i = 0; i <= 2; i++ ){
			wa.initialize("test/test-word-count-" + i + ".txt");
		
			wa.printGrid();
		}
		
		// Some simple testing
		ArrayList<Point> listPoints = new ArrayList<Point>();
		listPoints.add(new Point(0,0));
		listPoints.add(new Point(4,5));
		listPoints.add(new Point(19,20));
		listPoints.add(new Point(0,23));
		listPoints.add(new Point(16,20));
		listPoints.add(new Point(5,12));
		listPoints.add(new Point(22,23));
		listPoints.add(new Point(7,22));
		listPoints.add(new Point(20,4));
		listPoints.add(new Point(10,22));
		listPoints.add(new Point(16,4));
		
		Iterator<Point> itr = listPoints.iterator();
		while (itr.hasNext()){
			Point p = itr.next();
			if (wa.isWord( p.x, p.y )) {
				System.out.println("Word: " + wa.getWord( p.x, p.y ));
			}
		}
	}
}
