/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public abstract class SoapCustomComplexData
/*     */ {
/*     */   public String m_name;
/*     */   public DataResultSet m_elementSet;
/*     */   public Vector m_customDataList;
/*     */   public Hashtable m_customDataMap;
/*     */   public int m_maxOccurs;
/*     */ 
/*     */   public SoapCustomComplexData()
/*     */   {
/*  32 */     this.m_name = null;
/*  33 */     this.m_elementSet = null;
/*     */ 
/*  35 */     this.m_customDataList = null;
/*  36 */     this.m_customDataMap = null;
/*     */ 
/*  38 */     this.m_maxOccurs = 1;
/*     */   }
/*     */ 
/*     */   public void init(String name, DataResultSet drset) {
/*  42 */     this.m_name = name;
/*  43 */     this.m_elementSet = drset;
/*     */ 
/*  46 */     this.m_customDataList = new IdcVector();
/*  47 */     this.m_customDataMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void load()
/*     */     throws DataException, ServiceException
/*     */   {
/*  53 */     if (this.m_elementSet == null)
/*     */     {
/*  55 */       return;
/*     */     }
/*  57 */     int nameIndex = ResultSetUtils.getIndexMustExist(this.m_elementSet, "dataName");
/*  58 */     int typeIndex = ResultSetUtils.getIndexMustExist(this.m_elementSet, "dataType");
/*  59 */     int idcNameIndex = ResultSetUtils.getIndexMustExist(this.m_elementSet, "dataIdcName");
/*     */ 
/*  61 */     int isEnabledIndex = -1;
/*  62 */     FieldInfo fi = new FieldInfo();
/*  63 */     if (this.m_elementSet.getFieldInfo("isEnabled", fi))
/*     */     {
/*  65 */       isEnabledIndex = fi.m_index;
/*     */     }
/*     */ 
/*  68 */     for (this.m_elementSet.first(); this.m_elementSet.isRowPresent(); this.m_elementSet.next())
/*     */     {
/*  70 */       if (isEnabledIndex >= 0)
/*     */       {
/*  72 */         boolean isEnabled = StringUtils.convertToBool(this.m_elementSet.getStringValue(isEnabledIndex), false);
/*     */ 
/*  74 */         if (!isEnabled) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*  80 */       String dataName = this.m_elementSet.getStringValue(nameIndex);
/*  81 */       String dataType = this.m_elementSet.getStringValue(typeIndex);
/*  82 */       String dataIdcName = this.m_elementSet.getStringValue(idcNameIndex);
/*     */ 
/*  84 */       if (dataType.startsWith("c:"))
/*     */       {
/*  86 */         dataType = dataType.substring(2);
/*  87 */         addComplexElement(dataName, dataType, dataIdcName);
/*     */       }
/*  89 */       else if (dataType.startsWith("d:"))
/*     */       {
/*  91 */         dataType = dataType.substring(2);
/*  92 */         addDataListElements(dataType);
/*     */       }
/*     */       else
/*     */       {
/*  96 */         addSimpleElement(dataName, dataType, dataIdcName);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addSimpleElement(String dataName, String dataType, String dataIdcName)
/*     */     throws ServiceException
/*     */   {
/* 105 */     if (this.m_customDataMap.get(dataName) != null)
/*     */     {
/* 107 */       return;
/*     */     }
/*     */ 
/* 111 */     String dataSubType = null;
/* 112 */     int index = dataType.indexOf(":");
/* 113 */     if (index > 0)
/*     */     {
/* 115 */       dataSubType = dataType.substring(index + 1);
/* 116 */       dataType = dataType.substring(0, index);
/*     */     }
/* 118 */     Class dataClass = (Class)SoapCustomSerializer.m_simpleDataClassMap.get(dataType);
/* 119 */     if (dataClass == null)
/*     */     {
/* 121 */       return;
/*     */     }
/*     */ 
/* 125 */     SoapCustomElementData soapCustomData = null;
/*     */     try
/*     */     {
/* 128 */       soapCustomData = (SoapCustomElementData)dataClass.newInstance();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 132 */       throw new ServiceException(e);
/*     */     }
/* 134 */     soapCustomData.init(dataName, dataType, dataSubType, dataIdcName);
/*     */ 
/* 137 */     this.m_customDataList.addElement(soapCustomData);
/* 138 */     this.m_customDataMap.put(dataName, soapCustomData);
/*     */   }
/*     */ 
/*     */   public void addComplexElement(String dataName, String dataType, String dataIdcName)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void addDataListElements(String dataListName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 150 */     SoapCustomDataListData soapListData = (SoapCustomDataListData)SoapCustomSerializer.m_dataListMap.get(dataListName);
/*     */ 
/* 152 */     if (soapListData == null)
/*     */     {
/* 154 */       return;
/*     */     }
/*     */ 
/* 157 */     if (!soapListData.m_isLoaded)
/*     */     {
/* 159 */       soapListData.load();
/*     */     }
/*     */ 
/* 163 */     int numElements = soapListData.m_customDataList.size();
/* 164 */     for (int i = 0; i < numElements; ++i)
/*     */     {
/* 166 */       SoapCustomElementData soapData = (SoapCustomElementData)soapListData.m_customDataList.elementAt(i);
/*     */ 
/* 169 */       if (this.m_customDataMap.get(soapData.m_name) != null)
/*     */         continue;
/* 171 */       this.m_customDataList.addElement(soapData);
/* 172 */       this.m_customDataMap.put(soapData.m_name, soapData);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node, SoapCustomComplexElementData soapElementData)
/*     */   {
/* 180 */     Vector subNodes = node.m_subNodes;
/* 181 */     if (subNodes == null)
/*     */     {
/* 183 */       return;
/*     */     }
/* 185 */     int numSubNodes = subNodes.size();
/*     */ 
/* 187 */     for (int i = 0; i < numSubNodes; ++i)
/*     */     {
/* 189 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/* 190 */       String subNodeName = SoapUtils.getNodeName(subNode);
/*     */ 
/* 193 */       SoapCustomElementData soapData = (SoapCustomElementData)this.m_customDataMap.get(subNodeName);
/*     */ 
/* 195 */       if (soapData == null)
/*     */         continue;
/* 197 */       soapData.parseRequest(data, subNode);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer, SoapCustomComplexElementData soapElementData)
/*     */     throws IOException
/*     */   {
/* 205 */     int numData = this.m_customDataList.size();
/* 206 */     for (int i = 0; i < numData; ++i)
/*     */     {
/* 208 */       SoapCustomElementData soapData = (SoapCustomElementData)this.m_customDataList.elementAt(i);
/*     */ 
/* 210 */       soapData.sendResponse(data, buffer);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void writeSchemaElement(Writer writer, int numTabs) throws IOException
/*     */   {
/* 216 */     int numElements = this.m_customDataList.size();
/* 217 */     for (int i = 0; i < numElements; ++i)
/*     */     {
/* 219 */       SoapCustomElementData soapData = (SoapCustomElementData)this.m_customDataList.elementAt(i);
/*     */ 
/* 221 */       soapData.writeSchemaElement(writer, numTabs);
/*     */ 
/* 224 */       String className = soapData.getClass().getName();
/* 225 */       if (SoapCustomWsdlData.m_simpleDataTypeMap.get(className) != null)
/*     */         continue;
/* 227 */       SoapCustomWsdlData.m_simpleDataTypeList.addElement(soapData);
/* 228 */       SoapCustomWsdlData.m_simpleDataTypeMap.put(className, soapData);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void writeSchemaComplexType(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 236 */     numTabs = SoapUtils.writeSchemaComplexType(this.m_name, true, writer, numTabs);
/* 237 */     writeSchemaElement(writer, numTabs);
/* 238 */     SoapUtils.writeSchemaComplexType(null, false, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 243 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomComplexData
 * JD-Core Version:    0.5.4
 */