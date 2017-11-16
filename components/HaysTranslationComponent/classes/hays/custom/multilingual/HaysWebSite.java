package hays.custom.multilingual;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;

public class HaysWebSite {
	
	public static final Pattern LOCALE_REGEX = Pattern.compile("(.+)[-_](.+)");
	
	public String websiteId = null;
	public String haysLocaleId = null;
	public String languageCode = null;
	public Locale locale = null;
	public String countryCode = null;
	public String isoCountryCode = null;
	public IdcLocale ucmLocale = null;
	public String ucmLocaleId = null;
	public String domainId = null;
	public String languageId = null;
	public String distance_unit = null;
	public String display_salary_rate = null;
	public String searchWidgetSuffix = null;
	public String countryCordinates = null;
	public String countryName = null;
	public String searchFacetsSuffix = null;
	public String websiteDateFormat = null;
	public HaysWebSite primaryWebsite = null;
	public String display_postcode = null;
	public String languageLabel = null;
	public String leftNavInclude = null;
	public String jobtype_permanent = null;
	public String jobtype_temporary = null;
	public String jobtype_contract = null;
	public String jobtype_widget_suffix = null;
	public String jobtype_currency_pos = null;
	public String country_region = null;
	public String salary_range = null;
	public String location_column = null;
	public String countryNameISO = null;
	public String portalURL = null;
	List<HaysWebSite> secondaryWebsites = new ArrayList<HaysWebSite>();

	
	
	public HaysWebSite(String localeId, String siteId, String ucmLocaleId,String domainId,String languageId,String distance_unit,String diaplaySalaryRate,String isoCountryCode,
			String searchWidgetSuffix,String countryCordinates, String countryName,String searchFacetsSuffix,String websiteDateFormat,String display_postcode,String languageLabel,
			String languageCode,String leftNavInclude,String jobtype_permanent,String jobtype_temporary,String jobtype_contract,String jobtype_widget_suffix,String jobtype_currency_pos,
			String country_region,String salary_range,String location_column, String countryNameISO, String portalURL) {
		this.haysLocaleId = localeId;
		this.websiteId = siteId;
		Matcher matcher = LOCALE_REGEX.matcher(haysLocaleId);
	    if (matcher.find() && matcher.groupCount() > 1) {
	    	this.languageCode = languageCode;
	    	this.countryCode = matcher.group(2);
	    	this.locale = new Locale(languageCode, countryCode);
	    }
	    this.ucmLocaleId = ucmLocaleId;
	    this.ucmLocale = LocaleResources.getLocale(ucmLocaleId);
	    this.domainId = domainId;
	    this.languageId = languageId;
	    this.distance_unit = distance_unit;
	    this.display_salary_rate = diaplaySalaryRate;
	    this.isoCountryCode = isoCountryCode;
	    this.searchWidgetSuffix = searchWidgetSuffix;
	    this.countryCordinates = countryCordinates;
	    this.countryName = countryName;
	    this.searchFacetsSuffix = searchFacetsSuffix;
	    this.websiteDateFormat = websiteDateFormat;
	    this.display_postcode= display_postcode;
	    this.languageLabel= languageLabel;
	    this.leftNavInclude= leftNavInclude;
	    this.jobtype_permanent=jobtype_permanent;
	    this.jobtype_temporary=jobtype_temporary;
	    this.jobtype_contract=jobtype_contract;
	    this.jobtype_widget_suffix=jobtype_widget_suffix;
	    this.jobtype_currency_pos=jobtype_currency_pos;
	    this.country_region = country_region;
	    this.salary_range = salary_range;
	    this.location_column  = location_column;
	    this.countryNameISO = countryNameISO;
	    this.portalURL = portalURL;
	    
	}
	
	public void setPrimarySite(HaysWebSite site) {
		this.primaryWebsite = site;
	}
	
	public void addSecondarySite(HaysWebSite site) {
		//SystemUtils.trace("Translation", "add sec site: " + site);	
		if( site != null) {
			secondaryWebsites.add(site);
		}
			
	}
	
	public boolean isPrimarySite () {
		return (primaryWebsite == null)? true : false;
	}
	
	public String toString() {
		StringBuffer str = new StringBuffer("\n\nWEBSITE: ");
		str.append("\nsiteId = ").append(this.websiteId).append("\nlocaleId = ").append(this.haysLocaleId);
		str.append("\nlanguage = ").append(this.languageCode).append("\\ncountry = ").append(this.countryCode);
		str.append("\nlanguageID = ").append(this.languageId).append("\\domainId = ").append(this.domainId);
		str.append("\nDistance Unit = ").append(this.distance_unit);
		str.append("\nsearch_widget_suffix = ").append(this.searchWidgetSuffix);
		str.append("\nUCM locale = ").append(this.ucmLocale);
		str.append("\nprimarySite = ");
		str.append("\n country_region = ").append(this.country_region);
		str.append("\n salary_range = ").append(this.salary_range);
		str.append("\n location_column = ").append(this.location_column);
		if( this.primaryWebsite == null)
			str.append(" NULL");
		else
			str.append(this.primaryWebsite.websiteId);
		str.append("\n portal URL = ").append(this.portalURL);
		str.append("\nsecond sites = ").append(this.secondaryWebsites.size());
		return str.toString();
	}

}
