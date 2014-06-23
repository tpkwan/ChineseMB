package com.ijoomer.common.classes;

import java.util.ArrayList;
import java.util.List;

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
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.src.R;
import com.ijoomer.theme.ThemeManager;
import com.smart.framework.CustomAlertNeutral;

/**
 * This Class Contains All Method Related To FacebookSharingActivity.
 * 
 * @author tasol
 * 
 */
public class FacebookSharingActivity extends FragmentActivity {

	private LinearLayout lnrPbr;
	private IjoomerTextView txtPbr;
	private FacebookSharingMainFragment mainFragment;
	private UiLifecycleHelper uiHelper;

	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private boolean isSharingData = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeManager.getInstance().setTheme(this);
		super.onCreate(savedInstanceState);

		IjoomerUtilities.mSmartAndroidActivity = this;
		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			mainFragment = new FacebookSharingMainFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
		} else {
			// Or set the fragment from restored state info
			mainFragment = (FacebookSharingMainFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
			pendingPublishReauthorization = savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
		}

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		lnrPbr = (LinearLayout) findViewById(R.id.lnrPbr);
		txtPbr = (IjoomerTextView) findViewById(R.id.txtPbr);
		txtPbr.setText(getString(R.string.facebook_wall_posting));
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened())) {
			onSessionStateChange(session, session.getState(), null);
		} else {
			if (session == null) {
				session = new Session(this);
				Session.setActiveSession(session);
			}
			OpenRequest openRequest = new OpenRequest(this);
			List<String> writePermissions = new ArrayList<String>();
			writePermissions.add("publish_actions");
			openRequest.setPermissions(writePermissions);
			openRequest.setCallback(callback);
			session.openForPublish(openRequest);
		}

		uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
		uiHelper.onSaveInstanceState(outState);
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

	/**
	 * This method used to after session state change listener.
	 * 
	 * @param session
	 *            represent {@link Session}
	 * @param state
	 *            represent {@link SessionState}
	 * @param exception
	 *            represent exception
	 */
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened() && !isSharingData) {
			pendingPublishReauthorization = false;
			postData();
		} else if (state.isClosed()) {
			OpenRequest openRequest = new OpenRequest(this);
			List<String> writePermissions = new ArrayList<String>();
			writePermissions.add("publish_actions");
			openRequest.setPermissions(writePermissions);
			openRequest.setCallback(callback);
			session.openForPublish(openRequest);
		}
	}

	/**
	 * This method used to get session status callback change listener.
	 */
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	/**
	 * This method used to post data on facebook.
	 */
	private void postData() {
		lnrPbr.setVisibility(View.VISIBLE);
		isSharingData = true;

		Session session = Session.getActiveSession();
		if (session != null) {

			Bundle postParams = new Bundle();
			postParams.putString("name", getIntent().getStringExtra("IN_SHARE_CAPTION") == null ? "" : getIntent().getStringExtra("IN_SHARE_CAPTION"));
			postParams.putString("caption", getIntent().getStringExtra("IN_SHARE_CAPTION") == null ? "" : getIntent().getStringExtra("IN_SHARE_CAPTION"));
			postParams.putString("description", getIntent().getStringExtra("IN_SHARE_DESCRIPTION") == null ? "" : getIntent().getStringExtra("IN_SHARE_DESCRIPTION"));
			postParams.putString("link", getIntent().getStringExtra("IN_SHARE_SHARELINK") == null ? "" : getIntent().getStringExtra("IN_SHARE_SHARELINK"));
			postParams.putString("picture", getIntent().getStringExtra("IN_SHARE_THUMB") == null ? "" : getIntent().getStringExtra("IN_SHARE_THUMB"));
			postParams.putString("message", getIntent().getStringExtra("IN_SHARE_MESSAGE") == null ? "" : getIntent().getStringExtra("IN_SHARE_MESSAGE"));

			Request.Callback callback = new Request.Callback() {

				public void onCompleted(Response response) {
					isSharingData = false;
					lnrPbr.setVisibility(View.GONE);
					FacebookRequestError error = response.getError();
					if (error != null) {
						IjoomerUtilities.getCustomOkDialog(getString(R.string.facebook_share_title), error.getErrorMessage(), getString(R.string.ok), R.layout.ijoomer_ok_dialog,
								new CustomAlertNeutral() {

									@Override
									public void NeutralMethod() {
										finish();
									}
								});
					} else {
						IjoomerUtilities.getCustomOkDialog(getString(R.string.facebook_share_title), getString(R.string.facebook_share_success), getString(R.string.ok),
								R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

									@Override
									public void NeutralMethod() {
										finish();
									}
								});
					}
				}
			};

			Request request = new Request(Session.getActiveSession(), "me/feed", postParams, HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}

	}
}
