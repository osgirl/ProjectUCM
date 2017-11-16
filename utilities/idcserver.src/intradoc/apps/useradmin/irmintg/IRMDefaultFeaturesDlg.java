/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.PermissionsData;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMDefaultFeaturesDlg extends IRMFeaturesHelper
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   protected HashMap<Long, Vector<String>> m_rightsFeaturesMap;
/*     */   protected Vector<Long> m_ucmRightsList;
/*     */   protected DisplayChoice m_ucmRightsCombo;
/*     */   protected DataResultSet m_rsFeatureMap;
/*     */ 
/*     */   public IRMDefaultFeaturesDlg(JPanel panel, RoleGroupData data, ExecutionContext context, SystemInterface sys)
/*     */   {
/*  68 */     super(panel, data, context, sys);
/*  69 */     this.m_rightsFeaturesMap = new HashMap();
/*  70 */     this.m_ucmRightsList = new IdcVector();
/*  71 */     this.m_rsFeatureMap = new DataResultSet();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws ServiceException
/*     */   {
/*  80 */     getFeatureMapping();
/*  81 */     initClientRightsPanel(this.m_clientRightsPanel);
/*  82 */     initUCMRightsCombo(this.m_roleGroupData.m_privilege);
/*  83 */     initIRMFeatureCheckBoxes();
/*  84 */     this.m_ucmRightsCombo.addActionListener(this);
/*     */   }
/*     */ 
/*     */   public void initUCMRightsCombo(long privilege)
/*     */   {
/*  95 */     String[][] defs = PermissionsData.m_defs;
/*  96 */     int length = defs.length;
/*  97 */     for (int i = 0; i < length; ++i)
/*     */     {
/*  99 */       long rightsPrivilege = NumberUtils.parseHexStringAsLong(defs[i][2]);
/* 100 */       if ((rightsPrivilege & privilege) == 0L)
/*     */         continue;
/* 102 */       this.m_ucmRightsList.add(Long.valueOf(rightsPrivilege));
/* 103 */       this.m_ucmRightsCombo.addItem(LocaleResources.getString(defs[i][0], this.m_ctx), Long.toString(rightsPrivilege));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initClientRightsPanel(JPanel clientRightsPanel)
/*     */     throws ServiceException
/*     */   {
/* 116 */     super.initClientRightsPanel(clientRightsPanel);
/*     */ 
/* 118 */     JPanel pnlUCMRights = new PanePanel();
/* 119 */     pnlUCMRights.setLayout(new FlowLayout(3));
/* 120 */     clientRightsPanel.add("North", pnlUCMRights);
/*     */ 
/* 122 */     JLabel lblRights = new CustomLabel(LocaleResources.getString("apLabelUCMRights", this.m_ctx), 1);
/*     */ 
/* 124 */     lblRights.setFocusable(false);
/* 125 */     pnlUCMRights.add(lblRights);
/*     */ 
/* 127 */     this.m_ucmRightsCombo = new DisplayChoice();
/* 128 */     this.m_ucmRightsCombo.setEnabled(true);
/* 129 */     pnlUCMRights.add(this.m_ucmRightsCombo);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 134 */     initIRMFeatureCheckBoxes();
/*     */   }
/*     */ 
/*     */   public void getFeatureMapping()
/*     */   {
/* 143 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 147 */       AppLauncher.executeService("GET_IRM_DEFAULT_MAPPING", binder);
/*     */ 
/* 149 */       this.m_rsFeatureMap = ((DataResultSet)binder.getResultSet("rsDefaultFeatureMap"));
/*     */ 
/* 151 */       addResultSetToHashMap(this.m_rsFeatureMap, this.m_rightsFeaturesMap);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 156 */       MessageBox.reportError(this.m_systemInterface, new IdcMessage(LocaleResources.getString("apFailedToGetFeatureMapping", this.m_ctx), new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initIRMFeatureCheckBoxes()
/*     */   {
/* 169 */     boolean isUCMRightsListEmpty = this.m_ucmRightsList.isEmpty();
/* 170 */     long privilege = Long.parseLong(this.m_ucmRightsCombo.getSelectedInternalValue().toString());
/*     */ 
/* 172 */     Vector featureList = (Vector)this.m_rightsFeaturesMap.get(Long.valueOf(privilege));
/*     */ 
/* 174 */     for (int i = 0; i < this.m_irmFeatureBoxList.size(); ++i)
/*     */     {
/* 176 */       IRMFeatureBox featureBox = (IRMFeatureBox)this.m_irmFeatureBoxList.get(i);
/* 177 */       if (featureBox == null)
/*     */         continue;
/* 179 */       featureBox.setEnabled(!isUCMRightsListEmpty);
/* 180 */       if (isUCMRightsListEmpty)
/*     */         continue;
/* 182 */       if ((featureList != null) && (featureList.contains(featureBox.m_id)))
/*     */       {
/* 185 */         featureBox.setSelected(true);
/*     */       }
/*     */       else
/*     */       {
/* 189 */         featureBox.setSelected(false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void getMappingResultSet(DataResultSet rsMap)
/*     */   {
/* 201 */     rsMap.copy(this.m_rsFeatureMap);
/*     */   }
/*     */ 
/*     */   public String setMappingResultSet()
/*     */   {
/* 210 */     String errMessage = null;
/* 211 */     this.m_rsFeatureMap.removeAll();
/*     */ 
/* 213 */     if (errMessage == null)
/* 214 */       addHashMapToResultSet(this.m_rightsFeaturesMap, this.m_rsFeatureMap);
/* 215 */     return errMessage;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 221 */     boolean isSelected = false;
/* 222 */     if (e.getStateChange() == 1)
/*     */     {
/* 224 */       isSelected = true;
/*     */     }
/*     */ 
/* 227 */     long privilege = Long.parseLong(this.m_ucmRightsCombo.getSelectedInternalValue());
/*     */ 
/* 229 */     IRMFeatureBox checkBox = (IRMFeatureBox)e.getSource();
/* 230 */     Vector featureList = (Vector)this.m_rightsFeaturesMap.get(Long.valueOf(privilege));
/*     */ 
/* 232 */     if (isSelected)
/*     */     {
/* 234 */       if (featureList != null)
/*     */       {
/* 236 */         if (featureList.contains(checkBox.m_id))
/*     */           return;
/* 238 */         featureList.add(checkBox.m_id);
/*     */       }
/*     */       else
/*     */       {
/* 243 */         featureList = new Vector();
/* 244 */         featureList.add(checkBox.m_id);
/* 245 */         this.m_rightsFeaturesMap.put(Long.valueOf(privilege), featureList);
/*     */       }
/*     */     } else {
/* 248 */       if ((featureList == null) || (!featureList.contains(checkBox.m_id)))
/*     */         return;
/* 250 */       featureList.remove(checkBox.m_id);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 256 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92590 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMDefaultFeaturesDlg
 * JD-Core Version:    0.5.4
 */