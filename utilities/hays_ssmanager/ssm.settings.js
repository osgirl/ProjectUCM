/////////////////////////////////////////////////////////////////////////////
// 
// Solution  : SiteStudio
// Project   : Site Studio Manager (SSM)
//
// FileName  : ssm.settings.js
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
//***************************************************************************
//******************************** SETTINGS *********************************
//***************************************************************************
//***************************************************************************

SSM.Settings = function(xml)
{
    if (xml)
    {
        this.general = new SSM.Settings.General(xml.selectSingleNode('ssm:general'));
        this.addSection = new SSM.Settings.AddSection(xml.selectSingleNode('ssm:addSection'));
        this.removeSection = new SSM.Settings.RemoveSection(xml.selectSingleNode('ssm:removeSection'));
        this.moveSection = new SSM.Settings.MoveSection(xml.selectSingleNode('ssm:moveSection'));
        this.setErrorHandler = new SSM.Settings.SetErrorHandler(xml.selectSingleNode('ssm:setErrorHandler'));
        this.editProperties = new SSM.Settings.EditProperties(xml.selectSingleNode('ssm:editProperties'));
        this.editCustomProperties = new SSM.Settings.EditCustomProperties(xml.selectSingleNode('ssm:editCustomProperties'));
        this.primaryLayout = new SSM.Settings.PrimaryLayout(xml.selectSingleNode('ssm:primaryLayout'));
        this.secondaryLayout = new SSM.Settings.SecondaryLayout(xml.selectSingleNode('ssm:secondaryLayout'));

        this.sectionOverrides = new Object();
        var sectionOverrides = xml.selectNodes('ssm:sectionOverride');
        for (var i = 0 ; i < sectionOverrides.length ; i++)
        {
            var sectionId = sectionOverrides[i].getAttribute('nodeId');
            this.sectionOverrides[sectionId] = new SSM.Settings.Override(sectionOverrides[i], this);
        }
    }
    else
    {
        this.general = new SSM.Settings.General(null);
        this.addSection = new SSM.Settings.AddSection(null);
        this.removeSection = new SSM.Settings.RemoveSection(null);
        this.moveSection = new SSM.Settings.MoveSection(null);
        this.setErrorHandler = new SSM.Settings.SetErrorHandler(null);
        this.editProperties = new SSM.Settings.EditProperties(null);
        this.editCustomProperties = new SSM.Settings.EditCustomProperties(null);
        this.primaryLayout = new SSM.Settings.PrimaryLayout(null);
        this.secondaryLayout = new SSM.Settings.SecondaryLayout(null);

        this.sectionOverrides = new Object();
    }
}

SSM.Settings.Override = function(xml, base)
{
    this.addSection = new SSM.Settings.AddSection(xml.selectSingleNode('ssm:addSection'), base.addSection);
    this.removeSection = new SSM.Settings.RemoveSection(xml.selectSingleNode('ssm:removeSection'), base.removeSection);
    this.moveSection = new SSM.Settings.MoveSection(xml.selectSingleNode('ssm:moveSection'), base.moveSection);
    this.setErrorHandler = new SSM.Settings.SetErrorHandler(xml.selectSingleNode('ssm:setErrorHandler'), base.setErrorHandler);
    this.editProperties = new SSM.Settings.EditProperties(xml.selectSingleNode('ssm:editProperties'), base.editProperties);
    this.editCustomProperties = new SSM.Settings.EditCustomProperties(xml.selectSingleNode('ssm:editCustomProperties'), base.editCustomProperties);
    this.primaryLayout = new SSM.Settings.PrimaryLayout(xml.selectSingleNode('ssm:primaryLayout'), base.primaryLayout);
    this.secondaryLayout = new SSM.Settings.SecondaryLayout(xml.selectSingleNode('ssm:secondaryLayout'), base.secondaryLayout);
}

SSM.Settings.prototype.forSection = function(sectionId)
{
    return (this.sectionOverrides[sectionId] ? this.sectionOverrides[sectionId] : this);
}

//***************************************************************************

SSM.Settings.General = function(xml)
{
    this.contributorOnly = __BoolNodeAttr(xml, 'contributorOnly', false);
    this.autoManage = __BoolNodeAttr(xml, 'autoManage', false);
    this.displayConsole = __BoolNodeAttr(xml, 'displayConsole', false);
    this.updateTitle = __BoolNodeAttr(xml, 'updateTitle', true);
    this.updateUrl = __BoolNodeAttr(xml, 'updateUrl', true);

    if (document.location.href.indexOf('ssm.trace') >= 0)
        this.displayConsole = true;

    if ((document.location.hash.indexOf('ssm.action') >= 0) ||
        (document.location.hash.indexOf('ssm.section') >= 0))
        this.autoManage = true;
        
    var hierarchy = __NodeAttr(xml, 'hierarchy', 'showAll');

    if (String.CompareNoCase(hierarchy, 'hide'))
    {
        this.hierarchyHidden = true;
        this.hierarchyStartAtCurrentSection = true;
    }
    else if (String.CompareNoCase(hierarchy, 'showCurrentSectionOnly'))
    {
        this.hierarchyHidden = false;
        this.hierarchyStartAtCurrentSection = true;
    }
    else
    {
        this.hierarchyHidden = false;
        this.hierarchyStartAtCurrentSection = false;
    }
}

//***************************************************************************

SSM.Settings.AddSection = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isActionItemHidden = __BoolNodeAttr(xml, 'actionItemHidden', (base ? base.isActionItemHidden : false));

    this.isUrlDirNameHidden = __BoolChildNodeAttr(xml, 'ssm:urlDirName', 'hidden', (base ? base.isUrlDirNameHidden : false));
    this.isUrlDirNameDisabled = __BoolChildNodeAttr(xml, 'ssm:urlDirName', 'disabled', (base ? base.isUrlDirNameDisabled : false));
    
    this.isIdHidden = __BoolChildNodeAttr(xml, 'ssm:id', 'hidden', (base ? base.isIdHidden : false));
    this.isIdDisabled = __BoolChildNodeAttr(xml, 'ssm:id', 'disabled', (base ? base.isIdDisabled : false));
    this.isIdDefaultToManual = __BoolChildNodeAttr(xml, 'ssm:id', 'defaultToManual', (base ? base.isIdDefaultToManual : false));
    
    this.defaultPrimaryUrl = __ChildNodeAttr(xml, 'ssm:primaryUrl', 'default', (base ? base.defaultPrimaryUrl : ''));
    this.defaultSecondaryUrl = __ChildNodeAttr(xml, 'ssm:secondaryUrl', 'default', (base ? base.defaultSecondaryUrl : ''));
    this.defaultSecondaryUrlVariableField = __ChildNodeAttr(xml, 'ssm:secondaryUrlVariableField', 'default', (base ? base.defaultSecondaryUrlVariableField : ''));

    this.isInheritingPrimaryUrl = __BoolChildNodeAttr(xml, 'ssm:primaryUrl', 'inherit', (base ? base.isInheritingPrimaryUrl : true));
    this.isInheritingSecondaryUrl = __BoolChildNodeAttr(xml, 'ssm:secondaryUrl', 'inherit', (base ? base.isInheritingSecondaryUrl : true));
    this.isInheritingSecondaryUrlVariableField = __BoolChildNodeAttr(xml, 'ssm:secondaryUrlVariableField', 'inherit', (base ? base.isInheritingSecondaryUrlVariableField : true));
}

//***************************************************************************

SSM.Settings.RemoveSection = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isActionItemHidden = __BoolNodeAttr(xml, 'actionItemHidden', (base ? base.isActionItemHidden : false));
}

//***************************************************************************

SSM.Settings.MoveSection = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isActionItemHidden = __BoolNodeAttr(xml, 'actionItemHidden', (base ? base.isActionItemHidden : false));
    
    this.canBeSource = __BoolNodeAttr(xml, 'source', (base ? base.canBeSource : true));
    this.canBeTarget = __BoolNodeAttr(xml, 'target', (base ? base.canBeTarget : true));
}

//***************************************************************************

SSM.Settings.SetErrorHandler = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isActionItemHidden = __BoolNodeAttr(xml, 'actionItemHidden', (base ? base.isActionItemHidden : false));
}

//***************************************************************************

SSM.Settings.EditProperties = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isActionItemHidden = __BoolNodeAttr(xml, 'actionItemHidden', (base ? base.isActionItemHidden : false));

    this.isIdHidden = __BoolChildNodeAttr(xml, 'ssm:id', 'hidden', (base ? base.isIdHidden : false));
    this.isIdDisabled = __BoolChildNodeAttr(xml, 'ssm:id', 'disabled', (base ? base.isIdDisabled : false));
    this.isLabelHidden = __BoolChildNodeAttr(xml, 'ssm:label', 'hidden', (base ? base.isLabelHidden : false));
    this.isLabelDisabled = __BoolChildNodeAttr(xml, 'ssm:label', 'disabled', (base ? base.isLabelDisabled : false));
    this.isActiveHidden = __BoolChildNodeAttr(xml, 'ssm:active', 'hidden', (base ? base.isActiveHidden : false));
    this.isActiveDisabled = __BoolChildNodeAttr(xml, 'ssm:active', 'disabled', (base ? base.isActiveDisabled : false));
    this.isContributorOnlyHidden = __BoolChildNodeAttr(xml, 'ssm:contributorOnly', 'hidden', (base ? base.isContributorOnlyHidden : false));
    this.isContributorOnlyDisabled = __BoolChildNodeAttr(xml, 'ssm:contributorOnly', 'disabled', (base ? base.isContributorOnlyDisabled : false));
    this.isUrlDirNameHidden = __BoolChildNodeAttr(xml, 'ssm:urlDirName', 'hidden', (base ? base.isUrlDirNameHidden : false));
    this.isUrlDirNameDisabled = __BoolChildNodeAttr(xml, 'ssm:urlDirName', 'disabled', (base ? base.isUrlDirNameDisabled : false));
    this.isUrlPageNameHidden = __BoolChildNodeAttr(xml, 'ssm:urlPageName', 'hidden', (base ? base.isUrlPageNameHidden : false));
    this.isUrlPageNameDisabled = __BoolChildNodeAttr(xml, 'ssm:urlPageName', 'disabled', (base ? base.isUrlPageNameDisabled : false));
    this.isMaxAgeHidden = __BoolChildNodeAttr(xml, 'ssm:maxAge', 'hidden', (base ? base.isMaxAgeHidden : false));
    this.isMaxAgeDisabled = __BoolChildNodeAttr(xml, 'ssm:maxAge', 'disabled', (base ? base.isMaxAgeDisabled : false));
}

//***************************************************************************

SSM.Settings.EditCustomProperties = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isActionItemHidden = __BoolNodeAttr(xml, 'actionItemHidden', (base ? base.isActionItemHidden : false));
}

//***************************************************************************

SSM.Settings.PrimaryLayout = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isPreviewHidden = __BoolNodeAttr(xml, 'previewHidden', (base ? base.isPreviewHidden : false));
    this.isExternalHidden = __BoolNodeAttr(xml, 'externalHidden', (base ? base.isExternalHidden : false));
    
    this.presentation = __ChildNodeCDATA(xml, 'ssm:presentation', (base ? base.presentation : '$dDocTitle$'));
    this.limitScope = __BoolChildNodeAttr(xml, 'ssm:queryText', 'limitScope', (base ? base.limitScope : true));
    this.queryText = __ChildNodeCDATA(xml, 'ssm:queryText', (base ? base.queryText : ''));
}

//***************************************************************************

SSM.Settings.SecondaryLayout = function(xml, base)
{
    this.isHidden = __BoolNodeAttr(xml, 'hidden', (base ? base.isHidden : false));
    this.isPreviewHidden = __BoolNodeAttr(xml, 'previewHidden', (base ? base.isPreviewHidden : false));
    
    this.presentation = __ChildNodeCDATA(xml, 'ssm:presentation', (base ? base.presentation : '$dDocTitle$'));
    this.limitScope = __BoolChildNodeAttr(xml, 'ssm:queryText', 'limitScope', (base ? base.limitScope : true));
    this.queryText = __ChildNodeCDATA(xml, 'ssm:queryText', (base ? base.queryText : ''));
}

//***************************************************************************
//***************************************************************************
//***************************************************************************
//***************************************************************************
//***************************************************************************

__NodeAttr = function(nodeXml, attr, def)
{
    if (nodeXml)
    {
        var result = nodeXml.getAttribute(attr);
        if (result && (result.length > 0))
        {
            return result;
        }
    }
    
    return def;
}

__NodeCDATA = function(nodeXml, def)
{
    if (nodeXml)
    {
        for (var i = 0 ; i < nodeXml.childNodes.length ; i++)
        {
            if (nodeXml.childNodes[i].nodeType == Node.CDATA_SECTION_NODE)
            {
                return nodeXml.childNodes[i].nodeValue;
            }
        }
    }
    
    return def;
}

__BoolNodeAttr = function(nodeXml, attr, def) { return SSAjax.ToBool(__NodeAttr(nodeXml, attr, def)); }
__MatchNodeAttr = function(value, nodeXml, attr, def) { return String.CompareNoCase(value, __NodeAttr(nodeXml, attr, def)); }


__ChildNode = function(nodeXml, childXPath) { return (nodeXml ? nodeXml.selectSingleNode(childXPath) : null); }
__ChildNodeCDATA = function(nodeXml, childXPath, def) { return __NodeCDATA(__ChildNode(nodeXml, childXPath), def); }
__ChildNodeAttr = function(nodeXml, childXPath, attr, def) { return __NodeAttr(__ChildNode(nodeXml, childXPath), attr, def); }
__BoolChildNodeAttr = function(nodeXml, childXPath, attr, def) { return __BoolNodeAttr(__ChildNode(nodeXml, childXPath), attr, def); }
__MatchChildNodeAttr = function(value, nodeXml, childXPath, attr, def) { return __MatchNodeAttr(value, __ChildNode(nodeXml, childXPath), attr, def); }

//***************************************************************************
//***************************************************************************
//******************************** DEBUGGING ********************************
//***************************************************************************
//***************************************************************************

SSM.Settings.prototype.toString = function()
{
    var result = new Array();
    result.push(this.general.toString());
    result.push(this.addSection.toString());
    result.push(this.removeSection.toString());
    result.push(this.moveSection.toString());
    result.push(this.setErrorHandler.toString());
    result.push(this.editProperties.toString());
    result.push(this.primaryLayout.toString());
    result.push(this.secondaryLayout.toString());
    for (var i in this.sectionOverrides)
    {
        result.push('.SECTION OVERRIDE ' + i);
        result.push(this.sectionOverrides[i].toString());
    }

    return result.join('\n');        
}

SSM.Settings.Override.prototype.toString = function()
{
    var result = new Array();
    result.push(this.addSection.toString());
    result.push(this.removeSection.toString());
    result.push(this.moveSection.toString());
    result.push(this.setErrorHandler.toString());
    result.push(this.editProperties.toString());
    result.push(this.primaryLayout.toString());
    result.push(this.secondaryLayout.toString());
    return result.join('\n');        
}

SSM.Settings.General.prototype.toString = function() { return SSM.Settings.DumpObject('general', this); }
SSM.Settings.AddSection.prototype.toString = function() { return SSM.Settings.DumpObject('addSection', this); }
SSM.Settings.RemoveSection.prototype.toString = function() { return SSM.Settings.DumpObject('removeSection', this); }
SSM.Settings.MoveSection.prototype.toString = function() { return SSM.Settings.DumpObject('moveSection', this); }
SSM.Settings.SetErrorHandler.prototype.toString = function() { return SSM.Settings.DumpObject('setErrorHandler', this); }
SSM.Settings.EditProperties.prototype.toString = function() { return SSM.Settings.DumpObject('editProperties', this); }
SSM.Settings.PrimaryLayout.prototype.toString = function() { return SSM.Settings.DumpObject('primaryLayout', this); }
SSM.Settings.SecondaryLayout.prototype.toString = function() { return SSM.Settings.DumpObject('secondaryLayout', this); }

SSM.Settings.DumpObject = function(type, obj)
{
    var result = new Array();
    result.push('.' + type);
    for (var i in obj)
    {
        if (!SSAjax.IsFunction(obj[i]))
            result.push('\t' + i + ': ' + obj[i]);
    }
    return result.join('\n');
}
