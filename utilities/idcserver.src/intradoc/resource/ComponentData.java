/*    */ package intradoc.resource;
/*    */ 
/*    */ import intradoc.common.FileUtils;
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import intradoc.common.ResourceContainer;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.StringUtils;
/*    */ import intradoc.util.IdcMessage;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class ComponentData
/*    */ {
/*    */   public String m_componentName;
/*    */   public String m_type;
/*    */   public String m_file;
/*    */   public Vector m_tables;
/*    */   public int m_order;
/*    */   public ResourceContainer m_rc;
/* 39 */   public String m_workingDir = null;
/* 40 */   public String m_location = null;
/*    */ 
/*    */   public ComponentData(String name, String type, String workingDir, String file, String tableStr, int order)
/*    */     throws ServiceException
/*    */   {
/* 45 */     this.m_componentName = name;
/* 46 */     this.m_type = type;
/* 47 */     this.m_file = FileUtils.getAbsolutePath(workingDir, file);
/* 48 */     this.m_tables = StringUtils.parseArray(tableStr, ',', ',');
/* 49 */     this.m_order = order;
/*    */ 
/* 51 */     checkFile();
/*    */ 
/* 53 */     this.m_workingDir = workingDir;
/* 54 */     this.m_location = file;
/*    */   }
/*    */ 
/*    */   protected boolean checkFile() throws ServiceException
/*    */   {
/* 59 */     IdcMessage msg = IdcMessageFactory.lc("csComponentFileNotFoundShort", new Object[] { this.m_componentName, this.m_type, this.m_file });
/*    */ 
/* 61 */     FileUtils.validatePath(this.m_file, msg, 1);
/* 62 */     return true;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 68 */     IdcStringBuilder result = new IdcStringBuilder();
/* 69 */     result.append("m_componentName: " + this.m_componentName);
/* 70 */     result.append("\nm_type: " + this.m_type);
/* 71 */     result.append("\nm_file: " + this.m_file);
/* 72 */     result.append("\nm_order: " + this.m_order);
/* 73 */     result.append("\nm_workingDir: " + this.m_workingDir);
/* 74 */     result.append("\nm_location: " + this.m_location);
/* 75 */     result.append("\nm_tables: " + this.m_tables);
/* 76 */     return result.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 81 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ComponentData
 * JD-Core Version:    0.5.4
 */