package com.idega.block.calendar.presentation;

/**
 * Title: Calendar Description: idegaWeb Calendar (Block) Copyright: Copyright
 * (c) 2001 Company: idega
 * 
 * @author Laddi
 * @version 1.0
 */

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.idega.idegaweb.block.presentation.Builderaware;
import com.idega.idegaweb.presentation.*;
import com.idega.block.calendar.business.CalendarBusiness;
import com.idega.block.calendar.business.CalendarFinder;
import com.idega.block.calendar.data.CalendarCategory;
import com.idega.block.calendar.data.CalendarEntry;
import com.idega.block.category.business.CategoryFinder;
import com.idega.block.category.presentation.CategoryBlock;
import com.idega.core.builder.data.ICPage;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.util.IWTimestamp;

public class Calendar extends CategoryBlock implements Builderaware {
	
	public static final String CACHE_KEY = "calendar_cache";

	private int _timeStyle = IWTimestamp.SHORT;

	private int _dateStyle = IWTimestamp.SHORT;

	private boolean hasEdit = false, hasAdd = false, hasPref = false;

	private int _iLocaleID;

	private int _view = CalendarParameters.MONTH;

	private IWTimestamp _stamp;

	private String _width = "100%";

	private boolean _isSelectedDay = false;

	private Integer _daysAhead = null; // = 7;

	private Integer _daysBack = null; // = 7;

	private int _numberOfShown = 4;

	private String _bodyColor = "#000000";

	private String _headlineColor = "#000000";

	private String _dateColor = "#000000";

	private String _actionDay = "#660000";

	private ICPage _page;

	private boolean _showMonth = false;

	private boolean _showMonthButton = false;

	private boolean _asLineView = false;

	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.calendar";

	protected IWResourceBundle _iwrb;

	protected IWBundle _iwb;

	protected IWBundle _iwbCalendar;

	private String AddPermission = "add";

	private String PrePermission = "pref";

	private int iUserId = -1;

	private static final String HEADLINE_STYLE_NAME = "headline";

	private static final String BODY_STYLE_NAME = "body";

	private static final String DATE_STYLE_NAME = "date";

	private String CATEGORY_STYLE_NAME = "category";

	private String HEADLINE_STYLE = "font-family: Arial,Helvetica,sans-serif; font-size: 11px; font-weight: bold;";

	private String BODY_STYLE = "font-family: Arial,Helvetica,sans-serif; font-size: 10px;";

	private String DATE_STYLE = "font-family: Arial,Helvetica,sans-serif; font-size: 10px; font-weight: bold;";

	private String CATEGORY_STYLE = "font-family: Arial,Helvetica,sans-serif; font-size: 10px; font-weight: bold; color: #666666;";

	private String headlineStyleName;

	private String bodyStyleName;

	private String dateStyleName;

	private String categoryStyleName;

	private int _spaceBetween = 16;

	private int iCellpadding = 2;

	private boolean _showCategory;

	private boolean showBodyText = true;

	private Image iNextImage;

	private Image iPreviousImage;

	private String iInactiveDayStyleClass;

	private String iActiveDayStyleClass;

	private String iHeaderTextStyleClass;
	
	private String iMonthTextStyleClass;

	public Calendar() {
		// _stamp = IWTimestamp.getTimestampRightNow();
		setCacheable(getCacheKey(), (20 * 60 * 1000));
	}

	public Calendar(IWTimestamp timestamp) {
		this._stamp = timestamp;
		setCacheable(getCacheKey(), (20 * 60 * 1000));
	}

	public String getCacheKey() {
		return CACHE_KEY;
	}
	
	protected String getCacheState(IWContext iwc, String cacheStatePrefix) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(cacheStatePrefix);
		
		if (iwc.isParameterSet(CalendarParameters.PARAMETER_VIEW)) {
			buffer.append(iwc.getParameter(CalendarParameters.PARAMETER_VIEW));
		}
		if (iwc.isParameterSet(CalendarParameters.PARAMETER_DAY)) {
			buffer.append(iwc.getParameter(CalendarParameters.PARAMETER_DAY));
		}
		if (iwc.isParameterSet(CalendarParameters.PARAMETER_MONTH)) {
			buffer.append(iwc.getParameter(CalendarParameters.PARAMETER_MONTH));
		}
		if (iwc.isParameterSet(CalendarParameters.PARAMETER_YEAR)) {
			buffer.append(iwc.getParameter(CalendarParameters.PARAMETER_YEAR));
		}

		return cacheStatePrefix;
	}

	public void registerPermissionKeys() {
		registerPermissionKey(this.AddPermission);
		registerPermissionKey(this.PrePermission);
	}

	public String getCategoryType() {
		return ((com.idega.block.calendar.data.CalendarCategoryHome) com.idega.data.IDOLookup.getHomeLegacy(CalendarCategory.class)).createLegacy().getCategoryType();
	}

	public boolean getMultible() {
		return true;
	}

	public void _main(IWContext iwc) throws Exception {
		super._main(iwc);
	}

	public void main(IWContext iwc) throws Exception {
		super.main(iwc);
		this._iwrb = getResourceBundle(iwc);
		this._iwb = iwc.getIWMainApplication().getBundle(IW_CORE_BUNDLE_IDENTIFIER);
		this._iwbCalendar = getBundle(iwc);

		if (this.headlineStyleName == null) {
			this.headlineStyleName = getStyleName(HEADLINE_STYLE_NAME);
		}
		if (this.bodyStyleName == null) {
			this.bodyStyleName = getStyleName(BODY_STYLE_NAME);
		}
		if (this.dateStyleName == null) {
			this.dateStyleName = getStyleName(DATE_STYLE_NAME);
		}
		if (this.categoryStyleName == null) {
			this.categoryStyleName = getStyleName(this.CATEGORY_STYLE_NAME);
		}

		this.iUserId = iwc.getUserId();

		this.hasEdit = iwc.hasEditPermission(this);
		this.hasAdd = iwc.hasPermission(this.AddPermission, this);
		this.hasPref = iwc.hasPermission(this.PrePermission, this);

		this._iLocaleID = ICLocaleBusiness.getLocaleId(iwc.getCurrentLocale());

		if (iwc.getParameter(CalendarParameters.PARAMETER_VIEW) != null) {
			this._view = Integer.parseInt(iwc.getParameter(CalendarParameters.PARAMETER_VIEW));
		}

		if (this._stamp == null) {
			String day = iwc.getParameter(CalendarParameters.PARAMETER_DAY);
			String month = iwc.getParameter(CalendarParameters.PARAMETER_MONTH);
			String year = iwc.getParameter(CalendarParameters.PARAMETER_YEAR);
			this._stamp = CalendarBusiness.getTimestamp(day, month, year);
		}

		this._isSelectedDay = CalendarBusiness.getIsSelectedDay(iwc);

		switch (this._view) {
			case CalendarParameters.DAY:
				drawDay(iwc);
				break;
			case CalendarParameters.MONTH:
				drawMonth(iwc);
				break;
			case CalendarParameters.YEAR:
				drawYear(iwc);
				break;
			case CalendarParameters.AHEAD_VIEW:
				drawAheadView(iwc);
				break;
		}
	}

	private void getParameter(IWContext iwc) {
		String string = iwc.getParameter(CalendarParameters.PARAMETER_SHOW_CALENDAR);
		if (string != null && string.equalsIgnoreCase(CalendarParameters.PARAMETER_TRUE)) {
			this._showMonth = true;
			iwc.setSessionAttribute(CalendarParameters.PARAMETER_SHOW_CALENDAR, CalendarParameters.PARAMETER_TRUE);
		}
		else if (string != null && string.equalsIgnoreCase(CalendarParameters.PARAMETER_FALSE)) {
			this._showMonth = false;
			iwc.removeSessionAttribute(CalendarParameters.PARAMETER_SHOW_CALENDAR);
		}
		else if (string == null) {
			if (iwc.getSessionAttribute(CalendarParameters.PARAMETER_SHOW_CALENDAR) != null) {
				this._showMonth = true;
			}
		}
	}

	private void drawDay(IWContext iwc) {
		getParameter(iwc);

		Table outerTable = new Table();
		outerTable.setCellpaddingAndCellspacing(0);
		if (this._width != null) {
			outerTable.setWidth(this._width);
		}
		outerTable.setWidth(1, Table.HUNDRED_PERCENT);

		Table entriesTable = new Table();
		entriesTable.setWidth(Table.HUNDRED_PERCENT);
		outerTable.add(entriesTable, 1, 1);

		String[] localeStrings = null;
		Text headlineText = null;
		Text bodyText = null;
		IWTimestamp startDate = null;
		IWTimestamp endDate = null;
		boolean hasImage = true;
		int imageID;
		int ypos = 1;

		// /// Permisson Area ////////////
		boolean buttonsAdded = false;
		if (this.hasAdd || this.hasEdit) {
			entriesTable.add(getAddIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasPref || this.hasEdit) {
			entriesTable.add(getPropertiesIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasEdit) {
			entriesTable.add(getCategoryIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (buttonsAdded) {
			ypos++;
		// ///////////////////////////////
		}

		int numberOfShown = 0;

		List entries = null;
		if (this._isSelectedDay) {

			entries = CalendarFinder.getInstance().listOfEntries(this._stamp, getCategoryIds());
			if (entries != null) {
				numberOfShown = entries.size();
			}
		}
		else {
			if (this._daysAhead != null || this._daysBack != null) {
				entries = CalendarFinder.getInstance().listOfWeekEntries(this._stamp, this._daysAhead.intValue(), this._daysBack.intValue(), getCategoryIds());
			}
			else {
				entries = CalendarFinder.getInstance().listOfNextEntries(getCategoryIds());
			}
			if (entries != null) {
				if (entries.size() > this._numberOfShown) {
					numberOfShown = this._numberOfShown;
				}
				else {
					numberOfShown = entries.size();
				}
			}
		}

		if (entries != null) {
			CalendarEntry entry;
			for (int a = 0; a < numberOfShown; a++) {
				Image typeImage = null;
				entry = (CalendarEntry) entries.get(a);
				localeStrings = CalendarFinder.getInstance().getEntryStrings(entry, this._iLocaleID);
				imageID = CalendarFinder.getInstance().getImageID(entry.getEntryTypeID());

				if (imageID != -1) {
					try {
						typeImage = new Image(imageID);
						typeImage.setHorizontalSpacing(3);
					}
					catch (Exception e) {
						typeImage = null;
					}
				}
				if (typeImage == null) {
					typeImage = this._iwbCalendar.getImage("shared/day_dot.gif");
				}

				if (localeStrings != null) {
					if (localeStrings[0] != null) {
						headlineText = new Text(localeStrings[0]);
					}
					else {
						headlineText = null;
					}

					if (localeStrings[1] != null) {
						bodyText = new Text(localeStrings[1]);
					}
					else {
						bodyText = null;
					}
				}

				int xpos = 1;

				if (headlineText != null) {
					if (typeImage != null) {
						typeImage.setName(CalendarFinder.getInstance().getEntryTypeName(entry.getEntryTypeID(), this._iLocaleID));
						entriesTable.add(typeImage, xpos, ypos);
						entriesTable.setWidth(xpos, ypos, "1");
						hasImage = true;
						xpos++;
					}

					headlineText.setFontStyle("font-family: Arial,Helvetica,sans-serif; font-size: 11px; font-weight: bold; color: " + this._headlineColor + ";");
					entriesTable.add(headlineText, xpos, ypos);

					startDate = new IWTimestamp(entry.getDate());
					
					DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, iwc.getCurrentLocale());
					Date fromDate = new Date(startDate.getTimestamp().getTime());
					
					
					Text fromDateText = new Text(format.format(fromDate));
					
					fromDateText.setFontStyle("font-family: Arial,Helvetica,sans-serif; font-size: 10px; color: " + this._dateColor + ";");
					
					
					xpos++;
					entriesTable.setAlignment(xpos, ypos, "right");
					entriesTable.add(fromDateText, xpos, ypos);
					
					if(entry.getEndDate() != null) {
						endDate = new IWTimestamp(entry.getEndDate());
						Date toDate = new Date(endDate.getTimestamp().getTime());
						Text toDateText = new Text(format.format(toDate));
						toDateText.setFontStyle("font-family: Arial,Helvetica,sans-serif; font-size: 10px; color: " + this._dateColor + ";");
						
						xpos++;
						entriesTable.setAlignment(xpos, ypos, "right");
						entriesTable.add(toDateText, xpos, ypos);
					}

					// Checking permissions
					if (this.hasEdit || this.hasPref || this.iUserId == entry.getUserID()) {
						xpos++;
						entriesTable.add(getEditButtons(entry.getID()), xpos, ypos);
					}

					if (bodyText != null && bodyText.getText().length() > 0) {
						ypos++;
						bodyText.setFontStyle("font-family: Arial,Helvetica,sans-serif; font-size: 11px; color: " + this._bodyColor + ";");
						if (hasImage) {
							entriesTable.mergeCells(2, ypos, entriesTable.getColumns(), ypos);
							entriesTable.add(bodyText, 2, ypos);
						}
						else {
							entriesTable.mergeCells(1, ypos, entriesTable.getColumns(), ypos);
							entriesTable.add(bodyText, 1, ypos);
						}
					}

					ypos++;
					entriesTable.setHeight(ypos++, 12);
				}
			}
		}
		else {
			headlineText = new Text(this._iwrb.getLocalizedString("no_entries", "No entries in calendar"));
			headlineText.setFontStyle("font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 11px; font-weight: bold; color: " + this._headlineColor + ";");
			entriesTable.add(headlineText, 1, 2);
			ypos++;
		}
		if (buttonsAdded) {
			entriesTable.mergeCells(1, 1, entriesTable.getColumns(), 1);
		}

		if (this._showMonth) {
			SmallCalendar cal = getCalendar(this._stamp);
			cal.setICObjectInstanceID(this.getICObjectInstanceID());
			cal.setWidth(100);
			outerTable.setWidth(2, 1, "6");
			outerTable.setAlignment(3, 1, Table.HORIZONTAL_ALIGN_RIGHT);
			outerTable.add(cal, 3, 1);
		}

		if (this._showMonthButton) {
			outerTable.mergeCells(1, 2, outerTable.getColumns(), 2);
			outerTable.setAlignment(1, 2, Table.HORIZONTAL_ALIGN_RIGHT);
			if (this._showMonth) {
				Link link = new Link(this._iwrb.getLocalizedImageButton("hide_month", "Hide month"));
				link.addParameter(CalendarParameters.PARAMETER_SHOW_CALENDAR, CalendarParameters.PARAMETER_FALSE);
				outerTable.add(link, 1, 2);
			}
			else {
				Link link = new Link(this._iwrb.getLocalizedImageButton("show_month", "Show month"));
				link.addParameter(CalendarParameters.PARAMETER_SHOW_CALENDAR, CalendarParameters.PARAMETER_TRUE);
				outerTable.add(link, 1, 2);
			}
		}
		outerTable.setColumnVerticalAlignment(1, Table.VERTICAL_ALIGN_TOP);

		add(outerTable);
	}

	private void drawAheadView(IWContext iwc) {
		Table outerTable = new Table();
		outerTable.setCellpaddingAndCellspacing(0);
		if (this._width != null) {
			outerTable.setWidth(this._width);
		}
		outerTable.setWidth(1, Table.HUNDRED_PERCENT);

		Table dateTable;

		Table entriesTable;

		String[] localeStrings = null;
		Text headlineText = null;
		Text bodyText = null;
		IWTimestamp stamp = null;
		int ypos = 1;

		// /// Permisson Area ////////////
		boolean buttonsAdded = false;
		if (this.hasAdd || this.hasEdit) {
			outerTable.add(getAddIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasPref || this.hasEdit) {
			outerTable.add(getPropertiesIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasEdit) {
			outerTable.add(getCategoryIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (buttonsAdded) {
			ypos++;
			outerTable.setHeight(ypos++, 3);
		}
		// ///////////////////////////////

		int numberOfShown = 0;

		List entries = CalendarFinder.getInstance().listOfNextEntries(getCategoryIds());

		if (entries != null) {
			if (entries.size() > this._numberOfShown) {
				numberOfShown = this._numberOfShown;
			}
			else {
				numberOfShown = entries.size();
			}

			CalendarEntry entry;
			for (int a = 0; a < numberOfShown; a++) {
				entriesTable = new Table();
				entriesTable.setWidth(Table.HUNDRED_PERCENT);
				entriesTable.setCellpaddingAndCellspacing(0);
				outerTable.add(entriesTable, 1, ypos++);

				dateTable = new Table(2, 1);
				dateTable.setWidth(Table.HUNDRED_PERCENT);
				dateTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
				dateTable.setCellpaddingAndCellspacing(0);

				if ((a + 1) < numberOfShown && this._spaceBetween > 0) {
					outerTable.setHeight(ypos++, this._spaceBetween);
				}

				entry = (CalendarEntry) entries.get(a);
				localeStrings = CalendarFinder.getInstance().getEntryStrings(entry, this._iLocaleID);

				if (localeStrings != null) {
					if (localeStrings[0] != null) {
						headlineText = new Text(localeStrings[0]);
						headlineText.setStyle(this.headlineStyleName);
					}
					else {
						headlineText = null;
					}

					if (localeStrings[1] != null) {
						bodyText = new Text(localeStrings[1]);
						bodyText.setStyle(this.bodyStyleName);
					}
					else {
						bodyText = null;
					}
				}
				int row = 1;
				if (headlineText != null) {
					stamp = new IWTimestamp(entry.getDate());
					Text dateText = new Text(stamp.getLocaleDateAndTime(iwc.getCurrentLocale(), this._dateStyle, this._timeStyle));
					dateText.setStyle(this.dateStyleName);
					Text category = new Text(CategoryFinder.getInstance().getCategory(entry.getCategoryId()).getName(iwc.getCurrentLocale()));
					category.setStyle(this.categoryStyleName);
					dateTable.add(dateText, 1, 1);
					if (this._showCategory) {
						dateTable.add(category, 2, 1);
					}
					entriesTable.add(dateTable, 1, row++);
					entriesTable.add(headlineText, 1, row++);

					if (bodyText != null && bodyText.getText().length() > 0 && this.showBodyText) {
						entriesTable.add(bodyText, 1, row++);
					}
					if (this.hasEdit || this.hasPref || this.iUserId == entry.getUserID()) {
						entriesTable.setHeight(row++, 3);
						entriesTable.add(getEditButtons(entry.getID()), 1, row);
					}
				}
			}
		}
		else {
			headlineText = new Text(this._iwrb.getLocalizedString("no_entries", "No entries in calendar"));
			headlineText.setFontStyle("font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 11px; font-weight: bold; color: " + this._headlineColor + ";");
			outerTable.add(headlineText, 1, ypos);
		}

		add(outerTable);
	}

	private void drawMonth(IWContext iwc) {
		SmallCalendar cal = getCalendar(this._stamp);
		cal.setAsLineView(this._asLineView);
		cal.setICObjectInstanceID(this.getICObjectInstanceID());

		if (this._width != null) {
			try {
				cal.setWidth(Integer.parseInt(this._width));
			}
			catch (NumberFormatException e) {
				cal.setWidth(110);
			}
		}

		Table monthTable = new Table();
		monthTable.setCellpadding(0);
		monthTable.setCellspacing(0);
		int ypos = 1;

		// /// Permisson Area ////////////
		boolean buttonsAdded = false;
		if (this.hasAdd || this.hasEdit) {
			monthTable.add(getAddIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasPref || this.hasEdit) {
			monthTable.add(getPropertiesIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasEdit) {
			monthTable.add(getCategoryIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (buttonsAdded) {
			ypos++;
		// ///////////////////////////////
		}

		monthTable.add(cal, 1, ypos);
		add(monthTable);
	}

	private SmallCalendar getCalendar(IWTimestamp stamp) {
		List list = CalendarFinder.getInstance().getMonthEntries(stamp, getCategoryIds());

		SmallCalendar calendar = new SmallCalendar(stamp);
		calendar.setDaysAsLink(true);
		calendar.setNextImage(this.iNextImage);
		calendar.setPreviousImage(this.iPreviousImage);
		if (this._page != null) {
			calendar.setPage(this._page);
		}
		if (this.iInactiveDayStyleClass != null) {
			calendar.setInactiveTextStyleClass(this.iInactiveDayStyleClass);
		}
		if (this.iHeaderTextStyleClass != null) {
			calendar.setHeaderFontStyleName(this.iHeaderTextStyleClass);
		}
		calendar.setCellpadding(this.iCellpadding);
		if (this.iMonthTextStyleClass != null) {
			calendar.setMonthTextStyleClass(this.iMonthTextStyleClass);
		}

		if (list != null) {
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				CalendarEntry entry = (CalendarEntry) iter.next();
				if (this.iActiveDayStyleClass != null) {
					calendar.setDayFontStyleClass(new IWTimestamp(entry.getDate()), this.iActiveDayStyleClass);
				}
				calendar.setDayColor(new IWTimestamp(entry.getDate()), this._actionDay);
			}
		}

		return calendar;
	}

	private void drawYear(IWContext iwc) {
		Table yearTable = new Table();
		IWTimestamp yearStamp = null;
		SmallCalendar calendar = null;
		int ypos = 1;
		int xpos = 1;

		// /// Permisson Area ////////////
		boolean buttonsAdded = false;
		if (this.hasEdit || this.hasAdd) {
			yearTable.add(getAddIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasEdit || this.hasPref) {
			yearTable.add(getPropertiesIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (this.hasEdit) {
			yearTable.add(getCategoryIcon(), 1, ypos);
			buttonsAdded = true;
		}
		if (buttonsAdded) {
			ypos++;
		// ///////////////////////////////
		}

		for (int a = 1; a <= 12; a++) {
			yearStamp = new IWTimestamp(this._stamp.getDay(), a, this._stamp.getYear());
			calendar = getCalendar(yearStamp);
			calendar.setICObjectInstanceID(this.getICObjectInstanceID());
			calendar.setOnlySelectedHighlighted(true);
			calendar.useNextAndPreviousLinks(false);
			calendar.setAsLineView(this._asLineView);

			yearTable.add(calendar, xpos, ypos);
			yearTable.setRowVerticalAlignment(ypos, "top");

			if (!this._asLineView) {
				xpos = xpos % 3 + 1;
				if (xpos == 1) {
					ypos++;
				}
			}
			else {
				ypos++;
			}
		}

		add(yearTable);
	}

	private Link getAddIcon() {
		Image image = this._iwb.getImage("shared/create.gif");
		Link link = new Link(image);
		link.setWindowToOpen(CalendarEditor.class);
		link.addParameter(CalendarParameters.PARAMETER_IC_CAT, getCategoryId());
		link.addParameter(CalendarParameters.PARAMETER_INSTANCE_ID, getICObjectInstanceID());
		if (this._isSelectedDay) {
			link.addParameter(CalendarParameters.PARAMETER_ENTRY_DATE, this._stamp.toSQLString());
		}
		return link;
	}

	private Link getPropertiesIcon() {
		Image image = this._iwb.getImage("shared/edit.gif", "Types");
		Link link = new Link(image);
		link.setWindowToOpen(CalendarTypeEditor.class);
		return link;
	}

	private Link getCategoryIcon() {
		Image image = this._iwb.getImage("shared/edit.gif", "Categories");
		Link link = getCategoryLink(((com.idega.block.calendar.data.CalendarCategoryHome) com.idega.data.IDOLookup.getHomeLegacy(CalendarCategory.class)).createLegacy().getCategoryType());
		link.setImage(image);
		// link.setWindowToOpen(CalendarTypeEditor.class);
		return link;
	}

	private Table getEditButtons(int entryID) {
		Table table = new Table(2, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);

		Image editImage = this._iwb.getImage("shared/edit.gif");
		Image deleteImage = this._iwb.getImage("shared/delete.gif");

		Link editLink = new Link(editImage);
		editLink.setWindowToOpen(CalendarEditor.class);
		editLink.addParameter(CalendarParameters.PARAMETER_ENTRY_ID, entryID);
		editLink.addParameter(CalendarParameters.PARAMETER_MODE, CalendarParameters.PARAMETER_MODE_EDIT);
		editLink.addParameter(CalendarParameters.PARAMETER_IC_CAT, getCategoryId());
		editLink.addParameter(CalendarParameters.PARAMETER_INSTANCE_ID, getICObjectInstanceID());
		table.add(editLink, 1, 1);
		Link deleteLink = new Link(deleteImage);
		deleteLink.setWindowToOpen(ConfirmDeleteWindow.class);
		deleteLink.addParameter(ConfirmDeleteWindow.PRM_DELETE_ID, entryID);
		deleteLink.addParameter(ConfirmDeleteWindow.PRM_DELETE, CalendarParameters.PARAMETER_TRUE);
		table.add(deleteLink, 2, 1);

		return table;
	}

	public void setView(int view) {
		this._view = view;
	}

	public void setWidth(int width) {
		this._width = Integer.toString(width);
	}

	public void setWidth(String width) {
		this._width = width;
	}

	public void setNumberOfShown(int numberOfShown) {
		this._numberOfShown = numberOfShown;
	}

	public void setDaysAhead(int daysAhead) {
		this._daysAhead = new Integer(daysAhead);
	}

	public void setDaysBack(int daysBack) {
		this._daysBack = new Integer(daysBack);
	}

	public void setHeadlineColor(String headlineColor) {
		this._headlineColor = headlineColor;
	}

	public void setBodyColor(String bodyColor) {
		this._bodyColor = bodyColor;
	}

	public void setDateColor(String dateColor) {
		this._dateColor = dateColor;
	}

	public void setInActiveDayColor(String color) {
	}

	public void setActiveDayColor(String color) {
		this._actionDay = color;
	}

	public void setAsLineView(boolean line) {
		this._asLineView = line;
	}

	public void setDate(int year, int month, int day) {
		if (this._stamp == null) {
			this._stamp = new IWTimestamp(day, month, year);
		}
		else {
			this._stamp.setDay(day);
			this._stamp.setMonth(month);
			this._stamp.setYear(year);
		}
	}

	public void setPage(ICPage page) {
		this._page = page;
	}

	public void setShowMonthButton(boolean showButton) {
		this._showMonthButton = showButton;
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	public boolean deleteBlock(int ICObjectInstanceID) {
		return CalendarBusiness.deleteBlock(getICObjectInstanceID());
	}

	/**
	 * @see com.idega.presentation.Block#getStyleNames()
	 */
	public Map getStyleNames() {
		HashMap map = new HashMap();
		String[] styleNames = { HEADLINE_STYLE_NAME, BODY_STYLE_NAME, DATE_STYLE_NAME, this.CATEGORY_STYLE_NAME };
		String[] styleValues = { this.HEADLINE_STYLE, this.BODY_STYLE, this.DATE_STYLE, this.CATEGORY_STYLE };

		for (int a = 0; a < styleNames.length; a++) {
			map.put(styleNames[a], styleValues[a]);
		}

		return map;
	}

	/**
	 * Sets the spaceBetween.
	 * 
	 * @param spaceBetween
	 *          The spaceBetween to set
	 */
	public void setSpaceBetween(int spaceBetween) {
		this._spaceBetween = spaceBetween;
	}

	/**
	 * Sets the showCategory.
	 * 
	 * @param showCategory
	 *          The showCategory to set
	 */
	public void setShowCategory(boolean showCategory) {
		this._showCategory = showCategory;
	}

	/**
	 * Sets the dateStyle.
	 * 
	 * @param dateStyle
	 *          The dateStyle to set
	 */
	public void setDateStyle(int dateStyle) {
		this._dateStyle = dateStyle;
	}

	/**
	 * Sets the timeStyle.
	 * 
	 * @param timeStyle
	 *          The timeStyle to set
	 */
	public void setTimeStyle(int timeStyle) {
		this._timeStyle = timeStyle;
	}

	/**
	 * @param bodyStyleName
	 *          The bodyStyleName to set.
	 */
	public void setBodyStyleName(String bodyStyleName) {
		this.bodyStyleName = bodyStyleName;
	}

	/**
	 * @param categoryStyleName
	 *          The categoryStyleName to set.
	 */
	public void setCategoryStyleName(String categoryStyleName) {
		this.categoryStyleName = categoryStyleName;
	}

	/**
	 * @param dateStyleName
	 *          The dateStyleName to set.
	 */
	public void setDateStyleName(String dateStyleName) {
		this.dateStyleName = dateStyleName;
	}

	/**
	 * @param headlineStyleName
	 *          The headlineStyleName to set.
	 */
	public void setHeadlineStyleName(String headlineStyleName) {
		this.headlineStyleName = headlineStyleName;
	}

	/**
	 * @param showBodyText
	 *          The showBodyText to set.
	 */
	public void setShowBodyText(boolean showBodyText) {
		this.showBodyText = showBodyText;
	}

	public void setNextImage(Image nextImage) {
		this.iNextImage = nextImage;
	}

	public void setPreviousImage(Image previousImage) {
		this.iPreviousImage = previousImage;
	}

	public void setActiveDayStyleClass(String activeDayStyleClass) {
		this.iActiveDayStyleClass = activeDayStyleClass;
	}

	public void setInactiveDayStyleClass(String inactiveDayStyleClass) {
		this.iInactiveDayStyleClass = inactiveDayStyleClass;
	}

	public void setHeaderTextStyleClass(String headerTextStyleClass) {
		this.iHeaderTextStyleClass = headerTextStyleClass;
	}

	public void setCellpadding(int cellpadding) {
		this.iCellpadding = cellpadding;
	}
	public void setMonthTextStyleClass(String monthTextStyleClass) {
		this.iMonthTextStyleClass = monthTextStyleClass;
	}
}