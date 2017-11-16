/*     */ package intradoc.soap.generic;
/*     */ 
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import intradoc.soap.SoapXmlSerializer;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapGenericResultSetData
/*     */   implements SoapGenericData
/*     */ {
/*     */   protected SoapGenericSerializer m_serializer;
/*     */ 
/*     */   public SoapGenericResultSetData()
/*     */   {
/*  32 */     this.m_serializer = null;
/*     */   }
/*     */ 
/*     */   public void init(SoapGenericSerializer serializer) {
/*  36 */     this.m_serializer = serializer;
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*     */   {
/*  41 */     String rsetName = SoapUtils.getNodeProperty(node, "name");
/*  42 */     if (rsetName == null)
/*     */     {
/*  44 */       return;
/*     */     }
/*     */ 
/*  47 */     Vector fieldList = new IdcVector();
/*  48 */     Hashtable fieldMap = new Hashtable();
/*  49 */     Vector rowList = new IdcVector();
/*     */ 
/*  51 */     Vector rowNodes = node.m_subNodes;
/*  52 */     int numRowNodes = 0;
/*  53 */     if (rowNodes != null)
/*     */     {
/*  55 */       numRowNodes = rowNodes.size();
/*     */     }
/*     */ 
/*  59 */     for (int i = 0; i < numRowNodes; ++i)
/*     */     {
/*  61 */       PropertiesTreeNode rowNode = (PropertiesTreeNode)rowNodes.elementAt(i);
/*  62 */       String nodeName = SoapUtils.getNodeName(rowNode);
/*  63 */       if (!nodeName.equals("row"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  68 */       Properties rowProps = new Properties();
/*     */ 
/*  71 */       if (rowNode.m_properties != null)
/*     */       {
/*  73 */         Enumeration en = rowNode.m_properties.keys();
/*  74 */         while (en.hasMoreElements())
/*     */         {
/*  76 */           String key = (String)en.nextElement();
/*  77 */           String value = rowNode.m_properties.getProperty(key);
/*     */ 
/*  80 */           if (fieldMap.get(key) == null)
/*     */           {
/*  82 */             fieldList.addElement(key);
/*  83 */             fieldMap.put(key, key);
/*     */           }
/*     */ 
/*  86 */           rowProps.put(key, value);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*  91 */       Vector fieldNodes = rowNode.m_subNodes;
/*  92 */       int numFields = 0;
/*  93 */       if (fieldNodes != null)
/*     */       {
/*  95 */         numFields = fieldNodes.size();
/*     */       }
/*  97 */       for (int j = 0; j < numFields; ++j)
/*     */       {
/*  99 */         PropertiesTreeNode fieldNode = (PropertiesTreeNode)fieldNodes.elementAt(j);
/* 100 */         nodeName = SoapUtils.getNodeName(fieldNode);
/* 101 */         if (!nodeName.equals("field"))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 106 */         String fieldName = SoapUtils.getNodeProperty(fieldNode, "name");
/* 107 */         if (fieldName == null)
/*     */           continue;
/* 109 */         if (fieldMap.get(fieldName) == null)
/*     */         {
/* 111 */           fieldList.addElement(fieldName);
/* 112 */           fieldMap.put(fieldName, fieldName);
/*     */         }
/*     */ 
/* 115 */         rowProps.put(fieldName, fieldNode.m_value);
/*     */       }
/*     */ 
/* 119 */       rowList.addElement(rowProps);
/*     */     }
/*     */ 
/* 123 */     int numFields = fieldList.size();
/* 124 */     String[] fields = new String[numFields];
/* 125 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 127 */       fields[i] = ((String)fieldList.elementAt(i));
/*     */     }
/*     */ 
/* 130 */     DataResultSet drset = new DataResultSet(fields);
/* 131 */     int numRows = rowList.size();
/* 132 */     for (int i = 0; i < numRows; ++i)
/*     */     {
/* 134 */       Vector row = new IdcVector();
/* 135 */       row.setSize(numFields);
/* 136 */       Properties rowProps = (Properties)rowList.elementAt(i);
/*     */ 
/* 138 */       for (int j = 0; j < numFields; ++j)
/*     */       {
/* 140 */         String fieldName = fields[j];
/* 141 */         String fieldValue = rowProps.getProperty(fieldName);
/* 142 */         if (fieldValue == null)
/*     */         {
/* 144 */           fieldValue = "";
/*     */         }
/* 146 */         row.setElementAt(fieldValue, j);
/*     */       }
/*     */ 
/* 149 */       drset.addRow(row);
/*     */     }
/*     */ 
/* 152 */     data.addResultSet(rsetName, drset);
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */   {
/* 157 */     boolean isCollapseElements = SharedObjects.getEnvValueAsBoolean("IsSoapCollapseElements", false);
/*     */ 
/* 161 */     Enumeration en = data.getResultSetList();
/* 162 */     while (en.hasMoreElements())
/*     */     {
/* 164 */       String rsetName = (String)en.nextElement();
/* 165 */       ResultSet rset = data.getResultSet(rsetName);
/*     */ 
/* 168 */       String numRowsStr = "";
/* 169 */       if (rset instanceof DataResultSet)
/*     */       {
/* 171 */         DataResultSet drset = (DataResultSet)rset;
/* 172 */         int numRows = drset.getNumRows();
/*     */ 
/* 174 */         numRowsStr = "TotalRows=\"" + numRows + "\"";
/*     */       }
/*     */ 
/* 177 */       buffer.append("<idc:resultset name=\"" + rsetName + "\" " + numRowsStr + ">\r\n");
/*     */ 
/* 179 */       int numFields = rset.getNumFields();
/* 180 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 182 */         Properties fieldMap = new Properties();
/*     */ 
/* 184 */         StringBuffer attribBuffer = new StringBuffer();
/* 185 */         StringBuffer fieldBuffer = new StringBuffer();
/*     */ 
/* 187 */         for (int i = 0; i < numFields; ++i)
/*     */         {
/* 189 */           String fieldName = rset.getFieldName(i);
/* 190 */           if (fieldMap.get(fieldName) != null)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 195 */           fieldMap.put(fieldName, fieldName);
/*     */ 
/* 197 */           String fieldValue = rset.getStringValue(i);
/* 198 */           fieldValue = SoapUtils.encodeXmlValue(fieldValue);
/*     */ 
/* 200 */           if ((fieldValue.length() <= 0) && (isCollapseElements))
/*     */             continue;
/* 202 */           if (this.m_serializer.canAddLocalData(fieldName, false))
/*     */           {
/* 204 */             fieldBuffer.append("<idc:field name=\"");
/* 205 */             fieldBuffer.append(fieldName);
/* 206 */             fieldBuffer.append("\">");
/* 207 */             if (SoapXmlSerializer.m_useCDATA)
/*     */             {
/* 209 */               fieldBuffer.append("<![CDATA[");
/*     */             }
/* 211 */             fieldBuffer.append(fieldValue);
/* 212 */             if (SoapXmlSerializer.m_useCDATA)
/*     */             {
/* 214 */               fieldBuffer.append("]]>");
/*     */             }
/* 216 */             fieldBuffer.append("</idc:field>\r\n");
/*     */           } else {
/* 218 */             if (!this.m_serializer.canAddLocalData(fieldName, true))
/*     */               continue;
/* 220 */             attribBuffer.append(" " + fieldName + "=\"" + fieldValue + "\"");
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 226 */         buffer.append("<idc:row" + attribBuffer.toString() + ">\r\n");
/* 227 */         buffer.append(fieldBuffer.toString());
/* 228 */         buffer.append("</idc:row>\r\n");
/*     */       }
/*     */ 
/* 231 */       buffer.append("</idc:resultset>\r\n");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 237 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.generic.SoapGenericResultSetData
 * JD-Core Version:    0.5.4
 */