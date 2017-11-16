/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserProfileData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserProfileEditor
/*     */ {
/*  40 */   protected UserProfileData m_profileData = null;
/*  41 */   protected UserData m_userData = null;
/*  42 */   protected String m_profileDir = null;
/*     */ 
/*  44 */   protected Hashtable m_changedTopics = null;
/*  45 */   protected Hashtable m_resetTopics = null;
/*     */ 
/*  48 */   protected Workspace m_workspace = null;
/*  49 */   protected ExecutionContext m_executionContext = null;
/*     */ 
/*  51 */   protected boolean m_isLocked = false;
/*     */ 
/*     */   public UserProfileEditor(UserData userData, Workspace ws, ExecutionContext ctxt)
/*     */   {
/*  55 */     this.m_userData = userData;
/*  56 */     this.m_profileData = userData.getProfileData();
/*     */ 
/*  59 */     this.m_workspace = ws;
/*  60 */     this.m_executionContext = ctxt;
/*     */ 
/*  62 */     this.m_changedTopics = new Hashtable();
/*  63 */     this.m_resetTopics = new Hashtable();
/*     */   }
/*     */ 
/*     */   public String getProfileDirectory()
/*     */   {
/*  68 */     return this.m_profileDir;
/*     */   }
/*     */ 
/*     */   public void setProfileDirectory(String dir)
/*     */   {
/*  73 */     this.m_profileDir = dir;
/*     */   }
/*     */ 
/*     */   public void setUser(UserData userData)
/*     */   {
/*  78 */     this.m_userData = userData;
/*  79 */     this.m_profileData = userData.getProfileData();
/*     */ 
/*  82 */     this.m_changedTopics = new Hashtable();
/*  83 */     this.m_resetTopics = new Hashtable();
/*     */   }
/*     */ 
/*     */   public boolean isLocked()
/*     */   {
/*  91 */     return this.m_isLocked;
/*     */   }
/*     */ 
/*     */   public void checkForLockDirectory()
/*     */     throws ServiceException
/*     */   {
/*  99 */     if (this.m_isLocked)
/*     */       return;
/* 101 */     FileUtils.reserveDirectory(this.m_profileDir);
/* 102 */     this.m_isLocked = true;
/*     */   }
/*     */ 
/*     */   public void checkForReleaseDirectory()
/*     */   {
/* 111 */     if (!this.m_isLocked)
/*     */       return;
/* 113 */     this.m_isLocked = false;
/* 114 */     FileUtils.releaseDirectory(this.m_profileDir);
/*     */   }
/*     */ 
/*     */   public void doEdits(DataResultSet drset)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 136 */     if (drset == null)
/*     */     {
/* 138 */       return;
/*     */     }
/*     */ 
/* 141 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 143 */       Properties rowProps = drset.getCurrentRowProps();
/* 144 */       performEditAction(rowProps);
/*     */     }
/*     */ 
/* 147 */     for (Enumeration en = this.m_changedTopics.keys(); en.hasMoreElements(); )
/*     */     {
/* 149 */       String topic = (String)en.nextElement();
/* 150 */       TopicInfo info = (TopicInfo)this.m_changedTopics.get(topic);
/* 151 */       saveTopic(info);
/*     */     }
/*     */ 
/* 154 */     for (Enumeration en = this.m_resetTopics.keys(); en.hasMoreElements(); )
/*     */     {
/* 156 */       String topic = (String)en.nextElement();
/* 157 */       TopicInfo info = (TopicInfo)this.m_resetTopics.get(topic);
/* 158 */       resetTopic(info);
/*     */     }
/*     */ 
/* 162 */     long ts = FileUtils.touchFile(this.m_profileDir + "topics.gbl");
/* 163 */     this.m_profileData.setGlobalTimeStamp(ts);
/*     */   }
/*     */ 
/*     */   protected void performEditAction(Properties rowProps)
/*     */     throws DataException, ServiceException
/*     */   {
/* 172 */     String topic = rowProps.getProperty("topicName");
/* 173 */     String action = rowProps.getProperty("topicEditAction");
/*     */ 
/* 175 */     String key = rowProps.getProperty("topicKey");
/* 176 */     String val = rowProps.getProperty("topicValue");
/* 177 */     String reportVal = val;
/* 178 */     if (reportVal != null)
/*     */     {
/* 180 */       reportVal = val.trim();
/*     */     }
/*     */ 
/* 183 */     Report.trace("userprofile", "performEditAction - topic=" + topic + " action=" + action + " topicKey=" + key + " val=" + reportVal, null);
/*     */ 
/* 186 */     if (topic.startsWith("system:"))
/*     */     {
/* 188 */       String msg = LocaleUtils.encodeMessage("csSystemTopicNotEditable", null, topic);
/*     */ 
/* 190 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/* 191 */       return;
/*     */     }
/* 193 */     if (topic.startsWith("pne"))
/*     */     {
/* 195 */       boolean isSimple = SharedObjects.getEnvValueAsBoolean("UseSimpleNavigation", false);
/* 196 */       if (isSimple)
/*     */       {
/* 198 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 202 */     if (this.m_resetTopics.get(topic) != null)
/*     */     {
/* 205 */       return;
/*     */     }
/*     */ 
/* 208 */     TopicInfo info = loadTopicInfoForEdit(topic);
/* 209 */     if (action.equals("reset"))
/*     */     {
/* 211 */       this.m_resetTopics.put(topic, info);
/* 212 */       this.m_changedTopics.remove(topic);
/* 213 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 219 */       boolean topicChanged = true;
/*     */ 
/* 221 */       DataBinder topicData = info.m_data;
/*     */ 
/* 224 */       DataBinder binder = new DataBinder();
/* 225 */       StringReader sr = new StringReader(val);
/* 226 */       binder.receive(new BufferedReader(sr));
/*     */ 
/* 229 */       this.m_executionContext.setCachedObject("topicEditRowProps", rowProps);
/* 230 */       this.m_executionContext.setCachedObject("TopicEditRowProps", rowProps);
/* 231 */       this.m_executionContext.setCachedObject("TopicEditBinder", binder);
/* 232 */       int result = PluginFilters.filter("editTopic", this.m_workspace, topicData, this.m_executionContext);
/* 233 */       if (result == 1)
/*     */       {
/* 235 */         return;
/*     */       }
/*     */ 
/* 239 */       if (action.equals("updateSets"))
/*     */       {
/* 241 */         updateSets(topicData, key, binder);
/*     */       }
/* 243 */       else if (action.equals("deleteSets"))
/*     */       {
/* 245 */         deleteSets(topicData, key, binder);
/*     */       }
/* 247 */       else if ((action.equals("updateRows")) || (action.equals("updateRowsByKeyMap")))
/*     */       {
/* 250 */         updateRows(topicData, key, binder);
/*     */       }
/* 252 */       else if (action.equals("deleteRows"))
/*     */       {
/* 255 */         deleteRows(topicData, key, binder);
/*     */       }
/* 257 */       else if ((action.equals("addMruRow")) || (action.equals("addMruRowByKeyMap")))
/*     */       {
/* 259 */         addMruRow(topicData, key, binder);
/*     */       }
/* 261 */       else if (action.equals("updateKeys"))
/*     */       {
/* 264 */         if (!updateKeys(topicData, key, binder, false, topic))
/*     */         {
/* 266 */           topicChanged = false;
/*     */         }
/*     */       }
/* 269 */       else if (action.equals("updateKeyByName"))
/*     */       {
/* 271 */         if ((key.equals("touchCacheKey")) && (topic.equals("pne_portal")))
/*     */         {
/* 273 */           incrementCacheCounter(topicData);
/*     */         }
/*     */         else
/*     */         {
/* 277 */           String value = binder.getLocal(key);
/* 278 */           if (value == null)
/*     */           {
/* 280 */             value = "";
/*     */           }
/* 282 */           String curVal = topicData.getLocal(key);
/* 283 */           if ((curVal == null) || (!value.equals(curVal)))
/*     */           {
/* 285 */             topicData.putLocal(key, value);
/*     */           }
/*     */           else
/*     */           {
/* 289 */             topicChanged = false;
/*     */           }
/*     */         }
/*     */       }
/* 293 */       else if (action.equals("deleteKeys"))
/*     */       {
/* 296 */         updateKeys(topicData, key, binder, true, topic);
/*     */       }
/*     */       else
/*     */       {
/* 300 */         String msg = LocaleUtils.encodeMessage("csEditCommandDoesNotExist", null, action);
/*     */ 
/* 302 */         throw new DataException(msg);
/*     */       }
/* 304 */       if (topicChanged)
/*     */       {
/* 306 */         this.m_changedTopics.put(topic, info);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 311 */       throw new DataException(e, "csEditActionFailure", new Object[] { action, this.m_userData.m_name, topic });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void incrementCacheCounter(DataBinder topicData)
/*     */   {
/* 318 */     int counter = DataBinderUtils.getLocalInteger(topicData, "cacheCounter", 0);
/* 319 */     ++counter;
/* 320 */     topicData.putLocal("cacheCounter", "" + counter);
/*     */   }
/*     */ 
/*     */   protected void updateSets(DataBinder topicData, String key, DataBinder valData)
/*     */     throws DataException
/*     */   {
/* 326 */     Vector setNames = StringUtils.parseArray(key, ',', '^');
/* 327 */     int num = setNames.size();
/* 328 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 330 */       String setName = (String)setNames.elementAt(i);
/* 331 */       ResultSet rset = valData.getResultSet(setName);
/* 332 */       if (rset == null)
/*     */       {
/* 334 */         String msg = LocaleUtils.encodeMessage("csProfileResultSetMissing", null, setName);
/*     */ 
/* 336 */         throw new DataException(msg);
/*     */       }
/* 338 */       topicData.addResultSet(setName, rset);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteSets(DataBinder topicData, String key, DataBinder valData)
/*     */   {
/* 344 */     Vector setNames = StringUtils.parseArray(key, ',', '^');
/* 345 */     int num = setNames.size();
/* 346 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 348 */       String setName = (String)setNames.elementAt(i);
/* 349 */       topicData.removeResultSet(setName);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateRows(DataBinder topicData, String key, DataBinder valData) throws DataException
/*     */   {
/* 355 */     DataResultSet valSet = (DataResultSet)valData.getResultSet(key);
/* 356 */     DataResultSet tSet = (DataResultSet)topicData.getResultSet(key);
/*     */ 
/* 358 */     if (valSet == null)
/*     */     {
/* 360 */       valSet = createResultSet(key, valData);
/*     */     }
/* 362 */     if (tSet == null)
/*     */     {
/* 364 */       topicData.addResultSet(key, valSet);
/*     */     }
/*     */     else
/*     */     {
/* 368 */       boolean isUpdateOnly = StringUtils.convertToBool(valData.getLocal(key + ":updateOnly"), false);
/* 369 */       mergeSets(tSet, valSet, isUpdateOnly);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteRows(DataBinder topicData, String key, DataBinder valData) throws DataException
/*     */   {
/* 375 */     DataResultSet tSet = (DataResultSet)topicData.getResultSet(key);
/* 376 */     if (tSet == null)
/*     */     {
/* 378 */       return;
/*     */     }
/*     */ 
/* 381 */     String str = valData.getLocal(key);
/* 382 */     if (str == null)
/*     */     {
/* 384 */       String msg = LocaleUtils.encodeMessage("csValueNotDefined", null, key);
/* 385 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 388 */     Vector delRows = StringUtils.parseArray(str, ',', '^');
/* 389 */     int num = delRows.size();
/* 390 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 392 */       String id = (String)delRows.elementAt(i);
/* 393 */       Vector row = tSet.findRow(0, id);
/* 394 */       if (row == null)
/*     */         continue;
/* 396 */       tSet.deleteCurrentRow();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addMruRow(DataBinder topicData, String key, DataBinder valData)
/*     */     throws DataException
/*     */   {
/* 404 */     DataResultSet valSet = (DataResultSet)valData.getResultSet(key);
/* 405 */     if (valSet == null)
/*     */     {
/* 407 */       valSet = createResultSet(key, valData);
/*     */     }
/*     */ 
/* 410 */     boolean isUpdateOnly = StringUtils.convertToBool(valData.getLocal(key + ":updateOnly"), false);
/*     */ 
/* 412 */     DataResultSet toSet = (DataResultSet)topicData.getResultSet(key);
/* 413 */     if (toSet == null)
/*     */     {
/* 415 */       if (!isUpdateOnly)
/*     */       {
/* 417 */         topicData.addResultSet(key, valSet);
/* 418 */         toSet = valSet;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 423 */       if (!isUpdateOnly)
/*     */       {
/* 425 */         synchronizeSets(toSet, valSet);
/* 426 */         synchronizeSets(valSet, toSet);
/*     */       }
/*     */ 
/* 429 */       for (; valSet.isRowPresent(); valSet.next())
/*     */       {
/* 431 */         String val = valSet.getStringValue(0);
/* 432 */         Vector r = toSet.findRow(0, val);
/* 433 */         if (r != null)
/*     */         {
/* 435 */           toSet.deleteCurrentRow();
/*     */         }
/* 437 */         else if (isUpdateOnly) {
/*     */             continue;
/*     */           }
/*     */ 
/*     */ 
/* 442 */         Vector newRow = createRowWithDefaults(toSet, r, valData);
/* 443 */         if (toSet.isEmpty())
/*     */         {
/* 445 */           toSet.addRow(newRow);
/*     */         }
/*     */         else
/*     */         {
/* 449 */           toSet.insertRowAt(newRow, 0);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 454 */     if (toSet == null) {
/*     */       return;
/*     */     }
/*     */ 
/* 458 */     String cutStr = valData.getLocal(key + ":mru");
/* 459 */     if (cutStr == null)
/*     */     {
/* 461 */       cutStr = SharedObjects.getEnvironmentValue(key + ":mru");
/*     */     }
/* 463 */     if (cutStr == null)
/*     */     {
/* 468 */       cutStr = SharedObjects.getEnvironmentValue("UserProfileEditorDefaultMRULength");
/*     */     }
/* 470 */     int cutOff = NumberUtils.parseInteger(cutStr, 10);
/*     */ 
/* 472 */     int diff = toSet.getNumRows() - cutOff;
/* 473 */     if (diff < 0)
/*     */       return;
/* 475 */     for (int i = 0; i < diff; ++i)
/*     */     {
/* 477 */       toSet.last();
/* 478 */       toSet.deleteCurrentRow();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Vector createRowWithDefaults(DataResultSet drset, Vector dfltRow, DataBinder valData)
/*     */   {
/* 491 */     Vector retVal = new IdcVector();
/*     */ 
/* 493 */     int nfields = drset.getNumFields();
/* 494 */     retVal.setSize(nfields);
/*     */ 
/* 496 */     for (int i = 0; i < nfields; ++i)
/*     */     {
/* 498 */       FieldInfo info = new FieldInfo();
/* 499 */       drset.getIndexFieldInfo(i, info);
/* 500 */       String temp = valData.getAllowMissing(info.m_name);
/* 501 */       if (temp == null)
/*     */       {
/* 503 */         if (dfltRow != null)
/*     */         {
/* 505 */           temp = (String)dfltRow.elementAt(i);
/*     */         }
/*     */         else
/*     */         {
/* 509 */           temp = "";
/*     */         }
/*     */       }
/* 512 */       retVal.setElementAt(temp, i);
/*     */     }
/* 514 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected DataResultSet createResultSet(String key, DataBinder data)
/*     */     throws DataException
/*     */   {
/* 520 */     String str = data.getLocal(key + ":columns");
/* 521 */     if ((str == null) || (str.trim().length() == 0))
/*     */     {
/* 523 */       String msg = LocaleUtils.encodeMessage("csProfileSetNotDefined", null, key);
/*     */ 
/* 525 */       throw new DataException(msg);
/*     */     }
/* 527 */     Vector strs = StringUtils.parseArray(str, ',', '^');
/* 528 */     String[] columns = StringUtils.convertListToArray(strs);
/*     */ 
/* 530 */     DataResultSet drset = new DataResultSet(columns);
/*     */     try
/*     */     {
/* 533 */       Vector row = drset.createRow(data);
/* 534 */       drset.addRow(row);
/*     */ 
/* 536 */       data.addResultSet(key, drset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 540 */       throw new DataException(e, "csProfileResultSetError", new Object[] { key });
/*     */     }
/*     */ 
/* 543 */     return drset;
/*     */   }
/*     */ 
/*     */   protected void synchronizeSets(DataResultSet toSet, DataResultSet fromSet)
/*     */   {
/* 550 */     Vector appendInfos = new IdcVector();
/* 551 */     int len = fromSet.getNumFields();
/* 552 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 554 */       FieldInfo fromInfo = new FieldInfo();
/*     */ 
/* 556 */       fromSet.getIndexFieldInfo(i, fromInfo);
/* 557 */       appendInfos.addElement(fromInfo);
/*     */     }
/* 559 */     toSet.mergeFieldsWithFlags(appendInfos, 0);
/*     */   }
/*     */ 
/*     */   protected void mergeSets(DataResultSet toSet, DataResultSet fromSet, boolean isUpdateOnly) throws DataException
/*     */   {
/* 564 */     synchronizeSets(toSet, fromSet);
/*     */ 
/* 566 */     String name = toSet.getFieldName(0);
/* 567 */     toSet.merge(name, fromSet, isUpdateOnly);
/*     */   }
/*     */ 
/*     */   protected boolean updateKeys(DataBinder data, String keyStr, DataBinder valData, boolean isDelete, String topicName)
/*     */     throws ServiceException
/*     */   {
/* 574 */     List keys = StringUtils.makeListFromSequence(keyStr, '#', '*', 0);
/* 575 */     List vals = null;
/* 576 */     boolean changedSomething = false;
/* 577 */     if (!isDelete)
/*     */     {
/* 580 */       Properties props = valData.getLocalData();
/* 581 */       String valStr = props.getProperty(keyStr);
/* 582 */       if ((valStr != null) && (valStr.length() == 0))
/*     */       {
/* 585 */         vals = new ArrayList();
/* 586 */         vals.add("");
/*     */       }
/*     */       else
/*     */       {
/* 590 */         vals = StringUtils.makeListFromSequence(valStr, '#', '*', 0);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 595 */     int size = keys.size();
/* 596 */     if (vals != null)
/*     */     {
/* 598 */       int numVals = vals.size();
/* 599 */       if (numVals != size)
/*     */       {
/* 602 */         String errMsg = LocaleUtils.encodeMessage("csProfileUpdateTopicFormatError", null, keyStr);
/* 603 */         throw new ServiceException(errMsg);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 608 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 610 */       String key = (String)keys.get(i);
/* 611 */       if ((!isDelete) && (key.equals("touchCacheKey")) && (topicName.equals("pne_portal")))
/*     */       {
/* 613 */         incrementCacheCounter(data);
/* 614 */         changedSomething = true;
/*     */       }
/*     */       else
/*     */       {
/* 618 */         String curVal = data.getLocal(key);
/* 619 */         if (isDelete)
/*     */         {
/* 621 */           if (curVal == null)
/*     */             continue;
/* 623 */           changedSomething = true;
/* 624 */           data.removeLocal(key);
/*     */         }
/*     */         else
/*     */         {
/* 629 */           String newVal = (String)vals.get(i);
/* 630 */           if ((curVal != null) && (newVal.equals(curVal)))
/*     */             continue;
/* 632 */           changedSomething = true;
/* 633 */           data.putLocal(key, newVal);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 638 */     return changedSomething;
/*     */   }
/*     */ 
/*     */   public void loadTopicData(TopicInfo info, long ts) throws ServiceException
/*     */   {
/* 643 */     TopicInfo defInfo = ProfileCache.retrieveTopicDefaults(info, this.m_userData);
/* 644 */     String defCounter = defInfo.m_data.getLocal("topicCounter");
/*     */ 
/* 647 */     checkForLockDirectory();
/*     */ 
/* 649 */     DataBinder data = new DataBinder();
/* 650 */     if (ResourceUtils.serializeDataBinder(info.m_directory, info.m_filename, data, false, false))
/*     */     {
/* 652 */       info.init(data, false);
/* 653 */       String counter = data.getLocal("topicCounter");
/* 654 */       if ((defCounter == null) || (defCounter.equals(counter)))
/*     */       {
/* 656 */         info.m_lastLoaded = ts;
/* 657 */         return;
/*     */       }
/* 659 */       data.removeLocal("topicCounter");
/*     */     }
/*     */ 
/* 663 */     info.init(defInfo.m_data, true);
/* 664 */     saveTopic(info);
/*     */   }
/*     */ 
/*     */   public TopicInfo loadTopicInfo(String topic) throws ServiceException
/*     */   {
/* 669 */     TopicInfo result = null;
/*     */     try
/*     */     {
/* 672 */       result = loadTopicInfoEx(topic, false);
/*     */     }
/*     */     finally
/*     */     {
/* 676 */       checkForReleaseDirectory();
/*     */     }
/*     */ 
/* 679 */     return result;
/*     */   }
/*     */ 
/*     */   public TopicInfo loadTopicInfoEx(String topic, boolean isEdit) throws ServiceException
/*     */   {
/* 684 */     TopicInfo info = this.m_profileData.getTopic(topic);
/* 685 */     if (info == null)
/*     */     {
/* 687 */       validateTopicName(topic);
/*     */ 
/* 689 */       info = new TopicInfo(topic);
/* 690 */       info.m_directory = this.m_profileDir;
/* 691 */       this.m_profileData.addTopic(info);
/* 692 */       Report.trace("userprofile", "Creating new topic in loadTopicInfoEx for topic " + topic + " stored in directory " + info.m_directory, null);
/*     */     }
/* 695 */     else if (info.m_directory == null)
/*     */     {
/* 697 */       info.m_directory = this.m_profileDir;
/* 698 */       Report.trace("userprofile", "Assigning directory " + info.m_directory + " to partially initialized topic " + topic + " in method loadTopicInfoEx", null);
/*     */     }
/*     */ 
/* 702 */     File topicFile = new File(info.getFilePath());
/* 703 */     long ts = topicFile.lastModified();
/*     */ 
/* 708 */     if (ts != info.m_lastLoaded)
/*     */     {
/* 710 */       Report.trace("userprofile", "Reading in data from directory " + info.m_directory + " for topic " + topic + " in method loadTopicInfoEx, isEdit = " + isEdit, null);
/*     */ 
/* 712 */       loadTopicData(info, ts);
/*     */     }
/*     */ 
/* 715 */     return info;
/*     */   }
/*     */ 
/*     */   public TopicInfo loadTopicInfoCopy(String topic, boolean isEdit) throws ServiceException
/*     */   {
/* 720 */     TopicInfo info = loadTopicInfoEx(topic, isEdit);
/*     */ 
/* 723 */     TopicInfo tInfo = new TopicInfo();
/* 724 */     tInfo.copy(info);
/*     */ 
/* 726 */     return tInfo;
/*     */   }
/*     */ 
/*     */   protected TopicInfo loadTopicInfoForEdit(String topic) throws ServiceException
/*     */   {
/* 731 */     TopicInfo info = (TopicInfo)this.m_changedTopics.get(topic);
/* 732 */     if (info != null)
/*     */     {
/* 734 */       return info;
/*     */     }
/*     */ 
/* 737 */     info = loadTopicInfoCopy(topic, true);
/*     */ 
/* 739 */     return info;
/*     */   }
/*     */ 
/*     */   protected void validateTopicName(String topic) throws ServiceException
/*     */   {
/* 744 */     int result = Validation.checkUrlFilePathPart(topic);
/* 745 */     if ((result == 0) && (topic.indexOf(46) < 0) && (topic.indexOf(39) < 0))
/*     */       return;
/* 747 */     String msg = LocaleUtils.encodeMessage("csProfileTopicNameMissing", null, topic);
/*     */ 
/* 749 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   protected void saveTopic(TopicInfo info)
/*     */     throws ServiceException
/*     */   {
/* 756 */     checkForLockDirectory();
/*     */ 
/* 758 */     info.m_directory = this.m_profileDir;
/* 759 */     String subPath = FileUtils.directorySlashesEx(info.m_filename, false);
/*     */ 
/* 761 */     FileUtils.checkOrCreateSubDirectoryEx(info.m_directory, subPath, true);
/* 762 */     ResourceUtils.serializeDataBinder(info.m_directory, info.m_filename, info.m_data, true, false);
/*     */ 
/* 764 */     File f = new File(info.getFilePath());
/* 765 */     info.m_lastLoaded = f.lastModified();
/*     */ 
/* 767 */     this.m_profileData.setTopicInfo(info);
/*     */   }
/*     */ 
/*     */   protected void resetTopic(TopicInfo info)
/*     */     throws ServiceException
/*     */   {
/* 773 */     checkForLockDirectory();
/*     */ 
/* 777 */     File f = new File(info.getFilePath());
/* 778 */     if (f.exists())
/*     */     {
/* 780 */       f.delete();
/*     */     }
/*     */ 
/* 784 */     TopicInfo ti = ProfileCache.retrieveTopicDefaults(info, this.m_userData);
/* 785 */     info.copy(ti);
/* 786 */     saveTopic(info);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 791 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83762 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserProfileEditor
 * JD-Core Version:    0.5.4
 */