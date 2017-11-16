/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.apputilities.installer.PromptUser;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ServerInstallUtilsPromptUser
/*     */   implements PromptUser
/*     */ {
/*     */   public boolean m_isStrict;
/*     */   public int m_lineLength;
/*     */   public int m_height;
/*     */   public List<String> m_messages;
/*     */ 
/*     */   public ServerInstallUtilsPromptUser()
/*     */   {
/*  29 */     this.m_lineLength = 80;
/*  30 */     this.m_height = 24;
/*  31 */     this.m_messages = new ArrayList();
/*     */   }
/*     */ 
/*     */   public String prompt(int type, String label, String defValue, Object data, String explaination)
/*     */   {
/*  36 */     throw new AssertionError("Prompting from server not supported.");
/*     */   }
/*     */ 
/*     */   public String prompt(int type, IdcMessage label, String defValue, Object data, IdcMessage explaination)
/*     */   {
/*  42 */     throw new AssertionError("Prompting from server not supported.");
/*     */   }
/*     */ 
/*     */   public String trimStringMid(String msg)
/*     */   {
/*  47 */     return msg;
/*     */   }
/*     */ 
/*     */   public void setLineLength(int width)
/*     */   {
/*  52 */     this.m_lineLength = width;
/*     */   }
/*     */ 
/*     */   public int getLineLength()
/*     */   {
/*  57 */     return this.m_lineLength;
/*     */   }
/*     */ 
/*     */   public void setScreenHeight(int height)
/*     */   {
/*  62 */     this.m_height = height;
/*     */   }
/*     */ 
/*     */   public int getScreenHeight()
/*     */   {
/*  67 */     return this.m_height;
/*     */   }
/*     */ 
/*     */   public boolean getQuiet()
/*     */   {
/*  72 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean setQuiet(boolean newQuiet)
/*     */   {
/*  77 */     return true;
/*     */   }
/*     */ 
/*     */   public void outputMessage(String text)
/*     */   {
/*  82 */     if (this.m_isStrict)
/*     */     {
/*  84 */       throw new AssertionError(text);
/*     */     }
/*  86 */     this.m_messages.add(text);
/*     */   }
/*     */ 
/*     */   public void updateMessage(String text)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void finalizeOutput()
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 102 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78300 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ServerInstallUtilsPromptUser
 * JD-Core Version:    0.5.4
 */