/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditFormatsDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_action;
/*     */   protected String m_helpPage;
/*  68 */   protected DataBinder m_binder = null;
/*  69 */   protected ComponentValidator m_cmpValidator = null;
/*  70 */   protected ExecutionContext m_ctx = null;
/*     */   protected String[] m_conversions;
/*     */ 
/*     */   public EditFormatsDlg(SystemInterface sys, String title, ResultSet rset, String helpPage)
/*     */   {
/*  76 */     this.m_helper = new DialogHelper(sys, title, true);
/*  77 */     this.m_ctx = sys.getExecutionContext();
/*  78 */     this.m_systemInterface = sys;
/*  79 */     this.m_helpPage = helpPage;
/*  80 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */   }
/*     */ 
/*     */   public void init(Properties data)
/*     */     throws DataException
/*     */   {
/*  86 */     DataResultSet drset = SharedObjects.getTable("DocumentConversions");
/*     */ 
/*  89 */     Vector v = new IdcVector();
/*  90 */     v.addElement("PassThru");
/*     */ 
/*  92 */     if (drset != null)
/*     */     {
/*  94 */       String[][] table = ResultSetUtils.createStringTable(drset, new String[] { "drConversion", "drIsEnabledFlag" });
/*     */ 
/*  96 */       for (int i = 0; i < table.length; ++i)
/*     */       {
/*  98 */         String strIsEnabled = table[i][1];
/*  99 */         boolean isEnabled = StringUtils.convertToBool(strIsEnabled, false);
/* 100 */         if (isEnabled != true)
/*     */           continue;
/* 102 */         v.addElement(table[i][0]);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 108 */     this.m_conversions = new String[v.size()];
/* 109 */     v.copyInto(this.m_conversions);
/*     */     Component formatTxt;
/*     */     Component formatTxt;
/* 112 */     if (data != null)
/*     */     {
/* 114 */       formatTxt = new CustomLabel(data.getProperty("dFormat"));
/*     */     }
/*     */     else
/*     */     {
/* 118 */       formatTxt = new CustomTextField(20);
/*     */     }
/*     */ 
/* 121 */     JComboBox conversionChoice = new CustomChoice();
/* 122 */     int size = this.m_conversions.length;
/* 123 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 125 */       conversionChoice.addItem(this.m_conversions[i]);
/*     */     }
/*     */ 
/* 128 */     JTextField descriptionTxt = new CustomTextField(30);
/*     */ 
/* 130 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 137 */           Properties localData = this.m_dlgHelper.m_props;
/* 138 */           EditFormatsDlg.this.m_binder = new DataBinder();
/* 139 */           EditFormatsDlg.this.m_binder.setLocalData(localData);
/* 140 */           AppLauncher.executeService(EditFormatsDlg.this.m_action, EditFormatsDlg.this.m_binder);
/* 141 */           return true;
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 145 */           MessageBox.reportError(EditFormatsDlg.this.m_systemInterface, exp);
/*     */         }
/* 147 */         return false;
/*     */       }
/*     */     };
/* 150 */     okCallback.m_dlgHelper = this.m_helper;
/* 151 */     JPanel wrapper = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/* 153 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/*     */ 
/* 155 */     JPanel mainPanel = new PanePanel();
/* 156 */     this.m_helper.addComponent(wrapper, mainPanel);
/* 157 */     this.m_helper.makePanelGridBag(mainPanel, 2);
/*     */ 
/* 159 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 0);
/* 160 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelFormat", this.m_ctx), formatTxt, "dFormat");
/*     */ 
/* 162 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 0, 0, 0);
/* 163 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelConversion", this.m_ctx), conversionChoice, "dConversion");
/* 164 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelDescription", this.m_ctx), descriptionTxt, "dDescription");
/*     */ 
/* 167 */     if (data != null)
/*     */     {
/* 172 */       String cmpName = data.getProperty("idcComponentName");
/* 173 */       String overrideStatus = data.getProperty("overrideStatus");
/* 174 */       if ((cmpName.length() != 0) && (overrideStatus.length() == 0))
/*     */       {
/* 176 */         String enabled = data.getProperty("dIsEnabled");
/* 177 */         this.m_helper.m_props.put("dIsEnabled", (StringUtils.convertToBool(enabled, false)) ? "1" : "0");
/*     */ 
/* 179 */         this.m_action = "ADD_DOCFORMAT";
/*     */       }
/*     */       else
/*     */       {
/* 183 */         this.m_action = "EDIT_DOCFORMAT";
/*     */       }
/* 185 */       String selectedChoice = data.getProperty("dConversion");
/* 186 */       for (int i = 0; i < this.m_conversions.length; ++i)
/*     */       {
/* 188 */         if (!this.m_conversions[i].equalsIgnoreCase(selectedChoice))
/*     */           continue;
/* 190 */         conversionChoice.setSelectedIndex(i);
/* 191 */         break;
/*     */       }
/*     */ 
/* 194 */       descriptionTxt.setText(data.getProperty("dDescription"));
/* 195 */       this.m_helper.retrieveComponentValues();
/* 196 */       this.m_helper.m_props.put("dIsEnabled", data.get("dIsEnabled"));
/*     */     }
/*     */     else
/*     */     {
/* 200 */       this.m_action = "ADD_DOCFORMAT";
/* 201 */       this.m_helper.m_props.put("dIsEnabled", "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 207 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 217 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 218 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 223 */     String name = exchange.m_compName;
/* 224 */     String val = exchange.m_compValue;
/*     */ 
/* 226 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 228 */     int errType = 0;
/* 229 */     if (name.equals("dFormat"))
/*     */     {
/* 231 */       errType = 1;
/*     */     }
/* 233 */     else if (name.equals("dDescription"))
/*     */     {
/* 235 */       errType = 2;
/*     */     }
/*     */ 
/* 238 */     if (val.length() > maxLength)
/*     */     {
/* 240 */       switch (errType)
/*     */       {
/*     */       case 0:
/* 243 */         exchange.m_errorMessage = IdcMessageFactory.lc("apFieldNameExceedsMaxLength", new Object[] { name, Integer.valueOf(maxLength) });
/*     */ 
/* 245 */         break;
/*     */       case 1:
/* 247 */         exchange.m_errorMessage = IdcMessageFactory.lc("apFileFormatExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */ 
/* 249 */         break;
/*     */       default:
/* 251 */         exchange.m_errorMessage = IdcMessageFactory.lc("apFormatDescriptionExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */ 
/* 255 */       return false;
/*     */     }
/* 257 */     return true;
/*     */   }
/*     */ 
/*     */   public String getFormat()
/*     */   {
/* 262 */     return this.m_helper.m_props.getProperty("dFormat");
/*     */   }
/*     */ 
/*     */   public DataBinder getBinder()
/*     */   {
/* 267 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 272 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80076 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditFormatsDlg
 * JD-Core Version:    0.5.4
 */