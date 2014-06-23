package com.ijoomer.src;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;

import com.ijoomer.common.classes.FacebookLoginActivity;
import com.ijoomer.common.classes.IjoomerLoginMaster;
import com.ijoomer.common.classes.IjoomerUtilities;
import com.ijoomer.common.configuration.IjoomerGlobalConfiguration;
import com.ijoomer.customviews.IjoomerButton;
import com.ijoomer.customviews.IjoomerEditText;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.oauth.IjoomerOauth;
import com.ijoomer.weservice.WebCallListener;
import com.smart.framework.CustomAlertNeutral;


/**
 * This Class Contains All Method Related To IjoomerLoginActivity.
 *
 * @author tasol
 *
 */
public class IjoomerLoginActivity extends IjoomerLoginMaster{

    private IjoomerTextView btnSignUp;
    private IjoomerTextView btnForgetPassword;
    private IjoomerTextView btnForgetUserName;
    private IjoomerEditText edtUserName;
    private IjoomerEditText edtPassword;
    private IjoomerButton btnLogin;
    private IjoomerButton btnFbLogin;
    private IjoomerGlobalConfiguration globalConfiguration;

    private final int FACEBOOK_LOGIN = 1;

    /**
     * Overrides methods
     */

    @Override
    public int setLayoutId() {
        return R.layout.ijoomer_login;
    }

    @Override
    public void initComponents() {

        edtUserName = (IjoomerEditText) findViewById(R.id.edtUserName);
        edtPassword = (IjoomerEditText) findViewById(R.id.edtPassword);
        btnLogin = (IjoomerButton) findViewById(R.id.btnLogin);
        btnFbLogin = (IjoomerButton) findViewById(R.id.btnFbLogin);
        btnSignUp = (IjoomerTextView) findViewById(R.id.btnSignUp);
        btnForgetPassword = (IjoomerTextView) findViewById(R.id.btnForgetPassword);
        btnForgetUserName = (IjoomerTextView) findViewById(R.id.btnForgetUserName);
        globalConfiguration = new IjoomerGlobalConfiguration(IjoomerLoginActivity.this);

    }

    @Override
    public void prepareViews() {

        btnSignUp.setText(Html.fromHtml(getString(R.string.create_account)));
        btnForgetPassword.setText(Html.fromHtml(getString(R.string.forget_password)));
        btnForgetUserName.setText(Html.fromHtml(getString(R.string.forget_username)));
        btnSignUp.setMovementMethod(new LinkMovementMethod());
        edtUserName.setText(getSmartApplication().readSharedPreferences().getString(SP_USERNAME, ""));
        edtPassword.setText(getSmartApplication().readSharedPreferences().getString(SP_PASSWORD, ""));
        if (!IjoomerGlobalConfiguration.isAllowRegistration()) {
            // btnSignUp.setVisibility(View.GONE);
        }

    }

    @Override
    public void setActionListeners() {
        btnForgetUserName.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showForgetUserNameDialog();
            }
        });

        btnForgetPassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showForgetPasswordDialog();
            }
        });
        btnSignUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View paramView) {
                loadNew(IjoomerRegistrationStep1Activity.class, IjoomerLoginActivity.this, false);

            }
        });

        btnLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                getSmartApplication().writeSharedPreferences(SP_USERNAME, edtUserName.getText().toString().trim());
                getSmartApplication().writeSharedPreferences(SP_PASSWORD, edtPassword.getText().toString().trim());

                boolean validationFlag = true;
                if (edtPassword.getText().toString().trim().length() <= 0) {
                    edtPassword.setError(getString(R.string.validation_value_required));
                    validationFlag = false;
                }
                if (edtUserName.getText().toString().trim().length() <= 0) {
                    edtUserName.setError(getString(R.string.validation_value_required));
                    validationFlag = false;
                }
                if (validationFlag) {
                    IjoomerOauth.getInstance(IjoomerLoginActivity.this).authenticateUser(edtUserName.getText().toString().trim(), edtPassword.getText().toString().trim(), new WebCallListener() {
                        final SeekBar proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.authenticating_user));

                        @Override
                        public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
                            if (responseCode == 200) {
                                globalConfiguration.loadGlobalConfiguration(new WebCallListener() {

                                    @Override
                                    public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
                                        proSeekBar.setProgress(100);
                                        if (responseCode == 200) {
                                            try {

                                                if (getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY, "").length() > 0) {

                                                    try {
                                                        Intent intent = new Intent();
                                                        intent.setClassName(IjoomerLoginActivity.this, getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY, ""));
                                                        intent.putExtra("IN_OBJ",getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY_INTENT,""));
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        getSmartApplication().writeSharedPreferences(SP_LAST_ACTIVITY, "");
                                                        startActivity(intent);
                                                        getSmartApplication().writeSharedPreferences(SP_ISLOGOUT, false);
                                                    } catch (Exception e) {
                                                        try {
                                                            loadNew(IjoomerHomeActivity.class, IjoomerLoginActivity.this, true, "IN_USERID", "0");
                                                        } catch (Throwable e1) {
                                                            e1.printStackTrace();
                                                        }
                                                    } finally {
                                                        finish();
                                                    }
                                                } else {
                                                    if (getSmartApplication().readSharedPreferences().getString(SP_DEFAULT_LANDING_SCREEN, "").length() > 0) {
                                                        try {
                                                            Intent intent = new Intent();
                                                            intent.setClassName(IjoomerLoginActivity.this, getSmartApplication().readSharedPreferences().getString(SP_DEFAULT_LANDING_SCREEN, ""));
                                                            intent.putExtra("IN_OBJ",getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY_INTENT,""));
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(intent);
                                                        } catch (Exception e) {
                                                            try {
                                                                loadNew(IjoomerHomeActivity.class, IjoomerLoginActivity.this, true, "IN_USERID", "0");
                                                            } catch (Throwable e1) {
                                                                e1.printStackTrace();
                                                            }
                                                        } finally {
                                                            finish();
                                                        }

                                                    }else{
                                                        try {
                                                            loadNew(IjoomerHomeActivity.class, IjoomerLoginActivity.this, true, "IN_USERID", "0");
                                                        } catch (Throwable e1) {
                                                            e1.printStackTrace();
                                                        }
                                                    }
                                                }
                                            } catch (Throwable e) {
                                                e.printStackTrace();
                                            }
                                            getSmartApplication().writeSharedPreferences(SP_ISLOGOUT, false);
                                        } else {
                                            responseMessageHandler(responseCode, true);
                                        }
                                    }

                                    @Override
                                    public void onProgressUpdate(int progressCount) {
                                        if (progressCount > 90) {
                                            try {
                                                proSeekBar.setProgress(progressCount);
                                            } catch (Throwable e) {
                                            }
                                        }
                                    }
                                });

                            } else {
                                proSeekBar.setProgress(100);
                                responseMessageHandler(responseCode, true);
                            }
                        }

                        @Override
                        public void onProgressUpdate(int progressCount) {
                            if (progressCount < 91) {
                                proSeekBar.setProgress(progressCount);
                            }
                        }
                    });
                }

            }
        });

        btnFbLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), FacebookLoginActivity.class);
                startActivityForResult(intent, FACEBOOK_LOGIN);
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent("clearStackActivity");
        intent.setType("text/plain");
        sendBroadcast(intent);
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == FACEBOOK_LOGIN) {
                try {
                    checkFacebookUser(new JSONObject(data.getStringExtra("IN_FACEBOOK_USER")));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Class methods
     */

    /**
     * This method used to login using facebook user.
     * @param data facebook user data
     */
    private void checkFacebookUser(final JSONObject data) {

        if (data.has("errorMessage")) {
            try {
                IjoomerUtilities.getCustomOkDialog(getString(R.string.login), data.getString("errorMessage"), getString(R.string.ok), R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

                    @Override
                    public void NeutralMethod() {
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }

        } else {
            final SeekBar progressBar = IjoomerUtilities.getLoadingDialog("Connecting through facebook...");
            IjoomerOauth.getInstance(IjoomerLoginActivity.this).authenticateUserWithFacebook(data, new WebCallListener() {

                @Override
                public void onProgressUpdate(int progressCount) {
                    progressBar.setProgress(progressCount);
                }

                @Override
                public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
                    if (responseCode == 703) {
                        showUserSelectionDialog(data);
                    } else if (responseCode == 200) {
                        try {
                            if (getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY, "").length() > 0) {

                                try {
                                    Intent intent = new Intent();
                                    intent.setClassName(IjoomerLoginActivity.this, getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY, ""));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("IN_OBJ",getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY_INTENT,""));
                                    getSmartApplication().writeSharedPreferences(SP_LAST_ACTIVITY, "");
                                    startActivity(intent);
                                    getSmartApplication().writeSharedPreferences(SP_ISLOGOUT, false);
                                    getSmartApplication().writeSharedPreferences(SP_ISFACEBOOKLOGIN, true);
                                } catch (Exception e) {
                                    try {
                                        loadNew(IjoomerHomeActivity.class, IjoomerLoginActivity.this, true, "IN_USERID", "0");
                                    } catch (Throwable e1) {
                                        e1.printStackTrace();
                                    }
                                } finally {
                                    finish();
                                }

                            } else {
                                if (getSmartApplication().readSharedPreferences().getString(SP_DEFAULT_LANDING_SCREEN, "").length() > 0) {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setClassName(IjoomerLoginActivity.this, getSmartApplication().readSharedPreferences().getString(SP_DEFAULT_LANDING_SCREEN, ""));
                                        intent.putExtra("IN_OBJ",getSmartApplication().readSharedPreferences().getString(SP_LAST_ACTIVITY_INTENT,""));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        try {
                                            loadNew(IjoomerHomeActivity.class, IjoomerLoginActivity.this, true, "IN_USERID", "0");
                                        } catch (Throwable e1) {
                                            e1.printStackTrace();
                                        }
                                    } finally {
                                        finish();
                                    }
                                }else{
                                    try {
                                        loadNew(IjoomerHomeActivity.class, IjoomerLoginActivity.this, true, "IN_USERID", "0");
                                    } catch (Throwable e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        getSmartApplication().writeSharedPreferences(SP_ISLOGOUT, false);
                        getSmartApplication().writeSharedPreferences(SP_ISFACEBOOKLOGIN, true);
                    } else {
                        if (responseCode != 200) {
                            responseMessageHandler(responseCode, true);
                        }
                    }
                }
            });
        }

    }

    /**
     * This method used to shown response message.
     * @param responseCode represented response code
     * @param finishActivityOnConnectionProblem represented finish activity on connection problem
     */
    private void responseMessageHandler(final int responseCode, final boolean finishActivityOnConnectionProblem) {

        IjoomerUtilities.getCustomOkDialog(getString(R.string.login), getString(getResources().getIdentifier("code" + responseCode, "string", getPackageName())), getString(R.string.ok), R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

            @Override
            public void NeutralMethod() {
            }
        });

    }

}