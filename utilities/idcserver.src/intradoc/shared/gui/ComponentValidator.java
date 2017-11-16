/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Date;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ComponentValidator
/*     */ {
/*  35 */   protected ResultSet m_defSet = null;
/*  36 */   protected ExecutionContext m_context = null;
/*     */ 
/*     */   public ComponentValidator(ResultSet rset)
/*     */   {
/*  40 */     this.m_defSet = rset;
/*     */   }
/*     */ 
/*     */   public int getMaxLength(String name, int defMaxLength)
/*     */   {
/*  45 */     return getMaxLengthEx(name, null, defMaxLength);
/*     */   }
/*     */ 
/*     */   public int getMaxLengthEx(String name, FieldInfo info, int defMaxLength)
/*     */   {
/*  50 */     if (this.m_defSet == null)
/*     */     {
/*  52 */       return defMaxLength;
/*     */     }
/*     */ 
/*  55 */     int maxLength = defMaxLength;
/*  56 */     boolean isPresent = true;
/*  57 */     if (info == null)
/*     */     {
/*  59 */       info = new FieldInfo();
/*  60 */       isPresent = this.m_defSet.getFieldInfo(name, info);
/*     */     }
/*  62 */     if (isPresent)
/*     */     {
/*  64 */       maxLength = info.m_maxLen;
/*     */     }
/*     */ 
/*  67 */     if (maxLength == 0)
/*     */     {
/*  69 */       maxLength = defMaxLength;
/*     */     }
/*     */ 
/*  72 */     return maxLength;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String validateType(String name, String value)
/*     */   {
/*  79 */     IdcMessage msg = validateType(name, value, null);
/*  80 */     return (msg != null) ? LocaleUtils.encodeMessage(msg) : null;
/*     */   }
/*     */ 
/*     */   public IdcMessage validateType(String name, String value, Map options)
/*     */   {
/*  85 */     IdcMessage errMsg = null;
/*  86 */     if ((value == null) || (value.length() == 0) || (this.m_defSet == null))
/*     */     {
/*  88 */       return errMsg;
/*     */     }
/*     */ 
/*  91 */     FieldInfo info = new FieldInfo();
/*  92 */     boolean isPresent = this.m_defSet.getFieldInfo(name, info);
/*  93 */     if (!isPresent)
/*     */     {
/*  95 */       return errMsg;
/*     */     }
/*     */ 
/*  98 */     return validateTypeEx(info, value, options);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String validateTypeEx(FieldInfo info, String value)
/*     */   {
/* 105 */     IdcMessage msg = validateTypeEx(info, value, null);
/* 106 */     return (msg != null) ? LocaleUtils.encodeMessage(msg) : null;
/*     */   }
/*     */ 
/*     */   public IdcMessage validateTypeEx(FieldInfo info, String value, Map options)
/*     */   {
/* 111 */     IdcMessage errMsg = null;
/* 112 */     if ((value == null) || (value.length() == 0))
/*     */     {
/* 114 */       return errMsg;
/*     */     }
/*     */ 
/* 117 */     switch (info.m_type)
/*     */     {
/*     */     case 5:
/*     */       try
/*     */       {
/* 122 */         LocaleResources.parseDate(value, this.m_context);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 126 */         errMsg = IdcMessageFactory.lc(LocaleUtils.createMessageListFromThrowable(e), "apValueDateParseError", new Object[] { info.m_name, value });
/*     */       }
/*     */ 
/* 130 */       break;
/*     */     case 3:
/*     */       try
/*     */       {
/* 135 */         Integer.parseInt(value);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 139 */         errMsg = IdcMessageFactory.lc("apValueIntParseError", new Object[] { info.m_name, value });
/*     */       }
/*     */ 
/*     */     case 1:
/*     */     case 2:
/*     */     case 4:
/*     */     }
/*     */ 
/* 147 */     return errMsg;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String validate(String name, String value, Vector filter, int defaultLength, String wildCards)
/*     */   {
/* 155 */     IdcMessage msg = validate(name, value, filter, defaultLength, wildCards, null);
/* 156 */     return (msg != null) ? LocaleUtils.encodeMessage(msg) : null;
/*     */   }
/*     */ 
/*     */   public IdcMessage validate(String name, String value, Vector filter, int defaultLength, String wildCards, Map options)
/*     */   {
/* 162 */     IdcMessage errMsg = null;
/* 163 */     if (this.m_defSet == null)
/*     */     {
/* 165 */       errMsg = IdcMessageFactory.lc("apValueUnableToValidateMissingSet", new Object[] { name, value });
/* 166 */       return errMsg;
/*     */     }
/*     */ 
/* 169 */     FieldInfo info = new FieldInfo();
/* 170 */     boolean isPresent = this.m_defSet.getFieldInfo(name, info);
/* 171 */     if (!isPresent)
/*     */     {
/* 173 */       errMsg = IdcMessageFactory.lc("apValueUnableToValidateMissingColumn", new Object[] { name, value });
/* 174 */       return errMsg;
/*     */     }
/*     */ 
/* 177 */     errMsg = validateTypeEx(info, value, options);
/* 178 */     if (errMsg != null)
/*     */     {
/* 180 */       return errMsg;
/*     */     }
/*     */ 
/* 183 */     int maxLength = getMaxLengthEx(info.m_name, info, defaultLength);
/* 184 */     int len = value.length();
/* 185 */     if (len > maxLength)
/*     */     {
/* 187 */       errMsg = IdcMessageFactory.lc("apValueExeedsMaxLength", new Object[] { name, value, "" + maxLength });
/* 188 */       return errMsg;
/*     */     }
/*     */ 
/* 191 */     errMsg = matchesPattern(name, value, filter, wildCards, options);
/* 192 */     return errMsg;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String matchesPattern(String name, String val, Vector filter, String wildCard)
/*     */   {
/* 199 */     IdcMessage msg = matchesPattern(name, val, filter, wildCard, null);
/* 200 */     return (msg != null) ? LocaleUtils.encodeMessage(msg) : null;
/*     */   }
/*     */ 
/*     */   public IdcMessage matchesPattern(String name, String val, Vector filter, String wildCard, Map options)
/*     */   {
/* 205 */     IdcMessage errMsg = null;
/* 206 */     IdcMessage prevMsg = null;
/* 207 */     boolean isMatch = true;
/* 208 */     int size = filter.size();
/* 209 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 211 */       FilterData fd = (FilterData)filter.elementAt(i);
/* 212 */       if (!name.equals(fd.m_fieldDef.m_name))
/*     */         continue;
/* 214 */       String pattern = null;
/* 215 */       Vector values = fd.m_values;
/* 216 */       int num = values.size();
/* 217 */       if (num > 0)
/*     */       {
/* 219 */         pattern = (String)values.elementAt(0);
/*     */       }
/*     */ 
/* 222 */       String op = "=";
/* 223 */       Vector ops = fd.m_operators;
/* 224 */       num = ops.size();
/* 225 */       if (num > 0)
/*     */       {
/* 227 */         op = (String)ops.elementAt(0);
/*     */       }
/*     */ 
/* 230 */       String type = fd.m_fieldDef.m_type;
/* 231 */       String msgStub = null;
/* 232 */       if (type.equals("date"))
/*     */       {
/*     */         try
/*     */         {
/* 236 */           msgStub = "apValueDateParseError";
/* 237 */           Date dt = LocaleResources.parseDate(val, this.m_context);
/* 238 */           Date ct = LocaleResources.parseDate(pattern, this.m_context);
/* 239 */           if (op.equals(">="))
/*     */           {
/* 241 */             isMatch = dt.getTime() >= ct.getTime();
/* 242 */             msgStub = "apDateValueMismatchNotGreater";
/*     */           }
/* 244 */           else if (op.equals("<"))
/*     */           {
/* 246 */             isMatch = dt.getTime() < ct.getTime();
/* 247 */             msgStub = "apDateValueMismatchNotLess";
/*     */           }
/* 249 */           else if (op.equals("="))
/*     */           {
/* 251 */             isMatch = dt.getTime() == ct.getTime();
/* 252 */             msgStub = "apDateValueMismatchNotEqual";
/*     */           }
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 257 */           prevMsg = IdcMessageFactory.lc(e);
/* 258 */           isMatch = false;
/*     */         }
/*     */         finally
/*     */         {
/* 263 */           pattern = LocaleResources.localizeDate(pattern, this.m_context);
/*     */         }
/*     */       }
/* 266 */       else if (type.equals("int"))
/*     */       {
/* 268 */         long intVal = NumberUtils.parseLong(val, 0L);
/* 269 */         long intPattern = NumberUtils.parseLong(pattern, 0L);
/* 270 */         if (op.equals(">="))
/*     */         {
/* 272 */           isMatch = intVal >= intPattern;
/* 273 */           msgStub = "apIntValueMismatchNotGreater";
/*     */         }
/* 275 */         else if (op.equals("<"))
/*     */         {
/* 277 */           isMatch = intVal < intPattern;
/* 278 */           msgStub = "apIntValueMismatchNotLess";
/*     */         }
/* 280 */         else if (op.equals("="))
/*     */         {
/* 282 */           isMatch = intVal == intPattern;
/* 283 */           msgStub = "apIntValueMismatchNotEqual";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 288 */         isMatch = StringUtils.matchChars(val, pattern, false, false, wildCard.charAt(0), wildCard.charAt(1));
/* 289 */         msgStub = "apValuePatternMismatch";
/*     */       }
/*     */ 
/* 292 */       if (isMatch)
/*     */         continue;
/* 294 */       errMsg = IdcMessageFactory.lc(prevMsg, msgStub, new Object[] { name, val, pattern });
/* 295 */       break;
/*     */     }
/*     */ 
/* 299 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 305 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ComponentValidator
 * JD-Core Version:    0.5.4
 */