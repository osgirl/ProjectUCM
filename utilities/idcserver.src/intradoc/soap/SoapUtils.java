/*     */ package intradoc.soap;
/*     */ 
/*     */ import intradoc.common.CommonDataConversion;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.server.ServiceManager;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Writer;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class SoapUtils
/*     */ {
/*     */   public static String[] getNameValuePair(String nameValueStr, char delimiter)
/*     */   {
/*  34 */     if (nameValueStr == null)
/*     */     {
/*  36 */       return null;
/*     */     }
/*  38 */     nameValueStr = nameValueStr.trim();
/*     */ 
/*  40 */     String name = null;
/*  41 */     String value = null;
/*  42 */     int index = nameValueStr.indexOf(delimiter);
/*  43 */     if (index < 0)
/*     */     {
/*  45 */       name = nameValueStr;
/*  46 */       value = "";
/*     */     }
/*     */     else
/*     */     {
/*  50 */       name = nameValueStr.substring(0, index);
/*  51 */       if (index + 1 == nameValueStr.length())
/*     */       {
/*  53 */         value = "";
/*     */       }
/*     */       else
/*     */       {
/*  57 */         value = nameValueStr.substring(index + 1);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  62 */     value = stripEdgeChars(value, '\'', '\'');
/*  63 */     value = stripEdgeChars(value, '"', '"');
/*  64 */     value = stripEdgeChars(value, '<', '>');
/*     */ 
/*  66 */     String[] nameValue = new String[2];
/*  67 */     nameValue[0] = name.trim();
/*  68 */     nameValue[1] = value.trim();
/*     */ 
/*  70 */     return nameValue;
/*     */   }
/*     */ 
/*     */   public static String stripEdgeChars(String value, char beginChar, char endChar)
/*     */   {
/*  75 */     if ((value == null) || (value.equals("")))
/*     */     {
/*  77 */       return value;
/*     */     }
/*     */ 
/*  80 */     value = value.trim();
/*     */ 
/*  82 */     if (value.charAt(0) == beginChar)
/*     */     {
/*  84 */       if (value.length() > 1)
/*     */       {
/*  86 */         value = value.substring(1);
/*     */       }
/*     */       else
/*     */       {
/*  90 */         value = "";
/*  91 */         return value;
/*     */       }
/*     */     }
/*     */ 
/*  95 */     int length = value.length();
/*  96 */     if (value.charAt(length - 1) == endChar)
/*     */     {
/*  98 */       value = value.substring(0, length - 1);
/*     */     }
/* 100 */     return value;
/*     */   }
/*     */ 
/*     */   public static PropertiesTreeNode getFirstSubNode(List nodeList)
/*     */   {
/* 105 */     if ((nodeList == null) || (nodeList.size() <= 0))
/*     */     {
/* 107 */       return null;
/*     */     }
/*     */ 
/* 110 */     PropertiesTreeNode node = (PropertiesTreeNode)nodeList.get(0);
/* 111 */     return node;
/*     */   }
/*     */ 
/*     */   public static String getNodeName(PropertiesTreeNode node)
/*     */   {
/* 116 */     if (node == null)
/*     */     {
/* 118 */       return "";
/*     */     }
/*     */ 
/* 121 */     String name = node.m_name;
/* 122 */     int index = name.indexOf(58);
/* 123 */     if (index >= 0)
/*     */     {
/* 125 */       name = name.substring(index + 1);
/*     */     }
/*     */ 
/* 128 */     return name;
/*     */   }
/*     */ 
/*     */   public static String getNodeNamespace(PropertiesTreeNode node)
/*     */   {
/* 133 */     if (node == null)
/*     */     {
/* 135 */       return "";
/*     */     }
/*     */ 
/* 138 */     String name = node.m_name;
/* 139 */     String prefix = "";
/*     */ 
/* 141 */     int index = name.indexOf(58);
/* 142 */     if (index > 0)
/*     */     {
/* 144 */       prefix = name.substring(0, index);
/*     */     }
/*     */ 
/* 147 */     String namespace = "";
/* 148 */     if (prefix.equals(""))
/*     */     {
/* 150 */       namespace = getNodeProperty(node, "xmlns");
/*     */     }
/*     */     else
/*     */     {
/* 154 */       namespace = getNodeProperty(node, "xmlns:" + prefix);
/*     */     }
/*     */ 
/* 157 */     if (namespace == null)
/*     */     {
/* 159 */       namespace = "";
/*     */     }
/*     */ 
/* 162 */     return namespace;
/*     */   }
/*     */ 
/*     */   public static String getNodeProperty(PropertiesTreeNode node, String key)
/*     */   {
/* 167 */     if ((node == null) || (node.m_properties == null))
/*     */     {
/* 169 */       return null;
/*     */     }
/*     */ 
/* 172 */     return node.m_properties.getProperty(key);
/*     */   }
/*     */ 
/*     */   public static String getTempFile(DataBinder data, String key, String filePath)
/*     */   {
/* 178 */     if (key == null)
/*     */     {
/* 180 */       key = "primaryFile";
/*     */     }
/*     */ 
/* 184 */     if (filePath == null)
/*     */     {
/* 186 */       filePath = data.getLocal(key);
/* 187 */       if (filePath == null)
/*     */       {
/* 189 */         filePath = "temp.tmp";
/*     */       }
/*     */     }
/*     */ 
/* 193 */     filePath = FileUtils.fileSlashes(filePath);
/* 194 */     String extension = FileUtils.getExtension(filePath);
/* 195 */     if (extension == null)
/*     */     {
/* 197 */       extension = "";
/*     */     }
/*     */ 
/* 200 */     String tempDir = null;
/* 201 */     if (data.m_overrideTempDir != null)
/*     */     {
/* 203 */       tempDir = data.m_overrideTempDir;
/*     */     }
/*     */     else
/*     */     {
/* 207 */       tempDir = DataBinder.m_tempDir;
/*     */     }
/*     */ 
/* 210 */     String tempFileName = "" + DataBinder.getNextFileCounter();
/* 211 */     if (!extension.equals(""))
/*     */     {
/* 213 */       tempFileName = tempFileName + "." + extension;
/*     */     }
/*     */ 
/* 216 */     String tempFilePath = tempDir + tempFileName;
/* 217 */     data.addTempFile(tempFilePath);
/* 218 */     data.putLocal(key + ":path", tempFilePath);
/*     */ 
/* 220 */     return tempFilePath;
/*     */   }
/*     */ 
/*     */   public static byte[] readStream(InputStream is, int length, boolean isReset)
/*     */     throws IOException
/*     */   {
/* 226 */     if (isReset)
/*     */     {
/* 228 */       is.mark(length);
/*     */     }
/*     */ 
/* 231 */     byte[] b = new byte[length];
/* 232 */     int bytesLeft = length;
/* 233 */     int curByte = 0;
/*     */ 
/* 235 */     while (bytesLeft > 0)
/*     */     {
/* 237 */       int numRead = is.read(b, curByte, bytesLeft);
/* 238 */       curByte += numRead;
/* 239 */       bytesLeft -= numRead;
/*     */     }
/*     */ 
/* 242 */     if (isReset)
/*     */     {
/* 244 */       is.reset();
/*     */     }
/*     */ 
/* 247 */     return b;
/*     */   }
/*     */ 
/*     */   public static void writeFileContent(OutputStream os, String fileName)
/*     */     throws IOException
/*     */   {
/* 253 */     File file = new File(fileName);
/* 254 */     if (!file.exists())
/*     */     {
/* 256 */       return;
/*     */     }
/* 258 */     long fileLength = file.length();
/* 259 */     long bytesLeft = fileLength;
/* 260 */     int readSize = 10000;
/* 261 */     byte[] b = new byte[readSize];
/*     */ 
/* 263 */     FileInputStream fis = null;
/*     */     try
/*     */     {
/* 266 */       fis = new FileInputStream(fileName);
/*     */ 
/* 268 */       while (bytesLeft > 0L)
/*     */       {
/* 270 */         int numToRead = 10000;
/* 271 */         if (bytesLeft < numToRead)
/*     */         {
/* 273 */           numToRead = (int)bytesLeft;
/*     */         }
/*     */ 
/* 276 */         int numRead = fis.read(b, 0, numToRead);
/* 277 */         os.write(b, 0, numRead);
/*     */ 
/* 279 */         bytesLeft -= numRead;
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 284 */       if (fis != null)
/*     */       {
/* 286 */         fis.close();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isValidIdcService(String idcService)
/*     */   {
/* 293 */     if (idcService == null)
/*     */     {
/* 295 */       return false;
/*     */     }
/* 297 */     ServiceData serviceData = ServiceManager.getService(idcService);
/* 298 */     if (serviceData == null)
/*     */     {
/* 300 */       return false;
/*     */     }
/*     */ 
/* 305 */     return (serviceData.m_serviceType == null) || (!serviceData.m_serviceType.equalsIgnoreCase("SubService"));
/*     */   }
/*     */ 
/*     */   public static String encodeXmlValue(String value)
/*     */   {
/* 312 */     if (value == null)
/*     */     {
/* 314 */       value = "";
/*     */     }
/* 316 */     int flags = 0;
/*     */ 
/* 318 */     if (SoapXmlSerializer.m_omitIllegalChars)
/*     */     {
/* 320 */       flags = 4;
/*     */     }
/* 322 */     else if (SoapXmlSerializer.m_replaceIllegalChars)
/*     */     {
/* 324 */       flags = 1;
/*     */     }
/*     */ 
/* 327 */     return StringUtils.encodeXmlEscapeSequenceWithFlags(value, null, flags);
/*     */   }
/*     */ 
/*     */   public static String getTabString(int numTabs)
/*     */   {
/* 332 */     String str = "";
/* 333 */     for (int i = 0; i < numTabs; ++i)
/*     */     {
/* 335 */       str = str + '\t';
/*     */     }
/*     */ 
/* 338 */     return str;
/*     */   }
/*     */ 
/*     */   public static int writeSchemaComplexElement(String name, boolean isStartTag, Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 344 */     if (isStartTag)
/*     */     {
/* 346 */       String tabStr = getTabString(numTabs);
/*     */ 
/* 348 */       writer.write(tabStr + "<s:element name=\"" + name + "\">\r\n");
/* 349 */       writer.write(tabStr + "\t<s:complexType>\r\n");
/* 350 */       writer.write(tabStr + "\t\t<s:sequence>\r\n");
/*     */ 
/* 352 */       numTabs += 3;
/*     */     }
/*     */     else
/*     */     {
/* 356 */       numTabs -= 3;
/*     */ 
/* 358 */       String tabStr = getTabString(numTabs);
/*     */ 
/* 360 */       writer.write(tabStr + "\t\t</s:sequence>\r\n");
/* 361 */       writer.write(tabStr + "\t</s:complexType>\r\n");
/* 362 */       writer.write(tabStr + "</s:element>\r\n");
/*     */     }
/*     */ 
/* 365 */     return numTabs;
/*     */   }
/*     */ 
/*     */   public static int writeSchemaComplexType(String name, boolean isStartTag, Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 371 */     if (isStartTag)
/*     */     {
/* 373 */       String tabStr = getTabString(numTabs);
/*     */ 
/* 375 */       writer.write(tabStr + "<s:complexType name=\"" + name + "\">\r\n");
/* 376 */       writer.write(tabStr + "\t<s:sequence>\r\n");
/*     */ 
/* 378 */       numTabs += 2;
/*     */     }
/*     */     else
/*     */     {
/* 382 */       numTabs -= 2;
/*     */ 
/* 384 */       String tabStr = getTabString(numTabs);
/*     */ 
/* 386 */       writer.write(tabStr + "\t</s:sequence>\r\n");
/* 387 */       writer.write(tabStr + "</s:complexType>\r\n");
/*     */     }
/*     */ 
/* 390 */     return numTabs;
/*     */   }
/*     */ 
/*     */   public static void writeSchemaElement(String name, String type, int maxOccurs, Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 396 */     String tabStr = getTabString(numTabs);
/*     */ 
/* 398 */     String maxOccursStr = null;
/* 399 */     if (maxOccurs == -1)
/*     */     {
/* 401 */       maxOccursStr = "unbounded";
/*     */     }
/*     */     else
/*     */     {
/* 405 */       maxOccursStr = "1";
/*     */     }
/*     */ 
/* 408 */     writer.write(tabStr + "<s:element minOccurs=\"0\" maxOccurs=\"" + maxOccursStr + "\" name=\"" + name + "\" type=\"" + type + "\" />\r\n");
/*     */   }
/*     */ 
/*     */   public static void decodeFileContent(DataBinder data, String name)
/*     */   {
/* 415 */     int curFile = NumberUtils.parseInteger(data.getEnvironmentValue("SOAP:CurFileNumber"), 1);
/*     */ 
/* 417 */     String fileKey = "decodedfile" + curFile + ":path";
/* 418 */     String filePath = data.getLocal(fileKey);
/* 419 */     if (filePath == null)
/*     */     {
/* 421 */       return;
/*     */     }
/* 423 */     data.removeLocal(fileKey);
/*     */ 
/* 425 */     data.putLocal(name + ":path", filePath);
/*     */ 
/* 427 */     ++curFile;
/* 428 */     data.setEnvironmentValue("SOAP:CurFileNumber", "" + curFile);
/*     */   }
/*     */ 
/*     */   public static void writeEncodedFile(OutputStream os, String fileName, long fileLength)
/*     */     throws IOException
/*     */   {
/* 434 */     FileInputStream fis = null;
/*     */     try
/*     */     {
/* 437 */       fis = new FileInputStream(fileName);
/*     */ 
/* 439 */       long bytesLeft = fileLength;
/* 440 */       while (bytesLeft > 0L)
/*     */       {
/* 442 */         int numToRead = 9000;
/* 443 */         if (bytesLeft < numToRead)
/*     */         {
/* 445 */           numToRead = (int)bytesLeft;
/*     */         }
/*     */ 
/* 448 */         byte[] b = readStream(fis, numToRead, false);
/* 449 */         String encodedStr = CommonDataConversion.uuencode(b, 0, b.length);
/* 450 */         os.write(encodedStr.getBytes());
/*     */ 
/* 452 */         bytesLeft -= numToRead;
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 457 */       if (fis != null)
/*     */       {
/* 459 */         fis.close();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 466 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78397 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapUtils
 * JD-Core Version:    0.5.4
 */