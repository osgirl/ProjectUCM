/*     */ package intradoc.common;
/*     */ 
/*     */ public class IdcDateUtils
/*     */ {
/*  24 */   public static boolean m_ignoreUnsupported = false;
/*     */ 
/*     */   public static String computeCFormatStringFromFormat(IdcDateFormat format, String limit)
/*     */   {
/*     */     IdcDateToken[] tokens;
/*     */     IdcDateToken[] tokens;
/*  30 */     if (limit.equals("date"))
/*     */     {
/*  32 */       tokens = format.getDateFormatTokens();
/*     */     }
/*     */     else
/*     */     {
/*     */       IdcDateToken[] tokens;
/*  34 */       if (limit.equals("time"))
/*     */       {
/*  36 */         tokens = format.getTimeFormatTokens();
/*     */       }
/*     */       else
/*     */       {
/*  40 */         tokens = format.getAllFormatTokens();
/*     */       }
/*     */     }
/*  42 */     return computeCFormatStringFromTokens(tokens);
/*     */   }
/*     */ 
/*     */   public static String computeCFormatStringFromTokens(IdcDateToken[] tokens)
/*     */   {
/*  47 */     int len = tokens.length;
/*  48 */     StringBuffer buffer = new StringBuffer(len * 5);
/*  49 */     for (int i = 0; i < len; ++i)
/*     */     {
/*  51 */       IdcDateToken token = tokens[i];
/*  52 */       switch (token.m_type)
/*     */       {
/*     */       case 'F':
/*  55 */         switch (token.m_sym)
/*     */         {
/*     */         case 'G':
/*  59 */           if (m_ignoreUnsupported)
/*     */             continue;
/*  61 */           buffer.append("##"); break;
/*     */         case 'y':
/*  65 */           if (token.m_length == 2)
/*     */           {
/*  67 */             buffer.append("%y");
/*     */           }
/*     */           else
/*     */           {
/*  71 */             buffer.append("%Y");
/*     */           }
/*  73 */           break;
/*     */         case 'M':
/*  75 */           switch (token.m_length)
/*     */           {
/*     */           case 1:
/*  78 */             buffer.append("%-m");
/*  79 */             break;
/*     */           case 2:
/*  81 */             buffer.append("%m");
/*  82 */             break;
/*     */           case 3:
/*  84 */             buffer.append("%b");
/*  85 */             break;
/*     */           default:
/*  87 */             buffer.append("%B");
/*  88 */           }break;
/*     */         case 'w':
/*  99 */           buffer.append("%V");
/* 100 */           break;
/*     */         case 'W':
/* 106 */           if (m_ignoreUnsupported)
/*     */             continue;
/* 108 */           buffer.append("##"); break;
/*     */         case 'D':
/* 112 */           buffer.append("%j");
/* 113 */           break;
/*     */         case 'd':
/* 115 */           buffer.append("%d");
/* 116 */           break;
/*     */         case 'F':
/* 123 */           if (m_ignoreUnsupported)
/*     */             continue;
/* 125 */           buffer.append('#'); break;
/*     */         case 'E':
/* 129 */           switch (token.m_length)
/*     */           {
/*     */           case 1:
/*     */           case 2:
/* 133 */             buffer.append("%w");
/* 134 */             break;
/*     */           case 3:
/* 136 */             buffer.append("%a");
/* 137 */             break;
/*     */           default:
/* 139 */             buffer.append("%A");
/* 140 */           }break;
/*     */         case 'a':
/* 144 */           buffer.append("%p");
/* 145 */           break;
/*     */         case 'H':
/* 147 */           buffer.append("%H");
/* 148 */           break;
/*     */         case 'K':
/*     */         case 'k':
/* 155 */           if (!m_ignoreUnsupported)
/*     */           {
/* 157 */             buffer.append("##");
/*     */           }
/*     */         case 'h':
/* 160 */           buffer.append("%I");
/* 161 */           break;
/*     */         case 'm':
/* 163 */           buffer.append("%M");
/* 164 */           break;
/*     */         case 's':
/* 166 */           buffer.append("%S");
/* 167 */           break;
/*     */         case 'S':
/* 173 */           if (m_ignoreUnsupported)
/*     */             continue;
/* 175 */           buffer.append("###"); break;
/*     */         case 'z':
/* 179 */           if (token.m_length == 3)
/*     */           {
/* 181 */             buffer.append("%Z");
/*     */           }
/*     */           else
/*     */           {
/* 186 */             buffer.append("GMT%z");
/*     */           }
/* 188 */           break;
/*     */         case 'Z':
/* 190 */           buffer.append("%z");
/* 191 */           break;
/*     */         case 'I':
/*     */         case 'J':
/*     */         case 'L':
/*     */         case 'N':
/*     */         case 'O':
/*     */         case 'P':
/*     */         case 'Q':
/*     */         case 'R':
/*     */         case 'T':
/*     */         case 'U':
/*     */         case 'V':
/*     */         case 'X':
/*     */         case 'Y':
/*     */         case '[':
/*     */         case '\\':
/*     */         case ']':
/*     */         case '^':
/*     */         case '_':
/*     */         case '`':
/*     */         case 'b':
/*     */         case 'c':
/*     */         case 'e':
/*     */         case 'f':
/*     */         case 'g':
/*     */         case 'i':
/*     */         case 'j':
/*     */         case 'l':
/*     */         case 'n':
/*     */         case 'o':
/*     */         case 'p':
/*     */         case 'q':
/*     */         case 'r':
/*     */         case 't':
/*     */         case 'u':
/*     */         case 'v':
/*     */         case 'x':
/*     */         default:
/* 193 */           if (m_ignoreUnsupported)
/*     */             continue;
/* 195 */           buffer.append('?'); } break;
/*     */       case 'T':
/* 201 */         buffer.append(token.m_text);
/* 202 */         break;
/*     */       case 'Z':
/*     */       case 'z':
/* 206 */         break;
/*     */       case 'M':
/* 209 */         buffer.append("%p");
/* 210 */         break;
/*     */       case 'S':
/* 212 */         buffer.append(' ');
/* 213 */         break;
/*     */       case 'W':
/* 215 */         break;
/*     */       default:
/* 217 */         if (m_ignoreUnsupported)
/*     */           continue;
/* 219 */         buffer.append('?');
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 224 */     return buffer.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 230 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcDateUtils
 * JD-Core Version:    0.5.4
 */