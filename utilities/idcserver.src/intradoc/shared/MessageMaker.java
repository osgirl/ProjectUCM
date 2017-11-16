/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MessageMaker
/*     */ {
/*     */   public static String merge(String format, Parameters args)
/*     */   {
/*  33 */     StringBuffer[] segments = new StringBuffer[2];
/*  34 */     for (int i = 0; i < 2; ++i)
/*     */     {
/*  36 */       segments[i] = new StringBuffer();
/*     */     }
/*     */ 
/*  39 */     int part = 0;
/*  40 */     int braceStack = 0;
/*  41 */     boolean inQuote = false;
/*  42 */     StringBuffer buffer = new StringBuffer();
/*     */ 
/*  45 */     for (int i = 0; i < format.length(); ++i)
/*     */     {
/*  47 */       char ch = format.charAt(i);
/*  48 */       if (part == 0)
/*     */       {
/*  50 */         if (ch == '\'')
/*     */         {
/*  52 */           if ((i + 1 < format.length()) && (format.charAt(i + 1) == '\''))
/*     */           {
/*  54 */             segments[part].append(ch);
/*  55 */             ++i;
/*     */           }
/*     */           else
/*     */           {
/*  59 */             inQuote = !inQuote;
/*     */           }
/*     */         }
/*  62 */         else if ((ch == '{') && (!inQuote))
/*     */         {
/*  64 */           part = 1;
/*     */         }
/*     */         else
/*     */         {
/*  68 */           segments[part].append(ch);
/*     */         }
/*     */       }
/*  71 */       else if (inQuote)
/*     */       {
/*  73 */         segments[part].append(ch);
/*  74 */         if (ch != '\'')
/*     */           continue;
/*  76 */         inQuote = false;
/*     */       }
/*     */       else
/*     */       {
/*  81 */         switch (ch)
/*     */         {
/*     */         case '{':
/*  84 */           ++braceStack;
/*  85 */           segments[part].append(ch);
/*  86 */           break;
/*     */         case '}':
/*  89 */           if (braceStack == 0)
/*     */           {
/*  91 */             part = 0;
/*  92 */             appendFormat(segments, args, buffer);
/*     */           }
/*     */           else
/*     */           {
/*  96 */             --braceStack;
/*  97 */             segments[part].append(ch);
/*     */           }
/*  99 */           break;
/*     */         case '\'':
/* 102 */           inQuote = true;
/*     */         default:
/* 105 */           segments[part].append(ch);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 111 */     buffer.append(segments[0]);
/*     */ 
/* 113 */     return buffer.toString();
/*     */   }
/*     */ 
/*     */   protected static void appendFormat(StringBuffer[] segments, Parameters args, StringBuffer buff)
/*     */   {
/* 119 */     buff.append(segments[0]);
/*     */ 
/* 121 */     String value = null;
/*     */     try
/*     */     {
/* 124 */       value = args.get(segments[1].toString());
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/* 128 */       if (SystemUtils.m_verbose)
/*     */       {
/* 130 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/* 133 */     if ((value == null) || (value.length() == 0))
/*     */     {
/* 135 */       value = "<UNKNOWN>";
/*     */     }
/* 137 */     buff.append(value);
/*     */ 
/* 139 */     segments[0].setLength(0);
/* 140 */     segments[1].setLength(0);
/*     */   }
/*     */ 
/*     */   public static String encodeDataBinderMessage(String msg, Parameters params)
/*     */   {
/* 146 */     if (!msg.startsWith("!"))
/*     */     {
/* 148 */       return "!$" + merge(msg, params);
/*     */     }
/* 150 */     Vector message = LocaleUtils.decodeMessage(msg);
/* 151 */     int size = message.size();
/* 152 */     StringBuffer buf = new StringBuffer();
/*     */ 
/* 154 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 156 */       Object obj = message.elementAt(i);
/* 157 */       if (obj instanceof Object[])
/*     */       {
/* 159 */         Object[] item = (Object[])(Object[])obj;
/* 160 */         String key = (String)item[0];
/* 161 */         String[] args = (String[])(String[])item[1];
/* 162 */         int lparen = key.indexOf("(");
/* 163 */         int rparen = key.indexOf(")");
/* 164 */         if ((args.length == 0) && (lparen > 0) && (rparen > lparen) && (rparen + 1 == key.length()))
/*     */         {
/* 168 */           String keyList = key.substring(lparen + 1, rparen);
/* 169 */           key = key.substring(0, lparen);
/* 170 */           Vector list = StringUtils.parseArray(keyList, ',', '^');
/* 171 */           args = new String[list.size()];
/* 172 */           list.copyInto(args);
/* 173 */           for (int j = 0; j < args.length; ++j)
/*     */           {
/*     */             try
/*     */             {
/* 177 */               args[j] = params.get(args[j]);
/*     */             }
/*     */             catch (DataException ignore)
/*     */             {
/* 181 */               args[j] = null;
/*     */             }
/*     */           }
/*     */         }
/* 185 */         buf.append(LocaleUtils.encodeMessage(key, null, args));
/*     */       }
/*     */       else
/*     */       {
/* 189 */         buf.append("!$");
/* 190 */         buf.append(obj.toString());
/*     */       }
/*     */     }
/* 193 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 198 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97472 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.MessageMaker
 * JD-Core Version:    0.5.4
 */