// idega 2001 - Laddi

package com.idega.block.calendar.data;

import java.sql.SQLException;

import com.idega.block.text.data.LocalizedText;
import com.idega.data.GenericEntity;

public class CalendarEntryTypeBMPBean extends com.idega.data.GenericEntity implements com.idega.block.calendar.data.CalendarEntryType {

	public CalendarEntryTypeBMPBean() {
		super();
	}

	public CalendarEntryTypeBMPBean(int id) throws SQLException {
		super(id);
	}

	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addAttribute(getColumnNameImageID(), "ICFileID", true, true, Integer.class);
		addManyToManyRelationShip(LocalizedText.class, "CA_CALENDAR_TYPE_LOCALIZED_TEXT");
	}

	public static String getEntityTableName() {
		return "CA_CALENDAR_TYPE";
	}

	public static String getColumnNameCalendarTypeID() {
		return "CA_CALENDAR_TYPE_ID";
	}

	public static String getColumnNameImageID() {
		return "IC_FILE_ID";
	}

	public String getIDColumnName() {
		return getColumnNameCalendarTypeID();
	}

	public String getEntityName() {
		return getEntityTableName();
	}

	public int getImageID() {
		return getIntColumnValue(getColumnNameImageID());
	}

	public void setImageID(int imageID) {
		setColumn(getColumnNameImageID(), imageID);
	}

	// DELETE
	public void delete() throws SQLException {
		removeFrom(GenericEntity.getStaticInstance(LocalizedText.class));
		super.delete();
	}
}