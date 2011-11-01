package wordsearchdemo.com;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import wordSearch.*;
import wordsearchdemo.com.Global;

public class WordSearchDemoActivity extends Activity {
		
	private static int mGridSize = 5;
	ImageAdapter mIA;
	GridView mGridView;
	TextView mTextViewScore;
	TextView mTextViewTotal;
	HashSet<String> mWordsFound;
	int mMisses;
	int mNumWordsTotal;
	Spinner mSpinWordsFound;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	mGridView = (GridView) findViewById(R.id.gridview);
    	mTextViewScore = (TextView) findViewById(R.id.textViewScore);
    	mTextViewTotal = (TextView) findViewById(R.id.textViewTotal);
    	
    	Resources resources = getResources();
		InputStream is = resources.openRawResource(R.raw.dictionary);
		
		mSpinWordsFound = (Spinner) findViewById(R.id.spinWordsFound);
		
		mWordsFound = new HashSet<String>();
		mMisses = 0;

		mIA = new ImageAdapter(this, mGridSize, is);
		mNumWordsTotal = mIA.Initialize();
		mTextViewTotal.setText("Found: 0/" + mNumWordsTotal);
		mTextViewScore.setText("Score: 0");
		mGridView.setAdapter(mIA);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    			if (mIA.mStart < 0) {
    				mIA.mStart = position;
    				mIA.mEnd = -1;
    			}
    			else {
    				mIA.mEnd = position;
    				//Log.i(Global.INFO, "Checking " + ia.mStart + " to " + ia.mEnd);
    				mMisses++;
    				if (mIA.mWordGrid.isWord(mIA.mStart, mIA.mEnd)) {
    					String word = mIA.mWordGrid.getWord(mIA.mStart, mIA.mEnd);
    					if (!mWordsFound.contains(word)) {
    						Toast.makeText(WordSearchDemoActivity.this, "Found " + word, Toast.LENGTH_SHORT).show();
    						mIA.mStart = -1;
    						mIA.mEnd = -1;
    						mWordsFound.add(word);
    						mMisses--;
    						String[] array_words = new String[mWordsFound.size()];
    						Iterator<String> itr = mWordsFound.iterator();
    						
    						int i = 0;
    						while (itr.hasNext()) {
    							array_words[i] = itr.next();
    							i++;
    						}
    						ArrayAdapter<String> adapter = new ArrayAdapter<String>(WordSearchDemoActivity.this, android.R.layout.simple_spinner_item, array_words);
    						mSpinWordsFound.setAdapter(adapter);
    						mTextViewTotal.setText("Found: " + mWordsFound.size() + "/" + mNumWordsTotal);
    						Log.i(Global.INFO, "Found " + word);
    					}
    				}
    				else {
    					mIA.mStart = -1;
    					mIA.mEnd = -1;
    				}
    				int score = CalcScore();
					mTextViewScore.setText("Score: " + score);
    			}
    		}
    	});
    }
    
	public int CalcScore() {
		int score = 0;
		Iterator<String> itr = mWordsFound.iterator();
		
		while (itr.hasNext()) {
			score += itr.next().length();
		}
		
		score -= mMisses;
		
		return(score);
	}
	
	public void OnNewGame(View view) {
	    Log.i(Global.INFO, "Starting grid initialization");
	    mNumWordsTotal = mIA.Initialize();
		mGridView.setAdapter(mIA);
		mTextViewTotal.setText("Found: 0/" + mNumWordsTotal);
		mTextViewScore.setText("Score: 0");
		mWordsFound.clear();
		mMisses = 0;
		String[] array_words = new String[mWordsFound.size()];
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(WordSearchDemoActivity.this, android.R.layout.simple_spinner_item, array_words);
		mSpinWordsFound.setAdapter(adapter);
	    Log.i(Global.INFO, "Finished grid initialization");
	}
	public void OnExit(View view) {
	    System.exit(0);
	}
	public class ImageAdapter extends BaseAdapter {
    	private wordArray mWordGrid;
    	private Context mContext;
    	private int mGridSize;
    	public int mStart;
    	public int mEnd;
       	// references to our images
    	private Integer[] mThumbIds;

    	public ImageAdapter(Context c, int gridSize, InputStream is) {
    		mStart = -1;
    		mEnd = -1;
    		mContext = c;
    		mGridSize = gridSize;

    		mThumbIds = new Integer[mGridSize*mGridSize];
    		mWordGrid = new wordArray(mGridSize, is);
    	}
    	public int Initialize() {
    		int wordCount = mWordGrid.Initialize(41);
	    	for (int i=0; i<mGridSize*mGridSize; i++ ) {
	    		mThumbIds[i] = R.drawable.letter_a + mWordGrid.getChar(i) - 'a';
	    	}
	    	
	    	return(wordCount);
    	}
    	public int getCount() {
    		return mThumbIds.length;
    	}
    	public Object getItem(int position) {
    		return null;
    	}
    	public long getItemId(int position) {
    		return 0;
    	}
    	// create a new ImageView for each item referenced by the Adapter
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ImageView imageView;
    		if (convertView == null) {
    			// if it's not recycled, initialize some attributes
    			imageView = new ImageView(mContext);
    			imageView.setLayoutParams(new GridView.LayoutParams(40, 40));
    			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    			imageView.setPadding(0, 0, 0, 0);
    			}
    		else {
    			imageView = (ImageView) convertView;
    		}
    		imageView.setImageResource(mThumbIds[position]);
    		return imageView;
    	}
    }
}


		