package oracletextsearch.indexer;

import intradoc.common.IdcStringBuilder;
import intradoc.common.Report;
import intradoc.common.StringUtils;
import intradoc.indexer.OracleTextUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HaysOracleTextUtils extends OracleTextUtils{
	public static IdcStringBuilder buildOtsMeta(Properties props, List<String> textSdataFields, String[] drillDownFields, Map zoneFields, boolean alwaysUpperCase, String defaultValue, String trueValue)
	  {
	    return buildOtsMeta(props, textSdataFields, drillDownFields, zoneFields, alwaysUpperCase, defaultValue, trueValue, 249);
	  }

	  public static IdcStringBuilder buildOtsMeta(Properties props, List<String> textSdataFields, String[] drillDownFields, Map zoneFields, boolean alwaysUpperCase, String defaultValue, String trueValue, int maxSdataSize)
	   {
	     IdcStringBuilder drillDownTag = constructDrillDownTagEx(props, drillDownFields);
	 
	     IdcStringBuilder otsMeta = buildOtsMeta(props, textSdataFields, drillDownTag, zoneFields, alwaysUpperCase, defaultValue, trueValue, maxSdataSize);
	     return otsMeta;
	   }
	 
	   public static IdcStringBuilder buildOtsMeta(Properties props, List<String> textSdataFields, IdcStringBuilder drillDownTag, Map zoneFields, boolean alwaysUpperCase, String defaultValue, String trueValue, int maxSdataSize){
	   
    //public static IdcStringBuilder buildOtsMeta(Properties props, List textSdataFields, String drillDownFields[], Map zoneFields, boolean alwaysUpperCase, String defaultValue, String trueValue) {
        String escapeMap[][] = {
            {
                "\\", "\\\\"
            }, {
                "\n", "\\n"
            }
        };
        IdcStringBuilder builder = new IdcStringBuilder();
        for (Iterator i$ = textSdataFields.iterator(); i$.hasNext(); builder.append(">")) {
            String textField = (String)i$.next();
            String value = props.getProperty(textField);
            if (value == null || value.length() == 0) {
                value = defaultValue;
            } else
            if (alwaysUpperCase) {
                value = value.toUpperCase();
            }
            builder.append("<sd");
            builder.append(textField);
            builder.append(">");
            int charwidth = (value.getBytes().length + value.length() - 1) / value.length();
            String escapedValue = StringUtils.escapeCharArray(value.toCharArray(), escapeMap).toString();
            while (escapedValue.getBytes().length > maxSdataSize)
            {
              int size = (maxSdataSize - (escapedValue.getBytes().length - value.getBytes().length)) / charwidth;
              if (size > 0)
              {
                value = value.substring(0, size);
                escapedValue = StringUtils.escapeCharArray(value.toCharArray(), escapeMap).toString();
              }
              else
              {
                escapedValue = escapedValue.substring(0, maxSdataSize / charwidth);
              }
            }

            builder.append(escapedValue);
            builder.append("</sd");
            builder.append(textField);
        }

        builder.append((new StringBuilder()).append("<counter>").append(System.currentTimeMillis()).toString());
        builder.append("</counter>");
        builder.append(trueValue);
        
        /*if ((drillDownTag != null) && (drillDownTag.length() > 0))
        {
             builder.append(drillDownTag);
        }
        */
   
        for (Iterator i$ = zoneFields.keySet().iterator(); i$.hasNext(); builder.append(">")) {
            String key = (String)i$.next();
            String value = props.getProperty((new StringBuilder()).append("z").append(key).toString());
            if (value == null || value.trim().length() == 0) {
                value = defaultValue;
            }
            builder.append("<z");
            builder.append(key);
            builder.append(">");
            builder.append(value);
            builder.append("</z");
            builder.append(key);
        }
        Report.trace("indexer", "\nprepareIndexDoc() builder:" + builder, null);
        return builder;
    }


}
