/*     */ package intradoc.common;
/*     */ 
/*     */ public class SortUtilsComparator extends IdcLinguisticComparatorAdapter
/*     */ {
/*     */   public static final int T_STRING = 1;
/*     */   public static final int T_INTEGER = 2;
/*     */   public static final int T_ARRAY = 3;
/*     */   protected boolean m_useFirstIndexOfArray;
/*     */   protected int m_type;
/*     */   protected boolean m_isAscending;
/*     */ 
/*     */   public SortUtilsComparator(int type)
/*     */   {
/*  34 */     this.m_type = type;
/*  35 */     super.init(IdcLinguisticComparatorAdapter.m_defaultRule);
/*     */   }
/*     */ 
/*     */   public SortUtilsComparator(int type, boolean isAsc)
/*     */   {
/*  40 */     this.m_type = type;
/*  41 */     this.m_isAscending = isAsc;
/*     */ 
/*  45 */     super.init(IdcLinguisticComparatorAdapter.m_defaultRule);
/*     */   }
/*     */ 
/*     */   public void setNlsSortRule(String nlsSortRule)
/*     */   {
/*  57 */     if ((nlsSortRule == null) || (nlsSortRule.equals("")))
/*     */       return;
/*  59 */     super.init(nlsSortRule);
/*     */   }
/*     */ 
/*     */   public void setNlsSortUsingLocale(IdcLocale locale)
/*     */   {
/*  71 */     if (locale == null)
/*     */       return;
/*  73 */     super.init(locale);
/*     */   }
/*     */ 
/*     */   public void setUseFirstIndexOfArray(boolean useFirstIndexOfArray)
/*     */   {
/*  85 */     this.m_useFirstIndexOfArray = useFirstIndexOfArray;
/*     */   }
/*     */ 
/*     */   public boolean getUseFirstIndexOfArray()
/*     */   {
/*  95 */     return this.m_useFirstIndexOfArray;
/*     */   }
/*     */ 
/*     */   public int compare(Object arg1, Object arg2)
/*     */   {
/* 101 */     int result = 0;
/* 102 */     if (this.m_useFirstIndexOfArray)
/*     */     {
/* 104 */       Object[] arg1Array = (Object[])(Object[])arg1;
/* 105 */       arg1 = arg1Array[0];
/* 106 */       Object[] arg2Array = (Object[])(Object[])arg2;
/* 107 */       arg2 = arg2Array[0];
/*     */     }
/* 109 */     switch (this.m_type)
/*     */     {
/*     */     case 1:
/* 113 */       String s1 = (String)arg1;
/* 114 */       String s2 = (String)arg2;
/* 115 */       result = super.compare(s1, s2);
/*     */ 
/* 117 */       if (this.m_isAscending)
/*     */         break label333;
/* 119 */       result = -result; break;
/*     */     case 2:
/* 125 */       int i1 = NumberUtils.parseInteger((String)arg1, 0);
/* 126 */       int i2 = NumberUtils.parseInteger((String)arg2, 0);
/* 127 */       result = 0;
/* 128 */       if (i1 != i2)
/*     */       {
/* 130 */         result = (i1 > i2) ? 1 : -1;
/*     */       }
/* 132 */       if (this.m_isAscending)
/*     */         break label333;
/* 134 */       result = -result; break;
/*     */     case 3:
/* 140 */       Object[] a1 = (Object[])(Object[])arg1;
/* 141 */       Object[] a2 = (Object[])(Object[])arg2;
/* 142 */       if (a1 == null)
/*     */       {
/* 144 */         if (a2 == null)
/*     */         {
/* 146 */           result = 0;
/* 147 */           break label333:
/*     */         }
/* 149 */         result = 1;
/* 150 */         break label333:
/*     */       }
/* 152 */       if (a2 == null)
/*     */       {
/* 154 */         result = -1;
/* 155 */         break label333:
/*     */       }
/* 157 */       for (int i = 0; i < a1.length; ++i)
/*     */       {
/* 159 */         if (a1[i] == null)
/*     */         {
/* 161 */           if (a2[i] != null)
/*     */           {
/* 163 */             result = -1;
/* 164 */             break;
/*     */           }
/* 166 */           if (a2[i] == null) {
/*     */             continue;
/*     */           }
/*     */         }
/*     */ 
/* 171 */         if (a2[i] == null)
/*     */         {
/* 173 */           result = 1;
/* 174 */           break;
/*     */         }
/* 176 */         if (a1[i].equals(a2[i]))
/*     */           continue;
/* 178 */         result = 1;
/* 179 */         break;
/*     */       }
/*     */ 
/* 182 */       if (result != 0)
/*     */         break label333;
/* 184 */       result = a1.length - a2.length; break;
/*     */     default:
/* 189 */       throw new AssertionError("!$Unknown SortUtilsComparator type " + this.m_type);
/*     */     }
/* 191 */     label333: return result;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 196 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73807 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SortUtilsComparator
 * JD-Core Version:    0.5.4
 */