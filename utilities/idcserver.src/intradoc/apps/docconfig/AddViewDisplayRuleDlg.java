/*    */ package intradoc.apps.docconfig;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.SystemInterface;
/*    */ import intradoc.gui.CustomText;
/*    */ import intradoc.gui.DialogCallback;
/*    */ import intradoc.gui.DialogHelper;
/*    */ import intradoc.gui.GridBagHelper;
/*    */ import intradoc.gui.iwt.ComboChoice;
/*    */ import java.awt.event.ActionEvent;
/*    */ import java.util.Properties;
/*    */ import java.util.Vector;
/*    */ import javax.swing.JPanel;
/*    */ 
/*    */ public class AddViewDisplayRuleDlg
/*    */ {
/* 39 */   protected SystemInterface m_systemInterface = null;
/* 40 */   protected ExecutionContext m_context = null;
/* 41 */   protected DialogHelper m_helper = null;
/* 42 */   protected String m_helpPage = null;
/*    */ 
/*    */   public AddViewDisplayRuleDlg(SystemInterface sys, String title, String helpPage)
/*    */   {
/* 46 */     this.m_systemInterface = sys;
/* 47 */     this.m_context = sys.getExecutionContext();
/* 48 */     this.m_helper = new DialogHelper(sys, title, true);
/* 49 */     this.m_helpPage = helpPage;
/*    */   }
/*    */ 
/*    */   public int init(Properties props, Vector expressionList)
/*    */   {
/* 54 */     this.m_helper.m_props = props;
/*    */ 
/* 57 */     DialogCallback okCallback = new DialogCallback()
/*    */     {
/*    */       public boolean handleDialogEvent(ActionEvent e)
/*    */       {
/* 62 */         return true;
/*    */       }
/*    */     };
/* 65 */     okCallback.m_dlgHelper = this.m_helper;
/*    */ 
/* 67 */     initUI(okCallback, expressionList);
/*    */ 
/* 69 */     return this.m_helper.prompt();
/*    */   }
/*    */ 
/*    */   protected void initUI(DialogCallback okCallback, Vector expressionList)
/*    */   {
/* 74 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*    */ 
/* 77 */     String locale = this.m_helper.m_props.getProperty("schLocale");
/* 78 */     String label = LocaleResources.getString("apSchDisplayRuleDescription", this.m_context, locale);
/*    */ 
/* 80 */     CustomText cmp = new CustomText(label);
/* 81 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 82 */     this.m_helper.addComponent(mainPanel, cmp);
/*    */ 
/* 84 */     ComboChoice choice = new ComboChoice();
/* 85 */     choice.initChoiceList(expressionList);
/*    */ 
/* 87 */     this.m_helper.addExchangeComponent(mainPanel, choice, "schDisplayRule");
/*    */   }
/*    */ 
/*    */   protected Properties getProperties()
/*    */   {
/* 92 */     return this.m_helper.m_props;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 97 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AddViewDisplayRuleDlg
 * JD-Core Version:    0.5.4
 */