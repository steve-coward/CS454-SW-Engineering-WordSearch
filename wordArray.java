//
package wordSearch;

import java.util.Random;
import wordSearch.*;


public class wordArray {
	int m_size;
	char m_grid[];
	Dictionary m_dictionary;
	
	public wordArray(int size) {
		m_size = size;
		m_grid = new char [m_size*m_size];
		m_dictionary = new Dictionary();
	}
	
	// For 5x5 grid there are 26^25 combos.
	// The probability of randomly creating a 5 letter word from dictionary of 
	// 16600/3 words is extremely small.  Therefore an intelligent algorithm is
	// required.
	
	public boolean initialize() {
		
		int randNum;
		int len;
		int dir;
		int offset;
		String word;
		int start;
		String regExp;
		int numWords;
		int numWordsPlaced;
		int skipWords;
		int numTries;
		
		// For testing only, make random stream deterministic and repeatable
		Random rand = new Random( 19580426 );
		//Random rand = new Random(  );
		
		// Fill with .'s
		for (int i=0; i<m_size*m_size;i++) {
			m_grid[i] = '.';
		}
		
		// Pick a target number of words puzzle will contain
		// This will be an upper bound on number of words inserted
		// but actual number of words may be greater due to placing
		// one word near another word or set of words.
		// Test that all combos can generate at least one puzzle
		numWords = rand.nextInt(8) + 5;
		numWords = 12;
		System.out.println("Placing " + numWords + " words");
		
		numWordsPlaced = 0;
		numTries = 0;
		
		while ((numTries < 100) && (numWordsPlaced < numWords)) {
			numTries++;
			
			// Select a word length
			len = rand.nextInt(4) + 2;

			// How to randomize word selected that meets requirements?
			skipWords = rand.nextInt(m_dictionary.m_wordCount.get(len)) + 1;

			// Find direction of word
			dir = rand.nextInt(3); // hor, ver, diag
			
			// Find starting index of word
			if (dir == 0) {
				// horizontal
				// start = row*size+offset
				start = rand.nextInt(m_size)*m_size + rand.nextInt(m_size - len + 1);
				offset = 1;
			}
			else if (dir == 1) {
				// vertical
				// start = column+offset*size
				start = rand.nextInt(m_size) + rand.nextInt(m_size - len + 1)*m_size;
				offset = m_size;
			}
			else {
				// diagonal
				// start = diagonal+offset*size
				// there are 9 diagonals, clockwise starting at lower left
				// but disallow one letter words
				int diag;
				switch (len) {
				case 5:
					start = 0;
					break;
				case 4:
					diag = rand.nextInt(4);
					switch (diag) {
					case 0:start = 0;break;
					case 1:start = 1;break;
					case 2:start = 5;break;
					case 3:start = 6;break;
					default:start = 6;break;
					}
					break;
				case 3:
					diag = rand.nextInt(9);
					switch (diag) {
					case 0:start = 10;break;
					case 1:start = 5;break;
					case 2:start = 11;break;
					case 3:start = 0;break;
					case 4:start = 6;break;
					case 5:start = 12;break;
					case 6:start = 1;break;
					case 7:start = 7;break;
					case 8:start = 2;break;
					default:start = 2;break;
					}
					break;
				default:
				case 2:
					start = rand.nextInt(16);
					switch (start) {
					case 4:start = 16;break;
					case 9:start = 17;break;
					case 14:start = 18;break;
					default:start = 18;break;
					}
				}
				offset = m_size + 1;
			}


			// Get current state of grid
			regExp = readWord(dir, start, len);
			
			if (regExp.matches("[a-z]+")) {
				continue;
			}

			// Select word from dictionary
			word = m_dictionary.findMatch(regExp, skipWords);

			if (word.length() == len) {
				// found match! So place it into grid
				numTries = 0;
				for (int i=0; i<word.length();i++) {
					m_grid[start] = word.charAt(i);
					start += offset;
				}
				
				numWordsPlaced++;
				System.out.print("Placing " + word);
				printGrid();
			}
		}
		
		// Now fill empty spaces randomly
		for (int i=0; i<m_size*m_size;i++) {
			if (m_grid[i] == '.') {
				m_grid[i] = (char) ((rand.nextInt(26)) + 'a');
			}
		}

		printGrid();
	
		return(true);
	}
	
	public String readWord(int dir, int start, int length) {
		int offset;
		StringBuffer sb = new StringBuffer();

		if (dir == 0) {
			offset = 1;
		}
		else if (dir == 1) {
			offset = m_size;
		}
		else {
			offset = m_size + 1;
		}
		
		for (int i = 0; i < length; i++ ) {
			sb.append(m_grid[start]);
			start += offset;
		}

        String s = sb.toString();
        
        return(s);
	}
	
	
	public void printGrid() {
		System.out.println();
		
		for (int i=0; i<m_size;i++) {
			for (int j=0; j<m_size;j++) {
				System.out.print(m_grid[i*m_size+j]);
			}
			System.out.println();
		}
		System.out.println();
	}
}
