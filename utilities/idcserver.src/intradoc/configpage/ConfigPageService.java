/*     */ package intradoc.configpage;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConfigPageService extends Service
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void loadOptions()
/*     */     throws ServiceException, DataException
/*     */   {
/*  36 */     String osName = EnvUtils.getOSName();
/*  37 */     String osFamily = EnvUtils.getOSFamily();
/*  38 */     this.m_binder.putLocal("OS", osName);
/*  39 */     this.m_binder.putLocal("PLATFORM", osName);
/*  40 */     this.m_binder.putLocal("OSFAMILY", osFamily);
/*  41 */     this.m_binder.putLocal("PLATFORMFAMILY", osFamily);
/*     */ 
/*  43 */     DataResultSet cfgPageData = SharedObjects.getTable("ConfigPageData");
/*  44 */     String[] colFlds = { "colKey", "colPageTitleName", "colPageDescription", "colHelpIDTag", "colTblName", "colDataFile", "colCustomDataLoaderSubService" };
/*     */ 
/*  48 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(cfgPageData, colFlds, true);
/*  49 */     int colKeyIndx = finfo[0].m_index;
/*  50 */     int colPageTitleNameIndx = finfo[1].m_index;
/*  51 */     int colPageDescriptionIndx = finfo[2].m_index;
/*  52 */     int colHelpIDTagIndx = finfo[3].m_index;
/*  53 */     int colTblNameIndx = finfo[4].m_index;
/*  54 */     int colDataFileIndx = finfo[5].m_index;
/*  55 */     int colCustomDataLoaderSubServiceIndx = finfo[6].m_index;
/*     */ 
/*  57 */     String configData = this.m_binder.get("ConfigData");
/*  58 */     DataResultSet colSet = SharedObjects.getTable("ConfigPageData");
/*  59 */     Vector configPageRow = colSet.findRow(colKeyIndx, configData);
/*  60 */     if (configPageRow == null)
/*     */     {
/*  62 */       throw new DataException(LocaleUtils.encodeMessage("csNoConfigurationPageDefinedForOption", null, configData));
/*     */     }
/*     */ 
/*  67 */     String pageElementsTblName = (String)configPageRow.elementAt(colTblNameIndx);
/*  68 */     DataResultSet pageElements = SharedObjects.getTable(pageElementsTblName);
/*  69 */     if (pageElements == null)
/*     */     {
/*  71 */       throw new DataException("!csConfigurationOptionsTableNotDefined");
/*     */     }
/*     */ 
/*  75 */     String cfgFile = (String)configPageRow.elementAt(colDataFileIndx);
/*  76 */     Properties curOptionVals = ConfigPageUtils.loadConfigOptions(cfgFile, pageElements);
/*     */ 
/*  78 */     String colCustomDataLoaderSubService = (String)configPageRow.elementAt(colCustomDataLoaderSubServiceIndx);
/*  79 */     if ((colCustomDataLoaderSubService != null) && (colCustomDataLoaderSubService.length() > 0))
/*     */     {
/*  82 */       setCachedObject("CurrentConfigOptions-" + configData, curOptionVals);
/*  83 */       executeService(colCustomDataLoaderSubService);
/*     */     }
/*     */ 
/*  87 */     String[] cfdFlds = { "cfdName", "cfdType", "cfdOptionsTable", "cfdElementsDisableOnTrue", "cfdElementsDisableOnFalse" };
/*     */ 
/*  89 */     FieldInfo[] finfo2 = ResultSetUtils.createInfoList(pageElements, cfdFlds, true);
/*  90 */     int cfdNameIndx = finfo2[0].m_index;
/*  91 */     int cfdTypeIndx = finfo2[1].m_index;
/*  92 */     int cfdOptTblIndx = finfo2[2].m_index;
/*  93 */     int cfdDisableOnTrueIndx = finfo2[3].m_index;
/*  94 */     int cfdDisableOnFalseIndx = finfo2[4].m_index;
/*     */ 
/*  96 */     for (int i = 0; i < pageElements.getNumRows(); ++i)
/*     */     {
/*  98 */       Vector row = pageElements.getRowValues(i);
/*  99 */       String cfdName = (String)row.elementAt(cfdNameIndx);
/* 100 */       if ((cfdName == null) || (cfdName.length() <= 0))
/*     */         continue;
/* 102 */       String cfdType = ((String)row.elementAt(cfdTypeIndx)).toUpperCase();
/* 103 */       if (cfdType.indexOf("COMBO") >= 0)
/*     */       {
/* 105 */         String cfdOptTblName = (String)row.elementAt(cfdOptTblIndx);
/* 106 */         DataResultSet comboTbl = SharedObjects.getTable(cfdOptTblName);
/* 107 */         this.m_binder.addResultSet(cfdOptTblName, comboTbl);
/*     */       }
/* 109 */       String cfdElementsDisableOnTrue = (String)row.elementAt(cfdDisableOnTrueIndx);
/* 110 */       String cfdElementsDisableOnFalse = (String)row.elementAt(cfdDisableOnFalseIndx);
/* 111 */       computeDependantElementsDisable(cfdName, cfdElementsDisableOnTrue, curOptionVals, true);
/* 112 */       computeDependantElementsDisable(cfdName, cfdElementsDisableOnFalse, curOptionVals, false);
/*     */     }
/*     */ 
/* 119 */     String helpPage = (String)configPageRow.elementAt(colHelpIDTagIndx);
/* 120 */     if (helpPage != null)
/*     */     {
/* 122 */       this.m_binder.putLocal("configQuickHelpKey", helpPage);
/*     */     }
/* 124 */     String colPageTitleName = (String)configPageRow.elementAt(colPageTitleNameIndx);
/* 125 */     String colPageDescription = (String)configPageRow.elementAt(colPageDescriptionIndx);
/* 126 */     curOptionVals.put("colPageTitleName", colPageTitleName);
/* 127 */     curOptionVals.put("colPageDescription", colPageDescription);
/*     */ 
/* 129 */     DataBinder configBinder = new DataBinder();
/* 130 */     configBinder.addResultSet("ConfigurationFlagsData", pageElements);
/* 131 */     configBinder.setLocalData(curOptionVals);
/* 132 */     this.m_binder.merge(configBinder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void saveOptions() throws ServiceException, DataException, FileNotFoundException
/*     */   {
/* 138 */     boolean skipSave = StringUtils.convertToBool(this.m_binder.getAllowMissing("SkipSaveOptions"), false);
/*     */ 
/* 141 */     DataResultSet cfgPageData = SharedObjects.getTable("ConfigPageData");
/* 142 */     String[] colFlds = { "colKey", "colTblName", "colDataFile", "colCustomPostSubService" };
/*     */ 
/* 144 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(cfgPageData, colFlds, true);
/* 145 */     int colKeyIndx = finfo[0].m_index;
/* 146 */     int colTblNameIndx = finfo[1].m_index;
/* 147 */     int colDataFileIndx = finfo[2].m_index;
/* 148 */     int colCustomPostSubServiceIndx = finfo[3].m_index;
/*     */ 
/* 150 */     String configData = this.m_binder.get("ConfigData");
/* 151 */     DataResultSet colSet = SharedObjects.getTable("ConfigPageData");
/* 152 */     Vector configPageRow = colSet.findRow(colKeyIndx, configData);
/* 153 */     if (configPageRow == null)
/*     */     {
/* 155 */       throw new DataException(LocaleUtils.encodeMessage("csNoConfigurationPageDefinedForOption", null, configData));
/*     */     }
/*     */ 
/* 159 */     String pageElementsTblName = (String)configPageRow.elementAt(colTblNameIndx);
/*     */ 
/* 161 */     Properties props = new Properties();
/* 162 */     Vector order = new IdcVector();
/* 163 */     computeOptionFromHtmlElements(pageElementsTblName, props, order);
/*     */ 
/* 165 */     String colCustomPostSubService = (String)configPageRow.elementAt(colCustomPostSubServiceIndx);
/* 166 */     if ((colCustomPostSubService != null) && (colCustomPostSubService.length() > 0))
/*     */     {
/* 168 */       Object[] params = { props, order };
/* 169 */       setCachedObject("CustomConfigPostParams-" + configData, params);
/* 170 */       executeService(colCustomPostSubService);
/*     */     }
/*     */ 
/* 173 */     String file = (String)configPageRow.elementAt(colDataFileIndx);
/* 174 */     if (!skipSave)
/*     */     {
/* 176 */       ConfigPageUtils.saveConfigOptions(file, props, order);
/* 177 */       ConfigPageUtils.mergeConfigDataToSharedObjects(props);
/*     */     }
/*     */     else
/*     */     {
/* 181 */       Object[] params = { props, order, file };
/* 182 */       setCachedObject("CustomConfigPostParams-" + configData, params);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeOptionFromHtmlElements(String pageElementsTblName, Properties props, Vector order)
/*     */     throws DataException
/*     */   {
/* 189 */     DataResultSet pageElements = SharedObjects.getTable(pageElementsTblName);
/*     */ 
/* 191 */     if (pageElements == null)
/*     */     {
/* 193 */       throw new DataException("!csConfigurationOptionsTableNotDefined");
/*     */     }
/* 195 */     String[] flds = { "cfdName", "cfdDefault" };
/* 196 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(pageElements, flds, true);
/* 197 */     int nameIndex = finfo[0].m_index;
/* 198 */     int defValIndex = finfo[1].m_index;
/* 199 */     for (int i = 0; i < pageElements.getNumRows(); ++i)
/*     */     {
/* 201 */       Vector row = pageElements.getRowValues(i);
/* 202 */       String cfdName = (String)row.elementAt(nameIndex);
/* 203 */       if ((cfdName == null) || (cfdName.length() <= 0) || (cfdName.equals("null"))) {
/*     */         continue;
/*     */       }
/* 206 */       String newVal = this.m_binder.getLocal(cfdName);
/* 207 */       String defVal = (String)row.elementAt(defValIndex);
/* 208 */       if (newVal == null)
/*     */       {
/* 212 */         newVal = defVal;
/*     */       }
/* 214 */       order.addElement(cfdName);
/* 215 */       props.put(cfdName, newVal);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String cleanConversionNameForWeb(String conversionName)
/*     */   {
/* 222 */     return "conversion_" + conversionName.replace(' ', '_');
/*     */   }
/*     */ 
/*     */   protected void computeDependantElementsDisable(String parentElementName, String elementsToChange, Properties localData, boolean baseLine)
/*     */   {
/* 232 */     if ((elementsToChange == null) || (elementsToChange.length() == 0))
/*     */     {
/* 234 */       return;
/*     */     }
/*     */ 
/* 237 */     String valStr = localData.getProperty(parentElementName);
/* 238 */     boolean curVal = StringUtils.convertToBool(valStr, false);
/* 239 */     if (curVal != baseLine) {
/*     */       return;
/*     */     }
/* 242 */     Vector disList = StringUtils.parseArray(elementsToChange, ',', ',');
/* 243 */     for (int d = 0; d < disList.size(); ++d)
/*     */     {
/* 245 */       String disableName = (String)disList.elementAt(d);
/* 246 */       localData.put(disableName + ":disable", "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 253 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95030 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.configpage.ConfigPageService
 * JD-Core Version:    0.5.4
 */