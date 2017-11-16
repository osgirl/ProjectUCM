/*    */ package intradoc.common;
/*    */ 
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Properties;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class PropertiesTreeNode
/*    */ {
/* 30 */   public String m_name = null;
/* 31 */   public Properties m_properties = null;
/* 32 */   public String m_value = null;
/* 33 */   public Vector m_subNodes = null;
/*    */ 
/*    */   public PropertiesTreeNode(String name, Properties props)
/*    */   {
/* 37 */     this.m_name = name;
/* 38 */     this.m_properties = props;
/* 39 */     this.m_value = "";
/* 40 */     this.m_subNodes = new IdcVector();
/*    */   }
/*    */ 
/*    */   public void addSubNode(PropertiesTreeNode node)
/*    */   {
/* 45 */     this.m_subNodes.addElement(node);
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 51 */     return "m_name: " + this.m_name + "\nm_properties: " + this.m_properties + "\nm_value: " + this.m_value + "\nm_subNodes: " + this.m_subNodes.size();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 57 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.PropertiesTreeNode
 * JD-Core Version:    0.5.4
 */