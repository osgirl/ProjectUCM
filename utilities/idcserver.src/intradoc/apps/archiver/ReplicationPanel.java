/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DisplayLabel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ReplicationPanel extends ArchiverPanel
/*     */ {
/*     */   protected Vector m_exporters;
/*     */   protected JButton m_editRegExporterBtn;
/*     */   protected JButton m_regImporterBtn;
/*     */   protected JButton m_unRegImporterBtn;
/*     */ 
/*     */   public ReplicationPanel()
/*     */   {
/*  61 */     this.m_exporters = null;
/*  62 */     this.m_editRegExporterBtn = null;
/*     */ 
/*  65 */     this.m_regImporterBtn = null;
/*  66 */     this.m_unRegImporterBtn = null;
/*     */   }
/*     */ 
/*     */   public JPanel initUI()
/*     */   {
/*  71 */     JPanel expPanel = initExportUI();
/*  72 */     JPanel impPanel = initImportUI();
/*     */ 
/*  74 */     JPanel wrapper = new PanePanel();
/*  75 */     this.m_helper.makePanelGridBag(wrapper, 1);
/*  76 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  77 */     this.m_helper.addLastComponentInRow(wrapper, expPanel);
/*  78 */     this.m_helper.addLastComponentInRow(wrapper, impPanel);
/*     */ 
/*  80 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public JPanel initExportUI()
/*     */   {
/*  85 */     JPanel pnl = new PanePanel();
/*  86 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/*  88 */     DisplayLabel autoComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/*  89 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelExportAutomated", this.m_cxt), autoComp, "aIsAutomatedExport");
/*     */ 
/*  92 */     JPanel listPanel = new PanePanel();
/*  93 */     listPanel.setLayout(new BorderLayout());
/*  94 */     CustomLabel titleLabel = new CustomLabel(LocaleResources.getString("apLabelRegisteredExporters", this.m_cxt), 1);
/*     */ 
/*  96 */     listPanel.add("North", titleLabel);
/*  97 */     CustomTextArea exportList = new CustomTextArea(3, 50);
/*  98 */     exportList.setEnabled(false);
/*  99 */     listPanel.add("Center", exportList);
/* 100 */     this.m_helper.m_exchange.addComponent("aRegisteredExporters", exportList, null);
/*     */ 
/* 103 */     ActionListener edListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 107 */         RegisterExporterDlg dlg = new RegisterExporterDlg(ReplicationPanel.this.m_systemInterface, LocaleResources.getString("apLabelRegisteredExporter", ReplicationPanel.this.m_cxt), ReplicationPanel.this.m_collectionContext);
/*     */ 
/* 110 */         dlg.init(ReplicationPanel.this.m_helper.m_props);
/*     */       }
/*     */     };
/* 115 */     JPanel btnPanel = new PanePanel();
/* 116 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 119 */     this.m_editRegExporterBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/*     */ 
/* 121 */     this.m_editRegExporterBtn.addActionListener(edListener);
/* 122 */     this.m_editRegExporterBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableEditRegisteredExporters", this.m_cxt));
/*     */ 
/* 124 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 125 */     this.m_helper.addLastComponentInRow(btnPanel, this.m_editRegExporterBtn);
/*     */ 
/* 128 */     JPanel wrapper = new CustomPanel();
/* 129 */     wrapper.setLayout(new BorderLayout());
/* 130 */     wrapper.add("North", pnl);
/* 131 */     wrapper.add("Center", listPanel);
/* 132 */     wrapper.add("East", btnPanel);
/*     */ 
/* 134 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public JPanel initImportUI()
/*     */   {
/* 139 */     JPanel pnl = new CustomPanel();
/* 140 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/* 142 */     CustomLabel impComp = new CustomLabel();
/* 143 */     impComp.setMinWidth(100);
/* 144 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelRegisteredImporter", this.m_cxt), impComp, "aRegisteredImporter");
/*     */ 
/* 147 */     CustomLabel userComp = new CustomLabel();
/* 148 */     userComp.setMinWidth(100);
/* 149 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelLogonUserName", this.m_cxt), userComp, "aImportLogonUser");
/*     */ 
/* 152 */     JPanel btnPanel = new PanePanel();
/* 153 */     btnPanel.setLayout(new FlowLayout());
/*     */ 
/* 155 */     this.m_regImporterBtn = new JButton(LocaleResources.getString("apLabelRegisterSelf", this.m_cxt));
/* 156 */     btnPanel.add(this.m_regImporterBtn);
/*     */ 
/* 158 */     this.m_unRegImporterBtn = new JButton(LocaleResources.getString("apLabelUnregister", this.m_cxt));
/* 159 */     btnPanel.add(this.m_unRegImporterBtn);
/*     */ 
/* 161 */     this.m_helper.addComponent(pnl, btnPanel);
/*     */ 
/* 163 */     String editItems = "aRegisteredImporter,aImportLogonUser";
/*     */ 
/* 166 */     ActionListener regListener = new Object()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 170 */         boolean isReg = true;
/*     */         try
/*     */         {
/* 173 */           Object src = e.getSource();
/* 174 */           Properties props = (Properties)ReplicationPanel.this.m_helper.m_props.clone();
/* 175 */           if (src == ReplicationPanel.this.m_unRegImporterBtn)
/*     */           {
/* 177 */             isReg = false;
/*     */           }
/*     */           else
/*     */           {
/* 181 */             String warningMsg = null;
/* 182 */             boolean isAutoExport = StringUtils.convertToBool(props.getProperty("aIsAutomatedExport"), false);
/* 183 */             if (isAutoExport)
/*     */             {
/* 185 */               String importer = SharedObjects.getEnvironmentValue("IDC_Name");
/* 186 */               String regExportersStr = props.getProperty("aRegisteredExporters");
/* 187 */               Vector regExporters = StringUtils.parseArray(regExportersStr, ',', ',');
/* 188 */               int size = regExporters.size();
/* 189 */               for (int i = 0; i < size; ++i)
/*     */               {
/* 191 */                 String exporter = (String)regExporters.elementAt(i);
/* 192 */                 if (!exporter.equals(importer))
/*     */                   continue;
/* 194 */                 warningMsg = LocaleUtils.encodeMessage("apImporterMatchesExporter", null, exporter);
/* 195 */                 break;
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 200 */             String name = props.getProperty("aArchiveName");
/* 201 */             String pmsg = "";
/* 202 */             if (warningMsg != null)
/*     */             {
/* 204 */               pmsg = warningMsg;
/*     */             }
/* 206 */             pmsg = pmsg + LocaleUtils.encodeMessage("apVerifyAutomatedImportRegistration", null, name);
/*     */ 
/* 208 */             int result = MessageBox.doMessage(ReplicationPanel.this.m_systemInterface, pmsg, 2);
/*     */ 
/* 210 */             if (result == 0)
/*     */             {
/* 212 */               return;
/*     */             }
/*     */           }
/* 215 */           props.put("IsRegister", (isReg) ? "1" : "0");
/* 216 */           props.put("EditItems", "aRegisteredImporter,aImportLogonUser");
/*     */ 
/* 218 */           SharedContext shContext = ReplicationPanel.this.m_collectionContext.getSharedContext();
/* 219 */           AppContextUtils.executeService(shContext, "REGISTER_IMPORTER", props, true);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 224 */           IdcMessage msg = IdcMessageFactory.lc((isReg) ? "apUnableToRegisterImporter" : "apUnableToUnregisterImporter", new Object[0]);
/*     */ 
/* 226 */           MessageBox.reportError(ReplicationPanel.this.m_systemInterface, exp, msg);
/*     */         }
/*     */       }
/*     */     };
/* 230 */     this.m_regImporterBtn.addActionListener(regListener);
/* 231 */     this.m_unRegImporterBtn.addActionListener(regListener);
/*     */ 
/* 233 */     return pnl;
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean isEnabled)
/*     */   {
/* 239 */     CollectionData curCollection = this.m_collectionContext.getCurrentCollection();
/* 240 */     if ((curCollection != null) && (curCollection.isProxied()))
/*     */     {
/* 242 */       isEnabled = false;
/*     */     }
/*     */ 
/* 245 */     this.m_editRegExporterBtn.setEnabled(isEnabled);
/* 246 */     this.m_regImporterBtn.setEnabled(isEnabled);
/*     */ 
/* 248 */     if (isEnabled)
/*     */     {
/* 250 */       String regName = this.m_helper.m_props.getProperty("aRegisteredImporter");
/* 251 */       if ((regName == null) || (regName.length() == 0))
/*     */       {
/* 254 */         isEnabled = false;
/*     */       }
/*     */     }
/* 257 */     this.m_unRegImporterBtn.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 264 */     if (name.equals("aRegisteredExporters"))
/*     */     {
/* 266 */       if (updateComponent)
/*     */       {
/* 268 */         String value = this.m_helper.m_props.getProperty(name);
/* 269 */         exchange.m_compValue = refreshExporterDisplay(value);
/*     */       }
/*     */       else
/*     */       {
/* 273 */         String str = StringUtils.createString(this.m_exporters, ',', ',');
/* 274 */         this.m_helper.m_props.put(name, str);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/* 279 */       this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   protected String refreshExporterDisplay(String str)
/*     */   {
/* 285 */     this.m_exporters = StringUtils.parseArray(str, ',', ',');
/*     */ 
/* 287 */     StringBuffer buffer = new StringBuffer();
/* 288 */     int num = this.m_exporters.size();
/* 289 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 291 */       if (buffer.length() > 0)
/*     */       {
/* 293 */         buffer.append("\n");
/*     */       }
/* 295 */       buffer.append((String)this.m_exporters.elementAt(i));
/*     */     }
/*     */ 
/* 298 */     return buffer.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 303 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ReplicationPanel
 * JD-Core Version:    0.5.4
 */