/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditUrlReferenceDlg extends EditLinkBaseDlg
/*     */ {
/*     */   public boolean m_isLocal;
/*     */   public PageData m_pageData;
/*     */   JPanel m_pageChoicesPanel;
/*     */   EditPageHelper m_editPageHelper;
/*     */ 
/*     */   public EditUrlReferenceDlg(boolean isLocal, PageData pageData, SystemInterface sysInterface, PageManagerContext pageServices, Vector linkInfo, boolean isNew, String helpPage)
/*     */   {
/*  64 */     super(sysInterface, pageServices, linkInfo, isNew, helpPage);
/*     */ 
/*  66 */     this.m_isLocal = isLocal;
/*  67 */     this.m_pageData = pageData;
/*  68 */     this.m_title = ((this.m_isLocal) ? LocaleResources.getString("apLabelEditLocalPageLink", this.m_ctx) : LocaleResources.getString("apLabelEditExternalUrl", this.m_ctx));
/*     */ 
/*  70 */     this.m_pageChoicesPanel = null;
/*  71 */     this.m_editPageHelper = null;
/*     */   }
/*     */ 
/*     */   public boolean initLinkFields(JPanel top)
/*     */   {
/*  78 */     Component urlComp = null;
/*  79 */     if (this.m_isLocal)
/*     */     {
/*  81 */       this.m_pageChoicesPanel = new PanePanel();
/*  82 */       this.m_helper.m_gridHelper.useGridBag(this.m_pageChoicesPanel);
/*  83 */       urlComp = this.m_pageChoicesPanel;
/*     */     }
/*     */     else
/*     */     {
/*  88 */       urlComp = new CustomTextField(50);
/*     */     }
/*     */ 
/*  91 */     String urlFieldLabel = (this.m_isLocal) ? LocaleResources.getString("apLabelPageName", this.m_ctx) : LocaleResources.getString("apLabelExternalUrl", this.m_ctx);
/*     */ 
/*  93 */     String compId = "LinkData";
/*  94 */     boolean isLast = !this.m_isLocal;
/*  95 */     this.m_helper.addLabelFieldPairEx(top, urlFieldLabel, urlComp, compId, isLast);
/*     */ 
/*  97 */     if (this.m_isLocal)
/*     */     {
/* 100 */       addPageChoiceList();
/*     */ 
/* 103 */       JButton createPageButton = new JButton(LocaleResources.getString("apDlgButtonCreateNewPage", this.m_ctx));
/*     */ 
/* 105 */       this.m_helper.addComponent(top, createPageButton);
/* 106 */       createPageButton.addActionListener(new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 112 */             if (EditUrlReferenceDlg.this.promptNewPage() == true)
/*     */             {
/* 114 */               EditUrlReferenceDlg.this.addPageChoiceList();
/* 115 */               String pageName = (String)EditUrlReferenceDlg.this.m_linkInfo.elementAt(1);
/* 116 */               if (pageName != null)
/*     */               {
/* 118 */                 EditUrlReferenceDlg.this.m_helper.m_exchange.setComponentValue("LinkData", pageName);
/*     */               }
/* 120 */               EditUrlReferenceDlg.this.validate();
/*     */             }
/*     */           }
/*     */           catch (ServiceException err)
/*     */           {
/* 125 */             EditUrlReferenceDlg.this.reportError(err, IdcMessageFactory.lc("apUnableToCreateNewPage", new Object[0]));
/*     */           }
/*     */         }
/*     */ 
/*     */       });
/*     */     }
/*     */ 
/* 132 */     return true;
/*     */   }
/*     */ 
/*     */   public void addPageChoiceList()
/*     */   {
/* 137 */     Component dispComp = null;
/* 138 */     Vector pageList = this.m_pageServices.getPageList();
/* 139 */     JComboBox pageNames = new CustomChoice();
/* 140 */     String curChildId = (String)this.m_linkInfo.elementAt(1);
/*     */ 
/* 143 */     this.m_pageChoicesPanel.removeAll();
/*     */ 
/* 146 */     Hashtable myTree = new Hashtable();
/* 147 */     String marker = "1";
/*     */ 
/* 149 */     PageData pdata = this.m_pageData;
/*     */     do
/*     */     {
/* 152 */       myTree.put(pdata.m_pageId, marker);
/* 153 */       if (pdata.m_parent == null) {
/*     */         break;
/*     */       }
/*     */ 
/* 157 */       pdata = pdata.m_parent;
/* 158 */       if (pdata == null) {
/*     */         break;
/*     */       }
/*     */     }
/* 162 */     while (myTree.get(pdata.m_pageId) == null);
/*     */ 
/* 169 */     int npages = pageList.size();
/* 170 */     int count = 0;
/* 171 */     for (int i = 0; i < npages; ++i)
/*     */     {
/* 173 */       pdata = (PageData)pageList.elementAt(i);
/* 174 */       boolean hasOtherParent = false;
/* 175 */       if ((pdata.m_parent != null) && ((
/* 177 */         (pdata.m_parent != this.m_pageData) || (curChildId == null) || (!curChildId.equals(pdata.m_pageId)))))
/*     */       {
/* 180 */         hasOtherParent = true;
/*     */       }
/*     */ 
/* 183 */       if ((hasOtherParent) || (myTree.get(pdata.m_pageId) != null))
/*     */         continue;
/* 185 */       pageNames.addItem(pdata.m_pageId);
/* 186 */       ++count;
/*     */     }
/*     */ 
/* 190 */     if (count > 1)
/*     */     {
/* 192 */       dispComp = pageNames;
/*     */     }
/*     */     else
/*     */     {
/* 196 */       String showString = LocaleResources.getString("apNoAvailablePages", this.m_ctx);
/* 197 */       if (count == 1)
/*     */       {
/* 199 */         showString = (String)pageNames.getItemAt(0);
/*     */       }
/* 201 */       dispComp = new CustomLabel(showString);
/* 202 */       this.m_linkInfo.setElementAt(showString, 1);
/*     */     }
/* 204 */     this.m_helper.m_ok.setEnabled(count > 0);
/*     */ 
/* 207 */     if (!this.m_helper.m_exchange.replaceComponent("LinkData", dispComp))
/*     */     {
/* 209 */       this.m_helper.m_exchange.addComponent("LinkData", dispComp, null);
/*     */     }
/* 211 */     this.m_helper.addComponent(this.m_pageChoicesPanel, dispComp);
/*     */   }
/*     */ 
/*     */   public boolean promptNewPage() throws ServiceException
/*     */   {
/* 216 */     PageData pageData = new PageData();
/* 217 */     Properties localData = pageData.m_binder.getLocalData();
/*     */ 
/* 220 */     String curTitle = this.m_helper.m_exchange.getComponentValue("LinkTitle");
/* 221 */     if ((curTitle != null) && (curTitle.length() > 0))
/*     */     {
/* 223 */       localData.put("PageTitle", curTitle);
/*     */     }
/* 225 */     String curGroup = this.m_pageData.m_binder.getLocal("dSecurityGroup");
/* 226 */     if (curGroup == null)
/*     */     {
/* 228 */       curGroup = "Public";
/*     */     }
/* 230 */     localData.put("dSecurityGroup", curGroup);
/*     */ 
/* 232 */     if (this.m_editPageHelper.promptNewPage(this.m_sysInterface, pageData, curGroup, this.m_pageServices))
/*     */     {
/* 236 */       if ((curTitle == null) || (curTitle.length() == 0))
/*     */       {
/* 238 */         curTitle = localData.getProperty("PageTitle");
/* 239 */         this.m_helper.m_exchange.setComponentValue("LinkTitle", curTitle);
/*     */       }
/*     */ 
/* 242 */       String pageName = localData.getProperty("PageName");
/* 243 */       this.m_linkInfo.setElementAt(pageName, 1);
/* 244 */       return true;
/*     */     }
/* 246 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 251 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditUrlReferenceDlg
 * JD-Core Version:    0.5.4
 */