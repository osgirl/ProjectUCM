/*      */ package intradoc.serialize;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseStringException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderProtocolInterface;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerialize;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MutableResultSet;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Reader;
/*      */ import java.io.StringReader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.io.Writer;
/*      */ import java.security.MessageDigest;
/*      */ import java.security.NoSuchAlgorithmException;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.StringTokenizer;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DataBinderSerializer
/*      */   implements DataSerialize
/*      */ {
/*   36 */   protected IsoJavaEncodingMap m_encodingMap = null;
/*   37 */   protected String m_systemEncoding = null;
/*   38 */   protected String m_webEncoding = null;
/*   39 */   protected boolean m_isMultiMode = false;
/*   40 */   protected boolean m_useClientEncoding = false;
/*      */   protected boolean m_verboseEncodingTracing;
/*      */   protected boolean m_failOnReplacementCharacter;
/*   43 */   protected DataBinderProtocolInterface m_dataBinderProtocol = null;
/*   44 */   protected Object[] m_invalidEnvObjects = null;
/*      */ 
/*      */   public DataBinderSerializer()
/*      */   {
/*   51 */     this.m_encodingMap = new IsoJavaEncodingMap();
/*   52 */     this.m_verboseEncodingTracing = ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("encoding")));
/*      */ 
/*   55 */     this.m_failOnReplacementCharacter = SystemUtils.getFailOnReplacementCharacterDefault();
/*      */   }
/*      */ 
/*      */   public void setInvalidEnvObjects(Object[] invalidEnvObjects)
/*      */   {
/*   64 */     this.m_invalidEnvObjects = invalidEnvObjects;
/*      */   }
/*      */ 
/*      */   public boolean validateEnvironmentData(Object envData)
/*      */   {
/*   72 */     Object[] list = this.m_invalidEnvObjects;
/*   73 */     if ((list == null) || (envData == null))
/*      */     {
/*   75 */       return true;
/*      */     }
/*   77 */     boolean retVal = true;
/*   78 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/*   80 */       if (list[i] != envData)
/*      */         continue;
/*   82 */       retVal = false;
/*   83 */       break;
/*      */     }
/*      */ 
/*   86 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected boolean getShouldSort(Boolean shouldSort, String shouldSortEnvName, boolean shouldSortDefault)
/*      */   {
/*   91 */     if (shouldSort != null)
/*      */     {
/*   93 */       return shouldSort.booleanValue();
/*      */     }
/*   95 */     return SharedObjects.getEnvValueAsBoolean(shouldSortEnvName, shouldSortDefault);
/*      */   }
/*      */ 
/*      */   protected String[] getKeysFromMap(Map map, boolean shouldSort)
/*      */   {
/*  100 */     Set keySet = map.keySet();
/*  101 */     int numKeys = keySet.size();
/*  102 */     String[] keys = new String[numKeys];
/*  103 */     keys = (String[])(String[])keySet.toArray(keys);
/*  104 */     if (shouldSort)
/*      */     {
/*  106 */       Arrays.sort(keys);
/*      */     }
/*  108 */     return keys;
/*      */   }
/*      */ 
/*      */   public void send(DataBinder data, Writer writer, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/*  123 */     sendEx(data, writer, true, cxt);
/*      */   }
/*      */ 
/*      */   public void sendEx(DataBinder data, Writer writer, boolean sendHeader, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/*  135 */     if (cxt != null)
/*      */     {
/*  137 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/*  141 */     String respDateFormatStr = data.getLocal("ResponseDateFormat");
/*  142 */     if (respDateFormatStr != null)
/*      */     {
/*  144 */       IdcDateFormat respDateFormat = new IdcDateFormat();
/*      */       try
/*      */       {
/*  148 */         respDateFormat.init(respDateFormatStr);
/*  149 */         Enumeration rsKeys = data.getResultSetList();
/*  150 */         while (rsKeys.hasMoreElements())
/*      */         {
/*  152 */           String key = (String)rsKeys.nextElement();
/*  153 */           ResultSet rset = data.getResultSet(key);
/*  154 */           rset.setDateFormat(respDateFormat);
/*      */         }
/*      */ 
/*  157 */         String[] keys = getKeysFromMap(data.getLocalData(), false);
/*  158 */         int numKeys = keys.length;
/*  159 */         for (int k = 0; k < numKeys; ++k)
/*      */         {
/*  161 */           String key = keys[k];
/*  162 */           String value = data.getLocal(key);
/*  163 */           if (value == null) continue; if (value.length() == 0) {
/*      */             continue;
/*      */           }
/*      */ 
/*      */           try
/*      */           {
/*  169 */             Date tmpDate = data.m_blDateFormat.parseDate(value);
/*  170 */             String newDateStr = respDateFormat.format(tmpDate);
/*  171 */             data.putLocal(key, newDateStr);
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  180 */         data.putLocal("blDateFormat", respDateFormat.toPattern());
/*  181 */         data.m_determinedDataDateFormat = true;
/*  182 */         data.m_blDateFormat = respDateFormat;
/*      */       }
/*      */       catch (ParseStringException e)
/*      */       {
/*      */       }
/*      */ 
/*      */     }
/*  193 */     else if (data.m_blDateFormat != null)
/*      */     {
/*  195 */       data.putLocal("blDateFormat", data.m_blDateFormat.toPattern());
/*      */ 
/*  197 */       data.m_determinedDataDateFormat = true;
/*      */     }
/*  199 */     else if (data.m_localeDateFormat != null)
/*      */     {
/*  201 */       data.putLocal("blDateFormat", data.m_localeDateFormat.toPattern());
/*      */ 
/*  203 */       data.m_determinedDataDateFormat = true;
/*      */     }
/*      */ 
/*  206 */     Vector list = new IdcVector();
/*  207 */     Iterator it = data.getFieldTypes().keySet().iterator();
/*  208 */     while (it.hasNext())
/*      */     {
/*  210 */       String key = (String)it.next();
/*  211 */       String type = data.getFieldType(key);
/*  212 */       list.addElement(key + " " + type);
/*      */     }
/*      */ 
/*  215 */     if ((list.size() > 0) || (data.getLocal("blFieldTypes") != null))
/*      */     {
/*  217 */       data.putLocal("blFieldTypes", StringUtils.createString(list, ',', '^'));
/*      */     }
/*      */ 
/*  221 */     if (sendHeader)
/*      */     {
/*  223 */       String encodingHeader = packageEncodingHeader(data, null);
/*  224 */       if (encodingHeader != null)
/*      */       {
/*  226 */         writer.write(encodingHeader);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  231 */     packageProperties(data, writer, data.m_localData, "LocalData");
/*  232 */     if (!data.m_ioLocalOnly)
/*      */     {
/*  238 */       boolean shouldSort = getShouldSort(data.m_shouldSortResultSets, "DefaultShouldSortDataBinderResultSets", false);
/*      */       Map tempRsets;
/*      */       Iterator e;
/*  239 */       if (shouldSort)
/*      */       {
/*  242 */         String[] keys = getKeysFromMap(data.getResultSets(), true);
/*  243 */         int numKeys = keys.length;
/*  244 */         for (int k = 0; k < numKeys; ++k)
/*      */         {
/*  246 */           String key = keys[k];
/*  247 */           ResultSet rset = data.getResultSet(key);
/*  248 */           if (rset == null)
/*      */             continue;
/*  250 */           packageResultSet(data, writer, rset, key);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  257 */         tempRsets = data.cloneMap(data.getResultSets());
/*  258 */         for (int i = 0; i < data.m_rsetSuggestedOrder.size(); ++i)
/*      */         {
/*  260 */           String key = (String)data.m_rsetSuggestedOrder.elementAt(i);
/*  261 */           ResultSet rset = (ResultSet)tempRsets.get(key);
/*  262 */           if (rset == null)
/*      */             continue;
/*  264 */           packageResultSet(data, writer, rset, key);
/*  265 */           tempRsets.remove(key);
/*      */         }
/*      */ 
/*  268 */         for (e = tempRsets.keySet().iterator(); e.hasNext(); )
/*      */         {
/*  270 */           String key = (String)e.next();
/*  271 */           ResultSet rset = (ResultSet)tempRsets.get(key);
/*  272 */           packageResultSet(data, writer, rset, key);
/*      */         }
/*      */       }
/*      */ 
/*  276 */       shouldSort = getShouldSort(data.m_shouldSortOptionLists, "DefaultShouldSortDataBinderOptionLists", false);
/*      */       Hashtable tempOptLists;
/*      */       Enumeration e;
/*  277 */       if (shouldSort)
/*      */       {
/*  280 */         Hashtable optionLists = data.m_optionLists;
/*  281 */         String[] keys = getKeysFromMap(optionLists, true);
/*  282 */         int numKeys = keys.length;
/*  283 */         for (int k = 0; k < numKeys; ++k)
/*      */         {
/*  285 */           String key = keys[k];
/*  286 */           Vector options = (Vector)optionLists.get(key);
/*  287 */           packageOptionList(data, writer, options, key);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  293 */         tempOptLists = (Hashtable)data.m_optionLists.clone();
/*  294 */         for (int i = 0; i < data.m_optListSuggestedOrder.size(); ++i)
/*      */         {
/*  296 */           String key = (String)data.m_optListSuggestedOrder.elementAt(i);
/*  297 */           Vector options = (Vector)tempOptLists.get(key);
/*  298 */           if (options == null)
/*      */             continue;
/*  300 */           packageOptionList(data, writer, options, key);
/*  301 */           tempOptLists.remove(key);
/*      */         }
/*      */ 
/*  304 */         for (e = tempOptLists.keys(); e.hasMoreElements(); )
/*      */         {
/*  306 */           String key = (String)e.nextElement();
/*  307 */           Vector options = (Vector)tempOptLists.get(key);
/*  308 */           packageOptionList(data, writer, options, key);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  313 */       int unstructuredCount = data.m_unstructuredData.size();
/*  314 */       if (unstructuredCount > 0)
/*      */       {
/*  316 */         IdcStringBuilder result = new IdcStringBuilder("\n");
/*  317 */         for (int i = 0; i < unstructuredCount; ++i)
/*      */         {
/*  319 */           String line = (String)data.m_unstructuredData.elementAt(i);
/*  320 */           result.append(line);
/*  321 */           result.append('\n');
/*      */         }
/*  323 */         result.writeTo(writer);
/*  324 */         result.releaseBuffers();
/*      */       }
/*      */     }
/*      */ 
/*  328 */     if (writer == null)
/*      */       return;
/*  330 */     writer.flush();
/*      */   }
/*      */ 
/*      */   public byte[] sendBytes(DataBinder data, String javaEncoding, boolean sendHeader, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/*  341 */     byte[] responseBytes = null;
/*  342 */     if (this.m_dataBinderProtocol != null)
/*      */     {
/*  344 */       responseBytes = this.m_dataBinderProtocol.sendResponseBytes(data, cxt, javaEncoding);
/*      */     }
/*      */ 
/*  347 */     if (responseBytes == null)
/*      */     {
/*  350 */       String responseStr = null;
/*  351 */       if (this.m_dataBinderProtocol != null)
/*      */       {
/*  353 */         responseStr = this.m_dataBinderProtocol.sendResponse(data, cxt);
/*      */       }
/*      */ 
/*  356 */       if (responseStr == null)
/*      */       {
/*      */         try
/*      */         {
/*  361 */           IdcCharArrayWriter writer = new IdcCharArrayWriter();
/*  362 */           sendEx(data, writer, sendHeader, cxt);
/*  363 */           responseStr = writer.toStringRelease();
/*      */         }
/*      */         catch (Exception serializeError)
/*      */         {
/*  367 */           Report.trace("system", null, serializeError);
/*  368 */           responseStr = "<internal serialization error>";
/*      */         }
/*      */       }
/*  371 */       responseBytes = StringUtils.getBytes(responseStr, javaEncoding);
/*      */     }
/*      */ 
/*  374 */     return responseBytes;
/*      */   }
/*      */ 
/*      */   protected void packageProperties(DataBinder data, Writer writer, Properties props, String name)
/*      */     throws IOException
/*      */   {
/*  380 */     if (props.size() == 0)
/*      */     {
/*  382 */       return;
/*      */     }
/*  384 */     boolean shouldSort = getShouldSort(data.m_shouldSortProperties, "DefaultShouldSortDataBinderProperties", true);
/*  385 */     String[] keys = getKeysFromMap(props, shouldSort);
/*  386 */     int numKeys = keys.length;
/*      */ 
/*  388 */     IdcStringBuilder str = new IdcStringBuilder("@Properties ");
/*  389 */     appendEx(str, data, name);
/*  390 */     str.append('\n');
/*      */     try
/*      */     {
/*  393 */       for (int k = 0; k < numKeys; ++k)
/*      */       {
/*  395 */         String key = keys[k];
/*  396 */         String value = props.getProperty(key);
/*  397 */         appendEx(str, data, key).append('=');
/*  398 */         appendEx(str, data, value).append('\n');
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  403 */       Report.trace("system", "Unable to packageProperties for name=" + name + " and props= " + props, e);
/*      */     }
/*      */ 
/*  406 */     str.append("@end\n");
/*  407 */     str.writeTo(writer);
/*  408 */     str.releaseBuffers();
/*      */   }
/*      */ 
/*      */   protected void packageResultSet(DataBinder data, Writer writer, ResultSet aSet, String name)
/*      */     throws IOException
/*      */   {
/*  414 */     IdcStringBuilder strBuf = new IdcStringBuilder("@ResultSet ");
/*  415 */     appendEx(strBuf, data, name);
/*  416 */     strBuf.append('\n');
/*      */     try
/*      */     {
/*  421 */       int numOfFields = aSet.getNumFields();
/*  422 */       strBuf.append(Integer.toString(numOfFields));
/*  423 */       strBuf.append('\n');
/*  424 */       for (int i = 0; i < numOfFields; ++i)
/*      */       {
/*  426 */         FieldInfo info = new FieldInfo();
/*  427 */         aSet.getIndexFieldInfo(i, info);
/*  428 */         appendEx(strBuf, data, info.m_name);
/*  429 */         if ((info.m_isFixedLen) || (info.m_type != 6))
/*      */         {
/*  431 */           strBuf.append(' ');
/*  432 */           strBuf.append(Integer.toString(info.m_type));
/*  433 */           if (info.m_isFixedLen)
/*      */           {
/*  435 */             strBuf.append(' ');
/*  436 */             strBuf.append(Integer.toString(info.m_maxLen));
/*  437 */             if (info.m_type == 11)
/*      */             {
/*  439 */               strBuf.append(',');
/*  440 */               strBuf.append(Integer.toString(info.m_scale));
/*      */             }
/*      */           }
/*  443 */           strBuf.append('\n');
/*      */         }
/*      */         else
/*      */         {
/*  447 */           strBuf.append('\n');
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  452 */       for (aSet.first(); aSet.isRowPresent(); aSet.next())
/*      */       {
/*  454 */         for (int i = 0; i < numOfFields; ++i)
/*      */         {
/*  456 */           String value = aSet.getStringValue(i);
/*  457 */           if (value != null)
/*      */           {
/*  459 */             appendEx(strBuf, data, value);
/*      */           }
/*  461 */           strBuf.append('\n');
/*      */         }
/*  463 */         if (strBuf.m_length <= 10485760)
/*      */           continue;
/*  465 */         strBuf.writeTo(writer);
/*  466 */         strBuf.truncate(0);
/*      */       }
/*      */ 
/*  469 */       strBuf.append("@end\n");
/*  470 */       strBuf.writeTo(writer);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  474 */       Report.trace(null, null, e);
/*  475 */       IOException ioE = new IOException();
/*      */ 
/*  477 */       throw ioE;
/*      */     }
/*      */     finally
/*      */     {
/*  481 */       strBuf.releaseBuffers();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void packageOptionList(DataBinder data, Writer writer, Vector options, String name)
/*      */     throws IOException
/*      */   {
/*  488 */     IdcStringBuilder buf = new IdcStringBuilder();
/*  489 */     buf.append("@OptionList ");
/*  490 */     appendEx(buf, data, name).append('\n');
/*      */     try
/*      */     {
/*  493 */       int num = options.size();
/*  494 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  496 */         appendEx(buf, data, (String)options.elementAt(i)).append('\n');
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  501 */       e.printStackTrace();
/*      */     }
/*  503 */     buf.append("@end\n");
/*  504 */     buf.writeTo(writer);
/*  505 */     buf.releaseBuffers();
/*      */   }
/*      */ 
/*      */   public void receiveEx(DataBinder data, BufferedReader reader, boolean isHeaderOnly, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/*  538 */     if (cxt != null)
/*      */     {
/*  540 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/*  547 */     Vector rsetList = new IdcVector();
/*  548 */     parseReaderData(data, reader, rsetList, isHeaderOnly);
/*      */ 
/*  550 */     DataSerializeUtils.determineParameterizedLocalization(data, rsetList);
/*      */   }
/*      */ 
/*      */   protected void parseReaderData(DataBinder data, BufferedReader reader, Vector rsetList, boolean isHeaderOnly)
/*      */     throws IOException
/*      */   {
/*  556 */     data.m_bufReader = reader;
/*  557 */     data.m_previousValues.clear();
/*      */ 
/*  559 */     String[] types = DataBinder.DATA_TYPES;
/*  560 */     String line = null;
/*  561 */     String name = "";
/*  562 */     while ((line = readLine(data.m_bufReader)) != null)
/*      */     {
/*  564 */       if (line.length() <= 1)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  569 */       int type = -1;
/*  570 */       for (int i = 0; i < types.length; ++i)
/*      */       {
/*  572 */         if (line.charAt(0) != '@')
/*      */         {
/*  576 */           int loc = line.indexOf("StatusCode");
/*  577 */           if (loc < 0)
/*      */           {
/*  579 */             loc = line.indexOf("StatusMessage");
/*      */           }
/*  581 */           if (loc >= 0)
/*      */           {
/*  583 */             parseNameValue(data, line.substring(loc), "=", true);
/*      */           }
/*      */ 
/*  587 */           if (data.m_unstructuredData.indexOf(line) >= 0)
/*      */             break;
/*  589 */           data.m_unstructuredData.addElement(line); break;
/*      */         }
/*      */ 
/*  593 */         if (line.startsWith("@" + types[i]) != true) {
/*      */           continue;
/*      */         }
/*  596 */         type = i;
/*  597 */         int index = line.indexOf(" ");
/*  598 */         if (index < 0) {
/*      */           continue;
/*      */         }
/*      */ 
/*  602 */         name = line.substring(index).trim();
/*      */ 
/*  604 */         break;
/*      */       }
/*      */ 
/*  608 */       boolean isResultSet = false;
/*  609 */       switch (type)
/*      */       {
/*      */       case 0:
/*  612 */         parseProperties(data, name, data.m_bufReader);
/*  613 */         break;
/*      */       case 1:
/*  616 */         parseOptionList(data, name, data.m_bufReader);
/*  617 */         data.m_optListSuggestedOrder.addElement(name);
/*  618 */         break;
/*      */       case 2:
/*  621 */         isResultSet = true;
/*  622 */         ResultSet rset = parseResultSet(data, name, data.m_bufReader, isHeaderOnly);
/*  623 */         if (rsetList != null)
/*      */         {
/*  625 */           rsetList.addElement(rset);
/*      */         }
/*      */ 
/*  627 */         data.m_rsetSuggestedOrder.addElement(name);
/*  628 */         break;
/*      */       case 3:
/*      */       }
/*      */ 
/*  636 */       if ((data.m_ioLocalOnly) && (type == 0) && (!name.equals("Environment"))) {
/*      */         return;
/*      */       }
/*      */ 
/*  640 */       if ((isHeaderOnly) && (isResultSet))
/*      */         return;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void parseOptionList(DataBinder data, String name, Reader reader)
/*      */     throws IOException
/*      */   {
/*  649 */     BufferedReader br = (BufferedReader)reader;
/*      */ 
/*  651 */     Vector options = new IdcVector();
/*      */ 
/*  653 */     while ((line = readLine(br)) != null)
/*      */     {
/*      */       String line;
/*  655 */       if (line.startsWith("@end") == true)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*  660 */       String value = line.trim();
/*  661 */       value = decodeEx(data, value);
/*  662 */       options.addElement(value);
/*      */     }
/*      */ 
/*  665 */     data.m_optionLists.put(name, options);
/*      */   }
/*      */ 
/*      */   protected ResultSet parseResultSet(DataBinder data, String name, Reader reader, boolean isHeaderOnly)
/*      */     throws IOException
/*      */   {
/*      */     try
/*      */     {
/*  673 */       MutableResultSet rset = null;
/*  674 */       if (data.m_mutableClass != null)
/*      */       {
/*  676 */         rset = (MutableResultSet)data.m_mutableClass.newInstance();
/*      */       }
/*      */       else
/*      */       {
/*  680 */         rset = new DataResultSet();
/*      */       }
/*  682 */       rset.initEx(reader, data, isHeaderOnly);
/*  683 */       data.addResultSetDirect(name, rset);
/*  684 */       return rset;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  688 */       String msg = LocaleUtils.encodeMessage("syUnableToParseResultSet", null, name);
/*      */ 
/*  691 */       Report.trace(null, null, e);
/*  692 */       IOException ioE = new IOException(msg);
/*  693 */       SystemUtils.setExceptionCause(ioE, e);
/*  694 */       throw ioE;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void parseProperties(DataBinder data, String name, BufferedReader reader)
/*      */     throws IOException
/*      */   {
/*  702 */     Properties props = null;
/*  703 */     data.m_bufReader = reader;
/*  704 */     if (name.equals("Environment") == true)
/*      */     {
/*  708 */       props = new Properties();
/*      */     }
/*      */     else
/*      */     {
/*  712 */       props = data.m_localData;
/*      */     }
/*  714 */     readProperties(data, props, "@end", true, false);
/*      */   }
/*      */ 
/*      */   protected void readProperties(DataBinder data, Properties props, String endMark, boolean isJava, boolean isBytes)
/*      */     throws IOException
/*      */   {
/*  720 */     String line = "";
/*      */     while (true)
/*      */     {
/*  724 */       line = readInputLine(data, isBytes);
/*  725 */       if (line.equals(endMark)) {
/*      */         return;
/*      */       }
/*      */ 
/*  729 */       if (isJava)
/*      */       {
/*  731 */         line = decodeEx(data, line);
/*      */       }
/*      */ 
/*  734 */       int index = line.indexOf(61);
/*  735 */       if (index < 0)
/*      */       {
/*  738 */         IOException ioe = new IOException();
/*  739 */         ServiceException e = new ServiceException(null, "syUnableToParsePairs", new Object[] { line });
/*  740 */         e.wrapIn(ioe);
/*  741 */         throw ioe;
/*      */       }
/*  743 */       String name = line.substring(0, index);
/*  744 */       String value = line.substring(index + 1);
/*      */ 
/*  746 */       props.put(name, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String decode(DataBinder data, String in, ExecutionContext cxt)
/*      */   {
/*  766 */     return decodeEx(data, in);
/*      */   }
/*      */ 
/*      */   protected String decodeEx(DataBinder data, String in)
/*      */   {
/*  771 */     int length = in.length();
/*  772 */     if (length == 0)
/*      */     {
/*  774 */       return "";
/*      */     }
/*      */ 
/*  777 */     if ((data.m_tmpCharArray == null) || (data.m_tmpCharArray.length < length))
/*      */     {
/*  779 */       data.m_tmpCharArray = new char[length];
/*      */     }
/*  781 */     char[] chars = data.m_tmpCharArray;
/*  782 */     in.getChars(0, length, chars, 0);
/*  783 */     int i = 0; int j = 0;
/*  784 */     boolean isMutated = false;
/*  785 */     if (data.m_isCgi)
/*      */     {
/*  792 */       while (i < length)
/*      */       {
/*  794 */         char ch = chars[i];
/*  795 */         if ((ch == '+') || (ch == '%'))
/*      */         {
/*  797 */           isMutated = true;
/*  798 */           break;
/*      */         }
/*  800 */         ++i;
/*      */       }
/*  802 */       j = i;
/*      */       while (true) {
/*  804 */         if (i >= length)
/*      */           break label515;
/*  806 */         boolean doAppend = true;
/*  807 */         char ch = chars[i];
/*  808 */         ++i;
/*  809 */         switch (ch)
/*      */         {
/*      */         case '+':
/*  812 */           ch = ' ';
/*  813 */           break;
/*      */         case '%':
/*  815 */           int range = 0;
/*  816 */           if ((i + 4 < length) && (chars[i] == 'u'))
/*      */           {
/*  818 */             ++i;
/*  819 */             range = 4;
/*      */           }
/*  821 */           else if (i + 1 < length)
/*      */           {
/*  823 */             range = 2;
/*      */           }
/*  825 */           if (range > 0)
/*      */           {
/*  827 */             String str = in.substring(i, i + range);
/*      */             try
/*      */             {
/*  830 */               ch = (char)Integer.parseInt(str, 16);
/*  831 */               if (range == 4)
/*      */               {
/*  834 */                 str = "" + ch;
/*      */ 
/*  836 */                 String encoding = (null != data.m_clientEncoding) ? data.m_clientEncoding : this.m_systemEncoding;
/*      */ 
/*  838 */                 byte[] bytes = str.getBytes(encoding);
/*  839 */                 for (int index = 0; index < bytes.length; ++index)
/*      */                 {
/*  841 */                   chars[(j++)] = (char)bytes[index];
/*      */                 }
/*  843 */                 doAppend = false;
/*      */               }
/*  845 */               i += range;
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/*  849 */               String errMsg = LocaleUtils.encodeMessage("syErrorDecodingIntInString", null, str, in);
/*      */ 
/*  851 */               Report.trace(null, errMsg, e);
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  856 */             String errMsg = LocaleUtils.encodeMessage("syErrorDecodingString", null, in);
/*  857 */             Report.trace(null, errMsg, null);
/*      */           }
/*      */         }
/*      */ 
/*  861 */         if (doAppend)
/*      */         {
/*  863 */           chars[(j++)] = ch;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  869 */     while (i < length)
/*      */     {
/*  871 */       char ch = chars[i];
/*  872 */       if (ch == '\\')
/*      */       {
/*  874 */         isMutated = true;
/*  875 */         break;
/*      */       }
/*  877 */       ++i;
/*      */     }
/*  879 */     j = i;
/*      */ 
/*  881 */     while (i < length)
/*      */     {
/*  883 */       char ch = chars[i];
/*  884 */       if (ch == '\\')
/*      */       {
/*  886 */         ++i;
/*      */ 
/*  888 */         ch = chars[i];
/*  889 */         switch (ch)
/*      */         {
/*      */         case 'n':
/*  892 */           ch = '\n';
/*  893 */           break;
/*      */         case 'r':
/*  895 */           ch = '\r';
/*      */         }
/*      */       }
/*      */ 
/*  899 */       ++i;
/*  900 */       chars[(j++)] = ch;
/*      */     }
/*      */ 
/*  904 */     label515: return (isMutated) ? new String(chars, 0, j) : in;
/*      */   }
/*      */ 
/*      */   public String encode(DataBinder data, String in, ExecutionContext cxt)
/*      */   {
/*  923 */     if (cxt != null)
/*      */     {
/*  925 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/*  928 */     return encodeEx(data, in);
/*      */   }
/*      */ 
/*      */   protected String encodeEx(DataBinder data, String in)
/*      */   {
/*  933 */     IdcAppendable appendResult = appendEx(null, data, in);
/*  934 */     if (appendResult != null)
/*      */     {
/*  936 */       return appendResult.toString();
/*      */     }
/*  938 */     return in;
/*      */   }
/*      */ 
/*      */   protected IdcAppendable appendEx(IdcAppendable appendable, DataBinder data, String in)
/*      */   {
/*  946 */     int length = in.length();
/*      */ 
/*  948 */     if (length == 0)
/*      */     {
/*  950 */       return appendable;
/*      */     }
/*      */ 
/*  953 */     if ((data.m_tmpCharArray == null) || (data.m_tmpCharArray.length < 2 * length))
/*      */     {
/*  955 */       data.m_tmpCharArray = new char[2 * length];
/*      */     }
/*  957 */     char[] chars = data.m_tmpCharArray;
/*  958 */     in.getChars(0, length, chars, 0);
/*  959 */     int j = 0;
/*  960 */     boolean isMutated = false;
/*  961 */     for (int i = 0; i < length; ++i)
/*      */     {
/*  963 */       char ch = in.charAt(i);
/*  964 */       switch (ch)
/*      */       {
/*      */       case '\n':
/*      */       case '\r':
/*  968 */         isMutated = true;
/*  969 */         chars[(j++)] = '\\';
/*  970 */         char outch = (ch == '\n') ? 'n' : 'r';
/*  971 */         chars[(j++)] = outch;
/*  972 */         break;
/*      */       case '@':
/*      */       case '\\':
/*  975 */         isMutated = true;
/*  976 */         chars[(j++)] = '\\';
/*      */       default:
/*  979 */         chars[(j++)] = ch;
/*      */       }
/*      */     }
/*  981 */     if ((isMutated) || (appendable != null))
/*      */     {
/*  983 */       if (appendable == null)
/*      */       {
/*  985 */         appendable = new IdcStringBuilder(j);
/*      */       }
/*  987 */       appendable.append(chars, 0, j);
/*      */     }
/*  989 */     return appendable;
/*      */   }
/*      */ 
/*      */   protected byte[] getConvertedBytes(DataBinder data, String in, String encoding) throws IOException
/*      */   {
/*  994 */     byte[] byteArray = null;
/*      */     try
/*      */     {
/*  997 */       if (isRawData(data))
/*      */       {
/*  999 */         byteArray = StringUtils.getAsClientBytes(in);
/*      */       }
/*      */       else
/*      */       {
/* 1003 */         byteArray = StringUtils.getBytes(in, encoding);
/*      */       }
/*      */     }
/*      */     catch (UnsupportedEncodingException e)
/*      */     {
/* 1008 */       IOException ioE = new IOException();
/* 1009 */       SystemUtils.setExceptionCause(ioE, e);
/* 1010 */       throw ioE;
/*      */     }
/*      */ 
/* 1013 */     return byteArray;
/*      */   }
/*      */ 
/*      */   protected String getConvertedString(DataBinder data, byte[] byteArray, int offset, int length, String encoding)
/*      */     throws IOException
/*      */   {
/* 1019 */     String retStr = null;
/* 1020 */     if (isRawData(data))
/*      */     {
/* 1022 */       retStr = StringUtils.toStringRaw(byteArray, offset, length);
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/* 1028 */         retStr = StringUtils.getString(byteArray, offset, length, encoding);
/*      */       }
/*      */       catch (UnsupportedEncodingException e)
/*      */       {
/* 1032 */         IOException ioE = new IOException();
/* 1033 */         SystemUtils.setExceptionCause(ioE, e);
/* 1034 */         throw ioE;
/*      */       }
/*      */     }
/*      */ 
/* 1038 */     return retStr;
/*      */   }
/*      */ 
/*      */   protected boolean isRawData(DataBinder data)
/*      */   {
/* 1045 */     boolean isUndeterminedEncoding = (!data.m_determinedEncoding) && (((this.m_isMultiMode) || ((this.m_useClientEncoding) && (data.m_isJava))));
/*      */ 
/* 1047 */     return isUndeterminedEncoding;
/*      */   }
/*      */ 
/*      */   protected boolean isClientEncoding(DataBinder data)
/*      */   {
/* 1052 */     boolean retBool = false;
/* 1053 */     if ((data.m_determinedEncoding) && (((this.m_isMultiMode) || ((this.m_useClientEncoding) && (data.m_isJava)))))
/*      */     {
/* 1055 */       retBool = true;
/*      */     }
/* 1057 */     return retBool;
/*      */   }
/*      */ 
/*      */   public void parseRequest(DataBinder data, BufferedInputStream inStream, ExecutionContext cxt)
/*      */     throws IOException, DataException
/*      */   {
/* 1078 */     prepareParseRequest(data, inStream, cxt);
/* 1079 */     parseRequestBody(data, cxt);
/*      */   }
/*      */ 
/*      */   public void prepareParseRequest(DataBinder data, BufferedInputStream inStream, ExecutionContext cxt)
/*      */     throws IOException, DataException
/*      */   {
/* 1098 */     if (cxt != null)
/*      */     {
/* 1100 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 1103 */     data.m_inStream = inStream;
/*      */ 
/* 1107 */     readProperties(data, data.m_environment, "$$$$", false, true);
/*      */ 
/* 1110 */     String cgiDebug = data.m_environment.getProperty("CGI_DEBUG");
/* 1111 */     if (cgiDebug != null)
/*      */     {
/* 1113 */       boolean isDebug = StringUtils.convertToBool(cgiDebug, false);
/* 1114 */       if (isDebug)
/*      */       {
/* 1116 */         String cgiTimestamp = data.m_environment.getProperty("IDC_REQUEST_CTIME");
/* 1117 */         if (cgiTimestamp != null)
/*      */         {
/* 1119 */           Report.trace("system", "IDC_REQUEST_CTIME: " + cgiTimestamp, null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1126 */     data.m_isExternalRequest = (data.m_environment.getProperty("SERVER_SOFTWARE") != null);
/*      */   }
/*      */ 
/*      */   public void parseRequestBody(DataBinder data, ExecutionContext cxt)
/*      */     throws IOException, DataException
/*      */   {
/* 1140 */     if (cxt != null)
/*      */     {
/* 1142 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 1147 */     data.m_rawData = new IdcVector();
/* 1148 */     data.m_determinedEncoding = false;
/*      */ 
/* 1150 */     String contentLenStr = data.m_environment.getProperty("CONTENT_LENGTH");
/* 1151 */     if (contentLenStr == null)
/*      */     {
/* 1153 */       data.m_remainingLength = 0L;
/*      */     }
/*      */     else
/*      */     {
/* 1157 */       data.m_remainingLength = NumberUtils.parseLong(contentLenStr, 0L);
/*      */     }
/*      */ 
/* 1161 */     boolean isDefaultRequest = true;
/* 1162 */     if ((this.m_dataBinderProtocol != null) && (this.m_dataBinderProtocol.parseRequest(data, cxt)))
/*      */     {
/* 1164 */       isDefaultRequest = false;
/*      */     }
/*      */ 
/* 1167 */     if (isDefaultRequest)
/*      */     {
/* 1171 */       int type = determineContentType(data, cxt);
/*      */ 
/* 1173 */       translateEnvironment(data);
/*      */ 
/* 1175 */       switch (type)
/*      */       {
/*      */       case -1:
/* 1178 */         break;
/*      */       case 0:
/* 1181 */         parsePost(data, "&");
/* 1182 */         break;
/*      */       case 1:
/* 1185 */         prepareParseMultiContent(data);
/* 1186 */         parseMultiContent(data);
/*      */       }
/*      */ 
/*      */     }
/* 1192 */     else if (data.m_environment.getProperty("REQUEST_METHOD").equals("PROPFIND"))
/*      */     {
/* 1194 */       parsePost(data, "&");
/*      */     }
/*      */ 
/* 1199 */     if (isClientEncoding(data))
/*      */     {
/* 1201 */       String encoding = determineEncoding(data, null);
/* 1202 */       DataSerializeUtils.translateRemainingRaw(data, encoding);
/*      */     }
/*      */ 
/* 1206 */     if (this.m_dataBinderProtocol == null)
/*      */       return;
/* 1208 */     this.m_dataBinderProtocol.postParseRequest(data, cxt);
/*      */   }
/*      */ 
/*      */   public int determineContentType(DataBinder data, ExecutionContext cxt)
/*      */     throws DataException
/*      */   {
/* 1228 */     if (cxt != null)
/*      */     {
/* 1230 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 1233 */     String action = data.m_environment.getProperty("REQUEST_METHOD");
/* 1234 */     if ((action != null) && (((action.equalsIgnoreCase("GET")) || (action.equalsIgnoreCase("HEAD")))))
/*      */     {
/* 1236 */       if ((action.equalsIgnoreCase("HEAD")) && 
/* 1238 */         (SystemUtils.m_verbose))
/*      */       {
/* 1240 */         String host = data.m_environment.getProperty("HTTP_HOST");
/* 1241 */         Report.debug("system", "Received a HEAD request from host " + host, null);
/*      */       }
/*      */ 
/* 1244 */       return 0;
/*      */     }
/*      */ 
/* 1247 */     String contentType = data.m_environment.getProperty("CONTENT_TYPE");
/* 1248 */     if (contentType == null)
/*      */     {
/* 1250 */       String host = data.m_environment.getProperty("HTTP_HOST");
/* 1251 */       String msg = LocaleUtils.encodeMessage("syRequestWithBrowserHeaderSemanticsHasNoContentType", null, action, host);
/*      */ 
/* 1253 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1257 */     if (contentType.indexOf("x-unknown") >= 0)
/*      */     {
/* 1260 */       String line = null;
/*      */       try
/*      */       {
/* 1263 */         line = readLineEx(data, data.m_inStream, true, true, null);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1267 */         throw new DataException(null, e);
/*      */       }
/* 1269 */       data.m_isJava = true;
/* 1270 */       String bndKey = "JavaBoundary=";
/* 1271 */       int index = line.indexOf(bndKey);
/* 1272 */       if (index >= 0)
/*      */       {
/* 1274 */         index += bndKey.length();
/* 1275 */         String boundary = line.substring(index);
/* 1276 */         data.m_environment.put("BOUNDARY", boundary);
/*      */       }
/* 1278 */       return 1;
/*      */     }
/*      */ 
/* 1282 */     int index = contentType.indexOf(59);
/* 1283 */     String key = null;
/* 1284 */     String value = null;
/* 1285 */     if (index >= 0)
/*      */     {
/* 1287 */       key = contentType.substring(0, index);
/*      */     }
/* 1289 */     if ((key != null) && (key.equalsIgnoreCase("multipart/form-data") == true))
/*      */     {
/* 1293 */       index = contentType.toLowerCase().indexOf("boundary=");
/*      */ 
/* 1295 */       if (index < 0)
/*      */       {
/* 1297 */         data.m_environment.put("BOUNDARY", "---1234567890");
/*      */       }
/*      */       else
/*      */       {
/* 1301 */         index += 9;
/*      */ 
/* 1303 */         if (contentType.charAt(index) == '"')
/* 1304 */           ++index;
/* 1305 */         String[] tokens = contentType.substring(index).split("\\s|;|\"");
/* 1306 */         if (tokens.length > 0)
/* 1307 */           value = tokens[0];
/*      */         else
/* 1309 */           value = "---1234567890";
/* 1310 */         value = "" + value;
/*      */ 
/* 1312 */         data.m_environment.put("BOUNDARY", value);
/*      */       }
/*      */ 
/* 1316 */       String userAgent = data.m_environment.getProperty("HTTP_USER_AGENT");
/* 1317 */       if (userAgent != null)
/*      */       {
/* 1319 */         String uAgnt = userAgent.toUpperCase();
/* 1320 */         if (uAgnt.indexOf("JAVA") >= 0)
/*      */         {
/* 1322 */           data.m_isJava = true;
/*      */         }
/*      */       }
/*      */ 
/* 1326 */       if (data.m_environment.getProperty("HTTP_IDCVERSION") == null)
/*      */       {
/* 1328 */         String requestAgent = data.m_environment.getProperty("IDC_REQUEST_AGENT");
/* 1329 */         if ((requestAgent != null) && (requestAgent.equals("webserver")))
/*      */         {
/* 1331 */           data.m_isStandardHttpRequest = true;
/*      */         }
/*      */       }
/*      */ 
/* 1335 */       return 1;
/*      */     }
/* 1337 */     return 0;
/*      */   }
/*      */ 
/*      */   public void resetMultiContentFlags(DataBinder data, ExecutionContext cxt)
/*      */   {
/* 1343 */     if (cxt != null)
/*      */     {
/* 1345 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 1348 */     resetMultiContentFlagsEx(data);
/*      */   }
/*      */ 
/*      */   protected void resetMultiContentFlagsEx(DataBinder data)
/*      */   {
/* 1353 */     data.m_isSuspended = false;
/* 1354 */     data.m_isSkipFile = false;
/*      */   }
/*      */ 
/*      */   public void continueParse(DataBinder data, ExecutionContext cxt)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1361 */     if (cxt != null)
/*      */     {
/* 1363 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 1366 */     boolean isDefaultRequest = true;
/* 1367 */     if ((this.m_dataBinderProtocol != null) && (this.m_dataBinderProtocol.continueParse(data, cxt)))
/*      */     {
/* 1369 */       isDefaultRequest = false;
/*      */     }
/*      */ 
/* 1372 */     if (!isDefaultRequest) {
/*      */       return;
/*      */     }
/* 1375 */     if (!data.m_isSuspended)
/*      */     {
/* 1377 */       return;
/*      */     }
/* 1379 */     resetMultiContentFlagsEx(data);
/*      */ 
/* 1381 */     String filename = data.getLocal(data.m_suspendedFileKey);
/* 1382 */     String line = parseFile(data, data.m_suspendedFileKey, filename);
/*      */ 
/* 1384 */     if ((line != null) && (line.indexOf(data.m_boundary) >= 0))
/*      */     {
/* 1386 */       Report.trace("socketrequests", "Start reading files.", null);
/* 1387 */       parseMultiContent(data);
/* 1388 */       Report.trace("socketrequests", "Finished reading files.", null);
/*      */     }
/* 1390 */     if (this.m_dataBinderProtocol == null)
/*      */       return;
/* 1392 */     this.m_dataBinderProtocol.postContinueParse(data, cxt);
/*      */   }
/*      */ 
/*      */   protected void prepareParseMultiContent(DataBinder data)
/*      */     throws IOException, DataException
/*      */   {
/* 1400 */     String requestAgent = data.getEnvironmentValue("IDC_REQUEST_AGENT");
/* 1401 */     if ((requestAgent != null) && (requestAgent.equalsIgnoreCase("webserver")))
/*      */     {
/* 1403 */       data.m_extraTailLength = 3;
/*      */     }
/* 1405 */     String queryString = data.m_environment.getProperty("QUERY_STRING");
/*      */ 
/* 1409 */     if ((queryString != null) && (queryString.length() > 0))
/*      */     {
/* 1411 */       parseLocalParametersEx(data, queryString, "&", false);
/*      */     }
/*      */ 
/* 1414 */     if (data.m_boundary != null)
/*      */       return;
/* 1416 */     data.m_boundary = ("--" + data.m_environment.getProperty("BOUNDARY"));
/* 1417 */     data.m_boundaryBytes = StringUtils.getBytes(data.m_boundary, determineEncoding(data, null));
/*      */ 
/* 1419 */     if (data.m_boundary.length() == data.m_boundaryBytes.length)
/*      */       return;
/* 1421 */     throw new IOException("Boundary separator for upload post contains illegal characters.");
/*      */   }
/*      */ 
/*      */   protected void parseMultiContent(DataBinder data)
/*      */     throws IOException, DataException
/*      */   {
/* 1429 */     String line = null;
/* 1430 */     while ((!data.m_isSuspended) && ((line = parseForData(data)) != null) && 
/* 1432 */       (line.indexOf(data.m_boundary) >= 0));
/* 1440 */     if (checkIsApplicationCall(data))
/*      */     {
/* 1442 */       data.m_isJava = true;
/*      */     }
/*      */ 
/* 1445 */     if (!data.m_isSuspended)
/*      */     {
/* 1447 */       readToLastMultipartPostByte(data);
/*      */     }
/*      */ 
/* 1450 */     DataSerializeUtils.determineParameterizedLocalization(data, null);
/*      */   }
/*      */ 
/*      */   protected void readToLastMultipartPostByte(DataBinder data)
/*      */     throws IOException
/*      */   {
/* 1460 */     long len = data.m_extraTailLength + data.m_remainingLength;
/* 1461 */     if ((len > 0L) && (len < 16L) && (data.m_remainingLength >= 0L))
/*      */     {
/* 1463 */       data.m_inStream.read(data.m_tempByteBuf, 0, (int)len);
/* 1464 */       data.m_extraTailLength = 0;
/* 1465 */       data.m_remainingLength = 0L;
/*      */     }
/*      */     else
/*      */     {
/* 1469 */       if (data.m_extraTailLength <= 0) {
/*      */         return;
/*      */       }
/*      */ 
/* 1473 */       data.m_remainingLength += data.m_extraTailLength;
/* 1474 */       data.m_extraTailLength = 0;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String parseForData(DataBinder data)
/*      */     throws IOException, DataException
/*      */   {
/* 1482 */     String line = readLineEx(data, data.m_inStream, true, true, null);
/* 1483 */     if (line.length() == 0)
/*      */     {
/* 1485 */       line = readLineEx(data, data.m_inStream, true, true, null);
/*      */     }
/*      */ 
/* 1489 */     StringTokenizer strToken = new StringTokenizer(line, "\"");
/* 1490 */     if (!strToken.hasMoreElements())
/*      */     {
/* 1492 */       String msg = LocaleUtils.encodeMessage("syNamePropNotFound", null, "" + line);
/*      */ 
/* 1494 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1497 */     String token = strToken.nextToken();
/* 1498 */     if (token.indexOf("name") < 0)
/*      */     {
/* 1500 */       return line;
/*      */     }
/*      */ 
/* 1503 */     if (!strToken.hasMoreElements())
/*      */     {
/* 1505 */       throw new DataException("!syNameValuePairMissing");
/*      */     }
/*      */ 
/* 1508 */     String name = strToken.nextToken();
/* 1509 */     String fileName = null;
/* 1510 */     if (strToken.hasMoreElements())
/*      */     {
/* 1512 */       token = strToken.nextToken();
/* 1513 */       if (token.indexOf("filename") < 0)
/*      */       {
/* 1515 */         Report.trace(null, "It's not a file name, it's an error and = " + token, null);
/* 1516 */         return null;
/*      */       }
/* 1518 */       fileName = "";
/* 1519 */       if (strToken.hasMoreTokens())
/*      */       {
/* 1521 */         fileName = strToken.nextToken();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1526 */     if (fileName != null)
/*      */     {
/* 1529 */       data.m_localData.put(name, fileName);
/* 1530 */       if (isRawData(data))
/*      */       {
/* 1532 */         data.m_rawData.addElement(new String[] { name, fileName });
/*      */       }
/* 1534 */       data.m_hasAttachedFiles = true;
/* 1535 */       if (data.m_isSkipFile)
/*      */       {
/* 1537 */         data.m_isSuspended = true;
/* 1538 */         data.m_suspendedFileKey = name;
/* 1539 */         return null;
/*      */       }
/* 1541 */       return parseFile(data, name, fileName);
/*      */     }
/*      */ 
/* 1544 */     return parseDataValue(data, name);
/*      */   }
/*      */ 
/*      */   protected String parseDataValue(DataBinder data, String name)
/*      */     throws IOException
/*      */   {
/* 1550 */     String value = new String();
/*      */ 
/* 1553 */     String line = readLineEx(data, data.m_inStream, true, true, null);
/*      */     while (true)
/*      */     {
/* 1557 */       line = readLineEx(data, data.m_inStream, true, true, null);
/* 1558 */       if (line.indexOf(data.m_boundary) >= 0)
/*      */         break;
/* 1560 */       if (value.length() != 0)
/*      */       {
/* 1562 */         value = value + "\n";
/*      */       }
/* 1564 */       value = value + line;
/*      */     }
/*      */ 
/* 1568 */     if ((name.equals("ClientEncoding")) && (value.length() > 0))
/*      */     {
/* 1570 */       data.m_determinedEncoding = true;
/* 1571 */       data.m_clientEncoding = value;
/*      */     }
/* 1573 */     data.m_localData.put(name, value);
/* 1574 */     if (isRawData(data))
/*      */     {
/* 1576 */       data.m_rawData.addElement(new String[] { name, value });
/*      */     }
/*      */ 
/* 1582 */     return line;
/*      */   }
/*      */ 
/*      */   protected String readLine(BufferedReader reader) throws IOException
/*      */   {
/* 1587 */     String line = reader.readLine();
/* 1588 */     return line;
/*      */   }
/*      */ 
/*      */   protected String readInputLine(DataBinder data, boolean isBytes) throws IOException
/*      */   {
/* 1593 */     if (isBytes)
/*      */     {
/* 1598 */       if (data.m_isCgi)
/*      */       {
/* 1600 */         return readLineRaw(data, data.m_inStream, true);
/*      */       }
/* 1602 */       return readLineEx(data, data.m_inStream, true, false, null);
/*      */     }
/* 1604 */     String retVal = readLine(data.m_bufReader);
/* 1605 */     if (retVal == null)
/*      */     {
/* 1607 */       throw new IOException("!syInputTerminatedBeforeReadingLine");
/*      */     }
/* 1609 */     return retVal;
/*      */   }
/*      */ 
/*      */   public int readLineBytes(DataBinder data, byte[] inBytes, boolean doCount, int start, int length, int[] newOffset)
/*      */   {
/* 1615 */     boolean foundCR = false;
/* 1616 */     int end = start + length;
/* 1617 */     if (end > inBytes.length)
/*      */     {
/* 1619 */       end = inBytes.length;
/*      */     }
/* 1621 */     int lastCharOffset = end;
/*      */ 
/* 1624 */     int curOffset = end;
/* 1625 */     for (int i = start; i < end; ++i)
/*      */     {
/* 1627 */       char ch = (char)inBytes[i];
/* 1628 */       if (ch == '\n')
/*      */       {
/* 1630 */         lastCharOffset = i + 1;
/* 1631 */         curOffset = i;
/* 1632 */         if (!foundCR)
/*      */           break;
/* 1634 */         --curOffset; break;
/*      */       }
/*      */ 
/* 1638 */       foundCR = ch == '\r';
/*      */     }
/*      */ 
/* 1642 */     if (doCount)
/*      */     {
/* 1644 */       data.m_remainingLength -= lastCharOffset;
/*      */     }
/*      */ 
/* 1648 */     newOffset[0] = lastCharOffset;
/*      */ 
/* 1651 */     return curOffset;
/*      */   }
/*      */ 
/*      */   public String readLineEx(DataBinder data, BufferedInputStream inStream, boolean doCount, boolean allowRaw, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/* 1657 */     String line = null;
/*      */ 
/* 1660 */     if (cxt != null)
/*      */     {
/* 1662 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 1667 */     if ((allowRaw) && (isRawData(data)))
/*      */     {
/* 1669 */       byte[] bline = readStreamLineByteEx(data, inStream, doCount);
/* 1670 */       line = StringUtils.toStringRaw(bline);
/*      */     }
/*      */     else
/*      */     {
/* 1674 */       line = readStreamLineEx(data, inStream, doCount, null);
/*      */     }
/*      */ 
/* 1677 */     return line;
/*      */   }
/*      */ 
/*      */   protected String readLineRaw(DataBinder data, BufferedInputStream inStream, boolean doCount)
/*      */     throws IOException
/*      */   {
/* 1683 */     String line = null;
/*      */ 
/* 1686 */     byte[] bline = readStreamLineByteEx(data, inStream, doCount);
/* 1687 */     line = StringUtils.toStringRaw(bline);
/*      */ 
/* 1689 */     return line;
/*      */   }
/*      */ 
/*      */   protected String readStreamLineEx(DataBinder data, BufferedInputStream inStream, boolean doCount, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/* 1697 */     if (cxt != null)
/*      */     {
/* 1699 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 1702 */     byte[] bline = readStreamLineByteEx(data, inStream, doCount);
/*      */ 
/* 1704 */     return StringUtils.getString(bline, determineEncoding(data, null));
/*      */   }
/*      */ 
/*      */   protected byte[] readStreamLineByteEx(DataBinder data, BufferedInputStream inStream, boolean doCount)
/*      */     throws IOException
/*      */   {
/* 1710 */     int curCount = 0;
/* 1711 */     int bufSize = 0;
/* 1712 */     int lastCharOffset = 0;
/* 1713 */     int skipAmount = 0;
/* 1714 */     boolean foundIt = false;
/* 1715 */     boolean foundCR = false;
/*      */ 
/* 1717 */     while (!foundIt)
/*      */     {
/* 1719 */       inStream.mark(512);
/* 1720 */       if (curCount + 256 >= data.m_tempByteBuf.length)
/*      */       {
/* 1723 */         int tempBufSize = data.m_tempByteBuf.length;
/* 1724 */         byte[] tempBuf = new byte[tempBufSize];
/*      */ 
/* 1726 */         System.arraycopy(data.m_tempByteBuf, 0, tempBuf, 0, tempBufSize);
/* 1727 */         data.m_tempByteBuf = new byte[tempBufSize * 2];
/*      */ 
/* 1729 */         System.arraycopy(tempBuf, 0, data.m_tempByteBuf, 0, tempBufSize);
/*      */       }
/* 1731 */       int readCount = inStream.read(data.m_tempByteBuf, curCount, 256);
/* 1732 */       if (readCount < 0)
/*      */       {
/* 1734 */         throw new IOException("!syStreamTerminatedBeforeProtocol");
/*      */       }
/* 1736 */       int newCount = curCount + readCount;
/* 1737 */       for (int i = curCount; (i < newCount) && (!foundIt); ++i)
/*      */       {
/* 1739 */         char ch = (char)data.m_tempByteBuf[i];
/* 1740 */         if (ch == '\n')
/*      */         {
/* 1742 */           bufSize = i;
/* 1743 */           skipAmount = i - curCount + 1;
/* 1744 */           lastCharOffset = i + 1;
/* 1745 */           if (foundCR)
/*      */           {
/* 1747 */             --bufSize;
/*      */           }
/* 1749 */           foundIt = true;
/*      */         }
/* 1751 */         foundCR = ch == '\r';
/*      */       }
/* 1753 */       curCount = newCount;
/*      */     }
/*      */ 
/* 1756 */     inStream.reset();
/* 1757 */     inStream.skip(skipAmount);
/*      */ 
/* 1760 */     if (doCount)
/*      */     {
/* 1762 */       data.m_remainingLength -= lastCharOffset;
/*      */     }
/*      */ 
/* 1766 */     byte[] bLine = new byte[bufSize];
/* 1767 */     System.arraycopy(data.m_tempByteBuf, 0, bLine, 0, bufSize);
/*      */ 
/* 1769 */     return bLine;
/*      */   }
/*      */ 
/*      */   public void translateEnvironment(DataBinder data)
/*      */     throws IOException
/*      */   {
/* 1776 */     Enumeration en = data.m_environment.keys();
/* 1777 */     String headerEncoding = data.m_environment.getProperty("HEADER_ENCODING");
/* 1778 */     if (headerEncoding != null)
/*      */     {
/* 1781 */       String translatedEncoding = LocaleResources.getEncodingFromAlias(headerEncoding);
/* 1782 */       if (translatedEncoding != null)
/* 1783 */         headerEncoding = translatedEncoding;
/*      */     }
/*      */     else
/*      */     {
/* 1787 */       headerEncoding = this.m_systemEncoding;
/*      */     }
/*      */ 
/* 1790 */     data.m_previousValues.clear();
/* 1791 */     while (en.hasMoreElements())
/*      */     {
/* 1793 */       String key = (String)en.nextElement();
/* 1794 */       String val = data.m_environment.getProperty(key);
/* 1795 */       if (!key.equals("QUERY_STRING"))
/*      */       {
/* 1797 */         DataSerializeUtils.translateAndSetRawValueEx(data.m_environment, key, val, headerEncoding, data.m_previousValues);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void parsePost(DataBinder data, String delimiter)
/*      */     throws IOException, DataException, UnsupportedEncodingException
/*      */   {
/* 1805 */     byte[] bArray = null;
/* 1806 */     int bRead = 0;
/* 1807 */     int[] byteNextLineStart = { 0 };
/*      */ 
/* 1809 */     String action = data.m_environment.getProperty("REQUEST_METHOD");
/* 1810 */     if (action == null)
/*      */     {
/* 1812 */       throw new DataException("!syRequestMethodNotSpecified");
/*      */     }
/*      */ 
/* 1815 */     String queryString = data.m_environment.getProperty("QUERY_STRING");
/*      */ 
/* 1820 */     boolean hasQueryValues = false;
/* 1821 */     if ((queryString != null) && (queryString.length() > 0))
/*      */     {
/* 1823 */       boolean allowRaw = false;
/* 1824 */       if (action.equalsIgnoreCase("GET"))
/*      */       {
/* 1826 */         allowRaw = true;
/*      */ 
/* 1828 */         if (data.m_environment.getProperty("HTTP_IDCVERSION") == null)
/*      */         {
/* 1830 */           String requestAgent = data.m_environment.getProperty("IDC_REQUEST_AGENT");
/* 1831 */           if ((requestAgent != null) && (requestAgent.equals("webserver")))
/*      */           {
/* 1833 */             data.m_isStandardHttpRequest = true;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1838 */       parseLocalParametersEx(data, queryString, delimiter, allowRaw);
/* 1839 */       hasQueryValues = true;
/*      */ 
/* 1843 */       DataSerializeUtils.translateAndSetRawValueEx(data.m_environment, "QUERY_STRING", queryString, data.m_clientEncoding, data.m_previousValues);
/*      */     }
/*      */ 
/* 1847 */     if (action.equalsIgnoreCase("POST"))
/*      */     {
/* 1849 */       String numStr = data.m_environment.getProperty("CONTENT_LENGTH");
/*      */ 
/* 1854 */       int numBytes = NumberUtils.parseInteger(numStr, 0);
/* 1855 */       bArray = new byte[numBytes];
/*      */ 
/* 1858 */       bRead = 0;
/* 1859 */       while (bRead < numBytes)
/*      */       {
/* 1861 */         int count = data.m_inStream.read(bArray, bRead, numBytes - bRead);
/* 1862 */         if (count < 0)
/*      */         {
/* 1864 */           throw new IOException("!syPostInsufficientData");
/*      */         }
/* 1866 */         bRead += count;
/*      */       }
/*      */ 
/* 1871 */       data.m_postDataBuf = bArray;
/*      */ 
/* 1874 */       int endLine = readLineBytes(data, bArray, false, 0, bArray.length, byteNextLineStart);
/* 1875 */       if (endLine == numBytes)
/*      */       {
/* 1877 */         if (data.m_environment.getProperty("HTTP_IDCVERSION") == null)
/*      */         {
/* 1879 */           String requestAgent = data.m_environment.getProperty("IDC_REQUEST_AGENT");
/* 1880 */           if ((requestAgent != null) && (requestAgent.equals("webserver")))
/*      */           {
/* 1882 */             data.m_isStandardHttpRequest = true;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/* 1888 */         data.m_environment.put("IS_EXTENDED_POST", "1");
/*      */       }
/*      */ 
/* 1892 */       data.m_rawPostData = new byte[endLine];
/* 1893 */       System.arraycopy(bArray, 0, data.m_rawPostData, 0, endLine);
/*      */ 
/* 1898 */       String inBuffer = StringUtils.toStringRaw(bArray, 0, endLine);
/*      */ 
/* 1902 */       parseLocalParametersEx(data, inBuffer, delimiter, true);
/*      */     }
/* 1904 */     else if ((action.equalsIgnoreCase("GET")) || (action.equalsIgnoreCase("HEAD")))
/*      */     {
/* 1906 */       if (!hasQueryValues)
/*      */       {
/* 1908 */         String msg = LocaleUtils.encodeMessage("syGetMustHaveQueryString", null, action);
/* 1909 */         throw new DataException(msg);
/*      */       }
/* 1911 */       data.m_isGet = true;
/*      */     } else {
/* 1913 */       if (action.equalsIgnoreCase("PROPFIND"))
/*      */       {
/* 1915 */         return;
/*      */       }
/*      */ 
/* 1919 */       String host = data.m_environment.getProperty("HTTP_HOST");
/* 1920 */       String msg = LocaleUtils.encodeMessage("syRequestNotLegal", null, host, action);
/* 1921 */       throw new IOException(msg);
/*      */     }
/*      */ 
/* 1928 */     if (checkIsApplicationCall(data))
/*      */     {
/* 1930 */       data.setContentType("text/plain");
/* 1931 */       data.m_isJava = true;
/*      */     }
/*      */ 
/* 1935 */     Vector rsetList = new IdcVector();
/* 1936 */     int start = byteNextLineStart[0];
/* 1937 */     if ((bRead > start) && (data.m_isJava) && (bArray != null))
/*      */     {
/* 1939 */       int len = bArray.length - start;
/* 1940 */       int endLine = readLineBytes(data, bArray, false, start, len, byteNextLineStart);
/* 1941 */       boolean determinedEncoding = false;
/* 1942 */       String encoding = FileUtils.checkForUnicodeEncoding(bArray, start, endLine - start);
/* 1943 */       if (encoding != null)
/*      */       {
/* 1945 */         determinedEncoding = true;
/*      */       }
/*      */ 
/* 1948 */       String hdaHeader = StringUtils.getString(bArray, start, endLine - start, encoding);
/* 1949 */       if ((hdaHeader != null) && 
/* 1951 */         (!determinedEncoding))
/*      */       {
/* 1953 */         encoding = parseHdaEncoding(hdaHeader);
/* 1954 */         if ((encoding != null) && (encoding.length() > 0))
/*      */         {
/* 1957 */           start = byteNextLineStart[0];
/*      */ 
/* 1960 */           data.m_localData.put("ClientEncoding", encoding);
/* 1961 */           data.m_determinedEncoding = true;
/* 1962 */           data.m_clientEncoding = encoding;
/* 1963 */           determinedEncoding = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1969 */       if (!determinedEncoding)
/*      */       {
/* 1971 */         encoding = determineEncoding(data, null);
/*      */       }
/*      */ 
/* 1974 */       String content = StringUtils.getString(bArray, start, bRead - start, encoding);
/*      */ 
/* 1977 */       BufferedReader cReader = new BufferedReader(new StringReader(content));
/* 1978 */       data.m_isCgi = false;
/*      */ 
/* 1980 */       parseReaderData(data, cReader, rsetList, false);
/*      */     }
/*      */ 
/* 1983 */     DataSerializeUtils.determineParameterizedLocalization(data, rsetList);
/*      */   }
/*      */ 
/*      */   public boolean checkIsApplicationCall(DataBinder data)
/*      */   {
/* 1993 */     String isJava = data.getLocal("IsJava");
/* 1994 */     return (isJava != null) && (StringUtils.convertToBool(isJava, true));
/*      */   }
/*      */ 
/*      */   public void parseLocalParameters(DataBinder data, String params, String delimiter, ExecutionContext cxt)
/*      */   {
/* 2005 */     if (cxt != null)
/*      */     {
/* 2007 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/* 2009 */     parseLocalParametersEx(data, params, delimiter, true);
/*      */   }
/*      */ 
/*      */   protected void parseLocalParametersEx(DataBinder data, String params, String delimiter, boolean allowRaw)
/*      */   {
/* 2021 */     StringTokenizer envTokenizer = new StringTokenizer(params, delimiter);
/* 2022 */     data.m_previousValues.clear();
/* 2023 */     while (envTokenizer.hasMoreTokens())
/*      */     {
/* 2025 */       String envPair = decodeEx(data, envTokenizer.nextToken());
/* 2026 */       parseNameValue(data, envPair, "=", allowRaw);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void parseNameValue(DataBinder data, String envPair, String delimiter, boolean allowRaw)
/*      */   {
/* 2038 */     String key = null;
/* 2039 */     String value = null;
/* 2040 */     int index = envPair.indexOf(delimiter);
/* 2041 */     if (index < 0)
/*      */     {
/* 2043 */       key = envPair;
/* 2044 */       value = "";
/*      */     }
/*      */     else
/*      */     {
/* 2048 */       key = envPair.substring(0, index);
/* 2049 */       if (index + 1 == envPair.length())
/*      */       {
/* 2051 */         value = "";
/*      */       }
/*      */       else
/*      */       {
/* 2055 */         value = envPair.substring(index + 1);
/*      */       }
/*      */     }
/*      */ 
/* 2059 */     if ((key.equals("ClientEncoding")) && (value != null) && (value.length() > 0))
/*      */     {
/* 2061 */       data.m_determinedEncoding = true;
/* 2062 */       data.m_clientEncoding = value;
/*      */     }
/*      */ 
/* 2077 */     if ((data.m_determinedEncoding) || (!allowRaw))
/*      */     {
/*      */       try
/*      */       {
/* 2081 */         String encoding = (allowRaw) ? data.m_clientEncoding : this.m_systemEncoding;
/*      */ 
/* 2083 */         DataSerializeUtils.translateAndSetRawValueEx(data.m_localData, key, value, encoding, data.m_previousValues);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 2088 */         Report.trace(null, null, e);
/* 2089 */         e.printStackTrace();
/* 2090 */         DataSerializeUtils.addCollatedProps(data.m_localData, key, value, data.m_previousValues);
/*      */       }
/*      */     }
/*      */ 
/* 2094 */     if ((!data.m_determinedEncoding) && (((isRawData(data)) || (data.m_isCgi))) && (allowRaw))
/*      */     {
/* 2096 */       data.m_rawData.addElement(new String[] { key, value });
/* 2097 */       DataSerializeUtils.addCollatedProps(data.m_localData, key, value, data.m_previousValues);
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/* 2104 */         String encoding = ((allowRaw) && (data.m_determinedEncoding)) ? data.m_clientEncoding : this.m_systemEncoding;
/*      */ 
/* 2106 */         DataSerializeUtils.translateAndSetRawValueEx(data.m_localData, key, value, encoding, data.m_previousValues);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 2110 */         Report.trace(null, null, e);
/* 2111 */         e.printStackTrace();
/* 2112 */         DataSerializeUtils.addCollatedProps(data.m_localData, key, value, data.m_previousValues);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public int parseFormat(DataBinder data, BufferedInputStream bis, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/* 2130 */     String fileInfo = null;
/* 2131 */     int formatType = 0;
/* 2132 */     SystemUtils.reportDeprecatedUsage("parseFormat");
/*      */ 
/* 2135 */     if (cxt != null)
/*      */     {
/* 2137 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2142 */       String line = null;
/* 2143 */       String encoding = determineEncoding(data, null);
/*      */ 
/* 2145 */       while ((line = readLineEx(data, bis, false, true, null)) != null)
/*      */       {
/* 2147 */         if (line.length() == 0)
/*      */         {
/* 2150 */           bis.mark(100);
/*      */ 
/* 2154 */           int arraySize = 100;
/* 2155 */           byte[] inByte = new byte[arraySize];
/* 2156 */           bis.read(inByte, 0, 24);
/*      */ 
/* 2158 */           fileInfo = StringUtils.getString(inByte, 0, 24, encoding);
/* 2159 */           int index = fileInfo.indexOf("IDCFILE");
/* 2160 */           if (index >= 0)
/*      */             break;
/* 2162 */           fileInfo = null; break;
/*      */         }
/*      */ 
/* 2166 */         if (line.charAt(0) == '@')
/*      */         {
/* 2168 */           formatType = 1;
/* 2169 */           break;
/*      */         }
/*      */ 
/* 2174 */         if (data.m_isJava)
/*      */         {
/* 2176 */           line = decodeEx(data, line);
/*      */         }
/*      */ 
/* 2179 */         int index = line.indexOf(58);
/* 2180 */         if (index >= 0)
/*      */         {
/* 2182 */           String name = line.substring(0, index);
/* 2183 */           String value = line.substring(index + 1).trim();
/*      */ 
/* 2185 */           data.setEnvironmentValue(name, value);
/*      */         }
/*      */         else
/*      */         {
/* 2189 */           index = line.indexOf(61);
/* 2190 */           if (index >= 0)
/*      */           {
/* 2192 */             formatType = 2;
/* 2193 */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2201 */       throw new ServiceException("!syUnableToReadStream", e);
/*      */     }
/*      */ 
/* 2204 */     if (formatType == 0)
/*      */     {
/* 2206 */       if (fileInfo == null)
/*      */       {
/* 2208 */         formatType = 1;
/*      */       }
/*      */       else
/*      */       {
/* 2212 */         data.putLocal("IDCFILE", fileInfo);
/* 2213 */         formatType = 3;
/*      */       }
/*      */     }
/*      */ 
/* 2217 */     return formatType;
/*      */   }
/*      */ 
/*      */   protected String parseFile(DataBinder data, String name, String fileName)
/*      */     throws IOException, DataException
/*      */   {
/* 2236 */     String line = readLineEx(data, data.m_inStream, true, true, null);
/* 2237 */     String uLine = line.toUpperCase();
/* 2238 */     if (uLine.indexOf("CONTENT-TYPE") >= 0)
/*      */     {
/* 2240 */       line = readLineEx(data, data.m_inStream, true, true, null);
/*      */     }
/*      */ 
/* 2243 */     if (fileName.length() != 0)
/*      */     {
/* 2245 */       readFile(data, fileName, name);
/*      */     }
/*      */     else
/*      */     {
/* 2250 */       line = readLineEx(data, data.m_inStream, true, true, null);
/* 2251 */       if (line.indexOf(data.m_boundary) >= 0)
/*      */       {
/* 2253 */         return line;
/*      */       }
/*      */     }
/*      */ 
/* 2257 */     line = readLineEx(data, data.m_inStream, true, true, null);
/* 2258 */     return line;
/*      */   }
/*      */ 
/*      */   public void readFile(DataBinder data, String originalFilePath, String key)
/*      */     throws IOException, DataException
/*      */   {
/* 2268 */     long count = DataBinder.getNextFileCounter();
/*      */ 
/* 2270 */     originalFilePath = FileUtils.fileSlashes(originalFilePath);
/* 2271 */     String fileName = FileUtils.getName(originalFilePath);
/*      */ 
/* 2273 */     int index = fileName.lastIndexOf(46);
/* 2274 */     if (index < 0)
/*      */     {
/* 2277 */       index = fileName.length();
/*      */     }
/*      */ 
/* 2280 */     FileOutputStream fos = null;
/* 2281 */     String name = null;
/*      */ 
/* 2284 */     String digestAlgorithmName = data.getEnvironmentValue("Checksum_Algorithm");
/* 2285 */     if (digestAlgorithmName == null)
/*      */     {
/* 2287 */       digestAlgorithmName = "SHA-256";
/*      */     }
/*      */ 
/* 2290 */     StringBuffer fileChecksum = new StringBuffer();
/* 2291 */     MessageDigest fileDigest = null;
/* 2292 */     boolean doDigest = false;
/* 2293 */     if ((SharedObjects.getEnvValueAsBoolean(key + ":computeChecksum", false)) && (digestAlgorithmName != null) && (digestAlgorithmName != ""))
/*      */     {
/*      */       try
/*      */       {
/* 2298 */         fileDigest = MessageDigest.getInstance(digestAlgorithmName);
/* 2299 */         doDigest = true;
/*      */       }
/*      */       catch (NoSuchAlgorithmException nsae)
/*      */       {
/* 2303 */         doDigest = false;
/* 2304 */         Report.appWarning("Checksum", null, LocaleUtils.encodeMessage("syNoSuchChecksumDigestAlgorithm", nsae.getMessage(), digestAlgorithmName), nsae);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2311 */       if (data.m_overrideTempDir != null)
/*      */       {
/* 2313 */         name = data.m_overrideTempDir;
/*      */       }
/*      */       else
/*      */       {
/* 2317 */         name = DataBinder.m_tempDir;
/*      */       }
/* 2319 */       name = name + Long.toString(count) + fileName.substring(index);
/* 2320 */       fos = new FileOutputStream(name);
/* 2321 */       data.m_localData.put(key + ":path", name);
/* 2322 */       data.m_tempFiles.addElement(name);
/*      */ 
/* 2325 */       int bdryLength = data.m_boundaryBytes.length;
/* 2326 */       int arraySize = 1024;
/* 2327 */       byte[] bArray = new byte[arraySize];
/* 2328 */       boolean isEnd = false;
/*      */ 
/* 2330 */       long globalCount = 0L;
/* 2331 */       while (!isEnd)
/*      */       {
/* 2334 */         data.m_inStream.mark(2000);
/* 2335 */         int bRead = 0;
/* 2336 */         int wByte = 0;
/*      */         try
/*      */         {
/* 2342 */           bRead = data.m_inStream.read(bArray);
/*      */ 
/* 2345 */           while (bRead <= bdryLength + 4)
/*      */           {
/* 2347 */             int mRead = data.m_inStream.read(bArray, bRead, arraySize - bRead);
/* 2348 */             if (mRead < 0)
/*      */             {
/* 2350 */               isEnd = true;
/* 2351 */               break;
/*      */             }
/* 2353 */             bRead += mRead;
/*      */           }
/*      */ 
/* 2356 */           wByte = bRead - bdryLength - 2;
/* 2357 */           if (wByte <= 0)
/*      */           {
/* 2359 */             throw new IOException("!syFailedToReadAdditionalBytes");
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/* 2367 */           throw new DataException(e, "syClientAbortedUpload", new Object[] { fileName });
/*      */         }
/*      */ 
/* 2371 */         index = -1;
/* 2372 */         int end = bRead - bdryLength;
/* 2373 */         for (int j = 0; j < end; ++j)
/*      */         {
/* 2375 */           boolean matched = true;
/* 2376 */           for (int k = 0; k < bdryLength; ++k)
/*      */           {
/* 2378 */             if (bArray[(j + k)] == data.m_boundaryBytes[k])
/*      */               continue;
/* 2380 */             matched = false;
/* 2381 */             break;
/*      */           }
/*      */ 
/* 2384 */           if (!matched)
/*      */             continue;
/* 2386 */           index = j;
/* 2387 */           break;
/*      */         }
/*      */ 
/* 2391 */         int wb = wByte;
/* 2392 */         if (index >= 0)
/*      */         {
/* 2394 */           isEnd = true;
/* 2395 */           wByte = index;
/* 2396 */           wb = index - 2;
/*      */         }
/*      */ 
/* 2400 */         fos.write(bArray, 0, wb);
/* 2401 */         data.m_inStream.reset();
/* 2402 */         data.m_inStream.skip(wByte);
/*      */ 
/* 2405 */         if (doDigest)
/*      */         {
/* 2407 */           fileDigest.update(bArray, 0, wb);
/*      */         }
/*      */ 
/* 2410 */         data.m_remainingLength -= wByte;
/* 2411 */         globalCount += wByte;
/* 2412 */         data.reportProgress(fileName, globalCount);
/*      */       }
/*      */ 
/* 2415 */       if (doDigest)
/*      */       {
/* 2417 */         byte[] digestBuffer = fileDigest.digest();
/* 2418 */         for (int i = 0; i < digestBuffer.length; ++i)
/*      */         {
/* 2420 */           fileChecksum.append(Integer.toHexString(0x100 | 0xFF & digestBuffer[i]).substring(1));
/*      */         }
/* 2422 */         data.m_localData.put(key + ":checksum", fileChecksum.toString());
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2428 */       e.printStackTrace();
/*      */ 
/* 2430 */       throw e;
/*      */     }
/*      */     finally
/*      */     {
/* 2434 */       closeStream(fos);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void closeStream(OutputStream outStream)
/*      */   {
/*      */     try
/*      */     {
/* 2445 */       if (outStream != null)
/*      */       {
/* 2447 */         outStream.close();
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2452 */       Report.trace(null, null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String detectEncoding(DataBinder data, BufferedInputStream bstream, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/* 2466 */     byte[] temp = new byte[512];
/* 2467 */     int len = temp.length;
/* 2468 */     int bufSize = 0;
/* 2469 */     boolean foundIt = false;
/*      */ 
/* 2471 */     bstream.mark(len);
/* 2472 */     int end = bstream.read(temp, 0, len);
/*      */ 
/* 2474 */     boolean determinedEncoding = false;
/* 2475 */     boolean useBinderEncoding = true;
/* 2476 */     boolean skipHeader = false;
/* 2477 */     String encodingComment = null;
/* 2478 */     String defEncoding = null;
/* 2479 */     String encoding = FileUtils.checkForUnicodeEncoding(temp, 0, end);
/* 2480 */     if (encoding != null)
/*      */     {
/* 2482 */       encodingComment = "found magic sequence";
/* 2483 */       determinedEncoding = true;
/* 2484 */       useBinderEncoding = false;
/* 2485 */       defEncoding = encoding;
/*      */     }
/*      */ 
/* 2490 */     for (int i = 0; (i < end) && (!foundIt); ++i)
/*      */     {
/* 2492 */       char ch = (char)temp[i];
/* 2493 */       if (ch != '\n')
/*      */         continue;
/* 2495 */       bufSize = i;
/* 2496 */       foundIt = true;
/*      */     }
/*      */ 
/* 2499 */     if ((encoding == null) && (foundIt))
/*      */     {
/* 2501 */       encoding = FileUtils.checkForASCIIEncoding(temp, 0, bufSize);
/* 2502 */       if (encoding != null)
/*      */       {
/* 2505 */         defEncoding = encoding;
/* 2506 */         useBinderEncoding = false;
/* 2507 */         encodingComment = "found 7-bit clean";
/*      */       }
/*      */     }
/*      */ 
/* 2511 */     if (useBinderEncoding)
/*      */     {
/* 2513 */       defEncoding = determineEncoding(data, null);
/* 2514 */       encodingComment = "used DataBinder's encoding";
/*      */     }
/* 2516 */     if (foundIt)
/*      */     {
/* 2518 */       String line = null;
/* 2519 */       while (bufSize < end)
/*      */       {
/* 2521 */         IdcStringBuilder buf = new IdcStringBuilder();
/* 2522 */         int results = StringUtils.copyByteArray(buf, temp, 0, bufSize, defEncoding, 24);
/*      */ 
/* 2525 */         if (results == 0)
/*      */         {
/* 2527 */           int length = buf.length();
/* 2528 */           if ((length > 0) && (buf.charAt(length - 1) == '\n'))
/*      */           {
/* 2530 */             line = buf.toString();
/* 2531 */             break;
/*      */           }
/*      */         }
/* 2534 */         ++bufSize;
/*      */       }
/* 2536 */       if (line == null)
/*      */       {
/* 2538 */         Report.trace("encoding", "DataBinderSerializer.detectEncoding(): unable to convert bytes into a full line of characters", null);
/*      */ 
/* 2540 */         line = "";
/*      */       }
/* 2542 */       String hdaEncoding = parseHdaEncoding(line);
/* 2543 */       if (hdaEncoding != null)
/*      */       {
/* 2545 */         skipHeader = true;
/* 2546 */         if (!determinedEncoding)
/*      */         {
/* 2548 */           encoding = hdaEncoding;
/* 2549 */           encodingComment = "found encoding in header";
/*      */         }
/* 2553 */         else if (!hdaEncoding.equalsIgnoreCase(encoding))
/*      */         {
/* 2555 */           Report.trace("encoding", "Mismatched encoding " + hdaEncoding + " and " + encoding, null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2562 */     bstream.reset();
/*      */ 
/* 2565 */     if (skipHeader)
/*      */     {
/* 2567 */       if (this.m_verboseEncodingTracing)
/*      */       {
/* 2569 */         Report.debug("encoding", "detectEncoding() skipping " + bufSize + " bytes", null);
/*      */       }
/*      */ 
/* 2572 */       bstream.skip(bufSize);
/* 2573 */       if (this.m_verboseEncodingTracing)
/*      */       {
/* 2575 */         bstream.mark(15);
/* 2576 */         int cx = bstream.read(temp, 0, 15);
/* 2577 */         IdcStringBuilder traceBuf = new IdcStringBuilder();
/* 2578 */         IdcStringBuilder asciiBuf = new IdcStringBuilder();
/* 2579 */         traceBuf.append("detectEncoding() next bytes are: '");
/* 2580 */         asciiBuf.append("' (");
/* 2581 */         for (int i = 0; i < cx; ++i)
/*      */         {
/* 2583 */           byte b = temp[i];
/* 2584 */           if (i > 0)
/*      */           {
/* 2586 */             traceBuf.append(' ');
/*      */           }
/* 2588 */           String hexString = Integer.toHexString(b);
/* 2589 */           if (hexString.length() == 1)
/*      */           {
/* 2591 */             traceBuf.append('0');
/*      */           }
/* 2593 */           traceBuf.append(hexString);
/* 2594 */           if ((b >= 32) && (b < 128))
/*      */           {
/* 2596 */             asciiBuf.append((char)b);
/*      */           }
/*      */           else
/*      */           {
/* 2600 */             asciiBuf.append('.');
/*      */           }
/*      */         }
/* 2603 */         traceBuf.append(asciiBuf);
/* 2604 */         traceBuf.append(')');
/* 2605 */         Report.debug("encoding", traceBuf.toString(), null);
/* 2606 */         bstream.reset();
/*      */       }
/*      */     }
/*      */ 
/* 2610 */     if (encoding == null)
/*      */     {
/* 2613 */       encoding = defEncoding;
/* 2614 */       encodingComment = "using default encoding";
/*      */     }
/* 2616 */     if (this.m_verboseEncodingTracing)
/*      */     {
/* 2618 */       Report.debug("encoding", "detectEncoding() " + encodingComment + " for " + encoding, null);
/*      */     }
/*      */ 
/* 2621 */     return encoding;
/*      */   }
/*      */ 
/*      */   public String parseHdaEncodingEx(DataBinder data, String line)
/*      */   {
/* 2632 */     String encoding = null;
/* 2633 */     int index = 0;
/* 2634 */     boolean isJavaEncoding = true;
/*      */ 
/* 2636 */     int endIndex = -1;
/* 2637 */     int offset = line.indexOf("<?");
/* 2638 */     if (offset >= 0)
/*      */     {
/* 2640 */       endIndex = line.indexOf("?>");
/*      */     }
/* 2642 */     if (endIndex > 0)
/*      */     {
/* 2644 */       index = line.indexOf("jcharset");
/* 2645 */       if ((index < offset) || (index > endIndex))
/*      */       {
/* 2647 */         index = line.indexOf("encoding");
/* 2648 */         if ((index > offset) && (index < endIndex))
/*      */         {
/* 2650 */           isJavaEncoding = false;
/*      */         }
/*      */       }
/*      */ 
/* 2654 */       if ((index > offset) && (index < endIndex))
/*      */       {
/* 2656 */         int startIndex = 0;
/* 2657 */         int spaceIndex = 0;
/* 2658 */         boolean removeQuote = false;
/*      */ 
/* 2660 */         index = line.indexOf(61, index);
/*      */ 
/* 2662 */         if ((index > 0) && (index < endIndex))
/*      */         {
/* 2664 */           spaceIndex = line.indexOf(32, index);
/* 2665 */           if ((spaceIndex > 0) && (spaceIndex < endIndex))
/*      */           {
/* 2667 */             endIndex = spaceIndex;
/*      */           }
/*      */ 
/* 2670 */           startIndex = index + 1;
/* 2671 */           if (startIndex + 1 < endIndex)
/*      */           {
/* 2673 */             char ch = line.charAt(startIndex);
/* 2674 */             if ((ch == '"') || (ch == '\''))
/*      */             {
/* 2676 */               ++startIndex;
/* 2677 */               removeQuote = true;
/*      */             }
/*      */           }
/* 2680 */           char ch = line.charAt(endIndex - 1);
/* 2681 */           if ((ch == '"') || (ch == '\''))
/*      */           {
/* 2683 */             --endIndex;
/* 2684 */             removeQuote = true;
/*      */           }
/* 2686 */           if (startIndex < endIndex)
/*      */           {
/* 2688 */             encoding = line.substring(startIndex, endIndex);
/* 2689 */             if (removeQuote)
/*      */             {
/* 2691 */               encoding = encoding.trim();
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 2696 */           if (!isJavaEncoding)
/*      */           {
/* 2698 */             encoding = this.m_encodingMap.getJavaEncoding(encoding);
/*      */           }
/*      */           else
/*      */           {
/* 2703 */             encoding = LocaleResources.getEncodingFromAlias(encoding);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2709 */       if (data != null)
/*      */       {
/* 2711 */         index = line.indexOf("escape");
/* 2712 */         if (index >= 0)
/*      */         {
/* 2714 */           index = line.indexOf(61, index);
/* 2715 */           if ((index > 0) && (line.indexOf("url", index) > 0))
/*      */           {
/* 2717 */             data.m_isCgi = true;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2723 */     return encoding;
/*      */   }
/*      */ 
/*      */   public String parseHdaEncoding(String line)
/*      */   {
/* 2733 */     return parseHdaEncodingEx(null, line);
/*      */   }
/*      */ 
/*      */   public String packageEncodingHeader(DataBinder data, ExecutionContext cxt)
/*      */     throws IOException
/*      */   {
/* 2741 */     String jcharset = determineEncoding(data, cxt);
/*      */ 
/* 2743 */     return DataSerializeUtils.createEncodingHeaderString(jcharset);
/*      */   }
/*      */ 
/*      */   public String determineEncoding(DataBinder data, ExecutionContext cxt)
/*      */   {
/* 2755 */     if (cxt != null)
/*      */     {
/* 2757 */       retrieveExecutionContextInfo(data, cxt);
/*      */     }
/*      */ 
/* 2760 */     String encoding = null;
/*      */ 
/* 2764 */     if (isClientEncoding(data))
/*      */     {
/* 2766 */       encoding = data.m_clientEncoding;
/*      */     }
/*      */ 
/* 2770 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 2772 */       encoding = data.m_javaEncoding;
/*      */     }
/*      */ 
/* 2776 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 2778 */       encoding = this.m_systemEncoding;
/*      */     }
/* 2780 */     return encoding;
/*      */   }
/*      */ 
/*      */   protected void retrieveExecutionContextInfo(DataBinder data, ExecutionContext cxt)
/*      */   {
/* 2785 */     String encoding = (String)cxt.getCachedObject("ClientEncoding");
/* 2786 */     if ((encoding == null) || (encoding.length() <= 0))
/*      */       return;
/* 2788 */     data.m_clientEncoding = encoding;
/* 2789 */     data.m_determinedEncoding = true;
/*      */   }
/*      */ 
/*      */   public void setEncodingMap(ResultSet rset)
/*      */     throws DataException
/*      */   {
/* 2796 */     this.m_encodingMap.load(rset);
/*      */   }
/*      */ 
/*      */   public String getIsoEncoding(String javaEncoding)
/*      */   {
/* 2801 */     String isoEncoding = this.m_encodingMap.getIsoEncoding(javaEncoding);
/* 2802 */     if ((isoEncoding == null) && 
/* 2806 */       (this.m_encodingMap.getJavaEncoding(javaEncoding) != null))
/*      */     {
/* 2808 */       isoEncoding = javaEncoding;
/*      */     }
/*      */ 
/* 2811 */     return isoEncoding;
/*      */   }
/*      */ 
/*      */   public String getJavaEncoding(String isoEncoding)
/*      */   {
/* 2816 */     return this.m_encodingMap.getJavaEncoding(isoEncoding);
/*      */   }
/*      */ 
/*      */   public void setMultiMode(boolean flag)
/*      */   {
/* 2821 */     this.m_isMultiMode = flag;
/*      */   }
/*      */ 
/*      */   public boolean isMultiMode()
/*      */   {
/* 2826 */     return this.m_isMultiMode;
/*      */   }
/*      */ 
/*      */   public void setUseClientEncoding(boolean flag) {
/* 2830 */     this.m_useClientEncoding = flag;
/*      */   }
/*      */ 
/*      */   public boolean useClientEncoding() {
/* 2834 */     return this.m_useClientEncoding;
/*      */   }
/*      */ 
/*      */   public String getSystemEncoding() {
/* 2838 */     return this.m_systemEncoding;
/*      */   }
/*      */ 
/*      */   public void setSystemEncoding(String systemEncoding) {
/* 2842 */     this.m_systemEncoding = systemEncoding;
/*      */   }
/*      */ 
/*      */   public String getWebEncoding() {
/* 2846 */     String encoding = getWebEncodingEx();
/* 2847 */     if (encoding == null)
/*      */     {
/* 2849 */       return this.m_systemEncoding;
/*      */     }
/* 2851 */     return encoding;
/*      */   }
/*      */ 
/*      */   public String getWebEncodingEx() {
/* 2855 */     return this.m_webEncoding;
/*      */   }
/*      */ 
/*      */   public void setWebEncoding(String newEncoding) {
/* 2859 */     this.m_webEncoding = newEncoding;
/*      */   }
/*      */ 
/*      */   public void setDataBinderProtocol(DataBinderProtocolInterface dataBinderProtocol)
/*      */   {
/* 2864 */     this.m_dataBinderProtocol = dataBinderProtocol;
/*      */   }
/*      */ 
/*      */   public DataBinderProtocolInterface getDataBinderProtocol()
/*      */   {
/* 2869 */     return this.m_dataBinderProtocol;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2874 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99688 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.serialize.DataBinderSerializer
 * JD-Core Version:    0.5.4
 */