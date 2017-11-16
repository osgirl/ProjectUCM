/*    */ package intradoc.soap.generic;
/*    */ 
/*    */ import intradoc.common.PropertiesTreeNode;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.soap.SoapUtils;
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Enumeration;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class SoapGenericOptionListData
/*    */   implements SoapGenericData
/*    */ {
/*    */   public void init(SoapGenericSerializer serializer)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*    */   {
/* 38 */     String optListName = SoapUtils.getNodeProperty(node, "name");
/* 39 */     if (optListName == null)
/*    */     {
/* 41 */       return;
/*    */     }
/* 43 */     Vector optionList = new IdcVector();
/* 44 */     data.addOptionList(optListName, optionList);
/*    */ 
/* 46 */     Vector optNodes = node.m_subNodes;
/* 47 */     if (optNodes == null)
/*    */     {
/* 49 */       return;
/*    */     }
/*    */ 
/* 52 */     int numOptions = optNodes.size();
/* 53 */     for (int i = 0; i < numOptions; ++i)
/*    */     {
/* 55 */       PropertiesTreeNode optNode = (PropertiesTreeNode)optNodes.elementAt(i);
/* 56 */       String nodeName = SoapUtils.getNodeName(optNode);
/* 57 */       if (!nodeName.equals("option"))
/*    */         continue;
/* 59 */       optionList.addElement(optNode.m_value);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*    */   {
/* 67 */     Enumeration en = data.m_optionLists.keys();
/* 68 */     while (en.hasMoreElements())
/*    */     {
/* 70 */       String optName = (String)en.nextElement();
/* 71 */       Vector optList = data.getOptionList(optName);
/*    */ 
/* 73 */       buffer.append("<idc:optionlist name=\"" + optName + "\">\r\n");
/*    */ 
/* 75 */       int numOptions = optList.size();
/* 76 */       for (int i = 0; i < numOptions; ++i)
/*    */       {
/* 78 */         String option = (String)optList.elementAt(i);
/* 79 */         option = SoapUtils.encodeXmlValue(option);
/* 80 */         buffer.append("<idc:option>" + option + "</idc:option>\r\n");
/*    */       }
/*    */ 
/* 83 */       buffer.append("</idc:optionlist>\r\n");
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 89 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.generic.SoapGenericOptionListData
 * JD-Core Version:    0.5.4
 */