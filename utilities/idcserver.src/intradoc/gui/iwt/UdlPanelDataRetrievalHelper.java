/*    */ package intradoc.gui.iwt;
/*    */ 
/*    */ import java.awt.Dimension;
/*    */ import java.util.Dictionary;
/*    */ import java.util.Hashtable;
/*    */ 
/*    */ public class UdlPanelDataRetrievalHelper
/*    */   implements DataRetrievalHelper
/*    */ {
/*    */   public UdlPanel m_panel;
/*    */ 
/*    */   public UdlPanelDataRetrievalHelper(UdlPanel panel)
/*    */   {
/* 33 */     this.m_panel = panel;
/*    */   }
/*    */ 
/*    */   public Object get(Object source, Object key)
/*    */   {
/* 38 */     Object rc = null;
/* 39 */     Dictionary data = (Dictionary)((UserDrawListItem)source).getData();
/* 40 */     if (key instanceof ColumnInfo)
/*    */     {
/* 42 */       ColumnInfo cinfo = (ColumnInfo)key;
/* 43 */       rc = data.get(cinfo.m_fieldId);
/*    */     }
/* 45 */     else if (key instanceof Object[])
/*    */     {
/* 47 */       String type = (String)((Object[])(Object[])key)[0];
/* 48 */       ColumnInfo info = (ColumnInfo)((Object[])(Object[])key)[1];
/* 49 */       if ((info != null) && (type.equals("icons")) && (info.m_fieldId.equals(this.m_panel.m_iconField)) && (this.m_panel.m_idIndex >= 0))
/*    */       {
/* 53 */         rc = this.m_panel.m_icons.get(data.get(this.m_panel.m_idColumnName));
/*    */       }
/* 55 */       else if ((this.m_panel.m_iconSize != null) && (type.equals("iconDimensions")))
/*    */       {
/* 57 */         rc = new Dimension[] { this.m_panel.m_iconSize };
/*    */       }
/* 59 */       else if ((info != null) && (type.equals("info")))
/*    */       {
/* 61 */         rc = data.get("caption_" + info.m_fieldId);
/*    */       }
/*    */     }
/*    */ 
/* 65 */     return rc;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 70 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.UdlPanelDataRetrievalHelper
 * JD-Core Version:    0.5.4
 */