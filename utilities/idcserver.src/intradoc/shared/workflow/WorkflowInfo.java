/*    */ package intradoc.shared.workflow;
/*    */ 
/*    */ import intradoc.common.StringUtils;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.shared.SecurityUtils;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class WorkflowInfo
/*    */ {
/* 33 */   public Properties m_properties = null;
/* 34 */   public String m_wfName = null;
/* 35 */   public String m_wfID = null;
/* 36 */   public String m_wfStatus = null;
/*    */ 
/* 39 */   public String m_wfType = null;
/* 40 */   public boolean m_isCriteria = false;
/* 41 */   public boolean m_isCollaboration = false;
/*    */ 
/* 44 */   public String m_name = null;
/* 45 */   public String m_operator = null;
/* 46 */   public String m_value = null;
/*    */ 
/*    */   public WorkflowInfo(Properties props)
/*    */   {
/* 50 */     this.m_properties = props;
/*    */ 
/* 52 */     this.m_wfName = props.getProperty("dWfName");
/* 53 */     this.m_wfID = props.getProperty("dWfID");
/* 54 */     this.m_wfStatus = props.getProperty("dWfStatus");
/*    */ 
/* 56 */     this.m_wfType = props.getProperty("dWfType");
/* 57 */     this.m_isCriteria = this.m_wfType.equalsIgnoreCase("criteria");
/*    */ 
/* 59 */     if (!SecurityUtils.m_useCollaboration)
/*    */       return;
/* 61 */     this.m_isCollaboration = StringUtils.convertToBool(props.getProperty("dIsCollaboration"), false);
/*    */   }
/*    */ 
/*    */   public void addCriteriaInfo(Properties props)
/*    */   {
/* 67 */     DataBinder.mergeHashTables(this.m_properties, props);
/*    */ 
/* 69 */     this.m_name = this.m_properties.getProperty("dWfCriteriaName");
/* 70 */     this.m_operator = this.m_properties.getProperty("dWfCriteriaOperator");
/* 71 */     this.m_value = this.m_properties.getProperty("dWfCriteriaValue").toLowerCase();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 76 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.workflow.WorkflowInfo
 * JD-Core Version:    0.5.4
 */