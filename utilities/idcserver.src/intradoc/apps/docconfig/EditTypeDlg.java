/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.GuiUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.ImageLabel;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Image;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditTypeDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_action;
/*     */   protected DataBinder m_binder;
/*     */   protected String m_gifDir;
/*     */   protected String m_helpPage;
/*     */   protected ComponentValidator m_cmpValidator;
/*  77 */   protected ExecutionContext m_ctx = null;
/*     */ 
/*     */   public EditTypeDlg(SystemInterface sys, String title, ResultSet rset, String helpPage)
/*     */   {
/*  81 */     this.m_helper = new DialogHelper(sys, title, true);
/*  82 */     this.m_ctx = sys.getExecutionContext();
/*  83 */     this.m_systemInterface = sys;
/*  84 */     this.m_gifDir = SharedLoader.getDocGifSubDirectory();
/*  85 */     this.m_helpPage = helpPage;
/*  86 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */   }
/*     */ 
/*     */   public boolean init(Properties data)
/*     */   {
/*  92 */     String docType = null;
/*     */     Component name;
/*     */     Component name;
/*  93 */     if (data != null)
/*     */     {
/*  95 */       docType = data.getProperty("dDocType");
/*  96 */       name = new CustomLabel(docType);
/*     */     }
/*     */     else
/*     */     {
/* 100 */       name = new CustomTextField(20);
/*     */     }
/*     */ 
/* 103 */     JTextField descriptionTxt = new CustomTextField(20);
/*     */     try
/*     */     {
/* 108 */       DataBinder binder = new DataBinder();
/* 109 */       Properties localData = new Properties();
/* 110 */       localData.put("directoryID", "docgifs");
/* 111 */       localData.put("fileFilter", "*.gif|*.png");
/* 112 */       localData.put("fileListName", "docgifs");
/* 113 */       binder.setLocalData(localData);
/* 114 */       AppLauncher.executeService("GET_FILELIST", binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 119 */       MessageBox.reportError(this.m_systemInterface, e);
/* 120 */       return false;
/*     */     }
/*     */ 
/* 123 */     JComboBox gifChoice = new CustomChoice();
/* 124 */     Vector images = SharedObjects.getOptList("docgifs");
/* 125 */     int size = images.size();
/* 126 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 128 */       gifChoice.addItem(images.elementAt(i));
/*     */     }
/*     */ 
/* 131 */     ImageLabel gifLabel = new ImageLabel(40, 40, true);
/* 132 */     JPanel gifPanel = new CustomPanel();
/* 133 */     gifPanel.setLayout(new BorderLayout());
/* 134 */     gifPanel.add("Center", gifLabel);
/*     */ 
/* 136 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 143 */           Properties localData = this.m_dlgHelper.m_props;
/* 144 */           EditTypeDlg.this.m_binder = new DataBinder();
/* 145 */           EditTypeDlg.this.m_binder.setLocalData(localData);
/* 146 */           AppLauncher.executeService(EditTypeDlg.this.m_action, EditTypeDlg.this.m_binder);
/* 147 */           return true;
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 151 */           MessageBox.reportError(EditTypeDlg.this.m_systemInterface, exp);
/*     */         }
/* 153 */         return false;
/*     */       }
/*     */     };
/* 156 */     okCallback.m_dlgHelper = this.m_helper;
/* 157 */     JPanel wrapper = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/* 159 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/* 160 */     JPanel mainPanel = new PanePanel();
/* 161 */     this.m_helper.addComponent(wrapper, mainPanel);
/*     */ 
/* 163 */     this.m_helper.makePanelGridBag(mainPanel, 2);
/*     */ 
/* 165 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 0);
/* 166 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelName", this.m_ctx), name, "dDocType");
/*     */ 
/* 169 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 0, 0, 0);
/* 170 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelDescription", this.m_ctx), descriptionTxt, "dDescription");
/*     */ 
/* 172 */     this.m_helper.addLabelFieldPairEx(mainPanel, this.m_systemInterface.localizeCaption("apLabelImage"), gifChoice, "dGif", false);
/*     */ 
/* 175 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 176 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 10, 0, 0);
/* 177 */     this.m_helper.addComponent(mainPanel, gifPanel);
/*     */ 
/* 179 */     ItemListener gifListener = new ItemListener(gifChoice, gifLabel)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 183 */         int state = e.getStateChange();
/* 184 */         switch (state)
/*     */         {
/*     */         case 1:
/* 187 */           String selectedImage = (String)this.val$gifChoice.getSelectedItem();
/* 188 */           Image logo = GuiUtils.getAppImage(EditTypeDlg.this.m_gifDir + selectedImage);
/* 189 */           this.val$gifLabel.setImage(logo);
/*     */         }
/*     */       }
/*     */     };
/* 194 */     gifChoice.addItemListener(gifListener);
/*     */ 
/* 197 */     String gifName = null;
/* 198 */     if (data != null)
/*     */     {
/* 200 */       this.m_action = "EDIT_DOCTYPE";
/* 201 */       String desc = data.getProperty("dDescription");
/* 202 */       descriptionTxt.setText(data.getProperty("dDescription"));
/* 203 */       String gif = data.getProperty("dGif");
/* 204 */       gifChoice.setSelectedItem(gif);
/* 205 */       gifName = (String)gifChoice.getSelectedItem();
/*     */ 
/* 208 */       this.m_helper.m_props.put("dDescription", desc);
/* 209 */       this.m_helper.m_props.put("dGif", gif);
/* 210 */       this.m_helper.m_props.put("dDocType", docType);
/*     */     }
/*     */     else
/*     */     {
/* 214 */       this.m_action = "ADD_DOCTYPE";
/* 215 */       if (gifChoice.getItemCount() > 0)
/*     */       {
/* 218 */         gifName = (String)gifChoice.getItemAt(0);
/*     */       }
/*     */     }
/* 221 */     if (gifName != null)
/*     */     {
/* 223 */       Image logo = GuiUtils.getAppImage(this.m_gifDir + gifName);
/* 224 */       gifLabel.setImage(logo);
/*     */     }
/*     */ 
/* 227 */     return true;
/*     */   }
/*     */ 
/*     */   public int prompt(Properties props)
/*     */   {
/* 232 */     if (!init(props))
/*     */     {
/* 234 */       return 0;
/*     */     }
/*     */ 
/* 237 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String getDocType()
/*     */   {
/* 242 */     return this.m_helper.m_props.getProperty("dDocType");
/*     */   }
/*     */ 
/*     */   public DataBinder getBinder()
/*     */   {
/* 247 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 256 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 257 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 262 */     String name = exchange.m_compName;
/* 263 */     String val = exchange.m_compValue;
/*     */ 
/* 266 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 268 */     IdcMessage errMsg = null;
/* 269 */     if (name.equals("dDocType"))
/*     */     {
/* 271 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apContenTypeName", maxLength, null);
/*     */     }
/* 273 */     else if ((name.equals("dDescription")) && 
/* 275 */       (val.length() > maxLength))
/*     */     {
/* 277 */       errMsg = IdcMessageFactory.lc("apContentTypeExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */     }
/*     */ 
/* 281 */     if (errMsg != null)
/*     */     {
/* 283 */       exchange.m_errorMessage = errMsg;
/* 284 */       return false;
/*     */     }
/* 286 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 291 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80493 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditTypeDlg
 * JD-Core Version:    0.5.4
 */