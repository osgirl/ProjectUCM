/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.shared.ProfileUtils;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.CharConversionException;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserProfileHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void decodeTopicValues()
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  41 */     String decodeString = this.m_binder.getLocal("decodeValues");
/*     */ 
/*  43 */     if (decodeString == null)
/*     */     {
/*  45 */       return;
/*     */     }
/*     */ 
/*  49 */     Vector decodeVals = StringUtils.parseArray(decodeString, ',', '^');
/*     */ 
/*  52 */     int num = decodeVals.size();
/*  53 */     for (int i = 0; i < num; ++i)
/*     */     {
/*  55 */       String clmnName = (String)decodeVals.elementAt(i);
/*  56 */       String encodedValue = this.m_binder.getLocal(clmnName);
/*     */ 
/*  58 */       if (encodedValue == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  65 */       DataBinder binder = new DataBinder();
/*  66 */       binder.m_isCgi = true;
/*  67 */       String decodedValue = null;
/*     */       try
/*     */       {
/*  70 */         decodedValue = StringUtils.decodeUrlEncodedString(encodedValue, this.m_binder.m_javaEncoding);
/*     */       }
/*     */       catch (CharConversionException e)
/*     */       {
/*  74 */         throw new ServiceException(e);
/*     */       }
/*  76 */       this.m_binder.putLocal(clmnName, decodedValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepareTopicEdits()
/*     */     throws DataException, ServiceException
/*     */   {
/*  86 */     String columnsStr = null;
/*  87 */     String topicStringName = null;
/*  88 */     String topicAction = null;
/*  89 */     String topicName = null;
/*  90 */     String topicKey = null;
/*  91 */     ArrayList allKeys = null;
/*     */ 
/*  94 */     this.m_service.setCachedObject("UserTopicEdits", null);
/*     */ 
/*  97 */     String topicKeysStr = this.m_binder.getLocal("topicKeys");
/*  98 */     if (topicKeysStr != null)
/*     */     {
/* 100 */       Vector topicKeysVec = StringUtils.parseArray(topicKeysStr, ':', '*');
/* 101 */       allKeys = new ArrayList(topicKeysVec);
/*     */     }
/* 103 */     if (allKeys == null)
/*     */     {
/* 105 */       allKeys = new ArrayList();
/*     */     }
/*     */ 
/* 110 */     String numTopicStrings = this.m_binder.getLocal("numTopics");
/*     */ 
/* 113 */     int numTopics = NumberUtils.parseInteger(numTopicStrings, 0);
/* 114 */     for (int curTopic = 1; curTopic <= numTopics; ++curTopic)
/*     */     {
/* 118 */       topicStringName = "topicString" + String.valueOf(curTopic);
/* 119 */       allKeys.add(topicStringName);
/*     */     }
/*     */ 
/* 123 */     if (allKeys.size() < 1)
/*     */     {
/* 125 */       return;
/*     */     }
/*     */ 
/* 129 */     for (Iterator it = allKeys.iterator(); it.hasNext(); )
/*     */     {
/* 131 */       topicStringName = (String)it.next();
/*     */ 
/* 134 */       String topicString = this.m_binder.getLocal(topicStringName);
/* 135 */       if (topicString == null) continue; if (topicString.equals(""))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 141 */       Vector topicCols = StringUtils.parseArray(topicString, ':', '*');
/*     */ 
/* 143 */       int numTopicCols = topicCols.size();
/* 144 */       boolean isTopicError = false;
/*     */ 
/* 146 */       topicAction = (String)topicCols.elementAt(0);
/*     */ 
/* 149 */       if ((topicAction.equals("deleteSets")) || (topicAction.equals("deleteKeys")))
/*     */       {
/* 151 */         if (numTopicCols < 3)
/*     */         {
/* 153 */           isTopicError = true;
/*     */         }
/*     */       }
/* 156 */       else if ((topicAction.equals("updateRows")) || (topicAction.equals("addMruRowByKeyMap")))
/*     */       {
/* 158 */         if (numTopicCols < 5)
/*     */         {
/* 160 */           isTopicError = true;
/*     */         }
/*     */       }
/* 163 */       else if (topicAction.equals("updateRowsByKeyMap"))
/*     */       {
/* 165 */         if (numTopicCols < 6)
/*     */         {
/* 167 */           isTopicError = true;
/*     */         }
/*     */       }
/* 170 */       else if (numTopicCols < 4)
/*     */       {
/* 172 */         isTopicError = true;
/*     */       }
/*     */ 
/* 175 */       if (isTopicError)
/*     */       {
/* 177 */         throw new DataException("!csProfileUnableToEditTopic");
/*     */       }
/*     */ 
/* 180 */       topicName = (String)topicCols.elementAt(1);
/* 181 */       topicKey = (String)topicCols.elementAt(2);
/*     */ 
/* 183 */       DataBinder valueBinder = new DataBinder();
/*     */ 
/* 186 */       if ((topicAction.equals("addMruRow")) || (topicAction.equals("addMruRowByKeyMap")))
/*     */       {
/* 188 */         String keyMapStr = null;
/*     */ 
/* 190 */         columnsStr = (String)topicCols.elementAt(3);
/* 191 */         if (topicAction.equals("addMruRowByKeyMap"))
/*     */         {
/* 193 */           keyMapStr = (String)topicCols.elementAt(4);
/*     */         }
/*     */ 
/* 196 */         if (columnsStr != null)
/*     */         {
/* 198 */           valueBinder.putLocal(topicKey + ":columns", columnsStr);
/*     */ 
/* 201 */           Vector clmns = StringUtils.parseArray(columnsStr, ',', '^');
/* 202 */           Vector clmnsMap = null;
/*     */ 
/* 204 */           if (keyMapStr != null)
/*     */           {
/* 206 */             clmnsMap = StringUtils.parseArray(keyMapStr, ',', '^');
/*     */           }
/*     */ 
/* 209 */           int num = clmns.size();
/* 210 */           if ((clmnsMap != null) && (num != clmnsMap.size()))
/*     */           {
/* 212 */             throw new DataException("!csProfileUnableToEditTopic");
/*     */           }
/*     */ 
/* 215 */           for (int i = 0; i < num; ++i)
/*     */           {
/* 217 */             String clmnName = (String)clmns.elementAt(i);
/* 218 */             String keyName = clmnName;
/*     */ 
/* 220 */             if (clmnsMap != null)
/*     */             {
/* 222 */               keyName = (String)clmnsMap.elementAt(i);
/*     */             }
/*     */ 
/* 225 */             valueBinder.putLocal(clmnName, this.m_binder.get(keyName));
/*     */           }
/*     */         }
/*     */ 
/* 229 */         String mruValue = this.m_binder.getLocal("mruNumber");
/* 230 */         if (mruValue != null)
/*     */         {
/* 232 */           valueBinder.putLocal(topicKey + ":mru", mruValue);
/*     */         }
/*     */ 
/*     */       }
/* 236 */       else if ((topicAction.equals("updateRows")) || (topicAction.equals("updateRowsByKeyMap")))
/*     */       {
/* 239 */         columnsStr = (String)topicCols.elementAt(3);
/* 240 */         String rowStr = null;
/* 241 */         String rowMapStr = null;
/*     */ 
/* 243 */         if (topicAction.equals("updateRowsByKeyMap"))
/*     */         {
/* 246 */           rowMapStr = (String)topicCols.elementAt(4);
/*     */ 
/* 249 */           rowStr = (String)topicCols.elementAt(5);
/*     */         }
/*     */         else
/*     */         {
/* 254 */           rowStr = (String)topicCols.elementAt(4);
/*     */         }
/*     */ 
/* 258 */         Vector clmns = StringUtils.parseArray(columnsStr, ',', ',');
/* 259 */         String[] columns = StringUtils.convertListToArray(clmns);
/* 260 */         DataResultSet drset = new DataResultSet(columns);
/*     */ 
/* 263 */         Vector clmnsMap = null;
/* 264 */         if (topicAction.equals("updateRowsByKeyMap"))
/*     */         {
/* 266 */           clmnsMap = StringUtils.parseArray(rowMapStr, ',', ',');
/*     */ 
/* 268 */           if (clmnsMap.size() != clmns.size())
/*     */           {
/* 270 */             throw new DataException("!csProfileUnableToEditTopic");
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 275 */         int numRows = NumberUtils.parseInteger(rowStr, 0);
/* 276 */         int numCols = clmns.size();
/*     */ 
/* 278 */         for (int i = 1; i <= numRows; ++i)
/*     */         {
/* 280 */           Vector clmnVals = new IdcVector();
/*     */ 
/* 283 */           for (int j = 0; j < numCols; ++j)
/*     */           {
/* 285 */             String drClmName = null;
/*     */ 
/* 287 */             if (clmnsMap != null)
/*     */             {
/* 289 */               drClmName = (String)clmnsMap.elementAt(j);
/*     */             }
/*     */             else
/*     */             {
/* 293 */               drClmName = (String)clmns.elementAt(j);
/*     */             }
/*     */ 
/* 296 */             String curCol = drClmName + String.valueOf(i);
/*     */ 
/* 298 */             clmnVals.addElement(this.m_binder.get(curCol));
/*     */           }
/*     */ 
/* 301 */           drset.addRow(clmnVals);
/*     */         }
/*     */ 
/* 304 */         valueBinder.addResultSet(topicKey, drset);
/*     */       }
/* 307 */       else if (topicAction.equals("updateKeyByName"))
/*     */       {
/* 309 */         String keyName = (String)topicCols.elementAt(3);
/* 310 */         String keyValue = this.m_binder.getLocal(keyName);
/* 311 */         if (keyValue == null)
/*     */         {
/* 313 */           throw new DataException(LocaleUtils.encodeMessage("csProfileEditValueNotFound", null, keyName, topicKey));
/*     */         }
/*     */ 
/* 316 */         valueBinder.putLocal(topicKey, keyValue);
/*     */       }
/* 319 */       else if (topicAction.equals("updateKeys"))
/*     */       {
/* 321 */         String encodedKeyValue = (String)topicCols.elementAt(3);
/*     */ 
/* 324 */         valueBinder.m_isCgi = true;
/* 325 */         String keyValue = null;
/*     */         try
/*     */         {
/* 328 */           keyValue = StringUtils.decodeUrlEncodedString(encodedKeyValue, this.m_binder.m_javaEncoding);
/*     */         }
/*     */         catch (CharConversionException e)
/*     */         {
/* 332 */           throw new ServiceException(e);
/*     */         }
/* 334 */         valueBinder.m_isCgi = false;
/*     */ 
/* 336 */         valueBinder.putLocal(topicKey, keyValue);
/*     */       }
/* 339 */       else if (topicAction.equals("deleteRows"))
/*     */       {
/* 341 */         String encodedDeleteKeys = (String)topicCols.elementAt(3);
/*     */ 
/* 349 */         valueBinder.m_isCgi = true;
/*     */ 
/* 351 */         StringBuffer deleteKeyBuffer = new StringBuffer();
/* 352 */         Vector deleteKeyList = StringUtils.parseArray(encodedDeleteKeys, ',', '^');
/* 353 */         boolean firstTime = true;
/* 354 */         int numKeys = deleteKeyList.size();
/* 355 */         for (int i = 0; i < numKeys; ++i)
/*     */         {
/* 357 */           String deleteKey = (String)deleteKeyList.elementAt(i);
/*     */           try
/*     */           {
/* 360 */             deleteKey = StringUtils.decodeUrlEncodedString(deleteKey, this.m_binder.m_javaEncoding);
/*     */           }
/*     */           catch (CharConversionException e)
/*     */           {
/* 364 */             throw new ServiceException(e);
/*     */           }
/* 366 */           deleteKey = StringUtils.addEscapeChars(deleteKey, ',', '^');
/*     */ 
/* 368 */           if (!firstTime)
/*     */           {
/* 370 */             deleteKeyBuffer.append(',');
/*     */           }
/* 372 */           firstTime = false;
/* 373 */           deleteKeyBuffer.append(deleteKey);
/*     */         }
/* 375 */         valueBinder.m_isCgi = false;
/*     */ 
/* 377 */         valueBinder.putLocal(topicKey, deleteKeyBuffer.toString());
/*     */       }
/*     */ 
/* 381 */       ProfileUtils.addTopicEdit(topicName, topicAction, topicKey, valueBinder, this.m_binder);
/*     */ 
/* 383 */       this.m_service.setCachedObject("UserTopicEdits", this.m_binder.getResultSet("UserTopicEdits"));
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateTopicInformation() throws ServiceException, DataException
/*     */   {
/* 390 */     this.m_service.updateTopicInformation(this.m_binder);
/* 391 */     this.m_service.setCachedObject("UserTopicEdits", null);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadProfileForRequest()
/*     */     throws ServiceException, DataException
/*     */   {
/* 398 */     boolean isAddAction = true;
/*     */ 
/* 400 */     if (DataBinderUtils.getBoolean(this.m_binder, "isForcedpAction", false))
/*     */     {
/* 402 */       String action = this.m_binder.getLocal("dpAction");
/*     */ 
/* 404 */       if ((action != null) && (action.length() > 0))
/*     */       {
/* 406 */         this.m_binder.putLocal("dpAction", action);
/* 407 */         isAddAction = false;
/*     */       }
/*     */     }
/*     */ 
/* 411 */     int num = this.m_currentAction.getNumParams();
/* 412 */     if ((isAddAction) && (num > 0))
/*     */     {
/* 414 */       String action = this.m_currentAction.getParamAt(0);
/* 415 */       this.m_binder.putLocal("dpAction", action);
/*     */     }
/* 417 */     String tmp = null;
/*     */     try
/*     */     {
/* 420 */       PageMerger pageMerger = this.m_service.getPageMerger();
/* 421 */       tmp = pageMerger.evaluateResourceInclude("load_document_profile");
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 425 */       Report.trace(null, null, e);
/*     */     }
/*     */     finally
/*     */     {
/* 429 */       if (SystemUtils.m_verbose)
/*     */       {
/* 431 */         Report.debug("docprofile", "Loaded profiles with result:" + tmp, null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadTopic()
/*     */     throws ServiceException, DataException
/*     */   {
/* 447 */     String topic = this.m_binder.getLocal("userTopic");
/* 448 */     String rsNames = this.m_binder.getLocal("resultSets");
/* 449 */     Vector rsNameList = StringUtils.parseArray(rsNames, ',', ',');
/* 450 */     int rsCount = rsNameList.size();
/*     */ 
/* 452 */     if ((topic == null) || (topic.trim().length() == 0))
/*     */     {
/* 454 */       String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, "userTopic");
/*     */ 
/* 456 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 459 */     TopicInfo topicInfo = UserProfileUtils.getTopicInfo(this.m_service, topic);
/* 460 */     if (topicInfo == null)
/*     */       return;
/* 462 */     DataBinder data = topicInfo.m_data;
/* 463 */     if (data == null)
/*     */       return;
/* 465 */     if (rsCount > 0)
/*     */     {
/* 467 */       this.m_binder.mergeHashTablesInternal(this.m_binder.m_localData, data.m_localData, data, false);
/* 468 */       for (int i = 0; i < rsCount; ++i)
/*     */       {
/* 470 */         String rsName = (String)rsNameList.elementAt(i);
/* 471 */         ResultSet rs = data.getResultSet(rsName);
/* 472 */         if (rs == null)
/*     */           continue;
/* 474 */         this.m_binder.addResultSet(rsName, rs);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 480 */       this.m_binder.merge(data);
/* 481 */       this.m_binder.m_checkResultSetLocalization = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadProfileForSubmit()
/*     */     throws ServiceException, DataException
/*     */   {
/* 490 */     DocProfileManager.loadDocumentProfile(this.m_binder, this.m_service, true);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 496 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102402 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserProfileHandler
 * JD-Core Version:    0.5.4
 */