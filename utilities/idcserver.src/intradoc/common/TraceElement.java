/*     */ package intradoc.common;
/*     */ 
/*     */ public class TraceElement
/*     */ {
/*     */   public String m_message;
/*     */   public int m_nestLevel;
/*     */   public String m_filename;
/*     */   public int m_lineNumber;
/*     */   public int m_charStart;
/*     */   public int m_charEnd;
/*     */ 
/*     */   public TraceElement()
/*     */   {
/*     */   }
/*     */ 
/*     */   public TraceElement(String message, int nestLevel)
/*     */   {
/*  50 */     this.m_message = message;
/*  51 */     this.m_nestLevel = nestLevel;
/*     */   }
/*     */ 
/*     */   public TraceElement(String message, int nestLevel, DynamicHtml html) {
/*  55 */     this.m_message = message;
/*  56 */     this.m_nestLevel = nestLevel;
/*  57 */     setFromDynamicHtml(html);
/*     */   }
/*     */ 
/*     */   public void setFromDynamicHtml(DynamicHtml html)
/*     */   {
/*  69 */     if (null != html)
/*     */     {
/*  71 */       if (null != html.m_resourceString)
/*     */       {
/*  73 */         if (html.m_resourceString.length() > 0)
/*     */         {
/*  75 */           this.m_message = LocaleUtils.encodeMessage("csDynHTMLDebugTraceDynEvalVal", this.m_message, html.m_resourceString);
/*     */         }
/*     */         else
/*     */         {
/*  80 */           this.m_message = LocaleUtils.encodeMessage("csDynHTMLDebugTraceEmptyEvalVal", this.m_message);
/*     */         }
/*     */       }
/*  83 */       this.m_filename = html.m_fileName;
/*  84 */       if (null != html.m_fileName)
/*     */       {
/*  86 */         this.m_lineNumber = (html.m_parseLine + 1);
/*  87 */         this.m_charStart = (html.m_parseCharOffset + 1);
/*     */       }
/*     */       else
/*     */       {
/*  91 */         this.m_lineNumber = 0;
/*  92 */         this.m_charStart = 0;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  97 */       this.m_filename = null;
/*  98 */       this.m_lineNumber = 0;
/*  99 */       this.m_charStart = 0;
/*     */     }
/* 101 */     this.m_charEnd = 0;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 108 */     char[] prepend = new char[this.m_nestLevel];
/* 109 */     for (int i = 0; i < this.m_nestLevel; ++i)
/*     */     {
/* 111 */       prepend[i] = '+';
/*     */     }
/* 113 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 114 */     int pos = 0;
/* 115 */     int len = this.m_message.length();
/* 116 */     while (pos < len)
/*     */     {
/* 118 */       builder.append(prepend);
/* 119 */       int index = this.m_message.indexOf('\n', pos);
/* 120 */       if (index >= 0)
/*     */       {
/* 122 */         builder.append(this.m_message, pos, index + 1 - pos);
/* 123 */         pos = index + 1;
/*     */       }
/*     */       else
/*     */       {
/* 127 */         builder.append(this.m_message, pos, len - pos);
/* 128 */         break;
/*     */       }
/*     */     }
/* 131 */     if (null != this.m_filename)
/*     */     {
/* 133 */       builder.append(" (");
/* 134 */       builder.append(this.m_filename);
/* 135 */       builder.append(' ');
/* 136 */       builder.append(this.m_lineNumber);
/* 137 */       builder.append(')');
/*     */     }
/* 139 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 144 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66887 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TraceElement
 * JD-Core Version:    0.5.4
 */