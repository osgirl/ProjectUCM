/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapCustomPropertyListData extends SoapCustomElementData
/*     */ {
/*     */   public static final int USER_METADATA_LIST = 0;
/*     */   public static final int DOC_METADATA_LIST = 1;
/*     */   public static final int DEFAULT_LIST = 2;
/*     */   public int m_listType;
/*     */   public String m_metaTableName;
/*     */   public int m_metaNameIndex;
/*     */ 
/*     */   public SoapCustomPropertyListData()
/*     */   {
/*  36 */     this.m_listType = -1;
/*  37 */     this.m_metaTableName = null;
/*  38 */     this.m_metaNameIndex = -1;
/*     */   }
/*     */ 
/*     */   public void init(String name, String type, String subType, String idcName)
/*     */   {
/*  43 */     super.init(name, type, subType, idcName);
/*     */ 
/*  45 */     String nameField = null;
/*  46 */     if ((subType != null) && (subType.equals("CustomUserMeta")))
/*     */     {
/*  48 */       this.m_listType = 0;
/*  49 */       this.m_metaTableName = "UserMetaDefinition";
/*  50 */       nameField = "umdName";
/*     */     }
/*  52 */     else if ((subType != null) && (subType.equals("CustomDocMeta")))
/*     */     {
/*  54 */       this.m_listType = 1;
/*  55 */       this.m_metaTableName = "DocMetaDefinition";
/*  56 */       nameField = "dName";
/*     */     }
/*     */     else
/*     */     {
/*  60 */       this.m_listType = 2;
/*     */     }
/*     */ 
/*  63 */     if (nameField == null)
/*     */       return;
/*  65 */     DataResultSet metaSet = SharedObjects.getTable(this.m_metaTableName);
/*     */     try
/*     */     {
/*  68 */       this.m_metaNameIndex = ResultSetUtils.getIndexMustExist(metaSet, nameField);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  72 */       if (!SystemUtils.m_verbose)
/*     */         return;
/*  74 */       Report.debug("system", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*     */   {
/*  84 */     DataResultSet metaSet = null;
/*  85 */     if (this.m_metaTableName != null)
/*     */     {
/*  87 */       metaSet = SharedObjects.getTable(this.m_metaTableName);
/*     */     }
/*     */ 
/*  90 */     Properties props = getRequestProperties(node);
/*     */ 
/*  93 */     Enumeration en = props.keys();
/*  94 */     while (en.hasMoreElements())
/*     */     {
/*  96 */       String name = (String)en.nextElement();
/*  97 */       String value = props.getProperty(name);
/*     */ 
/* 100 */       if (metaSet != null)
/*     */       {
/* 102 */         Vector metaRow = metaSet.findRow(this.m_metaNameIndex, name);
/* 103 */         if (metaRow == null) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 109 */       data.putLocal(name, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getRequestProperties(PropertiesTreeNode node)
/*     */   {
/* 115 */     Properties props = new Properties();
/*     */ 
/* 117 */     Vector subNodes = node.m_subNodes;
/* 118 */     if (subNodes == null)
/*     */     {
/* 120 */       return props;
/*     */     }
/*     */ 
/* 124 */     int numSubNodes = subNodes.size();
/* 125 */     for (int i = 0; i < numSubNodes; ++i)
/*     */     {
/* 127 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/* 128 */       String subNodeName = SoapUtils.getNodeName(subNode);
/* 129 */       if (!subNodeName.equals("property"))
/*     */         continue;
/* 131 */       parseRequestProperty(subNode.m_subNodes, props);
/*     */     }
/*     */ 
/* 135 */     return props;
/*     */   }
/*     */ 
/*     */   public void parseRequestProperty(Vector nodes, Properties props)
/*     */   {
/* 140 */     if (nodes == null)
/*     */     {
/* 142 */       return;
/*     */     }
/*     */ 
/* 145 */     String propName = null;
/* 146 */     String propValue = "";
/*     */ 
/* 148 */     int numNodes = nodes.size();
/* 149 */     for (int i = 0; i < numNodes; ++i)
/*     */     {
/* 151 */       PropertiesTreeNode node = (PropertiesTreeNode)nodes.elementAt(i);
/* 152 */       String nodeName = SoapUtils.getNodeName(node);
/* 153 */       if (nodeName.equals("name"))
/*     */       {
/* 155 */         propName = node.m_value;
/*     */       } else {
/* 157 */         if (!nodeName.equals("value"))
/*     */           continue;
/* 159 */         propValue = node.m_value;
/*     */       }
/*     */     }
/*     */ 
/* 163 */     if (propName == null)
/*     */       return;
/* 165 */     props.put(propName, propValue);
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */   {
/* 172 */     boolean isCollapseElements = SharedObjects.getEnvValueAsBoolean("IsSoapCollapseElements", false);
/*     */ 
/* 176 */     DataResultSet metaSet = null;
/* 177 */     if (this.m_metaTableName != null)
/*     */     {
/* 179 */       metaSet = SharedObjects.getTable(this.m_metaTableName);
/*     */     }
/*     */ 
/* 182 */     buffer.append("<idc:" + this.m_name + ">\r\n");
/*     */ 
/* 184 */     if (metaSet != null)
/*     */     {
/* 186 */       for (metaSet.first(); ; metaSet.next()) { if (!metaSet.isRowPresent())
/*     */           break label373;
/* 188 */         String fieldName = metaSet.getStringValue(this.m_metaNameIndex);
/* 189 */         String fieldValue = data.getLocal(fieldName);
/* 190 */         if (fieldValue == null)
/*     */         {
/* 192 */           fieldValue = "";
/*     */         }
/*     */         else
/*     */         {
/* 196 */           fieldValue = SoapUtils.encodeXmlValue(fieldValue);
/*     */         }
/*     */ 
/* 199 */         if ((fieldValue.length() <= 0) && (isCollapseElements))
/*     */           continue;
/* 201 */         buffer.append("<idc:property>\r\n");
/* 202 */         buffer.append("<idc:name>" + fieldName + "</idc:name>\r\n");
/* 203 */         buffer.append("<idc:value>" + fieldValue + "</idc:value>\r\n");
/* 204 */         buffer.append("</idc:property>\r\n"); }
/*     */ 
/*     */ 
/*     */     }
/*     */ 
/* 210 */     String tempStr = data.getLocal("numFields");
/* 211 */     int numFields = NumberUtils.parseInteger(tempStr, 0);
/* 212 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 214 */       String fieldKey = "field" + i;
/* 215 */       String fieldName = data.getLocal(fieldKey);
/* 216 */       String fieldValue = data.getLocal(fieldName);
/*     */ 
/* 218 */       fieldValue = SoapUtils.encodeXmlValue(fieldValue);
/*     */ 
/* 220 */       if ((fieldValue.length() <= 0) && (isCollapseElements))
/*     */         continue;
/* 222 */       buffer.append("<idc:property>\r\n");
/* 223 */       buffer.append("<idc:name>" + fieldName + "</idc:name>\r\n");
/* 224 */       buffer.append("<idc:value>" + fieldValue + "</idc:value>\r\n");
/* 225 */       buffer.append("</idc:property>\r\n");
/*     */     }
/*     */ 
/* 230 */     label373: buffer.append("</idc:" + this.m_name + ">\r\n");
/*     */   }
/*     */ 
/*     */   public void writeSchemaElement(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 237 */     SoapUtils.writeSchemaElement(this.m_name, "s0:IdcPropertyList", 1, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public void writeSchemaComplexType(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 243 */     numTabs = SoapUtils.writeSchemaComplexType("IdcPropertyList", true, writer, numTabs);
/* 244 */     SoapUtils.writeSchemaElement("property", "s0:IdcProperty", -1, writer, numTabs);
/* 245 */     numTabs = SoapUtils.writeSchemaComplexType("IdcPropertyList", false, writer, numTabs);
/*     */ 
/* 247 */     numTabs = SoapUtils.writeSchemaComplexType("IdcProperty", true, writer, numTabs);
/* 248 */     SoapUtils.writeSchemaElement("name", "s:string", 1, writer, numTabs);
/* 249 */     SoapUtils.writeSchemaElement("value", "s:string", 1, writer, numTabs);
/* 250 */     SoapUtils.writeSchemaComplexType("IdcProperty", false, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public String getDataTypes()
/*     */   {
/* 256 */     return "propertylist, propertylist:CustomDocMeta, propertylist:CustomUserMeta";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 261 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomPropertyListData
 * JD-Core Version:    0.5.4
 */