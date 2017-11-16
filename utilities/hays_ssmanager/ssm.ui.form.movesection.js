/////////////////////////////////////////////////////////////////////////////
// 
// Solution  : SiteStudio
// Project   : Site Studio Manager (SSM)
//
// FileName  : ssm.ui.form.movesection.js
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
//***************************************************************************
//******************************* CONSTRUCTOR *******************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection = function(callback, selectedSectionId, locationType)
{
    SSM.UI.Disable();

    // default parameters if necessary
    this.callback = callback;
    this.selectedSectionId = selectedSectionId;
    this.locationType = (SSAjax.IsValid(locationType) ? locationType : SSM.UI.Forms.MoveSection.LocationType.IN);
    this.disallowedSections = SSM.currentSite.GetAllChildSectionIds(selectedSectionId, true);
    this.disallowedSections.push(selectedSectionId);

    // create underlying messagebox control
    this.msgbox = new SSAjax.DHTML.MessageBox(SSM.UI.Forms.MoveSection.Template,
                                              SSM.GetString('ssmMoveSection'),
                                              SSM.UI.Forms.MoveSection.MsgBoxCallback,
                                              SSAjax.DHTML.MessageBox.Style.MB_OKCANCEL,
                                              'ssm_form_move_section',
                                              'ssm_form',
                                              SSM.UI.main,
                                              false); // dont show immediately, want to initialize controls first

    this.msgbox.form = this; // back-reference so we can easily get to "this" form object from the messagebox object (for event handling)

    // get references for individual form controls
    this.controls = new Object();
    this.controls.label = SSAjax.DHTML.GetObject('ssm_form_move_section_label');
    this.controls.moveBefore = SSAjax.DHTML.GetObject('ssm_form_move_section_before');
    this.controls.moveAfter = SSAjax.DHTML.GetObject('ssm_form_move_section_after');
    this.controls.moveIn = SSAjax.DHTML.GetObject('ssm_form_move_section_in');
    this.controls.hierarchy = new SSAjax.DHTML.TreeCtrl('ssm_form_move_section_hierarchy', 'ssm_hierarchy', 'ssm_form_move_section_hierarchy_parent');

    // Localize the controls
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_move_section_prefix'), SSM.GetString('ssmMoveSectionNamePrefixLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_move_section_suffix'), SSM.GetString('ssmMoveSectionNameSuffixLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_move_section_before_label'), SSM.GetString('ssmMoveSectionBeforeLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_move_section_after_label'), SSM.GetString('ssmMoveSectionAfterLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_form_move_section_in_label'), SSM.GetString('ssmMoveSectionInLabel'));

    this.controls.label = SSAjax.DHTML.GetObject('ssm_form_move_section_label');
    SSAjax.DHTML.SetContent(this.controls.label, SSM.currentSite.GetSection(selectedSectionId).label);

    this.controls.hierarchy.showRootItemExpandIcons = false;
    this.controls.hierarchy.OnBeforeExpandItem = SSM.UI.Forms.MoveSection.OnBeforeExpandSection;
    this.controls.hierarchy.OnAfterSelectItem = SSM.UI.Forms.MoveSection.OnAfterSelectSection;

    var form = this;
    var refresh = function(e) { form.Refresh(); } // cant use this.Refresh() directly in DOM event handler, use closure instead

    SSAjax.DHTML.AddEvent(this.controls.moveBefore, 'click', refresh);
    SSAjax.DHTML.AddEvent(this.controls.moveAfter, 'click', refresh);
    SSAjax.DHTML.AddEvent(this.controls.moveIn, 'click', refresh);

    this.Initialize();
    this.Refresh();
    this.msgbox.Show();
}

//***************************************************************************
//***************************************************************************
//****************************** ENUMERATIONS *******************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.LocationType =  {
    BEFORE: 0,
    AFTER: 1,
    IN: 2
};

//***************************************************************************
//***************************************************************************
//***************************** BUTTON CALLBACK *****************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.MsgBoxCallback = function(msgbox, button)
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
    form.controls.hierarchy = null;
    form.controls.moveBefore = null;
    form.controls.moveAfter = null;
    form.controls.moveIn = null;
    form.controls.label = null;
    form.controls = null;
}

//***************************************************************************
//***************************************************************************
//******************************** INITIALIZE *******************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.prototype.Initialize = function()
{
    SSM.UI.LoadHierarchyTree(this.controls.hierarchy, this.selectedSectionId);
    SSM.UI.EnsureSectionExists(this.controls.hierarchy, this.selectedSectionId);

    this.controls.moveBefore.checked = (this.locationType == SSM.UI.Forms.MoveSection.LocationType.BEFORE);
    this.controls.moveAfter.checked = (this.locationType == SSM.UI.Forms.MoveSection.LocationType.AFTER);
    this.controls.moveIn.checked = (this.locationType == SSM.UI.Forms.MoveSection.LocationType.IN);
}

//***************************************************************************
//***************************************************************************
//******************************** FINALIZE *********************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.prototype.Finalize = function()
{
    var locationType = (this.controls.moveBefore.checked ? SSM.UI.Forms.MoveSection.LocationType.BEFORE
                                                         : (this.controls.moveAfter.checked ? SSM.UI.Forms.MoveSection.LocationType.AFTER
                                                                                            : SSM.UI.Forms.MoveSection.LocationType.IN));

    var selectedSection = SSM.currentSite.GetSection(this.controls.hierarchy.selectedItemId);

    switch(locationType)
    {
    case SSM.UI.Forms.MoveSection.LocationType.BEFORE:
        this.newParentId = selectedSection.parentSectionId;
        this.insertAfterId = SSM.currentSite.GetPreviousSiblingId(selectedSection.id);
        break;
        
    case SSM.UI.Forms.MoveSection.LocationType.AFTER:
        this.newParentId = selectedSection.parentSectionId;
        this.insertAfterId = selectedSection.id;
        break;
        
    case SSM.UI.Forms.MoveSection.LocationType.IN:
        this.newParentId = selectedSection.id;
        this.insertAfterId = SSM.currentSite.GetLastChildSectionId(selectedSection);
        break;
    }
}

//***************************************************************************
//***************************************************************************
//********************************* REFRESH *********************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.prototype.Refresh = function()
{
    // pull out latest values from controls...
    var locationType = (this.controls.moveBefore.checked ? SSM.UI.Forms.MoveSection.LocationType.BEFORE
                                                         : (this.controls.moveAfter.checked ? SSM.UI.Forms.MoveSection.LocationType.AFTER
                                                                                            : SSM.UI.Forms.MoveSection.LocationType.IN));

    var selectedSection = SSM.currentSite.GetSection(this.controls.hierarchy.selectedItemId);
    var id = (locationType == SSM.UI.Forms.MoveSection.LocationType.IN ? selectedSection.id : selectedSection.parentSectionId);
                                                                                                
    var bOK = SSAjax.IsValid(id);
    for (var i = 0 ; i < this.disallowedSections.length ; i++)
    {
        if (id == this.disallowedSections[i])
            bOK = false;
    }
    
    if (locationType == SSM.UI.Forms.MoveSection.LocationType.IN)
    {
        var settings = SSM.settings.forSection(selectedSection.id);
        if (!settings.moveSection.canBeTarget)
            bOK = false;
    }
    else
    {
        var settings = SSM.settings.forSection(selectedSection.parentSectionId);
        if (!settings.moveSection.canBeTarget)
            bOK = false;
    }

    // ucf p51040719 - should not be allowed to move BEFORE or AFTER root section
    if ((locationType != SSM.UI.Forms.MoveSection.LocationType.IN) &&
        (selectedSection.id == this.controls.hierarchy.GetRootItem().itemId))
    {
        bOK = false;
    }
    
    this.msgbox.EnableButton(SSAjax.DHTML.MessageBox.Button.IDOK, bOK);
    
    this.Finalize(); // remember latest values for next time round!
}

//***************************************************************************
//***************************************************************************
//********************************* VALIDATE ********************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.prototype.Validate = function()
{
    return true;
}

//***************************************************************************
//***************************************************************************
//****************************** EVENT HANDLERS *****************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.prototype.OnBeforeExpandSection = function(sectionId)
{
    SSM.UI.CreateSectionsChildren(this.controls.hierarchy, sectionId);
    return true;
}

SSM.UI.Forms.MoveSection.prototype.OnAfterSelectSection = function(sectionId)
{
    this.Refresh();
}

//***************************************************************************
//***************************************************************************
//***************************** STATIC METHODS ******************************
//***************************************************************************
//***************************************************************************

SSM.UI.Forms.MoveSection.FindForm = function(obj)
{
    if (obj.msgbox && obj.msgbox.form)
        return obj.msgbox.form;
    else if (obj.parentNode)
        return SSM.UI.Forms.MoveSection.FindForm(obj.parentNode);
    else
        return null;
}

//***************************************************************************

SSM.UI.Forms.MoveSection.OnBeforeExpandSection = function(tree, sectionId)
{
    var form = SSM.UI.Forms.MoveSection.FindForm(tree.div);
    if (form)
        return form.OnBeforeExpandSection(sectionId);

    return false;
}

SSM.UI.Forms.MoveSection.OnAfterSelectSection = function(tree, sectionId)
{
    var form = SSM.UI.Forms.MoveSection.FindForm(tree.div);
    if (form)
        form.OnAfterSelectSection(sectionId);
}

//***************************************************************************

// LOCALIZABLE
SSM.UI.Forms.MoveSection.Template = 
'<table>' +
'   <tr>' +
'       <td>' +
'           <span id="ssm_form_move_section_prefix">Move "<span id="ssm_form_move_section_label"></span>" section...</span>' +
'       </td>' +
'   </tr>' +
'   <tr>' +
'       <td>' +
'           <table>' +
'               <tr>' +
'                   <td><input type="radio" class="radio" name="ssm_form_move_section_where" id="ssm_form_move_section_before"></td><td><label for="" id="ssm_form_move_section_before_label">Before</label></td>' +
'                   <td><input type="radio" class="radio" name="ssm_form_move_section_where" id="ssm_form_move_section_after"></td><td><label for="" id="ssm_form_move_section_after_label">After</label></td>' +
'                   <td><input type="radio" class="radio" name="ssm_form_move_section_where" id="ssm_form_move_section_in"></td><td><label for="" id="ssm_form_move_section_in_label">As child of</label></td>' +
'               </tr>' +
'           </table>' +
'       </td>' +
'   </tr>' +
'   <tr>' +
'       <td>' +
'           <span id="ssm_form_move_section_suffix">...the following section:</span>' +
'       </td>' +
'   </tr>' +
'   <tr>' +
'       <td id="ssm_form_move_section_hierarchy_parent">' +
'       </td>' +
'   </tr>' +
'</table>';

//***************************************************************************
