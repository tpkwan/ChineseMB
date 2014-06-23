package com.ijoomer.components.jomsocial;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.ijoomer.common.classes.IjoomerFileChooserActivity;
import com.ijoomer.common.classes.IjoomerShareActivity;
import com.ijoomer.common.classes.IjoomerUtilities;
import com.ijoomer.common.classes.ViewHolder;
import com.ijoomer.common.configuration.IjoomerApplicationConfiguration;
import com.ijoomer.custom.interfaces.AnnouncementAndDiscussionListner;
import com.ijoomer.customviews.IjoomerButton;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.library.jomsocial.JomGroupDataProvider;
import com.ijoomer.src.R;
import com.ijoomer.weservice.WebCallListener;
import com.smart.framework.CustomAlertMagnatic;
import com.smart.framework.CustomAlertNeutral;
import com.smart.framework.ItemView;
import com.smart.framework.SmartListAdapterWithHolder;
import com.smart.framework.SmartListItem;

/**
 * This Class Contains All Method Related To JomGroupAnnouncementDetailsActivity.
 * 
 * @author tasol
 * 
 */
public class JomGroupAnnouncementDetailsActivity extends JomMasterActivity {

	private LinearLayout listFooter;
	private LinearLayout lnrAnnouncementDetailsHeader;
	private ListView lstGroupFiles;
	private IjoomerTextView txtAnnouncementDetailsEdit;
	private IjoomerTextView txtAnnouncementDetailsRemove;
	private IjoomerTextView txtAnnouncementDetailsUploadFile;
	private IjoomerTextView txtAnnouncementDetailsTitle;
	private IjoomerTextView txtAnnouncementDetailsCreated;
	private IjoomerTextView txtAnnouncementDetailsDescription;
	private IjoomerTextView txtAnnouncementDetailsFiles;
	private IjoomerTextView txtAnnouncementDetailsShare;
	private ImageView imgAnnouncementDetailsAvatar;
	private ImageView imgTagClose;
	private ProgressBar pbrGroupFiles;
	private PopupWindow dialog;
	private Dialog dialogAnnouncementOrDiscussion = null;

	private AQuery androidQuery;
	private ArrayList<SmartListItem> fileListData = new ArrayList<SmartListItem>();
	private ArrayList<HashMap<String, String>> fileList;
	private HashMap<String, String> IN_ANNOUCEMENT_DETAILS;
	private HashMap<String, String> IN_GROUP_DETAILS;
	private SmartListAdapterWithHolder fileListAdapter;

	private JomGroupDataProvider provider;
	private JomGroupDataProvider fileDataProvider;

	final private int DOWNLOAD_FILE_LOCATION = 5;
	final private int UPLOAD_FILE_LOCATION = 4;
	private int downlodIndex = 0;

	/**
	 * Overrides methods
	 */
	@Override
	public int setLayoutId() {
		return R.layout.jom_group_announcement_details;
	}

	@Override
	public void initComponents() {

		listFooter = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.ijoomer_list_footer, null);
		lnrAnnouncementDetailsHeader = (LinearLayout) findViewById(R.id.lnrAnnouncementDetailsHeader);

		txtAnnouncementDetailsTitle = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsTitle);
		txtAnnouncementDetailsCreated = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsCreated);
		txtAnnouncementDetailsDescription = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsDescription);
		txtAnnouncementDetailsFiles = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsFiles);
		txtAnnouncementDetailsShare = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsShare);
		txtAnnouncementDetailsEdit = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsEdit);
		txtAnnouncementDetailsRemove = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsRemove);
		txtAnnouncementDetailsUploadFile = (IjoomerTextView) findViewById(R.id.txtAnnouncementDetailsUploadFile);
		imgAnnouncementDetailsAvatar = (ImageView) findViewById(R.id.imgAnnouncementDetailsAvatar);

		androidQuery = new AQuery(this);
		provider = new JomGroupDataProvider(this);
		fileDataProvider = new JomGroupDataProvider(this);

		getIntentData();
		setAnnouncementDetails();

	}

	@Override
	public void prepareViews() {
		txtAnnouncementDetailsDescription.setMovementMethod(new ScrollingMovementMethod());
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == DOWNLOAD_FILE_LOCATION) {
				final String path = data.getStringExtra("IN_PATH");
				final String fileName = fileList.get(downlodIndex).get(NAME);
				androidQuery.download(fileList.get(downlodIndex).get(URL), new File(path + fileName), new AjaxCallback<File>() {
					@Override
					public void callback(String url, File object, AjaxStatus status) {
						super.callback(url, object, status);
						if (status.getCode() == 200) {
							fileList.get(downlodIndex).put(HITS, "" + (Integer.parseInt(fileList.get(downlodIndex).get(HITS)) + 1));
							fileListAdapter.notifyDataSetChanged();
							provider.downloadFile(fileList.get(downlodIndex).get(ID), new WebCallListener() {
								final SeekBar proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.download));

								@Override
								public void onProgressUpdate(int progressCount) {
									proSeekBar.setProgress(progressCount);
								}

								@Override
								public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
									if (responseCode == 200) {
										IjoomerUtilities.getCustomOkDialog(getString(R.string.download), getString(R.string.alert_message_file_downloaded), getString(R.string.ok),
												R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

													@Override
													public void NeutralMethod() {
														MediaScannerConnection.scanFile(JomGroupAnnouncementDetailsActivity.this, new String[] { path + fileName }, null,
																new OnScanCompletedListener() {

																	@Override
																	public void onScanCompleted(String path, Uri uri) {

																	}
																});
													}
												});
									} else {
										responseErrorMessageHandler(responseCode, false);
									}
								}
							});

						} else {
							IjoomerUtilities.getCustomOkDialog(getString(R.string.download), status.getMessage(), getString(R.string.ok), R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

								@Override
								public void NeutralMethod() {

								}
							});
						}
					}
				});
			} else if (requestCode == UPLOAD_FILE_LOCATION) {
				final String path = data.getStringExtra("IN_PATH");
				onResume();
				provider.uploadAnnouncementFile(path, IN_ANNOUCEMENT_DETAILS.get(ID), new WebCallListener() {
					final SeekBar proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.upload));

					@Override
					public void onProgressUpdate(int progressCount) {
						proSeekBar.setProgress(progressCount);
					}

					@Override
					public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
						if (responseCode == 200) {
							IjoomerUtilities.getCustomOkDialog(getString(R.string.upload_file), getString(R.string.alert_message_file_uploaded), getString(R.string.ok), R.layout.ijoomer_ok_dialog,
									new CustomAlertNeutral() {

										@Override
										public void NeutralMethod() {
											IN_ANNOUCEMENT_DETAILS.put(FILES, "" + (Integer.parseInt(IN_ANNOUCEMENT_DETAILS.get(FILES)) + 1));
											txtAnnouncementDetailsFiles.setText(Integer.parseInt(IN_ANNOUCEMENT_DETAILS.get(FILES)) > 1 ? IN_ANNOUCEMENT_DETAILS.get(FILES) + " "
													+ getString(R.string.files) : IN_ANNOUCEMENT_DETAILS.get(FILES) + " " + getString(R.string.file));
										}
									});
						} else {
							responseErrorMessageHandler(responseCode, false);
						}
					}
				});
			}
		}
	}

	@Override
	public void setActionListeners() {

		txtAnnouncementDetailsUploadFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(JomGroupAnnouncementDetailsActivity.this, IjoomerFileChooserActivity.class);
				intent.putExtra("IN_ISOPENFILE", true);
				startActivityForResult(intent, UPLOAD_FILE_LOCATION);

			}
		});

		txtAnnouncementDetailsEdit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialogAnnouncementOrDiscussion = getAnnouncementOrDiscussionCreateDialog(getString(R.string.group_announcement), IN_ANNOUCEMENT_DETAILS.get(TITLE),
						IN_ANNOUCEMENT_DETAILS.get(MESSAGE), IN_ANNOUCEMENT_DETAILS.get(FILEPERMISSION), new AnnouncementAndDiscussionListner() {

							@Override
							public void onClick(final String title, final String message, final String allowMemberToUploadFile) {
								if (!IN_ANNOUCEMENT_DETAILS.get(TITLE).equals(title) || !IN_ANNOUCEMENT_DETAILS.get(MESSAGE).equals(message)
										|| !IN_ANNOUCEMENT_DETAILS.get(FILEPERMISSION).equals(allowMemberToUploadFile)) {
									provider.addOrEditGroupAnnouncement(IN_GROUP_DETAILS.get(ID), IN_ANNOUCEMENT_DETAILS.get(ID), title, message, allowMemberToUploadFile,

									new WebCallListener() {
										final SeekBar proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.dialog_loading_sending_request));

										@Override
										public void onProgressUpdate(int progressCount) {
											proSeekBar.setProgress(progressCount);
										}

										@Override
										public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
											if (responseCode == 200) {
												dialogAnnouncementOrDiscussion.dismiss();
												IjoomerApplicationConfiguration.setReloadRequired(true);
												IN_ANNOUCEMENT_DETAILS.put(TITLE, title);
												IN_ANNOUCEMENT_DETAILS.put(MESSAGE, message);
												txtAnnouncementDetailsTitle.setText(IN_ANNOUCEMENT_DETAILS.get(TITLE));
												txtAnnouncementDetailsDescription.setText(IN_ANNOUCEMENT_DETAILS.get(MESSAGE));
											} else {
												responseErrorMessageHandler(responseCode, false);
											}
										}
									});
								}
							}
						});
			}
		});

		txtAnnouncementDetailsRemove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				IjoomerUtilities.getCustomConfirmDialog(getString(R.string.group_title_announcement_remove), getString(R.string.are_you_sure), getString(R.string.yes), getString(R.string.no),
						new CustomAlertMagnatic() {

							@Override
							public void PositiveMethod() {
								provider.removeAnnouncement(IN_GROUP_DETAILS.get(ID), IN_ANNOUCEMENT_DETAILS.get(ID), new WebCallListener() {
									final SeekBar proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.dialog_loading_sending_request));

									@Override
									public void onProgressUpdate(int progressCount) {
										proSeekBar.setProgress(progressCount);
									}

									@Override
									public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
										if (responseCode == 200) {
											IjoomerApplicationConfiguration.setReloadRequired(true);
											finish();
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

		txtAnnouncementDetailsFiles.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!IN_ANNOUCEMENT_DETAILS.get(FILES).equals("0")) {
					showFileDialog();
				}
			}
		});

		txtAnnouncementDetailsShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					loadNew(IjoomerShareActivity.class, JomGroupAnnouncementDetailsActivity.this, false, "IN_SHARE_CAPTION", IN_ANNOUCEMENT_DETAILS.get(TITLE), "IN_SHARE_DESCRIPTION",
							IN_ANNOUCEMENT_DETAILS.get(MESSAGE), "IN_SHARE_THUMB", IN_ANNOUCEMENT_DETAILS.get(USER_AVATAR), "IN_SHARE_SHARELINK", IN_ANNOUCEMENT_DETAILS.get(SHARELINK));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});

		imgAnnouncementDetailsAvatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (IN_ANNOUCEMENT_DETAILS.get(USER_PROFILE).equals("1")) {
					gotoProfile(IN_ANNOUCEMENT_DETAILS.get(USER_ID));
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
	 * This method used to get intent data.
	 */
	@SuppressWarnings("unchecked")
	private void getIntentData() {
		IN_ANNOUCEMENT_DETAILS = (HashMap<String, String>) getIntent().getSerializableExtra("IN_ANNOUCEMENT_DETAILS") == null ? new HashMap<String, String>() : (HashMap<String, String>) getIntent()
				.getSerializableExtra("IN_ANNOUCEMENT_DETAILS");
		IN_GROUP_DETAILS = (HashMap<String, String>) getIntent().getSerializableExtra("IN_GROUP_DETAILS") == null ? new HashMap<String, String>() : (HashMap<String, String>) getIntent()
				.getSerializableExtra("IN_GROUP_DETAILS");
	}

	
	/**
	 * This method used to set announcement details.
	 */
	private void setAnnouncementDetails() {
		androidQuery.id(imgAnnouncementDetailsAvatar).image(IN_ANNOUCEMENT_DETAILS.get(USER_AVATAR), true, true, getDeviceWidth(), 0);
		txtAnnouncementDetailsTitle.setText(IN_ANNOUCEMENT_DETAILS.get(TITLE));
		txtAnnouncementDetailsCreated.setText(IN_ANNOUCEMENT_DETAILS.get(DATE) + " " + String.format(getString(R.string.by), IN_ANNOUCEMENT_DETAILS.get(USER_NAME)));
		txtAnnouncementDetailsDescription.setText(IN_ANNOUCEMENT_DETAILS.get(MESSAGE));
		txtAnnouncementDetailsFiles.setText(Integer.parseInt(IN_ANNOUCEMENT_DETAILS.get(FILES)) > 1 ? IN_ANNOUCEMENT_DETAILS.get(FILES) + " " + getString(R.string.files) : IN_ANNOUCEMENT_DETAILS
				.get(FILES) + " " + getString(R.string.file));

		if (IN_ANNOUCEMENT_DETAILS.get(FILEPERMISSION).equals("1") || IN_GROUP_DETAILS.get(ISADMIN).equals("1") || IN_GROUP_DETAILS.get(ISCOMMUNITYADMIN).equals("1")) {
			txtAnnouncementDetailsUploadFile.setVisibility(View.VISIBLE);
		}
		if (IN_GROUP_DETAILS.get(ISADMIN).equals("1") || IN_GROUP_DETAILS.get(ISCOMMUNITYADMIN).equals("1")) {
			txtAnnouncementDetailsEdit.setVisibility(View.VISIBLE);
			txtAnnouncementDetailsRemove.setVisibility(View.VISIBLE);
		}

		if (txtAnnouncementDetailsEdit.getVisibility() == View.GONE && txtAnnouncementDetailsRemove.getVisibility() == View.GONE && txtAnnouncementDetailsUploadFile.getVisibility() == View.GONE) {
			lnrAnnouncementDetailsHeader.setVisibility(View.GONE);
		}

	}

	/**
	 * This method used to shown response message.
	 * @param responseCode represented response code
	 * @param finishActivityOnConnectionProblem represented finish activity on connection problem 
	 */
	private void responseErrorMessageHandler(final int responseCode, final boolean finishActivityOnConnectionProblem) {
		IjoomerUtilities.getCustomOkDialog(getString(R.string.group_announcement), getString(getResources().getIdentifier("code" + responseCode, "string", getPackageName())), getString(R.string.ok),
				R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

					@Override
					public void NeutralMethod() {
						
					}
				});
	}

	
	/**
	 * This method used to show file dialog.
	 */
	private void showFileDialog() {
		try {
			int popupWidth = getDeviceWidth() - convertSizeToDeviceDependent(50);
			int popupHeight = getDeviceHeight() - convertSizeToDeviceDependent(200);

			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = layoutInflater.inflate(R.layout.jom_group_file_popup, null);

			dialog = new PopupWindow(this);
			dialog.setContentView(layout);
			dialog.setWidth(popupWidth);
			dialog.setHeight(popupHeight);
			dialog.setFocusable(true);
			dialog.setBackgroundDrawable(new BitmapDrawable(getResources()));
			dialog.showAtLocation(layout, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);

			imgTagClose = (ImageView) layout.findViewById(R.id.imgTagClose);
			lstGroupFiles = (ListView) layout.findViewById(R.id.lstGroupFiles);
			lstGroupFiles.addFooterView(listFooter, null, false);
			lstGroupFiles.setAdapter(null);
			pbrGroupFiles = (ProgressBar) layout.findViewById(R.id.pbrGroupFiles);
			pbrGroupFiles.setVisibility(View.GONE);
			loadFileList();

			imgTagClose.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			lstGroupFiles.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if ((firstVisibleItem + visibleItemCount) >= totalItemCount && totalItemCount > 1) {
						if (!fileDataProvider.isCalling() && fileDataProvider.hasNextPage()) {
							listFooterVisible();
							fileDataProvider.getGroupFileList(IN_GROUP_DETAILS.get(ID), new WebCallListener() {

								@Override
								public void onProgressUpdate(int progressCount) {

								}

								@Override
								public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
									listFooterInvisible();
									if (responseCode == 200) {
										fileList = data1;
										prepareFileList(true);
									} else {
										responseErrorMessageHandler(responseCode, false);
									}
								}
							});
						}
					}
				}
			});

		} catch (Throwable e) {
			e.printStackTrace();
		}
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
	 * This method used to load file list.
	 */
	private void loadFileList() {
		fileDataProvider.restorePagingSettings();
		pbrGroupFiles.setVisibility(View.VISIBLE);
		fileDataProvider.getAnouncementFileList(IN_ANNOUCEMENT_DETAILS.get(ID), new WebCallListener() {

			@Override
			public void onProgressUpdate(int progressCount) {

			}

			@Override
			public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
				pbrGroupFiles.setVisibility(View.GONE);
				if (responseCode == 200) {
					fileList = data1;
					prepareFileList(false);
					fileListAdapter = getFileListAdapter();
					lstGroupFiles.setAdapter(fileListAdapter);
				} else {
					IjoomerUtilities.getCustomOkDialog(getString(R.string.group_files), getString(getResources().getIdentifier("code" + responseCode, "string", getPackageName())),
							getString(R.string.ok), R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

								@Override
								public void NeutralMethod() {
									dialog.dismiss();
								}
							});
				}
			}
		});
	}

	
	/**
	 * This method used to prepare list file.
	 * @param append represented data append
	 */
	private void prepareFileList(boolean append) {
		if (fileList != null) {
			if (!append) {
				fileListData.clear();
			}
			for (HashMap<String, String> hashmap : fileList) {
				SmartListItem item = new SmartListItem();
				item.setItemLayout(R.layout.jom_group_upload_file_list_item);
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(hashmap);
				item.setValues(obj);
				if (append) {
					fileListAdapter.add(item);
				} else {
					fileListData.add(item);
				}
			}
		}
	}

	/** 
	 * List adapter for file.
	 */
	private SmartListAdapterWithHolder getFileListAdapter() {

		SmartListAdapterWithHolder adapterWithHolder = new SmartListAdapterWithHolder(this, R.layout.jom_group_upload_file_list_item, fileListData, new ItemView() {
			@Override
			public View setItemView(final int position, View v, SmartListItem item, final ViewHolder holder) {

				holder.txtFileTitle = (IjoomerTextView) v.findViewById(R.id.txtFileTitle);
				holder.txtFileHit = (IjoomerTextView) v.findViewById(R.id.txtFileHit);
				holder.txtFileSize = (IjoomerTextView) v.findViewById(R.id.txtFileSize);
				holder.txtFileDesc = (IjoomerTextView) v.findViewById(R.id.txtFileDesc);
				holder.btnFileRemove = (IjoomerButton) v.findViewById(R.id.btnFileRemove);
				@SuppressWarnings("unchecked")
				final HashMap<String, String> row = (HashMap<String, String>) item.getValues().get(0);
				holder.txtFileTitle.setText(row.get(NAME));
				
				holder.txtFileSize.setText(getString(R.string.size) + ":" + IjoomerUtilities.readableFileSize(Long.parseLong(row.get(SIZE))));
				holder.txtFileHit.setText(getString(R.string.hits) + ":" + row.get(HITS));
				holder.txtFileDesc.setText(String.format(getString(R.string.by), row.get(USER_NAME) + " " + row.get(DATE)));

				if (row.get(DELETEALLOWED).equals("1")) {
					holder.btnFileRemove.setVisibility(View.VISIBLE);
				} else {
					holder.btnFileRemove.setVisibility(View.GONE);
				}
				holder.txtFileTitle.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						downlodIndex = position;
						Intent intent = new Intent(JomGroupAnnouncementDetailsActivity.this, IjoomerFileChooserActivity.class);
						startActivityForResult(intent, DOWNLOAD_FILE_LOCATION);
					}
				});
				holder.btnFileRemove.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						provider.removeFile(row.get(ID), new WebCallListener() {
							final SeekBar proSeekBar = IjoomerUtilities.getLoadingDialog(getString(R.string.dialog_loading_sending_request));

							@Override
							public void onProgressUpdate(int progressCount) {
								proSeekBar.setProgress(progressCount);
							}

							@Override
							public void onCallComplete(int responseCode, String errorMessage, ArrayList<HashMap<String, String>> data1, Object data2) {
								if (responseCode == 200) {
									IjoomerUtilities.getCustomOkDialog(getString(R.string.group_files), getString(R.string.file_removed_successfully), getString(R.string.ok),
											R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

												@Override
												public void NeutralMethod() {
													fileListAdapter.remove(fileListAdapter.getItem(position));
												}
											});
								} else {
									IjoomerUtilities.getCustomOkDialog(getString(R.string.group_files), getString(getResources().getIdentifier("code" + responseCode, "string", getPackageName())),
											getString(R.string.ok), R.layout.ijoomer_ok_dialog, new CustomAlertNeutral() {

												@Override
												public void NeutralMethod() {
												}
											});
								}
							}
						});

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
