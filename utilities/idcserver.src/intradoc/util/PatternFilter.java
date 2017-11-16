/*     */ package intradoc.util;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class PatternFilter
/*     */ {
/*     */   public static final int NOT_MATCHED = -1;
/*     */   public static final int IS_INCLUDED = 0;
/*     */   public static final int IS_EXCLUDED = 1;
/*     */   public List<Pattern> m_inclusiveRules;
/*     */   public List<Pattern> m_exclusiveRules;
/*     */ 
/*     */   public void add(boolean isExclusive, Pattern pattern)
/*     */   {
/*  38 */     List list = (isExclusive) ? this.m_exclusiveRules : this.m_inclusiveRules;
/*  39 */     if (list == null)
/*     */     {
/*  41 */       list = new ArrayList();
/*  42 */       if (isExclusive)
/*     */       {
/*  44 */         this.m_exclusiveRules = list;
/*     */       }
/*     */       else
/*     */       {
/*  48 */         this.m_inclusiveRules = list;
/*     */       }
/*     */     }
/*  51 */     list.add(pattern);
/*     */   }
/*     */ 
/*     */   public boolean isIncluded(String matchString)
/*     */   {
/*  62 */     List rules = this.m_inclusiveRules;
/*  63 */     int numRules = (rules == null) ? 0 : rules.size();
/*  64 */     if (numRules > 0)
/*     */     {
/*  66 */       boolean isIncluded = false;
/*  67 */       for (int r = numRules - 1; r >= 0; --r)
/*     */       {
/*  69 */         Pattern pattern = (Pattern)rules.get(r);
/*  70 */         if (!pattern.matcher(matchString).matches())
/*     */           continue;
/*  72 */         isIncluded = true;
/*  73 */         break;
/*     */       }
/*     */ 
/*  76 */       if (!isIncluded)
/*     */       {
/*  78 */         return false;
/*     */       }
/*     */     }
/*  81 */     rules = this.m_exclusiveRules;
/*  82 */     numRules = (rules == null) ? 0 : rules.size();
/*  83 */     if (numRules > 0)
/*     */     {
/*  85 */       for (int r = numRules - 1; r >= 0; --r)
/*     */       {
/*  87 */         Pattern pattern = (Pattern)rules.get(r);
/*  88 */         if (pattern.matcher(matchString).matches())
/*     */         {
/*  90 */           return false;
/*     */         }
/*     */       }
/*     */     }
/*  94 */     return true;
/*     */   }
/*     */ 
/*     */   public int getMatch(String matchString)
/*     */   {
/* 105 */     List rules = this.m_exclusiveRules;
/* 106 */     if (rules != null)
/*     */     {
/* 108 */       for (int r = rules.size() - 1; r >= 0; --r)
/*     */       {
/* 110 */         Pattern pattern = (Pattern)rules.get(r);
/* 111 */         if (pattern.matcher(matchString).matches())
/*     */         {
/* 113 */           return 1;
/*     */         }
/*     */       }
/*     */     }
/* 117 */     rules = this.m_inclusiveRules;
/* 118 */     if (rules != null)
/*     */     {
/* 120 */       for (int r = rules.size() - 1; r >= 0; --r)
/*     */       {
/* 122 */         Pattern pattern = (Pattern)rules.get(r);
/* 123 */         if (pattern.matcher(matchString).matches())
/*     */         {
/* 125 */           return 0;
/*     */         }
/*     */       }
/*     */     }
/* 129 */     return -1;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 135 */     StringBuilder str = new StringBuilder();
/* 136 */     List rules = this.m_inclusiveRules;
/* 137 */     if (rules != null)
/*     */     {
/* 139 */       for (Pattern pattern : rules)
/*     */       {
/* 141 */         str.append('+');
/* 142 */         str.append(pattern);
/* 143 */         str.append('\n');
/*     */       }
/*     */     }
/* 146 */     rules = this.m_exclusiveRules;
/* 147 */     if (rules != null)
/*     */     {
/* 149 */       for (Pattern pattern : rules)
/*     */       {
/* 151 */         str.append('-');
/* 152 */         str.append(pattern);
/* 153 */         str.append('\n');
/*     */       }
/*     */     }
/* 156 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 162 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92916 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.PatternFilter
 * JD-Core Version:    0.5.4
 */