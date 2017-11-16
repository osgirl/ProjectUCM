/*    */ package intradoc.apputilities.installer;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import java.io.PrintStream;
/*    */ import java.util.Properties;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class UserInteractionTest extends InteractiveInstaller
/*    */ {
/*    */   public UserInteractionTest(Properties installerProps, Properties overrideProps, PromptUser prompter)
/*    */   {
/* 32 */     super(installerProps, overrideProps, prompter);
/*    */   }
/*    */ 
/*    */   public boolean interactWithUser(Vector commandLineArguments)
/*    */     throws ServiceException
/*    */   {
/* 39 */     String path = this.m_installProps.getProperty("UserInteractionPath");
/* 40 */     if (path == null)
/*    */     {
/* 42 */       path = this.m_installer.getConfigValue("UserInteractionPath");
/*    */     }
/* 44 */     if (path == null)
/*    */     {
/* 46 */       throw new ServiceException("You must specify a UserInteractionPath.");
/*    */     }
/* 48 */     Vector results = processInstallationPath(path, true, null);
/* 49 */     for (int i = 0; i < results.size(); ++i)
/*    */     {
/* 51 */       String[] setting = (String[])(String[])results.elementAt(i);
/* 52 */       System.out.println(setting[0] + "=" + setting[1]);
/*    */     }
/* 54 */     return false;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 59 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.UserInteractionTest
 * JD-Core Version:    0.5.4
 */