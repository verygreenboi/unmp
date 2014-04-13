package com.parnswir.unmp;

import java.util.Hashtable;

import org.jaudiotagger.tag.TagOptionSingleton;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static SQLiteDatabase DB;
	
	private String[] drawerItems = C.FRAGMENTS;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawer;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Boolean rootView = true;
    
    private Hashtable<Integer, Fragment> fragmentCache = new Hashtable<Integer, Fragment>();
    
    private ListView contentList;
    private IconicAdapter adapter;
    private CoverList currentContent = new CoverList();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        initializeDrawer();
	}
	
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        MusicDatabaseHelper dbHelper = new MusicDatabaseHelper(this);
		DB = dbHelper.getWritableDatabase();
		
		adapter = new IconicAdapter(this, currentContent); 
		
		TagOptionSingleton.getInstance().setAndroid(true);
        
        mDrawerToggle.syncState();
    }
	
	
	@Override
	protected void onStart() {
		super.onStart();
		setPlayerServiceState(PlayerService.STOP);
		showPlayerHome();
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		setPlayerServiceState(PlayerService.START);
	}

	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
	
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawer);
        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }
	
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    switch (keyCode) {
		    case KeyEvent.KEYCODE_MENU:
		        if (mDrawerLayout.isDrawerOpen(mDrawer)) {
		        	mDrawerLayout.closeDrawer(mDrawer);
		        } else {
		        	mDrawerLayout.openDrawer(mDrawer);
		        }
		        return true;
		    case KeyEvent.KEYCODE_BACK:
		    	if (!rootView) {
		    		showPlayerHome();
		    		return true;
		    	} 
	    }
	    return super.onKeyUp(keyCode, event);
	}
	
	
	private void initializeDrawer() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	private void showPlayerHome() {
		selectItem(0);
	}
	
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}


	private void selectItem(int position) {
		rootView = (position == 0);
	    showFragment(position);
	    mDrawerList.setItemChecked(position, true);
	    closeDrawerDelayedBy(100);
	}
	
	
	private void closeDrawerDelayedBy(int milliseconds) {
		new Handler().postDelayed(new Runnable() {
	        @Override
	        public void run() {
	        	mDrawerLayout.closeDrawer(mDrawer);
	        }
	    }, milliseconds);
	}
	
	
	private void showFragment(int position) {
		Fragment fragment = getFragment(position);
	    FragmentManager fragmentManager = getFragmentManager();
	    fragmentManager.beginTransaction()
	                   .replace(R.id.content_frame, fragment)
	                   .commit();
	}
	
	
	private Fragment getFragment(int position) {
		Fragment fragment;
		if (fragmentCache.contains(position)) {
			fragment = fragmentCache.get(position);
		} else {
			fragment = new ContentFragment();
			Bundle args = new Bundle();
		    args.putInt(ContentFragment.ARG_FRAGMENT_NUMBER, position);
		    fragment.setArguments(args);
			fragmentCache.put(position, fragment);
		}
		return fragment;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
	          return true;
	        }		
		switch (item.getItemId()) {
		case R.id.action_search: startActivityNamed(DebugActivity.class); return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public void startActivityNamed(Class<?> className) {
		Intent intent = new Intent(this, className);
		startActivity(intent);
	}
	
	
	public void onClickCover(View view) {
		ActionBar actionBar = getActionBar();
		setOverlayVisibilityTo(!actionBar.isShowing());	
	}
	
	
	public void onClickPlay(View view) {
		
	}
	
	
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	
	
	public void setOverlayVisibilityTo(Boolean v) {
		ActionBar actionBar = getActionBar();
		RelativeLayout overlay = (RelativeLayout) findViewById(R.id.coverOverlay);
		if (!v) {
			actionBar.hide();
			overlay.setVisibility(View.INVISIBLE);
		} else  {
			actionBar.show();
			overlay.setVisibility(View.VISIBLE);
		}
	}


	public void onChangeToListFragment(int i, View layout) {
		String tableName = C.LIST_FRAGMENT_TABLENAMES[i-2];
		String nameColumn = C.getNameColumnFor(tableName);
		
        contentList = (ListView) layout.findViewById(R.id.contentList);
        contentList.setAdapter(adapter);
        currentContent.clear();
		
		Cursor cursor = DB.query(tableName, new String[] {nameColumn}, null, null, null, null, nameColumn);
		cursor.moveToFirst();
		while (! cursor.isAfterLast()) {
			currentContent.names.add(cursor.getString(0));
			cursor.moveToNext();
		} 
		cursor.close();
	}
	
	
	public void onClickOnListItem(View item) {
		TextView label = (TextView) item.findViewById(R.id.label);
		String text = (String) label.getText();
		selectItem(0);
		showToast(text);
	}
	
	
	private void setPlayerServiceState(int state) {
		Intent intent = new Intent(this, PlayerService.class);
		intent.putExtra(PlayerService.EXTRA_ID, state);
		startService(intent);
	}
}
