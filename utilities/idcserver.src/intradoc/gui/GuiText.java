/*    */ package intradoc.gui;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.LocaleResources;
/*    */ 
/*    */ public class GuiText
/*    */ {
/* 30 */   public static String m_okLabel = "OK";
/* 31 */   public static String m_cancelLabel = "Cancel";
/* 32 */   public static String m_resetLabel = "Reset";
/* 33 */   public static String m_helpLabel = "Help";
/* 34 */   public static String m_yesLabel = "Yes";
/* 35 */   public static String m_yesAllLabel = "Yes to all";
/* 36 */   public static String m_noLabel = "No";
/* 37 */   public static String m_noAllLabel = "No to all";
/* 38 */   public static String m_abortLabel = "Abort";
/* 39 */   public static String m_retryLabel = "Retry";
/* 40 */   public static String m_ignoreLabel = "Ignore";
/* 41 */   public static String m_ignoreAllLabel = "Ignore all";
/* 42 */   public static String m_browseLabel = "Browse...";
/* 43 */   public static String m_closeLabel = "Close";
/*    */ 
/* 45 */   public static String m_helpPageMissing = "Help page was not found.";
/* 46 */   public static String m_invalidValue = "Invalid value or choice in field {1}.";
/* 47 */   public static String m_unknownError = "Unknown error.";
/*    */ 
/* 49 */   public static String m_readyText = "Ready";
/* 50 */   public static String m_defaultTitle = "Content Server Application";
/* 51 */   public static String m_errorTitle = "Error";
/* 52 */   public static String m_selectChoiceTitle = "Select Choice";
/* 53 */   public static String m_overlengthSuffix = "...";
/* 54 */   public static String m_wideCharacter = "W";
/* 55 */   public static String m_internalPlusDisplayValueJoiner = "%1 (%0)";
/* 56 */   public static String m_internalIsDisplayValueJoiner = "%1";
/*    */ 
/*    */   public static void localize(ExecutionContext cxt)
/*    */   {
/* 62 */     m_okLabel = LocaleResources.getString("apLabelOK", cxt);
/* 63 */     m_cancelLabel = LocaleResources.getString("apLabelCancel", cxt);
/* 64 */     m_resetLabel = LocaleResources.getString("apLabelReset", cxt);
/* 65 */     m_helpLabel = LocaleResources.getString("apLabelHelp", cxt);
/* 66 */     m_yesLabel = LocaleResources.getString("apLabelYes", cxt);
/* 67 */     m_yesAllLabel = LocaleResources.getString("apLabelYesAll", cxt);
/* 68 */     m_noLabel = LocaleResources.getString("apLabelNo", cxt);
/* 69 */     m_noAllLabel = LocaleResources.getString("apLabelNoAll", cxt);
/* 70 */     m_abortLabel = LocaleResources.getString("apLabelAbort", cxt);
/* 71 */     m_retryLabel = LocaleResources.getString("apLabelRetry", cxt);
/* 72 */     m_ignoreLabel = LocaleResources.getString("apLabelIgnore", cxt);
/* 73 */     m_ignoreAllLabel = LocaleResources.getString("apLabelIgnoreAll", cxt);
/* 74 */     m_browseLabel = LocaleResources.getString("apLabelBrowse", cxt);
/* 75 */     m_closeLabel = LocaleResources.getString("apLabelClose", cxt);
/*    */ 
/* 77 */     m_helpPageMissing = LocaleResources.getString("apHelpPageMissing", cxt);
/* 78 */     m_invalidValue = LocaleResources.getString("apInvalidValue", cxt);
/* 79 */     m_unknownError = LocaleResources.getString("apUnknownError", cxt);
/*    */ 
/* 81 */     m_readyText = LocaleResources.getString("apReadyText", cxt);
/* 82 */     m_defaultTitle = LocaleResources.getString("apDefaultTitle", cxt);
/* 83 */     m_errorTitle = LocaleResources.getString("apErrorTitle", cxt);
/* 84 */     m_selectChoiceTitle = LocaleResources.getString("apSelectChoiceTitle", cxt);
/* 85 */     m_overlengthSuffix = LocaleResources.getString("apOverlengthSuffix", cxt);
/* 86 */     m_wideCharacter = LocaleResources.getString("apWideCharacter", cxt);
/* 87 */     m_internalPlusDisplayValueJoiner = LocaleResources.getString("apInternalPlusDisplayValueJoiner", cxt);
/* 88 */     m_internalIsDisplayValueJoiner = LocaleResources.getString("apInternalIsDisplayValueJoiner", cxt);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 93 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96523 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.GuiText
 * JD-Core Version:    0.5.4
 */