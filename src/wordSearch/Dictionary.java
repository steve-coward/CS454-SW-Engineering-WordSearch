//
package wordSearch;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import wordsearchdemo.com.Global;
import android.util.Log;

public class Dictionary {
	// HashSet is a collection. You can not store duplicate value in HashSet. 
	// This class makes no guarantees as to the order of the map; in particular,
	// it does not guarantee that the order will remain constant over time.
	// This implementation provides constant-time performance for the basic
	// operations (get and put), assuming the hash function disperses the
	// elements properly among the buckets.
	// HashSet is not memory efficient.  Dictionary size (assuming char)
	// is 16k x 5bytes = 80kB.  String type adds fair overhead over char type.
	// Java String does support regular expressions, which are used in initialization.

	HashSet<String> m_dictionary;
	HashMap<Integer, HashSet<String> > m_wordDictionary;
	HashMap<Integer, Integer> m_wordCount;
	// Iterator is member field to retain value between lookups and games
	// This allows efficient means to distribute grid initialization across
	// entire dictionary
	Iterator<String> mItr;
	
	public Dictionary () {
	}
	
	public Dictionary (InputStream is) {
		m_dictionary = new HashSet<String>();
		m_wordCount = new HashMap<Integer, Integer>();
		mItr = m_dictionary.iterator();

		for (int i = 0; i < 15; i++ ) {
			m_wordCount.put(i, 0);
		}
		
		try {
			// Open the file that is the first 
			// command line parameter
			//FileInputStream fstream = new FileInputStream("C:\\Users\\Steve\\My Projects\\WordSearchDemo\\dictionary.txt");
			//FileInputStream fstream = new FileInputStream("/data/data/dictionary.txt");
			//AssetFileDescriptor descriptor = getAssets().openFD("dictionary.txt");
			//FileInputStream fstream = new FileInputStream(context.get"dictionary.txt");
			// Get the object of DataInputStream
			
			//DataInputStream in = new DataInputStream(descriptor.createInputStream());
			//BufferedReader br = new BufferedReader(new InputStreamReader(in));
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String strLine;
			// Read File Line By Line
			// dictionary.txt should contain 16610 3, 4, and 5 letter words
			while ((strLine = br.readLine()) != null)   {
				// Add the string to the dictionary
				m_dictionary.add(strLine);

				// Update word length count
				m_wordCount.put(new Integer(strLine.length()), 
						m_wordCount.get(strLine.length()) + 1);
			}
			//Close the input stream
			is.close();
		} catch (Exception e){//Catch exception if any
			Log.e(Global.INFO, "Error: " + e.getMessage());
		}
	};
	
	public void advanceItr(int count) {
		count = count % m_dictionary.size();
		while (count > 0) { 	
			if(mItr.hasNext()) {
				mItr.next();
			}
			else {
				// wrap
				mItr = m_dictionary.iterator();
			}
			count--;
		}
	}
	
	public boolean IsWord(String word) {
		return(m_dictionary.contains(word));
	}
	
	public String findMatch(String regExp) {
		int wrap = 0;
		int tries = 0;

		assert(m_dictionary.size() > 1);

        // Could compare to starting iterator to know when wrap first occurs
		while ((wrap < 2) && (tries < 100)) {
        	tries++;
        	if (!mItr.hasNext()) {
        		wrap++;
        		mItr = m_dictionary.iterator();
        	}
        	
        	String word = (String)mItr.next();
        	if (word.length() == regExp.length()) {
        		if (word.matches(regExp)) {
        			return(word);
        		}
        	}
        }
        
        return("");
	}
}
