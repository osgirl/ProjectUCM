/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.irmintg.util.IRMUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMConstraints
/*     */ {
/*     */   protected RoleGroupData m_roleGroupData;
/*     */   protected JPanel m_roleConstraintsPnl;
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected Vector m_refreshPeriods;
/*     */   protected JCheckBox m_chkBoxWorkOffline;
/*     */   protected DisplayChoice m_cmbRefreshPeriod;
/*     */   protected JLabel m_refreshPeriodLabel;
/*     */   protected DataResultSet m_rsConstraints;
/*     */   protected HashMap<String, String> m_constraintsMap;
/*     */ 
/*     */   public IRMConstraints(JPanel constraintsPnl, RoleGroupData data, SystemInterface sysInterface, ExecutionContext context, JButton btnCancel)
/*     */   {
/*  78 */     this.m_roleConstraintsPnl = constraintsPnl;
/*  79 */     this.m_roleGroupData = data;
/*  80 */     this.m_sysInterface = sysInterface;
/*  81 */     this.m_ctx = context;
/*     */ 
/*  83 */     this.m_refreshPeriods = IRMUtils.getRefreshPeriods(context);
/*     */ 
/*  85 */     this.m_chkBoxWorkOffline = new CustomCheckbox(LocaleResources.getString("apLabelWorkOffline", this.m_ctx));
/*     */ 
/*  87 */     this.m_cmbRefreshPeriod = new DisplayChoice("refreshPeriods");
/*     */ 
/*  89 */     this.m_rsConstraints = new DataResultSet();
/*  90 */     this.m_constraintsMap = new HashMap();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws ServiceException
/*     */   {
/*  99 */     initRoleConstraintsPanel(this.m_roleConstraintsPnl);
/*     */     try
/*     */     {
/* 103 */       initConstraints();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 107 */       MessageBox.reportError(this.m_sysInterface, new IdcMessage(LocaleResources.getString("apFailedToGetIRMConstraints", this.m_ctx), new Object[0]));
/*     */ 
/* 110 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initConstraints()
/*     */     throws ServiceException
/*     */   {
/* 122 */     DataBinder binder = new DataBinder();
/* 123 */     binder.putLocal("isAccount", Boolean.toString(false));
/* 124 */     binder.putLocal("dRoleName", this.m_roleGroupData.m_roleName);
/*     */ 
/* 126 */     binder.putLocal("dGroupName", this.m_roleGroupData.m_groupName);
/*     */ 
/* 129 */     AppLauncher.executeService("GET_IRM_CONSTRAINTS", binder);
/*     */ 
/* 131 */     this.m_rsConstraints = ((DataResultSet)binder.getResultSet("rsConstraints"));
/*     */ 
/* 133 */     if ((this.m_rsConstraints == null) || (this.m_rsConstraints.isEmpty()))
/*     */       return;
/* 135 */     String allowOffline = this.m_rsConstraints.getStringValueByName("AllowOffline");
/*     */ 
/* 137 */     String refreshPeriod = this.m_rsConstraints.getStringValueByName("RefreshPeriodAmount");
/*     */ 
/* 139 */     String refreshUnits = this.m_rsConstraints.getStringValueByName("RefreshPeriodUnits");
/*     */ 
/* 142 */     if ((allowOffline != null) && (allowOffline.equalsIgnoreCase("1")))
/*     */     {
/* 146 */       this.m_chkBoxWorkOffline.setSelected(true);
/*     */     }
/*     */ 
/* 156 */     if ((refreshPeriod == null) || (refreshPeriod.isEmpty()) || (refreshUnits == null) || (refreshUnits.isEmpty())) {
/*     */       return;
/*     */     }
/* 159 */     String strRefreshPeriodUnits = refreshPeriod + " " + refreshUnits;
/*     */ 
/* 162 */     this.m_cmbRefreshPeriod.setSelectedItem(strRefreshPeriodUnits);
/*     */   }
/*     */ 
/*     */   public void initRoleConstraintsPanel(JPanel panel)
/*     */   {
/* 172 */     panel.setLayout(new GridBagLayout());
/* 173 */     GridBagConstraints gridBagConstraints = new GridBagConstraints();
/*     */ 
/* 175 */     gridBagConstraints.anchor = 23;
/* 176 */     gridBagConstraints.gridx = 0;
/* 177 */     gridBagConstraints.gridy = 0;
/*     */ 
/* 179 */     panel.add(this.m_chkBoxWorkOffline, gridBagConstraints);
/*     */ 
/* 181 */     gridBagConstraints.anchor = 23;
/* 182 */     gridBagConstraints.gridx = 0;
/* 183 */     gridBagConstraints.gridy = 1;
/*     */ 
/* 185 */     this.m_refreshPeriodLabel = new JLabel(LocaleResources.getString("apLabelRefreshPeriod", this.m_ctx));
/*     */ 
/* 187 */     panel.add(this.m_refreshPeriodLabel, gridBagConstraints);
/*     */ 
/* 189 */     this.m_cmbRefreshPeriod.init(this.m_refreshPeriods);
/*     */ 
/* 191 */     gridBagConstraints.gridx = 1;
/* 192 */     gridBagConstraints.gridy = 1;
/* 193 */     panel.add(this.m_cmbRefreshPeriod, gridBagConstraints);
/*     */   }
/*     */ 
/*     */   public String setConstraintsResultSet()
/*     */   {
/* 203 */     String errMessage = null;
/* 204 */     boolean boolAllowOffline = (this.m_chkBoxWorkOffline.isEnabled()) && (this.m_chkBoxWorkOffline.isSelected());
/*     */ 
/* 207 */     String allowOffline = "0";
/* 208 */     if (boolAllowOffline)
/*     */     {
/* 210 */       allowOffline = "1";
/*     */     }
/*     */ 
/* 213 */     String refreshPeriod = this.m_cmbRefreshPeriod.getSelectedInternalValue();
/* 214 */     String[] temp = refreshPeriod.split(" ");
/*     */ 
/* 216 */     String refreshAmount = temp[0];
/* 217 */     String refreshUnits = temp[1];
/*     */ 
/* 219 */     this.m_rsConstraints.removeAll();
/*     */ 
/* 221 */     if (errMessage == null)
/*     */     {
/* 223 */       this.m_constraintsMap.put("dGroupName", this.m_roleGroupData.m_groupName);
/*     */ 
/* 225 */       this.m_constraintsMap.put("dRoleName", this.m_roleGroupData.m_roleName);
/*     */ 
/* 227 */       this.m_constraintsMap.put("AllowOffline", String.valueOf(allowOffline));
/*     */ 
/* 229 */       this.m_constraintsMap.put("RefreshPeriodAmount", refreshAmount);
/*     */ 
/* 231 */       this.m_constraintsMap.put("RefreshPeriodUnits", refreshUnits);
/*     */     }
/*     */ 
/* 236 */     return errMessage;
/*     */   }
/*     */ 
/*     */   public void getConstraints(DataBinder binderConstraints)
/*     */   {
/* 244 */     if ((this.m_constraintsMap == null) || (this.m_constraintsMap.isEmpty()))
/*     */       return;
/* 246 */     Set set = this.m_constraintsMap.entrySet();
/* 247 */     Iterator i = set.iterator();
/*     */ 
/* 249 */     while (i.hasNext())
/*     */     {
/* 251 */       Map.Entry entry = (Map.Entry)i.next();
/* 252 */       binderConstraints.putLocal((String)entry.getKey(), (String)entry.getValue());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 265 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94362 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMConstraints
 * JD-Core Version:    0.5.4
 */