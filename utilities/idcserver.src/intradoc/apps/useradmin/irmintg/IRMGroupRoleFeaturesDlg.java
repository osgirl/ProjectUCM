/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMGroupRoleFeaturesDlg extends IRMFeaturesHelper
/*     */   implements ItemListener
/*     */ {
/*     */   protected HashMap<Long, Vector<String>> m_defaultRightsFeaturesMap;
/*     */   protected CustomCheckbox m_useDefaultFeaturesCheckBox;
/*     */   protected HashSet<String> m_overriddenFeaturesList;
/*     */   protected DataResultSet m_rsFeatureMap;
/*     */   protected boolean m_hasOverridenFeatures;
/*     */   protected long m_rolePrivilege;
/*     */ 
/*     */   public IRMGroupRoleFeaturesDlg(JPanel panel, RoleGroupData data, ExecutionContext context, SystemInterface sys)
/*     */   {
/*  84 */     super(panel, data, context, sys);
/*  85 */     this.m_defaultRightsFeaturesMap = new HashMap();
/*  86 */     this.m_overriddenFeaturesList = new HashSet();
/*  87 */     this.m_rsFeatureMap = new DataResultSet();
/*  88 */     this.m_rolePrivilege = this.m_roleGroupData.m_privilege;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws ServiceException
/*     */   {
/*  98 */     getFeatureMapping();
/*  99 */     initClientRightsPanel(this.m_clientRightsPanel);
/* 100 */     initUseDefaultFeatureCheckBox();
/* 101 */     initIRMFeatureCheckBoxes();
/*     */   }
/*     */ 
/*     */   public void initClientRightsPanel(JPanel clientRightsPanel)
/*     */     throws ServiceException
/*     */   {
/* 112 */     super.initClientRightsPanel(clientRightsPanel);
/*     */ 
/* 114 */     JPanel pnlUseDefaultFeatures = new PanePanel();
/* 115 */     pnlUseDefaultFeatures.setLayout(new FlowLayout(3));
/* 116 */     clientRightsPanel.add("North", pnlUseDefaultFeatures);
/*     */ 
/* 118 */     this.m_useDefaultFeaturesCheckBox = new CustomCheckbox(LocaleResources.getString("apUseDefaultIRMFeatures", this.m_ctx));
/*     */ 
/* 120 */     this.m_useDefaultFeaturesCheckBox.addItemListener(this);
/*     */ 
/* 122 */     pnlUseDefaultFeatures.add(this.m_useDefaultFeaturesCheckBox);
/*     */   }
/*     */ 
/*     */   public void getFeatureMapping()
/*     */   {
/* 130 */     DataBinder binder = new DataBinder();
/* 131 */     DataResultSet rsDefaultFeatureMap = null;
/*     */     try
/*     */     {
/* 134 */       AppLauncher.executeService("GET_IRM_DEFAULT_MAPPING", binder);
/*     */ 
/* 136 */       rsDefaultFeatureMap = (DataResultSet)binder.getResultSet("rsDefaultFeatureMap");
/*     */ 
/* 138 */       addResultSetToHashMap(rsDefaultFeatureMap, this.m_defaultRightsFeaturesMap);
/*     */ 
/* 141 */       if ((!this.m_roleGroupData.m_roleName.isEmpty()) && (!this.m_roleGroupData.m_groupName.isEmpty()))
/*     */       {
/* 144 */         binder.putLocal("isAccount", Boolean.toString(false));
/*     */ 
/* 146 */         binder.putLocal("dRoleName", this.m_roleGroupData.m_roleName);
/*     */ 
/* 148 */         binder.putLocal("dGroupName", this.m_roleGroupData.m_groupName);
/*     */ 
/* 151 */         AppLauncher.executeService("GET_RIGHT_IRMFEATURE_MAPPING", binder);
/*     */ 
/* 155 */         this.m_rsFeatureMap = ((DataResultSet)binder.getResultSet("rsFeatureMap"));
/*     */ 
/* 157 */         this.m_hasOverridenFeatures = Boolean.parseBoolean(binder.get("hasOverriddenFeatures"));
/*     */ 
/* 160 */         addResultSetToSet(this.m_rsFeatureMap, this.m_overriddenFeaturesList);
/*     */       }
/*     */       else
/*     */       {
/* 164 */         if (rsDefaultFeatureMap != null)
/*     */         {
/* 166 */           this.m_rsFeatureMap.mergeFields(rsDefaultFeatureMap);
/*     */         }
/*     */ 
/* 169 */         addResultSetToSet(this.m_rsFeatureMap, this.m_overriddenFeaturesList);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 174 */       MessageBox.reportError(this.m_systemInterface, new IdcMessage(LocaleResources.getString("apFailedToGetFeatureMapping", this.m_ctx), new Object[0]));
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 180 */       MessageBox.reportError(this.m_systemInterface, new IdcMessage(LocaleResources.getString("apFailedToGetFeatureMapping", this.m_ctx), new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initUseDefaultFeatureCheckBox()
/*     */   {
/* 192 */     if (this.m_hasOverridenFeatures)
/*     */     {
/* 194 */       this.m_useDefaultFeaturesCheckBox.setSelected(false);
/*     */     }
/*     */     else
/*     */     {
/* 198 */       this.m_useDefaultFeaturesCheckBox.setSelected(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initIRMFeatureCheckBoxes()
/*     */   {
/* 209 */     for (IRMFeatureBox featureBox : this.m_irmFeatureBoxList)
/*     */     {
/* 211 */       featureBox.removeItemListener(this);
/* 212 */       featureBox.setSelected(false);
/* 213 */       featureBox.addItemListener(this);
/* 214 */       if (this.m_useDefaultFeaturesCheckBox.isSelected())
/*     */       {
/* 216 */         featureBox.setEnabled(false);
/*     */       }
/*     */       else
/*     */       {
/* 220 */         featureBox.setEnabled(true);
/*     */       }
/*     */     }
/*     */     long rolePrivilege;
/*     */     Iterator i$;
/* 223 */     if (this.m_useDefaultFeaturesCheckBox.isSelected())
/*     */     {
/* 226 */       rolePrivilege = this.m_rolePrivilege;
/* 227 */       Set privileges = this.m_defaultRightsFeaturesMap.keySet();
/* 228 */       for (i$ = privileges.iterator(); i$.hasNext(); ) { long privilege = ((Long)i$.next()).longValue();
/*     */ 
/* 230 */         if (privilege <= rolePrivilege)
/*     */         {
/* 232 */           List features = (List)this.m_defaultRightsFeaturesMap.get(Long.valueOf(privilege));
/*     */ 
/* 234 */           for (String featureID : features)
/*     */           {
/* 236 */             IRMFeatureBox checkbox = (IRMFeatureBox)this.m_irmFeatureIdCheckBoxMap.get(featureID);
/*     */ 
/* 238 */             if (checkbox != null)
/*     */             {
/* 240 */               checkbox.setSelected(true);
/*     */             }
/*     */           }
/*     */         } }
/*     */ 
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 249 */       for (String featureID : this.m_overriddenFeaturesList)
/*     */       {
/* 256 */         if (this.m_irmFeatureIdCheckBoxMap.containsKey(featureID))
/*     */         {
/* 258 */           IRMFeatureBox checkbox = (IRMFeatureBox)this.m_irmFeatureIdCheckBoxMap.get(featureID);
/*     */ 
/* 260 */           checkbox.setSelected(true);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateFeatures(long privilege)
/*     */   {
/* 275 */     this.m_rolePrivilege = privilege;
/* 276 */     if (privilege == 0L)
/*     */     {
/* 278 */       this.m_useDefaultFeaturesCheckBox.setSelected(true);
/*     */     }
/* 280 */     initIRMFeatureCheckBoxes();
/*     */   }
/*     */ 
/*     */   public void getMappingResultSet(DataResultSet rsMap)
/*     */   {
/* 290 */     rsMap.copy(this.m_rsFeatureMap);
/*     */   }
/*     */ 
/*     */   public String setMappingResultSet()
/*     */   {
/* 298 */     String errMsg = null;
/*     */ 
/* 301 */     this.m_rsFeatureMap.removeAll();
/* 302 */     if (!this.m_useDefaultFeaturesCheckBox.isSelected())
/*     */     {
/* 316 */       if ((!this.m_useDefaultFeaturesCheckBox.isSelected()) && (this.m_overriddenFeaturesList.isEmpty()))
/*     */       {
/* 319 */         this.m_overriddenFeaturesList.add("EmptyFeature");
/*     */       }
/* 321 */       if ((!this.m_useDefaultFeaturesCheckBox.isSelected()) && (!this.m_overriddenFeaturesList.isEmpty()) && (this.m_overriddenFeaturesList.contains("EmptyFeature")))
/*     */       {
/* 326 */         int result = MessageBox.doMessage(this.m_systemInterface, "None of the IRMFeatures are selected.Do want to save the changes?", 2);
/*     */ 
/* 332 */         if (result == 0)
/*     */         {
/* 334 */           errMsg = "Select atleast an IRM Feature";
/*     */         }
/*     */         else
/*     */         {
/* 338 */           errMsg = null;
/*     */         }
/*     */       }
/*     */ 
/* 342 */       addSetToResultSet(this.m_overriddenFeaturesList, this.m_rsFeatureMap);
/*     */     }
/* 344 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 355 */     boolean isSelected = false;
/* 356 */     if (e.getStateChange() == 1)
/*     */     {
/* 358 */       isSelected = true;
/*     */     }
/* 360 */     if (e.getSource() == this.m_useDefaultFeaturesCheckBox)
/*     */     {
/* 362 */       initIRMFeatureCheckBoxes();
/*     */     } else {
/* 364 */       if ((!e.getSource() instanceof IRMFeatureBox) || 
/* 366 */         (this.m_useDefaultFeaturesCheckBox.isSelected()))
/*     */         return;
/* 368 */       IRMFeatureBox checkBox = (IRMFeatureBox)e.getSource();
/* 369 */       if (isSelected)
/*     */       {
/* 371 */         if (this.m_overriddenFeaturesList.contains(checkBox.m_id))
/*     */           return;
/* 373 */         this.m_overriddenFeaturesList.add(checkBox.m_id);
/*     */       }
/*     */       else {
/* 376 */         if (!this.m_overriddenFeaturesList.contains(checkBox.m_id))
/*     */           return;
/* 378 */         this.m_overriddenFeaturesList.remove(checkBox.m_id);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isDefaultFeatureList()
/*     */   {
/* 391 */     return !this.m_hasOverridenFeatures;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 402 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93140 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMGroupRoleFeaturesDlg
 * JD-Core Version:    0.5.4
 */