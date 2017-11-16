/*      */ package intradoc.data;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcAppender;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ import java.text.ParseException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.Collections;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DataBinder
/*      */   implements Parameters, DataDecode, IdcAppender
/*      */ {
/*      */   public Properties m_environment;
/*      */   public Properties m_localData;
/*      */   public Hashtable m_optionLists;
/*   73 */   public Vector m_rawData = new IdcVector();
/*      */   public byte[] m_rawPostData;
/*      */   public byte[] m_postDataBuf;
/*      */   public Map m_resultSets;
/*      */   public List m_activeResultSets;
/*      */   public ResultSet m_currentResultSet;
/*      */   public String m_currentSetName;
/*      */   public boolean m_alwaysUseActiveForGet;
/*   92 */   public boolean m_preferActiveData = true;
/*      */   public Vector m_unstructuredData;
/*      */   public boolean m_checkLocalLocalization;
/*      */   public boolean m_checkResultSetLocalization;
/*      */   public boolean m_determinedDataDateFormat;
/*      */   public boolean m_convertDatabaseDateFormats;
/*      */   public IdcDateFormat m_blDateFormat;
/*      */   public Map m_blFieldTypes;
/*      */   public IdcDateFormat m_localeDateFormat;
/*      */   public Map m_localizedFields;
/*      */   public Map m_localizedResultSets;
/*      */   public Vector m_rsetSuggestedOrder;
/*      */   public Vector m_optListSuggestedOrder;
/*      */   public Boolean m_shouldSortOptionLists;
/*      */   public Boolean m_shouldSortProperties;
/*      */   public Boolean m_shouldSortResultSets;
/*      */   public Vector m_tempFiles;
/*      */   public static String m_tempDir;
/*      */   public String m_overrideTempDir;
/*      */   public BufferedInputStream m_inStream;
/*  185 */   public byte[] m_tempByteBuf = new byte[1024];
/*      */ 
/*  187 */   public char[] m_tmpCharArray = new char[1024];
/*      */   public BufferedReader m_bufReader;
/*      */   public static boolean[] m_fileSyncObj;
/*      */   public static long m_fileCounter;
/*      */   public Map m_previousValues;
/*      */   public static final int CONTENT_ERROR = -1;
/*      */   public static final int URLENCODED_CONTENT = 0;
/*      */   public static final int MULTIPART_CONTENT = 1;
/*  202 */   public String m_contentType = "text/html";
/*      */   public static final int UNKNOWN_FORMAT = 0;
/*      */   public static final int HDA_FORMAT = 1;
/*      */   public static final int UPLOAD_FORMAT = 2;
/*      */   public static final int DOWNLOAD_FORMAT = 3;
/*      */   public boolean m_isExternalRequest;
/*      */   public boolean m_isStandardHttpRequest;
/*  234 */   public boolean m_isJava = true;
/*      */   public boolean m_isCgi;
/*      */   public boolean m_isGet;
/*      */   public boolean m_ioLocalOnly;
/*      */   public static final String[] DATA_TYPES;
/*      */   public long m_remainingLength;
/*      */   public int m_extraTailLength;
/*      */   public boolean m_hasAttachedFiles;
/*      */   public boolean m_isSuspended;
/*  312 */   public boolean m_isSkipFile = true;
/*      */   public String m_suspendedFileKey;
/*      */   public String m_boundary;
/*      */   public byte[] m_boundaryBytes;
/*      */   public ReportProgress m_progress;
/*      */   public String m_javaEncoding;
/*      */   public String m_clientEncoding;
/*      */   public boolean m_determinedEncoding;
/*      */   public boolean m_isNonStandardResponse;
/*      */   public Class m_mutableClass;
/*      */   protected DataFormatter m_dataFormat;
/*      */   protected String m_dataFormatOptions;
/*      */   public static int F_CLONE_LOCALDATA;
/*      */   public static int F_CLONE_RESULTSETS;
/*      */   public static int F_CLONE_FILES;
/*      */   public static int F_CLONE_ENCODINGFLAG;
/*      */ 
/*      */   public DataBinder()
/*      */   {
/*  368 */     this.m_environment = new IdcProperties();
/*  369 */     this.m_localData = new IdcProperties();
/*  370 */     this.m_tempFiles = new IdcVector();
/*  371 */     this.m_unstructuredData = new IdcVector();
/*  372 */     this.m_optListSuggestedOrder = new IdcVector();
/*  373 */     this.m_blDateFormat = new IdcDateFormat();
/*  374 */     this.m_blDateFormat.initDefault();
/*  375 */     this.m_blFieldTypes = new HashMap();
/*  376 */     this.m_localizedFields = new HashMap();
/*  377 */     this.m_previousValues = new HashMap();
/*      */ 
/*  379 */     clearResultSets();
/*      */   }
/*      */ 
/*      */   public DataBinder(boolean isJava)
/*      */   {
/*  384 */     this.m_environment = new IdcProperties();
/*  385 */     this.m_localData = new IdcProperties();
/*  386 */     this.m_tempFiles = new IdcVector();
/*  387 */     this.m_unstructuredData = new IdcVector();
/*  388 */     this.m_blDateFormat = new IdcDateFormat();
/*  389 */     this.m_blDateFormat.initDefault();
/*  390 */     this.m_blFieldTypes = new HashMap();
/*  391 */     this.m_localizedFields = new HashMap();
/*  392 */     this.m_previousValues = new HashMap();
/*      */ 
/*  394 */     clearResultSets();
/*  395 */     if (!isJava)
/*      */       return;
/*  397 */     this.m_isJava = true;
/*  398 */     this.m_isCgi = false;
/*      */   }
/*      */ 
/*      */   public DataBinder(Properties env)
/*      */   {
/*  409 */     this.m_environment = new IdcProperties(env);
/*  410 */     this.m_localData = new IdcProperties();
/*  411 */     this.m_tempFiles = new IdcVector();
/*  412 */     this.m_unstructuredData = new IdcVector();
/*  413 */     this.m_blDateFormat = new IdcDateFormat();
/*  414 */     this.m_blDateFormat.initDefault();
/*  415 */     this.m_blFieldTypes = new HashMap();
/*  416 */     this.m_localizedFields = new HashMap();
/*  417 */     this.m_previousValues = new HashMap();
/*      */ 
/*  419 */     clearResultSets();
/*      */   }
/*      */ 
/*      */   public void clearResultSets()
/*      */   {
/*  430 */     this.m_optionLists = new Hashtable();
/*  431 */     this.m_resultSets = new HashMap();
/*  432 */     this.m_activeResultSets = new ArrayList();
/*  433 */     this.m_rsetSuggestedOrder = new IdcVector();
/*  434 */     this.m_optListSuggestedOrder = new IdcVector();
/*  435 */     this.m_localizedResultSets = new HashMap();
/*  436 */     this.m_checkResultSetLocalization = false;
/*      */ 
/*  438 */     this.m_currentResultSet = null;
/*  439 */     this.m_currentSetName = "";
/*      */   }
/*      */ 
/*      */   public DataBinder createShallowCopy()
/*      */   {
/*  455 */     DataBinder retVal = createLocaleEquivalentDataBinder();
/*  456 */     retVal.m_localData = this.m_localData;
/*  457 */     retVal.m_optionLists = this.m_optionLists;
/*  458 */     retVal.m_resultSets = this.m_resultSets;
/*      */ 
/*  460 */     retVal.m_unstructuredData = this.m_unstructuredData;
/*  461 */     retVal.m_rsetSuggestedOrder = this.m_rsetSuggestedOrder;
/*  462 */     retVal.m_optListSuggestedOrder = this.m_optListSuggestedOrder;
/*      */ 
/*  464 */     retVal.m_localizedFields = this.m_localizedFields;
/*  465 */     retVal.m_localizedResultSets = this.m_localizedResultSets;
/*      */ 
/*  467 */     retVal.m_checkLocalLocalization = this.m_checkLocalLocalization;
/*  468 */     retVal.m_checkResultSetLocalization = this.m_checkResultSetLocalization;
/*      */ 
/*  470 */     return retVal;
/*      */   }
/*      */ 
/*      */   public DataBinder createLocaleEquivalentDataBinder()
/*      */   {
/*  481 */     DataBinder binder = new DataBinder();
/*  482 */     binder.m_environment = this.m_environment;
/*  483 */     binder.m_blDateFormat = this.m_blDateFormat;
/*  484 */     binder.m_blFieldTypes = this.m_blFieldTypes;
/*  485 */     binder.m_localeDateFormat = this.m_localeDateFormat;
/*  486 */     binder.m_preferActiveData = this.m_preferActiveData;
/*      */ 
/*  490 */     binder.m_isJava = true;
/*  491 */     binder.m_isCgi = false;
/*  492 */     return binder;
/*      */   }
/*      */ 
/*      */   public DataBinder createShallowCopyCloneResultSets()
/*      */   {
/*  501 */     DataBinder retVal = createShallowCopy();
/*  502 */     return cloneResultSets(retVal);
/*      */   }
/*      */ 
/*      */   public DataBinder cloneResultSets(DataBinder binder)
/*      */   {
/*  508 */     binder.m_resultSets = cloneMap(this.m_resultSets);
/*      */ 
/*  510 */     Set keys = binder.m_resultSets.keySet();
/*  511 */     for (String key : keys)
/*      */     {
/*  513 */       Object obj = binder.m_resultSets.get(key);
/*  514 */       if (obj instanceof DataResultSet)
/*      */       {
/*  516 */         DataResultSet drset = (DataResultSet)obj;
/*  517 */         binder.m_resultSets.put(key, drset.shallowClone());
/*      */       }
/*      */     }
/*      */ 
/*  521 */     binder.m_activeResultSets = cloneList(this.m_activeResultSets);
/*  522 */     binder.m_rsetSuggestedOrder = ((Vector)this.m_rsetSuggestedOrder.clone());
/*  523 */     binder.m_localizedResultSets = cloneMap(this.m_localizedResultSets);
/*  524 */     return binder;
/*      */   }
/*      */ 
/*      */   public DataBinder createShallowCopyWithClones(int flags)
/*      */   {
/*  534 */     DataBinder binder = createShallowCopy();
/*  535 */     if ((flags & F_CLONE_RESULTSETS) != 0)
/*      */     {
/*  537 */       cloneResultSets(binder);
/*      */     }
/*      */ 
/*  540 */     if ((flags & F_CLONE_LOCALDATA) != 0)
/*      */     {
/*  542 */       Properties props = (Properties)this.m_localData.clone();
/*  543 */       binder.setLocalData(props);
/*      */     }
/*      */ 
/*  546 */     if ((flags & F_CLONE_FILES) != 0)
/*      */     {
/*  548 */       binder.m_isSuspended = this.m_isSuspended;
/*  549 */       binder.m_suspendedFileKey = this.m_suspendedFileKey;
/*  550 */       binder.m_boundary = this.m_boundary;
/*  551 */       binder.m_boundaryBytes = this.m_boundaryBytes;
/*  552 */       binder.m_tempFiles = ((Vector)this.m_tempFiles.clone());
/*      */     }
/*      */ 
/*  555 */     if ((flags & F_CLONE_ENCODINGFLAG) != 0)
/*      */     {
/*  557 */       binder.m_isJava = this.m_isJava;
/*  558 */       binder.m_isCgi = this.m_isCgi;
/*      */     }
/*      */ 
/*  562 */     return binder;
/*      */   }
/*      */ 
/*      */   public void copyResultSetStateShallow(DataBinder binder)
/*      */   {
/*  568 */     this.m_optionLists = binder.m_optionLists;
/*  569 */     this.m_resultSets = binder.m_resultSets;
/*  570 */     this.m_activeResultSets = binder.m_activeResultSets;
/*  571 */     this.m_rsetSuggestedOrder = binder.m_rsetSuggestedOrder;
/*  572 */     this.m_optListSuggestedOrder = binder.m_optListSuggestedOrder;
/*      */ 
/*  574 */     this.m_currentResultSet = binder.m_currentResultSet;
/*  575 */     this.m_currentSetName = binder.m_currentSetName;
/*  576 */     this.m_localizedResultSets = binder.m_localizedResultSets;
/*  577 */     this.m_checkResultSetLocalization = binder.m_checkResultSetLocalization;
/*      */ 
/*  579 */     this.m_preferActiveData = binder.m_preferActiveData;
/*      */   }
/*      */ 
/*      */   public void copyLocalDataStateShallow(DataBinder binder)
/*      */   {
/*  591 */     this.m_localData = binder.m_localData;
/*  592 */     this.m_blFieldTypes = binder.m_blFieldTypes;
/*  593 */     this.m_localizedFields = binder.m_localizedFields;
/*  594 */     this.m_checkLocalLocalization = binder.m_checkLocalLocalization;
/*      */   }
/*      */ 
/*      */   public void copyLocalDataStateClone(DataBinder binder)
/*      */   {
/*  604 */     this.m_localData = ((Properties)binder.cloneMap(binder.m_localData));
/*  605 */     this.m_blFieldTypes = binder.cloneMap(this.m_blFieldTypes);
/*  606 */     this.m_localizedFields = binder.cloneMap(this.m_localizedFields);
/*  607 */     this.m_checkLocalLocalization = binder.m_checkLocalLocalization;
/*      */   }
/*      */ 
/*      */   public void applyPrefix(String prefix, int flags)
/*      */   {
/*  618 */     this.m_localData = applyPrefix(prefix, this.m_localData);
/*  619 */     this.m_blFieldTypes = applyPrefix(prefix, this.m_blFieldTypes);
/*      */   }
/*      */ 
/*      */   public Properties applyPrefix(String prefix, Map props)
/*      */   {
/*  624 */     Properties newProps = new IdcProperties();
/*  625 */     for (Iterator i$ = props.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*      */ 
/*  627 */       Object val = props.get(key);
/*  628 */       newProps.put(prefix + key, val); }
/*      */ 
/*  630 */     return newProps;
/*      */   }
/*      */ 
/*      */   public void merge(DataBinder newData)
/*      */   {
/*  642 */     mergeHashTablesInternal(this.m_localData, newData.m_localData, newData, false);
/*      */ 
/*  647 */     mergeHashTables(this.m_environment, newData.m_environment);
/*  648 */     mergeHashTables(this.m_blFieldTypes, newData.m_blFieldTypes);
/*  649 */     mergeHashTablesInternal(this.m_optionLists, newData.m_optionLists, newData, false);
/*  650 */     mergeHashTablesInternal(this.m_resultSets, newData.m_resultSets, newData, true);
/*      */ 
/*  652 */     mergeVectors(this.m_unstructuredData, newData.m_unstructuredData);
/*  653 */     mergeVectors(this.m_rsetSuggestedOrder, newData.m_rsetSuggestedOrder);
/*  654 */     mergeVectors(this.m_optListSuggestedOrder, newData.m_optListSuggestedOrder);
/*      */   }
/*      */ 
/*      */   public void mergeResultSets(DataBinder newData)
/*      */   {
/*  659 */     mergeHashTablesInternal(this.m_resultSets, newData.m_resultSets, newData, true);
/*      */   }
/*      */ 
/*      */   public void mergeResultSetRowIntoLocalData(ResultSet rset)
/*      */   {
/*  667 */     if (!rset.isRowPresent())
/*      */     {
/*  669 */       return;
/*      */     }
/*  671 */     addLocaleFields(rset);
/*  672 */     boolean oldSetLocalization = this.m_checkResultSetLocalization;
/*  673 */     boolean isSyncLocalization = attemptRawSynchronizeLocale(rset);
/*  674 */     if (!isSyncLocalization)
/*      */     {
/*  676 */       this.m_checkResultSetLocalization = true;
/*      */     }
/*  678 */     int nfields = rset.getNumFields();
/*  679 */     FieldInfo fi = new FieldInfo();
/*      */ 
/*  681 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/*  683 */       rset.getIndexFieldInfo(i, fi);
/*  684 */       String val = getResultSetValueWithFieldInfo(rset, fi);
/*  685 */       this.m_localData.put(fi.m_name, val);
/*      */     }
/*  687 */     this.m_checkResultSetLocalization = oldSetLocalization;
/*      */   }
/*      */ 
/*      */   public void mergeHashTablesInternal(Hashtable current, Hashtable newData, DataBinder binder, boolean isResultSets)
/*      */   {
/*  700 */     mergeHashTablesInternal(current, newData, binder, isResultSets);
/*      */   }
/*      */ 
/*      */   public void mergeHashTablesInternal(Map current, Map newData, DataBinder binder, boolean isResultSets)
/*      */   {
/*  713 */     if (newData == null)
/*      */     {
/*  715 */       return;
/*      */     }
/*  717 */     if ((!isResultSets) && (binder.m_checkLocalLocalization))
/*      */     {
/*  719 */       this.m_checkLocalLocalization = true;
/*      */     }
/*  721 */     if ((isResultSets) && (binder.m_checkResultSetLocalization))
/*      */     {
/*  723 */       this.m_checkResultSetLocalization = true;
/*      */     }
/*      */ 
/*  726 */     boolean localeFormatMatch = false;
/*  727 */     if (((this.m_localeDateFormat == null) && (binder.m_localeDateFormat == null)) || ((this.m_localeDateFormat != null) && (binder.m_localeDateFormat != null) && (this.m_localeDateFormat.equals(binder.m_localeDateFormat))))
/*      */     {
/*  731 */       localeFormatMatch = true;
/*      */     }
/*      */ 
/*  734 */     for (Iterator e = newData.keySet().iterator(); e.hasNext(); )
/*      */     {
/*  736 */       Object obj = e.next();
/*  737 */       if (obj == null) {
/*      */         continue;
/*      */       }
/*  740 */       String key = (String)obj;
/*  741 */       obj = newData.get(key);
/*  742 */       if (key.equals("blFieldTypes")) continue; if (key.equals("blDateFormat"))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  747 */       if (!isResultSets)
/*      */       {
/*  749 */         boolean alreadyLocalized = binder.m_localizedFields.get(key) != null;
/*  750 */         boolean noConversionNeeded = (alreadyLocalized) && (localeFormatMatch);
/*  751 */         String type = (String)binder.m_blFieldTypes.get(key);
/*  752 */         if (type != null)
/*      */         {
/*  754 */           this.m_blFieldTypes.put(key, type);
/*      */         }
/*  756 */         if (noConversionNeeded)
/*      */         {
/*  758 */           this.m_localizedFields.put(key, "");
/*      */         }
/*  760 */         if ((type != null) && (!noConversionNeeded) && 
/*  762 */           (type.equals("date")))
/*      */         {
/*  764 */           Date d = null;
/*  765 */           if (obj instanceof Date)
/*      */           {
/*  767 */             d = (Date)obj;
/*      */           }
/*  769 */           if ((d == null) && (binder.m_localeDateFormat != null) && (alreadyLocalized))
/*      */           {
/*      */             try
/*      */             {
/*  774 */               d = binder.m_localeDateFormat.parseDate((String)obj);
/*      */             }
/*      */             catch (ParseException ignore)
/*      */             {
/*  778 */               if (SystemUtils.m_verbose)
/*      */               {
/*  780 */                 Report.debug("systemparse", null, ignore);
/*      */               }
/*      */             }
/*      */           }
/*  784 */           if (d == null)
/*      */           {
/*      */             try
/*      */             {
/*  788 */               String s = (String)obj;
/*  789 */               if ((s != null) && (s.length() > 0))
/*      */               {
/*  791 */                 d = binder.m_blDateFormat.parseDate(s);
/*      */               }
/*      */             }
/*      */             catch (ParseException ignore)
/*      */             {
/*  796 */               if (SystemUtils.m_verbose)
/*      */               {
/*  798 */                 Report.debug("systemparse", null, ignore);
/*      */               }
/*      */             }
/*      */           }
/*      */           String value;
/*      */           String value;
/*  803 */           if (d == null)
/*      */           {
/*  805 */             value = (String)obj;
/*      */           }
/*      */           else
/*      */           {
/*      */             String value;
/*  809 */             if ((this.m_localeDateFormat != null) && (this.m_localizedFields.get(key) != null))
/*      */             {
/*  811 */               value = this.m_localeDateFormat.format(d);
/*      */             }
/*      */             else
/*      */             {
/*  815 */               value = this.m_blDateFormat.format(d);
/*      */             }
/*      */           }
/*      */ 
/*  819 */           obj = value;
/*      */         }
/*      */ 
/*      */       }
/*  825 */       else if (obj instanceof DataResultSet)
/*      */       {
/*  827 */         DataResultSet drset = (DataResultSet)obj;
/*      */ 
/*  829 */         if ((drset.getSupportedFeatures() & 1L) != 0L)
/*      */         {
/*  831 */           obj = drset.shallowClone();
/*      */         }
/*      */ 
/*  836 */         IdcDateFormat fmt = drset.getDateFormat();
/*  837 */         if ((this.m_localeDateFormat != null) && (fmt != null) && (!fmt.equals(this.m_localeDateFormat)))
/*      */         {
/*  840 */           this.m_checkResultSetLocalization = true;
/*      */         }
/*      */ 
/*  845 */         this.m_localizedResultSets.remove(key);
/*      */       }
/*      */ 
/*  849 */       current.put(key, obj);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void mergeHashTables(Hashtable current, Hashtable newData)
/*      */   {
/*  864 */     if (newData == null)
/*      */     {
/*  866 */       return;
/*      */     }
/*  868 */     current.putAll(newData);
/*      */   }
/*      */ 
/*      */   public static void mergeHashTables(Map current, Map newData)
/*      */   {
/*  880 */     if (newData == null)
/*      */     {
/*  882 */       return;
/*      */     }
/*  884 */     current.putAll(newData);
/*      */   }
/*      */ 
/*      */   public static void mergeVectors(Vector current, Vector newData)
/*      */   {
/*  895 */     if ((newData == null) || (newData == current))
/*      */     {
/*  897 */       return;
/*      */     }
/*      */ 
/*  900 */     List currentList = current;
/*  901 */     List newDataList = newData;
/*      */ 
/*  903 */     for (Iterator i$ = newDataList.iterator(); i$.hasNext(); ) { Object element = i$.next();
/*      */ 
/*  906 */       if (currentList.indexOf(element) < 0)
/*      */       {
/*  908 */         currentList.add(element);
/*      */       } }
/*      */ 
/*      */   }
/*      */ 
/*      */   public Map cloneMap(Map map)
/*      */   {
/*  922 */     if (map == null)
/*      */     {
/*  924 */       return map;
/*      */     }
/*  926 */     Map outMap = null;
/*  927 */     if (map instanceof HashMap)
/*      */     {
/*  929 */       HashMap m = (HashMap)map;
/*  930 */       outMap = (Map)m.clone();
/*      */     }
/*  932 */     else if (map instanceof Hashtable)
/*      */     {
/*  934 */       Hashtable m = (Hashtable)map;
/*  935 */       outMap = (Hashtable)m.clone();
/*      */     }
/*      */ 
/*  938 */     outMap.isEmpty();
/*  939 */     return outMap;
/*      */   }
/*      */ 
/*      */   public List cloneList(List list)
/*      */   {
/*  947 */     if (list == null)
/*      */     {
/*  949 */       return list;
/*      */     }
/*  951 */     List outList = null;
/*  952 */     if (list instanceof ArrayList)
/*      */     {
/*  954 */       ArrayList a = (ArrayList)list;
/*  955 */       outList = (List)a.clone();
/*      */     }
/*  957 */     else if (list instanceof Vector)
/*      */     {
/*  959 */       Vector v = (Vector)list;
/*  960 */       outList = (List)v.clone();
/*      */     }
/*      */ 
/*  963 */     outList.isEmpty();
/*  964 */     return outList;
/*      */   }
/*      */ 
/*      */   public IdcDateFormat getLocaleDateFormat()
/*      */   {
/*  975 */     if (null != this.m_localeDateFormat)
/*      */     {
/*  977 */       return this.m_localeDateFormat;
/*      */     }
/*      */ 
/*  985 */     return this.m_blDateFormat;
/*      */   }
/*      */ 
/*      */   public Date parseDate(String key, String date)
/*      */     throws DataException
/*      */   {
/*  993 */     Date d = null;
/*      */     try
/*      */     {
/*  996 */       if ((this.m_localeDateFormat != null) && (this.m_localizedFields.get(key) != null))
/*      */       {
/*  998 */         d = this.m_localeDateFormat.parseDate(date);
/*      */       }
/*      */       else
/*      */       {
/* 1002 */         d = this.m_blDateFormat.parseDate(date);
/*      */       }
/*      */     }
/*      */     catch (ParseException e)
/*      */     {
/* 1007 */       DataException de = new DataException(e.getMessage());
/* 1008 */       SystemUtils.setExceptionCause(de, e);
/* 1009 */       throw de;
/*      */     }
/*      */ 
/* 1012 */     return d;
/*      */   }
/*      */ 
/*      */   public String getActiveValue(String key)
/*      */     throws DataException
/*      */   {
/* 1025 */     return getEx(key, true, true, false, true);
/*      */   }
/*      */ 
/*      */   public String getActiveValueSearchAll(String key)
/*      */     throws DataException
/*      */   {
/* 1039 */     return getEx(key, true, true, true, true);
/*      */   }
/*      */ 
/*      */   public String get(String key)
/*      */     throws DataException
/*      */   {
/* 1057 */     return getEx(key, this.m_alwaysUseActiveForGet, true, true, true);
/*      */   }
/*      */ 
/*      */   public String getSystem(String key)
/*      */     throws DataException
/*      */   {
/* 1073 */     String value = getEx(key, this.m_alwaysUseActiveForGet, true, true, false);
/* 1074 */     if (value != null)
/*      */     {
/* 1076 */       value = convertToSystem(key, value);
/*      */     }
/* 1078 */     return value;
/*      */   }
/*      */ 
/*      */   public String get(String key, boolean isActive)
/*      */     throws DataException
/*      */   {
/* 1100 */     return getEx(key, isActive, true, !isActive, true);
/*      */   }
/*      */ 
/*      */   public String getSearchAllAllowMissing(String key)
/*      */   {
/*      */     try
/*      */     {
/* 1116 */       return getEx(key, this.m_alwaysUseActiveForGet, true, true, false);
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/* 1120 */       Report.trace(null, null, ignore);
/* 1121 */     }return null;
/*      */   }
/*      */ 
/*      */   public String getAllowMissing(String key)
/*      */   {
/*      */     try
/*      */     {
/* 1140 */       return getEx(key, true, true, true, false);
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/* 1144 */       Report.trace(null, null, ignore);
/* 1145 */     }return null;
/*      */   }
/*      */ 
/*      */   public String getActiveAllowMissing(String key)
/*      */   {
/*      */     try
/*      */     {
/* 1163 */       return getEx(key, true, true, false, false);
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/* 1167 */       Report.trace(null, null, ignore);
/* 1168 */     }return null;
/*      */   }
/*      */ 
/*      */   public String getFromSets(String key)
/*      */     throws DataException
/*      */   {
/* 1183 */     return getEx(key, true, false, true, true);
/*      */   }
/*      */ 
/*      */   public String getEx(String key, boolean fromActiveSets, boolean fromLocal, boolean fromAllSets, boolean mustExist)
/*      */     throws DataException
/*      */   {
/* 1214 */     String value = null;
/*      */ 
/* 1216 */     if ((fromActiveSets) || (this.m_preferActiveData))
/*      */     {
/* 1219 */       int numActive = this.m_activeResultSets.size();
/* 1220 */       for (int i = numActive - 1; 0 <= i; --i)
/*      */       {
/* 1222 */         Object[] obj = (Object[])(Object[])this.m_activeResultSets.get(i);
/* 1223 */         ResultSet rset = (ResultSet)obj[1];
/* 1224 */         value = getResultSetValue(rset, key);
/* 1225 */         if (value != null)
/*      */         {
/* 1227 */           return value;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1232 */     if (fromLocal)
/*      */     {
/* 1234 */       value = getLocal(key);
/* 1235 */       if (value != null)
/*      */       {
/* 1237 */         return value;
/*      */       }
/*      */     }
/*      */     Iterator e;
/* 1241 */     if ((fromAllSets) || (this.m_currentResultSet == null))
/*      */     {
/* 1244 */       for (e = this.m_resultSets.values().iterator(); e.hasNext(); )
/*      */       {
/* 1246 */         ResultSet rset = (ResultSet)e.next();
/* 1247 */         value = getResultSetValue(rset, key);
/* 1248 */         if (value != null)
/*      */         {
/* 1250 */           return value;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1255 */     value = this.m_environment.getProperty(key);
/* 1256 */     if ((value == null) && (mustExist))
/*      */     {
/* 1258 */       throw new DataException(LocaleUtils.encodeMessage("syParameterNotFound", null, key));
/*      */     }
/*      */ 
/* 1261 */     return value;
/*      */   }
/*      */ 
/*      */   public String getResultSetValue(ResultSet rset, String key)
/*      */   {
/* 1270 */     String value = null;
/* 1271 */     if (rset.isRowPresent())
/*      */     {
/* 1273 */       value = getResultSetValueWithFieldNameOrFieldInfo(rset, key, null);
/*      */     }
/* 1275 */     return value;
/*      */   }
/*      */ 
/*      */   protected String getResultSetValueWithFieldInfo(ResultSet rset, FieldInfo info)
/*      */   {
/* 1280 */     return getResultSetValueWithFieldNameOrFieldInfo(rset, info.m_name, info);
/*      */   }
/*      */ 
/*      */   protected String getResultSetValueWithFieldNameOrFieldInfo(ResultSet rset, String key, FieldInfo info)
/*      */   {
/* 1285 */     String value = null;
/* 1286 */     if (info != null)
/*      */     {
/* 1288 */       value = rset.getStringValue(info.m_index);
/*      */     }
/* 1290 */     else if (key != null)
/*      */     {
/* 1292 */       value = rset.getStringValueByName(key);
/*      */     }
/*      */ 
/* 1298 */     if ((value == null) || (value.length() == 0))
/*      */     {
/* 1300 */       return value;
/*      */     }
/*      */ 
/* 1303 */     IdcDateFormat localeDateFormat = this.m_localeDateFormat;
/* 1304 */     boolean isClonable = true;
/* 1305 */     if ((rset.isMutable()) && (rset instanceof MutableResultSet) && ((((MutableResultSet)rset).getSupportedFeatures() & 1L) == 0L))
/*      */     {
/* 1308 */       localeDateFormat = this.m_blDateFormat;
/* 1309 */       isClonable = false;
/*      */     }
/* 1311 */     if ((this.m_checkResultSetLocalization) || (!isClonable))
/*      */     {
/* 1313 */       IdcDateFormat fmt = rset.getDateFormat();
/* 1314 */       if (info == null)
/*      */       {
/* 1316 */         info = new FieldInfo();
/* 1317 */         rset.getFieldInfo(key, info);
/*      */       }
/* 1319 */       if ((info.m_type == 5) && (fmt != null) && (localeDateFormat != null) && (!localeDateFormat.equals(fmt)) && (value.length() > 0))
/*      */       {
/* 1322 */         Date d = rset.getDateValue(info.m_index);
/* 1323 */         value = localeDateFormat.format(d);
/*      */       }
/*      */     }
/* 1326 */     return value;
/*      */   }
/*      */ 
/*      */   public String putLocal(String key, String value)
/*      */   {
/* 1336 */     assert (value != null);
/* 1337 */     if (this.m_checkLocalLocalization)
/*      */     {
/* 1339 */       String typeStr = (String)this.m_blFieldTypes.get(key);
/* 1340 */       if ((typeStr != null) && (typeStr.equals("date")))
/*      */       {
/* 1342 */         this.m_localizedFields.put(key, "");
/*      */       }
/*      */     }
/* 1345 */     Object obj = this.m_localData.put(key, value);
/* 1346 */     if (obj instanceof String)
/*      */     {
/* 1348 */       value = (String)obj;
/*      */     }
/*      */     else
/*      */     {
/* 1352 */       if (obj != null)
/*      */       {
/* 1354 */         Report.trace(null, "local data contains wrong type for key: (" + key + "), value is: (" + obj + "), value class is:" + obj.getClass().getName(), null);
/*      */       }
/*      */ 
/* 1357 */       value = null;
/*      */     }
/* 1359 */     return localizeLocal(key, value);
/*      */   }
/*      */ 
/*      */   public String putLocalDate(String key, Date value)
/*      */   {
/* 1364 */     String str = null;
/* 1365 */     boolean isLocalized = false;
/* 1366 */     if ((this.m_checkLocalLocalization) && (this.m_localeDateFormat != null))
/*      */     {
/* 1368 */       str = this.m_localeDateFormat.format(value);
/* 1369 */       isLocalized = true;
/*      */     }
/*      */     else
/*      */     {
/* 1373 */       str = this.m_blDateFormat.format(value);
/*      */     }
/* 1375 */     this.m_blFieldTypes.put(key, "date");
/* 1376 */     String retVal = (String)this.m_localData.put(key, str);
/* 1377 */     retVal = localizeLocal(key, retVal);
/* 1378 */     if (isLocalized)
/*      */     {
/* 1380 */       this.m_localizedFields.put(key, "");
/*      */     }
/* 1382 */     return retVal;
/*      */   }
/*      */ 
/*      */   public String getLocal(String key)
/*      */   {
/* 1393 */     String value = this.m_localData.getProperty(key);
/* 1394 */     return localizeLocal(key, value);
/*      */   }
/*      */ 
/*      */   public String convertToSystem(String key, String value)
/*      */   {
/* 1407 */     if (value == null)
/*      */     {
/* 1409 */       return null;
/*      */     }
/*      */ 
/* 1412 */     String type = (String)this.m_blFieldTypes.get(key);
/* 1413 */     if ((type != null) && (type.equalsIgnoreCase("date")) && (value.length() > 0) && (!value.startsWith("{ts '"))) {
/*      */       try
/*      */       {
/*      */         Date d;
/*      */         Date d;
/* 1419 */         if (this.m_localeDateFormat != null)
/*      */         {
/* 1421 */           d = this.m_localeDateFormat.parseDate(value);
/*      */         }
/*      */         else
/*      */         {
/* 1425 */           d = this.m_blDateFormat.parseDate(value);
/*      */         }
/* 1427 */         value = LocaleUtils.formatODBC(d);
/*      */       }
/*      */       catch (ParseException ignore)
/*      */       {
/* 1431 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1433 */           Report.debug("systemparse", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/* 1437 */     return value;
/*      */   }
/*      */ 
/*      */   public String localizeLocal(String key, String value)
/*      */   {
/* 1448 */     if ((value != null) && (this.m_checkLocalLocalization) && (value.length() > 0))
/*      */     {
/* 1450 */       boolean isDate = false;
/* 1451 */       String tmp = (String)this.m_blFieldTypes.get(key);
/* 1452 */       if ((tmp != null) && (tmp.equals("date")))
/*      */       {
/* 1454 */         isDate = true;
/*      */       }
/* 1456 */       if ((isDate) && (this.m_blDateFormat != null) && (this.m_localeDateFormat != null) && (!this.m_localeDateFormat.equals(this.m_blDateFormat)))
/*      */       {
/* 1459 */         boolean isLocalized = this.m_localizedFields.get(key) != null;
/* 1460 */         if (!isLocalized)
/*      */         {
/*      */           try
/*      */           {
/* 1464 */             Date d = this.m_blDateFormat.parseDate(value);
/* 1465 */             value = this.m_localeDateFormat.format(d);
/* 1466 */             this.m_localData.put(key, value);
/* 1467 */             this.m_localizedFields.put(key, "");
/*      */           }
/*      */           catch (ParseException ignore)
/*      */           {
/* 1471 */             String msg = LocaleResources.getString("syUnableToLocalizeDate", null, value);
/* 1472 */             Report.trace(null, msg, ignore);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1477 */     return value;
/*      */   }
/*      */ 
/*      */   public Map getLocalizedFields()
/*      */   {
/* 1485 */     return this.m_localizedFields;
/*      */   }
/*      */ 
/*      */   public String getFieldType(String name)
/*      */   {
/* 1494 */     return (String)this.m_blFieldTypes.get(name);
/*      */   }
/*      */ 
/*      */   public void setFieldType(String name, String type)
/*      */   {
/* 1504 */     this.m_blFieldTypes.put(name, type);
/*      */   }
/*      */ 
/*      */   public void addLocaleFields(ResultSet rset)
/*      */   {
/* 1515 */     int count = rset.getNumFields();
/* 1516 */     FieldInfo info = new FieldInfo();
/* 1517 */     for (int i = 0; i < count; ++i)
/*      */     {
/* 1519 */       rset.getIndexFieldInfo(i, info);
/* 1520 */       if (info.m_type != 5)
/*      */         continue;
/* 1522 */       this.m_blFieldTypes.put(info.m_name, "date");
/*      */     }
/*      */   }
/*      */ 
/*      */   public Map getFieldTypes()
/*      */   {
/* 1532 */     return this.m_blFieldTypes;
/*      */   }
/*      */ 
/*      */   public void setFieldTypes(Map types)
/*      */   {
/* 1540 */     this.m_blFieldTypes = types;
/*      */   }
/*      */ 
/*      */   public boolean attemptRawSynchronizeLocale(ResultSet rset)
/*      */   {
/* 1551 */     boolean result = false;
/*      */ 
/* 1553 */     IdcDateFormat fmt = null;
/* 1554 */     if (this.m_localeDateFormat != null)
/*      */     {
/* 1556 */       fmt = this.m_localeDateFormat;
/*      */     }
/* 1558 */     else if (this.m_blDateFormat != null)
/*      */     {
/* 1561 */       fmt = this.m_blDateFormat;
/*      */     }
/*      */ 
/* 1564 */     if (fmt != null)
/*      */     {
/* 1566 */       if (rset.hasRawObjects())
/*      */       {
/* 1568 */         rset.setDateFormat(fmt);
/* 1569 */         result = true;
/*      */       }
/*      */       else
/*      */       {
/* 1573 */         IdcDateFormat rsDateFormat = rset.getDateFormat();
/* 1574 */         if ((rsDateFormat != null) && (rsDateFormat.equals(fmt)))
/*      */         {
/* 1576 */           result = true;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1581 */     return result;
/*      */   }
/*      */ 
/*      */   public void determineLocaleChecks()
/*      */   {
/* 1589 */     if (this.m_localeDateFormat == null)
/*      */     {
/* 1591 */       return;
/*      */     }
/* 1593 */     if (!this.m_localeDateFormat.equals(this.m_blDateFormat))
/*      */     {
/* 1595 */       this.m_checkLocalLocalization = true;
/*      */     }
/* 1597 */     Iterator it = this.m_resultSets.keySet().iterator();
/* 1598 */     while (it.hasNext())
/*      */     {
/* 1600 */       String name = (String)it.next();
/* 1601 */       ResultSet rset = getResultSet(name);
/* 1602 */       IdcDateFormat rsetFormat = rset.getDateFormat();
/* 1603 */       if ((rsetFormat != null) && (!this.m_localeDateFormat.equals(rsetFormat)))
/*      */       {
/* 1605 */         this.m_checkResultSetLocalization = true;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void removeLocal(String key)
/*      */   {
/* 1618 */     this.m_localData.remove(key);
/*      */   }
/*      */ 
/*      */   public Properties getLocalData()
/*      */   {
/* 1628 */     return this.m_localData;
/*      */   }
/*      */ 
/*      */   public void setLocalData(Properties localData)
/*      */   {
/* 1639 */     this.m_localData = localData;
/*      */   }
/*      */ 
/*      */   public Properties getEnvironment()
/*      */   {
/* 1653 */     return this.m_environment;
/*      */   }
/*      */ 
/*      */   public void setEnvironment(Properties env)
/*      */   {
/* 1670 */     if (!DataSerializeUtils.validateEnvironmentData(env))
/*      */     {
/* 1672 */       Report.trace("system", null, new Exception("Call to DataBinder.setEnvironment done with SharedObjects environment object"));
/* 1673 */       env = new IdcProperties(env);
/*      */     }
/* 1675 */     this.m_environment = env;
/*      */   }
/*      */ 
/*      */   public String getEnvironmentValue(String key)
/*      */   {
/* 1686 */     return this.m_environment.getProperty(key);
/*      */   }
/*      */ 
/*      */   public void setEnvironmentValue(String key, String value)
/*      */   {
/* 1696 */     this.m_environment.put(key, value);
/*      */   }
/*      */ 
/*      */   public ResultSet getCurrentActiveResultSet()
/*      */   {
/* 1709 */     return this.m_currentResultSet;
/*      */   }
/*      */ 
/*      */   public void addOptionList(String name, Vector options)
/*      */   {
/* 1722 */     this.m_optionLists.put(name, options);
/*      */   }
/*      */ 
/*      */   public Vector getOptionList(String name)
/*      */   {
/* 1733 */     return (Vector)this.m_optionLists.get(name);
/*      */   }
/*      */ 
/*      */   public Enumeration getOptionLists()
/*      */   {
/* 1742 */     return this.m_optionLists.keys();
/*      */   }
/*      */ 
/*      */   public boolean nextRow(String setName)
/*      */     throws IOException
/*      */   {
/* 1772 */     boolean isNewActiveSet = false;
/* 1773 */     if (!this.m_currentSetName.equals(setName))
/*      */     {
/* 1776 */       ResultSet rset = getActiveSet(setName);
/* 1777 */       if (rset == null)
/*      */       {
/* 1780 */         rset = getResultSet(setName);
/* 1781 */         isNewActiveSet = true;
/*      */       }
/* 1783 */       if ((rset == null) || (rset.isEmpty()))
/*      */       {
/* 1785 */         return false;
/*      */       }
/*      */ 
/* 1788 */       this.m_currentSetName = setName;
/* 1789 */       this.m_currentResultSet = rset;
/* 1790 */       if (isNewActiveSet == true)
/*      */       {
/* 1792 */         this.m_currentResultSet.first();
/* 1793 */         pushActiveResultSet(setName, rset);
/* 1794 */         return true;
/*      */       }
/*      */     }
/*      */ 
/* 1798 */     boolean isPresent = this.m_currentResultSet.next();
/* 1799 */     if (!isPresent)
/*      */     {
/* 1802 */       popActiveResultSet();
/*      */     }
/* 1804 */     return isPresent;
/*      */   }
/*      */ 
/*      */   public boolean isActiveSet(String rsetName)
/*      */   {
/* 1814 */     ResultSet rset = getActiveSet(rsetName);
/* 1815 */     return rset != null;
/*      */   }
/*      */ 
/*      */   public ResultSet popActiveResultSet()
/*      */   {
/* 1826 */     this.m_currentResultSet = null;
/* 1827 */     this.m_currentSetName = "";
/* 1828 */     int len = this.m_activeResultSets.size();
/* 1829 */     if (len == 0)
/*      */     {
/* 1831 */       return null;
/*      */     }
/* 1833 */     Object[] obj = (Object[])(Object[])this.m_activeResultSets.remove(len - 1);
/* 1834 */     --len;
/* 1835 */     if (len > 0)
/*      */     {
/* 1838 */       Object[] prevObj = (Object[])(Object[])this.m_activeResultSets.get(len - 1);
/* 1839 */       this.m_currentSetName = ((String)prevObj[0]);
/* 1840 */       this.m_currentResultSet = ((ResultSet)prevObj[1]);
/*      */     }
/* 1842 */     return (ResultSet)obj[1];
/*      */   }
/*      */ 
/*      */   public void pushActiveResultSet(String name, ResultSet aSet)
/*      */   {
/* 1856 */     Object[] obj = { name, aSet };
/* 1857 */     this.m_activeResultSets.add(obj);
/*      */   }
/*      */ 
/*      */   public ResultSet getActiveSet(String name)
/*      */   {
/* 1869 */     int size = this.m_activeResultSets.size();
/* 1870 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1872 */       Object[] obj = (Object[])(Object[])this.m_activeResultSets.get(i);
/* 1873 */       String objStr = (String)obj[0];
/* 1874 */       if (name.equals(objStr) == true)
/*      */       {
/* 1876 */         return (ResultSet)obj[1];
/*      */       }
/*      */     }
/* 1879 */     return null;
/*      */   }
/*      */ 
/*      */   public Enumeration getResultSetList()
/*      */   {
/* 1888 */     return Collections.enumeration(this.m_resultSets.keySet());
/*      */   }
/*      */ 
/*      */   public Map getResultSets()
/*      */   {
/* 1896 */     return this.m_resultSets;
/*      */   }
/*      */ 
/*      */   public ResultSet getResultSet(String name)
/*      */   {
/* 1907 */     return (ResultSet)this.m_resultSets.get(name);
/*      */   }
/*      */ 
/*      */   public ResultSet addResultSet(String name, ResultSet rset)
/*      */   {
/* 1926 */     if (name == null)
/*      */     {
/* 1928 */       return null;
/*      */     }
/*      */ 
/* 1933 */     addLocaleFields(rset);
/*      */ 
/* 1937 */     IdcDateFormat fmt = rset.getDateFormat();
/* 1938 */     if ((this.m_localeDateFormat != null) && (fmt != null) && (!fmt.equals(this.m_localeDateFormat)))
/*      */     {
/* 1941 */       this.m_checkResultSetLocalization = true;
/*      */     }
/*      */ 
/* 1946 */     this.m_localizedResultSets.remove(name);
/* 1947 */     return (ResultSet)this.m_resultSets.put(name, rset);
/*      */   }
/*      */ 
/*      */   public void addResultSetDirect(String name, ResultSet rset)
/*      */   {
/* 1956 */     this.m_resultSets.put(name, rset);
/*      */   }
/*      */ 
/*      */   public ResultSet removeResultSet(String name)
/*      */   {
/* 1966 */     return (ResultSet)this.m_resultSets.remove(name);
/*      */   }
/*      */ 
/*      */   public void setResultSets(Map map)
/*      */   {
/* 1975 */     this.m_resultSets = map;
/*      */   }
/*      */ 
/*      */   public void send(Writer writer)
/*      */     throws IOException
/*      */   {
/* 1989 */     DataSerializeUtils.sendEx(this, writer, true, null);
/*      */   }
/*      */ 
/*      */   public void sendWithEncoding(Writer writer, String javaEncoding)
/*      */     throws IOException
/*      */   {
/* 2001 */     String curClientEncoding = this.m_clientEncoding;
/* 2002 */     String curJavaEncoding = this.m_javaEncoding;
/*      */     try
/*      */     {
/* 2005 */       this.m_clientEncoding = null;
/* 2006 */       this.m_javaEncoding = javaEncoding;
/*      */ 
/* 2009 */       DataSerializeUtils.sendEx(this, writer, true, null);
/*      */     }
/*      */     finally
/*      */     {
/* 2013 */       this.m_clientEncoding = curClientEncoding;
/* 2014 */       this.m_javaEncoding = curJavaEncoding;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void sendEx(Writer writer, boolean sendHeader)
/*      */     throws IOException
/*      */   {
/* 2027 */     DataSerializeUtils.sendEx(this, writer, sendHeader, null);
/*      */   }
/*      */ 
/*      */   public byte[] sendBytes(String javaEncoding, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/* 2038 */     return DataSerializeUtils.sendBytes(this, javaEncoding, true, cxt);
/*      */   }
/*      */ 
/*      */   public void receive(BufferedReader reader)
/*      */     throws IOException
/*      */   {
/* 2054 */     DataSerializeUtils.receive(this, reader, false, null);
/*      */   }
/*      */ 
/*      */   public void receiveEx(BufferedReader reader, boolean isHeaderOnly)
/*      */     throws IOException
/*      */   {
/* 2080 */     DataSerializeUtils.receiveEx(this, reader, isHeaderOnly, null);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void parseLocalParameters(String params, String delimiter)
/*      */   {
/* 2089 */     DataSerializeUtils.parseLocalParameters(this, params, delimiter, null);
/*      */   }
/*      */ 
/*      */   public void setEncodeFlags(boolean cgiFlag, boolean javaFlag)
/*      */   {
/* 2106 */     this.m_isCgi = cgiFlag;
/* 2107 */     this.m_isJava = javaFlag;
/*      */   }
/*      */ 
/*      */   public String decode(String in)
/*      */   {
/* 2121 */     return DataSerializeUtils.decode(this, in, null);
/*      */   }
/*      */ 
/*      */   public String encode(String in)
/*      */   {
/* 2139 */     return DataSerializeUtils.encode(this, in, null);
/*      */   }
/*      */ 
/*      */   public Vector getTempFiles()
/*      */   {
/* 2148 */     return this.m_tempFiles;
/*      */   }
/*      */ 
/*      */   public void addTempFile(String name)
/*      */   {
/* 2157 */     this.m_tempFiles.addElement(name);
/*      */   }
/*      */ 
/*      */   public void cleanUpTempFiles()
/*      */   {
/* 2165 */     int len = this.m_tempFiles.size();
/* 2166 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 2168 */       File f = FileUtilsCfgBuilder.getCfgFile((String)this.m_tempFiles.elementAt(i), null, false);
/*      */       try
/*      */       {
/* 2171 */         f.delete();
/*      */       }
/*      */       catch (Throwable ignore)
/*      */       {
/* 2175 */         if (SystemUtils.m_verbose)
/*      */         {
/* 2177 */           Report.debug("system", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void setTemporaryDirectory(String dir)
/*      */   {
/* 2188 */     m_tempDir = FileUtils.directorySlashes(dir);
/*      */   }
/*      */ 
/*      */   public static String getTemporaryDirectory()
/*      */   {
/* 2196 */     return m_tempDir;
/*      */   }
/*      */ 
/*      */   public static void setFileCounterStart()
/*      */   {
/* 2201 */     Random rand = new Random();
/* 2202 */     int counter = rand.nextInt();
/* 2203 */     if (counter < 0)
/*      */     {
/* 2205 */       counter = 0 - counter;
/*      */     }
/* 2207 */     m_fileCounter = counter;
/*      */   }
/*      */ 
/*      */   public static long getNextFileCounter()
/*      */   {
/* 2214 */     synchronized (m_fileSyncObj)
/*      */     {
/* 2216 */       return ++m_fileCounter;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setOverrideTemporaryDirectory(String dir)
/*      */   {
/* 2222 */     this.m_overrideTempDir = FileUtils.directorySlashes(dir);
/*      */   }
/*      */ 
/*      */   public void setReportProgress(ReportProgress rp)
/*      */   {
/* 2229 */     this.m_progress = rp;
/*      */   }
/*      */ 
/*      */   public void reportProgress(String filename, long amtDone)
/*      */   {
/* 2234 */     String msg = LocaleUtils.encodeMessage("syReceivingFile", null, filename);
/* 2235 */     if (this.m_progress == null)
/*      */       return;
/* 2237 */     this.m_progress.reportProgress(0, msg, (float)amtDone, -1.0F);
/*      */   }
/*      */ 
/*      */   public void setContentType(String type)
/*      */   {
/* 2258 */     this.m_contentType = type;
/*      */   }
/*      */ 
/*      */   public String getContentType()
/*      */   {
/* 2268 */     return this.m_contentType;
/*      */   }
/*      */ 
/*      */   public String myBaseURL()
/*      */   {
/* 2281 */     IdcStringBuilder buffer = new IdcStringBuilder();
/*      */ 
/* 2283 */     buffer.append("http://" + this.m_environment.get("SERVER_NAME"));
/*      */ 
/* 2285 */     String portStr = (String)this.m_environment.get("SERVER_PORT");
/* 2286 */     if ((portStr != null) && 
/* 2288 */       (!portStr.equals("80")))
/*      */     {
/* 2290 */       buffer.append(":" + portStr);
/*      */     }
/*      */ 
/* 2294 */     buffer.append((String)this.m_environment.get("SCRIPT_NAME"));
/* 2295 */     return buffer.toString();
/*      */   }
/*      */ 
/*      */   public void appendTo(IdcAppendable appendable)
/*      */   {
/* 2307 */     appendTo(appendable);
/*      */   }
/*      */ 
/*      */   public void appendTo(IdcAppendableBase appendable)
/*      */   {
/* 2317 */     if (null == this.m_dataFormatOptions)
/*      */     {
/* 2319 */       this.m_dataFormatOptions = "text,rows=0,noshowenv";
/*      */     }
/* 2321 */     if ((null == this.m_dataFormat) || (!this.m_dataFormatOptions.equals(this.m_dataFormat.m_formatOptions)))
/*      */     {
/* 2323 */       this.m_dataFormat = new DataFormatter(this.m_dataFormatOptions, false);
/*      */     }
/* 2325 */     this.m_dataFormat.clear();
/* 2326 */     DataFormatUtils.appendDataBinder(this.m_dataFormat, null, this, 2);
/* 2327 */     appendable.append(this.m_dataFormat.toString());
/*      */   }
/*      */ 
/*      */   public void writeTo(Writer w) throws IOException
/*      */   {
/* 2332 */     FileUtils.appendToWriter(this, w);
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 2342 */     return StringUtils.appenderToString(this);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2349 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105472 $";
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  178 */     m_tempDir = "temp/";
/*      */ 
/*  191 */     m_fileSyncObj = new boolean[] { false };
/*      */ 
/*  275 */     DATA_TYPES = new String[] { "Properties", "OptionList", "ResultSet", "end" };
/*      */ 
/*  358 */     F_CLONE_LOCALDATA = 1;
/*  359 */     F_CLONE_RESULTSETS = 2;
/*  360 */     F_CLONE_FILES = 4;
/*  361 */     F_CLONE_ENCODINGFLAG = 8;
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataBinder
 * JD-Core Version:    0.5.4
 */