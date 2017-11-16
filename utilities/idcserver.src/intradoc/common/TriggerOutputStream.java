/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class TriggerOutputStream extends OutputStream
/*     */ {
/*     */   public static final int TRIGGER_OUTPUT_OFF = 1;
/*     */   public static final int TRIGGER_CHECK_ON = 2;
/*     */   public static final int TRIGGER_CHECK_OFF = 4;
/*     */   private static final int LT = 1;
/*     */   private static final int LE = 2;
/*     */   private static final int EQUAL = 4;
/*     */   private static final int GE = 8;
/*     */   private static final int GT = 16;
/*     */   private static final int NOOP = -1;
/*     */   protected TruncatedOutputStream m_source;
/*     */   protected OutputStream m_target;
/*     */   protected int m_flag;
/*     */   protected String m_triggerStr;
/*     */   protected Pattern m_triggerPattern;
/*     */   protected int m_operator;
/*     */   protected double m_cmpValue;
/*     */   protected boolean m_isTriggered;
/*     */   protected boolean m_isTriggerDisabled;
/*     */ 
/*     */   public TriggerOutputStream()
/*     */   {
/*  40 */     this.m_operator = -1;
/*  41 */     this.m_cmpValue = -1.0D;
/*     */   }
/*     */ 
/*     */   public void init(OutputStream source, OutputStream target)
/*     */   {
/*  47 */     if (source instanceof TruncatedOutputStream)
/*     */     {
/*  49 */       this.m_source = ((TruncatedOutputStream)source);
/*  50 */       this.m_isTriggerDisabled = false;
/*     */     }
/*     */     else
/*     */     {
/*  54 */       this.m_flag = 4;
/*  55 */       this.m_isTriggerDisabled = true;
/*     */     }
/*     */ 
/*  58 */     this.m_target = target;
/*     */   }
/*     */ 
/*     */   public boolean setTrigger(String trigger)
/*     */   {
/*  63 */     if (this.m_isTriggerDisabled)
/*     */     {
/*  65 */       return false;
/*     */     }
/*     */ 
/*  68 */     if (trigger == null)
/*     */     {
/*  70 */       this.m_flag = 1;
/*     */     }
/*  72 */     else if (trigger.length() == 0)
/*     */     {
/*  74 */       this.m_flag = 4;
/*     */     }
/*     */     else
/*     */     {
/*  78 */       if (trigger.startsWith("regex::"))
/*     */       {
/*  80 */         int end = trigger.lastIndexOf("::");
/*  81 */         if (end > 7)
/*     */         {
/*  83 */           String patternStr = trigger.substring(7, end);
/*  84 */           this.m_triggerPattern = Pattern.compile(patternStr);
/*     */ 
/*  86 */           String opStr = trigger.substring(end + 2).trim();
/*  87 */           char c = opStr.charAt(0);
/*  88 */           int index = 1;
/*  89 */           if (c == '>')
/*     */           {
/*  91 */             if (opStr.charAt(1) == '=')
/*     */             {
/*  93 */               this.m_operator = 8;
/*  94 */               index = 2;
/*     */             }
/*     */             else
/*     */             {
/*  98 */               this.m_operator = 16;
/*     */             }
/*     */           }
/* 101 */           else if (c == '<')
/*     */           {
/* 103 */             if (opStr.charAt(1) == '=')
/*     */             {
/* 105 */               this.m_operator = 2;
/* 106 */               index = 2;
/*     */             }
/*     */             else
/*     */             {
/* 110 */               this.m_operator = 1;
/*     */             }
/*     */           }
/* 113 */           else if (c == '=')
/*     */           {
/* 115 */             this.m_operator = 4;
/*     */           }
/* 117 */           if (this.m_operator != -1)
/*     */           {
/* 119 */             this.m_cmpValue = Double.parseDouble(opStr.substring(index));
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 125 */           this.m_triggerPattern = Pattern.compile(trigger.substring(7));
/*     */         }
/* 127 */         this.m_triggerStr = null;
/*     */       }
/*     */       else
/*     */       {
/* 131 */         this.m_triggerStr = trigger;
/* 132 */         this.m_triggerPattern = null;
/*     */       }
/* 134 */       this.m_flag = 2;
/*     */     }
/* 136 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean checkTrigger(String msg)
/*     */   {
/* 141 */     if ((this.m_flag == 1) || (this.m_flag == 4))
/*     */     {
/* 143 */       return false;
/*     */     }
/* 145 */     if ((this.m_triggerStr != null) && (msg.indexOf(this.m_triggerStr) > 0))
/*     */     {
/* 147 */       this.m_isTriggered = true;
/*     */     }
/* 149 */     else if ((this.m_triggerStr == null) && (this.m_triggerPattern != null))
/*     */     {
/* 151 */       Matcher matcher = this.m_triggerPattern.matcher(msg);
/* 152 */       if (matcher.find())
/*     */       {
/* 154 */         if (this.m_operator != -1)
/*     */         {
/* 156 */           String valueStr = matcher.group(1);
/*     */           try
/*     */           {
/* 159 */             if (valueStr.length() > 0)
/*     */             {
/* 161 */               double value = Double.parseDouble(valueStr);
/*     */ 
/* 163 */               switch (this.m_operator)
/*     */               {
/*     */               case 16:
/* 166 */                 this.m_isTriggered = (value > this.m_cmpValue);
/* 167 */                 break;
/*     */               case 8:
/* 169 */                 this.m_isTriggered = (value >= this.m_cmpValue);
/* 170 */                 break;
/*     */               case 4:
/* 172 */                 this.m_isTriggered = (value == this.m_cmpValue);
/* 173 */                 break;
/*     */               case 2:
/* 175 */                 this.m_isTriggered = (value <= this.m_cmpValue);
/* 176 */                 break;
/*     */               case 1:
/* 178 */                 this.m_isTriggered = (value < this.m_cmpValue);
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 186 */             System.out.println("Error occurred for parsing trigger value(" + valueStr + "): " + t);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 192 */           this.m_isTriggered = true;
/*     */         }
/*     */       }
/*     */     }
/* 196 */     return this.m_isTriggered;
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/* 202 */     if (this.m_flag == 1)
/*     */     {
/* 204 */       return;
/*     */     }
/*     */ 
/* 207 */     if (this.m_flag == 4)
/*     */     {
/* 209 */       this.m_target.write(b);
/*     */     } else {
/* 211 */       if (!this.m_isTriggered)
/*     */         return;
/* 213 */       byte[] tmp = this.m_source.getBytesFromMarker();
/* 214 */       this.m_source.mark();
/* 215 */       this.m_target.write(tmp);
/* 216 */       this.m_isTriggered = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int index, int len)
/*     */     throws IOException
/*     */   {
/* 230 */     if (this.m_flag == 1)
/*     */     {
/* 232 */       return;
/*     */     }
/*     */ 
/* 235 */     if (this.m_flag == 4)
/*     */     {
/* 237 */       this.m_target.write(b, index, len);
/*     */     } else {
/* 239 */       if (!this.m_isTriggered)
/*     */         return;
/* 241 */       this.m_isTriggered = false;
/* 242 */       byte[] tmp = this.m_source.getBytesFromMarker();
/* 243 */       this.m_source.mark();
/* 244 */       this.m_target.write(tmp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 251 */     if (this.m_target == null)
/*     */       return;
/* 253 */     this.m_target.close();
/* 254 */     this.m_target = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 260 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87203 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TriggerOutputStream
 * JD-Core Version:    0.5.4
 */