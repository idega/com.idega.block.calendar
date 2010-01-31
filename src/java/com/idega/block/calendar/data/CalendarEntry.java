package com.idega.block.calendar.data;


import java.sql.Timestamp;

import com.idega.block.category.data.Category;
import com.idega.block.category.data.CategoryEntity;

public interface CalendarEntry extends CategoryEntity {

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#getCategory
	 */
	public Category getCategory();

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#getEntryType
	 */
	public CalendarEntryType getEntryType();

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#getEntryTypeID
	 */
	public int getEntryTypeID();

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#getDate
	 */
	public Timestamp getDate();

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#getEndDate
	 */
	public Timestamp getEndDate();

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#getUserID
	 */
	public int getUserID();

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#getGroupID
	 */
	public int getGroupID();

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#setEntryTypeID
	 */
	public void setEntryTypeID(int entryTypeID);

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#setDate
	 */
	public void setDate(Timestamp date);

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#setEndDate
	 */
	public void setEndDate(Timestamp date);

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#setUserID
	 */
	public void setUserID(int userID);

	/**
	 * @see com.idega.block.calendar.data.CalendarEntryBMPBean#setGroupID
	 */
	public void setGroupID(int groupID);
}