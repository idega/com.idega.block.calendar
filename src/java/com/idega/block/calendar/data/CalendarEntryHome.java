package com.idega.block.calendar.data;


import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.data.IDOHome;

public interface CalendarEntryHome extends IDOHome {

	public CalendarEntry create() throws CreateException;

	public CalendarEntry findByPrimaryKey(Object pk) throws FinderException;

	public CalendarEntry createLegacy();

	public CalendarEntry findByPrimaryKey(int id) throws FinderException;

	public CalendarEntry findByPrimaryKeyLegacy(int id) throws SQLException;
	
	public Collection findDayEntries(Date date, int[] categoryIDs) throws FinderException;

}