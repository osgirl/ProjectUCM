/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.gui.CustomLabel;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ 
/*     */ public class LongTextCustomLabel extends CustomLabel
/*     */ {
/*  34 */   public final int FILEPATH = -1;
/*     */ 
/*  36 */   protected String m_originalText = null;
/*  37 */   protected long m_originalWidth = 0L;
/*     */   protected int m_break;
/*     */ 
/*     */   public LongTextCustomLabel()
/*     */   {
/*  43 */     this.m_break = 4;
/*     */   }
/*     */ 
/*     */   public LongTextCustomLabel(String title)
/*     */   {
/*  48 */     super(title);
/*  49 */     this.m_break = 4;
/*     */   }
/*     */ 
/*     */   public LongTextCustomLabel(String title, int custStyle)
/*     */   {
/*  54 */     super(title, custStyle);
/*  55 */     this.m_break = 4;
/*     */   }
/*     */ 
/*     */   public void setBreakValue(int breakValue)
/*     */   {
/*  60 */     this.m_break = breakValue;
/*     */   }
/*     */ 
/*     */   public int getBreakValue()
/*     */   {
/*  65 */     return this.m_break;
/*     */   }
/*     */ 
/*     */   public void setText(String str)
/*     */   {
/*  71 */     Dimension d = getSize();
/*  72 */     int dWidth = d.width;
/*     */ 
/*  74 */     if ((str.indexOf("/../") > 0) || (str.indexOf("...") > 0))
/*     */     {
/*  76 */       str = this.m_originalText;
/*     */     }
/*     */     else
/*     */     {
/*  80 */       this.m_originalText = str;
/*     */     }
/*     */ 
/*  83 */     Font font = getFont();
/*  84 */     if (font == null)
/*     */     {
/*  86 */       super.setText(str);
/*  87 */       return;
/*     */     }
/*     */ 
/*  90 */     FontMetrics fm = getFontMetrics(font);
/*  91 */     int sWidth = fm.stringWidth(str);
/*  92 */     if (sWidth > dWidth)
/*     */     {
/*  94 */       this.m_originalWidth = dWidth;
/*  95 */       String fmtStr = null;
/*  96 */       int diff = sWidth - dWidth;
/*  97 */       if (this.m_break == -1)
/*     */       {
/*  99 */         fmtStr = formatFilePathTextWithDots(sWidth, diff, str);
/*     */       }
/*     */       else
/*     */       {
/* 103 */         fmtStr = formatLongTextWithDots(sWidth, diff, str);
/*     */       }
/* 105 */       super.setText(fmtStr);
/*     */     }
/*     */     else
/*     */     {
/* 109 */       super.setText(str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g)
/*     */   {
/* 116 */     Dimension d = getSize();
/* 117 */     int dWidth = d.width;
/* 118 */     String str = getText();
/*     */ 
/* 120 */     if (this.m_originalWidth != dWidth)
/*     */     {
/* 122 */       setText(str);
/*     */     }
/* 124 */     super.paint(g);
/*     */   }
/*     */ 
/*     */   public String getOriginalText()
/*     */   {
/* 129 */     return this.m_originalText;
/*     */   }
/*     */ 
/*     */   protected String formatLongTextWithDots(int sWidth, int diff, String str)
/*     */   {
/* 134 */     String fmtStr = null;
/* 135 */     int len = str.length();
/* 136 */     int widthPerChar = sWidth / len;
/* 137 */     int numChars = diff / widthPerChar;
/* 138 */     String dotStr = "...";
/*     */ 
/* 141 */     if (numChars == 0)
/*     */     {
/* 143 */       numChars = 1;
/*     */     }
/*     */ 
/* 146 */     if (this.m_break == 4)
/*     */     {
/* 148 */       int subIdx = len - numChars - "...".length();
/* 149 */       if (subIdx <= 0)
/*     */       {
/* 151 */         fmtStr = str;
/*     */       }
/*     */       else
/*     */       {
/* 155 */         String tempStr = str.substring(0, subIdx);
/* 156 */         fmtStr = tempStr + "...";
/*     */       }
/*     */     }
/* 159 */     else if (this.m_break == 0)
/*     */     {
/* 161 */       int hlfLen = len / 2;
/* 162 */       String tempStr = str.substring(0, hlfLen);
/* 163 */       tempStr = tempStr + "...";
/* 164 */       int subIdx = hlfLen + numChars + "...".length();
/* 165 */       if (subIdx >= len)
/*     */       {
/* 167 */         fmtStr = str;
/*     */       }
/*     */       else
/*     */       {
/* 171 */         fmtStr = tempStr + str.substring(subIdx, len);
/*     */       }
/*     */     }
/* 174 */     else if (this.m_break == 2)
/*     */     {
/* 176 */       int subIdx = numChars + "...".length();
/*     */ 
/* 178 */       if (subIdx >= len)
/*     */       {
/* 180 */         fmtStr = str;
/*     */       }
/*     */       else
/*     */       {
/* 184 */         String tempStr = str.substring(subIdx, len);
/* 185 */         fmtStr = "..." + tempStr;
/*     */       }
/*     */     }
/*     */ 
/* 189 */     return fmtStr;
/*     */   }
/*     */ 
/*     */   protected String formatFilePathTextWithDots(int sWidth, int diff, String str)
/*     */   {
/* 194 */     String fmtStr = "";
/* 195 */     int len = str.length();
/* 196 */     int widthPerChar = sWidth / len;
/* 197 */     int numChars = diff / widthPerChar;
/* 198 */     int removedChars = 0;
/* 199 */     int index = 0;
/*     */ 
/* 202 */     if (numChars == 0)
/*     */     {
/* 204 */       numChars = 1;
/*     */     }
/*     */ 
/* 208 */     if ((str.startsWith("\\\\")) || (str.startsWith("/")))
/*     */     {
/* 210 */       index = 2;
/* 211 */       fmtStr = str.substring(0, index);
/*     */     }
/* 213 */     else if ((str.length() > 1) && (Character.isLetter(str.charAt(0))) && (str.charAt(1) == ':'))
/*     */     {
/* 216 */       index = 3;
/* 217 */       fmtStr = str.substring(0, index);
/*     */     }
/*     */ 
/* 221 */     int tmpIdx = str.indexOf("/", index + 1);
/* 222 */     if (tmpIdx < 0)
/*     */     {
/* 224 */       tmpIdx = str.indexOf("\\", index + 1);
/*     */     }
/*     */ 
/* 228 */     if (tmpIdx < 0)
/*     */     {
/* 230 */       fmtStr = str;
/* 231 */       return fmtStr;
/*     */     }
/*     */ 
/* 234 */     fmtStr = fmtStr + str.substring(index, tmpIdx + 1);
/* 235 */     index = tmpIdx + 1;
/*     */ 
/* 237 */     while (numChars > removedChars)
/*     */     {
/* 240 */       tmpIdx = str.indexOf("/", index + 1);
/* 241 */       if (tmpIdx < 0)
/*     */       {
/* 243 */         tmpIdx = str.indexOf("\\", index + 1);
/*     */ 
/* 245 */         if (tmpIdx < 0) {
/*     */           break;
/*     */         }
/*     */ 
/* 249 */         fmtStr = fmtStr + "..\\";
/*     */       }
/*     */       else
/*     */       {
/* 253 */         fmtStr = fmtStr + "../";
/*     */       }
/*     */ 
/* 256 */       removedChars = tmpIdx - index - 2;
/*     */ 
/* 259 */       if (tmpIdx < 0)
/*     */       {
/* 261 */         fmtStr = str;
/* 262 */         break;
/*     */       }
/*     */ 
/* 265 */       index = tmpIdx;
/*     */     }
/* 267 */     fmtStr = fmtStr + str.substring(index + 1, len);
/*     */ 
/* 269 */     return fmtStr;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 274 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.LongTextCustomLabel
 * JD-Core Version:    0.5.4
 */