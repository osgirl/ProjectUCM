/*    */ package intradoc.apps.shared;
/*    */ 
/*    */ import intradoc.common.ClassHelper;
/*    */ import intradoc.common.ClassHelperUtils;
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.util.IdcMessage;
/*    */ import java.awt.event.ActionEvent;
/*    */ import java.awt.event.ActionListener;
/*    */ import java.lang.reflect.Method;
/*    */ 
/*    */ public class ReflectionMethodActionListener
/*    */   implements ActionListener
/*    */ {
/* 32 */   public MainFrame m_frame = null;
/* 33 */   public String m_menuID = null;
/* 34 */   public String m_menuLabel = null;
/* 35 */   public String m_className = null;
/* 36 */   public String m_methodName = null;
/*    */ 
/* 38 */   public static final Class m_actionListenerClass = ClassHelperUtils.assertclass("java.awt.event.ActionListener");
/* 39 */   public static final Class m_actionEventClass = ClassHelperUtils.assertclass("java.awt.event.ActionEvent");
/* 40 */   public static final Class m_mainFrameClass = new MainFrame().getClass();
/*    */ 
/*    */   public ReflectionMethodActionListener(MainFrame mainFrame, String menuID, String menuLabel, String className, String methodName)
/*    */   {
/* 44 */     this.m_frame = mainFrame;
/* 45 */     this.m_menuID = menuID;
/* 46 */     this.m_menuLabel = menuLabel;
/* 47 */     this.m_className = className;
/* 48 */     this.m_methodName = methodName;
/*    */   }
/*    */ 
/*    */   public void actionPerformed(ActionEvent e)
/*    */   {
/* 53 */     IdcMessage errMsg = null;
/*    */ 
/* 55 */     Object[] methodParams = { this.m_menuID, this, e, this.m_frame };
/*    */     try
/*    */     {
/* 59 */       Class c = ClassHelperUtils.createClass(this.m_className);
/* 60 */       ClassHelperUtils.executeStaticMethod(c, this.m_methodName, methodParams, new Class[] { ClassHelper.m_stringClass, m_actionListenerClass, m_actionEventClass, m_mainFrameClass });
/*    */     }
/*    */     catch (Exception excep)
/*    */     {
/* 65 */       Report.trace("system", null, excep);
/* 66 */       errMsg = IdcMessageFactory.lc("apReflectionMethodActionListenerException", new Object[] { this.m_menuID, this.m_className, this.m_methodName });
/*    */     }
/*    */ 
/* 69 */     if (errMsg == null)
/*    */       return;
/* 71 */     this.m_frame.reportError(null, errMsg);
/*    */   }
/*    */ 
/*    */   @Deprecated
/*    */   public Method getMethod()
/*    */     throws ClassNotFoundException, NoSuchMethodException
/*    */   {
/* 84 */     Class c = Class.forName(this.m_className);
/* 85 */     return c.getMethod(this.m_methodName, new Class[] { ClassHelper.m_stringClass, m_actionListenerClass, m_actionEventClass, m_mainFrameClass });
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 93 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.ReflectionMethodActionListener
 * JD-Core Version:    0.5.4
 */