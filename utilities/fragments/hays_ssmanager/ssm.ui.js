/////////////////////////////////////////////////////////////////////////////
// 
// Solution  : SiteStudio
// Project   : Site Studio Manager (SSM)
//
// FileName  : ssm.ui.js
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

if (!SSM)
    throw "must include ssm.js before including this file"

//***************************************************************************

// namespace objects
SSM.UI = new Object();
SSM.UI.Forms = new Object();

// enumerated types
SSM.UI.ActionTab = {
    Section:'0',
    PrimaryLayout:'1',
    SecondaryLayout:'2',
    CustomProperties:'3'
}

SSM.UI.externalLayoutId = 'ssm.ui.externalLayout';

SSM.UI.visibleChildrenTotal = 26;
SSM.UI.useHttpGetForGetSiteDefinition = true;

//***************************************************************************
//***************************************************************************
//*************************** MESSAGE BOX METHODS ***************************
//***************************************************************************
//***************************************************************************

SSM.UI.DisplayErrorMessage = function(message, parent)   { SSAjax.DHTML.MessageBox.Show(                 message, SSM.GetString('ssmError'),       'ssm_error_message',   'ssm_error_message',   SSM.UI.FindMessageParent(parent)); }
SSM.UI.DisplayWarningMessage = function(message, parent) { SSAjax.DHTML.MessageBox.Show(                 message, SSM.GetString('ssmWarning'),     'ssm_warning_message', 'ssm_warning_message', SSM.UI.FindMessageParent(parent)); }
SSM.UI.DisplayInfoMessage = function(message, parent)    { SSAjax.DHTML.MessageBox.Show(                 message, SSM.GetString('ssmInformation'), 'ssm_info_message',    'ssm_info_message',    SSM.UI.FindMessageParent(parent)); }
SSM.UI.DisplayPrompt = function(value, message, parent)  { SSAjax.DHTML.MessageBox.ShowWithPrompt(value, message, SSM.GetString('ssmInformation'), 'ssm_info_message',    'ssm_info_message',    SSM.UI.FindMessageParent(parent)); }

SSM.UI.DisplayErrorMessageEx = function(message, title, style, callback, parent)   { SSAjax.DHTML.MessageBox.Show(                 message, title, 'ssm_error_message',   'ssm_error_message',   SSM.UI.FindMessageParent(parent), style, callback); }
SSM.UI.DisplayWarningMessageEx = function(message, title, style, callback, parent) { SSAjax.DHTML.MessageBox.Show(                 message, title, 'ssm_warning_message', 'ssm_warning_message', SSM.UI.FindMessageParent(parent), style, callback); }
SSM.UI.DisplayInfoMessageEx = function(message, title, style, callback, parent)    { SSAjax.DHTML.MessageBox.Show(                 message, title, 'ssm_info_message',    'ssm_info_message',    SSM.UI.FindMessageParent(parent), style, callback); }
SSM.UI.DisplayPromptEx = function(value, message, title, style, callback, parent)  { SSAjax.DHTML.MessageBox.ShowWithPrompt(value, message, title, 'ssm_info_message',    'ssm_info_message',    SSM.UI.FindMessageParent(parent), style, callback); }

SSM.UI.FindMessageParent = function(parent)
{
    if (SSAjax.IsValid(parent) && SSAjax.DHTML.IsVisible(parent))
        return parent;
    else if (SSAjax.IsValid(SSM.UI.main) && SSAjax.DHTML.IsVisible(SSM.UI.main))
        return SSM.UI.main;
    else
        return SSAjax.DHTML.GetBody();
}

//***************************************************************************
//***************************************************************************
//******************** TOP LEVEL ENABLE/DISABLE METHODS *********************
//***************************************************************************
//***************************************************************************

SSM.UI.Enable = function(b) { SSAjax.DHTML.Enable(SSM.UI.main, b); SSM.UI.EnableCheckForStateChanges(b);         }
SSM.UI.Disable = function() { SSAjax.DHTML.Enable(SSM.UI.main, false); SSM.UI.EnableCheckForStateChanges(false); }

SSM.UI.EnableAndUpdateState = function() { SSM.UI.Enable() ; SSM.UI.Update(false); }
SSM.UI.EnableAndUpdate = function() { SSM.UI.Enable() ; SSM.UI.Update(); }

//***************************************************************************
//***************************************************************************
//******************************* INITIALIZE ********************************
//***************************************************************************
//***************************************************************************

SSM.UI.Initialize = function()
{
    // set some ui component default values
    SSAjax.DHTML.DefaultDisabledLinkClassName = 'ssm_disabled_link';
    SSAjax.DHTML.DefaultDisabledTextBoxClassName = 'ssm_disabled_textbox';
    SSAjax.DHTML.DefaultDisabledCheckBoxClassName = 'ssm_disabled_checkbox';
    SSAjax.DHTML.DefaultDisabledComboBoxClassName = 'ssm_disabled_combobox';
    SSAjax.DHTML.DefaultDisabledRadioClassName = 'ssm_disabled_radio';
    SSAjax.DHTML.DefaultDisabledTreeCtrlClassName = 'ssm_disabled_treectrl';
    
    SSAjax.DHTML.MessageBox.defaultIcon = SSM.path + 'icons/ssm.gif';
    SSAjax.DHTML.Console.defaultIcon = SSM.path + 'icons/ssm.gif';

    if (SSAjax.IS_IE)
    {
        var sheet = document.styleSheets[0]; // see huge comment in SSAjax.DHTML.CreateCss() method
        sheet.disabled = true;
        SSAjax.DHTML.ProgressBar.CreateDefaultCssClasses(8, sheet);
        SSAjax.DHTML.MessageBox.CreateDefaultCssClasses(sheet);
        SSAjax.DHTML.ComboBox.CreateDefaultCssClasses(sheet);
        SSAjax.DHTML.TreeCtrl.CreateDefaultCssClasses(sheet);
        SSAjax.DHTML.TabCtrl.CreateDefaultCssClasses(sheet);
        SSAjax.DHTML.Menu.CreateDefaultCssClasses(sheet);
        SSAjax.DHTML.Console.CreateDefaultCssClasses(sheet);
        sheet.disabled = false;
    }
    else
    {
        SSAjax.DHTML.ProgressBar.CreateDefaultCssClasses(8);
        SSAjax.DHTML.MessageBox.CreateDefaultCssClasses();
        SSAjax.DHTML.ComboBox.CreateDefaultCssClasses();
        SSAjax.DHTML.TreeCtrl.CreateDefaultCssClasses();
        SSAjax.DHTML.TabCtrl.CreateDefaultCssClasses();
        SSAjax.DHTML.Menu.CreateDefaultCssClasses();
        SSAjax.DHTML.Console.CreateDefaultCssClasses();
    }

    // initialize UI components
    SSM.UI.main = SSAjax.DHTML.GetObject('ssm_main');
    SSM.UI.body = SSAjax.DHTML.GetObject('ssm_body');
    SSM.UI.title = SSAjax.DHTML.GetObject('ssm_title');
    SSM.UI.loadsite_link = SSAjax.DHTML.GetObject('ssm_loadsite_link');
    SSM.UI.logged_on_status = SSAjax.DHTML.GetObject('ssm_logged_on_status');
    SSM.UI.progress = new SSAjax.DHTML.ProgressBar('ssm_progress', 'ssm_progress', 20, 8, '&nbsp;&nbsp;', 'ssm_right_header');
    SSM.UI.hierarchy = new SSAjax.DHTML.TreeCtrl('ssm_hierarchy', 'ssm_hierarchy', 'ssm_hierarchy_parent');
    SSM.UI.properties = SSAjax.DHTML.GetObject('ssm_properties');
    SSM.UI.custom_properties = SSAjax.DHTML.GetObject('ssm_custom_properties');

    SSM.UI.actions_for_section = SSAjax.DHTML.GetObject('ssm_actions_for_section');
    SSM.UI.actions_for_primary_layout = SSAjax.DHTML.GetObject('ssm_actions_for_primary_layout');
    SSM.UI.actions_for_secondary_layout = SSAjax.DHTML.GetObject('ssm_actions_for_secondary_layout');
    SSM.UI.actions_for_custom_properties = SSAjax.DHTML.GetObject('ssm_actions_for_custom_properties');

    SSM.UI.actions = new SSAjax.DHTML.TabCtrl('ssm_actions', 'ssm_actions', 'ssm_actions_parent');
    SSM.UI.actions.OnAfterSelectTab =  SSM.UI.OnSelectActionTab;

    SSM.UI.actions.AddTab(SSM.GetString('ssmSectionTabLabel'), SSM.UI.ActionTab.Section, SSM.UI.actions_for_section);
    SSM.UI.actions.AddTab(SSM.GetString('ssmLayoutTabLabel'), SSM.UI.ActionTab.PrimaryLayout, SSM.UI.actions_for_primary_layout);
    SSM.UI.actions.AddTab(SSM.GetString('ssmSecondaryLayoutTabLabel'), SSM.UI.ActionTab.SecondaryLayout, SSM.UI.actions_for_secondary_layout);
    SSM.UI.actions.AddTab(SSM.GetString('ssmCustomPropertiesTabLabel'), SSM.UI.ActionTab.CustomProperties, SSM.UI.actions_for_custom_properties);
    SSM.UI.actions.SelectTab(SSM.UI.ActionTab.Section, false);
    
    SSM.UI.action_add_section = SSAjax.DHTML.GetObject('ssm_action_add_section');
    SSM.UI.action_add_section_link = SSAjax.DHTML.GetObject('ssm_action_add_section_link');
    SSM.UI.action_remove_section = SSAjax.DHTML.GetObject('ssm_action_remove_section');
    SSM.UI.action_remove_section_link = SSAjax.DHTML.GetObject('ssm_action_remove_section_link');
    SSM.UI.action_move_section = SSAjax.DHTML.GetObject('ssm_action_move_section');
    SSM.UI.action_move_section_link = SSAjax.DHTML.GetObject('ssm_action_move_section_link');
    SSM.UI.action_set_error_handler = SSAjax.DHTML.GetObject('ssm_action_set_error_handler');
    SSM.UI.action_set_error_handler_link = SSAjax.DHTML.GetObject('ssm_action_set_error_handler_link');
    SSM.UI.action_clear_error_handler = SSAjax.DHTML.GetObject('ssm_action_clear_error_handler');
    SSM.UI.action_clear_error_handler_link = SSAjax.DHTML.GetObject('ssm_action_clear_error_handler_link');
    SSM.UI.action_edit_properties = SSAjax.DHTML.GetObject('ssm_action_edit_properties');
    SSM.UI.action_edit_custom_properties = SSAjax.DHTML.GetObject('ssm_action_edit_custom_properties');
    
    SSM.UI.primary_layout_icon = SSAjax.DHTML.GetObject('ssm_primary_layout_icon');
    SSM.UI.primary_layout_label = SSAjax.DHTML.GetObject('ssm_primary_layout_label');
    SSM.UI.primary_layout_name = SSAjax.DHTML.GetObject('ssm_primary_layout_name');
    SSM.UI.primary_layout_info = SSAjax.DHTML.GetObject('ssm_primary_layout_info');
    SSM.UI.primary_layout_apply = SSAjax.DHTML.GetObject('ssm_primary_layout_apply');
    SSM.UI.primary_layout_clear = SSAjax.DHTML.GetObject('ssm_primary_layout_clear');
    SSM.UI.primary_layout_combo = new SSAjax.DHTML.ComboBox('ssm_primary_layout_combo', 'ssm_primary_layout_combo', 'ssm_primary_layout_combo_parent');
    SSM.UI.primary_layout_preview_frame = SSAjax.DHTML.GetObject('ssm_primary_layout_preview_frame');
    
    SSM.UI.secondary_layout_icon = SSAjax.DHTML.GetObject('ssm_secondary_layout_icon');
    SSM.UI.secondary_layout_label = SSAjax.DHTML.GetObject('ssm_secondary_layout_label');
    SSM.UI.secondary_layout_name = SSAjax.DHTML.GetObject('ssm_secondary_layout_name');
    SSM.UI.secondary_layout_info = SSAjax.DHTML.GetObject('ssm_secondary_layout_info');
    SSM.UI.secondary_layout_apply = SSAjax.DHTML.GetObject('ssm_secondary_layout_apply');
    SSM.UI.secondary_layout_clear = SSAjax.DHTML.GetObject('ssm_secondary_layout_clear');
    SSM.UI.secondary_layout_combo = new SSAjax.DHTML.ComboBox('ssm_secondary_layout_combo', 'ssm_secondary_layout_combo', 'ssm_secondary_layout_combo_parent');
    SSM.UI.secondary_layout_preview_frame = SSAjax.DHTML.GetObject('ssm_secondary_layout_preview_frame');
    
    SSM.UI.property_nodeId = SSAjax.DHTML.GetObject('ssm_property_nodeId');
    SSM.UI.property_label = SSAjax.DHTML.GetObject('ssm_property_label');
    SSM.UI.property_urlDirName = SSAjax.DHTML.GetObject('ssm_property_urlDirName');
    SSM.UI.property_urlPageName = SSAjax.DHTML.GetObject('ssm_property_urlPageName');
    SSM.UI.property_maxAge = SSAjax.DHTML.GetObject('ssm_property_maxAge');
    SSM.UI.property_active = SSAjax.DHTML.GetObject('ssm_property_active');
    SSM.UI.property_contributorOnly = SSAjax.DHTML.GetObject('ssm_property_contributorOnly');

    SSAjax.DHTML.AddEvent(SSM.UI.property_nodeId, 'change', SSM.UI.OnPropertyChanged);
    SSAjax.DHTML.AddEvent(SSM.UI.property_label, 'change', SSM.UI.OnPropertyChanged);
    SSAjax.DHTML.AddEvent(SSM.UI.property_urlDirName, 'change', SSM.UI.OnPropertyChanged);
    SSAjax.DHTML.AddEvent(SSM.UI.property_urlPageName, 'change', SSM.UI.OnPropertyChanged);
    SSAjax.DHTML.AddEvent(SSM.UI.property_maxAge, 'change', SSM.UI.OnPropertyChanged);
    SSAjax.DHTML.AddEvent(SSM.UI.property_active, 'click', SSM.UI.OnPropertyChanged);
    SSAjax.DHTML.AddEvent(SSM.UI.property_contributorOnly, 'click', SSM.UI.OnPropertyChanged);

    SSAjax.DHTML.AddEvent(SSM.UI.property_nodeId, 'keyup', SSM.UI.OnPropertyKeyUp);
    SSAjax.DHTML.AddEvent(SSM.UI.property_label, 'keyup', SSM.UI.OnPropertyKeyUp);
    SSAjax.DHTML.AddEvent(SSM.UI.property_urlDirName, 'keyup', SSM.UI.OnPropertyKeyUp);
    SSAjax.DHTML.AddEvent(SSM.UI.property_urlPageName, 'keyup', SSM.UI.OnPropertyKeyUp);
    SSAjax.DHTML.AddEvent(SSM.UI.property_maxAge, 'keyup', SSM.UI.OnPropertyKeyUp);

    SSAjax.DHTML.AddEvent(SSM.UI.primary_layout_info, 'click', SSM.UI.OnLayoutInfo);
    SSAjax.DHTML.AddEvent(SSM.UI.secondary_layout_info, 'click', SSM.UI.OnLayoutInfo);

    SSAjax.DHTML.AddEvent('ssm_help', 'click', SSM.UI.OnClickHelp);
    
    SSM.UI.primary_layout_combo.SetBeforeDropDownCallback(SSM.UI.OnBeforeDropDownLayoutCombo);
    SSM.UI.primary_layout_combo.SetAfterRollUpCallback(SSM.UI.OnAfterRollUpLayoutCombo);
    SSM.UI.primary_layout_combo.SetAfterSelectItemCallback(SSM.UI.OnAfterSelectItemLayoutCombo);

    SSM.UI.secondary_layout_combo.SetBeforeDropDownCallback(SSM.UI.OnBeforeDropDownLayoutCombo);
    SSM.UI.secondary_layout_combo.SetAfterRollUpCallback(SSM.UI.OnAfterRollUpLayoutCombo);
    SSM.UI.secondary_layout_combo.SetAfterSelectItemCallback(SSM.UI.OnAfterSelectItemLayoutCombo);
    
    SSM.UI.hierarchy.showRootItemExpandIcons = false;
    SSM.UI.hierarchy.dragDropEnabled = true;
    SSM.UI.hierarchy.OnBeforeExpandItem = SSM.UI.OnBeforeExpandSection;
    SSM.UI.hierarchy.OnBeforeSelectItem = SSM.UI.OnBeforeSelectSection;
    SSM.UI.hierarchy.OnAfterSelectItem = SSM.UI.OnAfterSelectSection;
    SSM.UI.hierarchy.OnBeforeDragItem = SSM.UI.OnBeforeDragSection;
    SSM.UI.hierarchy.OnBeforeDragOverItem = SSM.UI.OnBeforeDragOverSection;
    SSM.UI.hierarchy.OnBeforeDropItem = SSM.UI.OnBeforeDropSection;
    SSM.UI.hierarchy.OnContextMenu = SSM.UI.OnSectionContextMenu;

    SSM.UI.body.style.backgroundImage = 'url(' + SSM.path + 'icons/ssm_banner.gif)'; // cant put background image direct in .css because relative path wont work and absolute path wont be portable
    
    SSM.UI.LoadVisibleChildrenTotal();
    SSM.UI.LoadSiteDefinitionLoadOption();

    SSM.UI.LocalizeUserInterface();
}

//***************************************************************************
//***************************************************************************
//***************************** UN-INITIALIZE *******************************
//***************************************************************************
//***************************************************************************

SSM.UI.Uninitialize = function()
{ 
    SSAjax.DHTML.Hide(SSM.UI.body);

    SSM.UI.StopStateChangeTimer();
       
    SSM.UI.progress.RemoveFromDOM();
    SSM.UI.hierarchy.RemoveFromDOM();
    SSM.UI.actions.RemoveFromDOM();
    SSM.UI.primary_layout_combo.RemoveFromDOM();
    SSM.UI.secondary_layout_combo.RemoveFromDOM();
        
    SSM.UI.main = null;
    SSM.UI.body = null;
    SSM.UI.title = null;
    SSM.UI.loadsite_link = null;
    SSM.UI.logged_on_status = null;
    SSM.UI.progress = null;
    SSM.UI.hierarchy = null;
    SSM.UI.actions = null;
    SSM.UI.properties = null;
    SSM.UI.custom_properties = null;
    
    SSM.UI.actions_for_section = null;
    SSM.UI.actions_for_primary_layout = null;
    SSM.UI.actions_for_secondary_layout = null;
    SSM.UI.actions_for_custom_properties = null;
    
    SSM.UI.action_add_section = null;
    SSM.UI.action_add_section_link = null;
    SSM.UI.action_remove_section = null;
    SSM.UI.action_remove_section_link = null;
    SSM.UI.action_move_section = null;
    SSM.UI.action_move_section_link = null;
    SSM.UI.action_set_error_handler = null;
    SSM.UI.action_set_error_handler_link = null;
    SSM.UI.action_clear_error_handler = null;
    SSM.UI.action_clear_error_handler_link = null;
    SSM.UI.action_edit_properties = null;
    SSM.UI.action_edit_custom_properties = null;
    
    SSM.UI.primary_layout_icon = null;
    SSM.UI.primary_layout_label = null;
    SSM.UI.primary_layout_name = null;
    SSM.UI.primary_layout_info = null;
    SSM.UI.primary_layout_apply = null;
    SSM.UI.primary_layout_clear = null;
    SSM.UI.primary_layout_combo = null;
    SSM.UI.primary_layout_preview_frame = null;

    SSM.UI.secondary_layout_icon = null;
    SSM.UI.secondary_layout_label = null;
    SSM.UI.secondary_layout_name = null;
    SSM.UI.secondary_layout_info = null;
    SSM.UI.secondary_layout_apply = null;
    SSM.UI.secondary_layout_clear = null;
    SSM.UI.secondary_layout_combo = null;
    SSM.UI.secondary_layout_preview_frame = null;
    
    SSM.UI.property_nodeId = null;
    SSM.UI.property_label = null;
    SSM.UI.property_urlDirName = null;
    SSM.UI.property_urlPageName = null;
    SSM.UI.property_maxAge = null;
    SSM.UI.property_active = null;
    SSM.UI.property_contributorOnly = null;

    SSM.UI.StopTracing();

    SSM.UI.Forms = null;
    SSM.UI = null;
}

//***************************************************************************
//***************************************************************************
//***************************************************************************
//***************************************************************************

SSM.UI.ShowLoggedOnStatus = function(dUser)
{
    var currentNameObj = SSAjax.DHTML.GetObject('ssm_logged_on_name');
    var currentName = (currentNameObj ? currentNameObj.innerHTML : '');
    
    if (currentName != dUser)
    {
        var bAnonymous = String.CompareNoCase(dUser, 'anonymous');
    
        SSAjax.DHTML.ClearContent(SSM.UI.logged_on_status);

        if (bAnonymous)
        {
            SSAjax.DHTML.CreateSpan(null, 'ssm_not_logged_on', SSM.GetString('ssmNotLoggedOnMessage'), SSM.UI.logged_on_status);
            SSAjax.DHTML.CreateImg(null, null, SSM.path + 'icons/ssm_logged_off.gif', 'top', SSM.UI.logged_on_status);
        }
        else
        {
            SSAjax.DHTML.SetContent(SSM.UI.logged_on_status, SSM.GetString('ssmLoggedOnMessage', dUser));
            SSAjax.DHTML.CreateImg(null, null, SSM.path + 'icons/ssm_logged_on.gif', 'top', SSM.UI.logged_on_status);
        }
    }
}

//***************************************************************************
//***************************************************************************
//***************************************************************************
//***************************************************************************

SSM.UI.SetAppTitle = function(value)
{
    SSAjax.DHTML.SetContent(SSM.UI.title, value);
}

//***************************************************************************
//***************************************************************************
//***************************************************************************
//***************************************************************************

SSM.UI.LocalizeUserInterface = function()
{
    var obj = null;
    
    // Localize the user interface - 1. The Manager Header
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_loadsite_link'), SSM.GetString('ssmLoadSiteLinkLabel'));
    obj = SSAjax.DHTML.GetObject('ssm_help');
    if(obj != null)
    {
        obj.setAttribute('title', SSM.GetString('ssmHelpTitle'));
    }

    // Localize the user interface - 2. The Hierarchy Tree
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_hierarchy_label'), SSM.GetString('ssmHierarchyLabel'));

    // Localize the user interface - 3. The Section Tab
    var refTitle = SSM.GetString('ssmActionAddSectionTitle');
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_action_add_section'), SSM.GetString('ssmActionAddSection', refTitle));
    refTitle = SSM.GetString('ssmActionRemoveSectionTitle');
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_action_remove_section'), SSM.GetString('ssmActionRemoveSection', refTitle));
    refTitle = SSM.GetString('ssmActionMoveSectionTitle');
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_action_move_section'), SSM.GetString('ssmActionMoveSection', refTitle));
    refTitle = SSM.GetString('ssmActionSetErrorHandlerTitle');
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_action_set_error_handler'), SSM.GetString('ssmActionSetErrorHandler', refTitle));
    refTitle = SSM.GetString('ssmActionClearErrorHandlerTitle');
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_action_clear_error_handler'), SSM.GetString('ssmActionClearErrorHandler', refTitle));

    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_action_edit_properties'), SSM.GetString('ssmActionEditProperties'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_section_id_label'), SSM.GetString('ssmSectionIdLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_section_label_label'), SSM.GetString('ssmSectionLabelLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_include_in_navigation_label'), SSM.GetString('ssmIncludeInNavigationLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_contributor_only_label'), SSM.GetString('ssmContributorOnlyLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_url_directory_name_label'), SSM.GetString('ssmUrlDirectoryNameLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_url_page_name_label'), SSM.GetString('ssmUrlPageNameLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_maximum_age_label'), SSM.GetString('ssmMaximumAgeLabel'));

    // Localize the user interface - 4. The Layout Tab
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_preview_layout_label'), SSM.GetString('ssmPreviewLayoutLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_primary_layout_apply'), SSM.GetString('ssmLayoutApplyLabel'));
    SSAjax.DHTML.GetObject('ssm_primary_layout_apply');
    if(obj != null)
    {
        obj.setAttribute('title', SSM.GetString('ssmLayoutApplyTitle'));
    }
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_primary_layout_clear'), SSM.GetString('ssmLayoutClearLabel'));
    SSAjax.DHTML.GetObject('ssm_primary_layout_clear');
    if(obj != null)
    {
        obj.setAttribute('title', SSM.GetString('ssmLayoutClearTitle'));
    }

    // Localize the user interface - 5. The Secondary Layout Tab
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_preview_secondary_layout_label'), SSM.GetString('ssmPreviewSecondaryLayoutLabel'));
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_secondary_layout_apply'), SSM.GetString('ssmSecondaryLayoutApplyLabel'));
    SSAjax.DHTML.GetObject('ssm_secondary_layout_apply');
    if(obj != null)
    {
        obj.setAttribute('title', SSM.GetString('ssmSecondaryLayoutApplyTitle'));
    }
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_secondary_layout_clear'), SSM.GetString('ssmSecondaryLayoutClearLabel'));
    SSAjax.DHTML.GetObject('ssm_secondary_layout_clear');
    if(obj != null)
    {
        obj.setAttribute('title', SSM.GetString('ssmSecondaryLayoutClearTitle'));
    }
    
    // Localize the user interface - 6. The Custom Properties Tab
    SSAjax.DHTML.SetContent(SSAjax.DHTML.GetObject('ssm_action_edit_custom_properties'), SSM.GetString('ssmActionEditCustomProperties'));
    
    // Localize the user interface - 7. The Tab Labels
    SSM.UI.actions.SetTabLabel(SSM.UI.ActionTab.Section, SSM.GetString('ssmSectionTabLabel'));
    SSM.UI.actions.SetTabLabel(SSM.UI.ActionTab.PrimaryLayout, SSM.GetString('ssmLayoutTabLabel'));
    SSM.UI.actions.SetTabLabel(SSM.UI.ActionTab.SecondaryLayout, SSM.GetString('ssmSecondaryLayoutTabLabel'));
    SSM.UI.actions.SetTabLabel(SSM.UI.ActionTab.CustomProperties, SSM.GetString('ssmCustomPropertiesTabLabel'));
}

//***************************************************************************
//***************************************************************************
//************************ HIERARCHY TREE UI METHODS ************************
//***************************************************************************
//***************************************************************************

SSM.UI.LoadHierarchyTree = function(tree, selectedSectionId)
{
    tree.PreserveState(); // keep track of any existing selected/expanded sections
    
    SSM.UI.PreserveExpandedItemsStartIndex(tree);

    // figure out where to start populating the hierarchy tree
    var startingSectionId = SSM.currentSite.rootSectionId; // assume the root section
    if (SSM.settings.general.hierarchyStartAtCurrentSection)
        if (SSM.currentSite.GetSection(SSM.nodeId) != null)
            startingSectionId = SSM.nodeId; // unless we are only showing the current section (and the current section exists)

    tree.DeleteAllItems(); // clear out existing content...
    SSM.UI.CreateSection(tree, startingSectionId); // ... recreate tree hierarchy
    tree.ExpandItem(startingSectionId); // ... and expand the starting section if desired

    // because we create sections in the tree on the fly, as they are expanded, the tree ctrl will
    // have trouble automatically selecting the previously selected item unless we ensure it exists
    // in the tree BEFORE we do the .RestoreState()
    SSM.UI.EnsureSectionExists(tree, tree.previouslySelectedItemId);
    tree.RestoreState();

    SSM.UI.RestoreExpandedItemsStartIndex(tree)
    
    // if still nothing selected, use current section (if it exists), otherwise use the starting section
    if (tree.IsNothingSelected())
    {
        SSM.UI.EnsureSectionExists(tree, selectedSectionId);
        tree.SelectItem(selectedSectionId, false);
    }
}

//***************************************************************************

SSM.UI.PreserveExpandedItemsStartIndex = function(tree)
{
    for (var n=0; n<tree.previouslyExpandedItems.length; n++)
    {
        var item = tree.__GetItem(tree.previouslyExpandedItems[n]);
        if (item && item.start && item.start > 0)
        {
            tree.previouslyExpandedItems_StartIndex = tree.previouslyExpandedItems_StartIndex || {};
            tree.previouslyExpandedItems_StartIndex[n.toString()] = item.start;
        }
    }
}

//***************************************************************************

SSM.UI.RestoreExpandedItemsStartIndex = function(tree)
{
    for (var n=0; n<tree.previouslyExpandedItems.length; n++)
    {
        var section = SSM.currentSite.GetSection(tree.previouslyExpandedItems[n]);
        if (section && tree.previouslyExpandedItems_StartIndex && 
            tree.previouslyExpandedItems_StartIndex[n.toString()] && 
            tree.previouslyExpandedItems_StartIndex[n.toString()] > 0)
        {
            SSM.UI.MoveDown(tree, section.id, tree.previouslyExpandedItems_StartIndex[n.toString()]);
            tree.SelectItem(null);
        }
    }
}

//***************************************************************************

SSM.UI.CreateSection = function(tree, section, parentItem)
{
    section = SSM.currentSite.GetSection(section);

    if (!tree.ItemExists(section.id))
    {
        var label = section.label.replace(/ /gi, '&nbsp;');
        
        var icon;
        if (section.id == SSM.currentSite.rootSectionId)
            icon = SSM.path + 'icons/ssm_site.gif';
        else if (section.active && (section.id != SSM.currentSite.errorSectionId))
            icon = SSM.path + 'icons/ssm_section_active.gif';
        else if (!section.active && (section.id != SSM.currentSite.errorSectionId))
            icon = SSM.path + 'icons/ssm_section_inactive.gif';
        else if (section.active && (section.id == SSM.currentSite.errorSectionId))
            icon = SSM.path + 'icons/ssm_errorhandler_active.gif';
        else if (!section.active && (section.id == SSM.currentSite.errorSectionId))
            icon = SSM.path + 'icons/ssm_errorhandler_inactive.gif';

        var item = tree.AddItem(label, icon, section.id, section.subSections.length > 0, parentItem);
    }
}

//***************************************************************************

SSM.UI.CreateSectionsChildren = function(tree, section)
{
    section = SSM.currentSite.GetSection(section);

    if (tree.GetItemCount(section.id) != section.subSections.length)
    {
        section.subSections.start = section.subSections.start || 0;
        
        var start = section.subSections.start;
        var end = start + SSM.UI.visibleChildrenTotal;
        end = (end < section.subSections.length ? end : section.subSections.length);
        
        if (start > 0 && !tree.ItemExists(section.id + '-ssm-up-arrow'))
        {
            tree.AddItem('', SSM.path + 'icons/ssm_move_up.gif', section.id + '-ssm-up-arrow', false, section.id, false, false);
        }
        
        var item = tree.__GetItem(section.id); // stash the virtual list start index with the parent section
        if (item)
        {
            item.start = start;
        }
    
        for (var i = start; i < end; i++)
        {
            SSM.UI.CreateSection(tree, section.subSections[i], section.id);
        }
        
        if (section.subSections.length > end && !tree.ItemExists(section.id + '-ssm-down-arrow'))
        {
	       tree.AddItem('', SSM.path + 'icons/ssm_move_dn.gif', section.id + '-ssm-down-arrow', false, section.id, false, false);
        }
    }
}

//***************************************************************************

SSM.UI.MoveDown = function(tree, sectionId, pos)
{
    sectionId = sectionId.replace(/-ssm-down-arrow/, '');
    var section = SSM.currentSite.GetSection(sectionId);
    
    if (pos)
    {
        section.subSections.start = pos;
    }
    else
    {
        section.subSections.start = section.subSections.start || 0;
        section.subSections.start += parseInt(SSM.UI.visibleChildrenTotal / 2);
    }
    
    if (section.subSections.start > section.subSections.length)
    {
        section.subSections.start = section.subSections.length - SSM.UI.visibleChildrenTotal;
        section.subSections.start = (section.subSections.start >= 0 ? section.subSections.start : 0);
    }
    
    var item = tree.__GetItem(sectionId, '_children');
    
    while (item.childNodes.length > 0)
        SSAjax.DHTML.RemoveElementFromDOM(item.childNodes[0]);
        
    SSM.UI.CreateSectionsChildren(tree, sectionId);
}

//***************************************************************************

SSM.UI.MoveUp = function(tree, sectionId)
{
    sectionId = sectionId.replace(/-ssm-up-arrow/, '');
    var section = SSM.currentSite.GetSection(sectionId);
    
    section.subSections.start = section.subSections.start || 0;
    section.subSections.start -= parseInt(SSM.UI.visibleChildrenTotal / 2);
    section.subSections.start = (section.subSections.start > 0 ? section.subSections.start : 0);
        
    var item = tree.__GetItem(sectionId, '_children');
    
    while (item.childNodes.length > 0)
        SSAjax.DHTML.RemoveElementFromDOM(item.childNodes[0]);
      
    SSM.UI.CreateSectionsChildren(tree, sectionId);
}

//***************************************************************************

SSM.UI.EnsureSectionExists = function(tree, section)
{
    section = SSM.currentSite.GetSection(section);

    if (section && !tree.ItemExists(section.id)) // section exists, but not in hierarchy tree
    {
        SSM.UI.EnsureSectionExists(tree, section.parentSectionId); // work all way up parent chain... 
        
        var parentSection = SSM.currentSite.GetSection(section.parentSectionId);
        var index = SSM.UI.Find(parentSection.subSections, section.id);
        if (index > SSM.UI.visibleChildrenTotal) 
        {
            // create virtual list for this section
            
            if (index < parentSection.subSections.length &&
                index > (parentSection.subSections.length - SSM.UI.visibleChildrenTotal) && 
                (parentSection.subSections.length - SSM.UI.visibleChildrenTotal) > 0)
            {
                SSM.UI.MoveDown(tree, section.parentSectionId, parentSection.subSections.length - SSM.UI.visibleChildrenTotal);
            }
            else
            {
                SSM.UI.MoveDown(tree, section.parentSectionId, index);
            }
        }
        else
        {
            SSM.UI.CreateSectionsChildren(tree, section.parentSectionId); // ...before creating desired section and all its siblings
        }
    }
}

//***************************************************************************

SSM.UI.OnBeforeExpandSection = function(tree, sectionId)
{
    SSM.UI.CreateSectionsChildren(tree, sectionId);
    return true;
}

//***************************************************************************

SSM.UI.OnBeforeSelectSection = function(tree, sectionId)
{
    if (SSAjax.IsValid(sectionId))
    {
        if (sectionId.search(/-ssm-up-arrow/) > 0)
        {
            SSM.UI.MoveUp(tree, sectionId);
            return false;
        }

        if (sectionId.search(/-ssm-down-arrow/) > 0)
        {
            SSM.UI.MoveDown(tree, sectionId);
            return false;
        }
    }
    
    return true;
}

//***************************************************************************

SSM.UI.OnAfterSelectSection = function(tree, sectionId)
{
    if (SSAjax.IsValid(sectionId)) // ignore null selections (such as when doing DeleteAllItems() on the hierarchy)
        SSM.UI.Update();
}

//***************************************************************************

SSM.UI.OnBeforeDragSection = function(tree, dragItemId, e)
{
    if (dragItemId == SSM.currentSite.rootSectionId)
        return false; // dont let them drag root section anywhere

    if (SSM.settings.general.hierarchyStartAtCurrentSection && (dragItemId == SSM.nodeId))
        return false; // dont let them drag current section if settings = "showCurrentSectionOnly"
                
    var settings = SSM.settings.forSection(dragItemId);
    
    if (settings.moveSection.isHidden || !settings.moveSection.canBeSource)
        return false;
    else
        return true;
}

//***************************************************************************

SSM.UI.OnBeforeDragOverSection = function(tree, dragItemId, dropItemId, e)
{
    if (dropItemId == SSM.currentSite.rootSectionId)
        return false; // dont let them drop on root section
        
    if (SSM.settings.general.hierarchyStartAtCurrentSection && (dropItemId == SSM.nodeId))
        return false; // dont let them drop on current section if settings = "showCurrentSectionOnly"
        
    return SSM.settings.forSection(dropItemId).moveSection.canBeTarget;
}

//***************************************************************************

SSM.UI.OnBeforeDropSection = function(tree, dragItemId, dropItemId, e)
{
    SSM.UI.OnBeforeDropSection.dragItemId = dragItemId;
    SSM.UI.OnBeforeDropSection.dropItemId = dropItemId;

    var dragItem = SSM.currentSite.GetSection(dragItemId);
    var dropItem = SSM.currentSite.GetSection(dropItemId);
    
    var menu = new SSAjax.DHTML.Menu('ssm_section_contextmenu', 'ssm_section_contextmenu', true, SSM.UI.main);
    
    menu.AddItem(SSM.GetString("ssmMoveBeforeSection", dropItem.label), 'before');
    menu.AddItem(SSM.GetString("ssmMoveAfterSection", dropItem.label), 'after');
    menu.AddItem(SSM.GetString("ssmMoveIntoSection", dropItem.label), 'in');
    menu.AddSeparator();
    menu.AddItem(SSM.GetString("ssmCancelMoveSection"), 'cancel');

    menu.OnAfterSelectItem = SSM.UI.OnBeforeDropSection2;
    menu.OnAfterCancel = SSM.UI.OnBeforeDropSectionCancelled;

    SSM.UI.Disable();

    menu.MoveTo(SSAjax.DHTML.GetEventMouseXPos(e) - 10, SSAjax.DHTML.GetEventMouseYPos(e) - 10);
    menu.Show();
    
    SSM.UI.EnsureSectionMenuIsVisible(menu);    
    
    return false; // prevent droptarget highlighting from being cleared... means I'm responsible for calling FinishDragDropAction()
}

SSM.UI.OnBeforeDropSectionCancelled = function(itemId)
{
    SSM.UI.hierarchy.FinishDragDropAction();
    SSM.UI.Enable();
}

SSM.UI.OnBeforeDropSection2 = function(itemId)
{
    SSM.UI.hierarchy.FinishDragDropAction();
    SSM.UI.Enable();

    var dragItem = SSM.currentSite.GetSection(SSM.UI.OnBeforeDropSection.dragItemId);
    var dropItem = SSM.currentSite.GetSection(SSM.UI.OnBeforeDropSection.dropItemId);

    var newParentId = null;
    var insertAfterId = null;
    
    switch(itemId)
    {
        case 'before':
            SSM.MoveSection3(dragItem.id,
                                  dropItem.parentSectionId,
                                  SSM.currentSite.GetPreviousSiblingId(dropItem.id));
            break;
            
        case 'after':
            SSM.MoveSection3(dragItem.id,
                                  dropItem.parentSectionId,
                                  dropItem.id);
            break;
            
        case 'in':
            SSM.MoveSection3(dragItem.id,
                                  dropItem.id,
                                  SSM.currentSite.GetLastChildSectionId(dropItem));
            break;
            
        case 'cancel':
            break;
    }
}

//***************************************************************************

SSM.UI.OnSectionContextMenu = function(tree, sectionId, e)
{
    var section = SSM.currentSite.GetSection(sectionId);
    
    if (section)
    {
        var settings = SSM.settings.forSection(sectionId);

        var bIsRootSection = (sectionId == SSM.currentSite.rootSectionId);
        var bIsCurrentSection = (section.id == SSM.nodeId);
        var bIsCurrentSectionRoot = (SSM.settings.general.hierarchyStartAtCurrentSection && bIsCurrentSection);
        
        var bShowActive = !bIsRootSection && !settings.editProperties.isHidden && !settings.editProperties.isActiveHidden && !settings.editProperties.isActiveDisabled;
        var bShowAdd = !settings.addSection.isHidden;
        var bShowRemove = !bIsRootSection && !settings.removeSection.isHidden;
        var bShowMove = !bIsRootSection && !bIsCurrentSectionRoot && !settings.moveSection.isHidden && settings.moveSection.canBeSource;
        var bShowError = !bIsRootSection && !settings.setErrorHandler.isHidden;
        
        if (bShowActive || bShowAdd || bShowRemove || bShowMove || bShowError)
        {
            SSM.UI.hierarchy.SelectItem(section.id);
            SSM.UI.Disable();

            var bIsErrorSection = (section.id == SSM.currentSite.errorSectionId);
            var bIsActive = section.active;
    
            var menu = new SSAjax.DHTML.Menu('ssm_section_contextmenu', 'ssm_section_contextmenu', true, SSM.UI.main);

            menu.OnAfterSelectItem = SSM.UI.OnSectionContextMenuItemSelected;
            menu.OnAfterCancel = SSM.UI.OnSectionContextMenuCancelled;

            if (bShowActive)
            {
                menu.AddCheckedItem(SSM.GetString('ssmIncludeSectionInNavigation'), (bIsActive ? 'clear_active' : 'set_active'), bIsActive);
                menu.AddSeparator();
            }

            if (bShowAdd)
                menu.AddItem(SSM.GetString('ssmAddNewSection'), 'add');
            
            if (bShowRemove)
                menu.AddItem(SSM.GetString('ssmRemoveThisSection'), 'remove');
                
            if (bShowMove)
                menu.AddItem(SSM.GetString('ssmMoveThisSection'), 'move');

            if (bShowAdd || bShowRemove || bShowMove)
                menu.AddSeparator();

            if (bShowError)
                menu.AddCheckedItem(SSM.GetString('ssmSetSectionAsErrorHandler'), (bIsErrorSection ? 'clear_error_handler' : 'set_error_handler'), bIsErrorSection);
        
            menu.MoveTo(SSAjax.DHTML.GetEventMouseXPos(e) - 10, SSAjax.DHTML.GetEventMouseYPos(e) - 10);
            menu.Show();

            SSM.UI.EnsureSectionMenuIsVisible(menu);
        }
    }
    
    return false;
}

SSM.UI.OnSectionContextMenuCancelled = function()
{
    SSM.UI.Enable();
}

SSM.UI.OnSectionContextMenuItemSelected = function(itemId)
{
    SSM.UI.Enable();

    switch(itemId)
    {
        case 'add':
            SSM.AddSection();
            break;
            
        case 'remove':
            SSM.RemoveSection();
            break;

        case 'move':
            SSM.MoveSection();
            break;
                        
        case 'set_error_handler':
            SSM.SetErrorHandler();
            break;
            
        case 'clear_error_handler':
            SSM.ClearErrorHandler();
            break;
            
        case 'set_active':
            SSM.SetActive();
            break;

        case 'clear_active':
            SSM.ClearActive();
            break;
    }
}

SSM.UI.EnsureSectionMenuIsVisible = function(menu)
{
    if (SSAjax.IsValid(menu))
    {
        var win = window;
        var de = win.document.documentElement || win.document;

        var viewPortHeight = win.innerHeight || (de && de.clientHeight) || (win.document.body && win.document.body.clientHeight) || 0;
        var scrollY = win.pageYOffset || (de && de.scrollTop) || (win.document.body && win.document.body.scrollTop) || 0;
        if (SSAjax.IsValid(viewPortHeight) && SSAjax.IsValid(scrollY))
        {
            var pageBottom = viewPortHeight + scrollY;
            var menuHeight = menu.table.offsetHeight || SSAjax.ToInt(SSAjax.DHTML.GetStyle(menu.table, 'height'), 0);
            if (SSAjax.IsValid(pageBottom) && SSAjax.IsValid(menuHeight))
            {
                var menuYPos = SSAjax.DHTML.GetTop(menu.table);
                var menuBottom = menuHeight + menuYPos;

                if (SSAjax.IsValid(menuYPos) && SSAjax.IsValid(menuBottom) && (menuBottom > pageBottom))
                {
                    win.scrollTo(0, scrollY + (menuBottom - pageBottom));
                }
            }
        }
    }
};

//***************************************************************************
//***************************************************************************
//********************** ACTIONS TAB CTRL UI METHODS ************************
//***************************************************************************
//***************************************************************************

SSM.UI.OnSelectActionTab = function()
{
    SSM.UI.Update();
}

//***************************************************************************
//***************************************************************************
//******************* PROPERTIES ACTIONS PANE UI METHODS ********************
//***************************************************************************
//***************************************************************************

SSM.UI.LoadCustomProperties = function()
{
    if (SSM.currentSite.customSectionPropertyDefinitions.GetRowCount() == 0)
    {
        SSM.UI.actions.HideTab(SSM.UI.ActionTab.CustomProperties);
    }
    else
    {
        SSM.UI.actions.ShowTab(SSM.UI.ActionTab.CustomProperties);

        SSAjax.DHTML.DeleteTableRows(SSM.UI.custom_properties);

        var row = SSAjax.DHTML.AddTableRow(SSM.UI.custom_properties);
        var cell1 = SSAjax.DHTML.CreateElement('th', null, 'ssm_custom_property_buffer', '&nbsp;', row);
        var cell2 = SSAjax.DHTML.CreateElement('th', null, 'ssm_custom_property_name', SSM.GetString('ssmCustomPropertiesNameLabel'), row);
        var cell3 = SSAjax.DHTML.CreateElement('th', null, 'ssm_custom_property_value', SSM.GetString('ssmCustomPropertiesValueLabel'), row);		
		// *************CUSTOMISATION START : added another column for action
        var cell4 = SSAjax.DHTML.CreateElement('th', null, 'ssm_custom_property_value',  'Action',row);
		// *************CUSTOMISATION END

        for (var i = 0 ; i < SSM.currentSite.customSectionPropertyDefinitions.GetRowCount() ; i++)
        {
            var name = SSM.currentSite.customSectionPropertyDefinitions.GetRow(i).GetField('name');
            var type = SSM.currentSite.customSectionPropertyDefinitions.GetRow(i).GetField('type');

            row = SSAjax.DHTML.AddTableRow(SSM.UI.custom_properties);
            cell1 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_buffer', '&nbsp;', row);
            cell2 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_name', name, row);
            cell3 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_value', '', row);
            SSM.UI.LoadCustomPropertyValue(cell3, name, type);
			// *************CUSTOMISATION START
            cell4 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_value', '', row);
			SSM.UI.LoadCustomPropertyAction(cell4, name, type);
			// *************CUSTOMISATION END
        }

        row = SSAjax.DHTML.AddTableRow(SSM.UI.custom_properties);
        cell1 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_buffer', '&nbsp;', row);
        cell2 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_name', '&nbsp;', row);
        cell3 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_value', '&nbsp;', row);
		// *************CUSTOMISATION START
        cell4 = SSAjax.DHTML.CreateElement('td', null, 'ssm_custom_property_value', '&nbsp;', row);
		// *************CUSTOMISATION END
    }
}

//***************************************************************************

// *************CUSTOMISATION START
SSM.UI.LoadCustomPropertyAction = function(cell, name, type) {

	if( validTaxonomyFields.indexOf(name) >= 0   ) {
		var link = SSAjax.DHTML.CreateA('ssm_customproperty_link_'+name, 'a', "javascript:void(0)", "Update", cell);
		return;
	}
	else {
		SSAjax.DHTML.CreateTextBox('ssm_customproperty_' + name, 'text', null, false, cell);
	}
}
// *************CUSTOMISATION END

SSM.UI.LoadCustomPropertyValue = function(cell, name, type)
{
	// *************CUSTOMISATION START: for taxonomy create DIV to display the value and hidden text input to hold the real value
	if( validTaxonomyFields.indexOf(name) >= 0) {
		var hiddenValue = SSAjax.DHTML.CreateTextBox('ssm_customproperty_hidden_'+name, "text", null, true, cell);
		SSAjax.DHTML.Hide(hiddenValue);
		var textarea  = SSAjax.DHTML.CreateDiv( 'ssm_customproperty_' + name, "div", "",  cell);
		return;
	}
// *************CUSTOMISATION END

    switch(type.toLowerCase())
    {
        case 'boolean':
            var check = SSAjax.DHTML.CreateCheckbox('ssm_customproperty_' + name, 'checkbox', null, false, false, cell);
            SSAjax.DHTML.AddEvent(check, 'change', SSM.UI.OnPropertyChanged);
            break;

        case 'managedurl':
        case 'manageddoc':
        case 'managedquery':
        case 'text':
        case 'bigtext':
        case 'integer':
        case 'float':
        case 'size':
        case 'color':
        case 'url':
        case 'cssstyle':
        case 'siteid':
        case 'nodeid':
            var textbox = SSAjax.DHTML.CreateTextBox('ssm_customproperty_' + name, 'text', null, false, cell);
            SSAjax.DHTML.AddEvent(textbox, 'change', SSM.UI.OnPropertyChanged);
            SSAjax.DHTML.AddEvent(textbox, 'keyup', SSM.UI.OnPropertyKeyUp);
            break;
    }
}

//***************************************************************************

SSM.UI.OnPropertyKeyUp = function(e)
{
    if (SSAjax.IS_IE) // IE bugfix - enter key does not automatically trigger the onchange event
    {
        if (SSAjax.DHTML.GetEventKeyCode(e) == 13)
        {   
            SSM.UI.OnPropertyChanged(e);
            return SSAjax.DHTML.CancelEvent(e);
        }
    }
}

//***************************************************************************

SSM.UI.OnPropertyChanged = function(e)
{
    var target = SSAjax.DHTML.GetEventTarget(e);

    var ssmProperty = 'ssm_property_';
    var ssmCustomProperty = 'ssm_customproperty_';

    var bStandardProperty = (target.id.indexOf(ssmProperty) == 0);
    var bCustomProperty = (target.id.indexOf(ssmCustomProperty) == 0);
   
    var name = (bStandardProperty ? target.id.substr(ssmProperty.length) : target.id.substr(ssmCustomProperty.length));
    var value = (target.type == 'checkbox' ? target.checked : target.value);

    var bIsLabel = String.CompareNoCase(name, 'label');
    var bIsUrlDirName = String.CompareNoCase(name, 'urldirname');
    var bIsUrlPageName = String.CompareNoCase(name, 'urlpagename');

    if (bIsLabel)
    {
        var labelTrimmed = String.Trim(value);
        if (labelTrimmed.length == 0)
        {
            SSM.UI.DisplayErrorMessage(SSM.GetString('ssmSectionLabelIsMissing'));
            return SSAjax.DHTML.CancelEvent(e);
        }
    }
    
    if (bIsUrlDirName || bIsUrlPageName)
    {
        var prop = (bIsUrlDirName ? SSM.UI.property_urlDirName : SSM.UI.property_urlPageName);
        var oldValue = (bIsUrlDirName ? SSM.SelectedSection().urlDirName : SSM.SelectedSection().urlPageName);

        var pos = value.FindOneOf(SSM.UI.Forms.AddNewSection.InvalidSectionIdChars);
        if (pos >= 0)
        {
            SSM.UI.DisplayErrorMessage(SSM.GetString('ssmInvalidCharacterIn', value.charAt(pos), name));
            return SSAjax.DHTML.CancelEvent(e);
        }
    }

    SSM.SaveSectionProperty(name, value);
}

//***************************************************************************
//***************************************************************************
//******************* UPDATE UI WITH LATEST CONTENT/STATE *******************
//***************************************************************************
//***************************************************************************

SSM.UI.Update = function(bUpdateContent)
{
    bUpdateContent = SSAjax.ToBool(bUpdateContent, true);

    SSM.UI.actions.ShowTab(SSM.UI.ActionTab.PrimaryLayout, !SSM.SelectedSectionSettings().primaryLayout.isHidden);
    SSM.UI.actions.ShowTab(SSM.UI.ActionTab.SecondaryLayout, !SSM.SelectedSectionSettings().secondaryLayout.isHidden);
    SSM.UI.actions.ShowTab(SSM.UI.ActionTab.CustomProperties, !SSM.SelectedSectionSettings().editCustomProperties.isHidden && (SSM.currentSite.customSectionPropertyDefinitions.GetRowCount() > 0));

    if (bUpdateContent && SSAjax.IsValid(SSM.SelectedSection()))
    {
        var label = SSM.SelectedSection().label.replace(/ /gi, '&nbsp;');
        
	SSAjax.DHTML.SetContent('ssm_actions_for_section_title', SSM.GetString('ssmActionsForSectionTitle'));
	SSAjax.DHTML.SetContent('ssm_actions_for_primary_layout_title', SSM.GetString('ssmActionsForPrimaryLayoutTitle'));
	SSAjax.DHTML.SetContent('ssm_actions_for_secondary_layout_title', SSM.GetString('ssmActionsForSecondaryLayoutTitle'));
	SSAjax.DHTML.SetContent('ssm_actions_for_custom_properties_title', SSM.GetString('ssmActionsForCustomPropertiesTitle'));

        SSAjax.DHTML.SetContent('ssm_actions_for_section_label', label);
        SSAjax.DHTML.SetContent('ssm_actions_for_primary_layout_label', label);
        SSAjax.DHTML.SetContent('ssm_actions_for_secondary_layout_label', label);
        SSAjax.DHTML.SetContent('ssm_actions_for_custom_properties_label', label);
    }

    SSM.UI.UpdateUrl();
    SSM.UI.UpdateTitle();

    switch(SSM.UI.actions.selectedTabId)
    {
        case SSM.UI.ActionTab.Section:
            SSM.UI.UpdateActionsForSection(bUpdateContent);
            break;

        case SSM.UI.ActionTab.PrimaryLayout:
        case SSM.UI.ActionTab.SecondaryLayout:
            SSM.UI.UpdateActionsForLayout(bUpdateContent);
            break;
            
        case SSM.UI.ActionTab.CustomProperties:
            SSM.UI.UpdateActionsForCustomProperties(bUpdateContent);
		
            break;
    }
}

//***************************************************************************

SSM.UI.UpdateUrl = function()
{
    if (SSM.settings.general.updateUrl)
    {
        SSM.UI.EnableCheckForStateChanges(false);
        document.location.hash = SSM.UI.SaveState();
        SSM.UI.EnableCheckForStateChanges(true);
    }
}

//***************************************************************************

SSM.UI.UpdateTitle = function()
{
    if (SSM.settings.general.updateTitle)
    {
        var title = document.title;
        if (title.indexOf(' #') >= 0)
            title = title.substr(0, title.indexOf(' #'));
        
        switch(SSM.UI.actions.selectedTabId)
        {
            case SSM.UI.ActionTab.Section:
                document.title = title + SSM.GetString("ssmEditingSection", SSM.SelectedSection().label);
                break;

            case SSM.UI.ActionTab.PrimaryLayout:
                document.title = title + SSM.GetString("ssmEditingLayout", SSM.SelectedSection().label);
                break;
            
            case SSM.UI.ActionTab.SecondaryLayout:
                document.title = title + SSM.GetString("ssmEditingSecondaryLayout", SSM.SelectedSection().label);
                break;
            
            case SSM.UI.ActionTab.CustomProperties:
                document.title = title + SSM.GetString("ssmEditingCustomProperties", SSM.SelectedSection().label);
                break;
        }
    }
}

//***************************************************************************
//***************************************************************************
//********************* UPDATE ACTIONS FOR SECTION PANE *********************
//***************************************************************************
//***************************************************************************

SSM.UI.UpdateActionsForSection = function(bUpdateContent)
{
    var section = SSM.SelectedSection();

    var settings = SSM.settings.forSection(section.id);
        
    var bIsRootSection = (section.id == SSM.currentSite.rootSectionId);
    var bIsCurrentSection = (section.id == SSM.nodeId);
    var bIsCurrentSectionRoot = (SSM.settings.general.hierarchyStartAtCurrentSection && bIsCurrentSection);
    var bIsErrorSection = (section.id == SSM.currentSite.errorSectionId);
    var bIsActive = section.active;
    var bIsContribOnly = section.contributorOnly;

    SSAjax.DHTML.Enable(SSM.UI.action_remove_section_link, !bIsRootSection);
    SSAjax.DHTML.Enable(SSM.UI.action_move_section_link, !bIsRootSection && !bIsCurrentSectionRoot);
    SSAjax.DHTML.Enable(SSM.UI.action_set_error_handler_link, !bIsRootSection && !bIsErrorSection);
    SSAjax.DHTML.Enable(SSM.UI.action_clear_error_handler_link, !bIsRootSection && bIsErrorSection);

    SSAjax.DHTML.ShowListItem(SSM.UI.action_add_section, !settings.addSection.isHidden && !settings.addSection.isActionItemHidden);
    SSAjax.DHTML.ShowListItem(SSM.UI.action_remove_section, !settings.removeSection.isHidden && !settings.removeSection.isActionItemHidden);
    SSAjax.DHTML.ShowListItem(SSM.UI.action_move_section, !settings.moveSection.isHidden && !settings.moveSection.isActionItemHidden && settings.moveSection.canBeSource);
    SSAjax.DHTML.ShowListItem(SSM.UI.action_set_error_handler, !settings.setErrorHandler.isHidden && !settings.setErrorHandler.isActionItemHidden && !bIsErrorSection);
    SSAjax.DHTML.ShowListItem(SSM.UI.action_clear_error_handler, !settings.setErrorHandler.isHidden && !settings.setErrorHandler.isActionItemHidden && bIsErrorSection);
    SSAjax.DHTML.ShowListItem(SSM.UI.action_edit_properties, !settings.editProperties.isHidden && !settings.editProperties.isActionItemHidden);
    SSAjax.DHTML.ShowListItem(SSM.UI.action_edit_custom_properties, !settings.editCustomProperties.isHidden && !settings.editCustomProperties.isActionItemHidden);

    SSAjax.DHTML.ShowTable(SSM.UI.properties, !settings.editProperties.isHidden);
    SSAjax.DHTML.ShowTable(SSM.UI.custom_properties, !settings.editCustomProperties.isHidden);

    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(SSM.UI.property_nodeId), !settings.editProperties.isIdHidden);
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(SSM.UI.property_label), !settings.editProperties.isLabelHidden);
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(SSM.UI.property_active), !settings.editProperties.isActiveHidden);
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(SSM.UI.property_contributorOnly), !settings.editProperties.isContributorOnlyHidden);
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(SSM.UI.property_urlDirName), !settings.editProperties.isUrlDirNameHidden);
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(SSM.UI.property_urlPageName), !settings.editProperties.isUrlPageNameHidden);
    SSAjax.DHTML.ShowTableRow(SSAjax.DHTML.GetParentTableRow(SSM.UI.property_maxAge), !settings.editProperties.isMaxAgeHidden);

    SSAjax.DHTML.Enable(SSM.UI.property_nodeId, !settings.editProperties.isIdDisabled && !bIsRootSection);
    SSAjax.DHTML.Enable(SSM.UI.property_label, !settings.editProperties.isLabelDisabled);
    SSAjax.DHTML.Enable(SSM.UI.property_active, !settings.editProperties.isActiveDisabled && !bIsRootSection);
    SSAjax.DHTML.Enable(SSM.UI.property_contributorOnly, !settings.editProperties.isContributorOnlyDisabled);
    SSAjax.DHTML.Enable(SSM.UI.property_urlDirName, !settings.editProperties.isUrlDirNameDisabled);
    SSAjax.DHTML.Enable(SSM.UI.property_urlPageName, !settings.editProperties.isUrlPageNameDisabled);
    SSAjax.DHTML.Enable(SSM.UI.property_maxAge, !settings.editProperties.isMaxAgeDisabled);

    if (bUpdateContent)
    {
        SSM.UI.property_nodeId.value = section.id;
        SSM.UI.property_label.value = section.label;
        SSM.UI.property_urlDirName.value = section.urlDirName;
        SSM.UI.property_urlPageName.value = section.urlPageName;
        SSM.UI.property_maxAge.value = section.maxAge;
        SSM.UI.property_active.checked = section.active;
        SSM.UI.property_contributorOnly.checked = section.contributorOnly;
    }
}

//***************************************************************************
//***************************************************************************
//****************** UPDATE PRIMARY/SECONDARY LAYOUT PANES ******************
//***************************************************************************
//***************************************************************************

SSM.UI.UpdateActionsForLayout = function(bUpdateContent)
{
    var section = SSM.SelectedSection();
    var bPrimary = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout);
    var settings = (bPrimary ? SSM.SelectedSectionSettings().primaryLayout : SSM.SelectedSectionSettings().secondaryLayout);

    var url = (bPrimary ? section.primaryUrl : section.secondaryUrl);
    var label = (bPrimary ? SSM.UI.primary_layout_label : SSM.UI.secondary_layout_label);
    var name = (bPrimary ? SSM.UI.primary_layout_name : SSM.UI.secondary_layout_name);
    var info = (bPrimary ? SSM.UI.primary_layout_info : SSM.UI.secondary_layout_info);
    var clear = (bPrimary ? SSM.UI.primary_layout_clear : SSM.UI.secondary_layout_clear);
    var apply = (bPrimary ? SSM.UI.primary_layout_apply : SSM.UI.secondary_layout_apply);
    var icon = (bPrimary ? SSM.UI.primary_layout_icon : SSM.UI.secondary_layout_icon);
    var preview = (bPrimary ? SSM.UI.primary_layout_preview_frame : SSM.UI.secondary_layout_preview_frame);
    
    var bIsRootSection = (section.id == SSM.currentSite.rootSectionId);
    var bHasPage = (url.page.length > 0);
    var bIsExternal = url.external;

    var chosenDocName = SSAjax.ToString(bPrimary ? SSM.UI.primary_layout_combo.GetSelectedItemId() : SSM.UI.secondary_layout_combo.GetSelectedItemId());
    var chosenDocNameIsDifferent = ((chosenDocName.length > 0) && (url.page != chosenDocName));

    SSAjax.DHTML.ShowBlock(preview, !settings.isPreviewHidden);
    SSAjax.DHTML.ShowInline(info, (bHasPage && !bIsExternal));
    SSAjax.DHTML.ShowInline(clear, bHasPage);
    SSAjax.DHTML.Enable(apply, chosenDocNameIsDifferent);

    if (bUpdateContent)
    {
        var labelId = bHasPage ? "ssmSecondaryLayoutNameLabel" : "ssmSecondaryLayoutNameNotSet";
        if( bPrimary )
        {
            if( bIsRootSection )
            {
                labelId = bHasPage ? "ssmHomeLayoutNameLabel" : "ssmHomeLayoutNameNotSet";
            }
            else
            {
                labelId = bHasPage ? "ssmPrimaryLayoutNameLabel" : "ssmPrimaryLayoutNameNotSet";
            }
        }
        
        SSAjax.DHTML.SetContent(label, SSM.GetString(labelId, url.page));

        if (bPrimary)
        {
            icon.src = (bIsExternal ? SSM.path + 'icons/ssm_externalpage.gif'
                                    : (bIsRootSection ? SSM.path + 'icons/ssm_homepage.gif' 
                                                      : SSM.path + 'icons/ssm_primarypage.gif'));
        }

        SSM.GetPotentialLayoutsForSelectedSection(SSM.UI.UpdateActionsForLayout2);
    }
}

SSM.UI.UpdateActionsForLayout2 = function(searchresults)
{
    var section = SSM.SelectedSection();
    var bPrimary = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout);
    var settings = (bPrimary ? SSM.SelectedSectionSettings().primaryLayout : SSM.SelectedSectionSettings().secondaryLayout);
    var combo = (bPrimary ? SSM.UI.primary_layout_combo : SSM.UI.secondary_layout_combo);
    var page = (bPrimary ? SSM.SelectedSection().primaryUrl.page : SSM.SelectedSection().secondaryUrl.page);

    var fields = settings.presentation.match(/\$(.*?)\$/gi);
    for (var i = 0 ; i < fields.length ; i++)
        fields[i] = fields[i].substr(1, fields[i].length-2);

    combo.DeleteAllItems();
    for (var i = 0 ; i < searchresults.GetRowCount() ; i++)
    {
        var row = searchresults.GetRow(i);
        var dDocName = row.GetField('dDocName');
        
        if (fields.length > 0)
        {
            var values = new Array(fields.length);
            for (var x = 0 ; x < fields.length ; x++)
                values[x] = row.GetField(fields[x]);

            var presentation = settings.presentation;
            for (var x = 0 ; x < fields.length ; x++)
                presentation = presentation.replace('$' + fields[x] + '$', values[x]);

            combo.AppendItem(presentation, dDocName.toLowerCase());
        }
        else
        {
            combo.AppendItem(dDocName, dDocName.toLowerCase());
        }
    }
    if (bPrimary && !settings.isExternalHidden)
        combo.AppendItem(SSM.GetString('ssmChooseExternalUrl'), SSM.UI.externalLayoutId);
    
    if (SSM.IsExternalUrl(page))
    {
        combo.SetLabel(page, page);
        SSM.UI.PreviewLayout(page);
    }
    else
    {
        combo.SelectItemById(bPrimary ? SSM.SelectedSection().primaryUrl.page.toLowerCase() : SSM.SelectedSection().secondaryUrl.page.toLowerCase());
    }
}

//***************************************************************************
//***************************************************************************
//********************* UPDATE CUSTOM PROPERTIES PANE ***********************
//***************************************************************************
//***************************************************************************

SSM.UI.UpdateActionsForCustomProperties = function(bUpdateContent)
{
    var section = SSM.SelectedSection();

    if (bUpdateContent)
    {
        for (var i = 0 ; i < SSM.currentSite.customSectionPropertyDefinitions.GetRowCount() ; i++)
        {
            var name = SSM.currentSite.customSectionPropertyDefinitions.GetRow(i).GetField('name');
            var type = SSM.currentSite.customSectionPropertyDefinitions.GetRow(i).GetField('type');
            var value = section.GetCustomPropertyValue(name);
			//CUSTOM
			 if( validTaxonomyFields.indexOf(name) >= 0) {
				SSM.UI.UpdateLabelsForCustomProperties(name, value);
			 } else {
				SSM.UI.UpdateCustomPropertyValue(name, type, value);
			 }
        }
    }
}

// ******************* CUSTOMISATION STARTS


SSM.UI.UpdateLabelsForCustomProperties = function( name, value)
{//alert("UpdateLabelsForCustomProperties: " + name + ", " + value)
		var link = SSAjax.DHTML.GetObject('ssm_customproperty_link_' + name);		
		var input = SSAjax.DHTML.GetObject('ssm_customproperty_' + name);
		var input_hidden = SSAjax.DHTML.GetObject('ssm_customproperty_hidden_' + name);

		link.onclick = function() { (new OntBasedMetadata( name, input, input_hidden)).popup(); };
		
		input_hidden.value = SSAjax.ToString(value);

		if( value != null && value.length > 0 && value != ';;') { 
			var params = new SSAjax.Idc.Params();
			params.Add('metadata', name); 
			params.Add('siteId', SSM.siteId); 
			params.Add('property', 'siteLocale'); 
			if( value.indexOf(';') == 0 )
				temp = value.substring(1, value.length-1);
			else temp = value;
			params.Add('termId', temp );
			if(SSM.idc.http.status == SSAjax.Http.Response.SUCCEEDED)
				SSM.CallService('ONT_GET_TERM_LABEL', params, SSM.UI.SetCustomPropertyLabel, true,true);
		} else {
			input.innerHTML = '&nbsp;'; 
		}

}


var metaObj = null;

function OntBasedMetadata(metadata, el, el_hidden) {
	var This = this;
	this.meta = metadata;
	this.element = el;
	this.element_hidden = el_hidden;
	this.value = this.element_hidden.value; 

	this.updateValue = function(id, label) {
		id = ';' + id + ';';
		This.element_hidden.value = id;
		This.element.innerHTML = label;
		SSM.SimpleSaveSectionProperty(This.meta, id);
		return;
	}
	
	this.popup = function() {
		metaObj = this;
		window.open("?IdcService=ONT_DISPLAY_TREE&callback=window.opener.metaObj.updateValue&ont_metadata="+this.meta+"&value="+this.value+"&language="+g_LanguageCode+"&country="+g_CountryCode, "", "width=700, height=500");
	}
}


SSM.UI.SetCustomPropertyLabel = function() {
	var label = SSM.idc.binder.GetLocalData('termLabel'); //alert("SetCustomPropertyLabel: "  + label)
	var input = SSAjax.DHTML.GetObject('ssm_customproperty_' + SSM.idc.binder.GetLocalData('metadata'));
	input.innerHTML = SSAjax.ToString(label); 
	return;
}

SSM.SimpleSaveSectionProperty = function(name, value)
{//alert("SimpleSaveSectionProperty" + name + ", " + value)
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('property', name);
    params.Add('value', value);
    params.Add('autoGenerateNav', '1');

    // special case, if changing the nodeId, ensure same section remains selected when we reload site
    if (name == 'nodeId')
        SSM.UI.hierarchy.selectedItemId = value;

    SSM.CallService('SS_SET_NODE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

SSM.Empty = function() { return true;}

//******************** CUSTOMISATION ENDS


SSM.UI.UpdateCustomPropertyValue = function(name, type, value)
{ //alert("UpdateCustomPropertyValue")
	
    var input = SSAjax.DHTML.GetObject('ssm_customproperty_' + name);
    
    switch(type.toLowerCase())
    {
        case 'boolean':
            input.checked = SSAjax.ToBool(value, false);
            break;

        case 'managedurl':
        case 'manageddoc':
        case 'managedquery':
        case 'text':
        case 'bigtext':
        case 'integer':
        case 'float':
        case 'size':
        case 'color':
        case 'url':
        case 'cssstyle':
        case 'siteid':
        case 'nodeid':
            input.value = SSAjax.ToString(value);
            break;
    }
}




//***************************************************************************
//***************************************************************************
//***** PRIMARY/SECONDARY LAYOUT PREVIEW COMBO/FRAME UI EVENT HANDLING ******
//***************************************************************************
//***************************************************************************

SSM.UI.OnLayoutInfo = function()
{
    var section = SSM.SelectedSection();
    var bPrimary = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout);
    var dDocName = (bPrimary ? section.primaryUrl.page : section.secondaryUrl.page);

    SSM.PopupService('DOC_INFO_BY_NAME', new SSAjax.Idc.Param('dDocName', dDocName));
}

//***************************************************************************

SSM.UI.OnBeforeDropDownLayoutCombo = function()
{
    var bPrimary = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout);
    var combo = (bPrimary ? SSM.UI.primary_layout_combo : SSM.UI.secondary_layout_combo);
    
    SSM.UI.Disable();
    SSAjax.DHTML.Enable(combo);
}

SSM.UI.OnAfterRollUpLayoutCombo = function()
{
    SSM.UI.EnableAndUpdateState();
}

SSM.UI.OnAfterSelectItemLayoutCombo = function(dDocName)
{
    if (dDocName == SSM.UI.externalLayoutId)
        SSM.UI.SelectExternalLayout();
    else
        SSM.UI.SelectExistingLayout(dDocName);
}

//***************************************************************************

SSM.UI.SelectExternalLayout = function()
{
    SSM.UI.DisplayPromptEx('http://', null, SSM.GetString('ssmChooseExternalUrlPrompt'), SSAjax.DHTML.MessageBox.Style.MB_OKCANCEL, SSM.UI.SelectExternalLayout2);
}

SSM.UI.SelectExternalLayout2 = function(msgbox, button)
{
    if (button == SSAjax.DHTML.MessageBox.Button.IDOK)
    {
        var url = msgbox.GetPromptValue();

        if (!SSM.IsExternalUrl(url))
        {
            SSM.UI.DisplayErrorMessage(SSM.GetString('ssmUrlMustBeAbsolute'));
        }
        else
        {
            var combo = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout ? SSM.UI.primary_layout_combo : SSM.UI.secondary_layout_combo);
            combo.SetLabel(url, url);
            SSM.UI.PreviewLayout(url);
        }
    }
}

//***************************************************************************

SSM.UI.SelectExistingLayout = function(dDocName)
{    
    var section = SSM.SelectedSection();
    var bPrimary = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout);
    var settings = (bPrimary ? SSM.SelectedSectionSettings().primaryLayout : SSM.SelectedSectionSettings().secondaryLayout);
    
    var url = null;
    if (dDocName && !settings.isPreviewHidden)
    {
        url  = SSM.currentSite.GetFriendlyUrlForSection(section);
        url += '?previewLayoutDocName=' + dDocName;
        url += '&SSHideContributorUI=true';
        url += '&SSManagerPreview=true';
        url += (bPrimary ? '' : '&useSecondary=true');
    }

    SSM.UI.PreviewLayout(url);
}

//***************************************************************************

SSM.UI.PreviewLayout = function(url)
{
    var frame = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout ? SSM.UI.primary_layout_preview_frame : SSM.UI.secondary_layout_preview_frame);

    SSM.UI.Disable();
    SSM.UI.progress.Start();
    SSAjax.DHTML.AddEvent(frame, 'load', SSM.UI.PreviewLayout2);

    if (!SSAjax.IsString(url) || (url.length == 0))
    {
        frame.src = SSM.path + 'empty.htm';
    }
    else
    {
        frame.src = url;
    }
}

SSM.UI.PreviewLayout2 = function(e)
{
    var frame = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout ? SSM.UI.primary_layout_preview_frame : SSM.UI.secondary_layout_preview_frame);

    SSAjax.DHTML.RemoveEvent(frame, 'load', SSM.UI.PreviewLayout2);
    SSAjax.DHTML.RemoveAllEventsFromFrame(frame);
    
    SSM.UI.progress.Stop(SSM.UI.PreviewLayout3);
}

SSM.UI.PreviewLayout3 = function(e)
{
    SSM.UI.EnableAndUpdateState();
}

//***************************************************************************
//***************************************************************************
//********************************* HELP ************************************
//***************************************************************************
//***************************************************************************

SSM.UI.OnClickHelp = function()
{
    // BJC - 10/12/07 - no localized help files for this release (10.1.3.3.2) so hardwire to English.
    SSM.PopupUrl(SSM.GetConfigInfoValue('HttpRelativeWebRoot') + 'help/sitestudio/manager/en/x_site_manager.htm');
}

//***************************************************************************
//***************************************************************************
//********************** SAVE / LOAD STATE VARIABLES ************************
//***************************************************************************
//***************************************************************************

SSM.UI.SaveState = function()
{
    var state = new SSAjax.Idc.Params();
    state.Add('ssm.action', SSAjax.ToString(SSM.UI.actions.selectedTabId));
    state.Add('ssm.section', SSAjax.ToString(SSM.SelectedSection().id));
    return state.EncodeForGET();
}

SSM.UI.LoadActionState = function()
{
    var state = new SSAjax.Idc.Params();
    state.AddMultiple(SSAjax.GetUrlHash());
    var action = state.GetValue('ssm.action');
    if (SSAjax.IsValid(action))
        return action;
    else
        return SSM.UI.ActionTab.Section;
}

SSM.UI.LoadSectionState = function()
{
    var state = new SSAjax.Idc.Params();
    state.AddMultiple(SSAjax.GetUrlHash());
    var sectionId = state.GetValue('ssm.section');
    if (SSAjax.IsValid(sectionId) && SSM.currentSite.SectionExists(sectionId))
        return sectionId;
    else if (SSAjax.IsValid(SSM.nodeId) && SSM.currentSite.SectionExists(SSM.nodeId))
        return SSM.nodeId;
    else
        return SSM.currentSite.rootSectionId;
}

SSM.UI.LoadVisibleChildrenTotal = function()
{
    var state = new SSAjax.Idc.Params();
    state.AddMultiple(SSAjax.GetUrlHash());
    SSM.UI.visibleChildrenTotal = state.GetValue('ssm.child.count') || SSM.UI.visibleChildrenTotal;
    SSM.UI.visibleChildrenTotal = parseInt(SSM.UI.visibleChildrenTotal);
}

SSM.UI.LoadSiteDefinitionLoadOption = function()
{   
    var state = new SSAjax.Idc.Params();
    state.AddMultiple(SSAjax.GetUrlHash());
    SSM.UI.useHttpGetForGetSiteDefinition = SSAjax.ToBool(state.GetValue('ssm.use.get'), true);
}

//***************************************************************************

SSM.UI.StartStateChangeTimer = function() { SSM.UI.StopStateChangeTimer(); SSM.UI.stateChangeTimer = setTimeout(SSM.UI.CheckForStateChanges, 500); }
SSM.UI.StopStateChangeTimer = function() { clearTimeout(SSM.UI.stateChangeTimer); }

SSM.UI.RememberState = function() { SSM.UI.RememberState.hash = SSAjax.GetUrlHash(); }
SSM.UI.HasStateChanged = function() { return (SSAjax.ToString(SSM.UI.RememberState.hash) != SSAjax.GetUrlHash()); }

SSM.UI.EnableCheckForStateChanges = function(b)
{
    b = SSAjax.ToBool(b, true);
    
    SSM.UI.RememberState();

    if (b)
        SSM.UI.StartStateChangeTimer();
    else
        SSM.UI.StopStateChangeTimer();
}

SSM.UI.CheckForStateChanges = function()
{
    if (SSM.UI.HasStateChanged())
    {
        SSM.UI.RememberState();
        SSM.UI.actions.SelectTab(SSM.UI.LoadActionState(), false);
        SSM.UI.hierarchy.SelectItem(SSM.UI.LoadSectionState(), false);
        SSM.UI.Update();
    }

    SSM.UI.StartStateChangeTimer();
}

//***************************************************************************
//***************************************************************************
//***************************** TRACING METHODS *****************************
//***************************************************************************
//***************************************************************************

SSM.UI.StartTracing = function()
{
    if (!SSM.UI.console)
    {
        SSM.UI.console = new SSAjax.DHTML.Console('started...', 'Site Studio Designer Lite Console (<a class="ssm_action_link" href="javascript:SSM.UI.console.Clear();">clear</a>, <a class="ssm_action_link" href="javascript:SSM.UI.StopTracing();">close</a>)', 'ssm_console', 'ssm_console', 'ssm_main', '');
        SSM.UI.console.MoveTo(SSAjax.DHTML.GetRight('ssm_right_header') - SSAjax.DHTML.GetWidth(SSM.UI.console.div), SSAjax.DHTML.GetBottom('ssm_right_header'));
    }
}

SSM.UI.StopTracing = function()
{
    if (SSM.UI.console)
    {
        SSM.UI.console.RemoveFromDOM();
        SSM.UI.console = null;
    }
}

SSM.UI.ShowTracing = function(bOn) { if (SSM.UI.console) SSM.UI.console.Show(bOn); }
SSM.UI.HideTracing = function()    { if (SSM.UI.console) SSM.UI.console.Hide();    }

SSM.UI.Trace = function(line) { if (SSM.UI.console) SSM.UI.console.Append(line); }

//***************************************************************************

SSM.UI.TraceRequest = function(service, request)
{
    request = SSAjax.Escape(request).replace(/\n/gi, '<br>');

    var header = SSAjax.DHTML.CreateDiv(null, 'ssm_trace_idc_service_header', null, null);
    var plus = SSAjax.DHTML.CreateImg(null, null, SSAjax.path + 'icons/ssajax_tree_plus.gif', 'top', header);
    var headerSPAN = SSAjax.DHTML.CreateSpan(null, 'ssm_trace_idc_service_header_label', 'REQUEST: ', header);
    var serviceSPAN = SSAjax.DHTML.CreateSpan(null, 'ssm_trace_idc_service_header_name', service, header);
    var body = SSAjax.DHTML.CreateDiv(null, 'ssm_trace_idc_service_body', request, header);

    plus.style.cursor = 'pointer';
    body.style.display = 'none';
    plus.style.visibility = (request ? 'visible' : 'hidden');

    SSAjax.DHTML.AddEvent(plus, 'click', SSM.UI.TraceRequestToggle);
    
    SSM.UI.Trace(header);
}

//***************************************************************************

SSM.UI.TraceResponse = function(service, response, statusCode, statusMessage)
{
    response = SSAjax.Escape(response).replace(/\n/gi, '<br>');

    var header = SSAjax.DHTML.CreateDiv(null, 'ssm_trace_idc_service_header', null, null);
    var plus = SSAjax.DHTML.CreateImg(null, null, SSAjax.path + 'icons/ssajax_tree_plus.gif', 'top', header);
    var headerSPAN = SSAjax.DHTML.CreateSpan(null, 'ssm_trace_idc_service_header_label', 'RESPONSE: ', header);
    var serviceSPAN = SSAjax.DHTML.CreateSpan(null, 'ssm_trace_idc_service_header_name', service, header);
    var messageSPAN = SSAjax.DHTML.CreateSpan(null, 'ssm_trace_idc_service_header_message', ' ' + statusMessage, header);
    var body = SSAjax.DHTML.CreateDiv(null, 'ssm_trace_idc_service_body', response, header);

    plus.style.cursor = 'pointer';
    body.style.display = 'none';
    plus.style.visibility = (response ? 'visible' : 'hidden');

    SSAjax.DHTML.AddEvent(plus, 'click', SSM.UI.TraceResponseToggle);
    
    SSM.UI.Trace(header);
}

//***************************************************************************

SSM.UI.TraceRequestToggle = function(e)
{
    var icon = SSAjax.DHTML.GetEventTarget(e);
    var body = icon.parentNode.childNodes[3];
    
    if (SSAjax.DHTML.IsVisible(body))
    {
        SSAjax.DHTML.Hide(body);
        icon.src = SSAjax.path + 'icons/ssajax_tree_plus.gif';
    }
    else
    {
        SSAjax.DHTML.ShowBlock(body);
        icon.src = SSAjax.path + 'icons/ssajax_tree_minus.gif';
    }
}

//***************************************************************************

SSM.UI.TraceResponseToggle = function(e)
{
    var icon = SSAjax.DHTML.GetEventTarget(e);
    var body = icon.parentNode.childNodes[4];
    
    if (SSAjax.DHTML.IsVisible(body))
    {
        SSAjax.DHTML.Hide(body);
        icon.src = SSAjax.path + 'icons/ssajax_tree_plus.gif';
    }
    else
    {
        SSAjax.DHTML.ShowTable(body);
        icon.src = SSAjax.path + 'icons/ssajax_tree_minus.gif';
    }
}

//***************************************************************************

SSM.UI.Find = function(a, name)
{
    if (SSAjax.IsArray(a))
    {
        for(var n = 0 ; n < a.length ; n++)
            if (a[n] == name)
                return n;
    }

    return -1;	
}

//***************************************************************************
