/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.Component;
/*    */ import java.awt.Font;
/*    */ 
/*    */ public class GuiStyles
/*    */ {
/*    */   public static final short NORMAL_CUSTOM_STYLE = 0;
/*    */   public static final short BOLD_CUSTOM_STYLE = 1;
/*    */   public static final short HEADER_CUSTOM_STYLE = 2;
/*    */   public static final short TITLE_CUSTOM_STYLE = 3;
/*    */   public static final short ITALIC_CUSTOM_STYLE = 4;
/*    */   public static final short NORMAL_DIALOG_STYLE = 5;
/*    */   public static final short BOLD_DIALOG_STYLE = 6;
/* 36 */   public static String m_fontFamily = "Helvetica";
/*    */ 
/*    */   public static Font getCustomFont(int style)
/*    */   {
/* 40 */     String family = m_fontFamily;
/* 41 */     int type = 1;
/* 42 */     int size = 12;
/*    */ 
/* 44 */     switch (style)
/*    */     {
/*    */     case 0:
/* 47 */       type = 0;
/* 48 */       break;
/*    */     case 1:
/* 50 */       break;
/*    */     case 2:
/* 52 */       size = 14;
/* 53 */       break;
/*    */     case 3:
/* 55 */       size = 24;
/* 56 */       break;
/*    */     case 4:
/* 58 */       size = 14;
/* 59 */       type = 2;
/* 60 */       break;
/*    */     case 5:
/* 62 */       type = 0;
/* 63 */       family = "Dialog";
/* 64 */       break;
/*    */     case 6:
/* 66 */       type = 1;
/* 67 */       family = "Dialog";
/*    */     }
/*    */ 
/* 71 */     return new Font(family, type, size);
/*    */   }
/*    */ 
/*    */   public static void setCustomStyle(Component comp, int style)
/*    */   {
/* 76 */     Font font = getCustomFont(style);
/* 77 */     comp.setFont(font);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 82 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.GuiStyles
 * JD-Core Version:    0.5.4
 */