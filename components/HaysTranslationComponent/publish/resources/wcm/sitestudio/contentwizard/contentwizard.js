/////////////////////////////////////////////////////////////////////////////
// 
// Project   : Web Content Management JavaScript Library (WCM)
//
// FileName  : createnew.js
// FileType  : JavaScript
// Created   : November 2007
// Version   : 10gR4 (10.1.4.0.0)
//
// Comments  : 
//
// Copyright : Oracle, Incorporated Confidential and Proprietary
//
//             This computer program contains valuable, confidential and proprietary
//             information. Disclosure, use, or reproduction without the written
//             authorization of Oracle is prohibited. This unpublished
//             work by Oracle is protected by the laws of the United States
//             and other countries. If publication of the computer program should occur,
//             the following notice shall apply:
//
//             Copyright (c) 2007, 2008, Oracle. All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

if (!WCM) throw "must include wcm.js before including this file";

//***************************************************************************

WCM.ContentWizard = {};

//***************************************************************************

WCM.ContentWizard.wizardConfig = {}; // JSON/JavaScript object holding the CreateNew configuration info.
WCM.ContentWizard.config = null; // JSON/JavaScript object holding element's CreateNew configuration
WCM.ContentWizard.hasValidConfig = true;
WCM.ContentWizard.optionsBinder = null; // Response from SS_GET_CONFIG_INFO as a JSONBinder
WCM.ContentWizard.ContentItemLabels = {};

WCM.ContentWizard.SS_GET_CONFIG_INFO = 'SS_GET_CONFIG_INFO';
WCM.ContentWizard.pathToSpace = '../../base/images/space.gif';

WCM.ContentWizard.ChooseRegionSchemaPage = {};
WCM.ContentWizard.ChooseContentPage = {};
WCM.ContentWizard.CheckinContentPage = {};
WCM.ContentWizard.ChooseRegionTemplatePage = {};
WCM.ContentWizard.ConfirmPage = {};

WCM.ContentWizard.Pages =
[
	{ id: 'sslw_page_choose_region_schema',   name: "ChooseRegionSchemaPage",   Initialized: false, PageRef: WCM.ContentWizard.ChooseRegionSchemaPage },
	{ id: 'sslw_page_choose_content',         name: "ChooseContentPage",        Initialized: false, PageRef: WCM.ContentWizard.ChooseContentPage },
	{ id: 'sslw_page_checkin_content',        name: "CheckinContentPage",       Initialized: false, PageRef: WCM.ContentWizard.CheckinContentPage },
	{ id: 'sslw_page_choose_region_template', name: "ChooseRegionTemplatePage", Initialized: false, PageRef: WCM.ContentWizard.ChooseRegionTemplatePage },
	{ id: 'sslw_page_confirm',                name: "ConfirmPage",              Initialized: false, PageRef: WCM.ContentWizard.ConfirmPage }
];

var ContentSelectionMethod = {
	Unknown: 'Unknown',
	NewDataFile: 'NewDataFile',
	NewNativeDoc: 'NewNativeDoc',
	ExistingDoc: 'ExistingDoc',
	LocalFile: 'LocalFile',
	SelectedDoc: 'SelectedDoc',
	None: 'None'
}

WCM.ContentWizard.IsCreateNew = false;
WCM.ContentWizard.IsSwitchContent = false;
WCM.ContentWizard.IsSwitchPlaceholderDefinition = false;
WCM.ContentWizard.IsSwitchRegionTemplate = false;

WCM.ContentWizard.CurrentPage = WCM.ContentWizard.Pages[0];
WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.Unknown;
WCM.ContentWizard.NativeContentInfo = null;
WCM.ContentWizard.EditTargetUponExit = false;
WCM.ContentWizard.SelectedDocName = '';
WCM.ContentWizard.SelectedWebsiteObjectType = '';
WCM.ContentWizard.OriginalDocName = '';
WCM.ContentWizard.SelectedRegionDefinition = '';
WCM.ContentWizard.SelectedSubTemplate = '';
WCM.ContentWizard.SelectedRegionTemplate = '';
WCM.ContentWizard.SelectedDocNameRegionDefinition = '';
WCM.ContentWizard.OriginalDocNameRegionDefinition = '';

WCM.ContentWizard.OriginalTemplate = '';
WCM.ContentWizard.OriginalTemplateType = '';

WCM.ContentWizard.SourceSiteId = '';
WCM.ContentWizard.SourceNodeId = '';
WCM.ContentWizard.PageLocation = '';
WCM.ContentWizard.ServerCgiUrl = '';
WCM.ContentWizard.CanEditTarget = false;
WCM.ContentWizard.CanSwitchTemplate = true;

WCM.ContentWizard.AllowChoseRegionSchemaPage = false;
WCM.ContentWizard.HasExternalConfig = false;
WCM.ContentWizard.AllowChoseRegionTemplatePage = false;

WCM.ContentWizard.UseAutoAdvance = false;

//***************************************************************************

// ===========================
// I N I T I A L I Z A T I O N
// ===========================

WCM.ContentWizard.Initialize = function()
{
	$D().log('*******************************', window);
	$D().log('ContentWizard: Initializing', window);
	$D().log('*******************************', window);

	EnableButton( 'sslw_button_back', false );
	EnableButton( 'sslw_button_next', false );

	WCM.DHTML.AddEvent('sslw_button_cancel', 'click', WCM.ContentWizard.OnCancel);

	$D().log(WCM.ContentWizard.LogOptions, window);
	WCM.DHTML.FixIePngTransparency();

	WCM.ContentWizard.SourceSiteId = WCM.ContentWizard.GetExternal().GetOptions().siteId || '';
	WCM.ContentWizard.SourceNodeId = WCM.ContentWizard.GetExternal().GetOptions().nodeId || '';
	WCM.ContentWizard.PageLocation = WCM.ContentWizard.GetExternal().GetOptions().pageLocation || '';
	WCM.ContentWizard.ServerCgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(WCM.ContentWizard.GetExternal().GetOptions().httpCgiPath);
	WCM.ContentWizard.CanEditTarget = WCM.ContentWizard.GetExternal().GetOptions().canEditTarget || false;
	WCM.ContentWizard.SelectedDocName = WCM.ContentWizard.GetExternal().GetOptions().dDocName || '';
	WCM.ContentWizard.OriginalDocName = WCM.ContentWizard.GetExternal().GetOptions().dDocName || '';
	WCM.ContentWizard.OriginalDocNameRegionDefinition = WCM.ContentWizard.GetExternal().GetOptions().regionDefinition || '';
	WCM.ContentWizard.regionName = WCM.ContentWizard.GetExternal().GetOptions().regionName || '';
	WCM.ContentWizard.isSecondaryPage = WCM.ToBool(WCM.ContentWizard.GetExternal().GetOptions().isSecondaryPage, false);
	WCM.ContentWizard.IsCreateNew = WCM.ContentWizard.GetExternal().GetOptions().isCreateNew || false;
	WCM.ContentWizard.IsSwitchContent = WCM.ContentWizard.GetExternal().GetOptions().isSwitchContent || false;
	WCM.ContentWizard.IsSwitchPlaceholderDefinition = WCM.ContentWizard.GetExternal().GetOptions().isSwitchPlaceholderDefinition || false;
	WCM.ContentWizard.IsSwitchRegionTemplate = WCM.ContentWizard.GetExternal().GetOptions().isSwitchRegionTemplate || false;
	WCM.ContentWizard.UserLanguageId = WCM.ContentWizard.GetExternal().GetOptions().userLanguageId; 
	WCM.ContentWizard.CanSwitchTemplate = !WCM.ToBool( WCM.ContentWizard.GetExternal().GetOptions().blockSwitchTemplate, false );

	WCM.ContentWizard.OriginalTemplate = WCM.ContentWizard.GetExternal().GetOptions().template || '';
	WCM.ContentWizard.OriginalTemplateType = WCM.ContentWizard.GetExternal().GetOptions().templateType || '';





	// CUSTOMISE start
	WCM.ContentWizard.SiteLocale = WCM.ContentWizard.GetExternal().GetOptions().siteLocale || '';
	WCM.ContentWizard.LanguageCode = WCM.ContentWizard.GetExternal().GetOptions().languageCode || '';
	WCM.ContentWizard.CountryCode = WCM.ContentWizard.GetExternal().GetOptions().countryCode || '';
	// CUSTOMISE end

	
	WCM.ContentWizard.wizardConfig = WCM.ContentWizard.GetExternal().GetOptions();
	var config = WCM.ContentWizard.GetExternal().GetOptions().config;
	if( WCM.IsValid( config ) )
	{
		WCM.ContentWizard.config = config;
		WCM.ContentWizard.HasExternalConfig = true;
	}

	if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		WCM.ContentWizard.ChooseRegionTemplatePage.SetPageTitle();

		WCM.DHTML.Hide( 'sslw_button_back' );
		WCM.DHTML.Hide( 'sslw_button_next' );
		WCM.DHTML.Hide( 'sslw_button_finish' );
		WCM.DHTML.RemoveClass('sslw_button_cancel', 'sslw_first_button');
		WCM.DHTML.RemoveClass('sslw_button_cancel', 'sslw_first_button-rtl');

		WCM.DHTML.Show( 'sslw_button_ok' );
	}

	WCM.ContentWizard.LocalizeUI();

	WCM.DHTML.FlexToFillViewport('contents');
	WCM.DHTML.SetStyle('footer', 'visibility', 'visible'); //Initially hidden to improve display appearance during onload sizing operations 
	WCM.DHTML.AddWindowResizeEvent(window, WCM.ContentWizard.ResizeHandler);	
	
	WCM.ContentWizard.GetContentIDLabels();
}

//***************************************************************************

WCM.ContentWizard.LogOptions = function()
{
	return 'CreateNew Options: ' + $J(WCM.ContentWizard.GetExternal().GetOptions(), true);
};

//***************************************************************************

WCM.ContentWizard.GetContentIDLabels = function()
{
	var contentIds = [];

	if( WCM.ContentWizard.wizardConfig )
	{
		if( WCM.ContentWizard.wizardConfig['regionDefinitions'] )
		{
			WCM.ContentWizard.AddContentItemsToArray( contentIds, WCM.ContentWizard.wizardConfig['regionDefinitions'] );
		}

		if( WCM.ContentWizard.wizardConfig['regionTemplates'] )
		{
			WCM.ContentWizard.AddContentItemsToArray( contentIds, WCM.ContentWizard.wizardConfig['regionTemplates'] );
		}

		if( WCM.ContentWizard.wizardConfig['subTemplates'] )
		{
			WCM.ContentWizard.AddContentItemsToArray( contentIds, WCM.ContentWizard.wizardConfig['subTemplates'] );
		}

		if( WCM.ContentWizard.wizardConfig['regionMappings'] )
		{
			var regionMappings = WCM.ContentWizard.wizardConfig['regionMappings'];
			for (var key in regionMappings)
			{
				WCM.ContentWizard.AddContentItemsToArray( contentIds, regionMappings[key] );
			}
		}
	}

	// Call services required to obtain more info from the server
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_GET_DOCUMENT_LABELS');
	jsonBinder.SetResultSet( 'ContentIds', { fields: [{"name": "dDocName"}], rows: contentIds } );
	
	$D().log('Preparing service call for WCM.ContentWizard.GetDocumentLabels', window);

	jsonBinder.Send(WCM.ContentWizard.ServerCgiUrl, WCM.ContentWizard.GetDocumentLabelsCallback);
}

//***************************************************************************

WCM.ContentWizard.GetDocumentLabelsCallback = function(http)
{
	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		WCM.ContentWizard.ContentItemLabels = options;
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}

	WCM.ContentWizard.GetWizardConfiguration();
};

//***************************************************************************

WCM.ContentWizard.AddContentItemsToArray = function( contentItemArray, contentItems )
{
	if( contentItemArray && contentItems )
	{
		var numItems = WCM.ToInt(contentItems.length);
		
		for( var i = 0; i < numItems; i++ )
		{
			var dDocName = contentItems[i];
			contentItemArray[contentItemArray.length] = [ dDocName ];
		}
	}
}

//***************************************************************************

WCM.ContentWizard.GetWizardConfiguration = function()
{
	// Call services required to obtain more info from the server
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', WCM.ContentWizard.SS_GET_CONFIG_INFO);
	
	$D().log('Preparing service call for WCM.ContentWizard.Initialize', window);

	jsonBinder.Send(WCM.ContentWizard.ServerCgiUrl, WCM.ContentWizard.GetConfigInfoCallback);
}

//***************************************************************************

WCM.ContentWizard.GetConfigInfoCallback = function(http)
{
	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		WCM.ContentWizard.optionsBinder = new WCM.Idc.JSONBinder(text);			
		WCM.ContentWizard.InitializeControls();
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}
};

//***************************************************************************

WCM.ContentWizard.GetExternal = function()
{
	if (!WCM.ContentWizard.extId)
	{
		WCM.ContentWizard.extId = WCM.GetExternalId(window);
	}	
	return WCM.GetObject(WCM.ContentWizard.extId);
}

//***************************************************************************

WCM.ContentWizard.InitializeControls = function()
{
	$D().log('ContentWizard: InitializeControls', window);
	
	WCM.DHTML.AddEvent('sslw_button_next', 'click', WCM.ContentWizard.OnWizardNext);
	WCM.DHTML.AddEvent('sslw_button_finish', 'click', WCM.ContentWizard.OnWizardFinish);
	WCM.DHTML.AddEvent('sslw_button_back', 'click', WCM.ContentWizard.OnWizardBack);
	WCM.DHTML.AddEvent('sslw_button_ok', 'click', WCM.ContentWizard.OnWizardFinish);
	
	var context = 'default';
	if( WCM.ContentWizard.IsCreateNew )
	{
		context = 'CreateNewWizard';
	}
	else if( WCM.ContentWizard.IsSwitchContent )
	{
		context = 'ContentWizard';
	}
	else if( WCM.ContentWizard.IsSwitchPlaceholderDefinition )
	{
		context = 'PlaceholderDefinitionWizard';
	}
	else if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		context = 'SwitchViewWizard';
	}
	
	WCM.ContentWizard.InitializeHelp( context );


	if( !WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		var numRegionDefinitions = 0;
		if( WCM.ContentWizard.wizardConfig && WCM.ContentWizard.wizardConfig['regionDefinitions'] )
		{
			numRegionDefinitions = WCM.ToInt(WCM.ContentWizard.wizardConfig['regionDefinitions'].length);
		}
		var numSubTemplates = 0;
		if( WCM.ContentWizard.wizardConfig && WCM.ContentWizard.wizardConfig['subTemplates'] )
		{
			numSubTemplates = WCM.ToInt(WCM.ContentWizard.wizardConfig['subTemplates'].length);
		}
		if( !WCM.ContentWizard.CanSwitchTemplate )
		{
			numSubTemplates = 0;
		}

		if(WCM.ContentWizard.HasExternalConfig)
		{
			if( ( numRegionDefinitions == 0 ) && ( numSubTemplates == 0 ) )
			{	// probably a legacy region... proceed anyway...
				WCM.ContentWizard.AllowChoseRegionSchemaPage = false;
				WCM.ContentWizard.CurrentPage = WCM.ContentWizard.Pages[1];
				WCM.ContentWizard.SelectedRegionDefinition = '';
			}
			else	
			if( ( numRegionDefinitions == 1 ) && ( numSubTemplates == 0 ) )
			{
				// There is only one region schema, auto-select it for the user and move to page 2
				WCM.ContentWizard.AllowChoseRegionSchemaPage = false;
				WCM.ContentWizard.CurrentPage = WCM.ContentWizard.Pages[1];
				WCM.ContentWizard.SelectedRegionDefinition = WCM.ContentWizard.wizardConfig['regionDefinitions'][0];
	//			WCM.ContentWizard.ChooseRegionSchemaPage.OnChooseSchema( true );
	//			return;
			}
			else
			{
				// Progress to the first page, allowing the user to pick a region schema (or sub-template)
				WCM.ContentWizard.AllowChoseRegionSchemaPage = true;
			}
		}
		else
		{
			if( ( numRegionDefinitions == 0 ) && ( numSubTemplates == 0 ) )
			{
				// There are no region schemas available.  Therefore we cannot configure the wizard.  Generate an error and exit
				alert(WCM.GetString('wcmNoRegionDefinitionAssocWithPlaceholder'));
				WCM.ContentWizard.OnCancel();
				return;
			}
			else if( ( numRegionDefinitions == 1 ) && ( numSubTemplates == 0 ) )
			{
				// There is only one region schema, auto-select it for the user and move to page 2
				WCM.ContentWizard.AllowChoseRegionSchemaPage = false;
				WCM.ContentWizard.CurrentPage = WCM.ContentWizard.Pages[1];
				WCM.ContentWizard.SelectedRegionDefinition = WCM.ContentWizard.wizardConfig['regionDefinitions'][0];
				WCM.ContentWizard.ChooseRegionSchemaPage.OnChooseSchema( true );
				return;
			}
			else
			{
				// Progress to the first page, allowing the user to pick a region schema (or sub-template)
				WCM.ContentWizard.AllowChoseRegionSchemaPage = true;
			}
		}
	}
	else
	{
		WCM.ContentWizard.CurrentPage = WCM.ContentWizard.Pages[3];

		WCM.ContentWizard.SelectedRegionTemplate = '';
		if( WCM.ContentWizard.OriginalTemplateType == 'Region Template' )
		{
			WCM.ContentWizard.SelectedRegionTemplate = WCM.ContentWizard.OriginalTemplate;
		}

		var xRegionDefinition = WCM.ContentWizard.OriginalDocNameRegionDefinition;
		WCM.ContentWizard.SelectedDocNameRegionDefinition = xRegionDefinition;
 		WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.SelectedDoc;
		WCM.ContentWizard.AllowChoseRegionTemplatePage = true;
	}


	WCM.ContentWizard.OnWizardNextPage( WCM.ContentWizard.CurrentPage );
}

//***************************************************************************

WCM.ContentWizard.InitializeHelp = function( context )
{
	WCM.DHTML.AddEvent('wcm_help', 'click', $CBE(WCM.Help, {cgiPath : WCM.ContentWizard.ServerCgiUrl, helpContext : context, langId : WCM.ContentWizard.UserLanguageId} ));
}

//***************************************************************************
// Events
//***************************************************************************

WCM.ContentWizard.OnWizardNext = function()
{
	$D().log('ContentWizard: OnWizardNext', window);

	var nextPageId = OnNextPage( WCM.ContentWizard.CurrentPage.id );
	if( !nextPageId )
		return;

	var nextPage = GetWizardPageById( nextPageId );
	if( !nextPage )
		return;

	WCM.ContentWizard.OnWizardNextPage( nextPage );	
}

//***************************************************************************

WCM.ContentWizard.OnWizardNextPage = function( nextPage )
{
	if( !nextPage )
		return;
		
	if( !nextPage.Initialized )
	{
		nextPage.Initialized = true;
		OnInitPage( nextPage.id );
	}

	ShowPage( WCM.ContentWizard.CurrentPage.id, false );
	WCM.ContentWizard.CurrentPage = nextPage;
	ShowPage( WCM.ContentWizard.CurrentPage.id, true );
	
	OnActivatePage( WCM.ContentWizard.CurrentPage.id );
}

//***************************************************************************

WCM.ContentWizard.OnWizardBack = function()
{
	$D().log('ContentWizard: OnWizardBack', window);

	var nextPageId = OnBackPage( WCM.ContentWizard.CurrentPage.id );
	if( !nextPageId )
		return;

	var nextPage = GetWizardPageById( nextPageId );
	if( !nextPage )
		return;
		
	ShowPage( WCM.ContentWizard.CurrentPage.id, false );
	WCM.ContentWizard.CurrentPage = nextPage;
	ShowPage( WCM.ContentWizard.CurrentPage.id, true );
	
	OnActivatePage( WCM.ContentWizard.CurrentPage.id );
}

//***************************************************************************

WCM.ContentWizard.OnCancel = function()
{
	$D().log('WCM.ContentWizard.OnCancel', window);

	if( WCM.ContentWizard.GetExternal() )
	{
		// Close window.open type window
		window.setTimeoutEx(WCM.ContentWizard.CancelWizard, 500);
	}
}

//***************************************************************************

WCM.ContentWizard.CancelWizard = function()
{
	WCM.ContentWizard.CloseWizard();
}

//***************************************************************************

WCM.ContentWizard.CloseWizard = function( params )
{
	window.focus();
	WCM.ContentWizard.GetExternal().Close( params );
}

//***************************************************************************

WCM.ContentWizard.OnWizardFinish = function()
{
	$D().log('WCM.ContentWizard.OnWizardFinish', window);

	var fn = GetFunctionReference( WCM.ContentWizard.CurrentPage.id, 'OnWizardFinish' );
	if( fn )
	{
		fn( $CB(WCM.ContentWizard.OnWizardFinishCallback) );
	}
}

//***************************************************************************

WCM.ContentWizard.OnWizardFinishCallback = function( link )
{
	$D().log('WCM.ContentWizard.OnWizardFinishCallback', window);

	var dDocName = WCM.ContentWizard.GetSelectedDocName() || '';
	if( ( dDocName.length != 0 ) &&
		( WCM.ContentWizard.SourceSiteId.length != 0 ) )
	{
		WCM.ContentWizard.AddToWebsite( WCM.ContentWizard.SourceSiteId, dDocName, WCM.ContentWizard.FinishWizard );
	}
	else
	{
		WCM.ContentWizard.FinishWizard();
	}
}

//***************************************************************************

WCM.ContentWizard.AddToWebsite = function( siteId, dDocName, callback )
{
	var jsonRequest = new WCM.Idc.JSONBinder();
	jsonRequest.SetLocalDataValue('IdcService', 'SS_ADD_WEBSITE_ID');
	jsonRequest.SetLocalDataValue('fieldName', 'xWebsites');
	jsonRequest.SetLocalDataValue('dDocName', dDocName);
	jsonRequest.SetLocalDataValue('siteId', siteId);

	$D().log(function()
	{ 
		return 'Preparing service call for WCM.ContentWizard.AddToWebsite: ' + dDocName + ' (' + siteId + ')';
	}, window);

	var options = {};
	options.validate = false;

	jsonRequest.Send(WCM.ContentWizard.ServerCgiUrl, function(http) 
	{
		callback();
	}, options );
}

//***************************************************************************

WCM.ContentWizard.FinishWizard = function()
{
	$D().log('WCM.ContentWizard.FinishWizard', window);

	if( WCM.ContentWizard.GetExternal() )
	{
		// Close window.open type window
		if( WCM.ContentWizard.EditTargetUponExit )
		{
			WCM.ContentWizard.GetLinkAndExit();
		}
		else if( WCM.ContentWizard.IsSwitchContent || WCM.ContentWizard.IsSwitchPlaceholderDefinition || WCM.ContentWizard.IsSwitchRegionTemplate )
		{
			WCM.ContentWizard.SwitchAssociationAndExit();
		}
		else
		{
			var returnParams = { "dDocName": WCM.ContentWizard.SelectedDocName, "EditTarget": WCM.ContentWizard.EditTargetUponExit };
			$D().log($CB(WCM.ContentWizard.LogOnWizardFinishReturnParams, returnParams), window);
			
			window.setTimeoutEx($CB(WCM.ContentWizard.CloseWizard, returnParams), 500);
		}
	}
}

//*****************************************************************************

WCM.ContentWizard.LogOnWizardFinishReturnParams = function(returnParams)
{
	return 'WCM.ContentWizard.OnWizardFinish returning: ' + $J(returnParams, true);
};

//*****************************************************************************

WCM.ContentWizard.ResetWizardPages = function()
{
	for( var i = 0; i < WCM.ContentWizard.Pages.length; i++ )
	{
		var page = WCM.ContentWizard.Pages[i];
		page.Initialized = false;
	} 
	
	return page;
}

//*****************************************************************************
//*****************************************************************************
//*****************************************************************************
//*****************************************************************************

function OnInitPage( pageId )
{
	var fn = GetFunctionReference( pageId, 'OnInit' );
	if( fn )
	{
		fn();
	}
}

function OnActivatePage( pageId )
{
	var fn = GetFunctionReference( pageId, 'OnActivate' );
	if( fn )
	{
		fn();
	}
}

function OnNextPage( pageId )
{
	var fn = GetFunctionReference( pageId, 'OnWizardNext' );

	var bOK = false;	
	if( fn )
	{
		bOK = fn();
	}

	var nextPageId = null; 
	if( bOK )
	{
		nextPageId = ComputeNextPageId( pageId );
	}

	return nextPageId;
}

function OnBackPage( pageId )
{
	var fn = GetFunctionReference( pageId, 'OnWizardBack' );

	var bOK = false;	
	if( fn )
	{
		bOK = fn();
	}

	var nextPageId = null; 
	if( bOK )
	{
		nextPageId = ComputeBackPageId( pageId );
	}
	
	return nextPageId;
}

//***************************************************************************

function ComputeNextPageId( currentPageId )
{
	var nextPageId = null;

	switch( currentPageId )
	{
		case 'sslw_page_choose_region_schema':
			if( WCM.ContentWizard.SelectedSubTemplate.length > 0 )
			{
				nextPageId = 'sslw_page_confirm';
			}
			else
			{
				nextPageId = 'sslw_page_choose_content';
			}
			break;
			
		case 'sslw_page_choose_content':
			// If we're using an existing, go to the target
			if( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.SelectedDoc )
			{
				if( WCM.ContentWizard.AllowChoseRegionTemplatePage )
				{
					nextPageId = 'sslw_page_choose_region_template';
				}
				else
				{
					nextPageId = 'sslw_page_confirm';
				}
			}
			else if( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.None )
			{
				nextPageId = 'sslw_page_confirm';
			}
			else
			{
				nextPageId = 'sslw_page_checkin_content';
			}
			break;

		case 'sslw_page_checkin_content':
			if( WCM.ContentWizard.AllowChoseRegionTemplatePage )
			{
				nextPageId = 'sslw_page_choose_region_template';
			}
			else
			{
				nextPageId = 'sslw_page_confirm';
			}
			break;

		case 'sslw_page_choose_region_template':
			nextPageId = 'sslw_page_confirm';
			break;

		case 'sslw_page_confirm':
			break;
	}		

	return nextPageId;
}

//***************************************************************************

function ComputeBackPageId( currentPageId )
{
	var nextPageId = null;
	
	switch( currentPageId )
	{
		case 'sslw_page_choose_region_schema':
			break;
			
		case 'sslw_page_choose_content':
			if( WCM.ContentWizard.AllowChoseRegionSchemaPage )
			{
				nextPageId = 'sslw_page_choose_region_schema';
			}
			break;

		case 'sslw_page_checkin_content':
			nextPageId = 'sslw_page_choose_content';
			break;

		case 'sslw_page_choose_region_template':
			nextPageId = 'sslw_page_choose_content';
			break;

		case 'sslw_page_confirm':
			if( WCM.ContentWizard.SelectedSubTemplate.length > 0 )
			{
				nextPageId = 'sslw_page_choose_region_schema';
			}
			else if( ( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.SelectedDoc ) &&
				WCM.ContentWizard.AllowChoseRegionTemplatePage )
			{
				nextPageId = 'sslw_page_choose_region_template';
			}
			else
			{
				nextPageId = 'sslw_page_choose_content';
			}
			break;
	}		

	return nextPageId;
}

//***************************************************************************

function GetFunctionReference( pageId, fcnName )
{
	var fn = null;
	
	var pageObject = GetWizardPageById( pageId );
	if( pageObject )
	{
		if( WCM.IsValid( WCM.ContentWizard[ pageObject.name ] ) )
		{
			var pageRef = WCM.ContentWizard[ pageObject.name ];
			fn = ( WCM.IsValid( pageRef[ fcnName ] ) ? pageRef[ fcnName ] : null );
		}
	}
	
	return fn;
}

//***************************************************************************

function GetWizardPageById( id )
{
	var page = null;
	for( var i = 0; i < WCM.ContentWizard.Pages.length; i++ )
	{
		var tempPage = WCM.ContentWizard.Pages[i]; 
		if( tempPage.id == id )
		{
			page = tempPage;
			break;
		}
	} 
	
	return page;
}

function EnableButton( buttonId, bEnabled )
{
	var button = $ID(buttonId);
	if( button )
	{
		button.disabled = !bEnabled;
	}	
}

function IsButtonEnabled( buttonId )
{
	var enabled = false;
	
	var button = $ID(buttonId);
	if( button )
	{
		if( button.disabled )
		{
		}
		else
		{
			enabled = true;
		}
	}
	
	return enabled;
}

function SetButtonLabel( buttonId, label )
{
	var button = $ID(buttonId);
	if( button )
	{
		button.value = label;
	}	
}

function ShowFinishButton( bShow )
{
	EnableButton( 'sslw_button_finish', bShow );
	EnableButton( 'sslw_button_next', !bShow );
}


//*****************************************************************************

function $PageID( o )
{
	return document.getElementById(o);
}

//*****************************************************************************

function ShowPage( pageId, bShow )
{
	var pageObject = $PageID( pageId );
	if( pageObject )
	{
		if( bShow )
		{
			WCM.DHTML.Show( pageObject );
		}
		else
		{
			WCM.DHTML.Hide( pageObject );
		}
	}
}


//***************************************************************************
//***************************************************************************
// ChooseRegionSchemaPage
//***************************************************************************
//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnInit = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionSchemaPage.OnInit', window);
	
	WCM.ContentWizard.ChooseRegionSchemaPage.SetPageTitle();

	var numRegionDefinitions = 0;
	if( WCM.ContentWizard.wizardConfig && WCM.ContentWizard.wizardConfig['regionDefinitions'] )
	{
		numRegionDefinitions = WCM.ToInt(WCM.ContentWizard.wizardConfig['regionDefinitions'].length);
	}
	var numSubTemplates = 0;
	if( WCM.ContentWizard.wizardConfig && WCM.ContentWizard.wizardConfig['subTemplates'] )
	{
		numSubTemplates = WCM.ToInt(WCM.ContentWizard.wizardConfig['subTemplates'].length);
	}
	if( !WCM.ContentWizard.CanSwitchTemplate )
	{
		numSubTemplates = 0;
	}

	var selectRegionDefinition = true;

	if( WCM.ContentWizard.UseAutoAdvance )
	{
		EnableButton( 'sslw_subtemplate_select', false );
		EnableButton( 'sslw_region_schema_select', false );
	}
	
	WCM.DHTML.AddEvent('sslw_region_schema_radio_auto_advance',  'click', $CB(WCM.ContentWizard.ChooseRegionSchemaPage.ChooseRegionSchemaPageAutoAdvance, 'sslw_region_schema_radio'));
	WCM.DHTML.AddEvent('sslw_subtemplate_radio_auto_advance', 'click', $CB(WCM.ContentWizard.ChooseRegionSchemaPage.ChooseRegionSchemaPageAutoAdvance, 'sslw_subtemplate_radio'));

	// Populates the sub-templates combo
	var mappedTemplateDocName = WCM.ContentWizard.GetUrlMapping( "TemplateUrl" );
	var selectList = $ID('sslw_subtemplate_select');
	if( numSubTemplates > 0 )
	{
		selectList.selectedIndex = 0;
		
		for( var i = 0; i < numSubTemplates; i++ )
		{
			var subTemplate = WCM.ContentWizard.wizardConfig['subTemplates'][i];
			var subTemplateLabel = WCM.ContentWizard.getContentItemLabel(subTemplate);

			var newOption = new Option( subTemplateLabel );
			newOption.subtemplate = subTemplate;
			
			if( subTemplate.toLowerCase() == mappedTemplateDocName.toLowerCase() )
			{
				newOption.selected = true;
				selectRegionDefinition = false;
			}
			
			selectList.options[selectList.options.length] = newOption;
		}
		
		EnableButton( 'sslw_subtemplate_select', true );
		if( WCM.ContentWizard.UseAutoAdvance )
		{
			EnableButton( 'sslw_region_schema_select', false );
		}
		WCM.DHTML.AddEvent('sslw_subtemplate_select', 'change', $CB(WCM.ContentWizard.ChooseRegionSchemaPage.OnSubTemplateChanged, selectList) );
		
		$ID('sslw_subtemplate_radio').checked = true;
		$ID('sslw_region_schema_radio').checked = false;
	}


	// Populate the Region Schemas combo
	var mappedContentRegionDefinition = WCM.ContentWizard.OriginalDocNameRegionDefinition;
	selectList = $ID('sslw_region_schema_select');
	if( numRegionDefinitions > 0 )
	{
		selectList.selectedIndex = 0;
		
		var iLen = WCM.ToInt(WCM.ContentWizard.wizardConfig['regionDefinitions'].length);
		for( var i = 0; i < iLen; i++ )
		{
			var regionSchema = WCM.ContentWizard.wizardConfig['regionDefinitions'][i];
			var regionSchemaLabel = WCM.ContentWizard.getContentItemLabel(regionSchema);

			var newOption = new Option( regionSchemaLabel );
			newOption.regionschema = regionSchema;
			
			if( mappedContentRegionDefinition.toLowerCase() == regionSchema.toLowerCase() )
			{
				newOption.selected = true;
			}
			
			selectList.options[selectList.options.length] = newOption;
		}
		
		if( WCM.ContentWizard.UseAutoAdvance )
		{
			EnableButton( 'sslw_subtemplate_select', false );
		}
		EnableButton( 'sslw_region_schema_select', true );
		WCM.DHTML.AddEvent('sslw_region_schema_select', 'change', $CB(WCM.ContentWizard.ChooseRegionSchemaPage.OnRegionSchemaChanged, selectList) );

		if( selectRegionDefinition )
		{
			$ID('sslw_subtemplate_radio').checked = false;
			$ID('sslw_region_schema_radio').checked = true;
		}
	}

	WCM.DHTML.AddEvent('sslw_region_schema_radio', 'click', WCM.ContentWizard.ChooseRegionSchemaPage.OnClickedRadioButton);
	WCM.DHTML.AddEvent('sslw_subtemplate_radio',   'click', WCM.ContentWizard.ChooseRegionSchemaPage.OnClickedRadioButton);

	// Show the relevant rows	
	WCM.ContentWizard.ChooseRegionSchemaPage.TitleString = WCM.GetString('wcmSwitchChooseRegionDefinition');
	if( numRegionDefinitions > 0 )
	{
		WCM.ContentWizard.ChooseRegionSchemaPage.TitleString = 'wcmSwitchChooseRegionDefinition';
		WCM.DHTML.Show( $ID('sslw_region_schema_row') );
	}
	if( numSubTemplates > 0 )
	{
		WCM.ContentWizard.ChooseRegionSchemaPage.TitleString = 'wcmSwitchChooseSubTemplate';
		WCM.DHTML.Show( $ID('sslw_subtemplate_row') );
	}
	if( ( numRegionDefinitions > 0 ) && ( numSubTemplates > 0 ) )
	{
		WCM.ContentWizard.ChooseRegionSchemaPage.TitleString = 'wcmSwitchChooseRegionDefinitionOrSubTemplate';
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.ChooseRegionSchemaPageAutoAdvance = function(inputId)
{
	WCM.ContentWizard.UseAutoAdvance = true;
	$ID(inputId).checked = true;
	WCM.ContentWizard.ChooseRegionSchemaPage.UpdateFinishButton();
	WCM.ContentWizard.UseAutoAdvance = false;
	WCM.ContentWizard.OnWizardNext();
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.SetPageTitle = function()
{
	WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmChooseContentRegionDefinition'));

	if( WCM.ContentWizard.IsCreateNew )
	{
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmCreateNewChooseRegionSchema'));
	}
	else if( WCM.ContentWizard.IsSwitchContent )
	{
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString(WCM.ContentWizard.ChooseRegionSchemaPage.TitleString));
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnClickedRadioButton = function()
{
	WCM.ContentWizard.ChooseRegionSchemaPage.UpdateFinishButton();

	if( WCM.ContentWizard.UseAutoAdvance )
	{
		var bWasRegionSchemaListEnabled = IsButtonEnabled( 'sslw_region_schema_select' );
		var bWasSubtemplateListEnabled = IsButtonEnabled( 'sslw_subtemplate_select' );

		$ID('sslw_region_schema_select').disabled = $ID('sslw_region_schema_radio').checked ? false : true;
		$ID('sslw_subtemplate_select').disabled = $ID('sslw_subtemplate_radio').checked ? false : true;

		// Auto Advance
		if( IsButtonEnabled( 'sslw_button_next' ) )
		{
			if( $ID('sslw_region_schema_radio').checked && bWasRegionSchemaListEnabled )
			{
				WCM.ContentWizard.OnWizardNext();
			}
			else if( $ID('sslw_subtemplate_radio').checked && bWasSubtemplateListEnabled )
			{
				WCM.ContentWizard.OnWizardNext();
			}
		}
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnRegionSchemaChanged = function()
{
	WCM.ContentWizard.ChooseRegionSchemaPage.GetSelectedOption(false);
	$ID('sslw_region_schema_radio').checked = true;
	$ID('sslw_subtemplate_radio').checked = false;

	WCM.ContentWizard.SelectListAutoAdvance();
	WCM.ContentWizard.ChooseRegionSchemaPage.UpdateFinishButton();
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnSubTemplateChanged = function()
{
	WCM.ContentWizard.ChooseRegionSchemaPage.GetSelectedOption(false);
	$ID('sslw_subtemplate_radio').checked = true;
	$ID('sslw_region_schema_radio').checked = false;

	WCM.ContentWizard.SelectListAutoAdvance();
	WCM.ContentWizard.ChooseRegionSchemaPage.UpdateFinishButton();
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnActivate = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionSchemaPage.OnActivate', window);
	
	WCM.ContentWizard.ChooseRegionSchemaPage.SetPageTitle();

	WCM.ContentWizard.hasValidConfig = false;
	EnableButton( 'sslw_button_back', false );

	var bEnable = WCM.ContentWizard.ChooseRegionSchemaPage.GetSelectedOption(false);
	EnableButton( 'sslw_button_next', bEnable );

	WCM.ContentWizard.ChooseRegionSchemaPage.UpdateFinishButton();
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.UpdateFinishButton = function()
{
	var bCanFinish = WCM.ContentWizard.ChooseRegionSchemaPage.CanFinish();
	EnableButton( 'sslw_button_finish', bCanFinish );
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.CanFinish = function()
{
	var bCanFinish = false;
	
	if( $ID('sslw_subtemplate_radio').checked )
	{
		var selectList = $ID('sslw_subtemplate_select');
		if( selectList.selectedIndex >= 0 )
		{
			var subtemplate = selectList.options[selectList.selectedIndex].subtemplate;
			if( subtemplate.length != 0 )
			{
				bCanFinish = true;
			}
		}
	}
	
	return bCanFinish;
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnWizardNext = function( switchContentConfig )
{
	$D().log('WCM.ContentWizard.ChooseRegionSchemaPage.OnWizardNext', window);

	var bOK = false;

	if( WCM.ContentWizard.config && WCM.ContentWizard.hasValidConfig )
	{
		WCM.ContentWizard.ResetWizardPages();
		bOK = true;
	}
	else if( WCM.ContentWizard.ChooseRegionSchemaPage.GetSelectedOption() )
	{
		if( WCM.ContentWizard.SelectedRegionDefinition.length > 0 )
		{
			if (WCM.ContentWizard.HasExternalConfig)
			{
				WCM.ContentWizard.hasValidConfig = true;
				WCM.ContentWizard.OnWizardNext();
			}
			else
			{
				WCM.ContentWizard.hasValidConfig = false;
				window.setTimeoutEx( $CB(WCM.ContentWizard.ChooseRegionSchemaPage.OnChooseSchema, false), 10 );
			}
		}
		else if( WCM.ContentWizard.SelectedSubTemplate.length > 0 )
		{
			bOK = true;
		}
	}	
	
	return bOK;
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnWizardFinish = function( callback )
{
	$D().log('WCM.ContentWizard.ChooseRegionSchemaPage.OnWizardFinish', window);
	
	if( WCM.ContentWizard.ChooseRegionSchemaPage.CanFinish() &&
		WCM.ContentWizard.ChooseRegionSchemaPage.GetSelectedOption() )
	{
		if( WCM.ContentWizard.SelectedRegionDefinition.length > 0 )
		{
		}
		else if( WCM.ContentWizard.SelectedSubTemplate.length > 0 )
		{
			callback();
		}
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnChooseSchema = function( isInitializing )
{
	// Obtain the wizard configuration for the region schema
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_GET_SWITCH_CONTENT_CONFIG');
	jsonBinder.SetLocalDataValue('dDocName', WCM.ContentWizard.OriginalDocName);
	jsonBinder.SetLocalDataValue('siteId', WCM.ContentWizard.SourceSiteId);
	jsonBinder.SetLocalDataValue('nodeId', WCM.ContentWizard.SourceNodeId);
	jsonBinder.SetLocalDataValue('isSecondaryPage', WCM.ContentWizard.isSecondaryPage ? '1' : '0');
	jsonBinder.SetLocalDataValue('regionName', WCM.ContentWizard.regionName);
	jsonBinder.SetLocalDataValue('regionDefinition', WCM.ContentWizard.SelectedRegionDefinition );

	$D().log('Preparing service call for WCM.CONTRIBUTOR.Switch', window);

	jsonBinder.Send(WCM.ContentWizard.ServerCgiUrl, $CB(WCM.ContentWizard.ChooseRegionSchemaPage.SwitchContentCallback, isInitializing));
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.SwitchContentCallback = function(http, isInitializing)
{
	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		WCM.ContentWizard.config = options.config;
		if( WCM.ContentWizard.config )
		{
			WCM.ContentWizard.hasValidConfig = true;
			if( isInitializing )
			{
				WCM.ContentWizard.OnWizardNextPage( WCM.ContentWizard.CurrentPage );
			}
			else
			{
				WCM.ContentWizard.OnWizardNext();
			}
		}
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.GetSelectedOption = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionSchemaPage.GetSelectedOption', window);

	var bOK = false;
	WCM.ContentWizard.SelectedRegionDefinition = '';
	WCM.ContentWizard.SelectedSubTemplate = '';

	if( $ID('sslw_region_schema_radio').checked )
	{
		// One item needs to be selected
		var selectList = $ID('sslw_region_schema_select');
		if( selectList.selectedIndex >= 0 )
		{
			var doctypeinfo = selectList.options[selectList.selectedIndex].regionschema;
			WCM.ContentWizard.SelectedRegionDefinition = doctypeinfo;

			bOK = true;
		}
	}
	else if( $ID('sslw_subtemplate_radio').checked )
	{
		var selectList = $ID('sslw_subtemplate_select');
		if( selectList.selectedIndex >= 0 )
		{
			var subtemplate = selectList.options[selectList.selectedIndex].subtemplate;
			WCM.ContentWizard.SelectedSubTemplate = subtemplate;

			bOK = true;
		}
	}
	
	return bOK;
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionSchemaPage.OnWizardBack = function()
{
	$D().log('WCM.ChooseRegionSchemaPage.ChooseContentPage.OnWizardBack', window);
	return false;
}

//***************************************************************************
//***************************************************************************
// ChooseContentPage
//***************************************************************************
//***************************************************************************

WCM.ContentWizard.ChooseContentPage.OnInit = function()
{
	$D().log('WCM.ContentWizard.ChooseContentPage.OnInit', window);
	
	WCM.ContentWizard.ChooseContentPage.SetPageTitle();
	
	EnableButton( 'sslw_button_back', WCM.ContentWizard.AllowChoseRegionSchemaPage );
	WCM.ContentWizard.ChooseContentPage.selectionIsValid = false;

	var Settings = WCM.ContentWizard.config || {};
	if( WCM.ContentWizard.IsSwitchContent || WCM.ContentWizard.IsSwitchPlaceholderDefinition || WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		Settings = Settings.switchregioncontent || {};
	}

	WCM.DHTML.Hide( $ID('sslw_choose_content_new_row') );
	WCM.DHTML.Hide( $ID('sslw_choose_content_native_row') );
	WCM.DHTML.Hide( $ID('sslw_choose_content_existing_row') );
	WCM.DHTML.Hide( $ID('sslw_choose_content_local_row') );
	WCM.DHTML.Hide( $ID('sslw_choose_content_none_row') );
	WCM.DHTML.Hide( $ID('sslw_choose_selected_content_row') );

	WCM.DHTML.AddEvent('sslw_choose_content_new_auto_advance', 'click', $CB(WCM.ContentWizard.ChooseContentPage.ChooseContentPageAutoAdvance, 'sslw_choose_content_new'));
	WCM.DHTML.AddEvent('sslw_choose_content_native_auto_advance', 'click', $CB(WCM.ContentWizard.ChooseContentPage.ChooseContentPageAutoAdvance, 'sslw_choose_content_native'));
	WCM.DHTML.AddEvent('sslw_choose_content_existing_auto_advance', 'click', $CB(WCM.ContentWizard.ChooseContentPage.ChooseContentPageAutoAdvance, 'sslw_choose_content_existing'));
	WCM.DHTML.AddEvent('sslw_choose_content_local_auto_advance', 'click', $CB(WCM.ContentWizard.ChooseContentPage.ChooseContentPageAutoAdvance, 'sslw_choose_content_local'));
	WCM.DHTML.AddEvent('sslw_choose_content_none_auto_advance', 'click', $CB(WCM.ContentWizard.ChooseContentPage.ChooseContentPageAutoAdvance, 'sslw_choose_content_none'));
	WCM.DHTML.AddEvent('sslw_choose_selected_content_auto_advance', 'click', $CB(WCM.ContentWizard.ChooseContentPage.ChooseContentPageAutoAdvance, 'sslw_choose_selected_content'));

	if( WCM.ContentWizard.UseAutoAdvance )
	{
		EnableButton('sslw_native_content_select', false );
	}

	WCM.DHTML.AddEvent('sslw_choose_content_new',      'click', WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage);
	WCM.DHTML.AddEvent('sslw_choose_content_native',   'click', WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage);
	WCM.DHTML.AddEvent('sslw_choose_content_existing', 'click', WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage);
	WCM.DHTML.AddEvent('sslw_choose_content_local',    'click', WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage);
	WCM.DHTML.AddEvent('sslw_choose_content_none',     'click', WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage);
	WCM.DHTML.AddEvent('sslw_choose_selected_content', 'click', WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage);
	if( WCM.ContentWizard.UseAutoAdvance )
	{
		WCM.DHTML.AddEvent('sslw_native_content_select',   'change', WCM.ContentWizard.SelectListAutoAdvance );
	}
	else
	{
		WCM.DHTML.AddEvent('sslw_native_content_select',   'click', WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage);
	}

	$ID('sslw_choose_content_new').checked = false;
	$ID('sslw_choose_content_native').checked = false;
	$ID('sslw_choose_content_existing').checked = false;
	$ID('sslw_choose_content_local').checked = false;
	$ID('sslw_choose_content_none').checked = false;
	$ID('sslw_choose_selected_content').checked = false;

	var defaultOption = ''; // Order of importance is: new data file, existing data file, new local document, new native document

	// Show and hide various options based upon the configuration
	if( WCM.ContentWizard.IsSwitchContent || WCM.ContentWizard.IsSwitchPlaceholderDefinition || WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		if( WCM.ToBool(Settings['choosenone']) )
		{
			WCM.DHTML.Show( $ID('sslw_choose_content_none_row') );
			defaultOption = 'sslw_choose_content_none';
		}
	}
	
	if( WCM.ToBool(Settings['createnewnative']) &&
		Settings['createnewnativedoctypes'] && WCM.IsValid(Settings['createnewnativedoctypes']))
	{
		var selectList = $ID('sslw_native_content_select');
		
		var iLen = WCM.ToInt(Settings['createnewnativedoctypes'].length);
		for( var i = 0; i < iLen; i++ )
		{
			var doctype = Settings['createnewnativedoctypes'][i];
			
			var doctypeinfo = WCM.ContentWizard.GetDefaultDocumentInfo( doctype );
			if( doctypeinfo != null )
			{
				WCM.DHTML.Show( $ID('sslw_choose_content_native_row') );
				defaultOption = 'sslw_choose_content_native';
			
				var newOption = new Option( doctypeinfo.description );
				newOption.doctypeinfo = doctypeinfo;
				
				selectList.options[selectList.options.length] = newOption;
			}
		}
	}

	if( WCM.ToBool(Settings['chooselocal']) )
	{
		WCM.DHTML.Show( $ID('sslw_choose_content_local_row') );
		defaultOption = 'sslw_choose_content_local';
	}

	if( WCM.ContentWizard.IsSwitchContent || WCM.ContentWizard.IsSwitchPlaceholderDefinition || WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		if( WCM.ToBool(Settings['choosemanaged']) )
		{
			WCM.DHTML.Show( $ID('sslw_choose_content_existing_row') );
			defaultOption = 'sslw_choose_content_existing';
		}

		if( WCM.ContentWizard.SelectedDocName.length > 0 )
		{
			WCM.DHTML.Show( $ID('sslw_choose_selected_content_row') );
		}
	}

	if( WCM.ToBool(Settings['createnewxml']) )
	{
		var doctypeinfo = WCM.ContentWizard.GetDefaultDocumentInfo( "SSContributorDataFile" );
		if( doctypeinfo != null )
		{
			WCM.DHTML.Show( $ID('sslw_choose_content_new_row') );
			defaultOption = 'sslw_choose_content_new';
		}
	}

	if( defaultOption.length != 0 )
	{
		$ID(defaultOption).checked = true;
	}

	if( WCM.ContentWizard.SelectedDocName.length > 0 )
	{
		WCM.ContentWizard.ChooseContentPage.UpdatePageWithDocument( WCM.ContentWizard.SelectedDocName, null, null, true );
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.ChooseContentPageAutoAdvance = function(inputId)
{
	if (WCM.IsValid(inputId))
	{
		WCM.ContentWizard.UseAutoAdvance = true;
		$ID(inputId).checked = true;
		WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage();
		if (inputId == 'sslw_choose_content_native')
		{
			WCM.ContentWizard.SelectListAutoAdvance()
		}
		WCM.ContentWizard.UseAutoAdvance = false;
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.SetPageTitle = function()
{
	if( WCM.ContentWizard.IsCreateNew )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmChooseContentFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmCreateNewChooseFile'));
	}
	else if( WCM.ContentWizard.IsSwitchContent )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmChooseContentFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchChooseFile'));
	}
	else if( WCM.ContentWizard.IsSwitchPlaceholderDefinition )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmChoosePlaceholderDefinitionFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchChoosePlaceholderDefinition'));
	}
	else if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmChooseViewFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchChooseView'));
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.OnUpdateChooseContentPage = function()
{
	WCM.ContentWizard.ChooseContentPage.OnActivate();

	var bWasListEnabled = IsButtonEnabled( 'sslw_native_content_select' );
	$ID('sslw_native_content_select').disabled = $ID('sslw_choose_content_native').checked ? false : true;

	// Auto Advance
	if( WCM.ContentWizard.UseAutoAdvance && IsButtonEnabled( 'sslw_button_next' ) )
	{
		if( $ID('sslw_choose_content_native').checked && !bWasListEnabled )
		{
		}
		else
		{
			WCM.ContentWizard.OnWizardNext();
		}
	}
}

//***************************************************************************

WCM.ContentWizard.SelectListAutoAdvance = function()
{
	if( WCM.ContentWizard.UseAutoAdvance )
	{
		$D().log('WCM.ContentWizard.SelectListAutoAdvance', window);

		if( IsButtonEnabled( 'sslw_button_next' ) )
		{
			WCM.ContentWizard.OnWizardNext();
		}
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.ChooseManagedDocument = function()
{
	$D().log('WCM.ContentWizard.ChooseContentPage.ChooseManagedDocument', window);
	var Settings = WCM.ContentWizard.config || {};
	if( WCM.ContentWizard.IsSwitchContent || WCM.ContentWizard.IsSwitchPlaceholderDefinition || WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		Settings = Settings.switchregioncontent || {};
	}
	var queryTextObject = Settings.choosemanagedquerytext || {};

	var options = new Object();
	options.httpCgiPath = WCM.ToString(WCM.ContentWizard.GetExternal().GetOptions().httpCgiPath);
	options.queryText = WCM.ToString(queryTextObject["querytext"], '');
	options.coreContentOnly = WCM.ToString(queryTextObject["corecontentonly"], '');
	options.callback = $CB(WCM.ContentWizard.ChooseContentPage.OnSelectionComplete, WCM.ContentWizard.OnWizardNext);

	WCM.ContentServerPopup.ChooseManagedDocument(options);
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.OnSelectionComplete = function(returnParams, autoNextCallback)
{
	autoNextCallback = autoNextCallback || false;

	$D().log('WCM.ContentWizard.ChooseContentPage.OnSelectionComplete', window);
	
	if( returnParams )
	{
		var dID = WCM.ToString(returnParams['dID'], '');
		if( dID.length > 0 )
		{
			window.setTimeoutEx( $CB(WCM.ContentWizard.ChooseContentPage.UpdatePageWithDocument, null, dID, autoNextCallback), 10 );
		}
	}
}

//***************************************************************************

function CheckCheckinPopupUrl( options )
{
	var myFrame = $ID('sslw_checkin_content_frame');

	if (myFrame)
	{
		var bResetTimer = true;
		try
		{
			var dID = '';
			var url = myFrame.contentWindow.location.href;
			if( !url )
			{
				url = myFrame.location.href;
			}
			if( url )
			{
				var findStr = '&dID=';
				var pos = url.indexOf( findStr );
				if( pos > 0 )
				{
					dID = url.substring( pos + findStr.length );
					pos = dID.indexOf( '&' );
					if( pos > 0 ) 
					{
						dID = dID.substring( 0, pos );	
					}
				}
			}

			if( dID.length > 0 )
			{
				bResetTimer = false;
				options.callback( { "dID": dID } );
			}
		}
		catch(ex) // IE sometimes throws permission denied error when examining popup.closed if this timeout happens at exactly same time user clicks x button
		{
			// alert( "Exception!" );
		}
		
		if( bResetTimer && options['resizeAfterLoad'] && WCM.ToBool(options['resizeAfterLoad']) )
		{
			options.resizeAfterLoad = !WCM.ContentWizard.CheckinContentPage.CheckFrameLoaded();
		}

		if( bResetTimer )
		{
			window.setTimeoutEx($CB(CheckCheckinPopupUrl, options), 250);
		}
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.OnActivate = function()
{
	$D().log('WCM.ContentWizard.ChooseContentPage.OnActivate', window);
	
	WCM.ContentWizard.ChooseContentPage.SetPageTitle();

	EnableButton( 'sslw_button_back', WCM.ContentWizard.AllowChoseRegionSchemaPage );

	var bEnable = WCM.ContentWizard.ChooseContentPage.GetSelectedOption(false);
	EnableButton( 'sslw_button_next', bEnable );
	
	WCM.ContentWizard.ChooseContentPage.UpdateFinishButton();
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.UpdateFinishButton = function()
{
	$D().log('WCM.ContentWizard.ChooseContentPage.UpdateFinishButton', window);
	
	var bCanFinish = WCM.ContentWizard.ChooseContentPage.CanFinish();
	EnableButton( 'sslw_button_finish', bCanFinish );
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.CanFinish = function()
{
	var bCanFinish = false;

	if( $ID('sslw_choose_selected_content').checked )
	{
		if( WCM.ContentWizard.ChooseContentPage.selectionIsValid )
		{
			if( WCM.ContentWizard.SelectedDocName.length != 0 )
			{
				bCanFinish = true;
			}
		}
	}
	else if( $ID('sslw_choose_content_none').checked ? true : false )
	{
		bCanFinish = true;
	}
	
	return bCanFinish;
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.OnWizardNext = function()
{
	$D().log('WCM.ContentWizard.ChooseContentPage.OnWizardNext', window);

	var bOK = WCM.ContentWizard.ChooseContentPage.GetSelectedOption(true);
	return bOK;
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.OnWizardFinish = function( callback )
{
	$D().log('WCM.ContentWizard.ChooseContentPage.OnWizardFinish', window);

	if( WCM.ContentWizard.ChooseContentPage.CanFinish() &&
		WCM.ContentWizard.ChooseContentPage.GetSelectedOption( false ) )
	{
		callback();
	}
}

//***************************************************************************
WCM.ContentWizard.ChooseContentPage.GetSelectedOption = function( forNext )
{
	$D().log('WCM.ContentWizard.ChooseContentPage.OnWizardNext', window);

	var bOK = false;
	WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.Unknown;

	// One item needs to be selected
	if( !bOK )
	{
		bOK = $ID('sslw_choose_content_new').checked ? true : false;
		if( bOK )
		{
			WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.NewDataFile;
			WCM.ContentWizard.NativeContentInfo = WCM.ContentWizard.GetDefaultDocumentInfo( "SSContributorDataFile" );
		}
	}

	if( !bOK )
	{
		if( $ID('sslw_choose_content_native').checked )
		{
			// Ensure an item is selected
			var selectList = $ID('sslw_native_content_select');
			if( selectList.selectedIndex >= 0 )
			{
				bOK = true;
				WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.NewNativeDoc;
				var doctypeinfo = selectList.options[selectList.selectedIndex].doctypeinfo;
				WCM.ContentWizard.NativeContentInfo = doctypeinfo;
			}
		}
	}

	if( !bOK )
	{
		bOK = $ID('sslw_choose_content_existing').checked ? true : false;
		if( bOK )
		{
			WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.ExistingDoc;
			
			if( forNext )
			{
				bOK = false;

				WCM.ContentWizard.ChooseContentPage.ChooseManagedDocument();
			}
		}
	}

	if( !bOK )
	{
		bOK = $ID('sslw_choose_content_local').checked ? true : false;
		if( bOK )
		{
			WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.LocalFile;
		}
	}

	if( !bOK )
	{
		if( $ID('sslw_choose_selected_content').checked )
		{
			if( WCM.ContentWizard.ChooseContentPage.selectionIsValid )
			{
				if( WCM.ContentWizard.SelectedDocName.length != 0 )
				{
					bOK = true;
					WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.SelectedDoc;
				}
			}
		}
	}

	if( !bOK )
	{
		bOK = $ID('sslw_choose_content_none').checked ? true : false;
		if( bOK )
		{
			WCM.ContentWizard.ContentSelectionMethod = ContentSelectionMethod.None;
		}
	}

	return bOK;
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.OnWizardBack = function()
{
	$D().log('WCM.ContentWizard.ChooseContentPage.OnWizardBack', window);
	
	return WCM.ContentWizard.AllowChoseRegionSchemaPage;
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.ClearPage = function()
{
	$D().log('SwitchRegionContent: ClearPage', window );

	WCM.DHTML.SetInnerHTML('wcm_choose_doc_content_id', '');
	WCM.DHTML.SetInnerHTML('wcm_choose_doc_title', '');
	WCM.DHTML.SetInnerHTML('wcm_choose_doc_type', '');
	WCM.DHTML.SetInnerHTML('wcm_choose_doc_author', '');
	WCM.DHTML.SetInnerHTML('wcm_choose_doc_comments', '');

	$ID('sslw_doc_info_link').href = '';
	$ID('sslw_doc_info_link').removeAttribute('href');
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.UpdatePageWithDocument = function( dDocName, dID, autoNextCallback, isInitializing )
{
	autoNextCallback = autoNextCallback || false;
	isInitializing = isInitializing || false;
	dID = dID || '';
	dDocName = dDocName || '';
	$D().log('WCM.ContentWizard.ChooseContentPage.UpdatePageWithDocument - ' + dDocName + ':' + dID, window );

	WCM.ContentWizard.ChooseContentPage.selectionIsValid = false;
	if( isInitializing )
	{
		WCM.DHTML.SetInnerHTML('sslw_doc_content_id', dDocName);
		if( dDocName.length > 0 )
		{
			WCM.ContentWizard.ChooseContentPage.selectionIsValid = true;
		}
	}
	else
	{
		WCM.ContentWizard.ChooseContentPage.ClearPage();
	}
	WCM.ContentWizard.ChooseContentPage.OnActivate();

	var jsonBinder = new WCM.Idc.JSONBinder();
	if( dID.length > 0 )
	{
		jsonBinder.SetLocalDataValue('IdcService', 'DOC_INFO');
		jsonBinder.SetLocalDataValue('dID', dID);
	}
	else
	{
		jsonBinder.SetLocalDataValue('IdcService', 'DOC_INFO_BY_NAME');
		jsonBinder.SetLocalDataValue('dDocName', dDocName);
	}
	
	$D().log('Preparing service call for WCM.ContentWizard.ChooseContentPage.UpdatePageWithDocument', window);

	jsonBinder.Send(WCM.ContentWizard.ServerCgiUrl, $CB(WCM.ContentWizard.ChooseContentPage.DocInfoCallback, autoNextCallback, isInitializing));
}

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.DocInfoCallback = function(http, autoNextCallback, isInitializing)
{
	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		var docInfoBinder = new WCM.Idc.JSONBinder(text);
		WCM.ContentWizard.ChooseContentPage.UpdatePageWithBinder( docInfoBinder, autoNextCallback, isInitializing );
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}
};

//***************************************************************************

WCM.ContentWizard.ChooseContentPage.UpdatePageWithBinder = function( binder, autoNextCallback, isInitializing )
{
	isInitializing = isInitializing || false;

	var resultSetName = 'DOC_INFO';
	if( binder && WCM.IsValid( binder ) && WCM.IsValid(binder.GetResultSet(resultSetName)) &&
		( WCM.ToInt(binder.GetResultSetRows(resultSetName).length) > 0 ) )
	{
		var value = '';
	
		var dDocName = binder.GetResultSetValue(resultSetName, 'dDocName', 0);
		var xWebsiteObjectType = binder.GetResultSetValue(resultSetName, 'xWebsiteObjectType', 0);
		var xRegionDefinition = binder.GetResultSetValue(resultSetName, 'xRegionDefinition', 0) || '';

		// Store the dDocName and Website Object Type value
		WCM.ContentWizard.SelectedDocName = dDocName || '';
		WCM.ContentWizard.SelectedWebsiteObjectType = xWebsiteObjectType || '';
		if( xRegionDefinition != WCM.ContentWizard.SelectedDocNameRegionDefinition )
		{
			WCM.ContentWizard.SelectedRegionTemplate = '';
			var page = GetWizardPageById( 'sslw_page_choose_region_template' );
			page.Initialized = false;

			var regionTemplates = WCM.ContentWizard.GetRegionTemplatesForRegionDefinition( xRegionDefinition );
			var iLen = WCM.ToInt(regionTemplates.length);
			if( !WCM.ContentWizard.CanSwitchTemplate )
			{
				iLen = 0;
			}
			WCM.ContentWizard.AllowChoseRegionTemplatePage = ( iLen > 0 );
			WCM.ContentWizard.AllowChoseRegionTemplatePage = false;
		}
		WCM.ContentWizard.SelectedDocNameRegionDefinition = xRegionDefinition;

		// Populate the wizard page fields with the document's metadata
		value = binder.GetResultSetValue(resultSetName, 'dDocName', 0);
			WCM.DHTML.SetInnerHTML('sslw_doc_content_id', value);
		value = binder.GetResultSetValue(resultSetName, 'dDocTitle', 0);
			WCM.DHTML.SetInnerHTML('sslw_doc_title', value);
		value = binder.GetResultSetValue(resultSetName, 'dDocType', 0);
			WCM.DHTML.SetInnerHTML('sslw_doc_type', value);
		value = binder.GetResultSetValue(resultSetName, 'dDocAuthor', 0);
			WCM.DHTML.SetInnerHTML('sslw_doc_author', value);
		value = binder.GetResultSetValue(resultSetName, 'xRegionDefinition', 0);
			WCM.DHTML.SetInnerHTML('sslw_doc_region_definition', value);
		value = binder.GetResultSetValue(resultSetName, 'xComments', 0);
			WCM.DHTML.SetInnerHTML('sslw_doc_comments', value);

		var href = WCM.ContentWizard.ServerCgiUrl;
		href += '?IdcService=DOC_INFO_BY_NAME';
		href += '&dDocName=' + dDocName;
		$ID('sslw_doc_info_link').href = href;
		
		WCM.ContentWizard.ChooseContentPage.selectionIsValid = true;

		if( !isInitializing ||
			(
				!($ID('sslw_choose_content_new').checked) &&
				!($ID('sslw_choose_content_native').checked) &&
				!($ID('sslw_choose_content_existing').checked) &&
				!($ID('sslw_choose_content_local').checked) &&
				!($ID('sslw_choose_content_none').checked)
			)
		)
		{
			$ID('sslw_choose_content_new').checked = false;
			$ID('sslw_choose_content_native').checked = false;
			$ID('sslw_choose_content_existing').checked = false;
			$ID('sslw_choose_content_local').checked = false;
			$ID('sslw_choose_content_none').checked = false;
			$ID('sslw_choose_selected_content').checked = true;		
		}

		WCM.DHTML.Show( $ID('sslw_choose_selected_content_row') );
		
		WCM.ContentWizard.ChooseContentPage.OnActivate();
		if( autoNextCallback )
		{
			autoNextCallback();
		}
	}
}

//***************************************************************************

WCM.ContentWizard.GetDefaultDocumentInfo = function( doctype )
{
	var doctypeinfo = null;

	var resultSetName = 'SSDefaultDocuments';
	var binder = WCM.ContentWizard.optionsBinder;
	if( binder && WCM.IsValid( binder ) && WCM.IsValid(binder.GetResultSet(resultSetName)) )
	{
		var iLen = WCM.ToInt(binder.GetResultSetRows(resultSetName).length);
		for( var i = 0; i < iLen; i++ )
		{
			var token = binder.GetResultSetValue(resultSetName, 'token', i);
			var description = binder.GetResultSetValue(resultSetName, 'description', i);
			var primaryFile = binder.GetResultSetValue(resultSetName, 'primaryFile', i);
			
			if( token == doctype )
			{
				doctypeinfo = new Object();
				doctypeinfo.token = token;
				doctypeinfo.description = description;
				doctypeinfo.primaryFile = primaryFile;
				break;
			}
		}
	}

	return doctypeinfo;
}


//***************************************************************************
//***************************************************************************
// CheckinContentPage
//***************************************************************************
//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnInit = function()
{
	$D().log('WCM.ContentWizard.CheckinContentPage.OnInit', window);
	
	WCM.ContentWizard.CheckinContentPage.SetPageTitle();
	
	var checkinFrame = $ID('sslw_checkin_content_frame');
	WCM.DHTML.SetStyle(checkinFrame, 'height', '1500px');
	WCM.DHTML.SetStyle(checkinFrame, 'width', '95%');
	WCM.DHTML.AddEvent(checkinFrame, 'load', WCM.ContentWizard.CheckinContentPage.OnFrameLoad);	
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.CheckFrameLoaded = function()
{
	var func = null;
	try
	{
		var checkinFrame = $ID('sslw_checkin_content_frame');
		func = checkinFrame.contentWindow.wcmIsDocumentLoaded;
	}
	catch(e)
	{
	}

	var isLoaded = false;	
	if (WCM.IsFunction(func))
	{
		try
		{
			isLoaded = func.apply();
		}
		catch(e2)
		{
		}
	}

	if( isLoaded )
	{
		WCM.ContentWizard.CheckinContentPage.OnFrameLoad( true );
	}
	
	return isLoaded;
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnFrameLoad = function( isInitialResize )
{
	var checkinFrame = $ID('sslw_checkin_content_frame');
	var frameDoc = checkinFrame.contentDocument || checkinFrame.contentWindow.document || null;
	if (frameDoc)
	{
		var formTableHeight = WCM.DHTML.GetFullHeight($ID('checkin-form-display', frameDoc));

		var isIE = false;
		if( isInitialResize )
		{
			var index = -1;
			var msie = 'MSIE ';
			var ua = navigator.userAgent;

			if ((index = ua.indexOf(msie)) > 0 && WCM.ToInt(ua.substring((index = (index + msie.length)), index+1)) < 7)
			{
				isIE = true;
			}
			
			if( isIE )
			{
				WCM.ContentWizard.ResizeCheckinIframe('93%');
			}
		}

		if (formTableHeight)
		{
			WCM.DHTML.SetStyle(checkinFrame, 'height', formTableHeight + 'px');
		}

		if( isInitialResize && isIE )
		{
			window.setTimeoutEx($CB(WCM.ContentWizard.ResizeCheckinIframe, '95%'), 500);
		}
	}
	
	WCM.DHTML.RemoveEvent(checkinFrame, 'load', WCM.ContentWizard.CheckinContentPage.OnFrameLoad);	
}

//**********************************************************************

WCM.ContentWizard.ResizeCheckinIframe = function(vlu)
{
	WCM.DHTML.SetStyle($ID('sslw_checkin_content_frame'), 'width', vlu);
};

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.SetPageTitle = function()
{
	if( WCM.ContentWizard.IsCreateNew )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmCheckinContentFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmCreateNewCheckinFile'));
	}
	else if( WCM.ContentWizard.IsSwitchContent )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmCheckinContentFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchCheckinFile'));
	}
	else if( WCM.ContentWizard.IsSwitchPlaceholderDefinition )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmCheckinPlaceholderDefinitionFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchCheckinPlaceholderDefinition'));
	}
	else if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmCheckinViewFile'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchCheckinView'));
	}
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnActivate = function()
{
	$D().log('WCM.ContentWizard.CheckinContentPage.OnActivate', window);

	WCM.ContentWizard.CheckinContentPage.SetPageTitle();
	
	EnableButton( 'sslw_button_back', true );

	var Settings = WCM.ContentWizard.config || {};
	if( WCM.ContentWizard.IsSwitchContent || WCM.ContentWizard.IsSwitchPlaceholderDefinition || WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		Settings = Settings.switchregioncontent || {};
	}
	
	var options = new Object();
	options.httpCgiPath = WCM.ToString(WCM.ContentWizard.GetExternal().GetOptions().httpCgiPath);
	options.callback = $CB(WCM.ContentWizard.CheckinContentPage.OnCheckinComplete);

	WCM.PosterOptions = null;
	var formValues = null;
	
	var isDataFile = false;
	var doctypeinfo = WCM.ContentWizard.NativeContentInfo;
	switch( WCM.ContentWizard.ContentSelectionMethod )
	{
		case 'NewDataFile':
			isDataFile = true;
			// fall through
		case 'NewNativeDoc':
		{
			formValues = new Array();
			formValues.AddNameValuePair( 'IdcService', 'CHECKIN_NEW_FORM' );
			formValues.AddNameValuePair( 'ssUseSSCheckin', '1' );
			formValues.AddNameValuePair( 'ssDialogContext', '1' );
			formValues.AddNameValuePair( 'coreContentOnly', '1' );
			formValues.AddNameValuePair( 'AllowPrimaryMetaFile', '0' );
			formValues.AddNameValuePair( 'suppressAlternateFile', '1' );
			formValues.AddNameValuePair( 'ssDefaultDocumentToken', doctypeinfo.token );
			formValues.AddNameValuePair( 'primaryFile', doctypeinfo.primaryFile );


			

			
			// Turn off the upload applet
			formValues.AddNameValuePair( 'UploadApplet', '0' );
			formValues.AddNameValuePair( 'MultiUpload', '0' );

			if( isDataFile )
			{
				formValues.AddNameValuePair( 'xWebsiteObjectType', 'Data File' );
				formValues.AddNameValuePair( 'xWebsiteObjectType:isInfoOnly', '1' );
			}
			else
			{
				formValues.AddNameValuePair( 'xWebsiteObjectType', 'Native Document' );
			}

			var defaults = WCM.ToString(Settings["defaultmetadata"], '');
			var temp = defaults.replace( /\+/g, '%20' ); // Content Server URL encodes spaces with '+'.  Change them to '%20'
			defaults = decodeURIComponent( temp ).Trim();
			if( defaults.length > 0 )
			{
				var pairs = defaults.split("&");
				for (var i = 0; i < pairs.length; i++)
				{
					var p = pairs[i].split("=");
					
					formValues.AddNameValuePair( p[0], p[1] );
				}
			}

			// Set the title, if it does not have a default value
			if( formValues.Find( 'dDocTitle' ) < 0 )
			{
				formValues.AddNameValuePair( 'dDocTitle', doctypeinfo.description );
			}

			if( ( formValues.Find( 'xWebsiteSection' ) < 0 ) && ( WCM.ContentWizard.SourceSiteId.length != 0 ) && ( WCM.ContentWizard.SourceNodeId.length != 0 ) )
			{
				formValues.AddNameValuePair( 'xWebsiteSection', WCM.ContentWizard.SourceSiteId + ':' + WCM.ContentWizard.SourceNodeId );
			}

			if( formValues.Find( 'xRegionDefinition' ) < 0 )
			{
				formValues.AddNameValuePair( 'xRegionDefinition', WCM.ContentWizard.SelectedRegionDefinition );
			}

			// CUSTOMISE start
			if( formValues.Find( 'xLocale' ) < 0 )			{
				formValues.AddNameValuePair( 'xLocale', WCM.ContentWizard.SiteLocale );
			}
			//formValues.AddNameValuePair( 'ForceUserLocale', WCM.ContentWizard.SiteLocale );
			formValues.AddNameValuePair( 'LanguageCode', WCM.ContentWizard.LanguageCode );
			formValues.AddNameValuePair( 'CountryCode', WCM.ContentWizard.CountryCode );
			// CUSTOMISE end


		}
		break;

		case 'LocalFile':
		{
			formValues = new Array();
			formValues.AddNameValuePair( 'IdcService', 'CHECKIN_NEW_FORM' );
			formValues.AddNameValuePair( 'ssUseSSCheckin', '1' );
			formValues.AddNameValuePair( 'ssDialogContext', '1' );
			formValues.AddNameValuePair( 'coreContentOnly', '1' );

			// Turn off the upload applet
			formValues.AddNameValuePair( 'UploadApplet', '0' );
			formValues.AddNameValuePair( 'MultiUpload', '0' );
			
			var defaults = WCM.ToString(Settings["defaultmetadata"], '');
			var temp = defaults.replace( /\+/g, '%20' ); // Content Server URL encodes spaces with '+'.  Change them to '%20'
			defaults = decodeURIComponent( temp ).Trim();
			if( defaults.length > 0 )
			{
				var pairs = defaults.split("&");
				for (var i = 0; i < pairs.length; i++)
				{
					var p = pairs[i].split("=");
					
					formValues.AddNameValuePair( p[0], p[1] );
				}
			}

			if( ( formValues.Find( 'xWebsiteSection' ) < 0 ) && ( WCM.ContentWizard.SourceSiteId.length != 0 ) && ( WCM.ContentWizard.SourceNodeId.length != 0 ) )
			{
				formValues.AddNameValuePair( 'xWebsiteSection', WCM.ContentWizard.SourceSiteId + ':' + WCM.ContentWizard.SourceNodeId );
			}

			if( formValues.Find( 'xRegionDefinition' ) < 0 )
			{
				formValues.AddNameValuePair( 'xRegionDefinition', WCM.ContentWizard.SelectedRegionDefinition );
			}
		}
		break;
	}

	if( WCM.IsValid(formValues) && ( formValues.length > 0 ) )
	{
		formValues.AddNameValuePair( 'ssShowSubmitExtraActionsOnly', 'true' ); // Only show std_checkin_submit_extra_actions
		
		WCM.PosterOptions = {};
		WCM.PosterOptions.httpCgiPath = WCM.ToString(WCM.ContentWizard.ServerCgiUrl);
		WCM.PosterOptions.formValues = formValues;

		WCM.DHTML.SetAttribute( $ID('sslw_checkin_content_frame'), 'src', WCM.path + './base/wcm.poster.htm' );

		options.resizeAfterLoad = true;
		if( options.callback )
		{
			window.setTimeoutEx($CB(CheckCheckinPopupUrl, options), 500);
		}

		EnableButton( 'sslw_button_next', true );
		WCM.ContentWizard.CheckinContentPage.UpdateFinishButton();
	}
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.UpdateFinishButton = function()
{
	$D().log('WCM.ContentWizard.CheckinContentPage.UpdateFinishButton', window);
	
	EnableButton( 'sslw_button_finish', true );
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnWizardNext = function()
{
	$D().log('WCM.ContentWizard.CheckinContentPage.OnWizardNext', window);

	var bContinue = false;
	if( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.SelectedDoc )
	{
		WCM.DHTML.SetAttribute( $ID('sslw_checkin_content_frame'), 'src', WCM.ContentWizard.pathToSpace );
		bContinue = true;
	}
	else
	{
		try
		{
			WCM.ContentWizard.CheckinContentPage.checkinCompleteCallback = $CB(WCM.ContentWizard.OnWizardNext);

			// Manually try to submit the form
			var frameWindow = frames['sslw_checkin_content_frame'];
			var frameDoc = frameWindow.document;	
			frameWindow.postCheckInStandard(frameDoc.forms['Checkin'])
		}
		catch(e)
		{
			$D().error('Unable to perform submit action on the checkin content form.', window);
		}
	}	
	
	return bContinue;
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnWizardFinish = function( callback )
{
	$D().log('WCM.ContentWizard.CheckinContentPage.OnWizardFinish', window);

	try
	{
		WCM.ContentWizard.CheckinContentPage.checkinCompleteCallback = $CB(WCM.ContentWizard.CheckinContentPage.OnWizardFinishAfterCheckin, callback);

		// Manually try to submit the form
		var frameWindow = frames['sslw_checkin_content_frame'];
		var frameDoc = frameWindow.document;	
		frameWindow.postCheckInStandard(frameDoc.forms['Checkin'])
	}
	catch(e)
	{
		$D().error('Unable to perform submit action on the checkin content form.', window);
	}
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnWizardFinishAfterCheckin = function( callback )
{
	$D().log('WCM.ContentWizard.CheckinContentPage.OnWizardFinishAfterCheckin', window);

	try
	{
		callback();
	}
	catch(e)
	{
		$D().error('Unable to compute link after checkin..', window);
	}
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnWizardBack = function()
{
	$D().log('WCM.ContentWizard.CheckinContentPage.OnWizardBack', window);

	WCM.DHTML.SetAttribute( $ID('sslw_checkin_content_frame'), 'src', WCM.ContentWizard.pathToSpace );

	return true;
}

//***************************************************************************

WCM.ContentWizard.CheckinContentPage.OnCheckinComplete = function(returnParams)
{
	var callback = WCM.ContentWizard.CheckinContentPage.checkinCompleteCallback;
	WCM.ContentWizard.ChooseContentPage.OnSelectionComplete(returnParams, callback);
}

//***************************************************************************
//***************************************************************************
// ChooseRegionTemplatePage
//***************************************************************************
//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.OnInit = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionTemplatePage.OnInit', window);
	
	WCM.ContentWizard.ChooseRegionTemplatePage.SetPageTitle();
	
	EnableButton( 'sslw_button_back', !WCM.ContentWizard.IsSwitchRegionTemplate );

	var Settings = WCM.ContentWizard.config || {};
	if( WCM.ContentWizard.IsSwitchContent || WCM.ContentWizard.IsSwitchPlaceholderDefinition || WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		Settings = Settings.switchregioncontent || {};
	}

	if( WCM.ContentWizard.UseAutoAdvance )
	{
		EnableButton( 'sslw_region_template_select', false );
	}

	WCM.DHTML.AddEvent('sslw_choose_region_template_use_default', 'click', WCM.ContentWizard.ChooseRegionTemplatePage.OnUpdateChooseRegionTemplatePage);
	WCM.DHTML.AddEvent('sslw_choose_region_template_use_this',    'click', WCM.ContentWizard.ChooseRegionTemplatePage.OnUpdateChooseRegionTemplatePage);
	if( WCM.ContentWizard.UseAutoAdvance )
	{
		WCM.DHTML.AddEvent('sslw_region_template_select',             'change', WCM.ContentWizard.SelectListAutoAdvance );
	}

	$ID('sslw_choose_region_template_use_default').checked = false;
	$ID('sslw_choose_region_template_use_this').checked = false;

	var regionDefinition = WCM.ContentWizard.SelectedDocNameRegionDefinition;
	var regionTemplates = WCM.ContentWizard.GetRegionTemplatesForRegionDefinition( regionDefinition );

	var selectList = $ID('sslw_region_template_select');
	
	// Remove any existing items
	while( selectList.options.length > 0 )
	{
		selectList.options[0] = null;
	}
	
	var selectDefault = true;
	var mappedTemplateDocName = WCM.ContentWizard.GetUrlMapping( "TemplateUrl" );
	
	// Add region templates to the list		
	var iLen = WCM.ToInt(regionTemplates.length);
	for( var i = 0; i < iLen; i++ )
	{
		var regionTemplate = regionTemplates[i];
		var regionTemplateLabel = WCM.ContentWizard.getContentItemLabel(regionTemplate);

		var newOption = new Option( regionTemplateLabel );
		newOption.regionTemplate = regionTemplate;
		
		if( regionTemplate.toLowerCase() == mappedTemplateDocName.toLowerCase() )
		{
			newOption.selected = true;
			selectDefault = false;
		}

		selectList.options[selectList.options.length] = newOption
	}

	if( selectDefault )
	{
		$ID('sslw_choose_region_template_use_default').checked = true;
		
		if( iLen == 0 )
		{
			var newOption = new Option( WCM.GetString('wcmNoRegionTemplatesAvailable') );
			selectList.options[selectList.options.length] = newOption
			$ID('sslw_choose_region_template_use_this').disabled = true;
			$ID('sslw_region_template_select').disabled = true;
		}
	}
	else
	{
		$ID('sslw_choose_region_template_use_this').checked = true;
		$ID('sslw_region_template_select').disabled = false;
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.SetPageTitle = function()
{
	WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmChooseRegionTemplateFile'));
	WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchRegionTemplateFile'));
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.OnUpdateChooseRegionTemplatePage = function()
{
	WCM.ContentWizard.ChooseRegionTemplatePage.OnActivate();

	var bWasListEnabled = IsButtonEnabled( 'sslw_region_template_select' );
	$ID('sslw_region_template_select').disabled = $ID('sslw_choose_region_template_use_this').checked ? false : true;

	// Auto Advance
	if( WCM.ContentWizard.UseAutoAdvance && IsButtonEnabled( 'sslw_button_next' ) )
	{
		if( $ID('sslw_choose_region_template_use_this').checked && !bWasListEnabled )
		{
		}
		else
		{
			WCM.ContentWizard.OnWizardNext();
		}
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.UpdateFinishButton = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionTemplatePage.UpdateFinishButton', window);
	
	var bCanFinish = WCM.ContentWizard.ChooseRegionTemplatePage.CanFinish();
	EnableButton( 'sslw_button_finish', bCanFinish );
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.CanFinish = function()
{
	var bCanFinish = WCM.ContentWizard.ChooseRegionTemplatePage.GetSelectedOption(false);
	return bCanFinish;
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.OnActivate = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionTemplatePage.OnActivate', window);
	
	WCM.ContentWizard.ChooseRegionTemplatePage.SetPageTitle();

	EnableButton( 'sslw_button_back', !WCM.ContentWizard.IsSwitchRegionTemplate );

	var bEnable = WCM.ContentWizard.ChooseRegionTemplatePage.GetSelectedOption(false);
	EnableButton( 'sslw_button_next', bEnable );
	
	WCM.ContentWizard.ChooseRegionTemplatePage.UpdateFinishButton();
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.OnWizardNext = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionTemplatePage.OnWizardNext', window);

	var bOK = WCM.ContentWizard.ChooseRegionTemplatePage.GetSelectedOption(true);
	return bOK;
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.OnWizardFinish = function( callback )
{
	$D().log('WCM.ContentWizard.ChooseRegionTemplatePage.OnWizardFinish', window);

	var bOK = WCM.ContentWizard.ChooseRegionTemplatePage.GetSelectedOption(true);
	if( bOK )
	{
		callback();
	}
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.GetSelectedOption = function( forNext )
{
	$D().log('WCM.ContentWizard.ChooseRegionTemplatePage.OnWizardNext', window);

	var bOK = false;
	
	if( $ID('sslw_choose_region_template_use_default').checked )
	{
		bOK = true;
		WCM.ContentWizard.SelectedRegionTemplate = '';
	}
	else if( $ID('sslw_choose_region_template_use_this').checked )
	{
		// Ensure an item is selected
		var selectList = $ID('sslw_region_template_select');
		if( selectList.selectedIndex >= 0 )
		{
			bOK = true;
			var regionTemplate = selectList.options[selectList.selectedIndex].regionTemplate;
			WCM.ContentWizard.SelectedRegionTemplate = regionTemplate;
		}
	}

	return bOK;
}

//***************************************************************************

WCM.ContentWizard.ChooseRegionTemplatePage.OnWizardBack = function()
{
	$D().log('WCM.ContentWizard.ChooseRegionTemplatePage.OnWizardBack', window);
	
	return true;
}

//***************************************************************************

WCM.ContentWizard.GetRegionTemplatesForRegionDefinition = function( regionDefinition )
{
	regionDefinition = regionDefinition || '';
	
	var regionTemplates = [];

	if( WCM.ContentWizard.wizardConfig )
	{
		if( WCM.ContentWizard.wizardConfig['regionTemplates'] )
		{
			regionTemplates = WCM.ContentWizard.wizardConfig['regionTemplates'];
		}
		else if( WCM.ContentWizard.wizardConfig['regionMappings'] )
		{
			var regionMappings = WCM.ContentWizard.wizardConfig['regionMappings'];

			if( regionMappings[regionDefinition.toLowerCase()] )
			{
				regionTemplates = regionMappings[regionDefinition.toLowerCase()];
			}
		}
	}

	return regionTemplates;
}

//***************************************************************************

WCM.ContentWizard.GetUrlMapping = function( urlNameSuffix )
{
	var urlName = 'primary' + urlNameSuffix;
	if( WCM.ContentWizard.isSecondaryPage )
	{
		urlName = 'secondary' + urlNameSuffix;
	}

	var mappingDocNameValue = '';
	if( WCM.ContentWizard.wizardConfig && WCM.ContentWizard.wizardConfig['nodeUrlMappings'] )
	{
		var nodeUrlMappings = WCM.ContentWizard.wizardConfig['nodeUrlMappings'];
		if( nodeUrlMappings[ urlName ] )
		{
			var urlMappings = nodeUrlMappings[ urlName ];
			if( urlMappings[WCM.ContentWizard.regionName] )
			{
				mappingDocNameValue = WCM.ToString( urlMappings[WCM.ContentWizard.regionName] );
			}
		}
	}
	
	return mappingDocNameValue;
}

//***************************************************************************

WCM.ContentWizard.getContentItemLabel = function( dDocName )
{
	var label = dDocName;
	
	if( WCM.IsValid( WCM.ContentWizard.ContentItemLabels[dDocName] ) )
	{
		label = WCM.ToString( WCM.ContentWizard.ContentItemLabels[dDocName] );
	}
	
	return label;
}

//***************************************************************************
//***************************************************************************
//***************************************************************************
// ConfirmPage
//***************************************************************************
//***************************************************************************

WCM.ContentWizard.ConfirmPage.OnInit = function()
{
	$D().log('WCM.ContentWizard.ConfirmPage.OnInit', window);

	if( WCM.ContentWizard.CanEditTarget )
	{
		// Allow the editing choice
		WCM.DHTML.Show( $ID('sslw_page_confirm_options') );
		WCM.DHTML.Show( $ID('sslw_confirm_action_edit_row') );
		WCM.DHTML.Show( $ID('sslw_confirm_action_exit_row') );
	}
	$ID('sslw_confirm_action_exit').checked = true;

	WCM.DHTML.AddEvent('sslw_confirm_action_edit_auto_advance', 'click', $CB(WCM.ContentWizard.ConfirmPage.ConfirmPageAutoAdvance, 'sslw_confirm_action_edit'));
	WCM.DHTML.AddEvent('sslw_confirm_action_exit_auto_advance', 'click', $CB(WCM.ContentWizard.ConfirmPage.ConfirmPageAutoAdvance, 'sslw_confirm_action_exit'));

	WCM.DHTML.AddEvent('sslw_confirm_action_edit', 'click', WCM.ContentWizard.ConfirmPage.UpdateFinishButton);
	WCM.DHTML.AddEvent('sslw_confirm_action_exit', 'click', WCM.ContentWizard.ConfirmPage.UpdateFinishButton);
}

//***************************************************************************

WCM.ContentWizard.ConfirmPage.ConfirmPageAutoAdvance = function(inputId)
{
	if (WCM.IsValid(inputId))
	{
		WCM.ContentWizard.UseAutoAdvance = true;
		$ID(inputId).checked = true;
		WCM.ContentWizard.ConfirmPage.UpdateFinishButton();
		WCM.ContentWizard.UseAutoAdvance = false;
		WCM.ContentWizard.OnWizardFinish();
	}
}

//***************************************************************************

WCM.ContentWizard.ConfirmPage.SetPageTitle = function()
{
	if( WCM.ContentWizard.IsCreateNew )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmCreateNewConfirmTitle'));
		WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmCreateNewConfirmText', WCM.ContentWizard.SelectedDocName));
	}
	else if( WCM.ContentWizard.IsSwitchContent )
	{
		if( WCM.ContentWizard.SelectedSubTemplate.length > 0 )
		{
			WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmSwitchConfirmSubTemplateTitle'));
			WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmSubTemplateText', WCM.ContentWizard.SelectedSubTemplate));
		}
		else
		{
			WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmSwitchConfirmTitle'));
			if( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.None )
			{
				if( WCM.ContentWizard.OriginalDocName.length > 0 )
				{
					WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmRemove', WCM.ContentWizard.OriginalDocName));
				}
				else
				{
					WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmNone'));
				}
			}
			else
			{
				WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmText', WCM.ContentWizard.SelectedDocName));
			}
		}
	}
	else if( WCM.ContentWizard.IsSwitchPlaceholderDefinition )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmSwitchConfirmPlaceholderDefinitionTitle'));
		if( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.None )
		{
			if( WCM.ContentWizard.OriginalDocName.length > 0 )
			{
				WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmPlaceholderDefinitionRemove', WCM.ContentWizard.OriginalDocName));
			}
			else
			{
				WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmPlaceholderDefinitionNone'));
			}
		}
		else
		{
			WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmPlaceholderDefinitionText', WCM.ContentWizard.SelectedDocName));
		}
	}
	else if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmSwitchConfirmViewTitle'));
		if( WCM.ContentWizard.SelectedRegionTemplate.length > 0 )
		{
			WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmViewText', WCM.ContentWizard.SelectedRegionTemplate));
		}
		else
		{
			WCM.DHTML.SetInnerHTML($ID('instruction_text'), WCM.GetString('wcmSwitchConfirmViewDefault'));
		}
	}
}

//*************************************************************************

WCM.ContentWizard.ConfirmPage.UpdateFinishButton = function()
{
	var bEnable = WCM.ContentWizard.ConfirmPage.AllowNextConfirmPage();
	EnableButton( 'sslw_button_finish', bEnable );
}

//***************************************************************************

WCM.ContentWizard.ConfirmPage.AllowNextConfirmPage = function()
{
	var bContinue = false;
	
	if( $ID('sslw_confirm_action_edit').checked )
	{
		if( WCM.ContentWizard.SelectedDocName.length > 0 )
		{
			bContinue = true;
		}
	}
	else if( $ID('sslw_confirm_action_exit').checked )
	{
		bContinue = true;
	}

	return bContinue;
}

//***************************************************************************

WCM.ContentWizard.ConfirmPage.OnActivate = function()
{
	$D().log('WCM.ContentWizard.ConfirmPage.OnActivate', window);
	
	WCM.ContentWizard.ConfirmPage.SetPageTitle();	

	EnableButton( 'sslw_button_back', true );
	EnableButton( 'sslw_button_cancel', true );
	
	ShowFinishButton( true );
	WCM.ContentWizard.ConfirmPage.UpdateFinishButton();
}

//***************************************************************************

WCM.ContentWizard.ConfirmPage.OnWizardNext = function()
{
	$D().log('WCM.ContentWizard.ConfirmPage.OnWizardNext', window);

	WCM.ContentWizard.EditTargetUponExit = $ID('sslw_confirm_action_edit').checked ? true : false;
	
	var bContinue = WCM.ContentWizard.ConfirmPage.AllowNextConfirmPage();
	return bContinue;
}

//***************************************************************************

WCM.ContentWizard.ConfirmPage.OnWizardFinish = function( callback )
{
	$D().log('WCM.ContentWizard.ConfirmPage.OnWizardFinish', window);

	WCM.ContentWizard.EditTargetUponExit = $ID('sslw_confirm_action_edit').checked ? true : false;
	
	var bContinue = WCM.ContentWizard.ConfirmPage.AllowNextConfirmPage();
	if( bContinue )
	{
		callback();
	}
}

//***************************************************************************

WCM.ContentWizard.ConfirmPage.OnWizardBack = function()
{
	$D().log('WCM.ContentWizard.ConfirmPage.OnWizardBack', window);

	ShowFinishButton( false );
	return true;
}


//***************************************************************************
//***************************************************************************
// Miscellaneous
//***************************************************************************
//***************************************************************************

WCM.ContentWizard.ResizeHandler = function()
{
	var checkinDisplay = WCM.DHTML.GetStyle('sslw_page_checkin_content', 'display');
	if (checkinDisplay != 'none')
	{
		WCM.ContentWizard.CheckinContentPage.OnFrameLoad();
	}
	
	WCM.DHTML.FlexToFillViewport("contents");
};

//***************************************************************************

WCM.ContentWizard.AdjustIframesWidth = function(w)
{
	for (var frms = $GET('tag:iframe'); frms.next();)
	{
		WCM.DHTML.SetStyle(frms.at(), 'width', w);
	}
};

//***************************************************************************

WCM.ContentWizard.FixUpIeIframes = function()
{
	if( !WCM.ContentWizard.FrameSized )
	{
		WCM.ContentWizard.FrameSized = true;

		var index = -1;
		var msie = 'MSIE ';
		var ua = navigator.userAgent;

		if ((index = ua.indexOf(msie)) > 0 && WCM.ToInt(ua.substring((index = (index + msie.length)), index+1)) < 7)
		{
			WCM.ContentWizard.AdjustIframesWidth('97%');
			window.setTimeoutEx($CB(WCM.ContentWizard.AdjustIframesWidth, '99%'), 500);
		}
	}
};

//***************************************************************************
//***************************************************************************
// Finish Handlers
//***************************************************************************
//***************************************************************************

WCM.ContentWizard.GetLinkAndExit = function()
{
	$D().log('ContentWizard: CheckDocumentReleaseAndExit', window);
	
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_GET_LINK');
	jsonBinder.SetLocalDataValue('sourceSiteId', WCM.ContentWizard.SourceSiteId);
	jsonBinder.SetLocalDataValue('sourceNodeId', WCM.ContentWizard.SourceNodeId); 
	jsonBinder.SetLocalDataValue('ssDocName', WCM.ContentWizard.SelectedDocName);
	jsonBinder.SetLocalDataValue('SSContributor', '1');
	
	$D().log('Preparing service call for WCM.ContentWizard.GetLinkAndExit', window);

	jsonBinder.Send(WCM.ContentWizard.ServerCgiUrl, WCM.ContentWizard.GetLinkAndExitCallback);
}

//*****************************************************************************

WCM.ContentWizard.GetLinkAndExitCallback = function(http)
{
	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		var binder = new WCM.Idc.JSONBinder(text);

		if( WCM.ContentWizard.GetExternal() )
		{
			var ssUrl = WCM.ToString( binder.GetLocalDataValue('ssLink'), '' );
			if( ssUrl && ( ssUrl.length > 0 ) )
			{
				// Close window.open type window
				var params = {};
				params["dDocName"] = WCM.ContentWizard.SelectedDocName
				params["EditTarget"] = true;
				params["ssUrl"] = ssUrl;
				params["xWebsiteObjectType"] = WCM.ContentWizard.SelectedWebsiteObjectType;

				$D().log('WCM.ContentWizard.OnWizardFinish returning: ' + $J(params, true), window);

				window.setTimeoutEx($CB(WCM.ContentWizard.CloseWizard, params), 500);
			}
			else
			{
				alert(WCM.GetString('wcmCouldNotComputeLinkToNewContentItem'));

				var params = {};
				params["dDocName"] = WCM.ContentWizard.SelectedDocName
				params["EditTarget"] = false;

				$D().log($CB(WCM.ContentWizard.OnWizardFinishLogParams, params), window);

				window.setTimeoutEx($CB(WCM.ContentWizard.CloseWizard, params), 500);
			}
		}
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}
}

//*****************************************************************************

WCM.ContentWizard.OnWizardFinishLogParams = function(params)
{
	return 'WCM.ContentWizard.OnWizardFinish returning: ' + $J(params, true);
}

//*****************************************************************************

WCM.ContentWizard.SwitchAssociationAndExit = function()
{
	$D().log('WCM.ContentWizard.SwitchAssociationAndExit', window );
	
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_SWITCH_REGION_ASSOCIATION');

	var dDocName = WCM.ContentWizard.SelectedDocName;
	if( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.None )
	{
		dDocName = '';
	}

	if( WCM.ContentWizard.IsSwitchContent )
	{
		var subTemplateDocName = WCM.ContentWizard.SelectedRegionTemplate;
		if( WCM.ContentWizard.SelectedSubTemplate.length > 0 )
		{
			subTemplateDocName = WCM.ContentWizard.SelectedSubTemplate;
			dDocName = '';
		}

		jsonBinder.SetLocalDataValue( (WCM.ContentWizard.isSecondaryPage ? 'secondaryTemplateUrl' : 'primaryTemplateUrl'), subTemplateDocName );
		jsonBinder.SetLocalDataValue( (WCM.ContentWizard.isSecondaryPage ? 'secondaryUrl' : 'primaryUrl' ), dDocName );

		if( window.top && window.top.opener && window.top.opener.top &&
			window.top.opener.top.WCM && 
			window.top.opener.top.WCM.AssignContent &&
			window.top.opener.top.WCM.AssignContent.ApplyContentToNode )
		{
			window.top.opener.top.WCM.AssignContent.ApplyContentToNode(
						WCM.ContentWizard.SourceNodeId,
						WCM.ContentWizard.regionName,
						dDocName,
						subTemplateDocName,
						!WCM.ContentWizard.isSecondaryPage,
						$CB( WCM.ContentWizard.CloseWizard, { "dDocName": WCM.ContentWizard.SelectedDocName, "switched": true } )
			);
			return;
		}
	}
	else if( WCM.ContentWizard.IsSwitchPlaceholderDefinition )
	{
		jsonBinder.SetLocalDataValue( ( WCM.ContentWizard.isSecondaryPage ? 'secondaryPlaceholderDefinitionUrl' : 'primaryPlaceholderDefinitionUrl' ), dDocName );
	}
	else if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		jsonBinder.SetLocalDataValue( ( WCM.ContentWizard.isSecondaryPage ? 'secondaryTemplateUrl' : 'primaryTemplateUrl' ), WCM.ContentWizard.SelectedRegionTemplate );
	}
	
	jsonBinder.SetLocalDataValue('siteId', WCM.ContentWizard.SourceSiteId);
	jsonBinder.SetLocalDataValue('nodeId', WCM.ContentWizard.SourceNodeId);
	jsonBinder.SetLocalDataValue('region', WCM.ContentWizard.regionName);

	$D().log('Preparing service call for WCM.ContentWizard.SwitchAssociationAndExit', window);

	jsonBinder.Send(WCM.ContentWizard.ServerCgiUrl, WCM.ContentWizard.SwitchAssociationAndExitCallback);
}

//*****************************************************************************

WCM.ContentWizard.SwitchAssociationAndExitCallback = function(http)
{
	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		if( WCM.ContentWizard.GetExternal() )
		{
			// Close window.open type window
			window.setTimeoutEx($CB(WCM.ContentWizard.CloseWizard, { "dDocName": WCM.ContentWizard.SelectedDocName, "switched": true }), 500);
		}
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}
};

//*****************************************************************************

WCM.ContentWizard.GetSelectedDocName = function()
{
	$D().log(function()
	{
		return 'WCM.ContentWizard.GetSelectedDocName';
	}, window );
	
	var dDocName = WCM.ContentWizard.SelectedDocName;
	if( WCM.ContentWizard.ContentSelectionMethod == ContentSelectionMethod.None )
	{
		dDocName = '';
	}

	if( WCM.ContentWizard.IsSwitchContent )
	{
		if( WCM.ContentWizard.SelectedSubTemplate.length > 0 )
		{
			dDocName = WCM.ContentWizard.SelectedSubTemplate;
		}
	}
	else if( WCM.ContentWizard.IsSwitchPlaceholderDefinition )
	{
		dDocName = WCM.ContentWizard.SelectedDocName;
	}
	else if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		dDocName = WCM.ContentWizard.SelectedRegionTemplate;
	}
	
	return dDocName;
}

//*****************************************************************************

WCM.ContentWizard.LocalizeUI = function()
{
	if( WCM.ContentWizard.IsCreateNew )
	{
		document.title = WCM.GetString('wcmCreateNewTitle');
		WCM.DHTML.SetInnerHTML($ID('sslw_page_title'), WCM.GetString('wcmCreateNewBannerTitle'));
	}
	else if( WCM.ContentWizard.IsSwitchContent )
	{
		document.title = WCM.GetString('wcmSwitchContentTitle');
		WCM.DHTML.SetInnerHTML($ID('sslw_page_title'), WCM.GetString('wcmSwitchContentBannerTitle'));
	}
	else if( WCM.ContentWizard.IsSwitchPlaceholderDefinition )
	{
		document.title = WCM.GetString('wcmSwitchPlaceholderDefinitionTitle');
		WCM.DHTML.SetInnerHTML($ID('sslw_page_title'), WCM.GetString('wcmSwitchPlaceholderDefinitionBannerTitle'));
	}
	else if( WCM.ContentWizard.IsSwitchRegionTemplate )
	{
		document.title = WCM.GetString('wcmSwitchViewTitle');
		WCM.DHTML.SetInnerHTML($ID('sslw_page_title'), WCM.GetString('wcmSwitchViewBannerTitle'));
	}
	$ID('sslw_banner_logo').alt = WCM.GetString('wcmOracleImageAlt');

	WCM.DHTML.SetInnerHTML($ID('sslw_button_cancel'), WCM.GetString('wcmCancel'));
	WCM.DHTML.SetInnerHTML($ID('sslw_button_back'), WCM.GetString('wcmBack'));
	WCM.DHTML.SetInnerHTML($ID('sslw_button_next'), WCM.GetString('wcmNext'));
	WCM.DHTML.SetInnerHTML($ID('sslw_button_finish'), WCM.GetString('wcmFinish'));
	
	WCM.DHTML.SetInnerHTML($ID('sslw_banner_product_name'), WCM.GetString('wcmSiteStudio'));
	WCM.DHTML.SetInnerHTML($ID('wcm_help'), WCM.GetString('wcmHelpLabel'));
	
	WCM.DHTML.SetInnerHTML($ID('sslw_region_schema_radio_label'), WCM.GetString('wcmChooseRegionDefinition'));
	WCM.DHTML.SetInnerHTML($ID('sslw_subtemplate_radio_label'), WCM.GetString('wcmChooseSubTemplate'));
	WCM.DHTML.SetInnerHTML($ID('dialog_page_title'), WCM.GetString('wcmChooseContentFile'));
	WCM.DHTML.SetInnerHTML($ID('sslw_choose_content_new_label'), WCM.GetString('wcmNewDataFile'));
	WCM.DHTML.SetInnerHTML($ID('sslw_choose_content_native_label'), WCM.GetString('wcmNewNativeDoc'));
	WCM.DHTML.SetInnerHTML($ID('sslw_choose_content_existing_label'), WCM.GetString('wcmExistingServerFile'));
	WCM.DHTML.SetInnerHTML($ID('sslw_choose_content_local_label'), WCM.GetString('wcmExistingLocalFile'));
	WCM.DHTML.SetInnerHTML($ID('sslw_choose_content_none_label'), WCM.GetString('wcmChooseNone'));
	WCM.DHTML.SetInnerHTML($ID('sslw_choose_selected_content_label'), WCM.GetString('wcmChooseSelected'));

	WCM.DHTML.SetInnerHTML($ID('sslw_choose_region_template_use_default_label'), WCM.GetString('wcmUseDefaultRegionTemplate'));
	WCM.DHTML.SetInnerHTML($ID('sslw_choose_region_template_use_this_label'), WCM.GetString('wcmUseThisRegionTemplate'));

	WCM.DHTML.SetInnerHTML($ID('sslw_doc_info_content_id'), WCM.GetString('wcmDocInfoContentId'));
	WCM.DHTML.SetInnerHTML($ID('sslw_doc_info_title'), WCM.GetString('wcmDocInfoTitle'));
	WCM.DHTML.SetInnerHTML($ID('sslw_doc_info_type'), WCM.GetString('wcmDocInfoType'));
	WCM.DHTML.SetInnerHTML($ID('sslw_doc_info_author'), WCM.GetString('wcmDocInfoAuthor'));
	WCM.DHTML.SetInnerHTML($ID('sslw_doc_info_region_definition'), WCM.GetString('wcmDocInfoRegionDefinition'));
	WCM.DHTML.SetInnerHTML($ID('sslw_doc_info_comments'), WCM.GetString('wcmDocInfoComments'));
	WCM.DHTML.SetInnerHTML($ID('sslw_doc_info_link'), WCM.GetString('wcmDocInfoLinkLabel'));
	
	WCM.DHTML.SetInnerHTML($ID('sslw_confirm_action_edit_label'), WCM.GetString('wcmExitAndEdit'));
	WCM.DHTML.SetInnerHTML($ID('sslw_confirm_action_exit_label'), WCM.GetString('wcmExitNoEdit'));
	
	// Set the Auto Advance alt attributes
	for (var imgs = $GET('img'); imgs.next();)
	{
		var elem = imgs.at();
		if( elem && elem['src'] )
		{
			if( ( elem.src.indexOf('icon-move-rt.png') >= 0 ) ||
				( elem.src.indexOf('icon-move-rt-dis.png') >= 0 ) )
			{
				elem.alt = WCM.GetString('wcmAutoAdvanceAlt');
			}
		}
	}

	if( WCM.PageDirectionRTL )
	{
		WCM.DHTML.ChangeClass('wcm-banner-region', 'wcm-banner-region-rtl');
		WCM.DHTML.ChangeClass('app-name-heading', 'app-name-heading-rtl');
		WCM.DHTML.ChangeClass('global-link', 'global-link-rtl');
		WCM.DHTML.ChangeClass('dialog-footer-buttons', 'dialog-footer-buttons-rtl');
		WCM.DHTML.ChangeClass('sslw_first_button', 'sslw_first_button-rtl');
		WCM.DHTML.ChangeClass('sslw_last_button', 'sslw_last_button-rtl');
		WCM.DHTML.ChangeClass('sslw_first_button', 'sslw_first_button-rtl');
		WCM.DHTML.ChangeClass('sslw_last_button', 'sslw_last_button-rtl');

		// Swap the Auto Advance images		
		for (var imgs = $GET('img'); imgs.next();)
		{
			var elem = imgs.next();
			if( elem && elem['src'] )
			{
				if( elem.src.indexOf('icon-move-rt.png') >= 0 )
				{
					elem.src = '../../base/images/icon-move-left.png';
				}
				else if( elem.src.indexOf('icon-move-rt-dis.png') >= 0 )
				{
					elem.src = '../../base/images/icon-move-left-dis.png';
				}
			}
		}
	}

}

//*****************************************************************************

WCM.DHTML.AddEvent(window, 'load', WCM.ContentWizard.Initialize);

//*****************************************************************************
