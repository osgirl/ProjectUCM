/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class SearchOperatorParsedElements
/*     */ {
/*     */   public String m_operator;
/*     */   public String m_type;
/*     */   public String m_engineName;
/*     */   public List<SearchOperatorParsedElement> m_queryElements;
/*     */   public String m_prefix;
/*     */   public String m_suffix;
/*     */   public String[][] m_escapeMap;
/*     */   public String m_hexSkippedChars;
/*     */ 
/*     */   public void init(String op, String type, List<SearchOperatorParsedElement> elements, String engineName, String[][] escapeMap, String skippedChars)
/*     */   {
/* 275 */     this.m_operator = op;
/* 276 */     this.m_type = type;
/* 277 */     this.m_engineName = engineName;
/* 278 */     this.m_queryElements = elements;
/*     */ 
/* 280 */     if ((type != null) && (escapeMap != null) && (((type.equalsIgnoreCase("text")) || (type.equalsIgnoreCase("zone")))))
/*     */     {
/* 282 */       this.m_escapeMap = escapeMap;
/*     */     }
/*     */ 
/* 285 */     this.m_hexSkippedChars = skippedChars;
/*     */   }
/*     */ 
/*     */   public CharSequence getValue(CharSequence value, List<Action> valueActions)
/*     */   {
/* 290 */     if (valueActions == null)
/*     */     {
/* 292 */       return value;
/*     */     }
/* 294 */     String tmpValue = String.valueOf(value);
/*     */ 
/* 296 */     int actionCounter = 0;
/*     */ 
/* 298 */     if (Report.m_verbose)
/*     */     {
/* 300 */       Report.trace("searchqueryparse", "Processing actions list size : " + valueActions.size() + " on the input value : " + tmpValue + " with searchengine " + this.m_engineName, null);
/*     */     }
/*     */ 
/* 304 */     for (Action action : valueActions)
/*     */     {
/* 306 */       if (Report.m_verbose)
/*     */       {
/* 308 */         Report.trace("searchqueryparse", "Processing action number : " + actionCounter + " : " + action.getActionString() + " on the input value : " + tmpValue + " with searchengine " + this.m_engineName, null);
/*     */       }
/*     */ 
/* 312 */       tmpValue = action.getValue(tmpValue, this);
/* 313 */       actionCounter += 1;
/*     */     }
/* 315 */     return tmpValue;
/*     */   }
/*     */ 
/*     */   public void appendElements(IdcAppendable appendable, CharSequence name, CharSequence value)
/*     */   {
/* 320 */     for (SearchOperatorParsedElement element : this.m_queryElements)
/*     */     {
/* 322 */       CharSequence nextStr = element.m_parsedElement;
/* 323 */       if (element.m_parsedElementType == 1)
/*     */       {
/* 325 */         nextStr = getValue(name, element.m_actions);
/*     */       }
/* 327 */       else if (element.m_parsedElementType == 2)
/*     */       {
/* 329 */         nextStr = getValue(value, element.m_actions);
/*     */       }
/* 331 */       appendable.append(nextStr);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void copy(SearchOperatorParsedElements sope)
/*     */   {
/* 337 */     this.m_operator = sope.m_operator;
/* 338 */     this.m_type = sope.m_type;
/* 339 */     this.m_engineName = sope.m_engineName;
/* 340 */     if (sope.m_queryElements != null)
/*     */     {
/* 342 */       this.m_queryElements = new ArrayList();
/* 343 */       for (SearchOperatorParsedElement element : sope.m_queryElements)
/*     */       {
/* 345 */         this.m_queryElements.add(element.clone());
/*     */       }
/*     */     }
/* 348 */     if (sope.m_escapeMap != null)
/*     */     {
/* 350 */       this.m_escapeMap = ((String[][])sope.m_escapeMap.clone());
/*     */     }
/*     */ 
/* 353 */     this.m_hexSkippedChars = sope.m_hexSkippedChars;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 358 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96148 $";
/*     */   }
/*     */ 
/*     */   public static abstract enum Action
/*     */   {
/*  30 */     PREFIX, 
/*     */ 
/*  48 */     SUFFIX, 
/*     */ 
/*  67 */     UPPER_CASE, 
/*     */ 
/*  82 */     LOWER_CASE, 
/*     */ 
/*  97 */     ESCAPE, 
/*     */ 
/* 142 */     HEX_ENCODE;
/*     */ 
/*     */     public abstract String getValue(String paramString, SearchOperatorParsedElements paramSearchOperatorParsedElements);
/*     */ 
/*     */     public abstract String getActionString();
/*     */ 
/*     */     public static List<Action> getActions(String actions)
/*     */     {
/* 220 */       List actionList = new ArrayList();
/* 221 */       List actionListStr = StringUtils.makeListFromSequence(actions, '.', '.', 0);
/*     */ 
/* 223 */       if (Report.m_verbose)
/*     */       {
/* 225 */         Report.trace("searchqueryparse", "Called getActions with actions :" + actions + " resulting in " + actionListStr.size() + " actions", null);
/*     */       }
/*     */ 
/* 229 */       for (String action : actionListStr)
/*     */       {
/* 231 */         if (action.equalsIgnoreCase(PREFIX.getActionString()))
/*     */         {
/* 233 */           actionList.add(PREFIX);
/*     */         }
/* 235 */         else if (action.equalsIgnoreCase(SUFFIX.getActionString()))
/*     */         {
/* 237 */           actionList.add(SUFFIX);
/*     */         }
/* 239 */         else if (action.equalsIgnoreCase(UPPER_CASE.getActionString()))
/*     */         {
/* 241 */           actionList.add(UPPER_CASE);
/*     */         }
/* 243 */         else if (action.equalsIgnoreCase(LOWER_CASE.getActionString()))
/*     */         {
/* 245 */           actionList.add(LOWER_CASE);
/*     */         }
/* 247 */         else if (action.equalsIgnoreCase(ESCAPE.getActionString()))
/*     */         {
/* 249 */           actionList.add(ESCAPE);
/*     */         }
/* 251 */         else if (action.equalsIgnoreCase(HEX_ENCODE.getActionString()))
/*     */         {
/* 253 */           actionList.add(HEX_ENCODE);
/*     */         }
/*     */       }
/*     */ 
/* 257 */       return actionList;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SearchOperatorParsedElements
 * JD-Core Version:    0.5.4
 */