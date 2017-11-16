/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMFeaturesHelper
/*     */   implements ItemListener
/*     */ {
/*     */   protected RoleGroupData m_roleGroupData;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected JPanel m_clientRightsPanel;
/*     */   protected Vector<IRMFeatureBox> m_irmFeatureBoxList;
/*     */   protected HashMap<String, IRMFeatureBox> m_irmFeatureIdCheckBoxMap;
/*     */ 
/*     */   public IRMFeaturesHelper(JPanel panel, RoleGroupData data, ExecutionContext context, SystemInterface sys)
/*     */   {
/*  65 */     this.m_irmFeatureBoxList = new IdcVector();
/*  66 */     this.m_irmFeatureIdCheckBoxMap = new HashMap();
/*  67 */     this.m_roleGroupData = data;
/*  68 */     this.m_ctx = context;
/*  69 */     this.m_clientRightsPanel = panel;
/*  70 */     this.m_systemInterface = sys;
/*     */   }
/*     */ 
/*     */   public void initClientRightsPanel(JPanel clientRightsPanel)
/*     */     throws ServiceException
/*     */   {
/*  79 */     clientRightsPanel.setLayout(new BorderLayout());
/*     */ 
/*  81 */     JPanel jp2 = new PanePanel();
/*  82 */     jp2.setLayout(new BorderLayout());
/*  83 */     clientRightsPanel.add("Center", jp2);
/*     */ 
/*  85 */     JLabel lblFeatures = new JLabel();
/*  86 */     lblFeatures.setText(LocaleResources.getString("apLabelIRMFeatures", this.m_ctx));
/*     */ 
/*  88 */     jp2.add("North", lblFeatures);
/*     */ 
/*  90 */     IRMFeaturesPanel pnlFeatures = new IRMFeaturesPanel(this, this.m_systemInterface, this.m_ctx);
/*     */ 
/*  92 */     jp2.add("Center", pnlFeatures);
/*  93 */     this.m_irmFeatureBoxList = pnlFeatures.m_irmFeatureBoxListIRMFeaturesPanel;
/*  94 */     this.m_irmFeatureIdCheckBoxMap = pnlFeatures.m_irmFeatureIdCheckBoxMapIRMFeaturesPanel;
/*     */   }
/*     */ 
/*     */   public void addResultSetToSet(ResultSet rs, HashSet<String> list)
/*     */   {
/* 104 */     if ((rs == null) || (rs.isEmpty()))
/*     */       return;
/* 106 */     int featureIndex = rs.getFieldInfoIndex("IRMFeature");
/* 107 */     for (rs.first(); rs.isRowPresent(); rs.next())
/*     */     {
/* 109 */       String feature = rs.getStringValue(featureIndex);
/* 110 */       list.add(feature);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addHashMapToResultSet(HashMap<Long, Vector<String>> map, DataResultSet rs)
/*     */   {
/* 125 */     for (Iterator i$ = map.keySet().iterator(); i$.hasNext(); ) { long key = ((Long)i$.next()).longValue();
/*     */ 
/* 127 */       Vector irmFeatures = (Vector)map.get(Long.valueOf(key));
/* 128 */       if (irmFeatures != null)
/*     */       {
/* 130 */         for (int i = 0; i < irmFeatures.size(); ++i)
/*     */         {
/* 132 */           List rowList = new ArrayList();
/* 133 */           rowList.add(Long.valueOf(key));
/* 134 */           rowList.add(irmFeatures.get(i));
/* 135 */           rs.createAndAddRowInitializedWithList(rowList);
/*     */         }
/*     */       } }
/*     */ 
/*     */   }
/*     */ 
/*     */   public void addSetToResultSet(HashSet<String> list, DataResultSet rs)
/*     */   {
/* 149 */     if ((list == null) || (rs == null))
/*     */       return;
/* 151 */     for (String irmFeature : list)
/*     */     {
/* 153 */       Vector v = new Vector();
/* 154 */       v.add(irmFeature);
/* 155 */       v.add(this.m_roleGroupData.m_groupName);
/* 156 */       v.add(this.m_roleGroupData.m_roleName);
/* 157 */       rs.addRow(v);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addResultSetToHashMap(ResultSet rs, HashMap<Long, Vector<String>> map)
/*     */   {
/* 175 */     if ((rs == null) || (rs.isEmpty()))
/*     */       return;
/* 177 */     int privilegeIndex = rs.getFieldInfoIndex("dPrivilege");
/*     */ 
/* 179 */     int featureIndex = rs.getFieldInfoIndex("IRMFeature");
/*     */ 
/* 181 */     for (rs.first(); rs.isRowPresent(); rs.next())
/*     */     {
/* 183 */       long privilege = Long.parseLong(rs.getStringValue(privilegeIndex));
/* 184 */       String feature = rs.getStringValue(featureIndex);
/* 185 */       if (map.containsKey(Long.valueOf(privilege)))
/*     */       {
/* 187 */         Vector featureListTmp = (Vector)map.get(Long.valueOf(privilege));
/* 188 */         featureListTmp.add(feature);
/*     */       }
/*     */       else
/*     */       {
/* 192 */         Vector featureList = new IdcVector();
/* 193 */         featureList.add(feature);
/* 194 */         map.put(Long.valueOf(privilege), featureList);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 293 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92590 $";
/*     */   }
/*     */ 
/*     */   protected class IRMFeaturesPanel extends JPanel
/*     */   {
/*     */     protected SystemInterface m_systemInterfaceIRMFeaturesPanel;
/*     */     protected DataResultSet m_irmFeatures;
/*     */     protected ExecutionContext m_ctxIRMFeaturesPanel;
/*     */     protected ItemListener m_listener;
/*     */     protected Vector<IRMFeatureBox> m_irmFeatureBoxListIRMFeaturesPanel;
/*     */     protected HashMap<String, IRMFeatureBox> m_irmFeatureIdCheckBoxMapIRMFeaturesPanel;
/*     */ 
/*     */     protected IRMFeaturesPanel(ItemListener listener, SystemInterface sys, ExecutionContext ctx)
/*     */       throws ServiceException
/*     */     {
/* 223 */       this.m_irmFeatureBoxListIRMFeaturesPanel = new IdcVector();
/* 224 */       this.m_irmFeatureIdCheckBoxMapIRMFeaturesPanel = new HashMap();
/* 225 */       this.m_systemInterfaceIRMFeaturesPanel = sys;
/* 226 */       this.m_ctxIRMFeaturesPanel = ctx;
/* 227 */       this.m_listener = listener;
/* 228 */       init();
/*     */     }
/*     */ 
/*     */     protected void init() throws ServiceException
/*     */     {
/* 233 */       getIRMFeaturesList();
/* 234 */       initPanel();
/*     */     }
/*     */ 
/*     */     protected void getIRMFeaturesList() throws ServiceException
/*     */     {
/*     */       try
/*     */       {
/* 241 */         DataBinder binder = new DataBinder();
/* 242 */         AppLauncher.executeService("IRM_GET_FEATURES", binder);
/*     */ 
/* 244 */         this.m_irmFeatures = ((DataResultSet)binder.getResultSet("IRMFeatureList"));
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 249 */         MessageBox.reportError(this.m_systemInterfaceIRMFeaturesPanel, new IdcMessage(LocaleResources.getString("apFailedToGetIRMRights", this.m_ctxIRMFeaturesPanel), new Object[0]));
/*     */ 
/* 253 */         throw e;
/*     */       }
/*     */     }
/*     */ 
/*     */     protected void initPanel()
/*     */     {
/* 262 */       setLayout(new GridLayout(0, 3, 0, 0));
/*     */ 
/* 264 */       while ((this.m_irmFeatures != null) && 
/* 266 */         (this.m_irmFeatures.isRowPresent()))
/*     */       {
/* 268 */         String id = this.m_irmFeatures.getStringValueByName("featureID");
/*     */ 
/* 270 */         String label = this.m_irmFeatures.getStringValueByName("featureLabel");
/*     */ 
/* 272 */         String tooltip = this.m_irmFeatures.getStringValueByName("featureDescription");
/*     */ 
/* 274 */         IRMFeatureBox cb = new IRMFeatureBox(id, label, tooltip);
/* 275 */         cb.addItemListener(this.m_listener);
/* 276 */         this.m_irmFeatureBoxListIRMFeaturesPanel.add(cb);
/* 277 */         this.m_irmFeatureIdCheckBoxMapIRMFeaturesPanel.put(id, cb);
/* 278 */         add(cb);
/* 279 */         this.m_irmFeatures.next();
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMFeaturesHelper
 * JD-Core Version:    0.5.4
 */