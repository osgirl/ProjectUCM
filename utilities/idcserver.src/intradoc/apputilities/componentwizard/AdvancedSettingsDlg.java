/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class AdvancedSettingsDlg extends CWizardBaseDlg
/*     */ {
/*  57 */   protected DialogHelper m_dlgHelper = null;
/*  58 */   protected ComponentWizardManager m_manager = null;
/*     */ 
/*     */   public AdvancedSettingsDlg(ComponentWizardManager manager, SystemInterface sys)
/*     */   {
/*  62 */     this.m_manager = manager;
/*  63 */     this.m_systemInterface = sys;
/*     */ 
/*  65 */     this.m_helper = new DialogHelper(sys, LocaleResources.getString("csCompWizAdvSettingsTitle", null), true, true);
/*     */   }
/*     */ 
/*     */   public void showAdvanceBuildSettings(Properties props, boolean isView, boolean hasPref)
/*     */   {
/*  71 */     init();
/*  72 */     this.m_dlgHelper = ((DialogHelper)this.m_helper);
/*     */ 
/*  74 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/*  75 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/*  76 */     this.m_dlgHelper.m_scrollPane.setPreferredSize(new Dimension(775, 500));
/*  77 */     this.m_dlgHelper.m_props = props;
/*     */ 
/*  79 */     if (isView)
/*     */     {
/*  81 */       this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_AdvBuild");
/*  82 */       createViewSettingsUI(mainPanel);
/*     */     }
/*     */     else
/*     */     {
/*  86 */       this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_AdvBuildSettings");
/*  87 */       createEditSettingsUI(mainPanel);
/*     */     }
/*     */ 
/*  90 */     DialogCallback okCallback = new DialogCallback(hasPref)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/*  97 */           String installID = AdvancedSettingsDlg.this.m_helper.m_props.getProperty("installID");
/*  98 */           if ((((installID == null) || (installID.length() == 0))) && (this.val$hasPref))
/*     */           {
/* 100 */             throw new ServiceException(null, "csCompWizInstallIDNotSpecified", new Object[0]);
/*     */           }
/* 102 */           String addComps = AdvancedSettingsDlg.this.m_helper.m_props.getProperty("additionalComponents");
/* 103 */           if ((addComps != null) && (addComps.length() > 0))
/*     */           {
/* 105 */             CWizardGuiUtils.doMessage(AdvancedSettingsDlg.this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizAddCompsInfoMsg", new Object[0]), 1);
/*     */           }
/*     */ 
/* 110 */           String tags = AdvancedSettingsDlg.this.m_helper.m_props.getProperty("componentTags");
/* 111 */           List tagList = StringUtils.makeListFromSequenceSimple(tags);
/* 112 */           if (tagList.contains("home"))
/*     */           {
/* 114 */             throw new ServiceException(null, "csCompWizContainsInvalidTag", new Object[] { "home" });
/*     */           }
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 119 */           CWizardGuiUtils.reportError(AdvancedSettingsDlg.this.m_systemInterface, exp, (IdcMessage)null);
/* 120 */           return false;
/*     */         }
/* 122 */         return true;
/*     */       }
/*     */     };
/* 126 */     this.m_dlgHelper.addOK(okCallback);
/* 127 */     this.m_dlgHelper.addCancel(null);
/* 128 */     this.m_dlgHelper.addHelp(null);
/*     */ 
/* 130 */     this.m_dlgHelper.prompt();
/*     */   }
/*     */ 
/*     */   protected void createViewSettingsUI(JPanel mainPanel)
/*     */   {
/* 135 */     JPanel curPanel = null;
/* 136 */     String[][] settingsInfo = CWizardUtils.ADVANCED_SETTINGS_INFO;
/* 137 */     for (int i = 0; i < settingsInfo.length; ++i)
/*     */     {
/* 139 */       String name = settingsInfo[i][0];
/* 140 */       String label = LocaleResources.getString(settingsInfo[i][1], null);
/* 141 */       String type = settingsInfo[i][3];
/* 142 */       boolean isNewPanel = StringUtils.convertToBool(settingsInfo[i][5], false);
/* 143 */       if ((curPanel == null) || (isNewPanel))
/*     */       {
/* 145 */         curPanel = addNewSubPanel(mainPanel, true);
/*     */       }
/* 147 */       if (type.equals("bool"))
/*     */       {
/* 149 */         if (!StringUtils.convertToBool(this.m_helper.m_props.getProperty(name), false))
/*     */           continue;
/* 151 */         JCheckBox checkbox = new CustomCheckbox("   ", 1);
/* 152 */         this.m_dlgHelper.addLabelFieldPair(curPanel, label, checkbox, name);
/* 153 */         checkbox.setEnabled(false);
/*     */       }
/*     */       else
/*     */       {
/* 158 */         addLongTextCustomLabel(this.m_dlgHelper, curPanel, label, name);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createEditSettingsUI(JPanel mainPanel)
/*     */   {
/* 165 */     JPanel curPanel = null;
/* 166 */     String[][] settingsInfo = CWizardUtils.ADVANCED_SETTINGS_INFO;
/* 167 */     for (int i = 0; i < settingsInfo.length; ++i)
/*     */     {
/* 169 */       String name = settingsInfo[i][0];
/* 170 */       String label = LocaleResources.getString(settingsInfo[i][1], null);
/* 171 */       String desc = LocaleResources.getString(settingsInfo[i][2], null);
/* 172 */       String type = settingsInfo[i][3];
/*     */ 
/* 174 */       boolean isNewPanel = StringUtils.convertToBool(settingsInfo[i][5], false);
/* 175 */       if ((curPanel == null) || (isNewPanel))
/*     */       {
/* 177 */         curPanel = addNewSubPanel(mainPanel, true);
/*     */       }
/* 179 */       if (type.indexOf("choice") >= 0)
/*     */       {
/* 181 */         boolean isMulti = type.equals("multichoice");
/* 182 */         String optName = settingsInfo[i][4];
/*     */ 
/* 184 */         String[][] infoTable = (String[][])null;
/* 185 */         List infoList = null;
/* 186 */         if (optName.equals("version"))
/*     */         {
/* 188 */           infoTable = createCollapsedVersionTable();
/*     */         }
/* 190 */         else if (optName.equals("path"))
/*     */         {
/* 192 */           infoTable = CWizardUtils.createInfoTableFromNamedTable("ComponentCustomClassPathDefaults", "path");
/*     */         }
/* 195 */         else if (optName.equals("tags"))
/*     */         {
/* 197 */           DataResultSet components = this.m_manager.getEditComponents();
/* 198 */           infoList = CWizardUtils.createTagsList(components, false);
/*     */         }
/*     */ 
/* 201 */         if ((infoTable != null) || (infoList != null))
/*     */         {
/* 203 */           ComboChoice choice = new ComboChoice(isMulti);
/* 204 */           if (infoTable != null)
/*     */           {
/* 206 */             choice.initChoiceList(infoTable);
/*     */           }
/*     */           else
/*     */           {
/* 210 */             choice.initChoiceList(infoList);
/*     */           }
/* 212 */           this.m_dlgHelper.addLabelFieldPair(curPanel, label, choice, name);
/*     */         }
/*     */       }
/* 215 */       else if (type.equals("bool"))
/*     */       {
/* 217 */         JCheckBox box = new CustomCheckbox("   ", 1);
/* 218 */         this.m_dlgHelper.addLabelFieldPair(curPanel, label, box, name);
/*     */       }
/* 220 */       else if (type.equals("memo"))
/*     */       {
/* 222 */         JTextArea textArea = new CustomTextArea(3, 40);
/* 223 */         this.m_dlgHelper.addLabelFieldPair(curPanel, label, textArea, name);
/*     */       }
/*     */       else
/*     */       {
/* 227 */         this.m_dlgHelper.addLabelEditPair(curPanel, label, 40, name);
/*     */       }
/* 229 */       CWizardGuiUtils.addDescription(this.m_dlgHelper, curPanel, desc);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addLongTextCustomLabel(ContainerHelper helper, JPanel panel, String label, String name)
/*     */   {
/* 236 */     LongTextCustomLabel cp = new LongTextCustomLabel();
/* 237 */     cp.setMinWidth(400);
/* 238 */     helper.addLabelFieldPair(panel, LocaleResources.getString(label, null), cp, name);
/*     */   }
/*     */ 
/*     */   public String[][] createCollapsedVersionTable()
/*     */   {
/* 243 */     DataResultSet drset = SharedObjects.getTable("ServerVersionInfo");
/*     */ 
/* 245 */     DataResultSet newSet = new DataResultSet();
/* 246 */     newSet.copyFieldInfo(drset);
/*     */ 
/* 248 */     FieldInfo[] finfo = null;
/*     */     try
/*     */     {
/* 251 */       finfo = ResultSetUtils.createInfoList(newSet, new String[] { "version", "label" }, false);
/*     */     }
/*     */     catch (DataException d)
/*     */     {
/* 256 */       return (String[][])null;
/*     */     }
/*     */ 
/* 259 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 262 */       Vector row = drset.getCurrentRowValues();
/* 263 */       String version = (String)row.elementAt(finfo[0].m_index);
/* 264 */       String label = (String)row.elementAt(finfo[1].m_index);
/* 265 */       if (version == null) continue; if (label == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 271 */       int index = version.indexOf(".");
/* 272 */       int index2 = version.indexOf(".", index + 1);
/*     */ 
/* 274 */       if (index2 > index)
/*     */       {
/* 276 */         index = index2;
/*     */       }
/*     */ 
/* 279 */       String newVer = version;
/* 280 */       if (index > 0)
/*     */       {
/* 282 */         newVer = version.substring(0, index);
/*     */       }
/*     */ 
/* 285 */       Vector v = newSet.findRow(finfo[0].m_index, newVer);
/*     */ 
/* 287 */       if (v != null)
/*     */       {
/* 290 */         String newLabel = v.elementAt(finfo[1].m_index) + ", " + label;
/* 291 */         v.setElementAt(newLabel, finfo[1].m_index);
/*     */ 
/* 293 */         newSet.setRowValues(v, newSet.getCurrentRow());
/*     */       }
/*     */       else
/*     */       {
/* 298 */         v = (Vector)row.clone();
/* 299 */         v.setElementAt(newVer, finfo[0].m_index);
/* 300 */         newSet.addRow(v);
/*     */       }
/*     */     }
/*     */ 
/* 304 */     String[][] table = (String[][])null;
/*     */     try
/*     */     {
/* 307 */       table = ResultSetUtils.createStringTable(newSet, null);
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/* 311 */       if (SystemUtils.m_verbose)
/*     */       {
/* 313 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/* 317 */     return table;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 322 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79356 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.AdvancedSettingsDlg
 * JD-Core Version:    0.5.4
 */