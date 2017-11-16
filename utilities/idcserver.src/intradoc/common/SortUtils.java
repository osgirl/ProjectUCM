/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ 
/*     */ public class SortUtils
/*     */ {
/*     */   public static int[] acyclicTopologicalSort(int[][] depends, int[] counters, boolean direction)
/*     */     throws IllegalArgumentException, DAGHasCycleException
/*     */   {
/*  67 */     if ((null == depends) || ((null != counters) && (counters.length != depends.length)))
/*     */     {
/*  69 */       if (SystemUtils.m_verbose)
/*     */       {
/*  71 */         Report.trace("system", "counters or depends is empty", null);
/*     */       }
/*  73 */       throw new IllegalArgumentException();
/*     */     }
/*  75 */     int N = depends.length;
/*  76 */     int i = (direction) ? N - 1 : 0;
/*  77 */     int inc = (direction) ? -1 : 1;
/*     */ 
/*  79 */     if (null == counters)
/*     */     {
/*  81 */       counters = new int[N];
/*  82 */       for (int n = 0; n < N; ++n)
/*     */       {
/*  84 */         if (null == depends[n]) {
/*     */           continue;
/*     */         }
/*     */ 
/*  88 */         int D = depends[n].length;
/*  89 */         for (int d = 0; d < D; ++d)
/*     */         {
/*  91 */           int rightSide = depends[n][d];
/*  92 */           counters[rightSide] += 1;
/*     */         }
/*     */       }
/*     */     }
/*  96 */     int[] zeros = new int[N];
/*  97 */     int zeroPtr = i;
/*  98 */     for (int n = 0; n < N; ++n)
/*     */     {
/* 100 */       if (counters[n] != 0)
/*     */         continue;
/* 102 */       zeros[zeroPtr] = n;
/* 103 */       zeroPtr += inc;
/*     */     }
/*     */ 
/* 107 */     int[] sorted = new int[N];
/* 108 */     while (i != zeroPtr)
/*     */     {
/* 111 */       int n = zeros[i];
/* 112 */       sorted[i] = n;
/* 113 */       i += inc;
/*     */ 
/* 116 */       if (null != depends[n])
/*     */       {
/* 118 */         int D = depends[n].length;
/* 119 */         for (int d = 0; d < D; ++d)
/*     */         {
/* 121 */           int m = depends[n][d];
/*     */ 
/* 124 */           if (0 != counters[m] -= 1) {
/*     */             continue;
/*     */           }
/*     */ 
/* 128 */           zeros[zeroPtr] = m;
/* 129 */           zeroPtr += inc;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 136 */     int numCycles = 0;
/* 137 */     for (int n = 0; n < N; ++n)
/*     */     {
/* 139 */       if (counters[n] <= 0)
/*     */         continue;
/* 141 */       ++numCycles;
/*     */     }
/*     */ 
/* 144 */     if (numCycles > 0)
/*     */     {
/* 146 */       int[] cycles = new int[numCycles];
/* 147 */       int c = 0;
/* 148 */       for (int n = 0; n < N; ++n)
/*     */       {
/* 150 */         if (counters[n] <= 0)
/*     */           continue;
/* 152 */         cycles[(c++)] = n;
/*     */       }
/*     */ 
/* 155 */       throw new DAGHasCycleException(cycles);
/*     */     }
/*     */ 
/* 158 */     return sorted;
/*     */   }
/*     */ 
/*     */   public static String[] acyclicTopologicalSortNamed(String[] names, String[][] depends, boolean direction)
/*     */     throws IllegalArgumentException, DAGHasCycleException
/*     */   {
/* 182 */     if ((null == names) || (null == depends) || (names.length != depends.length))
/*     */     {
/* 184 */       String msg = "names or depends is empty";
/* 185 */       throw new IllegalArgumentException(msg);
/*     */     }
/* 187 */     int N = names.length;
/* 188 */     for (int n = 0; n < N; ++n)
/*     */     {
/* 190 */       if (null != names[n])
/*     */         continue;
/* 192 */       String msg = "names[" + n + "] is null";
/* 193 */       throw new IllegalArgumentException(msg);
/*     */     }
/*     */ 
/* 196 */     int[] counters = new int[N];
/* 197 */     int[][] deps = new int[N][];
/*     */ 
/* 199 */     for (int n = 0; n < N; ++n)
/*     */     {
/* 201 */       if (null == depends[n]) {
/*     */         continue;
/*     */       }
/*     */ 
/* 205 */       String[] depstr = depends[n];
/* 206 */       int D = 0;
/* 207 */       for (int ds = depstr.length - 1; ds >= 0; --ds)
/*     */       {
/* 209 */         if (depstr[ds] == null)
/*     */           continue;
/* 211 */         ++D;
/*     */       }
/*     */ 
/* 214 */       deps[n] = new int[D];
/* 215 */       int d = 0; for (int ds = 0; d < D; ++ds)
/*     */       {
/* 217 */         String rightSide = depends[n][ds];
/* 218 */         if (null == rightSide)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 223 */         for (int rightIndex = 0; rightIndex < N; ++rightIndex)
/*     */         {
/* 225 */           if (rightSide.equals(names[rightIndex])) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 230 */         if (rightIndex >= N)
/*     */         {
/* 232 */           String msg = "missing depend for " + rightSide + " (by " + names[n] + ')';
/* 233 */           throw new IllegalArgumentException(msg);
/*     */         }
/* 235 */         if (rightIndex == n)
/*     */         {
/* 238 */           if (SystemUtils.m_verbose)
/*     */           {
/* 240 */             Report.trace("system", "self-pointer (cycle) found for " + rightSide, null);
/*     */           }
/* 242 */           throw new DAGHasCycleException(n);
/*     */         }
/* 244 */         deps[n][d] = rightIndex;
/* 245 */         counters[rightIndex] += 1;
/* 246 */         ++d;
/*     */       }
/*     */     }
/*     */ 
/* 250 */     int[] sorted = acyclicTopologicalSort(deps, counters, direction);
/* 251 */     String[] sortedNames = new String[N];
/* 252 */     for (int n = 0; n < N; ++n)
/*     */     {
/* 254 */       int index = sorted[n];
/* 255 */       sortedNames[n] = names[index];
/*     */     }
/* 257 */     return sortedNames;
/*     */   }
/*     */ 
/*     */   public static String[] sortCaseInsensitiveStringList(String[] l, String[][] lowerCasedStrings)
/*     */   {
/* 262 */     return sortCaseInsensitiveStringList(l, lowerCasedStrings, false);
/*     */   }
/*     */ 
/*     */   public static String[] sortCaseInsensitiveStringList(String[] l, String[][] lowerCasedStrings, boolean isAsc)
/*     */   {
/* 275 */     String[][] strings = new String[l.length][];
/* 276 */     for (int i = 0; i < strings.length; ++i)
/*     */     {
/* 278 */       String toSortString = l[i];
/* 279 */       strings[i] = { toSortString.toLowerCase(), toSortString };
/*     */     }
/* 281 */     SortUtilsComparator c = new SortUtilsComparator(1, isAsc);
/* 282 */     c.setUseFirstIndexOfArray(true);
/* 283 */     Sort.sort(strings, c);
/* 284 */     String[] result = new String[strings.length];
/* 285 */     String[] lowerCasedReturn = null;
/* 286 */     if ((lowerCasedStrings != null) && (lowerCasedStrings.length > 0))
/*     */     {
/* 288 */       lowerCasedReturn = new String[strings.length];
/* 289 */       lowerCasedStrings[0] = lowerCasedReturn;
/*     */     }
/* 291 */     for (int i = 0; i < strings.length; ++i)
/*     */     {
/* 293 */       result[i] = strings[i][1];
/* 294 */       if (lowerCasedReturn == null)
/*     */         continue;
/* 296 */       lowerCasedReturn[i] = strings[i][0];
/*     */     }
/*     */ 
/* 299 */     return result;
/*     */   }
/*     */ 
/*     */   public static void sortStringList(List l)
/*     */   {
/* 304 */     sortStringList(l, true);
/*     */   }
/*     */ 
/*     */   public static void sortStringList(List l, boolean isAsc)
/*     */   {
/* 309 */     SortUtilsComparator c = new SortUtilsComparator(1, isAsc);
/* 310 */     Sort.sortList(l, c);
/*     */   }
/*     */ 
/*     */   public static void sortIntegerList(List l, boolean isAsc)
/*     */   {
/* 315 */     SortUtilsComparator c = new SortUtilsComparator(2, isAsc);
/*     */ 
/* 317 */     Sort.sortList(l, c);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 322 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92975 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SortUtils
 * JD-Core Version:    0.5.4
 */