//idega 2001 - Laddi

package com.idega.block.calendar.data;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import javax.ejb.FinderException;

import com.idega.block.calendar.business.CalendarBusiness;
import com.idega.block.category.data.Category;
import com.idega.block.category.data.CategoryEntityBMPBean;
import com.idega.block.text.data.LocalizedText;
import com.idega.data.GenericEntity;
import com.idega.data.query.AND;
import com.idega.data.query.InCriteria;
import com.idega.data.query.MatchCriteria;
import com.idega.data.query.OR;
import com.idega.data.query.SelectQuery;
import com.idega.data.query.Table;
import com.idega.user.data.UserBMPBean;
import com.idega.util.IWTimestamp;

public class CalendarEntryBMPBean extends CategoryEntityBMPBean implements CalendarEntry {

	public CalendarEntryBMPBean() {
		super();
	}

	public CalendarEntryBMPBean(int id) throws SQLException {
		super(id);
	}

	@Override
	public void insertStartData() throws Exception {
		CalendarBusiness.initializeCalendarEntry();
	}

	@Override
	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addAttribute(getColumnNameEntryTypeID(), "Type", true, true, Integer.class, "many-to-one", CalendarEntryType.class);
		addAttribute(getColumnNameEntryDate(), "Date", true, true, Timestamp.class);
		addAttribute(getColumnNameEntryEndDate(), "End date", true, true, Timestamp.class);
		addAttribute(getColumnNameUserID(), "User", true, true, Integer.class);
		addAttribute(getColumnNameGroupID(), "Group", true, true, Integer.class);
		addAttribute(getColumnNameAllDayEvent(), "All day event", Boolean.class);
		addAttribute(getColumnNameRepeatsEveryYear(), "Repeats every year", Boolean.class);
		addManyToManyRelationShip(LocalizedText.class);

		setNullable(getColumnNameEntryTypeID(), false);
		setNullable(getColumnNameEntryDate(), false);
	}

	public static String getEntityTableName() {
		return "CA_CALENDAR";
	}

	public static String getColumnNameCalendarID() {
		return "CA_CALENDAR_ID";
	}

	public static String getColumnNameEntryTypeID() {
		return CalendarEntryTypeBMPBean.getColumnNameCalendarTypeID();
	}

	public static String getColumnNameEntryDate() {
		return "ENTRY_DATE";
	}

	public static String getColumnNameEntryEndDate() {
		return "ENTRY_END_DATE";
	}

	public static String getColumnNameAllDayEvent() {
		return "ALL_DAY_EVENT";
	}

	public static String getColumnNameRepeatsEveryYear() {
		return "REPEAT_EVERY_YEAR";
	}

	public static String getColumnNameUserID() {
		return UserBMPBean.getColumnNameUserID();
	}

	public static String getColumnNameGroupID() {
		return com.idega.core.data.GenericGroupBMPBean.getColumnNameGroupID();
	}

	@Override
	public String getIDColumnName() {
		return getColumnNameCalendarID();
	}

	@Override
	public String getEntityName() {
		return getEntityTableName();
	}

	// GET
	public Category getCategory() {
		return (Category) getColumnValue(getColumnCategoryId());
	}

	public CalendarEntryType getEntryType() {
		return (CalendarEntryType) getColumnValue(getColumnNameEntryTypeID());
	}

	public int getEntryTypeID() {
		return getIntColumnValue(getColumnNameEntryTypeID());
	}

	public Timestamp getDate() {
		return (Timestamp) getColumnValue(getColumnNameEntryDate());
	}

	public Timestamp getEndDate() {
		return (Timestamp) getColumnValue(getColumnNameEntryEndDate());
	}

	public int getUserID() {
		return getIntColumnValue(getColumnNameUserID());
	}

	public int getGroupID() {
		return getIntColumnValue(getColumnNameGroupID());
	}
	
	public boolean isAllDayEvent() {
		return getBooleanColumnValue(getColumnNameAllDayEvent(), false);
	}
	
	public boolean isRepeatEveryYear() {
		return getBooleanColumnValue(getColumnNameRepeatsEveryYear(), false);
	}

	// SET
	public void setEntryTypeID(int entryTypeID) {
		setColumn(getColumnNameEntryTypeID(), entryTypeID);
	}

	public void setDate(Timestamp date) {
		setColumn(getColumnNameEntryDate(), date);
	}

	public void setEndDate(Timestamp date) {
		setColumn(getColumnNameEntryEndDate(), date);
	}

	public void setUserID(int userID) {
		setColumn(getColumnNameUserID(), userID);
	}

	public void setGroupID(int groupID) {
		setColumn(getColumnNameGroupID(), groupID);
	}
	
	public void setAllDayEvent(boolean allDayEvent) {
		setColumn(getColumnNameAllDayEvent(), allDayEvent);
	}
	
	public void setRepeatEveryYear(boolean repeatEveryYear) {
		setColumn(getColumnNameRepeatsEveryYear(), repeatEveryYear);
	}

	// DELETE
	@Override
	public void delete() throws SQLException {
		removeFrom(GenericEntity.getStaticInstance(LocalizedText.class));
		super.delete();
	}

	public static CalendarEntry getStaticInstance() {
		return (CalendarEntry) GenericEntity.getStaticInstance(CalendarEntry.class);
	}
	
	public Collection ejbFindDayEntries(Date date, int[] categoryIDs) throws FinderException {
		Table table = new Table(this);
		
		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new InCriteria(table.getColumn(getColumnCategoryId()), categoryIDs));
		
		IWTimestamp stamp = new IWTimestamp(date);
		
		AND and1 = new AND(new MatchCriteria(table.getColumn(getColumnNameEntryDate()), MatchCriteria.LIKE, "%" + stamp.getDateString("-MM-dd") + "%"), new MatchCriteria(table.getColumn(getColumnNameRepeatsEveryYear()), MatchCriteria.EQUALS, true));
		OR or1 = new OR(and1, new MatchCriteria(table.getColumn(getColumnNameEntryDate()), MatchCriteria.LIKE, stamp.getDateString("yyyy-MM-dd") + "%"));
		
		AND and2 = new AND(new MatchCriteria(table.getColumn(getColumnNameEntryDate()), MatchCriteria.LESSEQUAL, stamp.getDateString("yyyy-MM-dd")), new MatchCriteria(table.getColumn(getColumnNameEntryEndDate()), MatchCriteria.GREATEREQUAL, stamp.getDateString("yyyy-MM-dd")));
		OR or2 = new OR(or1, and2);
		
		query.addCriteria(or2);
		
		return idoFindPKsByQuery(query);
	}
	
	public Collection ejbFindRepeatedEntries(int[] categoryIDs) throws FinderException {
		Table table = new Table(this);
		
		SelectQuery query = new SelectQuery(table);
		query.addColumn(table, getIDColumnName());
		query.addCriteria(new InCriteria(table.getColumn(getColumnCategoryId()), categoryIDs));
		query.addCriteria(new MatchCriteria(table.getColumn(getColumnNameRepeatsEveryYear()), MatchCriteria.EQUALS, true));
		
		return idoFindPKsByQuery(query);
	}
}