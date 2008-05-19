package com.idega.block.calendar.data;


import javax.ejb.CreateException;
import com.idega.data.IDOHome;
import java.sql.SQLException;
import javax.ejb.FinderException;

public interface CalendarEntryHome extends IDOHome {

	public CalendarEntry create() throws CreateException;

	public CalendarEntry findByPrimaryKey(Object pk) throws FinderException;

	public CalendarEntry createLegacy();

	public CalendarEntry findByPrimaryKey(int id) throws FinderException;

	public CalendarEntry findByPrimaryKeyLegacy(int id) throws SQLException;
}