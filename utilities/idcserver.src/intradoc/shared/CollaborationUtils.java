/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CollaborationUtils
/*     */ {
/*     */   public static void setCollaborationName(DataBinder binder)
/*     */   {
/*  31 */     String clbraName = binder.getLocal("dClbraName");
/*  32 */     if ((clbraName != null) && (clbraName.length() != 0))
/*     */       return;
/*  34 */     String acct = binder.getLocal("dDocAccount");
/*  35 */     clbraName = parseCollaborationName(acct);
/*  36 */     binder.putLocal("dClbraName", clbraName);
/*     */   }
/*     */ 
/*     */   public static String checkAndGetDerivedCollaborationName(DataBinder binder)
/*     */   {
/*  42 */     String acct = binder.getLocal("dDocAccount");
/*  43 */     String clbraName = parseCollaborationName(acct);
/*  44 */     binder.putLocal("dClbraName", clbraName);
/*  45 */     binder.putLocal("isCollaboration", (clbraName.length() > 0) ? "1" : "");
/*  46 */     return clbraName;
/*     */   }
/*     */ 
/*     */   public static String parseCollaborationName(String acct)
/*     */   {
/*  51 */     String clbraName = "";
/*  52 */     if ((acct != null) && (acct.startsWith("prj/")))
/*     */     {
/*  55 */       clbraName = acct.substring(4);
/*     */     }
/*  57 */     return clbraName;
/*     */   }
/*     */ 
/*     */   public static String getPresentationString(String acct, ExecutionContext cxt)
/*     */   {
/*  62 */     String presStr = "";
/*  63 */     if (acct.equals("prj"))
/*     */     {
/*  65 */       presStr = "[" + LocaleResources.getString("apAllProjects", cxt) + "]";
/*     */     }
/*     */     else
/*     */     {
/*  69 */       presStr = parseCollaborationName(acct);
/*     */     }
/*     */ 
/*  72 */     return presStr;
/*     */   }
/*     */ 
/*     */   public static String getInternalString(String acct, ExecutionContext cxt)
/*     */   {
/*  77 */     String internalStr = "";
/*     */ 
/*  79 */     String allProjectsStr = "[" + LocaleResources.getString("apAllProjects", cxt) + "]";
/*  80 */     if (acct.equals(allProjectsStr))
/*     */     {
/*  82 */       internalStr = "prj";
/*     */     }
/*     */     else
/*     */     {
/*  86 */       internalStr = "prj/" + acct;
/*     */     }
/*     */ 
/*  89 */     return internalStr;
/*     */   }
/*     */ 
/*     */   public static boolean isInCollaboration(String acct, String clbraName)
/*     */   {
/*  95 */     acct = acct.toLowerCase();
/*  96 */     clbraName = clbraName.toLowerCase();
/*     */ 
/*  98 */     return acct.equals("prj/" + clbraName);
/*     */   }
/*     */ 
/*     */   public static boolean isInACollaboration(String acct)
/*     */   {
/* 103 */     return isInCollaboration(acct, null);
/*     */   }
/*     */ 
/*     */   public static void createAccessPresentationStr(Vector pList, Vector accList, boolean hasPrv, boolean hasPrefix, String prefix)
/*     */   {
/* 111 */     int num = accList.size();
/* 112 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 114 */       String uName = (String)accList.elementAt(i);
/* 115 */       ++i;
/* 116 */       if (uName.length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 120 */       if (hasPrefix)
/*     */       {
/* 122 */         uName = prefix + uName;
/*     */       }
/* 124 */       if (hasPrv)
/*     */       {
/* 126 */         int priv = NumberUtils.parseInteger((String)accList.elementAt(i), 0);
/* 127 */         String privStr = SecurityAccessListUtils.makePrivilegeStr(priv);
/* 128 */         uName = uName + " (" + privStr + ")";
/*     */       }
/* 130 */       pList.addElement(uName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 136 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CollaborationUtils
 * JD-Core Version:    0.5.4
 */