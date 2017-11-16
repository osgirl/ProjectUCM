/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.soap.SoapServiceSerializer;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapCustomSerializer
/*     */   implements SoapServiceSerializer
/*     */ {
/*     */   public Vector m_wsdlList;
/*     */   public Hashtable m_wsdlMap;
/*  37 */   public static Hashtable m_globalServiceMap = null;
/*  38 */   public static Hashtable m_simpleDataClassMap = null;
/*  39 */   public static Hashtable m_complexDataClassMap = null;
/*  40 */   public static Vector m_elementDataTypeList = null;
/*     */ 
/*  42 */   public static Hashtable m_dataListMap = null;
/*     */   public String m_dataTableName;
/*     */   public String m_wsdlTableName;
/*     */   public String m_dataListTableName;
/*     */   public String m_lockObject;
/*     */   public boolean m_isLocked;
/*     */ 
/*     */   public SoapCustomSerializer()
/*     */   {
/*  34 */     this.m_wsdlList = null;
/*  35 */     this.m_wsdlMap = null;
/*     */ 
/*  44 */     this.m_dataTableName = "SoapCustomSerializerData";
/*  45 */     this.m_wsdlTableName = "SoapCustom:Wsdls";
/*  46 */     this.m_dataListTableName = "SoapCustom:DataLists";
/*     */ 
/*  48 */     this.m_lockObject = "lockObject";
/*  49 */     this.m_isLocked = false;
/*     */   }
/*     */ 
/*     */   public void init() throws DataException, ServiceException {
/*  53 */     initDataClasses();
/*     */ 
/*  55 */     initDataLists();
/*  56 */     initWsdls();
/*     */     try
/*     */     {
/*  60 */       generateWsdls();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  64 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*  68 */     AppObjectRepository.putObject("SoapCustomSerializer", this);
/*     */   }
/*     */ 
/*     */   public void initDataClasses()
/*     */     throws DataException, ServiceException
/*     */   {
/*  74 */     m_simpleDataClassMap = new Hashtable();
/*  75 */     m_complexDataClassMap = new Hashtable();
/*  76 */     Hashtable classMap = new Hashtable();
/*  77 */     m_elementDataTypeList = new IdcVector();
/*     */ 
/*  80 */     DataResultSet drset = SharedObjects.getTable(this.m_dataTableName);
/*  81 */     if (drset == null)
/*     */     {
/*  83 */       String errorMsg = LocaleUtils.encodeMessage("csSoapTableMissing", null, this.m_dataTableName);
/*     */ 
/*  85 */       throw new DataException(errorMsg);
/*     */     }
/*  87 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "name");
/*  88 */     int locationIndex = ResultSetUtils.getIndexMustExist(drset, "location");
/*  89 */     int isComplexIndex = ResultSetUtils.getIndexMustExist(drset, "isComplex");
/*     */ 
/*  91 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  93 */       String typeName = drset.getStringValue(nameIndex);
/*  94 */       String typeLocation = drset.getStringValue(locationIndex);
/*  95 */       boolean isComplex = StringUtils.convertToBool(drset.getStringValue(isComplexIndex), false);
/*     */ 
/*  98 */       if (classMap.get(typeName) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 103 */       if (isComplex)
/*     */       {
/* 106 */         SoapCustomComplexData soapData = (SoapCustomComplexData)ComponentClassFactory.createClassInstance(typeLocation, typeLocation, "!csSoapSerializerCreateError");
/*     */ 
/* 109 */         Class soapDataClass = soapData.getClass();
/*     */ 
/* 111 */         m_complexDataClassMap.put(typeName, soapDataClass);
/*     */       }
/*     */       else
/*     */       {
/* 116 */         SoapCustomElementData soapData = (SoapCustomElementData)ComponentClassFactory.createClassInstance(typeLocation, typeLocation, "!csSoapSerializerCreateError");
/*     */ 
/* 119 */         Class soapDataClass = soapData.getClass();
/*     */ 
/* 121 */         m_simpleDataClassMap.put(typeName, soapDataClass);
/*     */ 
/* 123 */         String dataTypes = soapData.getDataTypes();
/* 124 */         Vector typeList = StringUtils.parseArrayEx(dataTypes, ',', '^', true);
/* 125 */         int numTypes = typeList.size();
/* 126 */         for (int i = 0; i < numTypes; ++i)
/*     */         {
/* 128 */           String curType = (String)typeList.elementAt(i);
/* 129 */           m_elementDataTypeList.addElement(curType);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initDataLists()
/*     */     throws DataException, ServiceException
/*     */   {
/* 138 */     Vector dataListList = new IdcVector();
/* 139 */     m_dataListMap = new Hashtable();
/*     */ 
/* 142 */     DataResultSet drset = SoapCustomUtils.getDataLists();
/* 143 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 145 */       String errorMsg = LocaleUtils.encodeMessage("csSoapTableMissing", null, this.m_dataListTableName);
/*     */ 
/* 147 */       throw new DataException(errorMsg);
/*     */     }
/* 149 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "dataListName");
/* 150 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 152 */       String listName = drset.getStringValue(nameIndex);
/*     */ 
/* 154 */       if (m_dataListMap.get(listName) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 159 */       SoapCustomDataListData listData = new SoapCustomDataListData();
/* 160 */       DataResultSet elementSet = SoapCustomUtils.getDataListElements(listName);
/* 161 */       listData.init(listName, elementSet);
/*     */ 
/* 163 */       dataListList.addElement(listData);
/* 164 */       m_dataListMap.put(listName, listData);
/*     */     }
/*     */ 
/* 168 */     SoapCustomDataListData.m_chainedListMap = new Properties();
/*     */ 
/* 170 */     int numLists = dataListList.size();
/* 171 */     for (int i = 0; i < numLists; ++i)
/*     */     {
/* 173 */       SoapCustomDataListData listData = (SoapCustomDataListData)dataListList.elementAt(i);
/*     */ 
/* 175 */       listData.load();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initWsdls()
/*     */     throws DataException, ServiceException
/*     */   {
/* 182 */     this.m_wsdlList = new IdcVector();
/* 183 */     this.m_wsdlMap = new Hashtable();
/*     */ 
/* 185 */     m_globalServiceMap = new Hashtable();
/*     */ 
/* 188 */     DataResultSet drset = SoapCustomUtils.getWsdlList();
/* 189 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 191 */       String errorMsg = LocaleUtils.encodeMessage("csSoapTableMissing", null, this.m_dataListTableName);
/*     */ 
/* 193 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 196 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "wsdlName");
/* 197 */     int descIndex = ResultSetUtils.getIndexMustExist(drset, "wsdlDescription");
/*     */ 
/* 199 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 201 */       String wsdlName = drset.getStringValue(nameIndex);
/* 202 */       String wsdlDesc = drset.getStringValue(descIndex);
/*     */ 
/* 204 */       if (this.m_wsdlMap.get(wsdlName) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 209 */       SoapCustomWsdlData wsdlData = new SoapCustomWsdlData();
/* 210 */       wsdlData.init(wsdlName, wsdlDesc);
/*     */ 
/* 212 */       this.m_wsdlList.addElement(wsdlData);
/* 213 */       this.m_wsdlMap.put(wsdlName, wsdlData);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean canParseRequest(DataBinder data, String serviceName)
/*     */   {
/* 221 */     return m_globalServiceMap.get(serviceName) != null;
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node, String serviceName)
/*     */     throws IOException, DataException
/*     */   {
/* 229 */     if (this.m_isLocked)
/*     */     {
/* 231 */       throw new DataException("The WSDLs are being regenerated.");
/*     */     }
/*     */ 
/* 235 */     String auth = data.getLocal("Auth");
/* 236 */     if (auth == null)
/*     */     {
/* 238 */       data.putLocal("Auth", "Internet");
/*     */     }
/*     */ 
/* 241 */     SoapCustomServiceData serviceData = (SoapCustomServiceData)m_globalServiceMap.get(serviceName);
/*     */ 
/* 243 */     serviceData.parseRequest(data, node);
/* 244 */     data.setEnvironmentValue("SOAP:CustomSerializer", serviceName);
/*     */ 
/* 248 */     auth = data.getLocal("Auth");
/* 249 */     if ((auth == null) || (!auth.equals("")))
/*     */       return;
/* 251 */     data.removeLocal("Auth");
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */     throws IOException
/*     */   {
/* 258 */     boolean isError = false;
/* 259 */     SoapCustomServiceData serviceData = null;
/*     */ 
/* 261 */     String service = data.getEnvironmentValue("SOAP:CustomSerializer");
/* 262 */     if (service == null)
/*     */     {
/* 264 */       isError = true;
/*     */     }
/*     */     else
/*     */     {
/* 268 */       serviceData = (SoapCustomServiceData)m_globalServiceMap.get(service);
/* 269 */       if (serviceData == null)
/*     */       {
/* 271 */         isError = true;
/*     */       }
/*     */     }
/*     */ 
/* 275 */     if (isError)
/*     */     {
/* 278 */       throw new IOException("The Soap custom serializer is invalid.");
/*     */     }
/* 280 */     serviceData.sendResponse(data, buffer);
/*     */   }
/*     */ 
/*     */   public void generateWsdls() throws IOException, DataException, ServiceException
/*     */   {
/* 285 */     synchronized (this.m_lockObject)
/*     */     {
/* 287 */       this.m_isLocked = true;
/*     */ 
/* 292 */       String wsdlDir = LegacyDirectoryLocator.getWebGroupRootDirectory("secure") + "wsdl/custom/";
/*     */ 
/* 294 */       FileUtils.checkOrCreateDirectory(wsdlDir, 3);
/*     */ 
/* 297 */       int numWsdls = this.m_wsdlList.size();
/* 298 */       for (int i = 0; i < numWsdls; ++i)
/*     */       {
/* 300 */         SoapCustomWsdlData wsdlData = (SoapCustomWsdlData)this.m_wsdlList.elementAt(i);
/* 301 */         wsdlData.generateWsdl(wsdlDir);
/*     */       }
/*     */ 
/* 304 */       this.m_isLocked = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 310 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomSerializer
 * JD-Core Version:    0.5.4
 */