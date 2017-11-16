/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ThreadInfoUtils
/*     */ {
/*     */   public static List retrieveCurrentThreadDump(boolean isVerbose)
/*     */   {
/*  32 */     List threadDump = new ArrayList();
/*  33 */     ThreadGroup tmpTopTG = Thread.currentThread().getThreadGroup();
/*  34 */     ThreadGroup topThreadGroup = tmpTopTG;
/*  35 */     while ((tmpTopTG = topThreadGroup.getParent()) != null)
/*     */     {
/*  37 */       topThreadGroup = tmpTopTG;
/*     */     }
/*  39 */     Thread[] list = new Thread[1000];
/*  40 */     topThreadGroup.enumerate(list, true);
/*     */ 
/*  42 */     int i = 0;
/*  43 */     for (i = 0; (i < list.length) && 
/*  45 */       (list[i] != null); ++i)
/*     */     {
/*  47 */       Vector row = new Vector();
/*  48 */       String name = list[i].getName();
/*  49 */       String id = "" + list[i].getId();
/*  50 */       String state = list[i].getState().toString();
/*  51 */       StackTraceElement[] stack = list[i].getStackTrace();
/*     */ 
/*  53 */       row.add(name);
/*  54 */       row.add(id);
/*  55 */       row.add(state);
/*  56 */       if (stack != null)
/*     */       {
/*  58 */         IdcStringBuilder builder = new IdcStringBuilder();
/*  59 */         String prevClassName = null;
/*  60 */         for (int j = 0; j < stack.length; ++j)
/*     */         {
/*  62 */           String displayStr = stack[j].toString();
/*  63 */           if (isVerbose)
/*     */           {
/*  65 */             String className = stack[j].getClassName();
/*  66 */             if ((prevClassName != null) && (prevClassName.equals(className)))
/*     */             {
/*  68 */               displayStr = displayStr + "[see prior]";
/*     */             }
/*     */             else
/*     */             {
/*  72 */               Object info = null;
/*     */               try
/*     */               {
/*  75 */                 info = SystemUtils.getIdcVersionInfo(className, "");
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/*     */               }
/*     */ 
/*  81 */               if (info != null)
/*     */               {
/*  83 */                 displayStr = displayStr + "[" + info + "]";
/*  84 */                 displayStr = displayStr.replaceAll("\\**", "");
/*  85 */                 prevClassName = className;
/*     */               }
/*     */               else
/*     */               {
/*  89 */                 prevClassName = null;
/*     */               }
/*     */             }
/*     */           }
/*  93 */           builder.append(displayStr);
/*  94 */           builder.append("\r\n");
/*     */         }
/*  96 */         row.add(builder.toString());
/*     */       }
/*     */       else
/*     */       {
/* 100 */         row.add("");
/*     */       }
/*     */ 
/* 103 */       threadDump.add(row);
/*     */     }
/*     */ 
/* 110 */     return threadDump;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 115 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 88132 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ThreadInfoUtils
 * JD-Core Version:    0.5.4
 */