//
package wordSearch;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

import android.content.res.AssetFileDescriptor;
import android.util.Log;
import wordsearchdemo.com.Global;

public class wordArray {
	private int m_size;
	private int m_numSlots;
	private int m_offset[];
	private int m_wordCount;
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
	public wordArray() {
	}
	public wordArray(int size, InputStream is) {
		m_size = size;
		m_numSlots = (m_size+2)*(m_size+2);
		m_wordCount = -1;
		
		m_offset = new int [m_numDirs];
		m_offset[m_horizontal] = 1;
		m_offset[m_vertical] = m_size+2;
		m_offset[m_dndiag] = m_size+3;
		m_offset[m_updiag] = -m_size-1;
		m_grid = new char [m_numSlots];
		m_dictionary = new Dictionary(is);
	}
	
	// For 5x5 grid there are 26^25 combos.
	// The probability of randomly creating a 5 letter word from dictionary of 
	// 16600/3 words is extremely small.  Therefore an intelligent algorithm is
	// required.
	//
	public int Initialize(long seed) {
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
		//Random rand = new Random( 19580426 );
		//Random rand = new Random( seed );
		Random rand = new Random( );
		
		Log.i(Global.INFO, "Seeding with random number " + seed);
		
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
		numWords = 5;
		//Log.i(Global.INFO, "Placing " + numWords + " words");
		
		numWordsPlaced = 0;
		numTries = 0;
		
		while ((numTries < 10) && (numWordsPlaced < numWords)) {
			numTries++;
			
			// Select a word length
			len = rand.nextInt(m_size-1) + 2;

			// How to randomize word selected that meets requirements?
			//skipWords = rand.nextInt(m_dictionary.m_wordCount.get(len)) + 1;
			skipWords = rand.nextInt(10) + 1;

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
				//Log.i(Global.INFO, );
				Log.i(Global.INFO, "Placing " + word);
				//printGrid();"Placing " + word
			}
		}
		
		//printGrid();
		
		// Now fill empty spaces randomly
		for (int i=0; i<(m_size+2)*(m_size+2);i++) {
			if (m_grid[i] == '.') {
				m_grid[i] = (char) ((rand.nextInt(26)) + 'a');
			}
		}

		printGrid();
		
		m_wordCount = countAllWords();
		Log.i(Global.INFO, "Grid contains " + m_wordCount + " words.");
	
		return(m_wordCount);
	}
	

	public int Initialize(String initFile) {

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

		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(initFile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			// expecting 5x5 character array as input
			int row = 0;
			while ((strLine = br.readLine()) != null) {
				assert(strLine.length() != m_size);
				assert(row < m_size);

				for (int i=0; i<m_size;i++) {
					m_grid[screen2grid(row*m_size+i)] = strLine.charAt(i);
				}
				row++;
			}
			assert(row == m_size);
			//Close the input stream
			in.close();
		} catch (Exception e){//Catch exception if any
			Log.e(Global.INFO, "Error: " + e.getMessage());
		}
		
		//printGrid();
		
		m_wordCount = countAllWords();
		Log.i(Global.INFO, "Grid contains " + m_wordCount + " words.\n");
	
		return(m_wordCount);
	}
	
	// 5x5 -> 7x7
	private int screen2grid(int p) {
		if (p<5)  return(p+8);
		if (p<10) return(p+10);
		if (p<15) return(p+12);
		if (p<20) return(p+14);
		return(p+16);
	}
	
	// start and end are in 5x5 space
	public Boolean isWord(int start, int end) {
		Boolean bIsWord = false;
		
		String word = "";
		
		if ((start < 0) || (start >= m_size*m_size) ||
			(end   < 0) || (end   >= m_size*m_size) ||
			(start == end)) {
			bIsWord = false;
		}
		else if ((end>start) && ((end-start)<m_size) && ((start/m_size) == (end/m_size))) {
			// horizontal
			//Log.i(Global.INFO, readWord(m_horizontal, screen2grid(start), end-start+1));
			word = readWord(m_horizontal, screen2grid(start), end-start+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%m_size==0)) {
			// vertical
			//Log.i(Global.INFO, readWord(m_vertical, screen2grid(start), (end-start)/m_size));
			word = readWord(m_vertical, screen2grid(start), (end-start)/m_size + 1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%(m_size+1)==0)) {
			// down diagonal
			//Log.i(Global.INFO, readWord(m_dndiag, screen2grid(start), (end-start)/(m_size+1)+1));
			word = readWord(m_dndiag, screen2grid(start), (end-start)/(m_size+1)+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end<start) && ((start-end)%(m_size-1)==0)) {
			// up diagonal
			//Log.i(Global.INFO, readWord(m_updiag, screen2grid(start), (start-end)/(m_size-1)+1));
			word = readWord(m_updiag, screen2grid(start), (start-end)/(m_size-1)+1);
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
		else if ((end>start) && ((end-start)<m_size) && ((start/m_size) == (end/m_size))) {
			// horizontal
			//Log.i(Global.INFO, readWord(m_horizontal, screen2grid(start), end-start+1));
			word = readWord(m_horizontal, screen2grid(start), end-start+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%m_size==0)) {
			// vertical
			//Log.i(Global.INFO, readWord(m_vertical, screen2grid(start), (end-start)/m_size));
			word = readWord(m_vertical, screen2grid(start), (end-start)/m_size + 1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end>start) && ((end-start)%(m_size+1)==0)) {
			// down diagonal
			//Log.i(Global.INFO, readWord(m_dndiag, screen2grid(start), (end-start)/(m_size+1)+1));
			word = readWord(m_dndiag, screen2grid(start), (end-start)/(m_size+1)+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		else if ((end<start) && ((start-end)%(m_size-1)==0)) {
			// up diagonal
			//Log.i(Global.INFO, readWord(m_updiag, screen2grid(start), (start-end)/(m_size-1)+1));
			word = readWord(m_updiag, screen2grid(start), (start-end)/(m_size-1)+1);
			bIsWord = m_dictionary.IsWord(word);
		}
		
		if (bIsWord) {
			return(word);
		}
		else {
			return("");
		}
	}
	
	public char getChar(int slot) {
		return(m_grid[screen2grid(slot)]);
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
	
	private String readRow(int start, int length) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < length; i++ ) {
			sb.append(m_grid[start]);
			start += m_offset[m_horizontal];
		}

		String s = sb.toString();
        
        return(s);
	}
	
	public int getWordCount() {
		if (m_wordCount < 0) {
			m_wordCount = countAllWords();
		}
		
		return(m_wordCount);
	}
	
	public void printGrid() {
		//printWholeGrid();
		
		Log.i(Global.INFO, "\n");
		
		int j = 8;
		for (int i=0; i<m_size;i++) {
			String row = readRow(j, m_size);
			Log.i(Global.INFO, row);
			j += 7;
		}
		Log.i(Global.INFO, "\n");
	}
	
	private void printWholeGrid() {
		Log.i(Global.INFO, "\n");
		
		int j = 0;
		for (int i=0; i<m_size+2;i++) {
			String row = readRow(j, m_size+2);
			Log.i(Global.INFO, row);
			j += 7;
		}
		Log.i(Global.INFO, "\n");
	}
	
	private int countAllWords() {
		int count = 0;
		String word;
		HashSet<String> foundwords = new HashSet<String>();
		
		for (int start=0; start<(m_size+2)*(m_size+2);start++) {
			for (int l=2; l<=m_size; l++) {
				for (int dir=0; dir<m_numDirs; dir++) {
					word = readWord(dir, start, l);
					if (m_dictionary.IsWord(word) && (!foundwords.contains(word))) {
						//Log.i(Global.INFO, "Found " + word);
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
		
		for (int start=0; start<(m_size+2)*(m_size+2);start++) {
			for (int l=2; l<=m_size; l++) {
				for (int dir=0; dir<m_numDirs; dir++) {
					word = readWord(dir, start, l);
					if (m_dictionary.IsWord(word) && (!foundwords.contains(word))) {
						//Log.i(Global.INFO, "Found " + word);
						foundwords.add(word);
					}
				}
			}
		}
		
		return(foundwords);
	}
}
