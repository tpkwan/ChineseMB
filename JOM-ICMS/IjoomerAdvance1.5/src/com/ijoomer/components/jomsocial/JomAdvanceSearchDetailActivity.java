package com.ijoomer.components.jomsocial;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.androidquery.AQuery;
import com.ijoomer.common.classes.ViewHolder;
import com.ijoomer.customviews.IjoomerTextView;
import com.ijoomer.src.R;
import com.smart.framework.ItemView;
import com.smart.framework.SmartListAdapterWithHolder;
import com.smart.framework.SmartListItem;

/**
 * This Class Contains All Method Related To JomAdvanceSearchDetailActivity.
 * 
 * @author tasol
 * 
 */
public class JomAdvanceSearchDetailActivity extends JomMasterActivity {

	private LinearLayout listFooter,topLayout,lnrSearch;
	private ListView lstFriend;
	private ListView lstMember;
	private IjoomerTextView txtFriend;
	private IjoomerTextView txtMember;
	private IjoomerTextView txtMap;
	private IjoomerTextView txtSearch;
	private IjoomerTextView txtPageOf;

	private AQuery androidQuery;
	private ArrayList<SmartListItem> listDataMember = new ArrayList<SmartListItem>();
	private ArrayList<HashMap<String, String>> IN_MEMBER_DATA;
	private SmartListAdapterWithHolder listAdapterWithHolderMember;



	/**
	 * Overrides method
	 */

	@Override
	public int setLayoutId() {
		return R.layout.jom_advance_search_list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initComponents() {

		listFooter = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.ijoomer_list_footer, null);
		topLayout = (LinearLayout) findViewById(R.id.topLayout);
		lnrSearch = (LinearLayout) findViewById(R.id.lnrSearch);
		lnrSearch.setVisibility(View.GONE);
		topLayout.setVisibility(View.GONE);
		lstMember = (ListView) findViewById(R.id.lstMember);
		lstFriend = (ListView) findViewById(R.id.lstFriend);
		lstFriend.addFooterView(listFooter);
		
		lstMember.addFooterView(listFooter);
		txtFriend = (IjoomerTextView) findViewById(R.id.txtFriend);
		txtFriend.setVisibility(View.GONE);
		txtMember = (IjoomerTextView) findViewById(R.id.txtMember);
		txtMember.setVisibility(View.VISIBLE);
		txtMap = (IjoomerTextView) findViewById(R.id.txtMap);
		txtMap.setVisibility(View.GONE);
		txtSearch = (IjoomerTextView) findViewById(R.id.txtSearch);
		txtSearch.setVisibility(View.GONE);
		txtPageOf = (IjoomerTextView) findViewById(R.id.txtPageOf);
		txtPageOf.setText(String.format(getString(R.string.page_of), 1, 1));

		androidQuery = new AQuery(this);
		IN_MEMBER_DATA = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("IN_MEMBER_DATA");
		prepareListMember(IN_MEMBER_DATA, false);
	}

	@Override
	public void prepareViews() {
		txtFriend.setTextColor(getResources().getColor(R.color.jom_blue));
	}


	@Override
	public void setActionListeners() {

		lstMember.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
				if (((HashMap<String, String>) ((SmartListItem) lstMember.getAdapter().getItem(pos)).getValues().get(0)).get(USER_PROFILE).equalsIgnoreCase("1")) {
					gotoProfile(((HashMap<String, String>) ((SmartListItem) lstMember.getAdapter().getItem(pos)).getValues().get(0)).get(USER_ID));
				}
			}
		});
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {

	}

	/**
	 * Class method
	 */

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
	 * This method used to prepare list for member data.
	 * @param data represented member data list
	 * @param append represented data append
	 */
	public void prepareListMember(ArrayList<HashMap<String, String>> data, boolean append) {
		if (data != null) {
			IN_MEMBER_DATA = data;
			if (!append) {
				listDataMember.clear();
			}
			for (HashMap<String, String> hashMap : data) {
				SmartListItem item = new SmartListItem();
				item.setItemLayout(R.layout.jom_friend_list_item);
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(hashMap);
				listAdapterWithHolderMember = getMemberListAdapter();
				item.setValues(obj);
				if (append) {
					listAdapterWithHolderMember.add(item);
				} else {
					listDataMember.add(item);
				}
			}
			lstMember.setAdapter(listAdapterWithHolderMember);
		}
	}

	

	/**
	 * List adapter for member.
	 */
	private SmartListAdapterWithHolder getMemberListAdapter() {

		SmartListAdapterWithHolder adapter = new SmartListAdapterWithHolder(JomAdvanceSearchDetailActivity.this, R.layout.jom_friend_list_item, listDataMember, new ItemView() {

			@Override
			public View setItemView(int position, View v, SmartListItem item, ViewHolder holder) {
				holder.friendmembertxtName = (IjoomerTextView) v.findViewById(R.id.txtName);
				holder.friendmemberImage = (ImageView) v.findViewById(R.id.imgFriend);
				holder.friendmemberimgOnlineStatus = (ImageView) v.findViewById(R.id.imgOnlineStatus);
				holder.friendmemberimgOnlineStatus.setImageDrawable(getResources().getDrawable(R.drawable.jom_friend_member_offline));

				@SuppressWarnings("unchecked")
				final HashMap<String, String> friend = (HashMap<String, String>) item.getValues().get(0);
				holder.friendmembertxtName.setText(friend.get(USER_NAME));
				androidQuery.id(holder.friendmemberImage).image(friend.get(USER_AVATAR), true, true, getDeviceWidth(), 0);
				if (friend.get(USER_ONLINE).equalsIgnoreCase("1")) {
					holder.friendmemberimgOnlineStatus.setImageDrawable(getResources().getDrawable(R.drawable.jom_friend_member_online));
				}

				return v;
			}

			@Override
			public View setItemView(int position, View v, SmartListItem item) {
				return null;
			}
		});
		return adapter;
	}

	

}
