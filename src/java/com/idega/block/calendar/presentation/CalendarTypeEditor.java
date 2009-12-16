package com.idega.block.calendar.presentation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Locale;

import javax.ejb.CreateException;

import com.idega.block.calendar.business.CalendarBusiness;
import com.idega.block.calendar.business.CalendarFinder;
import com.idega.block.calendar.data.CalendarEntryType;
import com.idega.block.text.business.TextFinder;
import com.idega.core.file.data.ICFile;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.localisation.presentation.ICLocalePresentation;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.block.presentation.Builderaware;
import com.idega.idegaweb.presentation.CalendarParameters;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.io.UploadFile;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.FileInput;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.util.FileUtil;

public class CalendarTypeEditor extends IWAdminWindow{

private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.calendar";

private boolean _isAdmin = false;
private boolean _update = false;

private int _typeID = -1;

private IWBundle _iwb;
private IWResourceBundle _iwrb;

public CalendarTypeEditor(){
  setWidth(380);
  setHeight(400);
  setUnMerged();
}

  public void main(IWContext iwc) throws Exception {
    /**
     * @todo permission
     */
    this._isAdmin = true; //AccessControl.hasEditPermission(this,iwc);
    this._iwb = iwc.getIWMainApplication().getBundle(Builderaware.IW_CORE_BUNDLE_IDENTIFIER);
    this._iwrb = getResourceBundle(iwc);

    addTitle(this._iwrb.getLocalizedString("calendar_type_editor","Calendar Type Editor"));

    Locale currentLocale = iwc.getCurrentLocale();
    Locale chosenLocale;

    String sLocaleId = iwc.getParameter(CalendarParameters.PARAMETER_LOCALE_DROP);

    int iLocaleId = -1;
    if(sLocaleId!= null){
      iLocaleId = Integer.parseInt(sLocaleId);
      chosenLocale = ICLocaleBusiness.getLocaleReturnIcelandicLocaleIfNotFound(iLocaleId);
    }
    else{
      chosenLocale = currentLocale;
      iLocaleId = ICLocaleBusiness.getLocaleId(chosenLocale);
    }

    if ( this._isAdmin ) {
      processForm(iwc, iLocaleId);
    }
    else {
      noAccess();
    }
  }

  private void processForm(IWContext iwc, int iLocaleId) {
    if ( iwc.getParameter(CalendarParameters.PARAMETER_TYPE_ID) != null ) {
      try {
        this._typeID = Integer.parseInt(iwc.getParameter(CalendarParameters.PARAMETER_TYPE_ID));
      }
      catch (NumberFormatException e) {
        this._typeID = -1;
      }
    }

    if ( iwc.getParameter(CalendarParameters.PARAMETER_MODE) != null ) {
      if ( iwc.getParameter(CalendarParameters.PARAMETER_MODE).equalsIgnoreCase(CalendarParameters.PARAMETER_MODE_CLOSE) ) {
        closeEditor(iwc);
      }
      else if ( iwc.getParameter(CalendarParameters.PARAMETER_MODE).equalsIgnoreCase(CalendarParameters.PARAMETER_MODE_SAVE) ) {
        saveType(iwc,iLocaleId);
      }
    }

    if ( this._typeID == -1 && iwc.getSessionAttribute(CalendarParameters.PARAMETER_TYPE_ID) != null ) {
      try {
        this._typeID = Integer.parseInt((String)iwc.getSessionAttribute(CalendarParameters.PARAMETER_TYPE_ID));
      }
      catch (NumberFormatException e) {
        this._typeID = -1;
      }
      iwc.removeSessionAttribute(CalendarParameters.PARAMETER_TYPE_ID);
    }

    if ( this._typeID != -1 ) {
      if ( iwc.getParameter(CalendarParameters.PARAMETER_MODE_DELETE) != null ) {
        deleteType(iwc);
      }
      else {
        this._update = true;
      }
    }

    initializeFields(iLocaleId);
  }

  private void initializeFields(int iLocaleID) {
    CalendarEntryType type = null;
    if ( this._update ) {
		type = CalendarFinder.getInstance().getEntryType(this._typeID);
	}

    String[] locStrings = null;
    if ( type != null ) {
		locStrings = TextFinder.getLocalizedString(type,iLocaleID);
	}
    
    getUnderlyingForm().setMultiPart();

    DropdownMenu localeDrop = ICLocalePresentation.getLocaleDropdownIdKeyed(CalendarParameters.PARAMETER_LOCALE_DROP);
      localeDrop.setToSubmit();
      localeDrop.setSelectedElement(Integer.toString(iLocaleID));

    addLeft(this._iwrb.getLocalizedString("locale","Locale")+": ",localeDrop,false);

    Table typesTable = new Table(3,1);
      typesTable.setCellpadding(0);
      typesTable.setCellspacing(0);

    DropdownMenu entryTypes = CalendarBusiness.getEntryTypes(CalendarParameters.PARAMETER_TYPE_ID,iLocaleID);
      entryTypes.addMenuElementFirst("-1","");
      entryTypes.setToSubmit();
      entryTypes.setMarkupAttribute("style",STYLE);
      if ( this._typeID != -1 ) {
    	  entryTypes.setSelectedElement(Integer.toString(this._typeID));
		}
    typesTable.add(entryTypes,1,1);
    typesTable.setWidth(2,1,"5");

    Image newImage = this._iwb.getImage("shared/create.gif",this._iwrb.getLocalizedString("new_type","New type"));
    Link newLink = new Link(newImage);
    typesTable.add(newLink,3,1);

    Image deleteImage = this._iwb.getImage("shared/delete.gif",this._iwrb.getLocalizedString("delete_type","Delete type"));
    Link deleteLink = new Link(deleteImage);
      deleteLink.addParameter(CalendarParameters.PARAMETER_MODE_DELETE,CalendarParameters.PARAMETER_TRUE);
      deleteLink.addParameter(CalendarParameters.PARAMETER_TYPE_ID,this._typeID);
    typesTable.add(deleteLink,3,1);

    addLeft(this._iwrb.getLocalizedString("type","Type")+":",typesTable,true,false);

    TextInput nameInput = new TextInput(CalendarParameters.PARAMETER_ENTRY_HEADLINE);
      nameInput.setLength(24);
      if ( locStrings != null && locStrings[0] != null ) {
			nameInput.setContent(locStrings[0]);
		}
    addLeft(this._iwrb.getLocalizedString("name","Name")+":",nameInput,true);

    if (type != null && type.getImageID() != -1) {
    	try {
			Image image = new Image(type.getImageID());
			addLeft(this._iwrb.getLocalizedString("old_image", "Old image") + ":", image, true);
		}
    	catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    FileInput file = new FileInput();
    addLeft(this._iwrb.getLocalizedString("new_image","New image")+":",file,true);

    addHiddenInput(new HiddenInput(CalendarParameters.PARAMETER_TYPE_ID,Integer.toString(this._typeID)));
    addSubmitButton(new SubmitButton(this._iwrb.getLocalizedImageButton("close","CLOSE"),CalendarParameters.PARAMETER_MODE,CalendarParameters.PARAMETER_MODE_CLOSE));
    addSubmitButton(new SubmitButton(this._iwrb.getLocalizedImageButton("save","SAVE"),CalendarParameters.PARAMETER_MODE,CalendarParameters.PARAMETER_MODE_SAVE));
  }

  private void deleteType(IWContext iwc) {
    CalendarBusiness.deleteEntryType(this._typeID);
    this._typeID = -1;
		IWCacheManager.getInstance(iwc.getIWMainApplication()).invalidateCache(Calendar.CACHE_KEY);
  }

  private void saveType(IWContext iwc,int iLocaleID) {
    String typeHeadline = iwc.getParameter(CalendarParameters.PARAMETER_ENTRY_HEADLINE);

	int fileID = -1;
	UploadFile uploadFile = iwc.getUploadedFile();
	if (uploadFile != null && uploadFile.getName() != null && uploadFile.getName().length() > 0) {
		try {
			FileInputStream input = new FileInputStream(uploadFile.getRealPath());

			ICFile file = ((com.idega.core.file.data.ICFileHome) com.idega.data.IDOLookup.getHome(ICFile.class)).create();
			file.setName(uploadFile.getName());
			file.setMimeType(uploadFile.getMimeType());
			file.setFileValue(input);
			file.setFileSize((int) uploadFile.getSize());
			file.store();

			fileID = ((Integer) file.getPrimaryKey()).intValue();
			uploadFile.setId(fileID);
			try {
				FileUtil.delete(uploadFile);
			}
			catch (Exception ex) {
				System.err.println("MediaBusiness: deleting the temporary file at " + uploadFile.getRealPath() + " failed.");
			}
		}
		catch (RemoteException e) {
			e.printStackTrace(System.err);
			uploadFile.setId(-1);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			uploadFile.setId(-1);
		}
		catch (CreateException e) {
			e.printStackTrace();
			uploadFile.setId(-1);
		}
	}

    int typeID = CalendarBusiness.saveEntryType(this._typeID,iLocaleID,typeHeadline,String.valueOf(fileID));
    iwc.setSessionAttribute(CalendarParameters.PARAMETER_TYPE_ID,Integer.toString(typeID));
		
    IWCacheManager.getInstance(iwc.getIWMainApplication()).invalidateCache(Calendar.CACHE_KEY);
  }

  private void closeEditor(IWContext iwc) {
    setParentToReload();
    close();
  }

  private void noAccess() throws IOException,SQLException {
    close();
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }
}