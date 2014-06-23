package com.ijoomer.common.classes;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.src.IjoomerLoginActivity;
import com.ijoomer.src.R;
import com.ijoomer.theme.ThemeManager;

/**
 * This Class Contains All Method Related To FacebookLoginActivity.
 * 
 * @author tasol
 * 
 */
public class FacebookLoginActivity extends FragmentActivity {

	private LinearLayout lnrPbr;
	private IjoomerTextView txtPbr;
	private FacebookLoginMainFragment mainFragment;
	private UiLifecycleHelper uiHelper;

	private boolean isFetchUserData = false;

	/**
	 * Overrides method
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeManager.getInstance().setTheme(this);
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			mainFragment = new FacebookLoginMainFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
		} else {
			// Or set the fragment from restored state info
			mainFragment = (FacebookLoginMainFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
		}

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		lnrPbr = (LinearLayout) findViewById(R.id.lnrPbr);
		txtPbr = (IjoomerTextView) findViewById(R.id.txtPbr);
		txtPbr.setText(getString(R.string.facebook_user_data_fetching));
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		} else {

			if (session == null) {
				session = new Session(this);
				Session.setActiveSession(session);
			}
			OpenRequest openRequest = new OpenRequest(this);
			List<String> readPermissions = new ArrayList<String>();
			readPermissions.add("email");
			openRequest.setPermissions(readPermissions);
			openRequest.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
			openRequest.setCallback(callback);
			session.openForRead(openRequest);

			// Session.openActiveSession(this, mainFragment,true, callback);
		}

		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			finish();
		} else {
			uiHelper.onActivityResult(requestCode, resultCode, data);
		}

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
		try {
			Session.getActiveSession().closeAndClearTokenInformation();
			Session.setActiveSession(null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	/**
	 * Class method
	 */

	/**
	 * This method used to after session state change listner.
	 * 
	 * @param session
	 *            represent {@link Session}
	 * @param state
	 *            represent {@link SessionState}
	 * @param exception
	 *            represent exception
	 */
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened() && !isFetchUserData) {
			facebookUserData();
		} else if (state.isClosed()) {
			OpenRequest openRequest = new OpenRequest(this);
			List<String> readPermissions = new ArrayList<String>();
			readPermissions.add("email");
			openRequest.setPermissions(readPermissions);
			openRequest.setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
			openRequest.setCallback(callback);
			session.openForRead(openRequest);
		}
	}

	/**
	 * This method used to get session status callback change listner.
	 */
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	/**
	 * This method used to get facebook user data.
	 */
	private void facebookUserData() {
		lnrPbr.setVisibility(View.VISIBLE);
		isFetchUserData = true;
		Bundle postParams = new Bundle();
		postParams
				.putString(
						"q",
						"select name,about_me,birthday_date,current_address,education,email,first_name,hometown_location,last_name,middle_name,pic,pic_big,pic_cover,pic_small,pic_square,political,quotes,relationship_status,religion,sex,sports,status,timezone,tv,uid,username,verified,website,work from user where uid=me()");

		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				isFetchUserData = false;
				lnrPbr.setVisibility(View.GONE);
				JSONObject json = null;
				try {
					json = response.getGraphObject().getInnerJSONObject().getJSONArray("data").getJSONObject(0);
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
				FacebookRequestError error = response.getError();
				if (error != null) {
					json = new JSONObject();
					try {
						json.put("errorMessage", error.getErrorMessage());
					} catch (Throwable e) {
						e.printStackTrace();
					}
					Intent intent = new Intent(getApplicationContext(), IjoomerLoginActivity.class);
					intent.putExtra("IN_FACEBOOK_USER", json.toString());
					setResult(RESULT_OK, intent);
					finish();
				} else {
					Intent intent = new Intent(getApplicationContext(), IjoomerLoginActivity.class);
					intent.putExtra("IN_FACEBOOK_USER", json.toString());
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		};

		Request request = new Request(Session.getActiveSession(), "fql", postParams, HttpMethod.GET, callback);

		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();

	}

}