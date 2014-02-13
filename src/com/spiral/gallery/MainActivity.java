package com.spiral.gallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;
import com.spiral.gallery.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnDragListener;

public class MainActivity extends Activity implements Request.Callback, OnDragListener, OnItemClickListener {
	private static final String TAG = "MainFragment";
	private UiLifecycleHelper uiHelper;
	private StaggeredGridView mGrid;
    private ArrayList<String> mData;
	
	/**
	 * Callback called whenever there is a session change
	 */
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG,"onCreate");
		uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
	    
	    LoginButton authButton = (LoginButton) findViewById(R.id.fb_btn);
	    authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
		
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        if(hasPhotoPermissions())
	        	getUserPhotos();
	        else
	        	requestPhotoPermissions();
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	private void requestPhotoPermissions(){
	    Session session = Session.getActiveSession();
		Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, Arrays.asList("user_photos"));	    
		session.requestNewReadPermissions(newPermissionsRequest);
	}
	
	private boolean hasPhotoPermissions(){
	    Session session = Session.getActiveSession();
	    for(String permission : session.getPermissions())
	    	if(permission.equals("user_photos"))
	    		return true;
	    return false;
	}
	
	private void getUserPhotos(){
	    Session session = Session.getActiveSession();
	    Log.d(TAG,"getUserData. session: "+session+", isOpened: "+session.isOpened());
		if (session != null && session.isOpened()) {
	        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
	        	
	            @Override
	            public void onCompleted(GraphUser user, Response response) {
	            	Log.d(TAG,"User id: "+user.getId());
	            	performQuery(user);
	            }
	        }); 
	        Request.executeBatchAsync(request);
	    } 
	}
	
	private void performQuery(GraphUser user){
		Log.d(TAG,"performQuery");
		String fqlQuery = "select src_small, src_big, caption FROM photo WHERE owner = "+user.getId();
	    Bundle params = new Bundle();
	    params.putString("q", fqlQuery);
	    Session session = Session.getActiveSession();
        Request request = new Request(session,
                "/fql",                         
                params,                         
                HttpMethod.GET,
        		this); 
        Request.executeBatchAsync(request);
	}

	@Override
	public void onCompleted(Response response) {
		GraphObject graphObject = response.getGraphObject();
		JSONArray array = (JSONArray) graphObject.getProperty("data");
//		for(int i = 0; i < array.length(); i++)
//			try {
////				Log.d(TAG,"element at "+i+": "+array.get(i));
//			} catch (JSONException e) {
//				Log.e(TAG,"JSONException! Msg: "+e.getMessage());
//			}
		StaggeredAdapter adapter = new StaggeredAdapter(array);
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		Log.d(TAG,"onDrag");
		return false;
	}

	@Override
	public void onItemClick(StaggeredGridView parent, View view, int position,
			long id) {
		Log.d(TAG,"onItemClick");
	}
}