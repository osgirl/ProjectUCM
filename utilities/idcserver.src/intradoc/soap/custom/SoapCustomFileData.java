/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.soap.SoapMultipartSerializer;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapCustomFileData extends SoapCustomElementData
/*     */ {
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*     */   {
/*  34 */     Vector subNodes = node.m_subNodes;
/*  35 */     if (subNodes == null)
/*     */     {
/*  37 */       return;
/*     */     }
/*     */ 
/*  40 */     String fileName = null;
/*  41 */     boolean isMtomContent = false;
/*     */ 
/*  43 */     int numSubNodes = subNodes.size();
/*  44 */     for (int i = 0; i < numSubNodes; ++i)
/*     */     {
/*  46 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/*  47 */       String subNodeName = SoapUtils.getNodeName(subNode);
/*  48 */       if (subNodeName.equals("fileName"))
/*     */       {
/*  50 */         fileName = subNode.m_value;
/*  51 */         data.putLocal(this.m_idcName, fileName);
/*     */       } else {
/*  53 */         if (!subNodeName.equals("fileContent"))
/*     */           continue;
/*  55 */         isMtomContent = SoapMultipartSerializer.processMtomFileContent(data, this.m_idcName, subNode, fileName);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  60 */     if (fileName == null)
/*     */     {
/*  62 */       return;
/*     */     }
/*     */ 
/*  65 */     if (isMtomContent)
/*     */       return;
/*  67 */     SoapUtils.decodeFileContent(data, this.m_idcName);
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */   {
/*  74 */     String fileName = data.getEnvironmentValue("SOAP:downloadName");
/*  75 */     if (fileName == null)
/*     */     {
/*  77 */       fileName = "";
/*     */     }
/*  79 */     fileName = SoapUtils.encodeXmlValue(fileName);
/*     */ 
/*  81 */     buffer.append("<idc:" + this.m_name + ">\r\n");
/*  82 */     buffer.append("<idc:fileName>" + fileName + "</idc:fileName>\r\n");
/*  83 */     buffer.append("<idc:fileContent>");
/*     */ 
/*  86 */     String serializerName = data.getEnvironmentValue("SOAP:Serializer");
/*  87 */     if ((serializerName == null) || (serializerName.equals("xml")))
/*     */     {
/*  91 */       int length = buffer.length();
/*  92 */       data.setEnvironmentValue("SOAP:startFileContentIndex", "" + length);
/*     */     }
/*  94 */     else if (SoapMultipartSerializer.isMtomRequest(data))
/*     */     {
/*  96 */       SoapMultipartSerializer.sendMtomFileResponse(data, buffer, this.m_idcName, fileName);
/*     */     }
/*     */ 
/*  99 */     buffer.append("</idc:fileContent>\r\n");
/*     */ 
/* 101 */     buffer.append("</idc:" + this.m_name + ">\r\n");
/*     */   }
/*     */ 
/*     */   public void writeSchemaElement(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 108 */     SoapUtils.writeSchemaElement(this.m_name, "s0:IdcFile", 1, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public void writeSchemaComplexType(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/* 115 */     numTabs = SoapUtils.writeSchemaComplexType("IdcFile", true, writer, numTabs);
/*     */ 
/* 117 */     SoapUtils.writeSchemaElement("fileName", "s:string", 1, writer, numTabs);
/* 118 */     SoapUtils.writeSchemaElement("fileContent", "s:base64Binary", 1, writer, numTabs);
/*     */ 
/* 120 */     SoapUtils.writeSchemaComplexType("IdcFile", false, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public String getDataTypes()
/*     */   {
/* 126 */     return "file";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomFileData
 * JD-Core Version:    0.5.4
 */