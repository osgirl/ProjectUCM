/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.util.Map;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public abstract class DocConfigPanel extends PanePanel
/*     */   implements Observer, PromptHandler, FocusListener, SharedContext
/*     */ {
/*     */   protected ContainerHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_subject;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected DocConfigContext m_docContext;
/*     */   protected DocumentLocalizedProfile m_docProfile;
/*     */   protected String m_baseClassName;
/*     */ 
/*     */   public DocConfigPanel()
/*     */   {
/*  39 */     this.m_helper = null;
/*  40 */     this.m_systemInterface = null;
/*  41 */     this.m_subject = null;
/*  42 */     this.m_ctx = null;
/*  43 */     this.m_docContext = null;
/*  44 */     this.m_docProfile = null;
/*  45 */     this.m_baseClassName = null;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys) throws ServiceException {
/*  49 */     initEx(sys, null);
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder) throws ServiceException
/*     */   {
/*  54 */     Properties props = null;
/*  55 */     if (binder != null)
/*     */     {
/*  57 */       props = binder.getLocalData();
/*     */     }
/*     */ 
/*  60 */     this.m_systemInterface = sys;
/*  61 */     this.m_helper = new ContainerHelper();
/*  62 */     this.m_helper.attachToContainer(this, this.m_systemInterface, props);
/*  63 */     this.m_ctx = sys.getExecutionContext();
/*     */ 
/*  65 */     UserData userData = AppLauncher.getUserData();
/*  66 */     this.m_docProfile = new DocumentLocalizedProfile(userData, 1, this.m_ctx);
/*     */ 
/*  68 */     if (this.m_subject == null)
/*     */       return;
/*  70 */     Vector subjects = StringUtils.parseArray(this.m_subject, ',', '^');
/*  71 */     int size = subjects.size();
/*  72 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  74 */       String subject = (String)subjects.elementAt(i);
/*  75 */       AppLauncher.addSubjectObserver(subject, this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setBaseClassName(String name)
/*     */   {
/*  82 */     this.m_baseClassName = name;
/*     */   }
/*     */ 
/*     */   public boolean canExit()
/*     */   {
/*  87 */     return true;
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e)
/*     */   {
/*  92 */     MessageBox.reportError(this.m_systemInterface, e);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, IdcMessage msg)
/*     */   {
/*  97 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void loadComponents()
/*     */   {
/* 107 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String retrieveAndValidate()
/*     */   {
/* 114 */     IdcMessage msg = retrievePanelValuesAndValidate();
/* 115 */     if (msg != null)
/*     */     {
/* 117 */       return LocaleUtils.encodeMessage(msg);
/*     */     }
/* 119 */     return null;
/*     */   }
/*     */ 
/*     */   public IdcMessage retrievePanelValuesAndValidate()
/*     */   {
/* 124 */     boolean success = this.m_helper.retrieveComponentValues();
/* 125 */     if (success)
/*     */     {
/* 127 */       return computeValidationErrorMessage(null);
/*     */     }
/* 129 */     return IdcMessageFactory.lc("apUnableToValidate", new Object[0]);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String validateData()
/*     */   {
/* 141 */     return null;
/*     */   }
/*     */ 
/*     */   public IdcMessage computeValidationErrorMessage(Map options)
/*     */   {
/* 151 */     return null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected String loadInformation()
/*     */   {
/* 162 */     Report.trace("debug", "DocConfigPanel.loadInformation() called.", null);
/* 163 */     return null;
/*     */   }
/*     */ 
/*     */   protected abstract void loadPanelInformation()
/*     */     throws DataException;
/*     */ 
/*     */   protected void loadConfiguration(Properties props)
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void setDocContext(DocConfigContext docContext)
/*     */   {
/* 181 */     this.m_docContext = docContext;
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/*     */     try
/*     */     {
/* 191 */       refreshView();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 195 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeNotify()
/*     */   {
/* 202 */     if (this.m_subject != null)
/*     */     {
/* 204 */       Vector subjects = StringUtils.parseArray(this.m_subject, ',', '^');
/* 205 */       int size = subjects.size();
/* 206 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 208 */         String subject = (String)subjects.elementAt(i);
/* 209 */         AppLauncher.removeSubjectObserver(subject, this);
/*     */       }
/*     */     }
/* 212 */     super.removeNotify();
/*     */   }
/*     */ 
/*     */   public void executeServiceWithCursor(String action, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 218 */     AppLauncher.executeService(action, binder, this.m_systemInterface);
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder binder, boolean isRefresh)
/*     */     throws ServiceException
/*     */   {
/* 227 */     AppLauncher.executeService(action, binder);
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 232 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 240 */     if (!this.m_helper.retrieveComponentValues())
/*     */     {
/* 242 */       return 0;
/*     */     }
/* 244 */     return 1;
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 262 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80220 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocConfigPanel
 * JD-Core Version:    0.5.4
 */