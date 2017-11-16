/*    */ package intradoc.shared.gui;
/*    */ 
/*    */ import intradoc.gui.ContainerHelper;
/*    */ import intradoc.shared.DocumentLocalizedProfile;
/*    */ import intradoc.shared.ViewFieldDef;
/*    */ import java.util.Enumeration;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Properties;
/*    */ import java.util.Vector;
/*    */ import javax.swing.JCheckBox;
/*    */ 
/*    */ public class SchemaView extends BaseView
/*    */ {
/*    */   public SchemaView(ContainerHelper helper, RefreshView refresher, DocumentLocalizedProfile docProfile)
/*    */   {
/* 34 */     super(helper, refresher, docProfile);
/*    */ 
/* 36 */     this.m_customMetaInSeparatePanel = false;
/* 37 */     this.m_isFieldOnlyFilter = false;
/* 38 */     setFilterHelpPage("EditViewValuesFilter");
/*    */   }
/*    */ 
/*    */   public void configureFilter(boolean forceRefresh)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void configureShowColumns(boolean forceRefresh)
/*    */   {
/*    */   }
/*    */ 
/*    */   public Properties populateFromFilter()
/*    */   {
/* 55 */     Properties props = new Properties();
/*    */     Enumeration en;
/* 56 */     if (this.m_useFilterBox.isSelected())
/*    */     {
/* 58 */       for (en = this.m_filterData.elements(); en.hasMoreElements(); )
/*    */       {
/* 60 */         FilterData fd = (FilterData)en.nextElement();
/* 61 */         if (!fd.m_isUsed)
/*    */         {
/*    */           continue;
/*    */         }
/*    */ 
/* 66 */         String fieldName = fd.m_fieldDef.m_name;
/*    */ 
/* 68 */         Vector values = fd.m_values;
/* 69 */         int size = values.size();
/* 70 */         if (size > 0)
/*    */         {
/* 72 */           String val = (String)values.elementAt(0);
/* 73 */           props.put(fieldName, val);
/*    */         }
/*    */       }
/*    */     }
/* 77 */     return props;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 82 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.SchemaView
 * JD-Core Version:    0.5.4
 */