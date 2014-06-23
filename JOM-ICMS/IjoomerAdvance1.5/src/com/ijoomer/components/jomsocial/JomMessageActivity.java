package com.ijoomer.components.jomsocial;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.androidquery.AQuery;
import com.ijoomer.common.classes.IjoomerUtilities;
import com.ijoomer.common.classes.ViewHolder;
import com.ijoomer.common.configuration.IjoomerApplicationConfiguration;
import com.ijoomer.customviews.IjoomerButton;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.library.jomsocial.JomMessageDataProvider;
import com.ijoomer.src.R;
import com.ijoomer.weservice.WebCallListener;
import com.smart.framework.CustomAlertMagnatic;
import com.smart.framework.CustomAlertNeutral;
import com.smart.framework.ItemView;
import com.smart.framework.SmartListAdapterWithHolder;
import com.smart.framework.SmartListItem;

/**
 * This Class Contains All Method Related To JomMessageActivity.
 * 
 * @author tasol
 * 
 */
public class JomMessageActivity extends JomMasterActivity implements JomTagHolder {

	private LinearLayout listFooter;
	private ListView lstMessage;
	private IjoomerButton btnCompose;
	private SeekBar proSeekBar;

	private AQuery androidQuery;
	private SmartListAdapterWithHolder lstMessageAdapter;
	private ArrayList<SmartListItem> listData;

	private JomMessageDataProvider providerMessage;
	private JomMessageDataProvider provider;

	/**
	 * Overrides methods
	 */

	@Override
	public int setLayoutId() {
		return R.layout.jom_message;
	}

	@Override
	public void initComponents() {

		listFooter = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.ijoomer_list_footer, null);
		lstMessage = (ListView) findViewById(R.id.lstMessage);
		lstMessage.addFooterView(listFooter, null, false);
		lstMessage.setAdapter(null);
		btnCompose = (IjoomerButton) findViewById(R.id.btnCompose);

		androidQuery = new AQuery(this);
		listData = new ArrayList<SmartListItem>();

		provider = new JomMessageDataProvider(this);
		providerMessage = new JomMessageDataProvider(this);
	}

	@Override
	public void prepareViews() {
		getMessageList(true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (IjoomerApplicationConfiguration.isReloadRequired()) {
			IjoomerApplicationConfiguration.setReloadRequired(false);
			getMessageList(false);
		}
	}

	@Override
	public void setActionListeners() {

		btnCompose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				loadNew(JomMessageComposeActivity.class, JomMessageActivity.this, false);
			}
		});

		lstMessage.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {

			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if ((firstVisibleItem + visibleItemCount) >= totalItemCount && totalItemCount > 1) {
					if (!providerMessage.isCalling() && providerMessage.hasNextPage()) {
						listFooterVisible();
						providerMessage.getMessageList(new WebCallListener() {

							@Override
							public void onProgressUpdate(int progressCount) {

							}

							@Override
							public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
								listFooterInvisible();
								if (responseCode == 200) {
									updateHeader(provider.getNotificationData());
									prepareList(data1, true);
								}
							}
						});
					}
				}

			}
		});

	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {

	}

	/**
	 * Class methods
	 */

	
	/**
	 * This method used to shown response message.
	 * @param responseCode represented response code
	 * @param finishActivityOnConnectionProblem represented finish activity on connection problem 
	 */
	private void responseErrorMessageHandler(final int responseCode, final boolean finishActivityOnConnectionProblem) {
		IjoomerUtilities.getCustomOkDialog(getString(R.string.message), getString(getResources().getIdentifier("code" + responseCode, "string", getPackageName())),
				getString(R.string.ok), R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

					@Override
					public void NeutralMethod() {
						
					}
				});
	}

	/**
	 * This method used to get message list.
	 * @param isDialogShow represented progress shown
	 */
	private void getMessageList(final boolean isDialogShow) {
		providerMessage.restorePagingSettings();
		if (isDialogShow) {
			proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.dialog_loading_sending_request));
		}
		providerMessage.getMessageList(new WebCallListener() {

			@Override
			public void onProgressUpdate(int progressCount) {
				if (isDialogShow) {
					proSeekBar.setProgress(progressCount);
				}
			}

			@Override
			public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
				lstMessage.setAdapter(null);
				if (responseCode == 200) {
					updateHeader(providerMessage.getNotificationData());
					prepareList(data1, false);
					lstMessageAdapter = getListAdapter();
					lstMessage.setAdapter(lstMessageAdapter);
				} else {
					responseErrorMessageHandler(responseCode, true);
				}
			}
		});
	}

	/**
	 * This method used to visible list footer
	 */
	public void listFooterVisible() {
		listFooter.setVisibility(View.VISIBLE);
	}

	/**
	 * This method used to gone list footer
	 */
	public void listFooterInvisible() {
		listFooter.setVisibility(View.GONE);
	}

	/**
	 * This method used to prepare list message.
	 * @param data represented message data
	 * @param append represented data append
	 */
	public void prepareList(ArrayList<HashMap<String, String>> data, boolean append) {
		if (data != null) {
			if (!append) {
				listData.clear();
			}
			for (HashMap<String, String> hashMap : data) {
				SmartListItem item = new SmartListItem();
				item.setItemLayout(R.layout.jom_message_list_item);
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(hashMap);
				item.setValues(obj);
				if (append) {
					lstMessageAdapter.add(item);
				} else {
					listData.add(item);
				}
			}

		}
	}

	/**
	 * List adapter for message
	 */

	private SmartListAdapterWithHolder getListAdapter() {
		SmartListAdapterWithHolder adapterWithHolder = new SmartListAdapterWithHolder(JomMessageActivity.this, R.layout.jom_message_list_item, listData, new ItemView() {
			@Override
			public View setItemView(final int position, View v, SmartListItem item, ViewHolder holder) {
				holder.lnrMessage = (LinearLayout) v.findViewById(R.id.lnrMessage);
				holder.imgMessageUser = (ImageView) v.findViewById(R.id.imgMessageUser);
				holder.txtMessageUserName = (IjoomerTextView) v.findViewById(R.id.txtMessageUserName);
				holder.txtMessageSubject = (IjoomerTextView) v.findViewById(R.id.txtMessageSubject);
				holder.txtMessageDate = (IjoomerTextView) v.findViewById(R.id.txtMessageDate);
				holder.btnMessageRemove = (IjoomerButton) v.findViewById(R.id.btnMessageRemove);
				holder.imgMessageIncoming = (ImageView) v.findViewById(R.id.imgMessageIncoming);
				holder.imgMessageOutgoing = (ImageView) v.findViewById(R.id.imgMessageOutgoing);

				holder.imgMessageIncoming.setVisibility(View.GONE);
				holder.imgMessageOutgoing.setVisibility(View.GONE);

				@SuppressWarnings("unchecked")
				final HashMap<String, String> row = (HashMap<String, String>) item.getValues().get(0);

				androidQuery.id(holder.imgMessageUser).image(row.get(USER_AVATAR), true, true, getDeviceWidth(), 0);
				holder.txtMessageSubject.setText(row.get(SUBJECT));
				holder.txtMessageUserName.setText(row.get(USER_NAME));
				holder.txtMessageDate.setText(row.get(DATE));

				if (row.get(OUTGOING).equals("1")) {
					holder.imgMessageOutgoing.setVisibility(View.VISIBLE);
				} else {
					holder.imgMessageIncoming.setVisibility(View.VISIBLE);
				}

				holder.imgMessageUser.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						if (row.get(USER_PROFILE).equals("1")) {
							gotoProfile(row.get(USER_ID));
						}
					}
				});
				holder.btnMessageRemove.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						IjoomerUtilities.getCustomConfirmDialog(getString(R.string.message_title_remove), getString(R.string.are_you_sure), getString(R.string.yes),
								getString(R.string.no), new CustomAlertMagnatic() {

									@Override
									public void PositiveMethod() {
										provider.removeMessage(row.get(ID), true, new WebCallListener() {
											@Override
											public void onProgressUpdate(int progressCount) {

											}

											@Override
											public void onCallComplete(final int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
												if (responseCode == 200) {
													updateHeader(provider.getNotificationData());
													lstMessageAdapter.remove(lstMessageAdapter.getItem(position));
												} else {
													responseErrorMessageHandler(responseCode, false);
												}
											}

										});
									}

									@Override
									public void NegativeMethod() {

									}

								});

					}
				});

				holder.lnrMessage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						try {
							loadNew(JomMessageDetailsActivity.class, JomMessageActivity.this, false, "IN_MESSAGE_DETAILS", row);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});

				return v;
			}

			@Override
			public View setItemView(int position, View v, SmartListItem item) {
				return null;
			}
		});
		return adapterWithHolder;
	}

}
