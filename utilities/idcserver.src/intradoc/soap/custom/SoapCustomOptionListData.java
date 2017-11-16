/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapCustomOptionListData extends SoapCustomElementData
/*     */ {
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*     */   {
/*  35 */     Vector subNodes = node.m_subNodes;
/*  36 */     if (subNodes == null)
/*     */     {
/*  38 */       return;
/*     */     }
/*     */ 
/*  41 */     Vector optionList = new IdcVector();
/*  42 */     data.addOptionList(this.m_idcName, optionList);
/*     */ 
/*  44 */     int numSubNodes = subNodes.size();
/*  45 */     for (int i = 0; i < numSubNodes; ++i)
/*     */     {
/*  47 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/*  48 */       String subNodeName = SoapUtils.getNodeName(subNode);
/*  49 */       if (!subNodeName.equals("option"))
/*     */         continue;
/*  51 */       optionList.addElement(subNode.m_value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */   {
/*  59 */     Vector optionList = data.getOptionList(this.m_idcName);
/*  60 */     if (optionList == null)
/*     */     {
/*  62 */       return;
/*     */     }
/*     */ 
/*  65 */     buffer.append("<idc:" + this.m_name + ">\r\n");
/*     */ 
/*  67 */     int numOptions = optionList.size();
/*  68 */     for (int i = 0; i < numOptions; ++i)
/*     */     {
/*  70 */       String option = (String)optionList.elementAt(i);
/*  71 */       option = SoapUtils.encodeXmlValue(option);
/*  72 */       buffer.append("<idc:option>" + option + "</idc:option>\r\n");
/*     */     }
/*     */ 
/*  75 */     buffer.append("</idc:" + this.m_name + ">\r\n");
/*     */   }
/*     */ 
/*     */   public void writeSchemaElement(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/*  83 */     SoapUtils.writeSchemaElement(this.m_name, "s0:IdcOptionList", 1, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public void writeSchemaComplexType(Writer writer, int numTabs)
/*     */     throws IOException
/*     */   {
/*  91 */     numTabs = SoapUtils.writeSchemaComplexType("IdcOptionList", true, writer, numTabs);
/*  92 */     SoapUtils.writeSchemaElement("option", "s:string", -1, writer, numTabs);
/*  93 */     SoapUtils.writeSchemaComplexType("IdcOptionList", false, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public String getDataTypes()
/*     */   {
/*  99 */     return "optionlist";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 104 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomOptionListData
 * JD-Core Version:    0.5.4
 */