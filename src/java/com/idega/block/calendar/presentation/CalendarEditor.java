package com.idega.block.calendar.presentation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

import com.idega.block.calendar.business.CalendarBusiness;
import com.idega.block.calendar.business.CalendarFinder;
import com.idega.block.calendar.data.CalendarEntry;
import com.idega.block.text.business.TextFinder;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.localisation.presentation.ICLocalePresentation;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.CalendarParameters;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.TimestampInput;
import com.idega.util.IWTimestamp;

public class CalendarEditor extends IWAdminWindow {

	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.calendar";
	private boolean _isAdmin = false;
	private boolean _update = false;
	private int _entryID = -1;
	//private int _typeID = -1;
	private int _userID = -1;
	private int _groupID = -1;
	private int _instanceID = -1;
	private IWTimestamp _stamp;
	private IWTimestamp _endStamp;
	private boolean _allDayEvent = false;
	private boolean _repeatEveryYear = false;

	private IWResourceBundle _iwrb;

	public CalendarEditor() {
		setWidth(550);
		setHeight(500);
		setUnMerged();
	}

	@Override
	public void main(IWContext iwc) throws Exception {
		/**
		 * @todo permission
		 */
		this._isAdmin = true;
		this._iwrb = getResourceBundle(iwc);
		addTitle(this._iwrb.getLocalizedString("calendar_admin", "Calendar Admin"));
		Locale currentLocale = iwc.getCurrentLocale(), chosenLocale;

		try {
			this._userID = LoginBusinessBean.getUser(iwc).getId();
		} catch (Exception e) {
			this._userID = -1;
		}
		try {
			this._groupID = LoginBusinessBean.getUser(iwc).getPrimaryGroup().getID();
		} catch (Exception e) {
			this._groupID = -1;
		}

		String sLocaleId = iwc.getParameter(CalendarParameters.PARAMETER_LOCALE_DROP);
		int iCategoryId = iwc.isParameterSet(CalendarParameters.PARAMETER_IC_CAT) ? Integer.parseInt(iwc.getParameter(CalendarParameters.PARAMETER_IC_CAT)) : -1;

		int iLocaleId = -1;
		if (sLocaleId != null) {
			iLocaleId = Integer.parseInt(sLocaleId);
			chosenLocale = ICLocaleBusiness.getLocaleReturnIcelandicLocaleIfNotFound(iLocaleId);
		} else {
			chosenLocale = currentLocale;
			iLocaleId = ICLocaleBusiness.getLocaleId(chosenLocale);
		}

		if (this._isAdmin) {
			processForm(iwc, iLocaleId, iCategoryId);
		} else {
			noAccess();
		}
	}

	private void processForm(IWContext iwc, int iLocaleId, int iCategoryId) {
		if (iwc.getParameter(CalendarParameters.PARAMETER_INSTANCE_ID) != null) {
			try {
				this._instanceID = Integer.parseInt(iwc.getParameter(CalendarParameters.PARAMETER_INSTANCE_ID));
			} catch (NumberFormatException e) {
				this._instanceID = -1;
			}
		}

		if (iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_ID) != null) {
			try {
				this._entryID = Integer.parseInt(iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_ID));
			} catch (NumberFormatException e) {
				this._entryID = -1;
			}
		}

		/*if (iwc.getParameter(CalendarParameters.PARAMETER_TYPE_ID) != null) {
			try {
				this._typeID = Integer.parseInt(iwc.getParameter(CalendarParameters.PARAMETER_TYPE_ID));
			} catch (NumberFormatException e) {
				this._typeID = -1;
			}
		}*/
		
		if (iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_DATE) != null) {
			try {
				this._stamp = new IWTimestamp(iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_DATE));
			} catch (Exception e) {
				this._stamp = new IWTimestamp();
			}
		} else {
			this._stamp = new IWTimestamp();
		}

		if (iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_END_DATE) != null) {
			try {
				this._endStamp = new IWTimestamp(iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_END_DATE));
			} catch (Exception e) {
			}
		}
		
		if (iwc.isParameterSet(CalendarParameters.PARAMETER_ALL_DAY_EVENT)) {
			this._allDayEvent = true;
		}

		if (iwc.isParameterSet(CalendarParameters.PARAMETER_REPEAT_EVERY_YEAR)) {
			this._repeatEveryYear = true;
		}
		
		if (iwc.getParameter(CalendarParameters.PARAMETER_MODE) != null) {
			if (iwc.getParameter(CalendarParameters.PARAMETER_MODE).equalsIgnoreCase(CalendarParameters.PARAMETER_MODE_CLOSE)) {
				closeEditor(iwc);
			} else
				if (iwc.getParameter(CalendarParameters.PARAMETER_MODE).equalsIgnoreCase(CalendarParameters.PARAMETER_MODE_SAVE)) {
					saveEntry(iwc, iLocaleId, iCategoryId);
				}
		}

		if (this._entryID == -1 && iwc.getSessionAttribute(CalendarParameters.PARAMETER_ENTRY_ID) != null) {
			try {
				this._entryID = Integer.parseInt((String) iwc.getSessionAttribute(CalendarParameters.PARAMETER_ENTRY_ID));
			} catch (NumberFormatException e) {
				this._entryID = -1;
			}
			iwc.removeSessionAttribute(CalendarParameters.PARAMETER_ENTRY_ID);
		}

		if (this._entryID != -1) {
			if (iwc.getParameter(CalendarParameters.PARAMETER_MODE_DELETE) != null) {
				deleteEntry(iwc);
			} else {
				this._update = true;
			}
		}

		DropdownMenu localeDrop = ICLocalePresentation.getLocaleDropdownIdKeyed(CalendarParameters.PARAMETER_LOCALE_DROP);
		localeDrop.setToSubmit();
		localeDrop.setSelectedElement(Integer.toString(iLocaleId));
		addLeft(this._iwrb.getLocalizedString("locale", "Locale") + ": ", localeDrop, false);
		addHiddenInput(new HiddenInput(CalendarParameters.PARAMETER_ENTRY_ID, Integer.toString(this._entryID)));

		initializeFields(iwc, iLocaleId, iCategoryId);
	}

	private void initializeFields(IWContext iwc, int iLocaleId, int iCategoryId) {
		CalendarEntry entry = null;
		if (this._update) {
			entry = CalendarFinder.getInstance().getEntry(this._entryID);
		}

		String[] locTexts = null;
		if (entry != null) {
			locTexts = TextFinder.getLocalizedString(entry, iLocaleId);
		}
		if (entry != null) {
			this._stamp = new IWTimestamp(entry.getDate());
			if (entry.getEndDate() != null) {
				this._endStamp = new IWTimestamp(entry.getEndDate());
			}
			this._allDayEvent = entry.isAllDayEvent();
			this._repeatEveryYear = entry.isRepeatEveryYear();
		}

		DropdownMenu categories = CalendarBusiness.getCategories(CalendarParameters.PARAMETER_IC_CAT, iwc.getCurrentLocale(), this._instanceID);
		if (entry != null) {
			categories.setSelectedElement(Integer.toString(entry.getCategoryId()));
		}
		addLeft(this._iwrb.getLocalizedString("category", "Category") + ":", categories, false);
		
		DropdownMenu entryTypes = CalendarBusiness.getEntryTypes(CalendarParameters.PARAMETER_TYPE_ID, iLocaleId);
		if (entry != null) {
			entryTypes.setSelectedElement(Integer.toString(entry.getEntryTypeID()));
		}
		addLeft(this._iwrb.getLocalizedString("type", "Type") + ":", entryTypes, false);

		TextInput entryHeadline = new TextInput(CalendarParameters.PARAMETER_ENTRY_HEADLINE);
		entryHeadline.setLength(24);
		if (locTexts != null && locTexts[0] != null) {
			entryHeadline.setContent(locTexts[0]);
		}
		addLeft(this._iwrb.getLocalizedString("headline", "Headline") + ":", entryHeadline, true);

		TextArea entryBody = new TextArea(CalendarParameters.PARAMETER_ENTRY_BODY);
		if (locTexts != null && locTexts[1] != null) {
			entryBody.setContent(locTexts[1]);
		}
		entryBody.setWidth("100%");
		entryBody.setRows(7);
		addLeft(this._iwrb.getLocalizedString("body", "Body") + ":", entryBody, true);

		IWTimestamp stamp = new IWTimestamp();
		TimestampInput entryDate = new TimestampInput(CalendarParameters.PARAMETER_ENTRY_DATE);
		entryDate.setYearRange(stamp.getYear() - 125, stamp.getYear() + 5);
		if (this._stamp == null) {
			this._stamp = IWTimestamp.RightNow();
		}
		entryDate.setTimestamp(this._stamp.getTimestamp());
		entryDate.setStyleAttribute(STYLE);

		TimestampInput entryEndDate = new TimestampInput(CalendarParameters.PARAMETER_ENTRY_END_DATE);
		entryEndDate.setYearRange(stamp.getYear() - 125, stamp.getYear() + 5);
		if (this._endStamp != null) {
			entryEndDate.setTimestamp(this._endStamp.getTimestamp());
		}
		entryEndDate.setStyleAttribute(STYLE);
		
		CheckBox allDayEvent = new CheckBox(CalendarParameters.PARAMETER_ALL_DAY_EVENT, Boolean.TRUE.toString());
		allDayEvent.setChecked(this._allDayEvent);
		
		CheckBox repeatEveryYear = new CheckBox(CalendarParameters.PARAMETER_REPEAT_EVERY_YEAR, Boolean.TRUE.toString());
		repeatEveryYear.setChecked(this._repeatEveryYear);

		addLeft(this._iwrb.getLocalizedString("date_start", "Start date") + ":", entryDate, true);
		addLeft(this._iwrb.getLocalizedString("date_end", "End date") + ":", entryEndDate, true);
		addInverseLeft(this._iwrb.getLocalizedString("all_day_event", "All day event"), allDayEvent, false);
		addInverseLeft(this._iwrb.getLocalizedString("repeat_every_year", "Repeat every year"), repeatEveryYear, false);
		addHiddenInput(new HiddenInput(CalendarParameters.PARAMETER_IC_CAT, String.valueOf(iCategoryId)));
		addHiddenInput(new HiddenInput(CalendarParameters.PARAMETER_INSTANCE_ID, String.valueOf(this._instanceID)));
		addSubmitButton(new SubmitButton(this._iwrb.getLocalizedImageButton("close", "CLOSE"), CalendarParameters.PARAMETER_MODE, CalendarParameters.PARAMETER_MODE_CLOSE));
		addSubmitButton(new SubmitButton(this._iwrb.getLocalizedImageButton("save", "SAVE"), CalendarParameters.PARAMETER_MODE, CalendarParameters.PARAMETER_MODE_SAVE));
	}

	private void saveEntry(IWContext iwc, int localeID, int categoryId) {
		String entryHeadline = iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_HEADLINE);
		String entryBody = iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_BODY);
		String entryDate = iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_DATE);
		String entryEndDate = iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_END_DATE);
		String entryType = iwc.getParameter(CalendarParameters.PARAMETER_TYPE_ID);
		boolean allDayEvent = iwc.isParameterSet(CalendarParameters.PARAMETER_ALL_DAY_EVENT);
		boolean repeatEveryYear = iwc.isParameterSet(CalendarParameters.PARAMETER_REPEAT_EVERY_YEAR);

		int entryID = CalendarBusiness.saveEntry(this._entryID, this._userID, this._groupID, localeID, categoryId, entryHeadline, entryBody, entryDate, entryEndDate, entryType, allDayEvent, repeatEveryYear);
		iwc.setSessionAttribute(CalendarParameters.PARAMETER_ENTRY_ID, Integer.toString(entryID));
		IWCacheManager.getInstance(iwc.getIWMainApplication()).invalidateCache(Calendar.CACHE_KEY);
	}

	private void deleteEntry(IWContext iwc) {
		CalendarBusiness.deleteEntry(this._entryID);
		IWCacheManager.getInstance(iwc.getIWMainApplication()).invalidateCache(Calendar.CACHE_KEY);
		closeEditor(iwc);
	}

	private void closeEditor(IWContext iwc) {
		setParentToReload();
		close();
	}

	private void noAccess() throws IOException, SQLException {
		addLeft(this._iwrb.getLocalizedString("no_access", "Login first!"));
		addSubmitButton(new CloseButton(this._iwrb.getImage("close.gif")));
	}

	@Override
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
}
