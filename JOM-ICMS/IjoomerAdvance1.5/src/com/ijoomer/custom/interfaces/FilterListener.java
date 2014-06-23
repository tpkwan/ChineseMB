package com.ijoomer.custom.interfaces;

import java.util.ArrayList;

import com.ijoomer.common.classes.FilterItem;

/**
 * This Interface Contains All Method Related To FilterListener.
 * 
 * @author tasol
 * 
 */
public interface FilterListener {

	public void onFilterApply(ArrayList<FilterItem> filteredItems);

}
