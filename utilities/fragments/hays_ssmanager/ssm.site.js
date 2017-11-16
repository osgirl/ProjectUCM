/////////////////////////////////////////////////////////////////////////////
// 
// Solution  : SiteStudio
// Project   : Site Studio Manager (SSM)
//
// FileName  : ssm.site.js
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
//********************************** SITE ***********************************
//***************************************************************************
//***************************************************************************

SSM.Site = function(siteId)
{
    this.id = siteId;
}

//***************************************************************************

SSM.Site.prototype.IsLoaded = function()
{
    return (SSAjax.IsValid(this.projectXml) &&                      // call .SetSiteHierarchy()
            SSAjax.IsValid(this.httpSiteAddress) &&                 // call .SetSiteAddress()
            SSAjax.IsValid(this.customSectionPropertyDefinitions)); // call .SetCustomSectionProperties()
}

//***************************************************************************

SSM.Site.prototype.SetSiteHierarchy = function(siteXml)
{
    var siteXmlDom = (new DOMParser()).parseFromString(siteXml, 'text/xml');
    SSAjax.XML.SetXPathNamespaces(siteXmlDom, 'xmlns:SS="http://www.stellent.com/sitestudio/Project/"');

    this.sections = new Object(); // going to cache section objects by id as they are created

    this.projectXml = siteXmlDom.selectSingleNode('/SS:project');
    this.rootSectionId = this.projectXml.selectSingleNode('SS:section').getAttribute('nodeId');
    this.errorSectionId = this.projectXml.getAttribute('errorNodeId');
}

SSM.Site.prototype.SetSiteAddress = function(httpSiteAddress)
{
    this.httpSiteAddress = httpSiteAddress;
}

SSM.Site.prototype.SetCustomSectionPropertyDefinitions = function(customSectionPropertyDefinitions)
{
    this.customSectionPropertyDefinitions = customSectionPropertyDefinitions;
}

//***************************************************************************

SSM.Site.prototype.GetSiteProperty = function(name) { return this.projectXml.getAttribute(name); }

SSM.Site.prototype.GetRootSection = function() { return this.GetSection(this.rootSectionId); }
SSM.Site.prototype.GetErrorSection = function() { return this.GetSection(this.errorSectionId); }

SSM.Site.prototype.GetSection = function(section)
{
    if (SSAjax.IsString(section))
    {
        if (SSAjax.IsValid(this.sections[section]))
        {
            return this.sections[section];
        }
        else
        {
            var xml = this.projectXml.selectSingleNode('//SS:section[@nodeId="' + section + '"]');
            if (xml)
            {
                this.sections[section] = new SSM.Site.Section(xml);
                return this.sections[section];
            }
            else
            {
                return null;
            }
        }
    }
    else
    {
        return section; // assume it's already an XML dom node
    }
}

//***************************************************************************

SSM.Site.prototype.SectionExists = function(section)
{
    return SSAjax.IsValid(this.GetSection(section));
}

//***************************************************************************

SSM.Site.prototype.GetNextOrPreviousOrParentSection = function(section)
{
    var otherSectionXml = SSAjax.XML.GetNextOrPreviousOrParentElement(section.xml);
    if (otherSectionXml)
        return this.GetSection(otherSectionXml.getAttribute('nodeId'));
    else
        return null;
}

//***************************************************************************

SSM.Site.prototype.GetPreviousSiblingId = function(section)
{
    section = this.GetSection(section);
    if (section)
    {
        var parentSection = this.GetSection(section.parentSectionId);
        if (parentSection)
        {
            for (var i = 0 ; i < parentSection.subSections.length ; i++)
                if (parentSection.subSections[i] == section.id)
                    return (i == 0 ? null : parentSection.subSections[i-1]);
         }
    }

    return null;
}

//***************************************************************************

SSM.Site.prototype.GetLastChildSectionId = function(section)
{
    section = this.GetSection(section);
    if (section)
    {
        if (section.subSections.length > 0)
            return section.subSections[section.subSections.length-1];
        else
            return null;
    }
}

//***************************************************************************

SSM.Site.prototype.GetFriendlyUrlForSection = function(section, bIncludeFile)
{
    section = this.GetSection(section);
    bIncludeFile = SSAjax.ToBool(bIncludeFile, true);

    var file = (bIncludeFile ? (section.urlPageName.length > 0 ? section.urlPageName : 'index.htm') : '');

    if (section.id == this.rootSectionId)
    {
        return this.httpSiteAddress + file;
    }
    else
    {
        var parentSection = this.GetSection(section.parentSectionId);
        var parentUrl = this.GetFriendlyUrlForSection(parentSection, false);
        var path = (section.urlDirName.length > 0 ? section.urlDirName : section.label);

        return parentUrl + path + '/' + file;
    }
}

//***************************************************************************

SSM.Site.prototype.GetAllChildSectionIds = function(section, bRecursive)
{
    var result = new Array();
    
    section = this.GetSection(section);
    if (section)
    {
        bRecursive = SSAjax.ToBool(bRecursive, false);

        for (var i = 0 ; i < section.subSections.length ; i++)
        {
            result.push(section.subSections[i]);
            if (bRecursive)
                result = result.concat(this.GetAllChildSectionIds(section.subSections[i], true));
        }
    }
    
    return result;
}

//***************************************************************************
//***************************************************************************
//******************************** SECTION **********************************
//***************************************************************************
//***************************************************************************

SSM.Site.Section = function(sectionXml)
{
    this.xml = sectionXml;
    
    this.id = sectionXml.getAttribute('nodeId');
    this.label = SSAjax.ToString(sectionXml.getAttribute('label'));
    this.urlDirName = SSAjax.ToString(sectionXml.getAttribute('urlDirName'));
    this.urlPageName = SSAjax.ToString(sectionXml.getAttribute('urlPageName'));
    this.maxAge = SSAjax.ToString(sectionXml.getAttribute('maxAge'));
    this.active = SSAjax.ToBool(sectionXml.getAttribute('active'), false);
    this.contributorOnly = SSAjax.ToBool(sectionXml.getAttribute('contributorOnly'), false);
    this.primaryUrl = new SSM.Site.Section.Url(SSAjax.ToString(sectionXml.getAttribute('primaryUrl')));
    this.secondaryUrl = new SSM.Site.Section.Url(SSAjax.ToString(sectionXml.getAttribute('secondaryUrl')));
    this.secondaryUrlVariableField = SSAjax.ToString(sectionXml.getAttribute('secondaryUrlVariableField'));

    var subSectionsXml = sectionXml.selectNodes('SS:section');
    if (subSectionsXml.length > 0)
    {
        this.subSections = new Array(subSectionsXml.length);
        for (var i = 0 ; i < subSectionsXml.length ; i++)
            this.subSections[i] = subSectionsXml[i].getAttribute('nodeId');
    }
    else
    {
        this.subSections = new Array();
    }

    this.parentSectionId = sectionXml.parentNode.getAttribute('nodeId');
}

SSM.Site.Section.prototype.GetCustomPropertyValue = function(name)
{
    return this.xml.getAttribute(name);
}

//***************************************************************************
//***************************************************************************
//************************ PRIMARY & SECONDARY URL **************************
//***************************************************************************
//***************************************************************************

SSM.Site.Section.Url = function(url) // equivalent of CSSSiteUrl class in designer c++ code
{
    this.page = '';
    this.params = '';
    this.external = false;
    
    if (url && url.length > 0)
    {
        this.external = (url.indexOf('://') > -1);

        var pos = url.indexOf('?');
        if (pos > -1)
        {
            this.page = url.substring(0, pos);
            this.params = url.substring(pos+1, url.length);
        }
        else
        {
            this.page = url;
            this.params = '';
        }
    }
}

SSM.Site.Section.Url.prototype.ToString = function()
{
    if (this.params.length > 0)
        return this.page + '?' + this.params;
    else
        return this.page;
}
