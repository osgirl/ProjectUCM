/*    */ package intradoc.gui;
/*    */ 
/*    */ import intradoc.common.StringUtils;
/*    */ 
/*    */ public class DisplayLabel extends CustomLabel
/*    */ {
/* 28 */   protected String[][] m_displayMap = (String[][])null;
/* 29 */   protected String m_defaultValue = "";
/*    */ 
/*    */   public DisplayLabel(String[][] display)
/*    */   {
/* 33 */     setDisplayMap(display, 0);
/*    */   }
/*    */ 
/*    */   public DisplayLabel(String[][] display, int def)
/*    */   {
/* 38 */     setDisplayMap(display, def);
/*    */   }
/*    */ 
/*    */   public void setDisplayMap(String[][] display)
/*    */   {
/* 43 */     setDisplayMap(display, 0);
/*    */   }
/*    */ 
/*    */   public void setDisplayMap(String[][] display, int def)
/*    */   {
/* 48 */     this.m_displayMap = display;
/* 49 */     if (def >= this.m_displayMap.length)
/*    */       return;
/* 51 */     this.m_defaultValue = this.m_displayMap[def][1];
/*    */   }
/*    */ 
/*    */   public void setTextInternal(String value)
/*    */   {
/* 58 */     String displayValue = value;
/* 59 */     if (this.m_displayMap != null)
/*    */     {
/* 61 */       displayValue = StringUtils.getPresentationString(this.m_displayMap, value);
/*    */     }
/*    */ 
/* 64 */     String str = this.m_defaultValue;
/* 65 */     if (displayValue != null)
/*    */     {
/* 67 */       str = displayValue;
/*    */     }
/* 69 */     super.setText(str);
/*    */   }
/*    */ 
/*    */   public String getTextInternal()
/*    */   {
/* 75 */     String value = super.getText();
/* 76 */     if (this.m_displayMap != null)
/*    */     {
/* 78 */       String mapVal = StringUtils.getInternalString(this.m_displayMap, value);
/* 79 */       if (mapVal == null)
/*    */       {
/* 81 */         value = this.m_defaultValue;
/*    */       }
/*    */       else
/*    */       {
/* 85 */         value = mapVal;
/*    */       }
/*    */     }
/* 88 */     return value;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 93 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.DisplayLabel
 * JD-Core Version:    0.5.4
 */