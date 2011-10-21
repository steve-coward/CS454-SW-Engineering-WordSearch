package wordsearchdemo.com;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import wordSearch.*;
import wordsearchdemo.com.Global;

public class WordSearchDemoActivity extends Activity {
		
	private static int mGridSize = 5;
	ImageAdapter mIA;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	final GridView gridview = (GridView) findViewById(R.id.gridview);
    	//final Button buttonNewGame = (Button) findViewById(R.id.buttonNewGame);
    	
    	Resources resources = getResources();
		InputStream is = resources.openRawResource(R.raw.dictionary);
		
		mIA = new ImageAdapter(this, mGridSize, is);
		mIA.Initialize();
		//buttonNewGame.setOnClickListener(new OnClickListener() {
    	//	@Override
    	//	public void onClick(View v) {
    	//	}
    	//});
		gridview.setAdapter(mIA);
    	gridview.setOnItemClickListener(new OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    			if (mIA.mStart < 0) {
    				mIA.mStart = position;
    				mIA.mEnd = -1;
    			}
    			else {
    				mIA.mEnd = position;
    				//Log.i(Global.INFO, "Checking " + ia.mStart + " to " + ia.mEnd);
    				if (mIA.mWordGrid.isWord(mIA.mStart, mIA.mEnd)) {
    					String word = mIA.mWordGrid.getWord(mIA.mStart, mIA.mEnd);
    	    			Toast.makeText(WordSearchDemoActivity.this, "Found " + word, Toast.LENGTH_SHORT).show();
    	    			mIA.mStart = -1;
    	    			mIA.mEnd = -1;
    	    			Log.i(Global.INFO, "Found " + word);
    				}
    				else {
    					mIA.mStart = -1;
    					mIA.mEnd = -1;
    				}
    			}
    		}
    	});
    }
    
    public void OnNewGame(View view) {
    	Log.i(Global.INFO, "Button Clicked ");
    	mIA.Initialize();
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


		