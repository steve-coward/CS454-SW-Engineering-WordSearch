//
package wordSearch;

import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

public class wordArray {
	private int m_size;
	private int m_numSlots;
	private int m_offset[];
	private char m_grid[];
	private Dictionary m_dictionary;
	
	private static int m_numDirs = 4;
	private static int m_horizontal = 0;
	private static int m_vertical = 1;
	private static int m_dndiag = 2;
	private static int m_updiag = 3;
	private static char m_deadchar = '5'; // anything not alphabetic or regular exp
	
	// Constructor
	//
	// Read dictionary file.
	//
	// Create a square character array, but do not initialize.
	// Character array is bordered with a non-alphabetic character
	// to simplify range detection when extracting words.  This
	// is not externally visibly however - the user of this class only
	// sees a 5x5 array.
	public wordArray(int size) {
		m_size = size;
		m_numSlots = (m_size+2)*(m_size+2);
		
		m_offset = new int [m_numDirs];
		m_offset[m_horizontal] = 1;
		m_offset[m_vertical] = m_size+2;
		m_offset[m_dndiag] = m_size+3;
		m_offset[m_updiag] = -m_size-1;
		m_grid = new char [m_numSlots];
		m_dictionary = new Dictionary();
	}
	
	// For 5x5 grid there are 26^25 combos.
	// The probability of randomly creating a 5 letter word from dictionary of 
	// 16600/3 words is extremely small.  Therefore an intelligent algorithm is
	// required.
	//
	public boolean initialize() {
		int len;
		int dir;
		String word;
		int start;
		String regExp;
		int numWords;
		int numWordsPlaced;
		int skipWords;
		int numTries;
		// Possible starting positions for up/dn diagonal words of each length.
		// It is just simpler this way.
		int[] dd4 = { 8,9,15,16 };
		int[] dd3 = { 8,9,15,16,10,17,22,23,24 };
		int[] dd2 = { 8,9,15,16,10,17,22,23,24,11,18,25,29,30,31,32 };
		int[] ud4 = { 36,29,30,37 };
		int[] ud3 = { 36,29,30,37,22,23,24,31,38 };
		int[] ud2 = { 36,29,30,37,22,23,24,31,38,15,16,17,18,25,32,39 };

		// For testing only, make random stream deterministic and repeatable
		Random rand = new Random( 19580426 );
		//Random rand = new Random(  );
		
		// Fill with .'s
		for (int i=0; i<m_numSlots;i+=m_offset[m_horizontal]) {
			m_grid[i] = '.';
		}
		for (int i=0; i<m_size+2;i+=m_offset[m_horizontal]) {
			m_grid[i] = m_deadchar;
		}
		for (int i=m_numSlots-m_size-2;i<m_numSlots;i++) {
			m_grid[i] = m_deadchar;
		}
		for (int i=0; i<m_numSlots;i+=m_offset[m_vertical]) {
			m_grid[i] = m_deadchar;
		}
		for (int i=m_size+1; i<m_numSlots;i+=m_offset[m_vertical]) {
			m_grid[i] = m_deadchar;
		}
				
		// Pick a target number of words puzzle will contain
		// This will be an upper bound on number of words inserted
		// but actual number of words may be greater due to placing
		// one word near another word or set of words.
		// Test that all combos can generate at least one puzzle
		numWords = rand.nextInt(8) + 5;
		numWords = 12;
		//System.out.println("Placing " + numWords + " words");
		
		numWordsPlaced = 0;
		numTries = 0;
		
		while ((numTries < 100) && (numWordsPlaced < numWords)) {
			numTries++;
			
			// Select a word length
			len = rand.nextInt(m_size-1) + 2;

			// How to randomize word selected that meets requirements?
			skipWords = rand.nextInt(m_dictionary.m_wordCount.get(len)) + 1;

			// Find direction of word
			dir = rand.nextInt(m_numDirs); // hor, ver, dndiag, updiag
			
			// Find starting index of word
			if (dir == m_horizontal) {
				// start = 8+row*size+offset
				start = 8+rand.nextInt(m_size)*(m_size+2) + rand.nextInt(m_size - len + 1);
			}
			else if (dir == m_vertical) {
				// start = 8+column+offset*size
				start = 8+rand.nextInt(m_size) + rand.nextInt(m_size - len + 1)*m_size;
			}
			else if (dir == m_dndiag) {
				switch (len) {
				case 5:
					start = 8;
					break;
				case 4:
					start = dd4[rand.nextInt(dd4.length)];
					break;
				case 3:
					start = dd3[rand.nextInt(dd3.length)];
					break;
				case 2:
				default:
					start = dd2[rand.nextInt(dd2.length)];
					break;
				}
			}
			else {
				switch (len) {
				case 5:
					start = 36;
					break;
				case 4:
					start = ud4[rand.nextInt(ud4.length)];
					break;
				case 3:
					start = ud3[rand.nextInt(ud3.length)];
					break;
				case 2:
				default:
					start = ud2[rand.nextInt(ud2.length)];
					break;
				}
			}

			// Get current state of grid
			regExp = readWord(dir, start, len);
			
			if (regExp.matches("[a-z]+")) {
				// all letters have been assigned
				continue;
			}

			// Select word from dictionary
			word = m_dictionary.findMatch(regExp, skipWords);

			if (word.length() == len) {
				// found match! So place it into grid
				
				// word should not contain m_deadchar
				assert(word.indexOf(m_deadchar) < 0);
				
				numTries = 0;
				for (int i=0; i<word.length();i++) {
					m_grid[start] = word.charAt(i);
					start += m_offset[dir];
				}
				
				numWordsPlaced++;
				//System.out.print("Placing " + word);
				//printGrid();
			}
		}
		
		// Now fill empty spaces randomly
		for (int i=0; i<m_size*m_size;i++) {
			if (m_grid[i] == '.') {
				m_grid[i] = (char) ((rand.nextInt(26)) + 'a');
			}
		}

		//printGrid();
		
		int count = countAllWords();
		System.out.println("Grid contains " + count + " words.\n");
	
		return(true);
	}
	
	private int screen2grid(int p) {
		if (p<5)  return(p+8);
		if (p<10) return(p+10);
		if (p<15) return(p+12);
		if (p<20) return(p+14);
		return(p+16);
	}
	
	// start and end are in 'character' space
	public Boolean isWord(int start, int end) {
		Boolean bIsWord = false;
		
		String word = "";
		
		if ((start < 0) || (start >= m_size*m_size) ||
			(end   < 0) || (end   >= m_size*m_size) ||
			(start == end)) {
			bIsWord = false;
		}
		else if ((end>start) && ((end-start)<5) && ((start/5) == (end/5))) {
			// horizontal
			//System.out.println(readWord(m_horizontal, screen2grid(start), end-start+1));
			word = readWord(m_horizontal, screen2grid(start), end-start+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%5==0)) {
			// vertical
			//System.out.println(readWord(m_vertical, screen2grid(start), (end-start)/5));
			word = readWord(m_vertical, screen2grid(start), (end-start)/5);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%6==0)) {
			// down diagonal
			//System.out.println(readWord(m_dndiag, screen2grid(start), (end-start)/6+1));
			word = readWord(m_dndiag, screen2grid(start), (end-start)/6+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end<start) && ((start-end)%4==0)) {
			// up diagonal
			//System.out.println(readWord(m_updiag, screen2grid(start), (start-end)/4+1));
			word = readWord(m_updiag, screen2grid(start), (start-end)/4+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		
		return(bIsWord);
	}
	// start and end are in 'character' space
	public String getWord(int start, int end) {
		Boolean bIsWord = false;
		String word = "";
		
		if ((start < 0) || (start >= m_size*m_size) ||
			(end   < 0) || (end   >= m_size*m_size) ||
			(start == end)) {
			bIsWord = false;
		}
		else if ((end>start) && ((end-start)<5) && ((start/5) == (end/5))) {
			// horizontal
			//System.out.println(readWord(m_horizontal, screen2grid(start), end-start+1));
			word = readWord(m_horizontal, screen2grid(start), end-start+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%5==0)) {
			// vertical
			//System.out.println(readWord(m_vertical, screen2grid(start), (end-start)/5));
			word = readWord(m_vertical, screen2grid(start), (end-start)/5);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%6==0)) {
			// down diagonal
			//System.out.println(readWord(m_dndiag, screen2grid(start), (end-start)/6+1));
			word = readWord(m_dndiag, screen2grid(start), (end-start)/6+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end<start) && ((start-end)%4==0)) {
			// up diagonal
			//System.out.println(readWord(m_updiag, screen2grid(start), (start-end)/4+1));
			word = readWord(m_updiag, screen2grid(start), (start-end)/4+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		
		if (bIsWord) {
			return(word);
		}
		else {
			return("");
		}
	}
	
	private String readWord(int dir, int start, int length) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < length; i++ ) {
			sb.append(m_grid[start]);
			if (m_grid[start] == m_deadchar) {
				break;
			}
			start += m_offset[dir];
		}

        String s = sb.toString();
        
        return(s);
	}
	
	
	public void printGrid() {
		//printWholeGrid();
		
		System.out.println();
		
		for (int i=0; i<m_size;i++) {
			for (int j=0; j<m_size;j++) {
				System.out.print(m_grid[8+i*(m_size+2)+j]);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	private void printWholeGrid() {
		System.out.println();
		
		for (int i=0; i<m_size+2;i++) {
			for (int j=0; j<m_size+2;j++) {
				System.out.print(m_grid[i*(m_size+2)+j]);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	private int countAllWords() {
		int count = 0;
		String word;
		HashSet<String> foundwords = new HashSet<String>();
		
		for (int start=0; start<m_size*m_size;start++) {
			for (int l=2; l<=m_size; l++) {
				for (int dir=0; dir<m_numDirs; dir++) {
					word = readWord(dir, start, l);
					if (m_dictionary.IsWord(word) && (!foundwords.contains(word))) {
						//System.out.println("Found " + word);
						foundwords.add(word);
						count++;
					}
				}
			}
		}
		
		return(count);
	}
	
	private ArrayList<String> findAllWords() {
		String word;
		ArrayList<String> foundwords = new ArrayList<String>();
		
		for (int start=0; start<m_size*m_size;start++) {
			for (int l=2; l<=m_size; l++) {
				for (int dir=0; dir<m_numDirs; dir++) {
					word = readWord(dir, start, l);
					if (m_dictionary.IsWord(word) && (!foundwords.contains(word))) {
						//System.out.println("Found " + word);
						foundwords.add(word);
					}
				}
			}
		}
		
		return(foundwords);
	}
}
