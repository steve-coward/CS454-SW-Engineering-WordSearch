//
package wordSearch;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;

import wordsearchdemo.com.Global;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.util.Log;
public class Dictionary {
	// HashSet is a collection. You can not store duplicate value in HashSet. 
	// This class makes no guarantees as to the order of the map; in particular,
	// it does not guarantee that the order will remain constant over time.
	// This implementation provides constant-time performance for the basic
	// operations (get and put), assuming the hash function disperses the
	// elements properly among the buckets. 

	HashSet<String> m_dictionary;
	HashMap<Integer, Integer> m_wordCount;
	
	public Dictionary () {
	}
	public Dictionary (InputStream is) {
		m_dictionary = new HashSet<String>();
		m_wordCount = new HashMap<Integer, Integer>();

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
	
	public boolean IsWord(String word) {
		return(m_dictionary.contains(word));
	}
	
	public String findMatch(String regExp, int randNum) {
		String match = "";
		Iterator<String> it=m_dictionary.iterator();

		if (randNum > m_wordCount.get(regExp.length())) {
			randNum = m_wordCount.get(regExp.length());
		}
		else if (randNum <= 0) {
			randNum = 1;
		}

        while(it.hasNext() && (randNum > 0)) {
        	String word = (String)it.next();
        	if ((word.length() == regExp.length())
        			&& (word.matches(regExp))) {
        		randNum--;
        		match = word;
        	}
        }
        
        return(match);
	}
}
