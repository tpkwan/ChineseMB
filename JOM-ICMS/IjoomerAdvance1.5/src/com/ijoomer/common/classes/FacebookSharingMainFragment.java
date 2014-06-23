package com.ijoomer.common.classes;

import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.widget.LoginButton;
import com.ijoomer.src.R;
import com.ijoomer.theme.ThemeManager;

/**
 * This Class Contains All Method Related To FacebookSharingMainFragment.
 * 
 * @author tasol
 * 
 */
public class FacebookSharingMainFragment extends Fragment {

	/**
	 * Overrides method
	 */
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions","publish_stream");
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(ThemeManager.getInstance().getFacebook(), container, false);

		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		authButton.setPublishPermissions(PERMISSIONS);
		authButton.setApplicationId(getString(R.string.facebook_app_id));

		return view;
	}
}
