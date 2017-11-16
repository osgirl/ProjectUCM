/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class RevisionImplementor
/*     */ {
/*  37 */   protected static RangeDefinition[] m_majorDef = null;
/*  38 */   protected static RangeDefinition m_minorDef = null;
/*  39 */   public static String m_invalidLabel = "Invalid";
/*     */ 
/*     */   public void init() throws ServiceException
/*     */   {
/*  43 */     String major = SharedObjects.getEnvironmentValue("MajorRevSeq");
/*  44 */     String minor = SharedObjects.getEnvironmentValue("MinorRevSeq");
/*  45 */     m_invalidLabel = LocaleResources.getString("apInvalidText", null);
/*     */ 
/*  47 */     if (major == null)
/*     */     {
/*  49 */       return;
/*     */     }
/*     */ 
/*  52 */     String errMsg = null;
/*  53 */     Vector ranges = StringUtils.parseArray(major, ',', ',');
/*  54 */     int size = ranges.size();
/*  55 */     m_majorDef = new RangeDefinition[size];
/*  56 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  58 */       String range = (String)ranges.elementAt(i);
/*  59 */       Vector r = StringUtils.parseArray(range, '-', '-');
/*  60 */       int rSize = r.size();
/*     */ 
/*  62 */       if (rSize != 2)
/*     */       {
/*  64 */         errMsg = LocaleUtils.encodeMessage("apMajorRevNotRange", null, range);
/*  65 */         break;
/*     */       }
/*     */ 
/*  68 */       m_majorDef[i] = new RangeDefinition();
/*  69 */       if (m_majorDef[i].init(r))
/*     */         continue;
/*  71 */       errMsg = LocaleUtils.encodeMessage("apMajorRevRangeNotWellDefined", null, range);
/*     */ 
/*  73 */       break;
/*     */     }
/*     */ 
/*  77 */     if (errMsg != null)
/*     */     {
/*  79 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/*  82 */     if (minor == null)
/*     */     {
/*  84 */       return;
/*     */     }
/*     */ 
/*  87 */     Vector r = StringUtils.parseArray(minor, '-', '-');
/*  88 */     int rSize = r.size();
/*     */ 
/*  90 */     if (rSize != 2)
/*     */     {
/*  92 */       String msg = LocaleUtils.encodeMessage("apMinorRevNotRange", null, minor);
/*  93 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  96 */     m_minorDef = new RangeDefinition();
/*  97 */     if (m_minorDef.init(r))
/*     */       return;
/*  99 */     String msg = LocaleUtils.encodeMessage("apMinorRevRangeNotWellDefined", null, minor);
/*     */ 
/* 101 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public String getFirst()
/*     */   {
/* 107 */     IdcStringBuilder buffer = null;
/* 108 */     if (m_majorDef != null)
/*     */     {
/* 110 */       buffer = new IdcStringBuilder();
/* 111 */       buffer.append(m_majorDef[0].m_begin);
/* 112 */       if (m_minorDef != null)
/*     */       {
/* 114 */         buffer.append(m_minorDef.m_begin);
/*     */       }
/* 116 */       return buffer.toString();
/*     */     }
/*     */ 
/* 119 */     return "1";
/*     */   }
/*     */ 
/*     */   public String getNext(String revStr)
/*     */   {
/* 124 */     revStr = revStr.trim();
/*     */ 
/* 126 */     if (m_majorDef == null)
/*     */     {
/* 128 */       String nextRev = m_invalidLabel;
/*     */       try
/*     */       {
/* 131 */         int rev = Integer.parseInt(revStr);
/* 132 */         nextRev = String.valueOf(rev + 1);
/*     */       }
/*     */       catch (NumberFormatException e)
/*     */       {
/* 136 */         if (SystemUtils.m_verbose)
/*     */         {
/* 138 */           Report.debug("systemparse", null, e);
/*     */         }
/*     */       }
/* 141 */       return nextRev;
/*     */     }
/*     */ 
/* 145 */     Vector segments = new IdcVector();
/* 146 */     SegmentDefinition currentDef = null;
/* 147 */     int segCount = 0;
/* 148 */     for (int i = 0; i < revStr.length(); ++i)
/*     */     {
/* 150 */       char ch = revStr.charAt(i);
/* 151 */       boolean isDigit = Character.isDigit(ch);
/* 152 */       if ((i == 0) || (!isDigit) || (!currentDef.m_isDigit))
/*     */       {
/* 154 */         currentDef = new SegmentDefinition(isDigit);
/* 155 */         segments.addElement(currentDef);
/* 156 */         ++segCount;
/*     */       }
/* 158 */       if (segCount > 2)
/*     */       {
/* 161 */         return null;
/*     */       }
/* 163 */       currentDef.m_segment.append(ch);
/*     */     }
/* 165 */     return computeNextRev(segments);
/*     */   }
/*     */ 
/*     */   protected String computeNextRev(Vector segments)
/*     */   {
/* 170 */     String l = m_invalidLabel;
/* 171 */     int size = segments.size();
/* 172 */     if (size > 0)
/*     */     {
/* 174 */       SegmentDefinition majorDef = (SegmentDefinition)segments.elementAt(0);
/* 175 */       String rev = getNextMajor(majorDef);
/* 176 */       if (rev != null)
/*     */       {
/* 180 */         if (size == 1)
/*     */         {
/* 182 */           if (m_minorDef != null)
/*     */           {
/* 184 */             l = majorDef.m_segment + m_minorDef.m_begin;
/*     */           }
/*     */           else
/*     */           {
/* 188 */             l = rev;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 193 */           SegmentDefinition minorDef = (SegmentDefinition)segments.elementAt(1);
/* 194 */           boolean[] isEnd = new boolean[1];
/* 195 */           isEnd[0] = false;
/* 196 */           String nextMinor = nextInRange(minorDef, m_minorDef, null, isEnd);
/* 197 */           IdcStringBuilder str = new IdcStringBuilder();
/* 198 */           if (isEnd[0] != 0)
/*     */           {
/* 200 */             String nextMajor = getNextMajor(majorDef);
/* 201 */             if (nextMajor != null)
/*     */             {
/* 203 */               str.append(nextMajor);
/* 204 */               str.append(m_minorDef.m_begin);
/*     */             }
/*     */           }
/* 207 */           else if (nextMinor != null)
/*     */           {
/* 209 */             str = majorDef.m_segment;
/* 210 */             str.append(nextMinor);
/*     */           }
/* 212 */           if (str.length() > 0)
/*     */           {
/* 214 */             l = str.toString();
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 218 */     return l;
/*     */   }
/*     */ 
/*     */   protected String getNextMajor(SegmentDefinition major)
/*     */   {
/* 223 */     int len = m_majorDef.length;
/* 224 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 226 */       RangeDefinition nextRange = null;
/* 227 */       if (i == len - 1)
/*     */       {
/* 229 */         nextRange = m_majorDef[0];
/*     */       }
/*     */       else
/*     */       {
/* 233 */         nextRange = m_majorDef[(i + 1)];
/*     */       }
/*     */ 
/* 236 */       boolean[] isEnd = new boolean[1];
/* 237 */       isEnd[0] = false;
/* 238 */       String str = nextInRange(major, m_majorDef[i], nextRange, isEnd);
/* 239 */       if (str != null)
/*     */       {
/* 241 */         return str;
/*     */       }
/*     */     }
/*     */ 
/* 245 */     return null;
/*     */   }
/*     */ 
/*     */   protected String nextInRange(SegmentDefinition segment, RangeDefinition range, RangeDefinition nextRange, boolean[] isEnd)
/*     */   {
/* 251 */     if (segment.m_isDigit != range.m_isDigit)
/*     */     {
/* 253 */       return null;
/*     */     }
/*     */ 
/* 256 */     String segStr = segment.m_segment.toStringNoRelease();
/* 257 */     if (segment.m_isDigit)
/*     */     {
/* 259 */       int begin = Integer.parseInt(range.m_begin);
/* 260 */       int end = Integer.parseInt(range.m_end);
/* 261 */       int seg = Integer.parseInt(segStr);
/* 262 */       if ((begin <= seg) && (seg < end))
/*     */       {
/* 264 */         return Integer.toString(seg + 1);
/*     */       }
/*     */ 
/* 268 */       if (seg == end)
/*     */       {
/* 270 */         isEnd[0] = true;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 275 */       char begin = range.m_begin.charAt(0);
/* 276 */       char end = range.m_end.charAt(0);
/* 277 */       char segCh = segStr.charAt(0);
/* 278 */       if ((begin <= segCh) && (segCh < end))
/*     */       {
/* 280 */         return String.valueOf((char)(segCh + '\001'));
/*     */       }
/*     */ 
/* 284 */       if (segCh == end)
/*     */       {
/* 286 */         isEnd[0] = true;
/*     */       }
/*     */     }
/*     */ 
/* 290 */     if ((isEnd[0] != 0) && 
/* 292 */       (nextRange != null))
/*     */     {
/* 294 */       return nextRange.m_begin;
/*     */     }
/*     */ 
/* 298 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean isValid(String revLabel)
/*     */   {
/*     */     try
/*     */     {
/* 306 */       String str = getNext(revLabel);
/* 307 */       if ((str == null) || (str.equals(m_invalidLabel)))
/*     */       {
/* 309 */         return false;
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 314 */       return false;
/*     */     }
/* 316 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 321 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92435 $";
/*     */   }
/*     */ 
/*     */   static class RangeDefinition
/*     */   {
/*     */     public String m_end;
/*     */     public String m_begin;
/*     */     public boolean m_isDigit;
/*     */ 
/*     */     public RangeDefinition()
/*     */     {
/* 350 */       this.m_begin = "1";
/* 351 */       this.m_end = "99";
/* 352 */       this.m_isDigit = true;
/*     */     }
/*     */ 
/*     */     public RangeDefinition(Vector r)
/*     */     {
/* 357 */       init(r);
/*     */     }
/*     */ 
/*     */     public boolean init(Vector r)
/*     */     {
/* 362 */       String str = (String)r.elementAt(0);
/* 363 */       this.m_begin = str.trim();
/* 364 */       str = (String)r.elementAt(1);
/* 365 */       this.m_end = str.trim();
/*     */ 
/* 367 */       this.m_isDigit = Character.isDigit(this.m_begin.charAt(0));
/* 368 */       if (!this.m_isDigit)
/*     */       {
/* 370 */         if ((this.m_begin.length() != 1) || (this.m_end.length() != 1) || (!Character.isLetter(this.m_begin.charAt(0))) || (!Character.isLetter(this.m_end.charAt(0))) || (this.m_end.charAt(0) < this.m_begin.charAt(0)))
/*     */         {
/* 375 */           return false;
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/*     */         try
/*     */         {
/* 382 */           int begin = Integer.parseInt(this.m_begin);
/* 383 */           int end = Integer.parseInt(this.m_end);
/* 384 */           if ((begin < 0) || (end < begin))
/*     */           {
/* 386 */             return false;
/*     */           }
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 391 */           return false;
/*     */         }
/*     */       }
/* 394 */       return true;
/*     */     }
/*     */   }
/*     */ 
/*     */   static class SegmentDefinition
/*     */   {
/*     */     public IdcStringBuilder m_segment;
/*     */     public boolean m_isDigit;
/*     */ 
/*     */     public SegmentDefinition()
/*     */     {
/* 331 */       this.m_segment = new IdcStringBuilder("1");
/* 332 */       this.m_isDigit = true;
/*     */     }
/*     */ 
/*     */     public SegmentDefinition(boolean isDigit)
/*     */     {
/* 337 */       this.m_segment = new IdcStringBuilder();
/* 338 */       this.m_isDigit = isDigit;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.RevisionImplementor
 * JD-Core Version:    0.5.4
 */