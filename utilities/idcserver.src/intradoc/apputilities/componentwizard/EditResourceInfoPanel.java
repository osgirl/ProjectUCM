/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditResourceInfoPanel extends EditBasePanel
/*     */ {
/*  61 */   protected ComboChoice m_fileNameChoice = null;
/*     */ 
/*  64 */   protected Hashtable m_checkboxList = null;
/*     */ 
/*     */   public void initUI(int editType)
/*     */   {
/*  74 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*  75 */     String cwtype = this.m_helper.m_props.getProperty("type");
/*     */ 
/*  77 */     this.m_helper.addLastComponentInRow(this, addTypeInfoPanel(cwtype));
/*  78 */     this.m_helper.addLastComponentInRow(this, addFileInfoPanel());
/*     */ 
/*  80 */     gbh.m_gc.weighty = 1.0D;
/*  81 */     gbh.addEmptyRow(this);
/*     */ 
/*  83 */     if ((cwtype == null) || (cwtype.length() == 0))
/*     */     {
/*  86 */       cwtype = IntradocComponent.RES_DEF[0][0];
/*  87 */       this.m_helper.m_props.put("type", cwtype);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  92 */       initFileNameChoice(cwtype, null);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*  96 */       if (SystemUtils.m_verbose)
/*     */       {
/*  98 */         Report.debug("applet", null, ignore);
/*     */       }
/*     */     }
/* 101 */     this.m_helper.m_props.put("loadOrder", "10");
/* 102 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected JPanel addTypeInfoPanel(String cwtype)
/*     */   {
/* 107 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/* 108 */     JPanel panel = new PanePanel();
/* 109 */     this.m_helper.makePanelGridBag(panel, 1);
/*     */ 
/* 112 */     CustomLabel typeMsg = new CustomLabel(LocaleResources.getString("csCompWizChooseResourceMsg", null), 1);
/*     */ 
/* 114 */     gbh.prepareAddLastRowElement(12);
/* 115 */     this.m_helper.addComponent(panel, typeMsg);
/*     */ 
/* 119 */     JPanel checkboxPanel = new PanePanel();
/* 120 */     checkboxPanel.setLayout(new GridLayout(0, 2));
/*     */ 
/* 122 */     ButtonGroup bg = new ButtonGroup();
/*     */ 
/* 124 */     int length = IntradocComponent.RES_DEF.length;
/* 125 */     this.m_checkboxList = new Hashtable();
/*     */ 
/* 128 */     ItemListener iListener = new Object()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 132 */         Object obj = e.getSource();
/* 133 */         for (Enumeration en = EditResourceInfoPanel.this.m_checkboxList.keys(); en.hasMoreElements(); )
/*     */         {
/* 135 */           int state = e.getStateChange();
/* 136 */           if (state != 1)
/*     */           {
/* 138 */             return;
/*     */           }
/* 140 */           String cname = (String)en.nextElement();
/* 141 */           Object cbox = EditResourceInfoPanel.this.m_checkboxList.get(cname);
/*     */ 
/* 143 */           if (obj == cbox)
/*     */           {
/* 145 */             EditResourceInfoPanel.this.m_helper.m_props.clear();
/* 146 */             EditResourceInfoPanel.this.m_helper.m_props.put("type", cname);
/* 147 */             EditResourceInfoPanel.this.m_resWizard.determineEnableDisable(cname, null, "resource");
/*     */             try
/*     */             {
/* 151 */               EditResourceInfoPanel.this.initFileNameChoice(cname, null);
/*     */             }
/*     */             catch (Exception exp)
/*     */             {
/* 155 */               exp.printStackTrace();
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 163 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 165 */       boolean flag = false;
/*     */ 
/* 167 */       if ((cwtype != null) && (cwtype.length() > 0))
/*     */       {
/* 169 */         if (IntradocComponent.RES_DEF[i][0].equals(cwtype))
/*     */         {
/* 172 */           flag = true;
/*     */         }
/*     */ 
/*     */       }
/* 177 */       else if (i == 0)
/*     */       {
/* 179 */         flag = true;
/*     */       }
/*     */ 
/* 182 */       JCheckBox box = new JCheckBox(LocaleResources.localizeMessage(IntradocComponent.RES_DEF[i][1], null), flag);
/* 183 */       bg.add(box);
/* 184 */       JPanel wrapperPanel = new PanePanel();
/* 185 */       FlowLayout layout = new FlowLayout();
/* 186 */       layout.setAlignment(0);
/* 187 */       wrapperPanel.setLayout(layout);
/* 188 */       wrapperPanel.add(box);
/*     */ 
/* 190 */       this.m_checkboxList.put(IntradocComponent.RES_DEF[i][0], box);
/* 191 */       box.addItemListener(iListener);
/*     */ 
/* 193 */       if ((i % 2 == 1) || (i == length - 1))
/*     */       {
/* 195 */         this.m_helper.m_gridHelper.prepareAddRowElement(17, 0);
/* 196 */         this.m_helper.addLastComponentInRow(checkboxPanel, wrapperPanel);
/*     */       }
/*     */       else
/*     */       {
/* 200 */         this.m_helper.m_gridHelper.prepareAddRowElement(17, 1);
/* 201 */         this.m_helper.addComponent(checkboxPanel, wrapperPanel);
/*     */       }
/*     */     }
/*     */ 
/* 205 */     this.m_helper.addLastComponentInRow(panel, checkboxPanel);
/*     */ 
/* 207 */     return panel;
/*     */   }
/*     */ 
/*     */   protected JPanel addFileInfoPanel()
/*     */   {
/* 212 */     JPanel panel = new PanePanel();
/* 213 */     this.m_helper.makePanelGridBag(panel, 2);
/* 214 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 215 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*     */ 
/* 218 */     String fileMsg = "!csCompWizAddFileMsg";
/*     */ 
/* 220 */     addDescrpAndComponent(panel, "!csCompWizLabelFileName2", fileMsg, this.m_fileNameChoice = new ComboChoice(), "filename", true);
/*     */ 
/* 224 */     String loadMsg = "!csCompWizLoadOrderMsg";
/*     */ 
/* 226 */     addDescrpAndComponent(panel, "!csCompWizLabelLoadOrder2", loadMsg, new CustomTextField(2), "loadOrder", false);
/*     */ 
/* 229 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void initFileNameChoice(String cwType, String mergeTable) throws ServiceException
/*     */   {
/* 234 */     Vector v = new IdcVector();
/* 235 */     DataResultSet drset = this.m_component.getResourceDefTable();
/* 236 */     String type = StringUtils.findString(IntradocComponent.RES_DEF, cwType, 0, 2);
/*     */ 
/* 238 */     String ext = StringUtils.findString(IntradocComponent.RES_DEF, cwType, 0, 3);
/*     */ 
/* 240 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 242 */       String val = drset.getStringValue(0);
/* 243 */       if (!val.equals(type))
/*     */         continue;
/* 245 */       String tempfile = drset.getStringValue(1);
/* 246 */       int index = tempfile.lastIndexOf(46);
/* 247 */       String tempExt = tempfile.substring(index + 1, tempfile.length());
/* 248 */       boolean isUnique = true;
/*     */ 
/* 250 */       for (int i = 0; i < v.size(); ++i)
/*     */       {
/* 252 */         String fname = (String)v.elementAt(i);
/* 253 */         if (!tempfile.equals(fname))
/*     */           continue;
/* 255 */         isUnique = false;
/* 256 */         break;
/*     */       }
/*     */ 
/* 260 */       if ((!isUnique) || (!tempExt.equals(ext)))
/*     */         continue;
/* 262 */       v.addElement(tempfile);
/*     */     }
/*     */ 
/* 267 */     if (v.size() > 0)
/*     */     {
/* 269 */       this.m_fileNameChoice.initChoiceList(v);
/* 270 */       this.m_fileNameChoice.select((String)v.elementAt(0));
/*     */     }
/*     */     else
/*     */     {
/* 274 */       v = null;
/* 275 */       this.m_fileNameChoice.initChoiceList(v);
/* 276 */       this.m_fileNameChoice.removeAllItems();
/*     */ 
/* 278 */       String filename = this.m_component.getDefaultFileName(cwType, mergeTable);
/* 279 */       if (filename == null)
/*     */       {
/* 281 */         filename = "";
/*     */       }
/* 283 */       this.m_fileNameChoice.setText(filename);
/*     */     }
/* 285 */     this.m_helper.m_props.put("filename", this.m_fileNameChoice.getText());
/*     */   }
/*     */ 
/*     */   public boolean validateEntries()
/*     */   {
/* 291 */     boolean validate = StringUtils.convertToBool(this.m_helper.m_props.getProperty("validateEntries"), true);
/*     */ 
/* 293 */     if ((!super.validateEntries()) || (!validate))
/*     */     {
/* 295 */       return false;
/*     */     }
/*     */ 
/* 298 */     boolean retVal = true;
/*     */     try
/*     */     {
/* 303 */       String filename = this.m_helper.m_props.getProperty("filename");
/* 304 */       String isAppend = "false";
/* 305 */       String isUnique = "true";
/*     */ 
/* 307 */       if (!this.m_component.isNameUnique(filename, false))
/*     */       {
/* 309 */         isUnique = "false";
/*     */       }
/*     */ 
/* 312 */       if (FileUtils.checkFile(FileUtils.directorySlashes(this.m_component.m_absCompDir) + filename, true, false) != -16)
/*     */       {
/* 315 */         isAppend = "true";
/*     */       }
/*     */ 
/* 318 */       this.m_helper.m_props.put("isUnique", isUnique);
/* 319 */       this.m_helper.m_props.put("isAppend", isAppend);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 323 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/* 324 */       retVal = false;
/*     */     }
/*     */ 
/* 327 */     return retVal;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 335 */     String name = exchange.m_compName;
/* 336 */     String val = exchange.m_compValue;
/* 337 */     IdcMessage errMsg = null;
/* 338 */     if (name.equals("filename"))
/*     */     {
/* 340 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelFileName", new Object[0]), false, true);
/*     */     }
/* 342 */     else if (name.equals("loadOrder"))
/*     */     {
/* 345 */       if ((val == null) || (val.length() == 0))
/*     */       {
/* 347 */         errMsg = IdcMessageFactory.lc("csCompWizLoadOrderReq", new Object[0]);
/*     */       }
/*     */ 
/* 351 */       if ((errMsg == null) && (Validation.checkInteger(val) != 0))
/*     */       {
/* 353 */         errMsg = IdcMessageFactory.lc("csCompWizLoadOrderReqNumber", new Object[0]);
/*     */       }
/*     */ 
/* 357 */       if ((errMsg == null) && (val.charAt(0) == '-'))
/*     */       {
/* 359 */         errMsg = IdcMessageFactory.lc("csCompWizLoadOrderReqPositive", new Object[0]);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 364 */       return super.validateComponentValue(exchange);
/*     */     }
/*     */ 
/* 367 */     if (errMsg != null)
/*     */     {
/* 369 */       exchange.m_errorMessage = errMsg;
/* 370 */       return false;
/*     */     }
/* 372 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 377 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.EditResourceInfoPanel
 * JD-Core Version:    0.5.4
 */