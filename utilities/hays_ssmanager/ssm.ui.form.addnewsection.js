/////////////////////////////////////////////////////////////////////////////
// 
// Solution  : SiteStudio
// Project   : Site Studio Manager (SSM)
//
// FileName  : ssm.ui.form.addnewsection.js
// FileType  : Javascript
// Author    : Jake Gordon
// Created   : January 2006
// Version   : v7.7
//
// Comments  : 
//
// Copyright : Stellent, Incorporated Confidential and Proprietary
//
//             This computer program contains valuable, confidential and proprietary
//             information. Disclosure, use, or reproduction without the written
//             authorization of Stellent is prohibited. This unpublished
//             work by Stellent is protected by the laws of the United States
//             and other countries. If publication of the computer program should occur,
//             the following notice shall apply:
//
//             Copyright (c) 1997-2001 IntraNet Solutions, Incorporated. All rights reserved.
//             Copyright (c) 2001-2006 Stellent, Incorporated. All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

if (!SSM.UI.Forms)
    throw "must include ssm.ui.js before including this file"

//***************************************************************************

SSM.UI.Forms.AddNewSection = function(callback, label, urlDirName, sectionIdType, sectionId)
{
    SSM.UI.Disable();

    // default parameters if necessary
    this.callback = callback;
    this.label = SSAjax.ToString(label, '');
    this.urlDirName = SSAjax.ToString(urlDirName, '');
    this.sectionIdType = (SSAjax.IsValid(sectionIdType) ? sectionIdType : SSM.UI.Forms.AddNewSection.SectionIdType.AUTO);
    this.sectionId = SSAjax.ToString(sectionId, '');

    // create underlying messagebox control
    this.msgbox = new SSAjax.DHTML.MessageBox(SSM.UI.Forms.AddNewSection.Template,
                                              SSM.GetString('ssmAddNewSection'),
                                              SSM.UI.Forms.AddNewSection.MsgBoxCallback,
                                              SSAjax.DHTML.MessageBox.Style.MB_OKCANCEL,
                                              'ssm_form_addnewsection',
                                              'ssm_form',
                                              SSM.UI.main,
                                              false); // dont show immediately, want to initialize controls first

    this.msgbox.form = this; // back-reference so we can easily get to "this" form object from the messagebox object (for event handling)

    // get references for individual form controls
    this.controls = new Object();
    this.controls.label = SSAjax.DHTML.GetObject('ssm_form_addnewsection_label');
    this.controls.urlDirName = SSAjax.DHTML.GetObject('ssm_form_addnewsection_urldirname');
    this.controls.autoSectionId = SSAjax.DHTML.GetObject('ssm_form_addnewsection_sectionid_auto');
    this.controls.manualSectionId = SSAjax.DHTML.GetObject('ssm_form_addnewsection_sectionid_manual');
    this.controls.sectionId = SSAjax.DHTML.GetObject('ssm_form_addnewsection_sectionid');

    // Localize the form labels
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_addnewsection_label_label'), SSM.GetString('ssmAddNewSectionLabelLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_addnewsection_urldirname_label'), SSM.GetString('ssmAddNewSectionUrlDirNameLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_addnewsection_id_auto_label'), SSM.GetString('ssmAddNewSectionIdAutoLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_addnewsection_id_manual_label'), SSM.GetString('ssmAddNewSectionIdManualLabel'));
    SSM.UI.Forms.AddNewSection.AutoSectionId = SSM.GetString('ssmAddNewSectionAutoLabel');
    
    var form = this;
    var refresh = function(e) { form.Refresh(); } // cant use this.Refresh() directly in DOM event handler, use closure instead
    
    // attach any necessary event handling
    SSAjax.DHTML.AddEvent(this.controls.label, 'keyup', refresh);
    SSAjax.DHTML.AddEvent(this.controls.urlDirName, 'keyup', refresh);
    SSAjax.DHTML.AddEvent(this.controls.sectionId, 'keyup', refresh);
    SSAjax.DHTML.AddEvent(this.controls.autoSectionId, 'click', refresh);
    SSAjax.DHTML.AddEvent(this.controls.manualSectionId, 'click', refresh);

    var settings = SSM.SelectedSectionSettings();
    
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(this.controls.urlDirName), !settings.addSection.isUrlDirNameHidden);
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(this.controls.sectionId), !settings.addSection.isIdHidden);
        
    this.Initialize();
    this.Refresh();
    this.msgbox.Show();

    this.controls.label.focus();
}

//***************************************************************************

SSM.UI.Forms.AddNewSection.SectionIdType =  {
    AUTO: 0,
    MANUAL: 1
};

SSM.UI.Forms.AddNewSection.InvalidSectionIdChars = ' ;/\\?:@&=+"#%<>*~|[]\',^()';
SSM.UI.Forms.AddNewSection.AutoSectionId = '(auto)';

//***************************************************************************

SSM.UI.Forms.AddNewSection.MsgBoxCallback = function(msgbox, button)
{
    var form = msgbox.form;

    form.Finalize();
    if (button == SSAjax.DHTML.MessageBox.Button.IDOK)
        if (!form.Validate())
            return false;

    SSM.UI.Enable();
    
    var finished = (SSAjax.IsValid(form.callback) ? form.callback(form, button) : true);
    if (!SSAjax.ToBool(finished, true)) // finished is false (and not null nor undefined)
    {
        SSM.UI.Disable();
        return false;
    }
    
    // important to remove cyclic DOM/JS references to avoid memory leaks
    form.msgbox.form = null;
    form.msgbox = null;
    form.controls.label = null;
    form.controls.urlDirName = null;
    form.controls.autoSectionId = null;
    form.controls.manualSectionId = null;
    form.controls.sectionId = null;
    form.controls = null;
}

//***************************************************************************

SSM.UI.Forms.AddNewSection.prototype.Initialize = function()
{
    this.controls.label.value = this.label;
    this.controls.urlDirName.value = this.urlDirName;
    this.controls.autoSectionId.checked = (this.sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.AUTO);
    this.controls.manualSectionId.checked = (this.sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL);
    this.controls.sectionId.value = this.sectionId;
}

SSM.UI.Forms.AddNewSection.prototype.Finalize = function()
{
    this.label = this.controls.label.value;
    this.urlDirName = this.controls.urlDirName.value;
    this.autoSectionId = this.controls.autoSectionId.checked;
    this.manualSectionId = this.controls.manualSectionId.checked;
    this.sectionIdType = (this.manualSectionId ? SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL : SSM.UI.Forms.AddNewSection.SectionIdType.AUTO);
    if (this.sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL)
        this.sectionId = this.controls.sectionId.value; // only remember sectionId if it was a manually entered one
}

SSM.UI.Forms.AddNewSection.prototype.Refresh = function()
{
    var settings = SSM.SelectedSectionSettings();
    
    // pull out latest values from controls...
    var label = this.controls.label.value;
    var urlDirName = this.controls.urlDirName.value;
    var autoSectionId = this.controls.autoSectionId.checked;
    var manualSectionId = this.controls.manualSectionId.checked;
    var sectionIdType = (manualSectionId ? SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL : SSM.UI.Forms.AddNewSection.SectionIdType.AUTO);
    var sectionId = this.controls.sectionId.value;

    // auto generate urlDirName by stripping out illegal chars
    var strippedLabel         = label.StripCharacters(SSM.UI.Forms.AddNewSection.InvalidSectionIdChars);
    //custom
    //var previousStrippedLabel = this.label.StripCharacters(SSM.UI.Forms.AddNewSection.InvalidSectionIdChars);
    var previousStrippedLabel = this.label.StripCharacters(SSM.UI.Forms.AddNewSection.InvalidSectionIdChars).toLowerCase();
    //custom

    if (urlDirName == previousStrippedLabel){
        //custom
        //this.controls.urlDirName.value = urlDirName = strippedLabel;
	this.controls.urlDirName.value = urlDirName = strippedLabel.toLowerCase();
        //custom
    }

    // if sectionIdType=auto then set sectionId='(auto)'. Otherwise if sectionIdType was changed from auto to manual then populate sectionId with last known value
    if (sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.AUTO)
        this.controls.sectionId.value = sectionId = SSM.UI.Forms.AddNewSection.AutoSectionId;
    else if (this.sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.AUTO)
        this.controls.sectionId.value = sectionId = this.sectionId;

    // enable/disable controls appropriately    
    var bLabelMissing = (label.length == 0);
    var bSectionIdMissing = ((sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL) && (sectionId.length == 0));

    SSAjax.DHTML.Enable(this.controls.urlDirName, !settings.addSection.isUrlDirNameDisabled);
    SSAjax.DHTML.Enable(this.controls.autoSectionId, !settings.addSection.isIdDisabled);
    SSAjax.DHTML.Enable(this.controls.manualSectionId, !settings.addSection.isIdDisabled);
    SSAjax.DHTML.Enable(this.controls.sectionId, !settings.addSection.isIdDisabled && (sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL));
    this.msgbox.EnableButton(SSAjax.DHTML.MessageBox.Button.IDOK, (!bLabelMissing && !bSectionIdMissing));

    // remember latest values for next time round!
    this.Finalize();
}

SSM.UI.Forms.AddNewSection.prototype.Validate = function()
{
    var labelTrimmed = String.Trim(this.label);
    if (labelTrimmed.length == 0)
    {
        SSM.UI.DisplayErrorMessage(SSM.GetString('ssmSectionLabelIsMissing'), this.msgbox.div);
        return false;
    }
    
    var pos = this.urlDirName.FindOneOf(SSM.UI.Forms.AddNewSection.InvalidSectionIdChars);
    if (pos >= 0)
    {
        SSM.UI.DisplayErrorMessage(SSM.GetString('ssmInvalidCharacterInUrl', this.urlDirName.charAt(pos)), this.msgbox.div);
        return false;
    }
    
    if (this.sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL)
    {
        pos = this.sectionId.FindOneOf(SSM.UI.Forms.AddNewSection.InvalidSectionIdChars);
        if (pos >= 0)
        {
            SSM.UI.DisplayErrorMessage(SSM.GetString('ssmInvalidCharacterInSectionId', this.sectionId.charAt(pos)), this.msgbox.div);
            return false;
        }
    }
    
	 //custom
   if (!(this.urlDirName.toLowerCase()==this.urlDirName)){
	SSM.UI.DisplayErrorMessage(SSM.GetString('ssmUseLowerCaseOnly'));
        return SSAjax.DHTML.CancelEvent(e);	
    }
    //end custom
	
    return true;
}

//***************************************************************************

// LOCALIZABLE
SSM.UI.Forms.AddNewSection.Template = 
'    <table>' +
'        <tr>' +
'            <td><label id="ssm_form_addnewsection_label_label" for="ssm_form_addnewsection_label">Label:</label></td>' +
'            <td><input type="text" class="text" id="ssm_form_addnewsection_label" /></td>' +
'        </tr>' +
'        <tr>' +
'            <td><label id="ssm_form_addnewsection_urldirname_label" for="ssm_form_addnewsection_url">URL:</label></td>' +
'            <td><input type="text" class="text" id="ssm_form_addnewsection_urldirname" /></td>' +
'        </tr>' +
'        <tr>' +
'            <td colspan="2">' +
'                <fieldset>' +
'                    <input type="radio" class="radio" name="ssm_form_addnewsection_sectionidtype" id="ssm_form_addnewsection_sectionid_auto" />' +
'                    <label id="ssm_form_addnewsection_id_auto_label" for="ssm_form_addnewsection_id_auto">Auto-generate section ID</label><br />' +
'                    <input type="radio" class="radio" name="ssm_form_addnewsection_sectionidtype" id="ssm_form_addnewsection_sectionid_manual" />' +
'                    <label id="ssm_form_addnewsection_id_manual_label" for="ssm_form_addnewsection_id_manual">Manually enter section ID</label><br />' +
'                    <input type="radio" class="radio" style="visibility:hidden;"/><!--padding for alignment-->' +
'                    <input type="text" class="text" id="ssm_form_addnewsection_sectionid" />' +
'                </fieldset>' +
'            </td>' +
'        </tr>' +
'    </table>';

//***************************************************************************
