package com.idega.block.calendar.presentation;

/**
 * Title: Calendar Description: idegaWeb Calendar (Block) Copyright: Copyright
 * (c) 2001 Company: idega
 * 
 * @author Laddi
 * @version 1.0
 */

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.idega.block.calendar.business.CalendarBusiness;
import com.idega.block.calendar.business.CalendarComparator;
import com.idega.block.calendar.business.CalendarFinder;
import com.idega.block.calendar.data.CalendarCategory;
import com.idega.block.calendar.data.CalendarEntry;
import com.idega.block.calendar.data.CalendarEntryType;
import com.idega.block.category.data.Category;
import com.idega.block.category.presentation.CategoryBlock;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.block.presentation.Builderaware;
import com.idega.idegaweb.presentation.CalendarParameters;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.util.IWTimestamp;
import com.idega.util.StringHandler;

public class Calendar2 extends CategoryBlock implements Builderaware {
	
	public static final String CACHE_KEY = "calendar_cache";
	private static final String IW_BUNDLE_IDENTIFIER = "com.idega.block.calendar";
	private static final String AddPermission = "add";
	private static final String PrePermission = "pref";

	private String _id = null;
	private int _timeStyle = IWTimestamp.SHORT;
	private int _dateStyle = IWTimestamp.SHORT;
	private String _datePattern = null;
	private String _timePattern = null;
	private int _numberOfShown = 6;
	private boolean _showToday = false;

	private boolean hasEdit = false;
	private boolean hasAdd = false;
	private boolean hasPref = false;

	private int _iLocaleID;
	private int iUserId = -1;

	protected IWResourceBundle _iwrb;
	protected IWBundle _iwb;
	protected IWBundle _iwbCalendar;

	public Calendar2() {
		setCacheable(getCacheKey(), (20 * 60 * 1000));
	}

	@Override
	public String getCacheKey() {
		return CACHE_KEY;
	}
	
	@Override
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	@Override
	protected String getCacheState(IWContext iwc, String cacheStatePrefix) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(cacheStatePrefix);
		
		return buffer.toString();
	}

	@Override
	public void registerPermissionKeys() {
		registerPermissionKey(AddPermission);
		registerPermissionKey(PrePermission);
	}

	@Override
	public String getCategoryType() {
		return ((com.idega.block.calendar.data.CalendarCategoryHome) com.idega.data.IDOLookup.getHomeLegacy(CalendarCategory.class)).createLegacy().getCategoryType();
	}

	@Override
	public boolean getMultible() {
		return true;
	}

	@Override
	public void _main(IWContext iwc) throws Exception {
		super._main(iwc);
	}

	@Override
	public void main(IWContext iwc) throws Exception {
		super.main(iwc);
		this._iwrb = getResourceBundle(iwc);
		this._iwb = iwc.getIWMainApplication().getBundle(IW_CORE_BUNDLE_IDENTIFIER);
		this._iwbCalendar = getBundle(iwc);

		this.iUserId = iwc.getUserId();

		this.hasEdit = iwc.hasEditPermission(this);
		this.hasAdd = iwc.hasPermission(AddPermission, this);
		this.hasPref = iwc.hasPermission(PrePermission, this);

		this._iLocaleID = ICLocaleBusiness.getLocaleId(iwc.getCurrentLocale());

		drawAheadView(iwc);
	}

	private void drawAheadView(IWContext iwc) {
		Layer layer = new Layer();
		layer.setStyleClass("calendar");
		if (this._id  != null) {
			layer.setID(this._id);
		}
		
		// /// Permisson Area ////////////
		if (this.hasAdd || this.hasEdit || hasPref) {
			Layer adminLayer = new Layer();
			adminLayer.setStyleClass("calendarAdmin");
			layer.add(adminLayer);
			
			if (this.hasAdd || this.hasEdit) {
				adminLayer.add(getAddIcon());
			}
			if (this.hasPref || this.hasEdit) {
				adminLayer.add(getPropertiesIcon());
			}
			if (this.hasEdit) {
				adminLayer.add(getCategoryIcon());
			}
		}

		String[] localeStrings = null;
		Text headlineText = null;
		Text bodyText = null;
		IWTimestamp stamp = null;
		IWTimestamp endStamp = null;
		int numberOfShown = 0;
		IWTimestamp now = new IWTimestamp();

		List entries = null;
		if (this._showToday) {
			entries = CalendarFinder.getInstance().getDayEntries(getCategoryIds());
		}
		else {
			entries = CalendarFinder.getInstance().listOfNextEntries(getCategoryIds());
			if (entries == null) {
				entries = new ArrayList();
			}
			
			List<CalendarEntry> repeated = CalendarFinder.getInstance().getRepeatedEntries(getCategoryIds());
			for (CalendarEntry calendarEntry : repeated) {
				IWTimestamp date = new IWTimestamp(calendarEntry.getDate());
				calendarEntry.setEndDate(calendarEntry.getDate());
				date.setYear(now.getYear());
				
				if (date.getMonth() >= now.getMonth()) {
					if ((date.getMonth() == now.getMonth() && date.getDay() >= now.getDay()) || date.getMonth() > now.getMonth()) {
						calendarEntry.setDate(date.getTimestamp());
					}
					else {
						date.addYears(1);
						calendarEntry.setDate(date.getTimestamp());
					}
				}
				else {
					date.addYears(1);
					calendarEntry.setDate(date.getTimestamp());
				}
				
				if (!entries.contains(calendarEntry)) {
					entries.add(calendarEntry);
				}
			}
		}
		
		Collections.sort(entries, new CalendarComparator());
		
		if (entries != null) {
			if (entries.size() > this._numberOfShown) {
				numberOfShown = this._numberOfShown;
			}
			else {
				numberOfShown = entries.size();
			}

			CalendarEntry entry;
			for (int a = 0; a < numberOfShown; a++) {
				entry = (CalendarEntry) entries.get(a);
				Category category = entry.getCategory();
				CalendarEntryType type = entry.getEntryType();
				localeStrings = CalendarFinder.getInstance().getEntryStrings(entry, this._iLocaleID);
				stamp = new IWTimestamp(entry.getDate());
				endStamp = entry.getEndDate() != null ? new IWTimestamp(entry.getEndDate()) : null;

				Layer calendarEntry = new Layer();
				calendarEntry.setStyleClass("calendarEntry");
				calendarEntry.setStyleClass(StringHandler.stripNonRomanCharacters(category.getName()).toLowerCase());
				layer.add(calendarEntry);

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

				if (headlineText != null) {
					if (type.getImageID() > 0) {
						try {
							Image image = new Image(type.getImageID());
							image.setStyleClass("entryImage");
							calendarEntry.add(image);
						}
						catch (SQLException e) {
							e.printStackTrace();
						}
					}
					
					Layer startDate = new Layer();
					startDate.setStyleClass("startDate");
					calendarEntry.add(startDate);
					
					Layer date = new Layer();
					Text dateText = new Text(_datePattern != null ? stamp.getDateString(_datePattern) : stamp.getLocaleDate(iwc.getCurrentLocale(), this._dateStyle));
					date.add(dateText);
					date.setStyleClass("date");
					startDate.add(date);

					if (!entry.isAllDayEvent()) {
						Layer time = new Layer();
						Text timeText = new Text(_timePattern != null ? stamp.getDateString(_timePattern) : stamp.getLocaleTime(iwc.getCurrentLocale(), this._timeStyle));
						time.add(timeText);
						time.setStyleClass("time");
						startDate.add(time);
					}
					
					if (endStamp != null && !entry.isRepeatEveryYear()) {
						Layer endDate = new Layer();
						endDate.setStyleClass("endDate");
						calendarEntry.add(endDate);
						
						date = new Layer();
						dateText = new Text(" - " + (_datePattern != null ? endStamp.getDateString(_datePattern) : endStamp.getLocaleDate(iwc.getCurrentLocale(), this._dateStyle)));
						date.add(dateText);
						date.setStyleClass("date");
						endDate.add(date);

						if (!entry.isAllDayEvent()) {
							Layer time = new Layer();
							Text timeText = new Text(_timePattern != null ? endStamp.getDateString(_timePattern) : endStamp.getLocaleTime(iwc.getCurrentLocale(), this._timeStyle));
							time.add(timeText);
							time.setStyleClass("time");
							endDate.add(time);
						}
					}
					
					Layer headline = new Layer();
					headline.setStyleClass("event");
					headline.add(headlineText);
					calendarEntry.add(headline);
					
					if (entry.isRepeatEveryYear()) {
						Layer birthday = new Layer();
						birthday.setStyleClass("birthday");
						
						int age = stamp.getYear() - endStamp.getYear();
						String ageString = String.valueOf(age);
						
						String text = ageString + " ";
						if (ageString.substring(ageString.length() - 1).equals("1") && ageString.indexOf("11") == -1) {
							text += this._iwrb.getLocalizedString("year_old", "year old");
						}
						else {
							text += this._iwrb.getLocalizedString("years_old", "year old");
						}
						birthday.add(new Text(text));
						calendarEntry.add(birthday);
					}

					if (bodyText != null && bodyText.getText().length() > 0) {
						Layer body = new Layer();
						body.setStyleClass("eventBody");
						body.add(bodyText);
						calendarEntry.add(body);
					}

					if (this.hasEdit || this.hasPref || this.iUserId == entry.getUserID()) {
						calendarEntry.add(getEditButtons(entry.getID()));
					}
				}
			}
		}
		else {
			Layer noEntriesLayer = new Layer();
			noEntriesLayer.setStyleClass("noEntriesFound");
			layer.add(noEntriesLayer);
			
			Text text = new Text(this._iwrb.getLocalizedString("no_entries", "No entries in calendar"));
			noEntriesLayer.add(text);
		}

		add(layer);
	}

	private Link getAddIcon() {
		Image image = this._iwb.getImage("shared/create.gif");
		Link link = new Link(image);
		link.setWindowToOpen(CalendarEditor.class);
		link.addParameter(CalendarParameters.PARAMETER_IC_CAT, getCategoryId());
		link.addParameter(CalendarParameters.PARAMETER_INSTANCE_ID, getICObjectInstanceID());
		return link;
	}

	private Link getPropertiesIcon() {
		Image image = this._iwb.getImage("shared/text.gif", "Types");
		Link link = new Link(image);
		link.setWindowToOpen(CalendarTypeEditor.class);
		return link;
	}

	private Link getCategoryIcon() {
		Image image = this._iwb.getImage("shared/edit.gif", "Categories");
		Link link = getCategoryLink(((com.idega.block.calendar.data.CalendarCategoryHome) com.idega.data.IDOLookup.getHomeLegacy(CalendarCategory.class)).createLegacy().getCategoryType());
		link.setImage(image);
		return link;
	}

	private Layer getEditButtons(int entryID) {
		Layer layer = new Layer();
		layer.setStyleClass("entryAdministration");

		Image editImage = this._iwb.getImage("shared/edit.gif");
		Image deleteImage = this._iwb.getImage("shared/delete.gif");

		Link editLink = new Link(editImage);
		editLink.setStyleClass("edit");
		editLink.setWindowToOpen(CalendarEditor.class);
		editLink.addParameter(CalendarParameters.PARAMETER_ENTRY_ID, entryID);
		editLink.addParameter(CalendarParameters.PARAMETER_MODE, CalendarParameters.PARAMETER_MODE_EDIT);
		editLink.addParameter(CalendarParameters.PARAMETER_IC_CAT, getCategoryId());
		editLink.addParameter(CalendarParameters.PARAMETER_INSTANCE_ID, getICObjectInstanceID());
		layer.add(editLink);
		
		Link deleteLink = new Link(deleteImage);
		deleteLink.setStyleClass("delete");
		deleteLink.setWindowToOpen(ConfirmDeleteWindow.class);
		deleteLink.addParameter(ConfirmDeleteWindow.PRM_DELETE_ID, entryID);
		deleteLink.addParameter(ConfirmDeleteWindow.PRM_DELETE, CalendarParameters.PARAMETER_TRUE);
		layer.add(deleteLink);

		return layer;
	}

	/**
	 * Sets number of shown entries.
	 * @param numberOfShown	The numberOfShown to set
	 */
	public void setNumberOfShown(int numberOfShown) {
		this._numberOfShown = numberOfShown;
	}

	/**
	 * Sets the dateStyle.
	 * @param dateStyle	The dateStyle to set
	 */
	public void setDateStyle(int dateStyle) {
		this._dateStyle = dateStyle;
	}

	/**
	 * Sets the timeStyle.
	 * @param timeStyle	The timeStyle to set
	 */
	public void setTimeStyle(int timeStyle) {
		this._timeStyle = timeStyle;
	}
	
	/**
	 * Sets the dateStyle.
	 * @param dateStyle	The dateStyle to set
	 */
	public void setDatePattern(String pattern) {
		this._datePattern = pattern;
	}

	/**
	 * Sets the timeStyle.
	 * @param timeStyle	The timeStyle to set
	 */
	public void setTimePattern(String pattern) {
		this._timePattern = pattern;
	}
	
	/**
	 * Sets the calendar ID.
	 * @param id	The id to set
	 */
	public void setCalendarID(String id) {
		this._id = id;
	}
	
	/**
	 * Sets to show only today's entries
	 */
	public void setShowToday(boolean showToday) {
		this._showToday = showToday;
	}

	@Override
	public boolean deleteBlock(int ICObjectInstanceID) {
		return CalendarBusiness.deleteBlock(getICObjectInstanceID());
	}
}