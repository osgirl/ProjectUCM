/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IRMAccountProtectionDlg extends IRMProtectionDlg
/*     */ {
/*     */   protected String m_accountName;
/*     */ 
/*     */   public IRMAccountProtectionDlg(SystemInterface sys, String title, String accountName)
/*     */   {
/*  47 */     super(sys, title);
/*  48 */     this.m_accountName = accountName;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  57 */     super.init(1, 2);
/*  58 */     packComponents();
/*  59 */     getProtectionData();
/*  60 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   protected void getProtectionData()
/*     */   {
/*  68 */     DataBinder binder = new DataBinder();
/*  69 */     Properties localData = binder.getLocalData();
/*  70 */     localData.put("isAccount", Boolean.TRUE.toString());
/*  71 */     localData.put("dDocAccount", this.m_accountName);
/*  72 */     super.getProtectionData(binder);
/*     */   }
/*     */ 
/*     */   protected void okHandler(ActionEvent event)
/*     */   {
/*  85 */     if ((this.m_accountName == null) || (this.m_accountName.length() == 0))
/*     */     {
/*  87 */       return;
/*     */     }
/*  89 */     DataBinder binder = new DataBinder();
/*  90 */     Properties localData = binder.getLocalData();
/*  91 */     localData.put("isAccount", Boolean.TRUE.toString());
/*  92 */     localData.put("dDocAccount", this.m_accountName);
/*  93 */     super.okHandler(event, binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 104 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92579 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMAccountProtectionDlg
 * JD-Core Version:    0.5.4
 */