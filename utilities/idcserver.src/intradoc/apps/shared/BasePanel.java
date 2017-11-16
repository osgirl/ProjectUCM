/*    */ package intradoc.apps.shared;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.SystemInterface;
/*    */ import intradoc.gui.ContainerHelper;
/*    */ import intradoc.gui.MessageBox;
/*    */ import intradoc.gui.PanePanel;
/*    */ import intradoc.util.IdcMessage;
/*    */ import javax.swing.JMenu;
/*    */ import javax.swing.JPanel;
/*    */ 
/*    */ public abstract class BasePanel extends PanePanel
/*    */ {
/*    */   public SystemInterface m_systemInterface;
/*    */   public ExecutionContext m_cxt;
/*    */   protected JMenu m_fMenu;
/*    */   protected ContainerHelper m_helper;
/*    */   protected String m_tableName;
/*    */ 
/*    */   public BasePanel()
/*    */   {
/* 38 */     this.m_systemInterface = null;
/* 39 */     this.m_cxt = null;
/* 40 */     this.m_fMenu = null;
/*    */ 
/* 42 */     this.m_helper = null;
/*    */   }
/*    */ 
/*    */   public void init(SystemInterface sys) throws ServiceException
/*    */   {
/* 47 */     init(sys, null);
/*    */   }
/*    */ 
/*    */   public void init(SystemInterface sys, JMenu fMenu) throws ServiceException
/*    */   {
/* 52 */     this.m_systemInterface = sys;
/* 53 */     this.m_cxt = sys.getExecutionContext();
/* 54 */     this.m_fMenu = fMenu;
/*    */ 
/* 56 */     this.m_helper = new ContainerHelper();
/* 57 */     this.m_helper.attachToContainer(this, sys, null);
/*    */ 
/* 59 */     initUI();
/*    */   }
/*    */ 
/*    */   protected abstract void initUI() throws ServiceException;
/*    */ 
/*    */   protected JPanel initList()
/*    */   {
/* 66 */     return null;
/*    */   }
/*    */ 
/*    */   public void reportError(IdcMessage msg)
/*    */   {
/* 71 */     MessageBox.reportError(this.m_systemInterface, msg);
/*    */   }
/*    */ 
/*    */   public void reportError(Exception e, IdcMessage msg)
/*    */   {
/* 76 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*    */   }
/*    */ 
/*    */   public void reportError(Exception e)
/*    */   {
/* 81 */     MessageBox.reportError(this.m_systemInterface, e);
/*    */   }
/*    */ 
/*    */   public String getString(String key)
/*    */   {
/* 86 */     return LocaleResources.getString(key, this.m_cxt);
/*    */   }
/*    */ 
/*    */   public boolean canExit()
/*    */   {
/* 91 */     return true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 96 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80420 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.BasePanel
 * JD-Core Version:    0.5.4
 */