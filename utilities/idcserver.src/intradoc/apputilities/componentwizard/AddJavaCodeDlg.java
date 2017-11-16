/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddJavaCodeDlg extends CWizardBaseDlg
/*     */ {
/*  56 */   protected boolean m_isFilter = false;
/*     */ 
/*  58 */   protected Vector m_typesOrClassNamesList = null;
/*  59 */   protected Vector m_descriptions = null;
/*     */ 
/*  62 */   protected DisplayChoice m_choices = null;
/*  63 */   protected JButton m_infoBtn = null;
/*     */ 
/*     */   public AddJavaCodeDlg(SystemInterface sys, String title, String helpPage, IntradocComponent comp)
/*     */     throws ServiceException
/*     */   {
/*  68 */     super(sys, title, helpPage);
/*  69 */     this.m_component = comp;
/*     */   }
/*     */ 
/*     */   public void init(boolean isFilter)
/*     */   {
/*  74 */     this.m_isFilter = isFilter;
/*  75 */     loadTypesOrClassNamesList();
/*     */ 
/*  77 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  82 */         Properties props = AddJavaCodeDlg.this.m_helper.m_props;
/*     */         try
/*     */         {
/*  85 */           AddJavaCodeDlg.this.formatPackageAndFilePath(props);
/*  86 */           if (AddJavaCodeDlg.this.m_isFilter)
/*     */           {
/*  88 */             String type = props.getProperty("type");
/*  89 */             String loadOrder = props.getProperty("loadOrder");
/*  90 */             if ((loadOrder == null) || (loadOrder.length() == 0))
/*     */             {
/*  92 */               props.put("loadOrder", "1");
/*     */             }
/*  94 */             if (AddJavaCodeDlg.this.m_component.isTypeDefined(type, true))
/*     */             {
/*  96 */               throw new ServiceException(LocaleUtils.encodeMessage("csCompWizFilterDefined", null, type));
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 103 */             String classname = props.getProperty("classname");
/* 104 */             if (AddJavaCodeDlg.this.m_component.isTypeDefined(classname, false))
/*     */             {
/* 106 */               throw new ServiceException(LocaleUtils.encodeMessage("csCompWizClassDefined", null, classname));
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 111 */           AddJavaCodeDlg.this.m_component.addJavaCode(props, AddJavaCodeDlg.this.m_isFilter, true, false);
/*     */ 
/* 114 */           CWizardGuiUtils.doMessage(AddJavaCodeDlg.this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizNewFileInfo", new Object[] { props.getProperty("filename") }), 1);
/*     */ 
/* 117 */           return true;
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 121 */           IdcMessage errMsg = null;
/*     */ 
/* 123 */           if (AddJavaCodeDlg.this.m_isFilter)
/*     */           {
/* 125 */             errMsg = IdcMessageFactory.lc("csCompWizAddError", new Object[] { props.getProperty("type") });
/*     */           }
/*     */           else
/*     */           {
/* 129 */             errMsg = IdcMessageFactory.lc("csCompWizAddError", new Object[] { props.getProperty("classname") });
/*     */           }
/*     */ 
/* 132 */           CWizardGuiUtils.reportError(AddJavaCodeDlg.this.m_systemInterface, exp, errMsg);
/* 133 */         }return false;
/*     */       }
/*     */     };
/* 138 */     JPanel mainPanel = getDialogHelper().initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 141 */     initUI(mainPanel);
/*     */   }
/*     */ 
/*     */   public String getComponentName()
/*     */   {
/* 146 */     if (this.m_isFilter)
/*     */     {
/* 148 */       return this.m_helper.m_props.getProperty("type");
/*     */     }
/*     */ 
/* 151 */     return this.m_helper.m_props.getProperty("classname");
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel)
/*     */   {
/* 157 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 159 */     this.m_choices = new DisplayChoice();
/* 160 */     this.m_choices.init(this.m_typesOrClassNamesList);
/*     */ 
/* 162 */     this.m_infoBtn = new JButton(LocaleResources.getString("csCompWizLabelInfoBtn", null));
/* 163 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 167 */         AddJavaCodeDlg.this.showDescription();
/*     */       }
/*     */     };
/* 170 */     this.m_infoBtn.addActionListener(listener);
/*     */ 
/* 172 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 5);
/* 173 */     if (this.m_isFilter)
/*     */     {
/* 175 */       this.m_helper.addLabelFieldPairEx(mainPanel, LocaleResources.getString("csCompWizLabelFilterType", null), this.m_choices, "type", false);
/*     */ 
/* 177 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 178 */       this.m_helper.addComponent(mainPanel, this.m_infoBtn);
/* 179 */       this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizLabelSpecialization2", null), new CustomTextField(50), "location");
/*     */ 
/* 182 */       this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizLabelLoadOrder2", null), new CustomTextField(50), "loadOrder");
/*     */ 
/* 185 */       this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizLabelParam2", null), new CustomTextField(50), "parameter");
/*     */ 
/* 187 */       this.m_helper.m_props.put("loadOrder", "1");
/*     */     }
/*     */     else
/*     */     {
/* 191 */       this.m_helper.addLabelFieldPairEx(mainPanel, LocaleResources.getString("csCompWizLabelClassName2", null), this.m_choices, "classname", false);
/*     */ 
/* 193 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 194 */       this.m_helper.addComponent(mainPanel, this.m_infoBtn);
/* 195 */       this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizLabelSpecialization2", null), new CustomTextField(50), "location");
/*     */     }
/*     */ 
/* 199 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected void loadTypesOrClassNamesList()
/*     */   {
/*     */     try
/*     */     {
/* 206 */       String tablename = "ClassNames";
/* 207 */       String errMsg = "!csCompWizClassNamesTableError";
/* 208 */       String type = "classname";
/*     */ 
/* 210 */       if (this.m_isFilter)
/*     */       {
/* 212 */         type = "type";
/* 213 */         tablename = "FilterTypes";
/* 214 */         errMsg = "!csCompWizFilterTypesTableError";
/*     */       }
/*     */ 
/* 217 */       DataResultSet drset = SharedObjects.getTable(tablename);
/*     */ 
/* 219 */       if (drset == null)
/*     */       {
/* 221 */         throw new ServiceException(errMsg);
/*     */       }
/*     */ 
/* 224 */       this.m_typesOrClassNamesList = new IdcVector();
/* 225 */       this.m_descriptions = new IdcVector();
/*     */ 
/* 227 */       String[] keys = { type, "description" };
/* 228 */       String[][] table = ResultSetUtils.createStringTable(drset, keys);
/*     */ 
/* 230 */       for (int i = 0; i < table.length; ++i)
/*     */       {
/* 232 */         String typeOrClassName = table[i][0];
/* 233 */         String desc = table[i][1];
/* 234 */         this.m_typesOrClassNamesList.addElement(typeOrClassName);
/* 235 */         this.m_descriptions.addElement(desc);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 240 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void showDescription()
/*     */   {
/* 246 */     IdcMessage info = null;
/*     */ 
/* 248 */     int index = this.m_choices.getSelectedIndex();
/* 249 */     if (index < 0)
/*     */     {
/* 251 */       if (this.m_isFilter)
/*     */       {
/* 253 */         info = IdcMessageFactory.lc("csCompWizFilterTypesDescError", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 257 */         info = IdcMessageFactory.lc("csCompWizClassNamesDescError", new Object[0]);
/*     */       }
/*     */ 
/* 260 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, info);
/* 261 */       return;
/*     */     }
/*     */ 
/* 264 */     String type = this.m_choices.getSelectedInternalValue();
/* 265 */     String text = (String)this.m_descriptions.elementAt(index);
/* 266 */     IdcMessage infoMessage = IdcMessageFactory.lc();
/* 267 */     infoMessage.m_msgLocalized = text;
/* 268 */     CWizardGuiUtils.doMessage(this.m_systemInterface, IdcMessageFactory.lc("csCompWizTypeInfoDesc", new Object[] { type }), infoMessage, 1);
/*     */   }
/*     */ 
/*     */   protected void formatPackageAndFilePath(Properties props)
/*     */     throws ServiceException
/*     */   {
/* 274 */     String location = props.getProperty("location");
/* 275 */     String pkg = null;
/* 276 */     String filePath = null;
/*     */ 
/* 278 */     String intradocDir = FileUtils.directorySlashes(SharedObjects.getEnvironmentValue("IntradocDir"));
/*     */ 
/* 280 */     int index = location.lastIndexOf(46);
/* 281 */     if (index < 0)
/*     */     {
/* 283 */       String name = CWizardUtils.removeSpaces(this.m_component.m_name);
/* 284 */       pkg = "custom." + name + "." + location;
/*     */ 
/* 286 */       filePath = intradocDir + "classes/" + name + "/" + location + ".java";
/* 287 */       props.put("location", pkg);
/*     */     }
/*     */     else
/*     */     {
/* 291 */       filePath = CWizardUtils.covertPackageToFilePath(intradocDir + "classes/", location);
/*     */     }
/*     */ 
/* 295 */     if (FileUtils.checkFile(filePath, true, false) != -16)
/*     */     {
/* 297 */       throw new ServiceException(LocaleUtils.encodeMessage("syFileExists", null, filePath));
/*     */     }
/*     */ 
/* 300 */     props.put("filename", filePath);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 311 */     String name = exchange.m_compName;
/* 312 */     String val = exchange.m_compValue;
/*     */ 
/* 314 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 320 */     IdcMessage errMsg = null;
/*     */ 
/* 322 */     if (name.equals("location"))
/*     */     {
/* 324 */       if ((val == null) || (val.length() == 0))
/*     */       {
/* 326 */         errMsg = IdcMessageFactory.lc("csCompWizNeedSpecialization", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 330 */         errMsg = Validation.checkUrlFileSegmentForDB(val, "csCompWizSpecializationErrorStub", 0, null);
/*     */       }
/*     */     }
/*     */ 
/* 334 */     if ((name.equals("loadOrder")) && 
/* 336 */       (val != null) && (val.length() > 0) && 
/* 338 */       (Validation.checkInteger(val) != 0))
/*     */     {
/* 340 */       errMsg = IdcMessageFactory.lc("csCompWizLoadOrderError", new Object[0]);
/*     */     }
/*     */ 
/* 344 */     if (errMsg != null)
/*     */     {
/* 346 */       exchange.m_errorMessage = errMsg;
/* 347 */       return false;
/*     */     }
/* 349 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 354 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79062 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.AddJavaCodeDlg
 * JD-Core Version:    0.5.4
 */