/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ 
/*     */ public class MultiPageScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public MultiPageScriptExtensions()
/*     */   {
/*  34 */     this.m_variableTable = new String[] { "PreviousPage", "NextPage", "IsCurrentNav", "IsMultiPage", "PreviousStartRow", "PreviousEndRow", "NextStartRow", "NextEndRow" };
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */     throws ServiceException
/*     */   {
/*  46 */     String msg = LocaleUtils.encodeMessage("csUnableToEvaluateNoBinder", null, info.m_key);
/*     */ 
/*  48 */     DataBinder binder = ScriptExtensionUtils.getBinder(context, msg);
/*  49 */     int id = ((int[])(int[])info.m_entry)[0];
/*     */ 
/*  51 */     String curPage = binder.getLocal("PageNumber");
/*  52 */     String resultCount = binder.getLocal("ResultCount");
/*     */ 
/*  54 */     String curStartRowString = binder.getLocal("StartRow");
/*  55 */     if (curPage == null)
/*     */     {
/*  57 */       curPage = binder.getLocal("ContentPageNumber");
/*     */     }
/*  59 */     if (curPage == null)
/*     */     {
/*  61 */       return false;
/*     */     }
/*  63 */     int pageNum = Integer.parseInt(curPage);
/*  64 */     int rowsPerPage = NumberUtils.parseInteger(resultCount, 20);
/*  65 */     int curStartRow = Integer.parseInt(curStartRowString);
/*     */     try
/*     */     {
/*  68 */       switch (id)
/*     */       {
/*     */       case 0:
/*  71 */         --pageNum;
/*  72 */         sVal[0] = Integer.toString(pageNum);
/*  73 */         bVal[0] = ((pageNum > 0) ? 1 : false);
/*  74 */         return true;
/*     */       case 1:
/*  78 */         ++pageNum;
/*  79 */         if (isConditional)
/*     */         {
/*  81 */           String numPagesStr = binder.getLocal("NumPages");
/*  82 */           if (numPagesStr != null)
/*     */           {
/*  84 */             int numPages = Integer.parseInt(numPagesStr);
/*  85 */             bVal[0] = ((pageNum <= numPages) ? 1 : false);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/*  90 */           sVal[0] = Integer.toString(pageNum);
/*     */         }
/*  92 */         return true;
/*     */       case 2:
/*  97 */         String navPage = binder.get("HeaderPageNumber");
/*  98 */         bVal[0] = (((navPage != null) && (navPage.equals(curPage))) ? 1 : false);
/*  99 */         sVal[0] = String.valueOf(bVal[0]);
/* 100 */         return true;
/*     */       case 3:
/* 105 */         bVal[0] = false;
/* 106 */         String numPagesStr = binder.getLocal("NumPages");
/* 107 */         if (numPagesStr != null)
/*     */         {
/* 109 */           int numPages = Integer.parseInt(numPagesStr);
/* 110 */           bVal[0] = ((numPages > 1) ? 1 : false);
/*     */         }
/* 112 */         return true;
/*     */       case 4:
/*     */       case 6:
/* 117 */         pageNum += ((id == 4) ? -1 : 1);
/* 118 */         if ((id == 4) && (pageNum >= 0))
/*     */         {
/* 120 */           sVal[0] = Integer.toString(curStartRow - rowsPerPage);
/*     */         }
/* 122 */         else if (pageNum >= 0)
/*     */         {
/* 124 */           sVal[0] = Integer.toString(curStartRow + rowsPerPage);
/*     */         }
/* 126 */         bVal[0] = ((pageNum >= 0) ? 1 : false);
/* 127 */         return true;
/*     */       case 5:
/*     */       case 7:
/* 132 */         pageNum += ((id == 5) ? -1 : 1);
/* 133 */         int newStartRow = curStartRow;
/* 134 */         if (id == 5)
/*     */         {
/* 136 */           newStartRow = curStartRow - rowsPerPage;
/*     */         }
/*     */         else
/*     */         {
/* 140 */           newStartRow = curStartRow + rowsPerPage;
/*     */         }
/* 142 */         int endRow = newStartRow + rowsPerPage - 1;
/* 143 */         String totalRowsStr = binder.getLocal("TotalRows");
/* 144 */         endRow = Math.min(endRow, Integer.parseInt(totalRowsStr));
/* 145 */         sVal[0] = Integer.toString(endRow);
/* 146 */         bVal[0] = ((endRow >= 1) ? 1 : false);
/* 147 */         return true;
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 153 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 156 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 161 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103493 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.MultiPageScriptExtensions
 * JD-Core Version:    0.5.4
 */