/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.VersionInfo;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.CharConversionException;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.text.ParseException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataSerializeUtils
/*     */ {
/*     */   protected static DataSerialize m_serialize;
/*     */   public static boolean m_useOverrideEncodingHeaderVersion;
/*     */   public static String m_overrideEncodingHeaderVersionString;
/*     */ 
/*     */   public static void setDataSerialize(DataSerialize ds)
/*     */   {
/*  43 */     m_serialize = ds;
/*     */   }
/*     */ 
/*     */   public static DataSerialize getDataSerialize()
/*     */   {
/*  48 */     return m_serialize;
/*     */   }
/*     */ 
/*     */   static boolean validateEnvironmentData(Object envData)
/*     */   {
/*  53 */     if (m_serialize != null)
/*     */     {
/*  55 */       return m_serialize.validateEnvironmentData(envData);
/*     */     }
/*  57 */     return true;
/*     */   }
/*     */ 
/*     */   public static void send(DataBinder data, Writer writer, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/*  63 */     m_serialize.sendEx(data, writer, true, cxt);
/*     */   }
/*     */ 
/*     */   public static void sendEx(DataBinder data, Writer writer, boolean sendHeader, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/*  69 */     m_serialize.sendEx(data, writer, sendHeader, cxt);
/*     */   }
/*     */ 
/*     */   public static byte[] sendBytes(DataBinder data, String javaEncoding, boolean sendHeader, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/*  75 */     return m_serialize.sendBytes(data, javaEncoding, sendHeader, cxt);
/*     */   }
/*     */ 
/*     */   public static void receive(DataBinder data, BufferedReader reader, boolean isHeaderOnly, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/*  81 */     m_serialize.receiveEx(data, reader, false, cxt);
/*     */   }
/*     */ 
/*     */   public static void receiveEx(DataBinder data, BufferedReader reader, boolean isHeaderOnly, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/*  87 */     m_serialize.receiveEx(data, reader, isHeaderOnly, cxt);
/*     */   }
/*     */ 
/*     */   public static String decode(DataBinder data, String in, ExecutionContext cxt)
/*     */   {
/*  92 */     return m_serialize.decode(data, in, cxt);
/*     */   }
/*     */ 
/*     */   public static String encode(DataBinder data, String in, ExecutionContext cxt)
/*     */   {
/*  97 */     return m_serialize.encode(data, in, cxt);
/*     */   }
/*     */ 
/*     */   public static void parseRequest(DataBinder data, BufferedInputStream inStream, ExecutionContext cxt)
/*     */     throws IOException, DataException
/*     */   {
/* 103 */     m_serialize.parseRequest(data, inStream, cxt);
/*     */   }
/*     */ 
/*     */   public static void prepareParseRequest(DataBinder data, BufferedInputStream inStream, ExecutionContext cxt)
/*     */     throws IOException, DataException
/*     */   {
/* 109 */     m_serialize.prepareParseRequest(data, inStream, cxt);
/*     */   }
/*     */ 
/*     */   public static void parseRequestBody(DataBinder data, ExecutionContext cxt) throws IOException, DataException
/*     */   {
/* 114 */     m_serialize.parseRequestBody(data, cxt);
/*     */   }
/*     */ 
/*     */   public static String parseHdaEncoding(String line)
/*     */   {
/* 119 */     return m_serialize.parseHdaEncoding(line);
/*     */   }
/*     */ 
/*     */   public static String parseHdaEncodingEx(DataBinder data, String line)
/*     */   {
/* 124 */     return m_serialize.parseHdaEncodingEx(data, line);
/*     */   }
/*     */ 
/*     */   public static int determineContentType(DataBinder data, ExecutionContext cxt) throws DataException
/*     */   {
/* 129 */     return m_serialize.determineContentType(data, cxt);
/*     */   }
/*     */ 
/*     */   public static void resetMultiContentFlags(DataBinder data, ExecutionContext cxt)
/*     */   {
/* 134 */     m_serialize.resetMultiContentFlags(data, cxt);
/*     */   }
/*     */ 
/*     */   public static void continueParse(DataBinder data, ExecutionContext cxt)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 140 */     m_serialize.continueParse(data, cxt);
/*     */   }
/*     */ 
/*     */   public static String readLineEx(DataBinder data, BufferedInputStream inStream, boolean doCount, boolean allowRaw, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/* 147 */     return m_serialize.readLineEx(data, inStream, doCount, allowRaw, cxt);
/*     */   }
/*     */ 
/*     */   public static void parseLocalParameters(DataBinder data, String params, String delimiter, ExecutionContext cxt)
/*     */   {
/* 152 */     m_serialize.parseLocalParameters(data, params, delimiter, cxt);
/*     */   }
/*     */ 
/*     */   public static String detectEncoding(DataBinder data, BufferedInputStream bstream, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/* 159 */     return m_serialize.detectEncoding(data, bstream, cxt);
/*     */   }
/*     */ 
/*     */   public static String packageEncodingHeader(DataBinder data, ExecutionContext cxt) throws IOException
/*     */   {
/* 164 */     return m_serialize.packageEncodingHeader(data, cxt);
/*     */   }
/*     */ 
/*     */   public static String createEncodingHeaderString(String jcharset)
/*     */   {
/* 169 */     String retStr = null;
/* 170 */     if ((jcharset != null) && (jcharset.length() > 0))
/*     */     {
/* 172 */       String version = (m_useOverrideEncodingHeaderVersion) ? m_overrideEncodingHeaderVersionString : VersionInfo.getProductVersion();
/* 173 */       if ((null != version) && (version.length() > 0))
/*     */       {
/* 175 */         version = "version=\"" + version + "\" ";
/*     */       }
/*     */       else
/*     */       {
/* 179 */         version = "";
/*     */       }
/* 181 */       retStr = "<?hda " + version + "jcharset=\"" + jcharset;
/* 182 */       String isoEncoding = getIsoEncoding(jcharset);
/*     */ 
/* 184 */       if ((isoEncoding != null) && (isoEncoding.length() > 0))
/*     */       {
/* 186 */         retStr = retStr + "\" encoding=\"" + isoEncoding;
/*     */       }
/*     */ 
/* 189 */       retStr = retStr + "\"?>\n";
/*     */     }
/*     */ 
/* 192 */     return retStr;
/*     */   }
/*     */ 
/*     */   public static String determineEncoding(DataBinder data, ExecutionContext cxt)
/*     */   {
/* 197 */     return m_serialize.determineEncoding(data, cxt);
/*     */   }
/*     */ 
/*     */   public static String parseCookie(String cookie, String name)
/*     */   {
/* 203 */     String retVal = null;
/* 204 */     String key = name + "=";
/* 205 */     int index = cookie.indexOf(key);
/* 206 */     if (index >= 0)
/*     */     {
/* 208 */       int startIndex = index + key.length();
/* 209 */       int endIndex = cookie.indexOf(";", startIndex);
/* 210 */       if (endIndex < 0)
/*     */       {
/* 212 */         endIndex = cookie.length();
/*     */       }
/* 214 */       retVal = cookie.substring(startIndex, endIndex);
/*     */       try
/*     */       {
/* 217 */         retVal = StringUtils.decodeHttpHeaderStyle(retVal);
/*     */       }
/*     */       catch (CharConversionException ignore)
/*     */       {
/* 221 */         Report.trace(null, "URL-decoding the cookie " + name + " failed.", ignore);
/*     */       }
/*     */     }
/* 224 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static void checkProcessRawEncoding(DataBinder data, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 238 */     if (data.m_determinedEncoding)
/*     */     {
/* 240 */       return;
/*     */     }
/* 242 */     String encoding = getWebEncoding();
/* 243 */     if (encoding == null)
/*     */     {
/* 245 */       encoding = determineEncoding(data, cxt);
/*     */     }
/*     */     try
/*     */     {
/* 249 */       translateRemainingRaw(data, encoding);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 254 */       throw new ServiceException(e);
/*     */     }
/* 256 */     data.m_determinedEncoding = true;
/* 257 */     data.m_clientEncoding = encoding;
/*     */   }
/*     */ 
/*     */   public static void translateRemainingRaw(DataBinder data, String encoding) throws IOException
/*     */   {
/* 262 */     data.m_previousValues.clear();
/* 263 */     for (int i = 0; i < data.m_rawData.size(); ++i)
/*     */     {
/* 265 */       String[] pair = (String[])(String[])data.m_rawData.elementAt(i);
/* 266 */       if (pair == null)
/*     */         continue;
/* 268 */       translateAndSetRawValueEx(data.m_localData, pair[0], pair[1], encoding, data.m_previousValues);
/*     */     }
/*     */ 
/* 271 */     data.m_rawData.removeAllElements();
/*     */   }
/*     */ 
/*     */   public static void translateAndSetRawValue(Properties props, String name, String value, String encoding)
/*     */     throws IOException
/*     */   {
/* 277 */     translateAndSetRawValueEx(props, name, value, encoding, null);
/*     */   }
/*     */ 
/*     */   public static void translateAndSetRawValueEx(Properties props, String name, String value, String encoding, Map<String, String> previousValues)
/*     */     throws IOException
/*     */   {
/* 283 */     byte[] bname = StringUtils.getAsClientBytes(name);
/* 284 */     if (encoding == null)
/*     */     {
/* 286 */       encoding = getSystemEncoding();
/*     */     }
/* 288 */     String nameStr = StringUtils.getString(bname, 0, bname.length, encoding);
/* 289 */     String valueStr = new String();
/*     */ 
/* 291 */     if ((nameStr != null) && (nameStr.length() > 0))
/*     */     {
/* 293 */       byte[] bvalue = StringUtils.getAsClientBytes(value);
/* 294 */       valueStr = StringUtils.getString(bvalue, 0, bvalue.length, encoding);
/*     */     }
/*     */ 
/* 297 */     addCollatedProps(props, nameStr, valueStr, previousValues);
/*     */   }
/*     */ 
/*     */   public static void addCollatedProps(Properties props, String name, String value, Map<String, String> previousValues)
/*     */   {
/* 303 */     String currVal = value;
/* 304 */     if (previousValues != null)
/*     */     {
/* 306 */       String prevVal = (String)previousValues.get(name);
/* 307 */       if ((prevVal != null) && (!name.equals("IdcService")) && (((!prevVal.equals(currVal)) || (StringUtils.convertToBool(props.getProperty("allowDuplicateValues"), false)))))
/*     */       {
/* 310 */         currVal = prevVal + "," + value;
/*     */       }
/* 312 */       previousValues.put(name, currVal);
/*     */     }
/* 314 */     props.put(name, currVal);
/*     */   }
/*     */ 
/*     */   public static void determineParameterizedLocalization(DataBinder data, List rsetList)
/*     */     throws IOException
/*     */   {
/* 330 */     boolean hasDateFields = false;
/* 331 */     String tmp = data.getLocal("blFieldTypes");
/* 332 */     if (tmp != null)
/*     */     {
/* 334 */       Vector list = StringUtils.parseArray(tmp, ',', '^');
/* 335 */       int size = list.size();
/* 336 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 338 */         tmp = (String)list.elementAt(i);
/* 339 */         int index = tmp.lastIndexOf(" ");
/* 340 */         if (index <= 0)
/*     */           continue;
/* 342 */         String key = tmp.substring(0, index);
/* 343 */         String type = tmp.substring(index + 1);
/* 344 */         if (type.length() > 0)
/*     */         {
/* 346 */           data.setFieldType(key, type);
/*     */         }
/* 348 */         if ((hasDateFields) || (!type.equals("date")))
/*     */           continue;
/* 350 */         hasDateFields = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 356 */     tmp = data.getLocal("blDateFormat");
/* 357 */     if ((tmp == null) || (tmp.length() <= 0))
/*     */       return;
/* 359 */     boolean hasInitFailure = false;
/* 360 */     tmp = LocaleUtils.addOptionalElements(tmp);
/* 361 */     IdcDateFormat fmt = new IdcDateFormat();
/*     */     try
/*     */     {
/* 364 */       fmt.init(tmp);
/*     */     }
/*     */     catch (ParseException e)
/*     */     {
/* 368 */       hasInitFailure = true;
/* 369 */       if (hasDateFields)
/*     */       {
/* 371 */         String msg = LocaleUtils.encodeMessage("syUnableToCreateDateFormat", e.getMessage(), tmp);
/*     */ 
/* 373 */         IOException ioE = new IOException(msg);
/* 374 */         SystemUtils.setExceptionCause(ioE, e);
/* 375 */         throw ioE;
/*     */       }
/*     */     }
/* 378 */     if (!hasInitFailure)
/*     */     {
/* 380 */       data.m_blDateFormat = fmt;
/* 381 */       data.m_determinedDataDateFormat = true;
/*     */ 
/* 383 */       if (rsetList == null)
/*     */         return;
/* 385 */       int size = rsetList.size();
/* 386 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 388 */         ResultSet rset = (ResultSet)rsetList.get(i);
/* 389 */         rset.setDateFormat(fmt);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 395 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 397 */       Report.debug(null, "skipping date format initialization", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void translateEnvironment(DataBinder data)
/*     */     throws IOException
/*     */   {
/* 407 */     Enumeration en = data.m_environment.keys();
/* 408 */     String headerEncoding = data.m_environment.getProperty("HEADER_ENCODING");
/* 409 */     if (headerEncoding != null)
/*     */     {
/* 412 */       String translatedEncoding = LocaleResources.getEncodingFromAlias(headerEncoding);
/* 413 */       if (translatedEncoding != null)
/* 414 */         headerEncoding = translatedEncoding;
/*     */     }
/*     */     else
/*     */     {
/* 418 */       headerEncoding = getSystemEncoding();
/*     */     }
/*     */ 
/* 421 */     data.m_previousValues.clear();
/* 422 */     while (en.hasMoreElements())
/*     */     {
/* 424 */       String key = (String)en.nextElement();
/* 425 */       String val = data.m_environment.getProperty(key);
/* 426 */       if (!key.equals("QUERY_STRING"))
/*     */       {
/* 428 */         translateAndSetRawValueEx(data.m_environment, key, val, headerEncoding, data.m_previousValues);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setEncodingMap(ResultSet rset)
/*     */     throws DataException
/*     */   {
/* 439 */     m_serialize.setEncodingMap(rset);
/*     */   }
/*     */ 
/*     */   public static String getIsoEncoding(String javaEncoding)
/*     */   {
/* 444 */     return m_serialize.getIsoEncoding(javaEncoding);
/*     */   }
/*     */ 
/*     */   public static String getJavaEncoding(String isoEncoding)
/*     */   {
/* 449 */     return m_serialize.getJavaEncoding(isoEncoding);
/*     */   }
/*     */ 
/*     */   public static void setMultiMode(boolean flag)
/*     */   {
/* 454 */     m_serialize.setMultiMode(flag);
/*     */   }
/*     */ 
/*     */   public static boolean isMultiMode()
/*     */   {
/* 459 */     return m_serialize.isMultiMode();
/*     */   }
/*     */ 
/*     */   public static void setUseClientEncoding(boolean flag)
/*     */   {
/* 464 */     m_serialize.setUseClientEncoding(flag);
/*     */   }
/*     */ 
/*     */   public static boolean useClientEncoding()
/*     */   {
/* 469 */     return m_serialize.useClientEncoding();
/*     */   }
/*     */ 
/*     */   public static String getSystemEncoding()
/*     */   {
/* 474 */     return m_serialize.getSystemEncoding();
/*     */   }
/*     */ 
/*     */   public static void setSystemEncoding(String systemEncoding)
/*     */   {
/* 479 */     m_serialize.setSystemEncoding(systemEncoding);
/*     */   }
/*     */ 
/*     */   public static String getWebEncoding()
/*     */   {
/* 484 */     return m_serialize.getWebEncoding();
/*     */   }
/*     */ 
/*     */   public static void setWebEncoding(String newEncoding)
/*     */   {
/* 489 */     m_serialize.setWebEncoding(newEncoding);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 494 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93419 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataSerializeUtils
 * JD-Core Version:    0.5.4
 */