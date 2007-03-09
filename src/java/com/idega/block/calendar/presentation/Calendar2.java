package com.idega.block.calendar.presentation;

/**
 * Title: Calendar Description: idegaWeb Calendar (Block) Copyright: Copyright
 * (c) 2001 Company: idega
 * 
 * @author Laddi
 * @version 1.0
 */

import java.util.List;

import com.idega.block.calendar.business.CalendarBusiness;
import com.idega.block.calendar.business.CalendarFinder;
import com.idega.block.calendar.data.CalendarCategory;
import com.idega.block.calendar.data.CalendarEntry;
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

	public String getCacheKey() {
		return CACHE_KEY;
	}
	
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	protected String getCacheState(IWContext iwc, String cacheStatePrefix) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(cacheStatePrefix);
		
		return buffer.toString();
	}

	public void registerPermissionKeys() {
		registerPermissionKey(AddPermission);
		registerPermissionKey(PrePermission);
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
				Layer calendarEntry = new Layer();
				calendarEntry.setStyleClass("calendarEntry");
				layer.add(calendarEntry);

				entry = (CalendarEntry) entries.get(a);
				localeStrings = CalendarFinder.getInstance().getEntryStrings(entry, this._iLocaleID);
				stamp = new IWTimestamp(entry.getDate());

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
					Layer date = new Layer();
					Text dateText = new Text(_datePattern != null ? stamp.getDateString(_datePattern) : stamp.getLocaleDate(iwc.getCurrentLocale(), this._dateStyle));
					date.add(dateText);
					date.setStyleClass("date");
					calendarEntry.add(date);

					Layer time = new Layer();
					Text timeText = new Text(_timePattern != null ? stamp.getDateString(_timePattern) : stamp.getLocaleTime(iwc.getCurrentLocale(), this._timeStyle));
					time.add(timeText);
					time.setStyleClass("time");
					calendarEntry.add(time);
					
					Layer headline = new Layer();
					headline.setStyleClass("event");
					headline.add(headlineText);
					calendarEntry.add(headline);

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
		link.setID("calendarAdd");
		return link;
	}

	private Link getPropertiesIcon() {
		Image image = this._iwb.getImage("shared/text.gif", "Types");
		Link link = new Link(image);
		link.setWindowToOpen(CalendarTypeEditor.class);
		link.setID("calendarProperties");
		return link;
	}

	private Link getCategoryIcon() {
		Image image = this._iwb.getImage("shared/edit.gif", "Categories");
		Link link = getCategoryLink(((com.idega.block.calendar.data.CalendarCategoryHome) com.idega.data.IDOLookup.getHomeLegacy(CalendarCategory.class)).createLegacy().getCategoryType());
		link.setImage(image);
		link.setID("calendarCategory");
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

	public boolean deleteBlock(int ICObjectInstanceID) {
		return CalendarBusiness.deleteBlock(getICObjectInstanceID());
	}
}