/*     */ package intradoc.autosuggest.utils;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestConstants;
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.partition.AutoSuggestPartitionedContext;
/*     */ import intradoc.autosuggest.records.TermGramParameters;
/*     */ import intradoc.common.DateUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class AutoSuggestUtils
/*     */ {
/*     */   public static Map<String, TermGramParameters> contructGramParameters(String input)
/*     */   {
/*  40 */     Map gramMap = new HashMap();
/*  41 */     for (int gramLength = getMinimumGramLength(); gramLength <= getMaximumGramLength(); ++gramLength)
/*     */     {
/*  43 */       contructGramParameters(gramMap, input, gramLength);
/*     */     }
/*  45 */     return gramMap;
/*     */   }
/*     */ 
/*     */   public static Map<String, TermGramParameters> contructGramParameters(Map<String, TermGramParameters> gramMap, String input, int gramLength)
/*     */   {
/*  57 */     for (short index = 0; index <= input.length() - gramLength; index = (short)(index + 1))
/*     */     {
/*  59 */       String gram = input.substring(index, index + gramLength);
/*  60 */       TermGramParameters gramParams = (TermGramParameters)gramMap.get(gram);
/*  61 */       if (gramParams == null)
/*     */       {
/*  63 */         gramParams = new TermGramParameters(0, index);
/*     */       }
/*  65 */       gramParams.incrementFrequency();
/*  66 */       gramMap.put(gram, gramParams);
/*     */     }
/*  68 */     return gramMap;
/*     */   }
/*     */ 
/*     */   public static double getLevensteinDistance(String source, String destination)
/*     */   {
/*  85 */     char[] sourceArray = source.toCharArray();
/*  86 */     int sourceLength = sourceArray.length;
/*  87 */     int destinationLength = destination.length();
/*  88 */     float[] previousCostArray = new float[sourceLength + 1];
/*  89 */     float[] currentCostArray = new float[sourceLength + 1];
/*     */ 
/*  91 */     if ((sourceLength == 0) || (destinationLength == 0))
/*     */     {
/*  93 */       if (sourceLength == destinationLength)
/*     */       {
/*  95 */         return 1.0D;
/*     */       }
/*  97 */       return 0.0D;
/*     */     }
/*  99 */     for (int sourceIndex = 0; sourceIndex <= sourceLength; ++sourceIndex)
/*     */     {
/* 101 */       previousCostArray[sourceIndex] = sourceIndex;
/*     */     }
/* 103 */     for (int destIndex = 1; destIndex <= destinationLength; ++destIndex)
/*     */     {
/* 105 */       char destCharacter = destination.charAt(destIndex - 1);
/* 106 */       currentCostArray[0] = destIndex;
/* 107 */       for (int sourceIndex = 1; sourceIndex <= sourceLength; ++sourceIndex)
/*     */       {
/* 109 */         float cost = (float)((sourceArray[(sourceIndex - 1)] == destCharacter) ? 0.0D : 1.5D);
/* 110 */         currentCostArray[sourceIndex] = (float)Math.min(Math.min(currentCostArray[(sourceIndex - 1)] + 1.0F, previousCostArray[sourceIndex] + 0.1D), previousCostArray[(sourceIndex - 1)] + cost);
/*     */       }
/*     */ 
/* 115 */       float[] swapSpace = previousCostArray;
/* 116 */       previousCostArray = currentCostArray;
/* 117 */       currentCostArray = swapSpace;
/*     */     }
/* 119 */     return 1.0F - previousCostArray[sourceLength] / Math.max(destination.length(), sourceArray.length);
/*     */   }
/*     */ 
/*     */   public static AutoSuggestContext prepareContext(Service service, Workspace workspace, String partition, String contextKey)
/*     */     throws DataException, ServiceException
/*     */   {
/* 132 */     AutoSuggestContext context = null;
/* 133 */     if ((partition != null) && (partition.length() > 0))
/*     */     {
/* 135 */       context = new AutoSuggestPartitionedContext(partition, contextKey, service, workspace);
/*     */     }
/*     */     else
/*     */     {
/* 139 */       context = new AutoSuggestContext(contextKey, service, workspace);
/*     */     }
/* 141 */     return context;
/*     */   }
/*     */ 
/*     */   public static AutoSuggestContext prepareContext(Service service, Workspace workspace, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 154 */     AutoSuggestContext context = null;
/* 155 */     String partition = binder.getLocal("partition");
/* 156 */     String contextKey = binder.getLocal("context");
/* 157 */     if ((partition != null) && (partition.length() > 0))
/*     */     {
/* 159 */       context = new AutoSuggestPartitionedContext(partition, contextKey, service, workspace);
/*     */     }
/*     */     else
/*     */     {
/* 163 */       context = new AutoSuggestContext(contextKey, service, workspace);
/*     */     }
/* 165 */     return context;
/*     */   }
/*     */ 
/*     */   public static Properties parseContextKey(String contextKey)
/*     */   {
/* 176 */     Properties contextProperties = new Properties();
/* 177 */     contextProperties.put("contextKey", contextKey);
/* 178 */     String parsedContextKey = contextKey;
/* 179 */     int indexOfOpeningBracket = contextKey.indexOf('(');
/* 180 */     if (indexOfOpeningBracket > -1)
/*     */     {
/* 182 */       parsedContextKey = contextKey.substring(0, indexOfOpeningBracket);
/* 183 */       int indexOfClosingBracket = contextKey.indexOf(')');
/* 184 */       if ((indexOfClosingBracket != -1) && (indexOfClosingBracket > indexOfOpeningBracket))
/*     */       {
/* 186 */         String contextPropertiesStr = contextKey.substring(indexOfOpeningBracket + 1, indexOfClosingBracket);
/* 187 */         StringUtils.parsePropertiesEx(contextProperties, contextPropertiesStr, '.', '\\', '=');
/* 188 */         contextProperties.put("contextKey", parsedContextKey);
/*     */       }
/*     */     }
/* 191 */     return contextProperties;
/*     */   }
/*     */ 
/*     */   public static int getMinimumGramLength()
/*     */   {
/* 199 */     int minGramLength = SharedObjects.getEnvironmentInt("AutoSuggestMinGramLength", AutoSuggestConstants.DEFAULT_MIN_GRAM_LENGTH);
/* 200 */     return minGramLength;
/*     */   }
/*     */ 
/*     */   public static int getMaximumGramLength()
/*     */   {
/* 208 */     int maxGramLength = SharedObjects.getEnvironmentInt("AutoSuggestMaxGramLength", AutoSuggestConstants.DEFAULT_MAX_GRAM_LENGTH);
/* 209 */     return maxGramLength;
/*     */   }
/*     */ 
/*     */   public static double getProximityCutOff()
/*     */   {
/* 217 */     String proximityCutOffStr = SharedObjects.getEnvironmentValue("AutoSuggestProximityCutOff");
/* 218 */     double proximityCutOff = NumberUtils.parseDouble(proximityCutOffStr, AutoSuggestConstants.DEFAULT_PROXIMITY_CUT_OFF);
/* 219 */     return proximityCutOff;
/*     */   }
/*     */ 
/*     */   public static long getAutoSuggesterThreadTimeOut()
/*     */     throws DataException
/*     */   {
/* 229 */     String autoSuggesterThreadTimeOutStr = SharedObjects.getEnvironmentValue("AutoSuggesterThreadTimeOut");
/* 230 */     if ((autoSuggesterThreadTimeOutStr == null) || (autoSuggesterThreadTimeOutStr.length() == 0))
/*     */     {
/* 232 */       autoSuggesterThreadTimeOutStr = Long.toString(AutoSuggestConstants.AUTO_SUGGESTER_THREAD_TIMEOUT);
/*     */     }
/* 234 */     long autoSuggesterThreadTimeOut = DateUtils.parseTime(autoSuggesterThreadTimeOutStr);
/* 235 */     return autoSuggesterThreadTimeOut;
/*     */   }
/*     */ 
/*     */   public static long getAutoSuggesterPerGramThreadTimeOut()
/*     */     throws DataException
/*     */   {
/* 245 */     String autoSuggesterPerGramThreadTimeOutStr = SharedObjects.getEnvironmentValue("AutoSuggesterPerGramThreadTimeOut");
/* 246 */     if ((autoSuggesterPerGramThreadTimeOutStr == null) || (autoSuggesterPerGramThreadTimeOutStr.length() == 0))
/*     */     {
/* 248 */       autoSuggesterPerGramThreadTimeOutStr = Long.toString(AutoSuggestConstants.AUTO_SUGGESTER_PER_GRAM_THREAD_TIMEOUT);
/*     */     }
/* 250 */     long autoSuggesterPerGramThreadTimeOut = DateUtils.parseTime(autoSuggesterPerGramThreadTimeOutStr);
/* 251 */     return autoSuggesterPerGramThreadTimeOut;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 255 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103915 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.AutoSuggestUtils
 * JD-Core Version:    0.5.4
 */