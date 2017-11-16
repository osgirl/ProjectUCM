/////////////////////////////////////////////////////////////////////////////
// 
// Project   : Web Content Management JavaScript Library (WCM)
//
// FileName  : wcm.contributor.js
// FileType  : JavaScript
// Created   : June 2007
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

if (!WCM.DHTML) throw "must include wcm.dhtml.js before including this file";

//***************************************************************************

if (!$GET) throw "must include wcm.get.js before including this file";

//***************************************************************************

if (!WCM.Menu) throw "must include wcm.menu.js before including this file";

//***************************************************************************

WCM.CONTRIBUTOR = WCM.CONTRIBUTOR || {}; // namespace object

//***************************************************************************

WCM.CONTRIBUTOR.mode = "wcm.contributor.mode";
WCM.CONTRIBUTOR.editTarget = "wcm.edit.target";
WCM.CONTRIBUTOR.isResizing = false;
WCM.CONTRIBUTOR.SS_GET_CONTRIBUTOR_CONFIG = "SS_GET_CONTRIBUTOR_CONFIG";
WCM.CONTRIBUTOR.SS_GET_LEGACY_CONTRIBUTOR_CONFIG = "SS_GET_LEGACY_CONTRIBUTOR_CONFIG";
WCM.CONTRIBUTOR.SS_GET_SWITCH_CONTENT_CONFIG = "SS_GET_SWITCH_CONTENT_CONFIG";
WCM.CONTRIBUTOR.SS_GET_LEGACY_SWITCH_CONTENT_CONFIG = "SS_GET_LEGACY_SWITCH_CONTENT_CONFIG";
WCM.CONTRIBUTOR.isAfterPreload = false;
WCM.CONTRIBUTOR.bannerElmt = null;
WCM.CONTRIBUTOR.bannerHeight = 0;
WCM.CONTRIBUTOR.highlightElmts = new Array();
WCM.CONTRIBUTOR.regionMarkers = new Array();
WCM.CONTRIBUTOR.ssInfoXmlDoc = null;
WCM.CONTRIBUTOR.tooltip = null;

//***************************************************************************

WCM.CONTRIBUTOR.PreloadEditor = function()
{
	if (window["ephoxQuickStart"] && !WCM.CONTRIBUTOR.isAfterPreload)
	{
		WCM.CONTRIBUTOR.isAfterPreload = true;
		ephoxQuickStart(WCM.path + '3rdparty/editlivejava/');
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.DrawBanner = function()
{
	var bod = $TAG('body')[0];
	var banner = WCM.DHTML.CreateElement('div');
	if (bod && banner)
	{
		var bannerClass = 'wcm-contributor-banner';
		if (WCM.PageDirectionRTL)
		{
			bannerClass = 'wcm-contributor-banner-rtl';
		}
		WCM.DHTML.AddClass(banner, bannerClass);
		var bannerLogo = WCM.DHTML.CreateElement('img');
		WCM.DHTML.SetAttribute(bannerLogo, 'src', WCM.path + 'base/images/header_logo.gif');
		WCM.DHTML.SetAttribute(bannerLogo, 'alt', WCM.GetString('wcmBannerLogo'));
		var bannerLabel = WCM.DHTML.CreateElement('span');

		if (window['g_ssIsDesignMode'] && WCM.ToBool(window['g_ssIsDesignMode']))
		{
			WCM.DHTML.Append(bannerLabel, WCM.GetString('wcmDesignMode'));
		}
		else
		{
			WCM.DHTML.Append(bannerLabel, WCM.GetString('wcmContributionMode'));
		}
		WCM.DHTML.Append(banner, bannerLogo);
		WCM.DHTML.Append(banner, WCM.GetString('wcmSiteStudio'));
		WCM.DHTML.Append(banner, bannerLabel);
		WCM.DHTML.Insert(bod, WCM.DHTML.GetFirstChild(bod), banner);

		// Add the page-level menu items
		var menuSpan = WCM.DHTML.CreateElement('span');
		WCM.DHTML.AddClass(menuSpan, 'wcm-contributor-page-menu');

		var menuSep = WCM.DHTML.CreateElement('img');
		WCM.DHTML.SetAttribute(menuSep, 'src', WCM.path + 'base/images/header_menu_separator.gif');
		WCM.DHTML.SetAttribute(menuSep, 'alt', WCM.GetString('wcmBannerMenuSeparatorAlt'));
		WCM.DHTML.AddClass(menuSep, 'wcm-contributor-menu-separator');
		WCM.DHTML.Append(menuSpan, menuSep);

		// Add the "Approve All" Menu item to the Contributor Banner (if applicable)
		if (window['g_ssEnableApproveAll'] && WCM.ToBool(window['g_ssEnableApproveAll']))
		{
			var hasApprovals = false;
			for (var rgns = $GET('wcm-region'); rgns.next();)
			{
				if (hasApprovals)
				{
					break;
				}
				
				var actions = null;
				var regionConfig = null;

				if ((regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(rgns.pos())) && 
					(actions = WCM.CONTRIBUTOR.GetRegionActions(rgns.pos())))
				{
					for (var actns = $GET(actions); actns.next();)
					{
						if (actns.at().action === 'approve')
						{
							if (WCM.IsValid(regionConfig['state']) && WCM.IsValid(regionConfig['state']['revalidateLoginID']))
							{
							}
							else
							{
								hasApprovals = true;
								break;
							}
						}
					}
				}
			}

			if (hasApprovals)
			{
				var menuItem = WCM.DHTML.CreateElement('a');
				WCM.DHTML.SetAttribute(menuItem, 'href', '#');
				WCM.DHTML.AddClass(menuItem, 'wcm-contributor-menu-item');
				WCM.DHTML.AddEvent(menuItem, 'click', WCM.CONTRIBUTOR.ApproveAll);

				var itemIcon = WCM.DHTML.CreateElement('img');

				WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-approve-all.gif');
				WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerMenuItemAltApproveAll'));
				WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerMenuItemAltApproveAll'));

				WCM.DHTML.AddClass(itemIcon, 'wcm-contributor-menu-item-icon');
				WCM.DHTML.Append(menuItem, itemIcon);

				WCM.DHTML.Append(menuItem, WCM.GetString('wcmBannerMenuItemApproveAll'));

				WCM.DHTML.Append(menuSpan, menuItem);
			}
		}

		// Add the "Design Mode"
		if (window['g_ssIsDesignMode'] && WCM.ToBool(window['g_ssIsDesignMode']))
		{
		}
		else
		{
			if (window['g_ssAllowPagePublishMarking'] && WCM.ToBool(window['g_ssAllowPagePublishMarking']))
			{
				var menuItem = WCM.DHTML.CreateElement('a');
				WCM.DHTML.SetAttribute(menuItem, 'id', WCM.GenerateUniqueId('menuItem_'));
				WCM.DHTML.SetAttribute(menuItem, 'href', '#');
				WCM.DHTML.AddClass(menuItem, 'wcm-contributor-menu-item');
				WCM.DHTML.AddEvent(menuItem, 'click', WCM.CONTRIBUTOR.PublishNowClickHandler);

				var itemIcon = WCM.DHTML.CreateElement('img');
				WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-publish.gif');
				WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerMenuItemAltPublishNow'));
				WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerMenuItemAltPublishNow'));
				WCM.DHTML.AddClass(itemIcon, 'wcm-contributor-menu-item-icon');
				WCM.DHTML.Append(menuItem, itemIcon);

				WCM.DHTML.Append(menuItem, WCM.GetString('wcmBannerMenuItemPublishNow'));

				WCM.DHTML.Append(menuSpan, menuItem);
				WCM.CONTRIBUTOR.publishNowLinkId = WCM.GetId(menuItem);
			}

			var menuItem = WCM.DHTML.CreateElement('a');
			WCM.DHTML.SetAttribute(menuItem, 'href', '#');
			WCM.DHTML.AddClass(menuItem, 'wcm-contributor-menu-item');
			WCM.DHTML.AddEvent(menuItem, 'click', WCM.CONTRIBUTOR.DiffClickHandler);

			var itemIcon = WCM.DHTML.CreateElement('img');
			WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-compare.gif');
			WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerMenuItemAltCompare'));
			WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerMenuItemAltCompare'));
			WCM.DHTML.AddClass(itemIcon, 'wcm-contributor-menu-item-icon');
			WCM.DHTML.Append(menuItem, itemIcon);

			WCM.DHTML.Append(menuItem, WCM.GetString('wcmBannerMenuItemCompare'));
			WCM.DHTML.Append(menuSpan, menuItem);
		}

		if (window['g_ssAllowDesignMode'] && WCM.ToBool(window['g_ssAllowDesignMode']))
		{
			if(window['g_ssIsDesignMode'] && WCM.ToBool(window['g_ssIsDesignMode']))
			{
			}
			else
			{
				// Add a separator
				var menuSep = WCM.DHTML.CreateElement('img');
				WCM.DHTML.SetAttribute(menuSep, 'src', WCM.path + 'base/images/header_menu_separator.gif');
				WCM.DHTML.SetAttribute(menuSep, 'alt', WCM.GetString('wcmBannerMenuSeparatorAlt'));
				WCM.DHTML.AddClass(menuSep, 'wcm-contributor-menu-separator');
				WCM.DHTML.Append(menuSpan, menuSep);
			}

			// Add the design mode controls
			var menuItem = WCM.DHTML.CreateElement('a');
			WCM.DHTML.SetAttribute(menuItem, 'href', '#');
			WCM.DHTML.AddClass(menuItem, 'wcm-contributor-menu-item');
			WCM.DHTML.AddEvent(menuItem, 'click', WCM.CONTRIBUTOR.DesignModeClickHandler);

			var itemIcon = WCM.DHTML.CreateElement('img');
			if (window['g_ssIsDesignMode'] && WCM.ToBool(window['g_ssIsDesignMode']))
			{
				WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-design-mode-exit.png');
				WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerItemDesignModeEnabled'));
				WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerItemDesignModeEnabled'));
			}
			else
			{
				WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-design-mode-enter.png');
				WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerItemDesignModeDisabled'));
				WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerItemDesignModeDisabled'));
			}
			WCM.DHTML.AddClass(itemIcon, 'wcm-contributor-menu-item-icon');
			WCM.DHTML.Append(menuItem, itemIcon);

			WCM.DHTML.Append(menuSpan, menuItem);


			// Add the Show Placeholder Properties button
			if (window['g_ssIsDesignMode'] && WCM.ToBool(window['g_ssIsDesignMode']))
			{
				var menuItem = WCM.DHTML.CreateElement('a');
				WCM.DHTML.SetAttribute(menuItem, 'href', '#');
				WCM.DHTML.AddClass(menuItem, 'wcm-contributor-menu-item');
				WCM.DHTML.AddEvent(menuItem, 'click', WCM.CONTRIBUTOR.PlaceHolderPropertiesClickHandler);

				var itemIcon = WCM.DHTML.CreateElement('img');
				WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-design-placeholder-properties.png');
				WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerItemShowPlaceholderProperties'));
				WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerItemShowPlaceholderProperties'));
				WCM.DHTML.AddClass(itemIcon, 'wcm-contributor-menu-item-icon');
				WCM.DHTML.Append(menuItem, itemIcon);

				WCM.DHTML.Append(menuSpan, menuItem);
			}

			// Add the primary/secondary switcher
			if (window['g_ssAlternatePageExists'] && WCM.ToBool(window['g_ssAlternatePageExists']) &&
				window['g_ssIsDesignMode'] && WCM.ToBool(window['g_ssIsDesignMode']))
			{
				var menuItem = WCM.DHTML.CreateElement('a');
				WCM.DHTML.SetAttribute(menuItem, 'href', '#');
				WCM.DHTML.AddClass(menuItem, 'wcm-contributor-menu-item');
				WCM.DHTML.AddEvent(menuItem, 'click', WCM.CONTRIBUTOR.PrimarySecondarySwitcherClickHandler);

				var itemIcon = WCM.DHTML.CreateElement('img');
				if (window['g_ssIsSecondaryPage'] && WCM.ToBool(window['g_ssIsSecondaryPage']))
				{
					WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-design-mode-to-primary.png');
					WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerItemSwitchToPrimary'));
					WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerItemSwitchToPrimary'));
				}
				else
				{
					WCM.DHTML.SetAttribute(itemIcon, 'src', WCM.path + 'base/images/icon-design-mode-to-secondary.png');
					WCM.DHTML.SetAttribute(itemIcon, 'alt', WCM.GetString('wcmBannerItemSwitchToSecondary'));
					WCM.DHTML.SetAttribute(itemIcon, 'title', WCM.GetString('wcmBannerItemSwitchToSecondary'));
				}
				WCM.DHTML.AddClass(itemIcon, 'wcm-contributor-menu-item-icon');
				WCM.DHTML.Append(menuItem, itemIcon);
				WCM.DHTML.Append(menuSpan, menuItem);
			}
		}

		WCM.DHTML.Append(banner, menuSpan);

		WCM.CONTRIBUTOR.bannerElmt = banner;
		WCM.CONTRIBUTOR.SizeBanner();
		if (!WCM.IS_IE)
		{
			WCM.DHTML.SetStyle(banner, 'position', 'fixed');
		}
		var bannerHeight = WCM.DHTML.GetFullHeight(banner);
		var bodyTopMargin = WCM.ToInt(WCM.DHTML.GetStyle(bod, 'margin-top')) + bannerHeight;
		WCM.CONTRIBUTOR.bannerHeight = bodyTopMargin;
		WCM.DHTML.SetStyle(bod, 'margin-top', bodyTopMargin + 'px');

		WCM.DHTML.DragInit({obj: banner});
		WCM.DHTML.SetStyle(banner, 'cursor', 'move')

		WCM.DHTML.FadeIn(banner);
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.PublishNowClickHandler = function(e)
{
	WCM.CONTRIBUTOR.PublishNow();
	return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.DiffClickHandler = function(e)
{
	WCM.CONTRIBUTOR.Diff();
	return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.PlaceHolderPropertiesClickHandler = function(e)
{
	try
	{
		WCM.CONTRIBUTOR.ShowPlaceholderSectionProperties();
	}
	catch (e) { }

	return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.PrimarySecondarySwitcherClickHandler = function(e)
{
	if (window['g_ssIsSecondaryPage'] && WCM.ToBool(window['g_ssIsSecondaryPage']))
	{
		WCM.CONTRIBUTOR.ViewPrimaryPage();
	}
	else
	{
		WCM.CONTRIBUTOR.ViewSecondaryPage();
	}
	return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.DesignModeClickHandler = function(e)
{
	try
	{
		var qs = '';
		qs = WCM.GetQueryString(WCM.GetQueryString());
		qs = WCM.RemoveQueryStringValue('SSDesignMode', qs);
		if (window['g_ssIsDesignMode'] && WCM.ToBool(window['g_ssIsDesignMode']))
		{
			WCM.SetCookie('wcm.design.mode', "false");
		}
		else
		{
			WCM.SetCookie('wcm.design.mode', "true");
		}

		var url = '';
		$D().log('Reloading page... ' + url, window);
		url = (WCM.GetUrlBase() + qs + WCM.GetBookmark());

		WCM.ReloadURL(url);
	}
	catch (e) { }

	return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.SizeBanner = function()
{
	var bannerWidth = WCM.DHTML.GetViewportWidth();
	WCM.DHTML.SetStyle(WCM.CONTRIBUTOR.bannerElmt, 'width', bannerWidth + 'px');
}

//***************************************************************************

WCM.CONTRIBUTOR.GetRegionConfig = function(num)
{
	return (WCM.CONTRIBUTOR.regions && WCM.CONTRIBUTOR.regions[num]);
}

//***************************************************************************

WCM.CONTRIBUTOR.GetRegionConfigByDataFile = function(datafile)
{
	var reg = null;
	if (WCM.CONTRIBUTOR.regions)
	{
		for (var rgns = $GET(WCM.CONTRIBUTOR.regions); rgns.next();)
		{
			if (rgns.at().dataFile && datafile &&
			    rgns.at().dataFile.toLowerCase() === datafile.toLowerCase())
			{
				reg = rgns.at();
				break;
			}
		}
	}
	return reg;
}

//***************************************************************************

WCM.CONTRIBUTOR.GetRegionActions = function(num)
{
	var regionConfig = null;
	if (regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(num))
	{
		return regionConfig['actions'];
	}
	return null;
}

//***************************************************************************

WCM.CONTRIBUTOR.GetRegionMenu = function(num)
{
	var regionConfig = null;
	if (regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(num))
	{
		var regionMenu = null;
		if (regionMenu = WCM.GetObject(regionConfig.regionMenuId))
		{
			return regionMenu;
		}
	}
	return null;
}

//***************************************************************************

WCM.CONTRIBUTOR.GetRegionHighlight = function(num)
{
	var elm = null;
	
	try
	{
		elm = $GET('wcm-region-highlight').at(num);
	}
	catch(e) { }

	return elm;
}

//***************************************************************************

WCM.CONTRIBUTOR.InitializeTooltip = function()
{
	WCM.CONTRIBUTOR.tooltip = WCM.DHTML.CreateElement('div');
	if (WCM.CONTRIBUTOR.tooltip)
	{
		WCM.DHTML.AddClass(WCM.CONTRIBUTOR.tooltip, 'wcm-contributor-tooltip');

		WCM.DHTML.Append($TAG('body')[0], WCM.CONTRIBUTOR.tooltip);
		WCM.DHTML.SetOpacity(WCM.CONTRIBUTOR.tooltip, 0);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.ShowTooltip = function(e, regionMarkerId, regionConfigPos)
{
	var regionConfig = null;
	var regionMarker = $ID(regionMarkerId);
	if (WCM.IsValid(regionMarker) && WCM.IsValid(regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(regionConfigPos)))
	{
		WCM.DHTML.SetStyle(WCM.CONTRIBUTOR.tooltip, 'zIndex', WCM.DHTML.CalculateTopZindex());
		WCM.DHTML.SetStyle(WCM.CONTRIBUTOR.tooltip, 'display', 'inline');

		var ds = '<table border=0>';
		
		if( regionConfig.isLegacyRegionDefinition )
		{
			ds += '<tr>';
			ds += '<td><label>' + WCM.GetString('wcmPropsRegionName') + '</label></td>';
			ds += '<td><span>' + regionConfig.regionName + '</span></td>';
			ds += '<td><span>' + '&nbsp;' + '</span></td>';
			ds += '</tr>';
			
			ds += '<tr>';
			ds += '<td><label>' + WCM.GetString('wcmPropsDataFile') + '</label></td>';
			ds += '<td><span>' + regionConfig.dataFile + '</span></td>';

			if (regionConfig.isNativeDoc)
				ds += '<td><span>' + WCM.GetString('wcmPropsNativeDoc') + '</span></td>';
			else			
				ds += '<td><span>' + '&nbsp;' + '</span></td>';

			ds += '</tr>';
		}
		else
		{
			ds += '<tr>';
			ds += '<td><label>' + WCM.GetString('wcmPropsPlaceholderName') + '</label></td>';
			ds += '<td><span>' + regionConfig.regionName + '</span></td>';
			ds += '<td><span>' + '&nbsp;' + '</span></td>';
			ds += '</tr>';

			ds += '<tr>';
			ds += '<td><label>' + WCM.GetString('wcmPropsDataFile') + '</label></td>';
			ds += '<td><span>' + regionConfig.dataFile + '</span></td>';

			if (regionConfig.isNativeDoc)
				ds += '<td><span>' + WCM.GetString('wcmPropsNativeDoc') + '</span></td>';
			else			
				ds += '<td><span>' + '&nbsp;' + '</span></td>';

			ds += '</tr>';

			ds += '<tr>';
			ds += '<td><label>' + WCM.GetString('wcmPropsPlaceholderDefinition') + '</label></td>';
			ds += '<td><span>' + regionConfig.placeholderDefinition + '</span></td>';
			ds += '<td><span>' + WCM.CONTRIBUTOR.MapOrigin(regionConfig.origins.SS_PLACEHOLDER_DEFINITION) + '</span></td>';
			ds += '</tr>';

			ds += '<tr>';
			ds += '<td><label>' + WCM.GetString('wcmPropsRegionDefinition') + '</label></td>';
			ds += '<td><span>' + regionConfig.regionDefinition + '</span></td>';
			ds += '<td><span>' + WCM.CONTRIBUTOR.MapOrigin(regionConfig.origins.SS_REGION_DEFINITION) + '</span></td>';
			ds += '</tr>';

			if (WCM.IsValid(regionConfig.templateType))
			{
				ds += '<tr>';
				ds += '<td><label>' + regionConfig.templateType + '</label></td>';
				ds += '<td><span>' + regionConfig.template + '</span></td>';
				ds += '<td><span>' + WCM.CONTRIBUTOR.MapOrigin(regionConfig.origins.SS_TEMPLATE) + '</span></td>';
				ds += '</tr>';
			}
			else if (WCM.IsValid(regionConfig.template))
			{
				ds += '<tr>';
				ds += '<td><label>' + WCM.GetString('wcmPropsTemplate') + '</label></td>';
				ds += '<td><span>' + regionConfig.template + '</span></td>';
				ds += '<td><span>' + WCM.CONTRIBUTOR.MapOrigin(regionConfig.origins.SS_TEMPLATE) + '</span></td>';
				ds += '</tr>';
			}
			else
			{
				ds += '<tr>';
				ds += '<td><label>' + WCM.GetString('wcmPropsRegionTemplate') + '</label></td>';
				ds += '<td><span>' + '&nbsp;' + '</span></td>';
				ds += '<td><span>' + '&nbsp;' + '</span></td>';
				ds += '</tr>';
			}
		}
		
		ds += '</table>';
		
		WCM.DHTML.SetInnerHTML(WCM.CONTRIBUTOR.tooltip, ds);
	
		var regionMarkerWidth = WCM.DHTML.GetFullWidth(regionMarker);
		var regionMarkerHeight = WCM.DHTML.GetFullHeight(regionMarker);
		var regionMarkerPaddingTop = WCM.ToInt(WCM.DHTML.GetStyle(regionMarker, "padding-top"), 2);
		var regionMarkerPaddingRight = WCM.ToInt(WCM.DHTML.GetStyle(regionMarker, "padding-right"), 2);

		// var regionMarkerLeft = WCM.ToInt(WCM.DHTML.GetX(regionMarker));
		var regionMarkerTop = WCM.ToInt(WCM.DHTML.GetY(regionMarker));
		
		var tipWidth = WCM.DHTML.GetFullWidth(WCM.CONTRIBUTOR.tooltip);
		var tipHeight = WCM.DHTML.GetFullHeight(WCM.CONTRIBUTOR.tooltip);		

		var tipX = WCM.DHTML.GetEventMouseXPos(e);		
		var tipY = regionMarkerTop - tipHeight;
		
		var windowWidth = WCM.DHTML.GetViewportWidth(window);
		var windowHeight = WCM.DHTML.GetViewportHeight(window);

		// if it spills off the rhs, pull it back till it doesn't
		if (tipX + tipWidth > windowWidth)
			tipX = windowWidth - tipWidth;

		// and if that pushes it off the lhs, bring it back to 0			
		if (tipX < 0)
			tipX = 0;

		var scrollY = WCM.DHTML.GetScrollY(window);

		// if spilled off the top...			
		if (tipY - scrollY < 0)
		{	// place it below instead (even if it then/also spills off the bottom...) 
 			tipY = regionMarkerTop + regionMarkerHeight;
		}
		
		WCM.DHTML.SetX(WCM.CONTRIBUTOR.tooltip, tipX);
		WCM.DHTML.SetY(WCM.CONTRIBUTOR.tooltip, tipY);

		WCM.DHTML.FadeIn(WCM.CONTRIBUTOR.tooltip, 20, 250);

		return WCM.DHTML.CancelEvent(e);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.MapOrigin = function (origin)
{
	if (!WCM.IsValid(origin))
		return "";

	if (origin == "node")
		return WCM.GetString('wcmOriginNode');
		
	if (origin == "tag")
		return WCM.GetString('wcmOriginTag');
	
	if (origin == "pc")
		return WCM.GetString('wcmOriginPC');

	if (origin == "site")
		return WCM.GetString('wcmOriginSite');

	if (origin == "env")
		return WCM.GetString('wcmOriginEnv');

	if (origin == "default")
		return WCM.GetString('wcmOriginDefault');

	if (origin == "metadata")
		return WCM.GetString('wcmOriginMetadata');

	return origin;
}

//***************************************************************************

WCM.CONTRIBUTOR.HideTooltip = function(e, regionMarkerId)
{
//	var regionMarker = $ID(regionMarkerId);
//	window.setTimeoutEx(function()
//	{
		WCM.DHTML.SetStyle(WCM.CONTRIBUTOR.tooltip, 'display', 'none');
		WCM.DHTML.SetOpacity(WCM.CONTRIBUTOR.tooltip, 0);
//	}, 250);
	
	//let this event ripple up to the regionMarker object
	//return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.CalculateAndHighlight = function(elementClassName, highlightClassName)
{
	WCM.DHTML.Remove($GET(highlightClassName).at());
	for (var elms = $GET(highlightClassName); elms.next();)
	{
		WCM.DHTML.Remove(elms.at());
	}

	var count = 0;
	for (elms = $GET(elementClassName); elms.next();)
	{
		var elem = elms.at();
		
		var regionContentElmts = WCM.DHTML.GetChildren(elem);
		for (var rgns = $GET(regionContentElmts); rgns.next();)
		{
			var disp = WCM.ToString(WCM.DHTML.GetStyle(rgns.at(), 'display'), '').toLowerCase();
			if (disp == 'block' || disp == 'table')
			{
				WCM.DHTML.SetStyle(elem, 'display', 'block');
				break;
			}
		}

		if (WCM.PageDirectionRTL)
		{
			var txt = WCM.DHTML.GetInnerText(elem, true);
			if (txt.length > 0)
			{
				// Temporarily tweak the display of the element, so the page x coordinaate can be properly computed
				WCM.DHTML.SetStyle(elem, 'display', 'block');
			}
		}

		var div = WCM.DHTML.CreateElement('div');
		WCM.DHTML.SetInnerHTML(div, '&nbsp;');
		WCM.DHTML.AddClass(div, highlightClassName);
		WCM.DHTML.SetStyle(div, 'position', 'absolute');
		WCM.DHTML.SetStyle(div, 'display', 'none');
		WCM.DHTML.SetStyle(div, 'height', WCM.DHTML.GetFullHeight(elem) + 'px');
		WCM.DHTML.SetStyle(div, 'width', WCM.DHTML.GetFullWidth(elem) + 'px');		
		WCM.DHTML.SetX(div, WCM.DHTML.GetPageX(elem));
		WCM.DHTML.SetY(div, WCM.DHTML.GetPageY(elem));
		
		WCM.DHTML.SetStyle(elem, 'display', 'inline');
		
		WCM.DHTML.Append($TAG('body')[0], div);
		WCM.DHTML.SetOpacity(div, 0);
		
		WCM.CONTRIBUTOR.highlightElmts[elms.pos()] = div;
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.DrawRegionMarkers = function()
{
	for (var markers = $GET('wcm-region-marker'); markers.next();)
	{
		WCM.DHTML.Remove(markers.at());	
	}
	
	for (var regions = $GET('wcm-region'); regions.next();)
	{
		var region = regions.at();
		
		var regionMarker = WCM.DHTML.CreateElement('div');
		WCM.DHTML.AddClass(regionMarker, 'wcm-region-marker');
		
		if (typeof supressRegionHighlight == 'undefined' || !supressRegionHighlight)
		{
			WCM.DHTML.AddEvent(regionMarker, 'mouseover', $CBE(WCM.CONTRIBUTOR.RegionMouseOver, regions.pos()));
			WCM.DHTML.AddEvent(regionMarker, 'mouseout', $CBE(WCM.CONTRIBUTOR.RegionMouseOut, regions.pos()));
		}
		
		var regionMarkerBody = WCM.DHTML.CreateElement('div');
		var cssClass = 'wcm-region-marker-body';
		if (WCM.PageDirectionRTL)
		{
			cssClass = 'wcm-region-marker-body-rtl';
		}
		WCM.DHTML.AddClass(regionMarkerBody, cssClass);	
		WCM.DHTML.Append(regionMarker, regionMarkerBody);
		
		var regionLabel = WCM.DHTML.CreateElement('span');
		cssClass = 'wcm-region-label';
		if (WCM.PageDirectionRTL)
		{
			cssClass = 'wcm-region-label-rtl';
		}
		WCM.DHTML.AddClass(regionLabel, cssClass);
		
		if (WCM.IsValid(WCM.CONTRIBUTOR.tooltip))
		{
			WCM.DHTML.AddEvent(regionLabel, 'mouseover', $CBE(WCM.CONTRIBUTOR.ShowTooltip, WCM.GetId(regionMarker), regions.pos()));
			WCM.DHTML.AddEvent(regionLabel, 'mouseout', $CBE(WCM.CONTRIBUTOR.HideTooltip, WCM.GetId(regionMarker)));
		}

		WCM.DHTML.Append(regionMarkerBody, regionLabel);

		var regionLabelStr = null;
		if (WCM.IsValid(WCM.CONTRIBUTOR.ssInfoXmlDoc))
		{
			var regionDom = WCM.CONTRIBUTOR.ssInfoXmlDoc.selectSingleNode('/ssinfo/region[@id="' + WCM.DHTML.GetAttribute(region, "id") + '"]');
			if (WCM.IsValid(regionDom))
			{
				regionLabelStr = regionDom.getAttribute('name');
			}
		}
		
		regionLabelStr = regionLabelStr || WCM.DHTML.GetAttribute(region, "id");
		WCM.DHTML.Append(regionLabel, regionLabelStr);
		
		var iconContainer = WCM.DHTML.CreateElement('span');
		cssClass = 'wcm-region-icon-span';
		if (WCM.PageDirectionRTL)
		{
			cssClass = 'wcm-region-icon-span-rtl';
		}
		WCM.DHTML.AddClass(iconContainer, cssClass);
		WCM.DHTML.Append(regionMarkerBody, iconContainer);
		
		var menuIcon = WCM.DHTML.CreateElement('img');
		WCM.DHTML.SetAttribute(menuIcon, 'id', WCM.GenerateUniqueId('MENUICON'));
		WCM.DHTML.SetAttribute(menuIcon, 'align', 'absmiddle');
		WCM.DHTML.SetAttribute(menuIcon, 'src', WCM.path + 'base/images/actions_icon.gif');
		var alt = WCM.GetString('wcmContributorIconAlt', WCM.DHTML.GetAttribute(region, "id"));
		WCM.DHTML.SetAttribute(menuIcon, 'alt', alt);
		WCM.DHTML.Append(iconContainer, menuIcon);
		WCM.DHTML.AddEvent(menuIcon, 'click', $CBE(WCM.CONTRIBUTOR.ShowRegionMenuHandler, regions.pos()));

		WCM.DHTML.Append($TAG('body')[0], regionMarker);
		
		var posX = WCM.ToInt(WCM.DHTML.GetPageX(region)) - WCM.ToInt(WCM.DHTML.GetStyle(regionMarker, "padding-left"));
		var posY = WCM.ToInt(WCM.DHTML.GetPageY(region)) - WCM.ToInt(WCM.DHTML.GetStyle(regionMarker, "padding-top"));

		// Ensure the region marker is not obscured by other HTML elements on the page
		var zIndex = WCM.DHTML.CalculateTopZindex(window)
		WCM.DHTML.SetStyle(regionMarker, 'zIndex', zIndex); 

		var actions = null;
		var regionConfig = null;
		var hasEditAction = false;
		var hasSwitchAction = false;
		var hasDesignerSwitchAction = false;
		var hasNoFile = false;
		var hasSubTemplate = false;		
		
		if ((regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(regions.pos())) && 
		    (actions = WCM.CONTRIBUTOR.GetRegionActions(regions.pos())))
		{
			hasNoFile = (WCM.IsValid(regionConfig.dataFile) && regionConfig.dataFile == "");
			hasSubTemplate = (WCM.IsValid(regionConfig.template) && regionConfig.template != "" && WCM.IsValid(regionConfig.templateType) && regionConfig.templateType == 'Subtemplate');
		
			var oldMenu = WCM.GetObject(regionConfig.regionMenuId, null, true);
			if (oldMenu)
			{
				oldMenu.RemoveMenuFromDOM();
				regionConfig.regionMenuId = null;
			}
	
			var menu = new WCM.Menu({clickMenuItem: $CB(WCM.CONTRIBUTOR.SelectRegionMenuItem, regions.pos())});
			regionConfig.regionMenuId = menu.id;
			menu.regionMenuIconId = WCM.GetId(menuIcon);
			
			for (var actns = $GET(actions); actns.next();)
			{
				var action = actns.at();				
				if (action.action === 'switch')
				{
					hasSwitchAction = true;
					if (hasNoFile && !hasSubTemplate)
					{
						action.caption = WCM.GetString('wcmAssignRegionContent');
					}
				}
				if (action.action === 'designerChooseDataFile')
				{
					hasDesignerSwitchAction = true;
					if (hasNoFile && !hasSubTemplate)
					{
						action.caption = WCM.GetString('wcmAssignRegionContent');
					}
				}				
				menu.AddItem(action.caption, action.action);
				if (action.action === 'edit')
				{
					menu.AddSeparator();
					hasEditAction = true;
				}
				if (action.action === 'designerChooseRegionTemplate')
				{
					menu.AddSeparator();
				}
			}
		}
		else
		{
			$D().error('Unable to locate Region configuration.', window);
		}
		
		if (WCM.IsValid(regionConfig.state))
		{
			if (regionConfig.state.checkedOut)
			{
				var checkoutIndicator = WCM.DHTML.CreateElement('img');
				WCM.DHTML.SetAttribute(checkoutIndicator, 'src', WCM.path + 'base/images/indicator-checkout.gif');
				WCM.DHTML.SetAttribute(checkoutIndicator, 'alt', WCM.GetString('wcmCheckedOutToUser', regionConfig.state.checkOutUser));
				WCM.DHTML.SetAttribute(checkoutIndicator, 'title', WCM.GetString('wcmCheckedOutToUser', regionConfig.state.checkOutUser));
				WCM.DHTML.Insert(regionMarkerBody, regionLabel, checkoutIndicator);
			}
			if (WCM.IsValid(regionConfig.state.inWorkflow) && regionConfig.state.inWorkflow)
			{
				var workflowIndicator = WCM.DHTML.CreateElement('img');
				WCM.DHTML.SetAttribute(workflowIndicator, 'src', WCM.path + 'base/images/indicator-workflow.gif');
				WCM.DHTML.SetAttribute(workflowIndicator, 'alt', WCM.GetString('wcmInWorkflow'));
				WCM.DHTML.SetAttribute(workflowIndicator, 'title', WCM.GetString('wcmInWorkflow'));
				WCM.DHTML.Insert(regionMarkerBody, regionLabel, workflowIndicator);
			}
			if (WCM.IsValid(regionConfig.state.replaceable) && regionConfig.state.replaceable)
			{
				var replaceableIndicator = WCM.DHTML.CreateElement('img');
				WCM.DHTML.SetAttribute(replaceableIndicator, 'src', WCM.path + 'base/images/indicator-replaceable.png');
				WCM.DHTML.SetAttribute(replaceableIndicator, 'alt', WCM.GetString('wcmReplaceable'));
				WCM.DHTML.SetAttribute(replaceableIndicator, 'title', WCM.GetString('wcmReplaceable'));
				WCM.DHTML.Insert(regionMarkerBody, regionLabel, replaceableIndicator);
			}		
			
		}
		
		if (hasEditAction)
		{
			var editLink = WCM.DHTML.CreateElement('a');
			WCM.DHTML.SetAttribute(editLink, 'href', '#');
			WCM.DHTML.AddEvent(editLink, 'click', $CBE(WCM.CONTRIBUTOR.EditHandler, regions.pos()));
			WCM.DHTML.Insert(iconContainer, menuIcon, editLink);
			
			var editIcon = WCM.DHTML.CreateElement('img');
			WCM.DHTML.SetAttribute(editIcon, 'align', 'absmiddle');
			WCM.DHTML.SetAttribute(editIcon, 'src', WCM.path + 'base/images/html_edit.png');
			WCM.DHTML.SetAttribute(editIcon, 'alt', WCM.GetString('wcmEditRegionContent'));
			WCM.DHTML.SetAttribute(editIcon, 'title', WCM.GetString('wcmEditRegionContent'));
			WCM.DHTML.Append(editLink, editIcon);
		}
		else if (hasSwitchAction && hasNoFile)
		{
			var assignLink = WCM.DHTML.CreateElement('a');
			WCM.DHTML.SetAttribute(assignLink, 'href', '#');
			WCM.DHTML.AddEvent(assignLink, 'click', $CBE(WCM.CONTRIBUTOR.SwitchHandler, regions.pos()));
			WCM.DHTML.Insert(iconContainer, menuIcon, assignLink);

			var assignIcon = WCM.DHTML.CreateElement('img');
			WCM.DHTML.SetAttribute(assignIcon, 'align', 'absmiddle');
			
			if (!hasSubTemplate)
			{
				WCM.DHTML.SetAttribute(assignIcon, 'src', WCM.path + 'base/images/assign_datafile.png');
				WCM.DHTML.SetAttribute(assignIcon, 'alt', WCM.GetString('wcmAssignRegionContent'));
				WCM.DHTML.SetAttribute(assignIcon, 'title', WCM.GetString('wcmAssignRegionContent'));
			}
			else
			{
				WCM.DHTML.SetAttribute(assignIcon, 'src', WCM.path + 'base/images/switch_datafile.png');
				WCM.DHTML.SetAttribute(assignIcon, 'alt', WCM.GetString('wcmSwitchRegionContent'));
				WCM.DHTML.SetAttribute(assignIcon, 'title', WCM.GetString('wcmSwitchRegionContent'));
			}
			
			WCM.DHTML.Append(assignLink, assignIcon);
		}
		else if (hasDesignerSwitchAction)
		{
			var assignIcon = WCM.DHTML.CreateElement('img');
			WCM.DHTML.SetAttribute(assignIcon, 'align', 'absmiddle');
			if (hasNoFile && !hasSubTemplate)
			{
				WCM.DHTML.SetAttribute(assignIcon, 'src', WCM.path + 'base/images/assign_datafile.png');
				WCM.DHTML.SetAttribute(assignIcon, 'alt', WCM.GetString('wcmAssignRegionContent'));
				WCM.DHTML.SetAttribute(assignIcon, 'title', WCM.GetString('wcmAssignRegionContent'));
			}
			else
			{
				WCM.DHTML.SetAttribute(assignIcon, 'src', WCM.path + 'base/images/switch_datafile.png');
				WCM.DHTML.SetAttribute(assignIcon, 'alt', WCM.GetString('wcmSwitchRegionContent'));
				WCM.DHTML.SetAttribute(assignIcon, 'title', WCM.GetString('wcmSwitchRegionContent'));
			}
			// can't use an <a> tag because Designer suppresses navigation
			WCM.DHTML.AddEvent(assignIcon, 'click', $CBE(WCM.CONTRIBUTOR.DesignerChooseDataFileHandler, regions.pos()));
			WCM.DHTML.Insert(iconContainer, menuIcon, assignIcon);
		}

		if (WCM.PageDirectionRTL)
		{
			// Temporarily tweak the display of the element, so the width can be properly computed
			WCM.DHTML.SetStyle(region, 'display', 'block');
			
			var elemX = WCM.ToInt(WCM.DHTML.GetPageX(region));
			var elemWidth = WCM.ToInt(WCM.DHTML.GetFullWidth(region));
			
			var markerWidth = WCM.ToInt(WCM.DHTML.GetFullWidth( regionMarker ));

			if (WCM.IS_IE)
			{
				// Set the "width" style for the marker to what was just computed.  This avoids wrapping problems in IE.
				WCM.DHTML.SetStyle(regionMarker, 'width', markerWidth + 'px');
			}
			
			var padRt = WCM.ToInt(WCM.DHTML.GetStyle(regionMarker, "padding-right"));
			posX = (elemX + elemWidth) - markerWidth + padRt;

			WCM.DHTML.SetStyle(region, 'display', 'inline');

			if (posX < 0)
			{
				posX = 0;
			}
		}

		WCM.DHTML.SetX(regionMarker, posX);
		var h = WCM.ToInt(WCM.DHTML.GetStyle(regionMarker, 'height'), 22);
		var markerPosY = ((posY - h) > WCM.CONTRIBUTOR.bannerHeight) ? posY - h : posY;
		WCM.DHTML.SetY(regionMarker, markerPosY);

		// If this region marker overlaps an existing one, adjust it by a smidge
		for (var markers = $GET(WCM.CONTRIBUTOR.regionMarkers); markers.next();)
		{		
			var testMarker = markers.at();
			if ((markerPosY >= testMarker.markerPosY) && (markerPosY < (testMarker.markerPosY + testMarker.height)))
			{
				markerPosY = testMarker.markerPosY + ((testMarker.height * 3) / 4);
			}
		}
		WCM.DHTML.SetY(regionMarker, markerPosY);


		WCM.CONTRIBUTOR.regionMarkers[regions.pos()] =
		{
			posX: posX,
			posY: posY,
			width: WCM.DHTML.GetFullWidth(regionMarker),
			height: WCM.DHTML.GetFullHeight(regionMarker),
			markerPosY: markerPosY,
			num: regions.pos()
		};
		
		WCM.DHTML.DragInit({obj: regionMarker});
		WCM.DHTML.SetStyle(regionLabel, 'cursor', 'move')

		WCM.DHTML.FadeIn(regionMarker);		
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.OverlapsExistingMarker = function(regionMarker)
{
	for (var markers = $GET(WCM.CONTRIBUTOR.regionMarkers); markers.next();)
	{		
		var testMarker = markers.at();
		if (regionMarker != testMarker)
		{
			if ((regionMarker.posX >= testMarker.posX && regionMarker.posX < (testMarker.posX + testMarker.width))
					&& (regionMarker.posY >= testMarker.posY && regionMarker.posY < (testMarker.posY + testMarker.height)))
			{
				return true;
			}
		}
	}
	return false;
};

//***************************************************************************

WCM.CONTRIBUTOR.RegionMouseOver = function(e, num)
{
	var elem = WCM.CONTRIBUTOR.highlightElmts[num];
	if (elem)
	{
		WCM.DHTML.SetStyle(elem, 'display', 'block');
		WCM.DHTML.SetOpacity(elem, .45);
		return WCM.DHTML.CancelEvent(e);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.RegionMouseOut = function(e, num)
{
	var elem = WCM.CONTRIBUTOR.highlightElmts[num];
	if (elem)
	{
		WCM.DHTML.SetStyle(elem, 'display', 'none');
		WCM.DHTML.SetOpacity(elem, 0);
		return WCM.DHTML.CancelEvent(e);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.GetSSInfoXmlDoc = function()
{
	var ssInfo = null;
	if (ssInfo = $ID('ssInfo'))
	{		
		var xmlStr = WCM.DHTML.GetInnerHTML(ssInfo);
		var xmlDom = (new DOMParser()).parseFromString(xmlStr, 'text/xml');
		return xmlDom;
	}
	else
	{
		return null;
	}
}

//***************************************************************************		

WCM.CONTRIBUTOR.ShowRegionMenuHandler = function(e, num)
{
	WCM.CONTRIBUTOR.ShowRegionMenu(num);
	return WCM.DHTML.CancelEvent(e);		
}

//***************************************************************************

WCM.CONTRIBUTOR.KeyboardAccessibilityHandler = function (e)
{
	if (WCM.DHTML.GetEventCtrlKey(e))
	{
		switch (WCM.DHTML.GetEventKeyCode(e))
		{
			case 38/*Up Arrow Key*/:
				WCM.CONTRIBUTOR.ShowPreviousRegionMenu();
				return WCM.DHTML.CancelEvent(e);
			case 40/*Down Arrow Key*/:
				WCM.CONTRIBUTOR.ShowNextRegionMenu();
				return WCM.DHTML.CancelEvent(e);
		}
	}	
};

//***************************************************************************

WCM.CONTRIBUTOR.ShowRegionMenu = function(num, scrollIntoView)
{	
	var regionMenu = null;
	if (regionMenu = WCM.CONTRIBUTOR.GetRegionMenu(num))
	{
		WCM.CONTRIBUTOR.HideAllRegionMenus();
		regionMenu.MoveTo(WCM.DHTML.GetPageX(regionMenu.regionMenuIconId)+4, WCM.DHTML.GetPageY(regionMenu.regionMenuIconId)+4);
		regionMenu.Show();
		
		if (WCM.ToBool(scrollIntoView))
		{
			WCM.CONTRIBUTOR.ScrollIntoView(regionMenu.regionMenuIconId);
		}
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.ShowNextRegionMenu = function()
{
	var pos = WCM.CONTRIBUTOR.GetVisibleRegionMenuPos();
	if (WCM.IsNumber(pos) && pos < (WCM.CONTRIBUTOR.GetRegionMenuCount()-1))
	{
		WCM.CONTRIBUTOR.ShowRegionMenu(pos+1, true);
	}
	else
	{
		WCM.CONTRIBUTOR.ShowRegionMenu(0, true);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.ShowPreviousRegionMenu = function()
{	
	var pos = WCM.CONTRIBUTOR.GetVisibleRegionMenuPos();
	if (WCM.IsNumber(pos) && pos)
	{
		WCM.CONTRIBUTOR.ShowRegionMenu(pos-1, true);
	}
	else
	{
		WCM.CONTRIBUTOR.ShowRegionMenu(WCM.CONTRIBUTOR.GetRegionMenuCount()-1, true);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.GetVisibleRegionMenuPos = function()
{
	var regions = null;
	for (regions = $GET('wcm-region'); regions.next();)
	{
		var regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(regions.pos());
		if (regionConfig)
		{
			var regionMenu = WCM.GetObject(regionConfig.regionMenuId);
			if (regionMenu && regionMenu.isVisible)
			{
				break;
			}
		}
	}
	return regions.pos();
}

//***************************************************************************

WCM.CONTRIBUTOR.GetRegionMenuCount = function()
{
	return $GET('wcm-region').count();
}

//***************************************************************************

WCM.CONTRIBUTOR.HideAllRegionMenus = function()
{
	var count = 0;
	for (var regions = $GET('wcm-region'); regions.next();)
	{
		var regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(regions.pos());
		if (regionConfig)
		{
			var regionMenu = WCM.GetObject(regionConfig.regionMenuId);
			if (regionMenu)
			{
				regionMenu.Hide();
			}
		}
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.ScrollIntoView = function(elm)
{	
	WCM.DHTML.ScrollTo(0, Math.round(WCM.DHTML.GetPageY(elm) - ((WCM.DHTML.GetViewportHeight() - WCM.DHTML.GetFullHeight(elm)) / 2)));
}

//***************************************************************************

WCM.CONTRIBUTOR.Resize = function()
{
	WCM.CONTRIBUTOR.SizeBanner();
}

//***************************************************************************

WCM.CONTRIBUTOR.LogContributorModeData = function()
{
	$D().log(WCM.CONTRIBUTOR.LogDataIsland, window);
	$D().log(WCM.CONTRIBUTOR.LogRegionData, window);
};

//***************************************************************************

WCM.CONTRIBUTOR.LogDataIsland = function()
{
	var ssInfo = null;
	if (ssInfo = $ID('ssInfo'))
		return 'ssInfoXmlDoc: ' + WCM.DHTML.GetInnerHTML(ssInfo);
	else 
		return 'ssInfoXmlDoc: null';
};

//***************************************************************************

WCM.CONTRIBUTOR.LogRegionData = function()
{
	return 'WCM.CONTRIBUTOR: ' + $J(WCM.CONTRIBUTOR.regions, true);
};

//***************************************************************************

WCM.CONTRIBUTOR.DrawContributorMode = function()
{
	WCM.DHTML.AddEvent(window, 'load', WCM.CONTRIBUTOR.LoadHandler);
}

//***************************************************************************

WCM.CONTRIBUTOR.LoadHandler = function()
{
	if (WCM && !WCM.ToBool(WCM.GetQueryStringValue('wcm.suppress.ui')))
	{
		WCM.InitializeRTL();

		WCM.CONTRIBUTOR.ssInfoXmlDoc = WCM.CONTRIBUTOR.GetSSInfoXmlDoc();

		if (WCM.ToBool(window['g_ssIsDesignMode']) || !WCM.ToBool(window.SSForceContributor))	
			WCM.CONTRIBUTOR.DrawBanner();

		if (WCM.ToBool(window['g_ssIsDesignMode']) || WCM.ToBool(window.SSForceContributor) || WCM.ToBool(window["SSShowAssignmentTooltips"]) )	
			WCM.CONTRIBUTOR.InitializeTooltip();

		WCM.CONTRIBUTOR.CalculateAndHighlight('wcm-region', 'wcm-region-highlight');
		WCM.CONTRIBUTOR.DrawRegionMarkers();		
		WCM.DHTML.AddWindowResizeEvent(window, WCM.CONTRIBUTOR.Resize);
		WCM.DHTML.AddEvent(window.document, 'keydown', WCM.CONTRIBUTOR.KeyboardAccessibilityHandler);	

		WCM.CONTRIBUTOR.LogContributorModeData();

		var datafile = WCM.GetQueryStringValue(WCM.CONTRIBUTOR.editTarget);
		if (WCM.IsValid(datafile) && datafile != '')
		{
			WCM.CONTRIBUTOR.Edit(WCM.CONTRIBUTOR.GetRegionConfigByDataFile(datafile));
		}		

		//window.setTimeoutEx($CB($D().getCallback('ContributorModeFinishedLoading'), WCM.GetId(window)), 1000);
	}

	if (WCM.ToBool(window['g_ssIsDesignMode']) || WCM.ToBool(window.SSForceContributor))	
		WCM.CONTRIBUTOR.DesignerReportStuff();
};

//***************************************************************************

WCM.CONTRIBUTOR.SelectRegionMenuItem = function(itemId, regionNum)
{
	itemId = WCM.ToString(itemId, '  ');
	itemId = itemId.charAt(0).toUpperCase() + itemId.substring(1);
	regionNum = WCM.ToInt(regionNum, 9999);
	
	$D().log('Region action item selected: ' + itemId + ' - Region number: ' + regionNum, window);

	var regionConfig = null;
	if (WCM.CONTRIBUTOR[itemId] && (regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(regionNum)))
	{
		WCM.CONTRIBUTOR[itemId].apply(null, [regionConfig]);
	}
	else
	{
		$D().error('Region action item not defined: ' + itemId + ' - Region number: ' + regionNum, window);
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.EditHandler = function(e, pos)
{
	WCM.CONTRIBUTOR.Edit(WCM.CONTRIBUTOR.GetRegionConfig(pos));
	return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.Edit = function(regionConfig)
{
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_CHECKOUT_BY_NAME');
	jsonBinder.SetLocalDataValue('dDocName', regionConfig['dataFile']);
	
	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);

	var paramsId = WCM.AddObject({id: WCM.GenerateUniqueId('CHECKOUTRESPONSE'), jsonBinder: jsonBinder, regionConfig: regionConfig});
	jsonBinder.Send(cgiUrl, $CB(WCM.CONTRIBUTOR.CheckOutResponseCallback, paramsId));
};

//***************************************************************************

WCM.CONTRIBUTOR.CheckOutResponseCallback = function(http, paramsId)
{
	var params = WCM.GetObject(paramsId);
	WCM.RemoveObject(paramsId);

	if (params && WCM.IsValid(params.jsonBinder.hasError) && !params.jsonBinder.hasError)
	{
		WCM.CONTRIBUTOR.DoEdit(params.regionConfig);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.DoEdit = function(regionConfig)
{
	var form = null;
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('dDocName', regionConfig['dataFile']);
	jsonBinder.SetLocalDataValue('siteId', regionConfig['siteId']);
	jsonBinder.SetLocalDataValue('nodeId', regionConfig['nodeId']); 
	
	if (!WCM.ToBool(regionConfig['isLegacyRegionDefinition']))
	{
		jsonBinder.SetLocalDataValue('IdcService', WCM.CONTRIBUTOR.SS_GET_CONTRIBUTOR_CONFIG);
		jsonBinder.SetLocalDataValue('regionDefinition', regionConfig['regionDefinition']);
		
		if (regionConfig['placeholderDefinition'] && WCM.IsString(regionConfig['placeholderDefinition']) &&
		    (regionConfig['placeholderDefinition'].length > 0))
		{
			jsonBinder.SetLocalDataValue('placeholderDefinition', regionConfig['placeholderDefinition']);
		}
		else if (regionConfig['placeholderDefinitionXml'] && WCM.IsString(regionConfig['placeholderDefinitionXml']) &&
		    (regionConfig['placeholderDefinitionXml'].length > 0))
		{
			jsonBinder.SetLocalDataValue('placeholderDefinitionXml', regionConfig['placeholderDefinitionXml']);
		}
	}
	else
	{
		if (WCM.IsValid(WCM.CONTRIBUTOR.ssInfoXmlDoc))
		{
			var regionId = '' + regionConfig['regionName'];
			var regionDom = WCM.CONTRIBUTOR.ssInfoXmlDoc.selectSingleNode('/ssinfo/region[@id="' + regionId + '"]');
			var regionStr = WCM.Serialize(regionDom);

			jsonBinder.SetLocalDataValue('IdcService', WCM.CONTRIBUTOR.SS_GET_LEGACY_CONTRIBUTOR_CONFIG);
			jsonBinder.SetLocalDataValue('regionDefinition', regionStr);
		}
		else
		{
			$D().error('Unable to locate legacy schema (ssInfo).', window);
			return;
		}
	}

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	
	$D().log('Preparing service call for WCM.CONTRIBUTOR.DoEdit', window);

	var paramsId = WCM.AddObject({id: WCM.GenerateUniqueId('GETCONFIGRESPONSE'), jsonBinder: jsonBinder, regionConfig: regionConfig});
	jsonBinder.Send(cgiUrl, $CB(WCM.CONTRIBUTOR.ShowContributorForm, paramsId));
};

//***************************************************************************

WCM.CONTRIBUTOR.ShowContributorForm = function(http, paramsId)
{
	var params = WCM.GetObject(paramsId);
	WCM.RemoveObject(paramsId);
	
	var jsonBinder = null;
	var regionConfig = null;
	
	if (params && WCM.IsValid(jsonBinder = params.jsonBinder) && WCM.IsValid(regionConfig = params.regionConfig))
	{	
		if (WCM.IsValid(jsonBinder.hasError) && !jsonBinder.hasError)
		{
			var options = null;
			var text = http.GetResponseText();		
			if (options = $J(text))
			{
				paramsId = WCM.AddObject({id: WCM.GenerateUniqueId('FORMCLOSEHANDLER'), regionConfig: regionConfig});
				options.callback = $CB(WCM.CONTRIBUTOR.PopuFormCloseCallback, paramsId);

				options.pageLocation = WCM.GetUrlBase() + (WCM.IsValid(WCM.Proxy) ? WCM.Proxy.RemoveProxyValuesFromQuery() : WCM.GetQueryString()) + WCM.GetBookmark();
				options.regionName = regionConfig['regionName'] || '';
				options.isLegacyRegion = WCM.ToBool(regionConfig['isLegacyRegionDefinition']);
				form = new WCM.SiteStudioPopupForm(options);

				$D().startProfiling('FORM_LOAD', window);		
				form.Open();					
			}
			else
			{
				$D().error('Unable to parse configuration to JSON', window); 
				WCM.CONTRIBUTOR.UndoCheckout(regionConfig, $CB(WCM.CONTRIBUTOR.ProxyCancelEdit, true));
			}
		}
		else
		{
			WCM.CONTRIBUTOR.UndoCheckout(regionConfig, $CB(WCM.CONTRIBUTOR.ProxyCancelEdit, true));
		}
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.PopuFormCloseCallback = function(returnValue, paramsId)
{
	var params = WCM.GetObject(paramsId);
	WCM.RemoveObject(paramsId);
	
	var regionConfig = params.regionConfig;
	
	if (WCM.IsValid(returnValue))
	{
		window.setTimeoutEx(WCM.CONTRIBUTOR.SuccessfulEditCallback, 3000);
	}
	else
	{
		WCM.CONTRIBUTOR.UndoCheckout(regionConfig, $CB(WCM.CONTRIBUTOR.ProxyCancelEdit, false));
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.SuccessfulEditCallback = function()
{
	if (!WCM.IsValid(WCM.Proxy))
	{
		var url = (WCM.GetUrlBase() + WCM.RemoveQueryStringValue(WCM.CONTRIBUTOR.editTarget, WCM.GetQueryString()) + WCM.GetBookmark());
		$D().log('Reloading page...' + url, window);						
		WCM.ReloadURL(url);
	}
	else
	{
		WCM.Proxy.SuccessfulEdit();
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.ProxyCancelEdit = function(useTimout)
{
	if (WCM.IsValid(WCM.Proxy))
	{
		if (WCM.ToBool(useTimout))
		{
			window.setTimeout(WCM.Proxy.CancelEdit, 2000);
		}
		else
		{
			WCM.Proxy.CancelEdit();
		}
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.UndoCheckout = function(regionConfig, callback)
{
	callback = callback || WCM.EmptyFunction;

	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'UNDO_CHECKOUT_BY_NAME');
	jsonBinder.SetLocalDataValue('dDocName', regionConfig['dataFile']);
	
	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);

	var paramsId = WCM.AddObject({id: WCM.GenerateUniqueId('UNDOCHECKOUTHANDLER'), callback: callback});
	jsonBinder.Send(cgiUrl, $CB(WCM.CONTRIBUTOR.UndoCheckoutResponseCallback, paramsId));
};

//***************************************************************************

WCM.CONTRIBUTOR.UndoCheckoutResponseCallback = function(regionConfig, paramsId)
{
	var params = WCM.GetObject(paramsId);
	WCM.RemoveObject(paramsId);
	
	if (params && params.callback)
	{
		params.callback();
		$D().getCallback('UndoCheckout')(window);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.Coao = function(regionConfig)
{
	$D().log('Checkout And Open Invoked', window);

	if (WCM.IS_WINDOWS)
	{
		var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
		var suffix = '?IdcService=SS_EDIT_NATIVE_DOCUMENT&dDocName=' + WCM.ToString(regionConfig['dataFile']);

		var options = {url: cgiUrl + suffix, width: 300, height: 200};
		var popup = new WCM.Popup(options);
		popup.Open();					
		popup.Focus();
	}
	else
	{
		alert(WCM.GetString('wcmEditTargetUnsupportedPlatform'));
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.DocInfo = function(regionConfig)
{
	$D().log('DocInfo', window);

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	var suffix = '?IdcService=DOC_INFO_BY_NAME&dDocName=' + WCM.ToString(regionConfig['dataFile']);

	// window.location.href = cgiUrl + suffix;
	window.open(cgiUrl + suffix, "_blank");
}

//***************************************************************************

WCM.CONTRIBUTOR.DocInfoUpdate = function(regionConfig)
{
	$D().log('MetadataUpdate', window);

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	var suffix = '?IdcService=GET_UPDATE_FORM';
	suffix += '&dID=' + WCM.ToString(regionConfig['dataFileID']);
	suffix += '&dDocName=' + WCM.ToString(regionConfig['dataFile']);

	// window.location.href = cgiUrl + suffix;
	window.open(cgiUrl + suffix, "_blank");
}

//***************************************************************************

WCM.CONTRIBUTOR.Approve = function(regionConfig)
{
	$D().log('Approve', window);

	// Ascertain which workflow service to use by inspecting the "workflowEdit" state.
	var serviceName = 'WORKFLOW_APPROVE';
	if (regionConfig['state'] && regionConfig['state']['workflowEdit'])
	{
		serviceName = 'WORKFLOW_EDIT_APPROVE';
	}

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	
	var cgiParams = '';
	cgiParams = WCM.SetQueryStringValue('IdcService', serviceName, cgiParams);
	cgiParams = WCM.SetQueryStringValue('dID', regionConfig['dataFileID'], cgiParams);

	if (WCM.IsValid(regionConfig['state']) && WCM.IsValid(regionConfig['state']['revalidateLoginID']))
	{
		cgiParams = WCM.SetQueryStringValue('revalidateLoginID', WCM.ToString(regionConfig['state']['revalidateLoginID']), cgiParams);
	}

	var redirectUrl = window.location.href;
	cgiParams = WCM.SetQueryStringValue('RedirectUrl', redirectUrl, cgiParams);

	window.location.href = cgiUrl + cgiParams;
}

//***************************************************************************

WCM.CONTRIBUTOR.ApproveAll = function(e)
{
	$D().log('ApproveAll', window);

	var workflowRegions = new Array();
	var workflowDataFiles = {};

	for (var rgns = $GET('wcm-region'); rgns.next();)
	{		
		var actions = null;
		var regionConfig = null;
		var hasApproval = false;

		if ((regionConfig = WCM.CONTRIBUTOR.GetRegionConfig(rgns.pos())) && 
			(actions = WCM.CONTRIBUTOR.GetRegionActions(rgns.pos())))
		{
			for (var actns = $GET(actions); actns.next();)
			{
				if (actns.at().action === 'approve')
				{
					if(WCM.IsValid(regionConfig['state']) && WCM.IsValid(regionConfig['state']['revalidateLoginID']))
					{
					}
					else
					{
						hasApproval = true;
						break;
					}
				}
			}

			if (hasApproval)
			{
				var dID = WCM.ToString(regionConfig['dataFileID']).toUpperCase();
				if (workflowDataFiles['d' + dID] && WCM.ToBool(workflowDataFiles['d' + dID]))
				{
				}
				else
				{
					workflowDataFiles['d' + dID] = true;
					workflowRegions[workflowRegions.length] = regionConfig;
				}
			}
		}
	}

	if (workflowRegions.length > 0)
	{
		var approveCallback = WCM.CONTRIBUTOR.ReloadAfterApprove;
		var prevApproveCallback = null;

		var workflowRgns = $GET(workflowRegions);
		for (workflowRgns = workflowRgns.reverse(); workflowRgns.next();)
		{
			var regionConfig = workflowRgns.at();

			prevApproveCallback = approveCallback;
			approveCallback = $CB(WCM.CONTRIBUTOR.ApproveOne, regionConfig, prevApproveCallback);
		}

		approveCallback();
	}

	if (e)	
	{
		return WCM.DHTML.CancelEvent(e);
	}	
}

//***************************************************************************

WCM.CONTRIBUTOR.ApproveOne = function(regionConfig, callback)
{
	$D().log('ApproveOne', window);

	if (WCM.IsValid(regionConfig['state']) && WCM.IsValid(regionConfig['state']['revalidateLoginID']))
	{
		// We cannot use this mechanism for revalidated login approvals, so carry on here.
		if (callback)
		{
			callback();
		}
		return;
	}

	// Ascertain which workflow service to use by inspecting the "workflowEdit" state.
	var serviceName = 'WORKFLOW_APPROVE';
	if(regionConfig['state'] && regionConfig['state']['workflowEdit'])
	{
		serviceName = 'WORKFLOW_EDIT_APPROVE';
	}


	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', serviceName);
	jsonBinder.SetLocalDataValue('dID', regionConfig['dataFileID']);

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	
	$D().log('Preparing service call for WCM.CONTRIBUTOR.ApproveOne', window);

	jsonBinder.Send(cgiUrl, $CB(WCM.CONTRIBUTOR.ApproveHandler, jsonBinder, callback));
}

//***************************************************************************

WCM.CONTRIBUTOR.ApproveHandler = function(http, jsonBinder, callback)
{
	if (WCM.IsValid(jsonBinder.hasError) && WCM.ToBool(jsonBinder.hasError))
	{
		// Allow enough time for the error dialog to display properly
		window.setTimeoutEx(WCM.CONTRIBUTOR.ReloadAfterApprove, 2000 );
		return;
	}

	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		if (callback)
		{
			callback();
		}
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.ReloadAfterApprove = function()
{
	$D().log('ReloadAfterApprove', window);
	WCM.ReloadURL();
}

//***************************************************************************

WCM.CONTRIBUTOR.Reject = function(regionConfig)
{
	$D().log('Reject', window);

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	
	var cgiParams = '';
	cgiParams = WCM.SetQueryStringValue('IdcService', 'WORKFLOW_REJECT_FORM', cgiParams);
	cgiParams = WCM.SetQueryStringValue('dID', regionConfig['dataFileID'], cgiParams);

	window.location.href = cgiUrl + cgiParams;
}

//***************************************************************************

WCM.CONTRIBUTOR.NameSwitch = function(regionName)
{
	for (var regions = $GET(WCM.CONTRIBUTOR.regions); regions.next();)
	{
		var regionConfig = regions.at();
		if (regionConfig.regionName == regionName)
		{
			WCM.CONTRIBUTOR.Switch(regionConfig);
			break;
		}
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.SwitchHandler = function(e, pos)
{
	WCM.CONTRIBUTOR.Switch(WCM.CONTRIBUTOR.GetRegionConfig(pos));
	return WCM.DHTML.CancelEvent(e);
};

//***************************************************************************

WCM.CONTRIBUTOR.Switch = function(regionConfig)
{
	$D().log('Switch', window);

	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('dDocName', regionConfig['dataFile']);
	jsonBinder.SetLocalDataValue('siteId', regionConfig['siteId']);
	jsonBinder.SetLocalDataValue('nodeId', regionConfig['nodeId']);
	jsonBinder.SetLocalDataValue('isSecondaryPage', regionConfig['isSecondaryPage']);
	jsonBinder.SetLocalDataValue('regionName', regionConfig['regionName']);

	// CUSTOMISATION start
	//alert(regionConfig['LanguageCode']); alert(regionConfig['SiteLocale'])
	jsonBinder.SetLocalDataValue('SiteLocale', regionConfig['SiteLocale']);
	jsonBinder.SetLocalDataValue('LanguageCode', regionConfig['LanguageCode']);
	jsonBinder.SetLocalDataValue('CountryCode', regionConfig['CountryCode']);
	// CUSTOMISATION ends


	if (!WCM.ToBool(regionConfig['isLegacyRegionDefinition']))
	{
		jsonBinder.SetLocalDataValue('regionDefinition', WCM.ToString(regionConfig['regionDefinition']));

		var placeholderDefinition = regionConfig['placeholderDefinition'];
		var placeholderDefinitionXml = regionConfig['placeholderDefinitionXml'];
		if (WCM.IsValid(placeholderDefinition) && (WCM.ToString(placeholderDefinition).length > 0))
		{
			jsonBinder.SetLocalDataValue('IdcService', 'SS_GET_PLACEHOLDER_SWITCH_CONTENT_CONFIG');
			jsonBinder.SetLocalDataValue('placeholderDefinition', placeholderDefinition);
		}
		else if (WCM.IsValid( placeholderDefinitionXml ) && ( WCM.ToString(placeholderDefinitionXml).length > 0))
		{
			jsonBinder.SetLocalDataValue('IdcService', 'SS_GET_PLACEHOLDER_SWITCH_CONTENT_CONFIG');
			jsonBinder.SetLocalDataValue('placeholderDefinitionXml', placeholderDefinitionXml);
		}
		else
		{
			alert(WCM.GetString('wcmNoPlaceholderAssociations'));
			return;
		}
	}
	else
	{
		if (WCM.IsValid(WCM.CONTRIBUTOR.ssInfoXmlDoc))
		{
			var regionId = '' + regionConfig['regionName'];
			var regionDom = WCM.CONTRIBUTOR.ssInfoXmlDoc.selectSingleNode('/ssinfo/region[@id="' + regionId + '"]');
			var regionStr = WCM.Serialize(regionDom);
			
			jsonBinder.SetLocalDataValue('IdcService', WCM.CONTRIBUTOR.SS_GET_LEGACY_SWITCH_CONTENT_CONFIG);
			jsonBinder.SetLocalDataValue('regionDefinition', regionStr);
		}
		else
		{
			$D().error('Unable to locate legacy schema (ssInfo).', window);
			return;
		}
	}
	
	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	$D().log('Preparing service call for WCM.CONTRIBUTOR.Switch', window);

	var paramsId = WCM.AddObject({id: WCM.GenerateUniqueId('SWITCHCONTENTHANDLER'), regionConfig: regionConfig});
	jsonBinder.Send(cgiUrl, $CB(WCM.CONTRIBUTOR.ShowSwitchContentWizard, paramsId));
};

//***************************************************************************

WCM.CONTRIBUTOR.ShowSwitchContentWizard = function(http, paramsId)
{
	var params = WCM.GetObject(paramsId);
	WCM.RemoveObject(paramsId);
	
	var regionConfig = null;
	
	if (params && WCM.IsValid(regionConfig = params.regionConfig))
	{
		var options = null;
		var text = http.GetResponseText();
		if (options = $J(text))
		{
			options.url = WCM.path + 'sitestudio/contentwizard/contentwizard.htm';
			options.isSwitchContent = true;
			options.blockSwitchTemplate = WCM.ToBool(regionConfig.state['blockSwitchTemplate']);
			options.callback = WCM.CONTRIBUTOR.ReloadUrlCallback;
			
			form = new WCM.SiteStudioPopupForm(options);
			form.Open();					
		}
		else
		{
			$D().error('Unable to parse configuration to JSON', window); 
		}
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.ReloadUrlCallback = function(returnValue)
{
	if (returnValue)
	{
		$D().log('Reloading page...', window);
		window.setTimeoutEx(WCM.ReloadURL, 3000);
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.SwitchRegionTemplate = function(regionConfig)
{
	$D().log('SwitchRegionTemplate', window);
	
	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_GET_PLACEHOLDER_SWITCH_CONTENT_CONFIG');
	jsonBinder.SetLocalDataValue('dDocName', regionConfig['dataFile']);
	jsonBinder.SetLocalDataValue('siteId', regionConfig['siteId']);
	jsonBinder.SetLocalDataValue('nodeId', regionConfig['nodeId']);
	jsonBinder.SetLocalDataValue('isSecondaryPage', regionConfig['isSecondaryPage']);
	jsonBinder.SetLocalDataValue('regionName', regionConfig['regionName']);

	var regionDefinition = WCM.ToString(regionConfig['regionDefinition']);
	if (WCM.IsValid( regionDefinition ) && ( WCM.ToString(regionDefinition).length > 0))
	{
		jsonBinder.SetLocalDataValue('regionDefinition', regionDefinition);
		jsonBinder.SetLocalDataValue('template', regionConfig['template']);
		jsonBinder.SetLocalDataValue('templateType', regionConfig['templateType']);
	}
	else
	{
		alert(WCM.GetString('wcmNoRegionDefinitionAssociation'));
		return;
	}
	
	var placeholderDefinition = regionConfig['placeholderDefinition'];
	var placeholderDefinitionXml = regionConfig['placeholderDefinitionXml'];
	if (WCM.IsValid(placeholderDefinition) && (WCM.ToString(placeholderDefinition).length > 0))
	{
		jsonBinder.SetLocalDataValue('placeholderDefinition', placeholderDefinition);
	}
	else if (WCM.IsValid(placeholderDefinitionXml) && (WCM.ToString(placeholderDefinitionXml).length > 0))
	{
		jsonBinder.SetLocalDataValue('placeholderDefinitionXml', placeholderDefinitionXml);
	}
	else
	{
		alert(WCM.GetString('wcmNoPlaceholderAssociations'));
		return;
	}
	
	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	
	$D().log('Preparing service call for WCM.CONTRIBUTOR.SwitchRegionTemplate', window);

	var paramsId = WCM.AddObject({id: WCM.GenerateUniqueId('SWITCHTEMPLATEHANDLER'), regionConfig: regionConfig});
	jsonBinder.Send(cgiUrl, $CB(WCM.CONTRIBUTOR.ShowSwitchRegionTemplateWizard, paramsId));
};

//***************************************************************************

WCM.CONTRIBUTOR.ShowSwitchRegionTemplateWizard = function(http, paramsId)
{
	var params = WCM.GetObject(paramsId);
	WCM.RemoveObject(paramsId);
	
	var regionConfig = null;
	
	if (params && WCM.IsValid(regionConfig = params.regionConfig))
	{
		var options = null;
		var text = http.GetResponseText();
		if (options = $J(text))
		{
			options.url = WCM.path + 'sitestudio/contentwizard/contentwizard.htm';
			options.isSwitchRegionTemplate = true;
			options.callback = WCM.CONTRIBUTOR.ReloadUrlCallback;
			
			form = new WCM.SiteStudioPopupForm(options);
			form.Open();					
		}
		else
		{
			$D().error('Unable to parse configuration to JSON', window); 
		}
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.SwitchPlaceholderDefinition = function(regionConfig)
{
	$D().log('SwitchPlaceholderDefinition', window);

	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_GET_SWITCH_PLACEHOLDER_DEFINITION_CONFIG');
	jsonBinder.SetLocalDataValue('siteId', regionConfig['siteId']);
	jsonBinder.SetLocalDataValue('nodeId', regionConfig['nodeId']);
	jsonBinder.SetLocalDataValue('isSecondaryPage', regionConfig['isSecondaryPage']);
	jsonBinder.SetLocalDataValue('regionName', regionConfig['regionName']);

	if (!WCM.ToBool(regionConfig['isLegacyRegionDefinition']))
	{
		var urlPlaceholderDefinition = regionConfig['urlPlaceholderDefinition'];
		if (WCM.IsValid(urlPlaceholderDefinition) && (WCM.ToString(urlPlaceholderDefinition).length > 0))
		{
			jsonBinder.SetLocalDataValue('placeholderDefinition', urlPlaceholderDefinition);
		}
	}
	
	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	
	$D().log('Preparing service call for WCM.CONTRIBUTOR.SwitchPlaceholderDefinition', window);

	var paramsId = WCM.AddObject({id: WCM.GenerateUniqueId('SWITCHPLACEHOLDERHANDLER'), regionConfig: regionConfig});
	jsonBinder.Send(cgiUrl, $CB(WCM.CONTRIBUTOR.ShowSwitchPlaceholderDefinitionWizard, paramsId));
};

//***************************************************************************

WCM.CONTRIBUTOR.ShowSwitchPlaceholderDefinitionWizard = function(http, paramsId)
{
	var params = WCM.GetObject(paramsId);
	WCM.RemoveObject(paramsId);
	
	var regionConfig = null;
	
	if (params && WCM.IsValid(regionConfig = params.regionConfig))
	{
		var options = null;
		var text = http.GetResponseText();		
		if (options = $J(text))
		{
			options.url = WCM.path + 'sitestudio/contentwizard/contentwizard.htm';
			options.isSwitchPlaceholderDefinition = true;
			options.callback = WCM.CONTRIBUTOR.ReloadUrlCallback;
			
			form = new WCM.SiteStudioPopupForm(options);
			form.Open();
		}
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.Usage = function(regionConfig)
{
	$D().log('Usage', window);

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	var suffix = '';
	suffix += '?IdcService=SS_GET_DOCUMENT_USAGE';
	suffix += '&dDocName=' + WCM.ToString(regionConfig['dataFile']);
	suffix += '&showSiteId=' + WCM.ToString(regionConfig['siteId']);

	// window.location.href = cgiUrl + suffix;	
	window.open(cgiUrl + suffix, '_blank');
}

//***************************************************************************

WCM.CONTRIBUTOR.Diff = function(regionConfig)
{	
	var qs = '';
	var firstUrl = '';
	var secondUrl = '';

	WCM.SetCookie('previewId', '');
	
	qs = WCM.GetQueryString(WCM.GetQueryString());
	qs = WCM.RemoveQueryStringValue('previewId', qs);
	
	qs = WCM.SetQueryStringValue('wcm.contributor.mode', 'false', qs);
	firstUrl = (WCM.GetUrlBase() + qs + WCM.GetBookmark());

	qs = WCM.SetQueryStringValue('wcm.contributor.mode', 'true', qs);
	qs = WCM.SetQueryStringValue('SSHideContributorUI', 'true', qs);
	secondUrl = (WCM.GetUrlBase() + qs + WCM.GetBookmark());
	
	var cb = $CB(WCM.SetCookie, 'wcm.contributor.mode', 'true');
	
	var differ = new WCM.Diff({firstUrl: firstUrl, secondUrl: secondUrl, callback: cb, modal: 0});
	differ.DoDiff();	
}

//***************************************************************************

WCM.CONTRIBUTOR.Tracker = function(regionConfig)
{
	$D().log('Tracker', window);

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(regionConfig['httpCgiPath']);
	var suffix = '';
	suffix += '?IdcService=SCT_GET_DOCUMENT_INFO_ADMIN';
	suffix += '&RowsPerPage=25';
	suffix += '&NumDocs=250';
	suffix += '&FromDate=' + WCM.ToString(regionConfig['fromDate']);
	suffix += '&ToDate=' + WCM.ToString(regionConfig['toDate']);
	suffix += '&SctFmtFromDate=' + WCM.ToString(regionConfig['fmtFromDate']);
	suffix += '&SctFmtToDate=' + WCM.ToString(regionConfig['fmtToDate']);
	suffix += '&defaultQuery=qSctrWSContentAccess_Drill';
	suffix += '&QueryName=qSctrWSContentAccess_Drill2';
	suffix += '&DocCriteriaValue=' + WCM.ToString(regionConfig['dataFile']);
	suffix += '&oldTitle=Web Site Content Accesses by Site ID';
	suffix += '&DocCriteriaValue2=' + WCM.ToString(regionConfig['siteId']);
	
	// window.location.href = cgiUrl + suffix;	
	window.open(cgiUrl + suffix, '_blank');
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerChooseDataFile = function(regionConfig)
{
	$D().log('DesignerChooseDataFile', window);

	try
	{
		if (window.top && window.top.WCM && window.top.WCM.AssignContent &&
			window.top.WCM.AssignContent.SwitchRegionContent)
		{
			window.top.WCM.AssignContent.SwitchRegionContent(regionConfig);
		}
		else
		if (regionConfig['isLegacyRegionDefinition'])
			window.external.ChooseDataFile(regionConfig['regionName']);
		else
			window.external.ChoosePlaceholderDataFile(regionConfig['regionName']);
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerChooseDataFileHandler = function(e, num)
{
	WCM.CONTRIBUTOR.DesignerChooseDataFile(WCM.CONTRIBUTOR.GetRegionConfig(num));
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerChooseRegionTemplate = function(regionConfig)
{
	$D().log('DesignerChooseRegionTemplate', window);

	try
	{
		if (regionConfig['isLegacyRegionDefinition'])
		{
		}
		else
		{
			if (window.top && window.top.WCM && window.top.WCM.AssignContent &&
				window.top.WCM.AssignContent.SwitchRegionTemplate)
			{
				window.top.WCM.AssignContent.SwitchRegionTemplate(regionConfig);
			}
			else
			{
				window.external.ChooseViewFile(regionConfig['regionName']);
			}
		}
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerChooseRegionTemplateHandler = function(e, num)
{
	WCM.CONTRIBUTOR.DesignerChooseRegionTemplate(WCM.CONTRIBUTOR.GetRegionConfig(num));
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerSetReplaceable = function(regionConfig)
{
	$D().log('DesignerSetReplaceable', window);

	try
	{
		if (window.top && window.top.WCM && window.top.WCM.AssignContent &&
			window.top.WCM.AssignContent.SetReplaceable)
		{
			window.top.WCM.AssignContent.SetReplaceable(regionConfig);
			return;
		}
		else
		{
			window.external.SetReplaceable(regionConfig['regionName']);
		}
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerClearReplaceable = function(regionConfig)
{
	$D().log('DesignerSetReplaceable', window);

	try
	{
		if (window.top && window.top.WCM && window.top.WCM.AssignContent &&
			window.top.WCM.AssignContent.ClearReplaceable)
		{
			window.top.WCM.AssignContent.ClearReplaceable(regionConfig);
			return;
		}
		else
		{
			window.external.ClearReplaceable(regionConfig['regionName']);
		}
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerApplyToAllNodes = function(regionConfig)
{
	$D().log('DesignerApplyToAllNodes', window);

	try
	{
		if (window.top && window.top.WCM && window.top.WCM.AssignContent &&
			window.top.WCM.AssignContent.ApplyDataFileToAllNodes)
		{
			window.top.WCM.AssignContent.ApplyDataFileToAllNodes(regionConfig);
		}
		else
		{
			window.external.ApplyToAllNodes(regionConfig['regionName']);
		}
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerGenerateUniqueDatafiles = function(regionConfig)
{
	$D().log('DesignerGenerateUniqueDatafiles', window);

	try
	{
		window.external.GenerateUniqueDatafiles(regionConfig['regionName']);
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.DesignerReportStuff = function()
{
	$D().log('DesignerReportStuff', window);

	try
	{
		var report = '';
		
		for (var regions = $GET(WCM.CONTRIBUTOR.regions); regions.next();)
		{
			var regionConfig = regions.at();

			if (regions.pos() > 0) 
				report += '&';
				
			report += regionConfig.regionName + '=' + regionConfig.dataFile;
		}
	
		window.external.ReportStuff(report);
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.ViewPrimaryPage = function()
{
	$D().log('ViewPrimaryPage', window);

	try
	{
		var qs = '';
		qs = WCM.GetQueryString(WCM.GetQueryString());
		qs = WCM.RemoveQueryStringValue('useSecondary', qs);

		var url = '';
		url = (WCM.GetUrlBaseMinusFile() + '/' + qs + WCM.GetBookmark());
		$D().log('Reloading page... ' + url, window);

		WCM.ReloadURL(url);
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.ViewSecondaryPage = function()
{
	$D().log('ViewSecondaryPage', window);

	try
	{
		var qs = '';
		qs = WCM.GetQueryString(WCM.GetQueryString());
		qs = WCM.RemoveQueryStringValue('useSecondary', qs);
		qs = WCM.SetQueryStringValue('useSecondary', '1', qs);

		var url = '';
		url = (WCM.GetUrlBase() + qs + WCM.GetBookmark());
		$D().log('Reloading page... ' + url, window);
		
		WCM.ReloadURL(url);
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.ShowPlaceholderSectionProperties = function()
{
	$D().log('ShowPlaceholderSectionProperties', window);

	try
	{
		if (window.top && window.top.WCM && window.top.WCM.AssignContent &&
			window.top.WCM.AssignContent.ShowPlaceholderSectionProperties)
		{
			var config = {};
			config.httpCgiPath = g_httpCgiPath;
			config.siteId = g_ssSourceSiteId;
			config.nodeId = g_ssSourceNodeId;
			config.isSecondaryPage = WCM.ToBool(g_ssIsSecondaryPage);
			
			window.top.WCM.AssignContent.ShowPlaceholderSectionProperties(config);
		}
		else if (window.external)
		{
			window.external.ShowPlaceholderSectionProperties();
		}
	}
	catch (e) { }
}

//***************************************************************************

WCM.CONTRIBUTOR.PublishNow = function()
{
	$D().log('PublishNow', window);

	var jsonBinder = new WCM.Idc.JSONBinder();
	jsonBinder.SetLocalDataValue('IdcService', 'SS_PUBLISH_THIS_PAGE');
	jsonBinder.SetLocalDataValue('siteId', g_ssSourceSiteId);
	jsonBinder.SetLocalDataValue('nodeId', g_ssSourceNodeId);
	jsonBinder.SetLocalDataValue('isSecondaryPage', g_ssIsSecondaryPage ? '1' : '0');
	jsonBinder.SetLocalDataValue('queryString', WCM.ToString(window.location.search));

	var cgiUrl = window.location.protocol + '//' + window.location.host + WCM.ToString(g_httpCgiPath);
	
	$D().log('Preparing service call for WCM.CONTRIBUTOR.PublishNow', window);

	jsonBinder.Send(cgiUrl, WCM.CONTRIBUTOR.PublishNowRequestCallback);
}

//***************************************************************************

WCM.CONTRIBUTOR.PublishNowRequestCallback = function(http)
{
	var options = null;
	var text = http.GetResponseText();		
	if (options = $J(text))
	{
		WCM.DHTML.Hide(WCM.CONTRIBUTOR.publishNowLinkId);
	}
	else
	{
		$D().error('Unable to parse configuration to JSON', window); 
	}
};

//***************************************************************************

WCM.CONTRIBUTOR.Initialize = function()
{
	if(WCM.CONTRIBUTOR.IsContributorMode())
	{
		WCM.CONTRIBUTOR.DrawContributorMode();
	}
}

//***************************************************************************

WCM.CONTRIBUTOR.Initialize();

//***************************************************************************




