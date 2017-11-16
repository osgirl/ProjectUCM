/*     */ package intradoc.soap.generic;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.soap.SoapServiceSerializer;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapGenericSerializer
/*     */   implements SoapServiceSerializer
/*     */ {
/*     */   protected String m_dataTableName;
/*     */   protected String m_dataResourceName;
/*     */   protected String m_fileName;
/*     */   public static final String GENERIC_NAMESPACE = "http://www.stellent.com/IdcService/";
/*     */   public static final int ATTRIBUTE_MODE = 0;
/*     */   public static final int FIELD_MODE = 1;
/*     */   public static final int MIXED_MODE = 2;
/*     */   protected Properties m_attributeFieldProps;
/*     */   protected Properties m_ignoreFieldProps;
/*     */   protected int m_fieldTagMode;
/*     */   protected Vector m_dataList;
/*     */   protected Hashtable m_dataMap;
/*     */ 
/*     */   public SoapGenericSerializer()
/*     */   {
/*  35 */     this.m_dataTableName = "SoapGenericSerializerData";
/*  36 */     this.m_dataResourceName = "SoapGenericFieldProperties";
/*  37 */     this.m_fileName = "generic.hda";
/*     */ 
/*  47 */     this.m_attributeFieldProps = null;
/*  48 */     this.m_ignoreFieldProps = null;
/*  49 */     this.m_fieldTagMode = -1;
/*     */ 
/*  52 */     this.m_dataList = null;
/*  53 */     this.m_dataMap = null;
/*     */   }
/*     */ 
/*     */   public void init() throws DataException, ServiceException {
/*  57 */     cacheDataObjects();
/*  58 */     cacheFieldProperties();
/*     */   }
/*     */ 
/*     */   protected void cacheDataObjects()
/*     */     throws DataException, ServiceException
/*     */   {
/*  64 */     DataResultSet drset = SharedObjects.getTable(this.m_dataTableName);
/*  65 */     if (drset == null)
/*     */     {
/*  67 */       String errorMsg = LocaleUtils.encodeMessage("csSoapTableMissing", null, this.m_dataTableName);
/*     */ 
/*  69 */       throw new DataException(errorMsg);
/*     */     }
/*  71 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "name");
/*  72 */     int locationIndex = ResultSetUtils.getIndexMustExist(drset, "location");
/*     */ 
/*  75 */     this.m_dataList = new IdcVector();
/*  76 */     this.m_dataMap = new Hashtable();
/*     */ 
/*  78 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  80 */       String name = drset.getStringValue(nameIndex);
/*  81 */       String location = drset.getStringValue(locationIndex);
/*     */ 
/*  83 */       if (this.m_dataMap.get(name) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  88 */       SoapGenericData soapData = (SoapGenericData)ComponentClassFactory.createClassInstance(location, location, "!csSoapSerializerCreateError");
/*     */ 
/*  91 */       soapData.init(this);
/*     */ 
/*  93 */       this.m_dataList.addElement(soapData);
/*  94 */       this.m_dataMap.put(name, soapData);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void cacheFieldProperties()
/*     */     throws DataException, ServiceException
/*     */   {
/* 101 */     String soapDir = LegacyDirectoryLocator.getAppDataDirectory() + "soap/generic/";
/* 102 */     FileUtils.checkOrCreateDirectory(soapDir, 3);
/*     */ 
/* 104 */     DataBinder data = new DataBinder();
/*     */ 
/* 106 */     boolean isSaveFile = false;
/*     */ 
/* 108 */     String filePath = soapDir + this.m_fileName;
/* 109 */     File file = FileUtilsCfgBuilder.getCfgFile(filePath, "Soap", false);
/* 110 */     if (file.exists())
/*     */     {
/* 113 */       ResourceUtils.serializeDataBinder(soapDir, this.m_fileName, data, false, false);
/* 114 */       if (data.getResultSet("SoapGenericFieldDefinitions") != null)
/*     */       {
/* 116 */         data = new DataBinder();
/* 117 */         isSaveFile = true;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 122 */       isSaveFile = true;
/*     */     }
/*     */ 
/* 125 */     if (isSaveFile)
/*     */     {
/* 128 */       if (SharedObjects.getHtmlResource(this.m_dataResourceName) == null)
/*     */       {
/* 130 */         String errorMsg = LocaleUtils.encodeMessage("csSoapResourceMissing", null, this.m_dataResourceName);
/*     */ 
/* 132 */         throw new DataException(errorMsg);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 137 */         PageMerger merger = new PageMerger(data, null);
/* 138 */         merger.evaluateResourceInclude(this.m_dataResourceName);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 142 */         throw new DataException(e.getMessage());
/*     */       }
/*     */ 
/* 145 */       ResourceUtils.serializeDataBinder(soapDir, this.m_fileName, data, true, false);
/*     */     }
/*     */     else
/*     */     {
/* 149 */       ResourceUtils.serializeDataBinder(soapDir, this.m_fileName, data, false, false);
/*     */     }
/*     */ 
/* 153 */     this.m_attributeFieldProps = new Properties();
/*     */ 
/* 155 */     String fieldTagModeStr = SharedObjects.getEnvironmentValue("SoapGenericFieldTagMode");
/* 156 */     if (fieldTagModeStr != null)
/*     */     {
/* 158 */       if (fieldTagModeStr.equals("field"))
/*     */       {
/* 160 */         this.m_fieldTagMode = 1;
/*     */       }
/* 162 */       else if (fieldTagModeStr.equals("attribute"))
/*     */       {
/* 164 */         this.m_fieldTagMode = 0;
/*     */       }
/*     */       else
/*     */       {
/* 168 */         this.m_fieldTagMode = 2;
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 173 */       this.m_fieldTagMode = 2;
/*     */     }
/*     */ 
/* 176 */     if (this.m_fieldTagMode == 2)
/*     */     {
/* 178 */       Enumeration en = data.m_localData.keys();
/* 179 */       while (en.hasMoreElements())
/*     */       {
/* 181 */         String fieldName = (String)en.nextElement();
/* 182 */         String fieldValue = data.getLocal(fieldName);
/*     */ 
/* 184 */         if (fieldValue.equalsIgnoreCase("attribute"))
/*     */         {
/* 186 */           this.m_attributeFieldProps.put(fieldName, "1");
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 192 */     this.m_ignoreFieldProps = new Properties();
/*     */ 
/* 194 */     String ignoreFieldListStr = data.getLocal("ignoreFieldList");
/* 195 */     Vector ignoreFieldList = StringUtils.parseArray(ignoreFieldListStr, ',', '^');
/* 196 */     int numFields = ignoreFieldList.size();
/* 197 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 199 */       String fieldName = (String)ignoreFieldList.elementAt(i);
/* 200 */       fieldName = fieldName.trim();
/*     */ 
/* 202 */       this.m_ignoreFieldProps.put(fieldName, "1");
/*     */     }
/* 204 */     this.m_ignoreFieldProps.put("IdcService", "1");
/* 205 */     this.m_ignoreFieldProps.put("IsJava", "1");
/* 206 */     this.m_ignoreFieldProps.put("blDateFormat", "1");
/* 207 */     this.m_ignoreFieldProps.put("IsSoap", "1");
/*     */   }
/*     */ 
/*     */   public boolean canParseRequest(DataBinder data, String serviceName)
/*     */   {
/* 214 */     return serviceName.equals("service");
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node, String serviceName)
/*     */     throws IOException, DataException
/*     */   {
/* 223 */     String idcService = SoapUtils.getNodeProperty(node, "IdcService");
/* 224 */     if (!SoapUtils.isValidIdcService(idcService))
/*     */     {
/* 226 */       String errorMsg = LocaleUtils.encodeMessage("csSoapGenericInvalidService", null, idcService);
/*     */ 
/* 228 */       throw new DataException(errorMsg);
/*     */     }
/* 230 */     data.putLocal("IdcService", idcService);
/*     */ 
/* 232 */     data.m_environment.put("SOAP:DefaultDownloadType", "multipart");
/*     */ 
/* 234 */     Vector subNodes = node.m_subNodes;
/* 235 */     if (subNodes == null)
/*     */     {
/* 237 */       return;
/*     */     }
/*     */ 
/* 240 */     int numSubNodes = subNodes.size();
/* 241 */     for (int i = 0; i < numSubNodes; ++i)
/*     */     {
/* 243 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/* 244 */       String nodeName = SoapUtils.getNodeName(subNode);
/* 245 */       if ((!nodeName.equalsIgnoreCase("document")) && (!nodeName.equalsIgnoreCase("user"))) {
/*     */         continue;
/*     */       }
/* 248 */       parseDocumentNode(data, subNode);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseDocumentNode(DataBinder data, PropertiesTreeNode node)
/*     */     throws DataException, IOException
/*     */   {
/* 257 */     if (node.m_properties != null)
/*     */     {
/* 259 */       Enumeration en = node.m_properties.keys();
/* 260 */       while (en.hasMoreElements())
/*     */       {
/* 262 */         String key = (String)en.nextElement();
/* 263 */         String value = node.m_properties.getProperty(key);
/*     */ 
/* 265 */         data.putLocal(key, value);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 270 */     Vector subNodes = node.m_subNodes;
/* 271 */     if (node.m_subNodes == null)
/*     */     {
/* 273 */       return;
/*     */     }
/* 275 */     int numSubNodes = subNodes.size();
/* 276 */     for (int i = 0; i < numSubNodes; ++i)
/*     */     {
/* 278 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/* 279 */       String nodeName = SoapUtils.getNodeName(subNode);
/*     */ 
/* 281 */       SoapGenericData soapData = (SoapGenericData)this.m_dataMap.get(nodeName);
/* 282 */       if (soapData == null)
/*     */         continue;
/* 284 */       soapData.parseRequest(data, subNode);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */     throws IOException
/*     */   {
/* 292 */     String idcService = data.getAllowMissing("IdcService");
/* 293 */     buffer.append("<idc:service xmlns:idc=\"http://www.stellent.com/IdcService/\" IdcService=\"" + idcService + "\">\r\n");
/*     */ 
/* 297 */     buffer.append("<idc:document");
/*     */ 
/* 300 */     Enumeration en = data.m_localData.keys();
/* 301 */     while (en.hasMoreElements())
/*     */     {
/* 304 */       String key = (String)en.nextElement();
/* 305 */       if (canAddLocalData(key, true))
/*     */       {
/* 307 */         String value = data.getLocal(key);
/* 308 */         value = SoapUtils.encodeXmlValue(value);
/*     */ 
/* 310 */         buffer.append(" " + key + "=\"" + value + "\"");
/*     */       }
/*     */     }
/*     */ 
/* 314 */     buffer.append(">\r\n");
/*     */ 
/* 317 */     int numData = this.m_dataList.size();
/* 318 */     for (int i = 0; i < numData; ++i)
/*     */     {
/* 320 */       SoapGenericData soapData = (SoapGenericData)this.m_dataList.elementAt(i);
/* 321 */       soapData.sendResponse(data, buffer);
/*     */     }
/*     */ 
/* 325 */     buffer.append("</idc:document>\r\n");
/* 326 */     buffer.append("</idc:service>\r\n");
/*     */   }
/*     */ 
/*     */   protected boolean canAddLocalData(String fieldName, boolean isAttribute)
/*     */   {
/* 331 */     if (this.m_ignoreFieldProps.getProperty(fieldName) != null)
/*     */     {
/* 333 */       return false;
/*     */     }
/*     */ 
/* 336 */     if (this.m_fieldTagMode != 2)
/*     */     {
/* 338 */       if ((isAttribute) && (this.m_fieldTagMode == 0))
/*     */       {
/* 340 */         return true;
/*     */       }
/*     */ 
/* 344 */       return (!isAttribute) && (this.m_fieldTagMode == 1);
/*     */     }
/*     */ 
/* 354 */     return isAttribute == (this.m_attributeFieldProps.getProperty(fieldName) != null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 362 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.generic.SoapGenericSerializer
 * JD-Core Version:    0.5.4
 */