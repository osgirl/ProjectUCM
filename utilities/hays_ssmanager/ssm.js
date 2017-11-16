/////////////////////////////////////////////////////////////////////////////
// 
// Solution  : SiteStudio
// Project   : Site Studio Manager (SSM)
//
// FileName  : ssm.js
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

SSM = new Object(); // namespace objects
SSM.Strings = new Array(); // localized strings array (will be populated by loading locale specific ssm.strings.js file)

//***************************************************************************
//***************************************************************************
//************************ HOOK LOAD/UNLOAD EVENTS **************************
//***************************************************************************
//***************************************************************************

SSM.Startup = function(cgiUrl, siteId, userLanguageId, settingsUrl)
{
    SSM.settingsUrl = SSAjax.ToString(settingsUrl);
    SSM.cgiurl = SSAjax.ToString(cgiUrl);
    SSM.siteId = SSAjax.ToString(siteId);
    SSM.nodeId = (typeof g_ssSourceNodeId == _U ? null : g_ssSourceNodeId);
    SSM.userLanguageId = SSAjax.ToString(userLanguageId, "en");
    
    SSAjax.Startup(SSAjax.Features.ALL, SSM.Initialize, SSM.Uninitialize, SSM.userLanguageId);
}

//***************************************************************************
//***************************************************************************
//******************************* INITIALIZE ********************************
//***************************************************************************
//***************************************************************************

SSM.Initialize = function()
{
    // then load the ssm features
    SSM.path = SSAjax.GetPathToScript('ssm.js');
    SSAjax.LoadMultipleScripts(
        [SSM.path + 'ssm.settings.js',
         SSM.path + 'ssm.site.js',
         SSM.path + 'ssm.ui.js',
         SSM.path + 'ssm.ui.form.addnewsection.js',
         SSM.path + 'ssm.ui.form.movesection.js',
         SSM.path + 'lang/en/ssm.strings.js',
         SSM.path + 'lang/' + SSM.userLanguageId + '/ssm.strings.js'],
        SSM.Initialize2);
}
SSM.Initialize2 = function()
{
    // initialize the UI components
    SSM.UI.Initialize();

    // create an idc client control and hook into its tracing mechanism
    SSM.idc = new SSAjax.Idc();
    SSM.idc.SetTraceRequest(SSM.UI.TraceRequest);
    SSM.idc.SetTraceResponse(SSM.UI.TraceResponse);

    // initialize the configuration settings
    SSM.InitializeSettings(SSM.settingsUrl, SSM.Initialize3, false);
}
SSM.Initialize3 = function()
{
    // ask the server for a GET_DOC_CONFIG_INFO result set for general information
    SSM.InitializeDocConfigInfo(SSM.Initialize4, false);
}
SSM.Initialize4 = function()
{
    // go get the site label to display in the title bar
    SSM.InitializeSiteLabel(SSM.Initialize5, !SSM.settings.general.autoManage);
}
SSM.Initialize5 = function()
{
    // initialization is complete... if auto-connect is enabled then load the site (assuming ssm is visible)
    if (SSM.settings.general.autoManage && SSAjax.DHTML.IsVisible(SSM.UI.main))
        SSM.LoadSite();
}

//***************************************************************************
//***************************************************************************

SSM.InitializeSettings = function(settingsUrl, callback, bStopProgressAndReEnable)
{
    if (SSAjax.ToString(settingsUrl).length == 0)
        SSM.InitializeSettings3(callback);
    else
        SSM.CallUrl(settingsUrl, function() { SSM.InitializeSettings2(callback); }, bStopProgressAndReEnable, false);
}
SSM.InitializeSettings2 = function(callback)
{
    if (SSM.idc.http.status != SSAjax.Http.Response.SUCCEEDED)
    {
        SSM.UI.DisplayErrorMessage('Failed to load Manager settings file.');
        return;
    }

    SSM.InitializeSettings3(callback, SSM.idc.http.GetResponseXML());
}
SSM.InitializeSettings3 = function(callback, xml)
{
    if (xml)
    {
        SSAjax.XML.SetXPathNamespaces(xml, 'xmlns:ssm="http://www.stellent.com/sitestudio/managersettings/"');
        SSM.settings = new SSM.Settings(xml.selectSingleNode('ssm:settings'));
    }
    else
    {
        SSM.settings = new SSM.Settings();
    }

    if (!SSM.IsForceContributorOnly() && !SSM.IsManagerPreview())
        if (!SSM.settings.general.contributorOnly || SSM.IsContributorOnly())
            SSAjax.DHTML.ShowTable(SSM.UI.main);

    if (SSM.settings.general.displayConsole)
        SSM.UI.StartTracing();

    if (SSM.settings.general.hierarchyHidden)
    {
        SSAjax.DHTML.Hide('ssm_hierarchy_label');
        SSM.UI.hierarchy.Hide();
    }

    if (SSM.settings.moveSection.isHidden)
        SSM.UI.hierarchy.dragDropEnabled = false;

    callback();
}

//***************************************************************************
//***************************************************************************

SSM.InitializeDocConfigInfo = function(callback, bStopProgressAndReEnable)
{
    SSM.CallService('GET_DOC_CONFIG_INFO', null, function() { SSM.InitializeDocConfigInfo2(callback); }, bStopProgressAndReEnable);
}
SSM.InitializeDocConfigInfo2 = function(callback)
{
    SSM.configInfo = SSM.idc.binder;
    callback();
}
    
//***************************************************************************
//***************************************************************************

SSM.InitializeSiteLabel = function(callback, bStopProgressAndReEnable)
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('property', 'siteLabel');
    SSM.CallService('SS_GET_SITE_PROPERTY', params, function() { SSM.InitializeSiteLabel2(callback); }, bStopProgressAndReEnable);
}
SSM.InitializeSiteLabel2 = function(callback)
{
    SSM.UI.SetAppTitle(SSM.idc.binder.GetLocalData('value'));
    callback();
}

//***************************************************************************
//***************************************************************************
//***************************** UN-INITIALIZE *******************************
//***************************************************************************
//***************************************************************************

SSM.Uninitialize = function()
{
    SSM.idc = null;
    SSM.currentSite = null;
    SSM.configInfo = null;
    SSM.settings = null;

    if (SSAjax.IsValid(SSM.UI)) // need to play it safe in case Uninit is called before Init has finished (p51040415)
        SSM.UI.Uninitialize();

    SSM = null;
}

//***************************************************************************
//***************************************************************************
//******************************* LOAD SITE *********************************
//***************************************************************************
//***************************************************************************

SSM.LoadSite = function()
{
    if (!SSM.UI.useHttpGetForGetSiteDefinition)
    {
        SSM.CallService('SS_GET_SITE_DEFINITION_FOR_USER', new SSAjax.Idc.Params('siteId', SSM.siteId), SSM.LoadSite2, false);
    }
    else
    {
        var url = SSM.cgiurl + '?IdcService=SS_GET_SITE_DEFINITION_FOR_USER&siteId=' + SSM.siteId + '&IsJava=1&IsSoap=1';

        var Http = new SSAjax.Http();
        Http.Send(url, function(http)
        {
            if (http && http.status === SSAjax.Http.Response.SUCCEEDED)
            {
                var xml = (new DOMParser()).parseFromString(http.GetResponseText(), 'text/xml');
                SSAjax.XML.SetXPathNamespaces(xml, 'xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:idc="http://www.stellent.com/IdcService/"');
                SSM.idc.binder = SSAjax.Idc.Binder.LoadFromSoap(xml);
                SSM.LoadSite2();
            }	
        }, null, 'text/html');    
    }
}
SSM.LoadSite2 = function()
{
    SSM.currentSite = new SSM.Site(SSM.siteId);
    SSM.currentSite.SetSiteHierarchy(SSM.idc.binder.GetLocalData('siteXml'));
    SSM.currentSite.SetSiteAddress(SSM.idc.binder.GetLocalData('HttpSiteAddress'));

    SSM.UI.SetAppTitle(SSM.currentSite.GetSiteProperty('siteLabel'));

    // Now that the user has logged in, re-localize the user interface.
    var userLanguageId = SSAjax.ToString(SSM.idc.binder.GetLocalData('userLanguageId'), "en");
    if( !(userLanguageId == SSM.userLanguageId) )
    {
        SSM.userLanguageId = userLanguageId;
        SSAjax.userLanguageId = userLanguageId;

        SSAjax.LoadMultipleScripts(
            [
                SSAjax.path + 'lang/' + SSAjax.userLanguageId + '/ssajax.strings.js',
                SSM.path + 'lang/' + SSM.userLanguageId + '/ssm.strings.js',
            ],
            SSM.UI.LocalizeUserInterface);
    }

    SSM.CallService('SS_GET_ALL_CUSTOM_NODE_PROP_DEFS', new SSAjax.Idc.Params('siteId', SSM.siteId), SSM.LoadSite3, true);
}
SSM.LoadSite3 = function()
{
    SSM.currentSite.SetCustomSectionPropertyDefinitions(SSM.idc.binder.GetResultSet('CustomNodePropDefs'));

    SSM.UI.LoadCustomProperties();
    SSM.UI.LoadHierarchyTree(SSM.UI.hierarchy, SSM.UI.LoadSectionState());

    if (SSAjax.ToBool(SSM.firstTime, true))
    {
        SSAjax.DHTML.SetContent(SSM.UI.loadsite_link, SSM.GetString('ssmRefresh'));
        SSM.UI.loadsite_link.title = SSM.GetString('ssmRefreshThisSite');
        SSAjax.DHTML.ShowTable(SSM.UI.body);
        SSM.UI.actions.SelectTab(SSM.UI.LoadActionState(), false);
        SSM.firstTime = false;
    }
    
    SSM.UI.Update();
    
    SSM.UI.CheckForStateChanges();
}

SSM.RegenerateSiteNavAndReloadSite = function()
{
    // SSM.CallService('SS_CREATE_SITE_NAV_JS', new SSAjax.Idc.Params('siteId', SSM.siteId), SSM.LoadSite, false);
    SSM.LoadSite();
}

SSM.SwitchSite = function(siteId)
{
    SSM.siteId = siteId;
    SSM.InitializeSettings(SSM.settingsUrl, SSM.SwitchSite2, false);
}
SSM.SwitchSite2 = function()
{
    SSM.settings.general.autoManage = true;
    SSM.Initialize4();
}

//***************************************************************************
//***************************************************************************
//****************************** ADD SECTION ********************************
//***************************************************************************
//***************************************************************************

SSM.AddSection = function()
{
    new SSM.UI.Forms.AddNewSection(SSM.AddSection2);
}
SSM.AddSection2 = function(form, button)
{
    if (button != SSAjax.DHTML.MessageBox.Button.IDOK)
        return;

    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('newNodeId', (form.sectionIdType == SSM.UI.Forms.AddNewSection.SectionIdType.MANUAL ? form.sectionId : ''));
    params.Add('property0', 'label');
    params.Add('value0', form.label);
    params.Add('property1', 'urlDirName');
    params.Add('value1', form.urlDirName);
    params.Add('autoGenerateNav', '1');
    
    if (SSM.SelectedSection().id != SSM.currentSite.rootSectionId)
    {
        var counter = 2;
        
        var primaryUrl = (SSM.SelectedSectionSettings().addSection.isInheritingPrimaryUrl ? SSM.SelectedSection().primaryUrl.page : SSM.SelectedSectionSettings().addSection.defaultPrimaryUrl);
        if (primaryUrl.length > 0)
        {
            params.Add('property' + counter, 'primaryUrl');
            params.Add('value' + counter, primaryUrl);
            counter++;
        }

        var secondaryUrl = (SSM.SelectedSectionSettings().addSection.isInheritingSecondaryUrl ? SSM.SelectedSection().secondaryUrl.page : SSM.SelectedSectionSettings().addSection.defaultSecondaryUrl);
        if (secondaryUrl.length > 0)
        {
            params.Add('property' + counter, 'secondaryUrl');
            params.Add('value' + counter, secondaryUrl);
            counter++;
        }

        var secondaryUrlVariableField = (SSM.SelectedSectionSettings().addSection.isInheritingSecondaryUrlVariableField ? SSM.SelectedSection().secondaryUrlVariableField : SSM.SelectedSectionSettings().addSection.defaultSecondaryUrlVariableField);
        if (secondaryUrlVariableField.length > 0)
        {
            params.Add('property' + counter, 'secondaryUrlVariableField');
            params.Add('value' + counter, secondaryUrlVariableField);
            counter++;
        }
    }
    
    SSM.CallService('SS_ADD_NODE', params, SSM.AddSection3, false);
}
SSM.AddSection3 = function()
{
    SSM.UI.hierarchy.selectedItemId = SSM.idc.binder.GetLocalData('newNodeId'); // bit of a hack to ensure newly created section is selected after site is re-loaded
    SSM.RegenerateSiteNavAndReloadSite();
}

//***************************************************************************
//***************************************************************************
//***************************** REMOVE SECTION ******************************
//***************************************************************************
//***************************************************************************

SSM.RemoveSection = function()
{
    var section = SSM.SelectedSection();

    SSM.UI.DisplayWarningMessageEx(SSM.GetString('ssmRemoveSectionWarning', section.label, section.id), SSM.GetString('ssmRemoveSection'), SSAjax.DHTML.MessageBox.Style.MB_YESNO, SSM.RemoveSection2);
}
SSM.RemoveSection2 = function(msgbox, button)
{
    if (button != SSAjax.DHTML.MessageBox.Button.IDYES)
        return;

    var section = SSM.SelectedSection();
    var otherSection = SSM.currentSite.GetNextOrPreviousOrParentSection(section);
    SSM.UI.hierarchy.selectedItemId = otherSection.id; // bit of a hack to ensure sensible sibling (or parent) of about-to-be-deleted section is selected after site is re-loaded

    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', section.id);
    params.Add('autoGenerateNav', '1');

    SSM.CallService('SS_DELETE_NODE', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

//***************************************************************************
//***************************************************************************
//****************************** MOVE SECTION *******************************
//***************************************************************************
//***************************************************************************

SSM.MoveSection = function()
{
    var form = new SSM.UI.Forms.MoveSection(SSM.MoveSection2, SSM.SelectedSection().id);
}
SSM.MoveSection2 = function(form, button)
{
    if (button != SSAjax.DHTML.MessageBox.Button.IDOK)
        return;
        
    SSM.MoveSection3(SSM.UI.hierarchy.selectedItemId, form.newParentId, form.insertAfterId);
}
SSM.MoveSection3 = function(sectionId, newParentId, insertAfterId)
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', sectionId);
    params.Add('newParentId', newParentId);
    params.Add('insertAfterId', insertAfterId);
    params.Add('autoGenerateNav', '1');

    SSM.CallService('SS_MOVE_NODE', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

//***************************************************************************
//***************************************************************************
//****************************** EDIT SECTION *******************************
//***************************************************************************
//***************************************************************************

SSM.SaveSectionProperty = function(name, value)
{
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

//***************************************************************************
//***************************************************************************
//************************ SET & CLEAR ERROR HANDLER ************************
//***************************************************************************
//***************************************************************************

SSM.SetErrorHandler = function()
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('property', 'errorNodeId');
    params.Add('value', SSM.UI.hierarchy.selectedItemId);
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_SITE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

SSM.ClearErrorHandler = function()
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('property', 'errorNodeId');
    params.Add('value', '');
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_SITE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

//***************************************************************************
//***************************************************************************
//*************************** SET & CLEAR ACTIVE ****************************
//***************************************************************************
//***************************************************************************

SSM.SetActive = function()
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('property', 'active');
    params.Add('value', true);
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_NODE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

SSM.ClearActive = function()
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('property', 'active');
    params.Add('value', false);
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_NODE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

//***************************************************************************
//***************************************************************************
//********************** APPLY & CLEAR PRIMARY LAYOUT ***********************
//***************************************************************************
//***************************************************************************

SSM.ApplyPrimaryLayout = function()
{
    var url = new SSM.Site.Section.Url(SSM.UI.primary_layout_combo.GetSelectedItemId());
    if (!url.external)
        url.params = SSM.SelectedSection().primaryUrl.params; // retain previous params if not external URL.
    
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('property', 'primaryUrl');
    params.Add('value', url.ToString(), false);
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_NODE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

SSM.ClearPrimaryLayout = function()
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('property', 'primaryUrl');
    params.Add('value', '', false);
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_NODE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

//***************************************************************************
//***************************************************************************
//********************* APPLY & CLEAR SECONDARY LAYOUT **********************
//***************************************************************************
//***************************************************************************

SSM.ApplySecondaryLayout = function()
{
    var url = new SSM.Site.Section.Url(SSM.UI.secondary_layout_combo.GetSelectedItemId());
    if (!url.external)
        url.params = SSM.SelectedSection().secondaryUrl.params; // retain previous params if not external URL.
    
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('property', 'secondaryUrl');
    params.Add('value', url.ToString(), false);
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_NODE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

SSM.ClearSecondaryLayout = function()
{
    var params = new SSAjax.Idc.Params();
    params.Add('siteId', SSM.siteId);
    params.Add('nodeId', SSM.UI.hierarchy.selectedItemId);
    params.Add('property', 'secondaryUrl');
    params.Add('value', '', false);
    params.Add('autoGenerateNav', '1');
    SSM.CallService('SS_SET_NODE_PROPERTY', params, SSM.RegenerateSiteNavAndReloadSite, false);
}

//***************************************************************************

SSM.GetPotentialLayoutsForSelectedSection = function(callback)
{
    var section = SSM.SelectedSection();
    var bPrimary = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout);
    var globalSettings = (bPrimary ? SSM.settings.primaryLayout : SSM.settings.secondaryLayout);
    var sectionSettings = (bPrimary ? SSM.SelectedSectionSettings().primaryLayout : SSM.SelectedSectionSettings().secondaryLayout);
    var bUseGlobal = ((globalSettings.queryText == sectionSettings.queryText) && (globalSettings.limitScope == sectionSettings.limitScope));

    if (bUseGlobal && globalSettings.potentialLayouts)
    {
        callback(globalSettings.potentialLayouts);
        return;
    }
    else if (!bUseGlobal && sectionSettings.potentialLayouts)
    {
        callback(sectionSettings.potentialLayouts);
        return;
    }

    SSM.GetPotentialLayoutsForSelectedSection.callback = callback; // save for later
    
    var params = new SSAjax.Idc.Params();
    params.AddQueryTextParams();
    params.Add('QueryText', (bUseGlobal ? globalSettings.queryText : sectionSettings.queryText));
    params.Add('ssWebsiteObjectType', 'Layout File');
    params.Add('ssLimitScope', (bUseGlobal ? globalSettings.limitScope : sectionSettings.limitScope));
    params.Add('siteId', SSM.siteId);
    params.Add('computeFriendlyUrls', 'false');
    params.Add('ResultCount', SSM.GetConfigInfoValue('MaxResults'));
    
    SSM.CallService('SS_GET_SEARCH_RESULTS', params, SSM.GetPotentialLayoutsForSelectedSection2, true);
}
SSM.GetPotentialLayoutsForSelectedSection2 = function()
{
    var bPrimary = (SSM.UI.actions.selectedTabId == SSM.UI.ActionTab.PrimaryLayout);
    
    var section = SSM.SelectedSection();
    var globalSettings = (bPrimary ? SSM.settings.primaryLayout : SSM.settings.secondaryLayout);
    var sectionSettings = (bPrimary ? SSM.SelectedSectionSettings().primaryLayout : SSM.SelectedSectionSettings().secondaryLayout);
    var bUseGlobal = ((globalSettings.queryText == sectionSettings.queryText) && (globalSettings.limitScope == sectionSettings.limitScope));

    if (bUseGlobal)
    {
        globalSettings.potentialLayouts = SSM.idc.binder.GetResultSet('SearchResults');
        SSM.GetPotentialLayoutsForSelectedSection.callback(globalSettings.potentialLayouts);
    }
    else
    {
        sectionSettings.potentialLayouts = SSM.idc.binder.GetResultSet('SearchResults');
        SSM.GetPotentialLayoutsForSelectedSection.callback(sectionSettings.potentialLayouts);
    }
}

//***************************************************************************

SSM.GetDocInfoByName = function(dDocName, callback, bStopProgressAndReEnable)
{
    bStopProgressAndReEnable = SSAjax.ToBool(bStopProgressAndReEnable, true); // default to true
    SSM.GetDocInfoByName.callback = callback; // save callback
    
    var params = new SSAjax.Idc.Params();
    params.Add('dDocName', dDocName);
    SSM.CallService('DOC_INFO_BY_NAME', params, SSM.GetDocInfoByName2, bStopProgressAndReEnable);
}

SSM.GetDocInfoByName2 = function()
{
    var docinfo = SSM.idc.binder.GetResultSet('DOC_INFO').GetRow(0);
    SSM.GetDocInfoByName.callback(docinfo);    
}

//***************************************************************************
//***************************************************************************
//******************* CALL SERVICE/URL & HANDLE RESPONSE ********************
//***************************************************************************
//***************************************************************************

SSM.CallService = function(service, params, callback, bStopProgressAndReEnable, bAutoError)
{
    SSM.HandleResponse.callback = callback;
    SSM.HandleResponse.bStopProgressAndReEnable = bStopProgressAndReEnable;
    SSM.HandleResponse.bAutoError = bAutoError;
    
    SSM.UI.Disable();
    SSM.UI.progress.Start();
    SSM.idc.CallService(SSM.cgiurl, service, params, SSM.HandleResponse);
}

SSM.CallUrl = function(url, callback, bStopProgressAndReEnable, bAutoError)
{
    SSM.HandleResponse.callback = callback;
    SSM.HandleResponse.bStopProgressAndReEnable = bStopProgressAndReEnable;
    SSM.HandleResponse.bAutoError = bAutoError;
    
    SSM.UI.Disable();
    SSM.UI.progress.Start();
    SSM.idc.CallUrl(url, SSM.HandleResponse);
}

//***************************************************************************

SSM.HandleResponse = function()
{
    bAutoError = SSAjax.ToBool(SSM.HandleResponse.bAutoError, true);
    
    if (bAutoError && (SSM.idc.http.status != SSAjax.Http.Response.SUCCEEDED))
    {
        SSM.UI.progress.Stop(SSM.HandleResponse2);
    }
    else if (SSAjax.ToBool(SSM.HandleResponse.bStopProgressAndReEnable, true))
    {
        SSM.UI.progress.Stop(SSM.HandleResponse2);
    }
    else
    {
        SSM.HandleResponse2();
    }
}
SSM.HandleResponse2 = function()
{
    bAutoError = SSAjax.ToBool(SSM.HandleResponse.bAutoError, true);

    if (bAutoError && (SSM.idc.http.status != SSAjax.Http.Response.SUCCEEDED))
    {
        SSM.UI.DisplayErrorMessage(SSM.idc.http.message);
        return;
    }
    
    if (SSAjax.ToBool(SSM.HandleResponse.bStopProgressAndReEnable, true))
        SSM.UI.Enable();

    if (SSM.idc.binder)
        SSM.UI.ShowLoggedOnStatus(SSM.idc.binder.GetLocalData('dUser'));
        
    SSM.HandleResponse.callback();
}

//***************************************************************************

SSM.PopupUrl = function(url)
{
    window.open(url, '_ssm_popup');
}

SSM.PopupService = function(service, params)
{
    // allow "params" to be passed as single Param or querystring and convert to Params object here...
    var paramString = '';
    if ((params instanceof SSAjax.Idc.Param) || (params instanceof SSAjax.Idc.Params))
        paramString = '&' + params.EncodeForGET();
    else if (SSAjax.IsString(params))
        paramString = '&' + SSAjax.Idc.Params.CreateFromString(params).EncodeForGET();

    window.open(SSM.cgiurl + '?IdcService=' + service + paramString, '_ssm_popup');
}

//***************************************************************************
//***************************************************************************
//*********************** CONTROLLER UTILITY METHODS ************************
//***************************************************************************
//***************************************************************************

SSM.SiteHasLoaded = function() { return (SSM.currentSite && SSM.currentSite.IsLoaded()); }
SSM.SelectedSection = function() { return SSM.currentSite.GetSection(SSM.UI.hierarchy.selectedItemId); }

SSM.GetConfigInfoValue = function(name) { return SSM.configInfo.GetLocalData(name); }

SSM.SelectedSectionSettings = function() { return SSM.settings.forSection(SSM.UI.hierarchy.selectedItemId); }

SSM.IsContributorOnly = function()
{
    if (typeof SSContributor != _U)
    {
        return SSAjax.ToBool(SSContributor); // site studio already worked it out for us
    }
    else
    {
        return SSM.GetBoolCookieValue('SSContributor', true, false);
    }
}

SSM.IsForceContributorOnly = function()
{
    if (typeof SSForceContributor != _U)
    {
        return SSAjax.ToBool(SSForceContributor); // site studio already worked it out for us
    }
    else
    {
        return SSM.GetBoolCookieValue('SSForceContributor', true, false);
    }
}

SSM.IsManagerPreview = function()
{
    return SSM.GetBoolCookieValue('SSManagerPreview', true, false);
}

SSM.GetBoolCookieValue = function(name, bQueryStringAlso, bDef)
{
    // check in a cookie
    var aCookie = document.cookie.split("; ");
    for (var i = 0 ; i < aCookie.length ; i++)
    {
        var aCrumb = aCookie[i].split("=");
        if (String.CompareNoCase(aCrumb[0], name))
            return SSAjax.ToBool(aCrumb[1]);
    }

    if (SSAjax.ToBool(bQueryStringAlso, true))
    {
        // check for in the query string
        if (window.location.href.indexOf('?') >= 0)
        {
            var queryString = window.location.href.split('?')[1];
            var params = queryString.split('&');
            for (var i = 0 ; i < params.length ; i++)
            {
                var aCrumb = params[i].split("=");
                if (String.CompareNoCase(aCrumb[0], name))
                    return SSAjax.ToBool(aCrumb[1]);
            }
        }
    }
    
    return SSAjax.ToBool(bDef, false);
}

//***************************************************************************

SSM.IsExternalUrl = function(url)
{
    url = SSAjax.ToString(url);
    return ((url.length > 8) && (String.CompareNoCase(url.substr(0,7), 'http://') ||
                                 String.CompareNoCase(url.substr(0,8), 'https://')));
}

//***************************************************************************

SSM.GetString = function(key)
{
	var argValues = SSM.GetString.arguments;
	var argCount = argValues.length;
	var value = SSM.Strings[key];
	if (value == null)
		return key;

	for (var i=1; i<argCount; i++)
	{
		var regexp = eval("/{" + i + "[.!}]*}/g");
		value = value.replace(regexp, argValues[i]);
		regexp = eval("/{" + i + "q[.!}]*}/g");
		value = value.replace(regexp, "\'" + argValues[i] + "\'");
	}
	return value;
}

//***************************************************************************
