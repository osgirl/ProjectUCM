/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapCustomResultSetData extends SoapCustomComplexData
/*     */ {
/*     */   public void init(String name, DataResultSet elementSet)
/*     */   {
/*  35 */     super.init(name, elementSet);
/*     */ 
/*  37 */     this.m_maxOccurs = -1;
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node, SoapCustomComplexElementData soapElementData)
/*     */   {
/*  44 */     String rsetName = soapElementData.m_idcName;
/*     */ 
/*  47 */     DataResultSet drset = (DataResultSet)data.getResultSet(rsetName);
/*  48 */     if (drset == null)
/*     */     {
/*  50 */       drset = new DataResultSet();
/*  51 */       data.addResultSet(rsetName, drset);
/*     */     }
/*     */ 
/*  55 */     DataBinder rowData = new DataBinder();
/*     */ 
/*  58 */     Vector rowNodes = node.m_subNodes;
/*  59 */     if (rowNodes == null)
/*     */     {
/*  61 */       return;
/*     */     }
/*  63 */     int numRowNodes = rowNodes.size();
/*     */ 
/*  65 */     for (int i = 0; i < numRowNodes; ++i)
/*     */     {
/*  67 */       PropertiesTreeNode rowNode = (PropertiesTreeNode)rowNodes.elementAt(i);
/*  68 */       String rowNodeName = SoapUtils.getNodeName(rowNode);
/*  69 */       SoapCustomElementData soapData = (SoapCustomElementData)this.m_customDataMap.get(rowNodeName);
/*     */ 
/*  71 */       if (soapData == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  77 */       soapData.parseRequest(rowData, rowNode);
/*     */     }
/*     */ 
/*  81 */     Vector appendList = new IdcVector();
/*     */ 
/*  83 */     Enumeration en = rowData.getLocalData().keys();
/*  84 */     while (en.hasMoreElements())
/*     */     {
/*  86 */       String fieldName = (String)en.nextElement();
/*  87 */       FieldInfo fi = new FieldInfo();
/*  88 */       if (!drset.getFieldInfo(fieldName, fi))
/*     */       {
/*  90 */         fi.m_name = fieldName;
/*  91 */         fi.m_type = 6;
/*  92 */         appendList.addElement(fi);
/*     */       }
/*     */     }
/*  95 */     drset.mergeFieldsWithFlags(appendList, 0);
/*     */ 
/*  98 */     Vector row = drset.createEmptyRow();
/*     */ 
/* 100 */     en = rowData.getLocalData().keys();
/* 101 */     while (en.hasMoreElements())
/*     */     {
/* 103 */       String fieldName = (String)en.nextElement();
/* 104 */       String fieldValue = rowData.getLocal(fieldName);
/*     */ 
/* 106 */       FieldInfo fi = new FieldInfo();
/* 107 */       if (drset.getFieldInfo(fieldName, fi))
/*     */       {
/* 109 */         row.setElementAt(fieldValue, fi.m_index);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 116 */     if (row.size() <= 0)
/*     */       return;
/* 118 */     drset.addRow(row);
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer, SoapCustomComplexElementData soapElementData)
/*     */     throws IOException
/*     */   {
/* 126 */     String rsetName = soapElementData.m_idcName;
/* 127 */     String tagName = soapElementData.m_name;
/*     */ 
/* 129 */     ResultSet rset = data.getResultSet(rsetName);
/* 130 */     if (rset == null)
/*     */     {
/* 132 */       return;
/*     */     }
/*     */ 
/* 135 */     int numData = this.m_customDataList.size();
/* 136 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 138 */       buffer.append("<idc:" + tagName + ">\r\n");
/*     */ 
/* 140 */       DataBinder rowData = new DataBinder();
/*     */ 
/* 142 */       int numFields = rset.getNumFields();
/* 143 */       for (int j = 0; j < numFields; ++j)
/*     */       {
/* 145 */         String fieldName = rset.getFieldName(j);
/* 146 */         String fieldValue = rset.getStringValue(j);
/*     */ 
/* 148 */         if (fieldValue == null)
/*     */         {
/* 150 */           fieldValue = "";
/*     */         }
/*     */ 
/* 153 */         rowData.putLocal(fieldName, fieldValue);
/* 154 */         rowData.putLocal("field" + j, fieldName);
/*     */       }
/*     */ 
/* 157 */       rowData.putLocal("numFields", "" + numFields);
/*     */ 
/* 159 */       for (int i = 0; i < numData; ++i)
/*     */       {
/* 161 */         SoapCustomElementData soapData = (SoapCustomElementData)this.m_customDataList.elementAt(i);
/*     */ 
/* 163 */         soapData.sendResponse(rowData, buffer);
/*     */       }
/*     */ 
/* 166 */       buffer.append("</idc:" + tagName + ">\r\n");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 172 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomResultSetData
 * JD-Core Version:    0.5.4
 */