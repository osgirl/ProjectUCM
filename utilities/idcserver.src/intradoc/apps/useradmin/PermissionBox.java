/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ 
/*     */ class PermissionBox extends JCheckBox
/*     */ {
/*     */   public String m_idStr;
/*     */   public long m_privilege;
/* 780 */   public Vector m_defaultSelectRights = null;
/* 781 */   public Vector m_defaultUnSelectRights = null;
/*     */ 
/*     */   public PermissionBox(String label, String idStr, String priv, String defaultSelectRights, String defaultUnSelectRights)
/*     */   {
/* 785 */     super(label);
/* 786 */     this.m_idStr = idStr;
/* 787 */     this.m_privilege = NumberUtils.parseHexStringAsLong(priv);
/*     */ 
/* 789 */     if ((defaultSelectRights != null) && (defaultSelectRights.length() > 0))
/*     */     {
/* 791 */       this.m_defaultSelectRights = StringUtils.parseArray(defaultSelectRights, ',', '^');
/*     */     }
/* 793 */     if ((defaultUnSelectRights == null) || (defaultUnSelectRights.length() <= 0))
/*     */       return;
/* 795 */     this.m_defaultUnSelectRights = StringUtils.parseArray(defaultUnSelectRights, ',', '^');
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 801 */     return "releaseInfo=dev,releaseRevision=$Rev: 92578 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.PermissionBox
 * JD-Core Version:    0.5.4
 */