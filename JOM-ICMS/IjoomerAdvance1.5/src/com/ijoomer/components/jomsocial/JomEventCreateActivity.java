package com.ijoomer.components.jomsocial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.ijoomer.common.classes.IjoomerMapAddress;
import com.ijoomer.common.classes.IjoomerUtilities;
import com.ijoomer.common.classes.IjoomerUtilities.MyCustomAdapter;
import com.ijoomer.common.configuration.IjoomerApplicationConfiguration;
import com.ijoomer.custom.interfaces.CustomClickListner;
import com.ijoomer.customviews.IjoomerButton;
import com.ijoomer.customviews.IjoomerCheckBox;
import com.ijoomer.customviews.IjoomerEditText;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.library.jomsocial.JomEventDataProvider;
import com.ijoomer.src.R;
import com.ijoomer.weservice.WebCallListener;
import com.smart.framework.CustomAlertNeutral;

/**
 * This Class Contains All Method Related To JomEventCreateActivity.
 *
 * @author tasol
 *
 */
public class JomEventCreateActivity extends JomMasterActivity {

    private LinearLayout lnr_form;
    private IjoomerButton btnCancel;
    private IjoomerButton btnCreate;
    private IjoomerEditText editMap;

    ArrayList<HashMap<String, String>> IN_FIELD_LIST;
    private JomEventDataProvider dataProvider;

    final private String STARTDATE = "startdate";
    final private String ALLDAY = "allday";
    final private String REPEAT = "repeat";
    final private String OFFSET = "offset";
    final private String TICKET = "ticket";
    final private String REPEATEND = "repeatend";
    private String IN_EVENT_ID;
    private String IN_GROUP_ID;
    final private int GET_ADDRESS_FROM_MAP = 1;

    /**
     * Override methods
     */

    @Override
    public int setLayoutId() {
        return R.layout.jom_dynamic_view;
    }

    @Override
    public void initComponents() {
        lnr_form = (LinearLayout) findViewById(R.id.lnr_form);
        btnCancel = (IjoomerButton) findViewById(R.id.btnCancel);
        btnCreate = (IjoomerButton) findViewById(R.id.btnCreate);

        dataProvider = new JomEventDataProvider(this);

        getIntentData();
    }

    @Override
    public void prepareViews() {
        createForm();
        if (!IN_EVENT_ID.equals("0") || !IN_GROUP_ID.equals("0")) {
            btnCreate.setText(getString(R.string.save));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GET_ADDRESS_FROM_MAP) {
                editMap.setText(((HashMap<String, String>) data.getSerializableExtra("MAP_ADDRESSS_DATA")).get("address"));
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    @Override
    public void setActionListeners() {
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        btnCreate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                updateEventDetails();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Class methods
     */

    /**
     * This method used to get intent data.
     */
    @SuppressWarnings("unchecked")
    private void getIntentData() {
        IN_FIELD_LIST = ((ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("IN_FIELD_LIST")) == null ? new ArrayList<HashMap<String, String>>() : ((ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("IN_FIELD_LIST"));
        IN_EVENT_ID = getIntent().getStringExtra("IN_EVENT_ID") == null ? "0" : getIntent().getStringExtra("IN_EVENT_ID");
        IN_GROUP_ID = getIntent().getStringExtra("IN_GROUP_ID") == null ? "0" : getIntent().getStringExtra("IN_GROUP_ID");
    }

    /**
     * This method used to update event details.
     */
    @SuppressWarnings("unchecked")
    private void updateEventDetails() {

        boolean validationFlag = true;
        ArrayList<HashMap<String, String>> eventField = new ArrayList<HashMap<String, String>>();
        int size = lnr_form.getChildCount();
        for (int i = 0; i < size; i++) {
            View v = (LinearLayout) lnr_form.getChildAt(i);
            HashMap<String, String> field = new HashMap<String, String>();
            field.putAll((HashMap<String, String>) v.getTag());
            IjoomerEditText edtValue = null;
            Spinner spnrValue = null;
            IjoomerCheckBox chbValue = null;
            if (field != null) {
                if (field.get(TYPE).equals(TEXT)) {
                    edtValue = (IjoomerEditText) ((LinearLayout) v.findViewById(R.id.lnrEdit)).findViewById(R.id.txtValue);
                } else if (field.get(TYPE).equals(TEXTAREA)) {
                    edtValue = (IjoomerEditText) ((LinearLayout) v.findViewById(R.id.lnrEditArea)).findViewById(R.id.txtValue);
                } else if (field.get(TYPE).equals(DATETIME)) {
                    edtValue = (IjoomerEditText) ((LinearLayout) v.findViewById(R.id.lnrEditClickable)).findViewById(R.id.txtValue);
                } else if (field.get(TYPE).equals(MAP)) {
                    edtValue = (IjoomerEditText) ((LinearLayout) v.findViewById(R.id.lnrEditMap)).findViewById(R.id.txtValue);
                }

                if (field.get(TYPE).equals(CHECKBOX)) {
                    chbValue = (IjoomerCheckBox) ((LinearLayout) v.findViewById(R.id.lnrCheckbox)).findViewById(R.id.txtValue);
                    field.put(VALUE, chbValue.isChecked() ? "1" : "0");
                    if (field.get(NAME).toString().trim().equalsIgnoreCase(ALLDAY)) {
                        if (chbValue.isChecked()) {
                            int counter = 0;
                            for (HashMap<String, String> hashMap : eventField) {
                                if (hashMap.get(NAME).toString().trim().equalsIgnoreCase(STARTDATE) || hashMap.get(NAME).toString().trim().equalsIgnoreCase(ENDDATE)) {
                                    hashMap.put(VALUE, hashMap.get(VALUE).substring(0, hashMap.get(VALUE).indexOf(" ")));
                                    counter++;
                                    if (counter >= 2) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    eventField.add(field);
                } else if (field.get(TYPE).equals(SELECT)) {
                    if (field.get(NAME).equals(REPEAT)) {
                        final Spinner spn = (Spinner) ((LinearLayout) v.findViewById(R.id.lnrRepeatEvent)).findViewById(R.id.txtSpnValue);
                        final IjoomerEditText edt = (IjoomerEditText) ((LinearLayout) v.findViewById(R.id.lnrRepeatEvent)).findViewById(R.id.txtEdtValue);
                        try {
                            JSONArray options = new JSONArray(field.get(OPTIONS));

                            field.put(VALUE, ((JSONObject) options.get(spn.getSelectedItemPosition())).getString(VALUE));
                            eventField.add(field);

                            if (field.get(VALUE).toString().trim().length() > 0) {
                                HashMap<String, String> fieldRepeatnd = new HashMap<String, String>();
                                fieldRepeatnd.put(NAME, REPEATEND);
                                if (edt.getText().toString().trim().length() > 0) {
                                    fieldRepeatnd.put(VALUE, edt.getText().toString().trim());
                                    eventField.add(fieldRepeatnd);
                                } else {
                                    validationFlag = true;
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        spnrValue = (Spinner) ((LinearLayout) v.findViewById(R.id.lnrSpin)).findViewById(R.id.txtValue);
                        try {
                            JSONArray options = new JSONArray(field.get(OPTIONS));
                            field.put(VALUE, ((JSONObject) options.get(spnrValue.getSelectedItemPosition())).getString(VALUE));
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        eventField.add(field);

                    }
                }

                if (edtValue != null) {
                    if (field.get(REQUIRED).equals("1") && edtValue.getText().toString().length() <= 0) {
                        edtValue.setError(getString(R.string.validation_value_required));
                        validationFlag = false;
                    } else {
                        field.put(VALUE, edtValue.getText().toString().trim());
                        eventField.add(field);
                    }
                }

            }
        }

        if (validationFlag) {

            final SeekBar proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.dialog_loading_sending_request));
            dataProvider.addOrEditEventSubmit(IN_EVENT_ID, IN_GROUP_ID, eventField, new WebCallListener() {

                @Override
                public void onProgressUpdate(int progressCount) {
                    proSeekBar.setProgress(progressCount);
                }

                @Override
                public void onCallComplete(final int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
                    if (responseCode == 200) {
                        updateHeader(dataProvider.getNotificationData());
                        IjoomerApplicationConfiguration.setReloadRequired(true);
                        finish();
                    } else {
                        if(errorMessage!=null && errorMessage.length()>0 && !errorMessage.equals("null")){
                            IjoomerUtilities.getCustomOkDialog(getString(R.string.group),
                                    errorMessage, getString(R.string.ok), R.layout.ijoomer_ok_dialog,
                                    new CustomAlertNeutral() {

                                        @Override
                                        public void NeutralMethod() {

                                        }
                                    });
                        }else{
                            IjoomerUtilities.getCustomOkDialog(getString(R.string.event), getString(getResources().getIdentifier("code" + responseCode, "string", getPackageName())), getString(R.string.ok), R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

                                @Override
                                public void NeutralMethod() {
                                   
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    /**
     * This method used to create dynamic form for event.
     */
    private void createForm() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 10;

        LinearLayout layout = null;
        int size = IN_FIELD_LIST.size();
        for (int j = 0; j < size; j++) {
            final HashMap<String, String> field = IN_FIELD_LIST.get(j);
            View fieldView = inflater.inflate(R.layout.jom_dynamic_view_item, null);

            if (field.get(TYPE).equals(TEXT)) {
                final IjoomerEditText edit;
                layout = ((LinearLayout) fieldView.findViewById(R.id.lnrEdit));
                layout.setVisibility(View.VISIBLE);
                edit = ((IjoomerEditText) layout.findViewById(R.id.txtValue));
                edit.setText(Html.fromHtml(field.get(VALUE)));

                if (field.get(NAME).equalsIgnoreCase(TICKET)) {
                    edit.setHint(field.get(CAPTION) + " " + getString(R.string.ticket_unlimited));
                } else {
                    edit.setHint(field.get(CAPTION));
                }
            } else if (field.get(TYPE).equals(TEXTAREA)) {
                final IjoomerEditText edit;
                layout = ((LinearLayout) fieldView.findViewById(R.id.lnrEditArea));
                layout.setVisibility(View.VISIBLE);
                edit = ((IjoomerEditText) layout.findViewById(R.id.txtValue));
                edit.setText(Html.fromHtml(field.get(VALUE)));
                edit.setHint(field.get(CAPTION));

            } else if (field.get(TYPE).equals(SELECT)) {
                if (field.get(NAME).toString().trim().equalsIgnoreCase(REPEAT)) {
                    layout = ((LinearLayout) fieldView.findViewById(R.id.lnrRepeatEvent));
                    layout.setVisibility(View.VISIBLE);
                    final Spinner spn = ((Spinner) layout.findViewById(R.id.txtSpnValue));
                    final IjoomerEditText edt = ((IjoomerEditText) layout.findViewById(R.id.txtEdtValue));
                    final IjoomerTextView txt = ((IjoomerTextView) layout.findViewById(R.id.txtLable));
                    MyCustomAdapter adapter = IjoomerUtilities.getSpinnerAdapter(field);
                    spn.setAdapter(adapter);
                    spn.setSelection(adapter.getDefaultPosition());
                    edt.setVisibility(View.GONE);
                    txt.setVisibility(View.GONE);
                    txt.setText(" * ");
                    if (IN_FIELD_LIST.get(++j).get(VALUE).toString().trim().length() > 0) {
                        edt.setText(IN_FIELD_LIST.get(j).get(VALUE).toString().trim());
                    } else {
                        edt.setText("");
                    }
                    edt.setHint(getString(R.string.ends));
                    edt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(final View v) {

                            IjoomerUtilities.getDateDialog(((IjoomerEditText) v).getText().toString(), false, new CustomClickListner() {

                                @SuppressWarnings("unchecked")
                                @Override
                                public void onClick(String value) {
                                    ((IjoomerEditText) v).setText(value);
                                    ((IjoomerEditText) v).setError(null);
                                    int size = lnr_form.getChildCount();
                                    for (int i = 0; i < size; i++) {
                                        View v = (LinearLayout) lnr_form.getChildAt(i);
                                        HashMap<String, String> field = new HashMap<String, String>();
                                        field.putAll((HashMap<String, String>) v.getTag());
                                        if (field.get(TYPE).equals(DATETIME) && field.get(NAME).equalsIgnoreCase(ENDDATE)) {
                                            final IjoomerEditText edtValue = ((IjoomerEditText) ((LinearLayout) v.findViewById(R.id.lnrEditClickable)).findViewById(R.id.txtValue));
                                            String endDate = edtValue.getText().toString().trim().split(" ")[0];
                                            if (IjoomerUtilities.getDateFromString(value).compareTo(IjoomerUtilities.getDateFromString(endDate)) < 0) {
                                                edtValue.setText(value + " " + (edtValue.getText().toString().trim().split(" ")[1]));
                                                break;
                                            }

                                        }
                                    }
                                }
                            });

                        }
                    });
                    spn.setOnItemSelectedListener(new OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                            if (pos != 0) {
                                edt.setVisibility(View.VISIBLE);
                                txt.setVisibility(View.VISIBLE);
                            } else {
                                edt.setVisibility(View.GONE);
                                txt.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });

                } else {
                    layout = ((LinearLayout) fieldView.findViewById(R.id.lnrSpin));
                    layout.setVisibility(View.VISIBLE);
                    MyCustomAdapter adapter = IjoomerUtilities.getSpinnerAdapter(field);
                    ((Spinner) layout.findViewById(R.id.txtValue)).setAdapter(adapter);
                    ((Spinner) layout.findViewById(R.id.txtValue)).setSelection(adapter.getDefaultPosition());

                    if (field.get("value").toString().trim().length() <= 0) {
                        if (field.get(NAME).equalsIgnoreCase(OFFSET)) {

                            try {

                                int selectedIndex = 0;
                                JSONArray jsonArray = null;
                                TimeZone t = TimeZone.getDefault();
                                jsonArray = new JSONArray(field.get("options"));
                                int len = jsonArray.length();
                                for (int k = 0; k < len; k++) {
                                    JSONObject options = (JSONObject) jsonArray.get(k);

                                    if (options.getString(NAME).contains(t.getDisplayName(false, TimeZone.SHORT).split("\\+")[1])) {
                                        selectedIndex = k;
                                        break;
                                    }
                                }
                                ((Spinner) layout.findViewById(R.id.txtValue)).setSelection(selectedIndex);
                            } catch (Throwable e) {
                                e.printStackTrace();
                                ((Spinner) layout.findViewById(R.id.txtValue)).setSelection(0);
                            }
                        }

                    }
                }

            } else if (field.get(TYPE).equals(DATETIME)) {
                final IjoomerEditText edit;
                layout = ((LinearLayout) fieldView.findViewById(R.id.lnrEditClickable));
                layout.setVisibility(View.VISIBLE);
                edit = ((IjoomerEditText) layout.findViewById(R.id.txtValue));
                edit.setText(field.get(VALUE));
                edit.setHint(field.get(CAPTION));
                edit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        IjoomerUtilities.getDateTimeDialog(((IjoomerEditText) v).getText().toString(), new CustomClickListner() {

                            @SuppressWarnings("unchecked")
                            @Override
                            public void onClick(String value) {
                                ((IjoomerEditText) v).setText(value);
                                ((IjoomerEditText) v).setError(null);
                                if (field.get(NAME).equalsIgnoreCase(ENDDATE)) {
                                    int size = lnr_form.getChildCount();
                                    for (int i = 0; i < size; i++) {
                                        View v = (LinearLayout) lnr_form.getChildAt(i);
                                        HashMap<String, String> field = new HashMap<String, String>();
                                        field.putAll((HashMap<String, String>) v.getTag());
                                        if (field.get(TYPE).equals(SELECT) && field.get(NAME).equalsIgnoreCase(REPEAT)) {
                                            final IjoomerEditText edtValue = ((IjoomerEditText) ((LinearLayout) v.findViewById(R.id.lnrRepeatEvent)).findViewById(R.id.txtEdtValue));
                                            String repeatEndDate = edtValue.getText().toString().trim();
                                            String endDate = value.toString().trim().split(" ")[0];
                                            if (IjoomerUtilities.getDateFromString(endDate).compareTo(IjoomerUtilities.getDateFromString(repeatEndDate)) > 0) {
                                                edtValue.setText(endDate);
                                                break;
                                            }

                                        }
                                    }
                                }
                            }
                        });

                    }
                });

            } else if (field.get(TYPE).equals(MULTIPLESELECT)) {
                final IjoomerEditText edit;
                layout = ((LinearLayout) fieldView.findViewById(R.id.lnrEditClickable));
                layout.setVisibility(View.VISIBLE);
                edit = ((IjoomerEditText) layout.findViewById(R.id.txtValue));
                edit.setText(field.get(VALUE));
                edit.setHint(field.get(CAPTION));
                edit.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        IjoomerUtilities.getMultiSelectionDialog(field.get(CAPTION), field.get(OPTIONS), "", new CustomClickListner() {

                            @Override
                            public void onClick(String value) {
                                ((IjoomerEditText) v).setText(value);
                            }
                        });

                    }
                });

            } else if (field.get(TYPE).equals(MAP)) {
                final IjoomerEditText edit;
                final ImageView imgMap;
                layout = ((LinearLayout) fieldView.findViewById(R.id.lnrEditMap));
                layout.setVisibility(View.VISIBLE);
                edit = ((IjoomerEditText) layout.findViewById(R.id.txtValue));
                imgMap = ((ImageView) layout.findViewById(R.id.imgMap));
                edit.setText(field.get(VALUE));
                edit.setHint(field.get(CAPTION));

                if (field.get(VALUE).toString().trim().length() <= 0) {
                    try {
                        Address address = IjoomerUtilities.getAddressFromLatLong(0, 0);
                        edit.setText(address.getSubAdminArea());
                    } catch (Throwable e) {
                        edit.setText("");
                    }
                }
                imgMap.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        editMap = edit;
                        Intent intent = new Intent(JomEventCreateActivity.this, IjoomerMapAddress.class);
                        startActivityForResult(intent, GET_ADDRESS_FROM_MAP);
                    }
                });

            } else if (field.get(TYPE).equals(CHECKBOX)) {
                final IjoomerCheckBox chb;
                layout = ((LinearLayout) fieldView.findViewById(R.id.lnrCheckbox));
                layout.setVisibility(View.VISIBLE);
                chb = ((IjoomerCheckBox) layout.findViewById(R.id.txtValue));
                chb.setTextAppearance(JomEventCreateActivity.this, R.style.ijoomer_textview_h2);
                chb.setText(field.get(CAPTION));
                if (field.get(VALUE).toString().trim().length() > 0) {
                    chb.setChecked(field.get(VALUE).toString().equals("1") ? true : false);
                }

            }

            if (!field.get(NAME).toString().trim().equalsIgnoreCase(REPEAT)) {
                try {
                    if (field.get(REQUIRED).equalsIgnoreCase("1")) {
                        ((IjoomerTextView) layout.findViewById(R.id.txtLable)).setText("* ");
                    } else {
                        ((IjoomerTextView) layout.findViewById(R.id.txtLable)).setText("   ");
                    }
                } catch (Exception e) {
                    ((IjoomerTextView) layout.findViewById(R.id.txtLable)).setText("   ");
                }
            }

            fieldView.setTag(field);
            lnr_form.addView(fieldView, params);
        }

    }

}
