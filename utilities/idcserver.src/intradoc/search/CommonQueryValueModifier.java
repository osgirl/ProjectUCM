/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class CommonQueryValueModifier
/*     */ {
/*  36 */   public static int KEY_INDEX = 0;
/*  37 */   public static int FUNCTIONS_INDEX = 1;
/*  38 */   public static int CLASSNAME_INDEX = 2;
/*     */ 
/*  40 */   public static boolean m_isInitialized = false;
/*     */   public static Map m_queryActionMap;
/*  48 */   public static String[][] m_queryActionsTable = { { "escapeReservedChars", "0", "" }, { "escapeReservedWord", "1", "" } };
/*     */ 
/*     */   public static void initializeQueryActionTable()
/*     */   {
/*  59 */     m_queryActionMap = new HashMap();
/*     */ 
/*  61 */     for (int actionNo = 0; actionNo < m_queryActionsTable.length; ++actionNo)
/*     */     {
/*  63 */       String key = m_queryActionsTable[actionNo][KEY_INDEX];
/*     */ 
/*  65 */       String functionIndexes = m_queryActionsTable[actionNo][FUNCTIONS_INDEX];
/*  66 */       List functionIndexList = StringUtils.makeListFromSequenceSimple(functionIndexes);
/*     */ 
/*  68 */       String className = m_queryActionsTable[actionNo][CLASSNAME_INDEX];
/*     */ 
/*  70 */       m_queryActionMap.put(key, new Object[] { key, functionIndexList, className });
/*     */     }
/*     */ 
/*  73 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static void addOrUpdateQueryAction(String key, Object[] queryActionRow)
/*     */   {
/*  84 */     if (m_queryActionMap == null)
/*     */     {
/*  86 */       initializeQueryActionTable();
/*     */     }
/*     */ 
/*  89 */     if ((key == null) || (key.length() == 0))
/*     */     {
/*  91 */       key = (String)queryActionRow[0];
/*     */     }
/*  93 */     m_queryActionMap.put(key, queryActionRow);
/*     */   }
/*     */ 
/*     */   public static String processActions(IdcAppendable processedValueBuilder, String actionsString, String value, CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */     throws ServiceException
/*     */   {
/* 112 */     if ((actionsString == null) || (actionsString.length() == 0))
/*     */     {
/* 114 */       return value;
/*     */     }
/*     */ 
/* 117 */     if (Report.m_verbose)
/*     */     {
/* 119 */       Report.trace("searchquery", "Processing actions '" + actionsString + "' on query value '" + value + "'", null);
/*     */     }
/*     */ 
/* 122 */     if ((!m_isInitialized) || (m_queryActionMap == null))
/*     */     {
/* 124 */       initializeQueryActionTable();
/*     */     }
/*     */ 
/* 127 */     String processedValue = value;
/* 128 */     List actionList = StringUtils.makeListFromSequenceSimple(actionsString);
/*     */ 
/* 132 */     for (int actionNo = 0; actionNo < actionList.size(); ++actionNo)
/*     */     {
/* 134 */       String actionKey = (String)(String)actionList.get(actionNo);
/* 135 */       Object[] actionData = (Object[])(Object[])m_queryActionMap.get(actionKey);
/*     */ 
/* 137 */       String className = (String)actionData[CLASSNAME_INDEX];
/*     */ 
/* 139 */       if ((className == null) || (className.length() == 0))
/*     */       {
/* 141 */         processedValue = processAction(actionKey, actionData, processedValue, csConfig, context, companion, binder, args);
/*     */       }
/*     */       else
/*     */       {
/* 145 */         Class actionClass = ClassHelperUtils.createClass(className);
/* 146 */         ClassHelperUtils.executeStaticMethodConvertToStandardExceptions(actionClass, "processAction", new Object[] { actionKey, actionData, value, csConfig, context, companion, binder, args });
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 151 */     processedValueBuilder.append(processedValue);
/*     */ 
/* 153 */     return processedValue;
/*     */   }
/*     */ 
/*     */   public static String processAction(String actionKey, Object[] actionData, String value, CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */   {
/* 172 */     List functionIndexList = (List)actionData[FUNCTIONS_INDEX];
/* 173 */     String processedValue = value;
/*     */ 
/* 175 */     for (int functionNo = 0; functionNo < functionIndexList.size(); ++functionNo)
/*     */     {
/* 177 */       int functionIndex = NumberUtils.parseInteger((String)functionIndexList.get(functionNo), -1);
/* 178 */       processedValue = processActionEx(functionIndex, processedValue, csConfig, context, companion, binder, args);
/*     */     }
/*     */ 
/* 181 */     return processedValue;
/*     */   }
/*     */ 
/*     */   public static String processActionEx(int functionIndex, String value, CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */   {
/* 198 */     String processedValue = value;
/*     */ 
/* 200 */     switch (functionIndex)
/*     */     {
/*     */     case 0:
/* 204 */       processedValue = escapeReservedChars(value, csConfig, context, companion, binder, args);
/* 205 */       break;
/*     */     case 1:
/* 209 */       processedValue = escapeReservedWord(value, csConfig, context, companion, binder, args);
/*     */     }
/*     */ 
/* 215 */     return processedValue;
/*     */   }
/*     */ 
/*     */   public static String escapeReservedChars(String value, CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */   {
/* 232 */     char[] characterList = value.toCharArray();
/* 233 */     IdcAppendable escapedValueBuilder = new IdcStringBuilder();
/* 234 */     String engineName = csConfig.getCurrentEngineName();
/* 235 */     if (companion != null)
/*     */     {
/* 237 */       engineName = companion.m_queryDefinitionLabel;
/*     */     }
/*     */ 
/* 240 */     if (Report.m_verbose)
/*     */     {
/* 242 */       Report.trace("searchquery", "Processing escape reserved characters on query value " + value, null);
/*     */     }
/*     */ 
/* 247 */     boolean isEscapedValue = false;
/* 248 */     if ((characterList[0] == '{') && (characterList[(characterList.length - 1)] == '}') && (characterList.length > 2))
/*     */     {
/* 250 */       isEscapedValue = true;
/*     */     }
/*     */ 
/* 253 */     for (int charNo = 0; charNo < characterList.length; ++charNo)
/*     */     {
/* 255 */       if ((((isEscapedValue != true) || ((charNo != 0) && (charNo != characterList.length - 1)))) && 
/* 257 */         (QueryValueModifierUtils.isCharReserved(engineName, characterList[charNo], csConfig)))
/*     */       {
/* 259 */         escapedValueBuilder.append('\\');
/*     */       }
/*     */ 
/* 263 */       escapedValueBuilder.append(characterList[charNo]);
/*     */     }
/*     */ 
/* 266 */     return escapedValueBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static String escapeReservedWord(String value, CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */   {
/* 283 */     IdcAppendable escapedValueBuilder = new IdcStringBuilder();
/* 284 */     String engineName = csConfig.getCurrentEngineName();
/*     */ 
/* 286 */     if (Report.m_verbose)
/*     */     {
/* 288 */       Report.trace("searchquery", "Processing escape reserved words on query value " + value, null);
/*     */     }
/*     */ 
/* 291 */     if (companion != null)
/*     */     {
/* 293 */       engineName = companion.m_queryDefinitionLabel;
/*     */     }
/*     */ 
/* 296 */     if (QueryValueModifierUtils.isWordReserved(engineName, value, csConfig))
/*     */     {
/* 298 */       escapedValueBuilder.append('{');
/* 299 */       escapedValueBuilder.append(value);
/* 300 */       escapedValueBuilder.append('}');
/*     */     }
/*     */     else
/*     */     {
/* 304 */       escapedValueBuilder.append(value);
/*     */     }
/*     */ 
/* 307 */     return escapedValueBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 312 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82192 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CommonQueryValueModifier
 * JD-Core Version:    0.5.4
 */