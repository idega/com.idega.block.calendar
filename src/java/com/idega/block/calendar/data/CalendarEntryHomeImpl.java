package com.idega.block.calendar.data;


import java.sql.SQLException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.idega.data.IDOFactory;

public class CalendarEntryHomeImpl extends IDOFactory implements CalendarEntryHome {

	public Class getEntityInterfaceClass() {
		return CalendarEntry.class;
	}

	public CalendarEntry create() throws CreateException {
		return (CalendarEntry) super.createIDO();
	}

	public CalendarEntry findByPrimaryKey(Object pk) throws FinderException {
		return (CalendarEntry) super.findByPrimaryKeyIDO(pk);
	}

	public CalendarEntry createLegacy() {
		try {
			return create();
		}
		catch (CreateException ce) {
			throw new RuntimeException(ce.getMessage());
		}
	}

	public CalendarEntry findByPrimaryKey(int id) throws FinderException {
		return (CalendarEntry) super.findByPrimaryKeyIDO(id);
	}

	public CalendarEntry findByPrimaryKeyLegacy(int id) throws SQLException {
		try {
			return findByPrimaryKey(id);
		}
		catch (FinderException fe) {
			throw new SQLException(fe.getMessage());
		}
	}
		
	public java.util.Collection findDayEntries(Date date, int[] categoryIDs) throws FinderException {
		com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
		java.util.Collection ids = ((CalendarEntryBMPBean)entity).ejbFindDayEntries(date, categoryIDs);
		this.idoCheckInPooledEntity(entity);
		return this.getEntityCollectionForPrimaryKeys(ids);
	}
}