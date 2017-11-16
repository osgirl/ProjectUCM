/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.text.FieldPosition;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.SimpleTimeZone;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TimeZoneFormat
/*     */ {
/*     */   public static final int FORMAT_BY_ID = 1;
/*     */   public static final int FORMAT_UTC_PLUS_OFFSET = 2;
/*     */   public static final int FORMAT_RAW_OFFSET = 4;
/*     */   public static final int FORMAT_IN_DST = 8;
/*     */   protected Map<String, TimeZone> m_timeZones;
/*     */   public String[] m_utcTexts;
/*     */ 
/*     */   @Deprecated
/*     */   public final IdcTimeZone m_utc;
/*     */ 
/*     */   public TimeZoneFormat()
/*     */   {
/*  38 */     this.m_timeZones = new HashMap();
/*  39 */     this.m_utcTexts = new String[] { "UTC", "GMT" };
/*     */ 
/*  41 */     this.m_utc = IdcTimeZone.wrap(new SimpleTimeZone(0, this.m_utcTexts[0]));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void addTimeZone(String name, IdcTimeZone tz)
/*     */   {
/*  48 */     this.m_timeZones.put(name, tz);
/*     */   }
/*     */ 
/*     */   public void addTZ(String name, TimeZone tz)
/*     */   {
/*  53 */     this.m_timeZones.put(name, tz);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public StringBuffer format(Object o, StringBuffer b, FieldPosition p)
/*     */   {
/*  61 */     if (o instanceof TimeZone)
/*     */     {
/*  63 */       TimeZone tz = (TimeZone)o;
/*  64 */       String name = format(tz, 0);
/*  65 */       b.append(name);
/*     */     }
/*  67 */     return b;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase appendFormat(Object o, IdcAppendableBase b, Date dte, int formatOptions)
/*     */   {
/*  74 */     if (o instanceof TimeZone)
/*     */     {
/*  76 */       TimeZone tz = (TimeZone)o;
/*  77 */       boolean isDLS = tz.inDaylightTime(dte);
/*     */ 
/*  80 */       if ((formatOptions & 0x6) == 0)
/*     */       {
/*  83 */         String name = format(tz, (isDLS) ? 8 : 0);
/*  84 */         b.append(name);
/*     */       }
/*     */       else
/*     */       {
/*  88 */         if ((formatOptions & 0x2) != 0)
/*     */         {
/*  90 */           b.append(this.m_utcTexts[0]);
/*     */         }
/*  92 */         int offset = tz.getRawOffset();
/*     */ 
/*  95 */         offset /= 60000;
/*     */ 
/*  97 */         if (isDLS)
/*     */         {
/* 100 */           offset += 60;
/*     */         }
/*     */ 
/* 104 */         String flag = "+";
/* 105 */         if (offset < 0)
/*     */         {
/* 107 */           flag = "-";
/* 108 */           offset *= -1;
/*     */         }
/* 110 */         b.append(flag);
/* 111 */         int hours = offset / 60;
/* 112 */         int minutes = offset % 60;
/* 113 */         if (hours < 10)
/*     */         {
/* 115 */           b.append("0");
/*     */         }
/* 117 */         b.append("" + hours);
/* 118 */         if (minutes < 10)
/*     */         {
/* 120 */           b.append("0");
/*     */         }
/* 122 */         b.append("" + minutes);
/*     */       }
/*     */     }
/* 125 */     return b;
/*     */   }
/*     */ 
/*     */   public IdcAppendable appendFormat(Object o, IdcAppendable b, Date dte, int formatOptions)
/*     */   {
/* 132 */     appendFormat(o, b, dte, formatOptions);
/* 133 */     return b;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String format(IdcTimeZone tz, boolean isDST)
/*     */   {
/* 140 */     return tz.getDisplayName(isDST, 0);
/*     */   }
/*     */ 
/*     */   public String format(TimeZone tz, int flags)
/*     */   {
/* 148 */     return tz.getDisplayName((flags & 0x8) != 0, 0);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String format(IdcTimeZone tz, Date d)
/*     */   {
/* 155 */     return format(tz, tz.inDaylightTime(d));
/*     */   }
/*     */ 
/*     */   public String format(TimeZone tz, Date d, int flags)
/*     */   {
/* 160 */     return format(tz, flags | ((tz.inDaylightTime(d)) ? 8 : 0));
/*     */   }
/*     */ 
/*     */   public TimeZone parseTimeZone(ParseStringLocation parseLocation, String text, int flags)
/*     */   {
/* 165 */     if (parseLocation == null)
/*     */     {
/* 167 */       parseLocation = new ParseStringLocation();
/*     */     }
/* 169 */     ParseStringLocation p = parseLocation;
/*     */ 
/* 171 */     if (text.substring(p.m_index).startsWith("STZ:"))
/*     */     {
/* 173 */       TimeZone tz = IdcTimeZone.wrap(readConfiguration(text.substring(p.m_index)));
/* 174 */       if (tz != null)
/*     */       {
/* 176 */         p.m_index = text.length();
/*     */       }
/* 178 */       return tz;
/*     */     }
/* 180 */     if (text.substring(p.m_index).startsWith("ZI:"))
/*     */     {
/* 182 */       String name = text.substring(p.m_index + 3);
/* 183 */       TimeZone tz = LocaleResources.getTimeZone(name, null);
/* 184 */       if (tz != null)
/*     */       {
/* 186 */         return tz;
/*     */       }
/* 188 */       tz = TimeZone.getTimeZone(name);
/* 189 */       return tz;
/*     */     }
/*     */ 
/* 192 */     char state = 'T';
/* 193 */     int index = p.m_index;
/* 194 */     int l = text.length();
/* 195 */     IdcStringBuilder tz = new IdcStringBuilder();
/* 196 */     int count = 0;
/* 197 */     while ((state != 'F') && (index < l))
/*     */     {
/* 199 */       char thischar = text.charAt(index);
/* 200 */       switch (state)
/*     */       {
/*     */       case 'T':
/* 203 */         if ((count == 0) && (((Character.isDigit(thischar)) || (thischar == '+') || (thischar == '-'))))
/*     */         {
/* 207 */           p.m_elementLocation = "TimezoneOffset";
/* 208 */           state = 'H';
/* 209 */           if ((thischar == '+') || (thischar == '-'))
/*     */           {
/* 211 */             tz.append(thischar);
/* 212 */             ++index;
/*     */           }
/*     */         }
/* 215 */         else if ((Character.isLetterOrDigit(thischar)) || (thischar == '.') || (thischar == '/') || (thischar == '\\') || (thischar == '_') || ((count == 0) && (((thischar == '+') || (thischar == '-')))))
/*     */         {
/* 219 */           p.m_elementLocation = "TimezoneText";
/* 220 */           tz.append(thischar);
/* 221 */           ++count;
/* 222 */           ++index;
/*     */         }
/*     */         else
/*     */         {
/* 226 */           state = 'F';
/* 227 */           String tzString = tz.toString();
/* 228 */           for (int i = 0; i < this.m_utcTexts.length; ++i)
/*     */           {
/* 231 */             if (!tzString.equalsIgnoreCase(this.m_utcTexts[i]))
/*     */               continue;
/* 233 */             state = 'S';
/* 234 */             break;
/*     */           }
/*     */         }
/*     */ 
/* 238 */         break;
/*     */       case 'S':
/* 241 */         if ((thischar == '+') || (thischar == '-'))
/*     */         {
/* 243 */           p.m_elementLocation = "TimezoneOffset";
/* 244 */           tz.append(thischar);
/* 245 */           state = 'H';
/* 246 */           count = 0;
/* 247 */           ++index;
/*     */         }
/* 249 */         else if (Character.isSpaceChar(thischar))
/*     */         {
/* 251 */           state = 'F';
/*     */         }
/*     */         else
/*     */         {
/* 255 */           state = 'F';
/* 256 */           index = -1;
/*     */         }
/* 258 */         break;
/*     */       case 'H':
/* 261 */         if ((count == 2) && (thischar == ':'))
/*     */         {
/* 263 */           tz.append(thischar);
/* 264 */           ++index;
/* 265 */           count = 0;
/* 266 */           state = 'M';
/*     */         }
/* 268 */         else if ((count == 2) && (Character.isDigit(thischar)))
/*     */         {
/* 270 */           count = 0;
/* 271 */           state = 'M';
/*     */         }
/* 273 */         else if ((count >= 2) || (!Character.isDigit(thischar)))
/*     */         {
/* 275 */           state = 'F';
/* 276 */           index = -1;
/*     */         }
/*     */         else
/*     */         {
/* 280 */           tz.append(thischar);
/* 281 */           ++index;
/* 282 */           ++count;
/*     */         }
/* 284 */         break;
/*     */       case 'M':
/* 287 */         if (!Character.isDigit(thischar))
/*     */         {
/* 289 */           state = 'F';
/* 290 */           index = -1;
/*     */         }
/*     */         else
/*     */         {
/* 294 */           tz.append(thischar);
/* 295 */           ++index;
/* 296 */           ++count;
/* 297 */           if (count == 2)
/*     */           {
/* 299 */             state = 'F';
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 305 */     if (index == -1)
/*     */     {
/* 307 */       p.setErrorMessage(p.m_index, -2, "!syUnableToParseTimeZone");
/* 308 */       return null;
/*     */     }
/* 310 */     p.m_index = index;
/* 311 */     p.m_elementLocation = "TimezoneLookup";
/* 312 */     TimeZone zone = determineTimeZone(tz.toString());
/* 313 */     if (zone == null)
/*     */     {
/* 315 */       String msg = LocaleUtils.encodeMessage("syIllegalTimeZone", null, tz.toString());
/*     */ 
/* 317 */       p.setErrorMessage(index, -5, msg);
/*     */     }
/* 319 */     return zone;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public IdcTimeZone parseTimeZone(String text)
/*     */   {
/* 330 */     ParseStringLocation parseLocation = new ParseStringLocation();
/* 331 */     return parseTimeZoneWithPattern(text, parseLocation);
/*     */   }
/*     */ 
/*     */   public Object parseObject(String text)
/*     */     throws ParseStringException
/*     */   {
/* 337 */     return parseTimeZoneDirect(text);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public IdcTimeZone parseTimeZoneDirect(String text)
/*     */     throws ParseStringException
/*     */   {
/* 345 */     ParseStringLocation parseLocation = new ParseStringLocation();
/* 346 */     TimeZone itz = parseTimeZoneWithPattern(text, parseLocation);
/* 347 */     if (parseLocation.m_state != 0)
/*     */     {
/* 349 */       String errMsg = parseLocation.m_errMsg;
/* 350 */       if (errMsg == null)
/*     */       {
/* 352 */         errMsg = LocaleUtils.encodeMessage("syUnableToParseDate", null, text);
/*     */       }
/* 354 */       throw new ParseStringException(parseLocation, errMsg);
/*     */     }
/* 356 */     return IdcTimeZone.wrap(itz);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public IdcTimeZone parseTimeZoneWithPattern(String text, ParseStringLocation p)
/*     */   {
/* 363 */     TimeZone tz = parseTimeZone(p, text, 0);
/* 364 */     return IdcTimeZone.wrap(tz);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public IdcTimeZone getTimeZone(String zone)
/*     */     throws ServiceException
/*     */   {
/* 374 */     TimeZone tz = determineTimeZone(zone);
/* 375 */     if (tz == null)
/*     */     {
/* 377 */       String msg = LocaleUtils.encodeMessage("syIllegalTimeZone", null, zone);
/*     */ 
/* 379 */       throw new ServiceException(msg);
/*     */     }
/* 381 */     return IdcTimeZone.wrap(tz);
/*     */   }
/*     */ 
/*     */   protected TimeZone determineTimeZone(String zone)
/*     */   {
/* 386 */     if (zone.length() == 0)
/*     */     {
/* 388 */       return null;
/*     */     }
/* 390 */     TimeZone tz = (TimeZone)this.m_timeZones.get(zone);
/* 391 */     if (tz != null)
/*     */     {
/* 393 */       return tz;
/*     */     }
/*     */ 
/* 396 */     int offset = 0;
/* 397 */     int tzOffset = 0;
/* 398 */     char firstChar = zone.charAt(0);
/* 399 */     String utcText = null;
/* 400 */     boolean startsWithUtc = false;
/* 401 */     for (int i = 0; i < this.m_utcTexts.length; ++i)
/*     */     {
/* 403 */       if (!zone.startsWith(this.m_utcTexts[i]))
/*     */         continue;
/* 405 */       utcText = this.m_utcTexts[i];
/* 406 */       startsWithUtc = true;
/*     */     }
/*     */ 
/* 409 */     int zoneLen = zone.length();
/* 410 */     if ((startsWithUtc) || (firstChar == '-') || (firstChar == '+') || (Character.isDigit(firstChar)))
/*     */     {
/* 413 */       if (startsWithUtc)
/*     */       {
/* 415 */         offset = utcText.length();
/*     */       }
/* 417 */       if (offset < zoneLen)
/*     */       {
/* 419 */         int sign = zone.charAt(offset);
/*     */ 
/* 421 */         if ((sign != 43) && (sign != 45))
/*     */         {
/* 423 */           if (startsWithUtc)
/*     */           {
/* 425 */             return null;
/*     */           }
/* 427 */           sign = 43;
/*     */         }
/*     */         else
/*     */         {
/* 431 */           ++offset;
/*     */         }
/* 433 */         if (zoneLen - offset < 4)
/*     */         {
/* 436 */           return null;
/*     */         }
/*     */ 
/* 440 */         int minuteOffset = offset + 2;
/* 441 */         String d1 = zone.substring(offset, minuteOffset);
/* 442 */         if (zone.charAt(minuteOffset) == ':')
/*     */         {
/* 444 */           ++minuteOffset;
/*     */         }
/* 446 */         String d2 = zone.substring(minuteOffset);
/*     */ 
/* 448 */         int i1 = NumberUtils.parseInteger(d1, -1);
/* 449 */         int i2 = NumberUtils.parseInteger(d2, -1);
/*     */ 
/* 451 */         if ((i1 == -1) || (i2 == -1) || (d1.length() < 2) || (d2.length() < 2))
/*     */         {
/* 453 */           return null;
/*     */         }
/*     */ 
/* 456 */         tzOffset = 60 * i1 + i2;
/* 457 */         if (sign == 45)
/*     */         {
/* 459 */           tzOffset *= -1;
/*     */         }
/*     */       }
/* 462 */       tz = new SimpleTimeZone(tzOffset * 60 * 1000, zone);
/*     */     }
/*     */ 
/* 465 */     if (tz == null)
/*     */     {
/* 467 */       tz = TimeZone.getTimeZone(zone);
/*     */     }
/* 469 */     if (tz != null)
/*     */     {
/* 471 */       this.m_timeZones.put(zone, tz);
/*     */     }
/*     */ 
/* 474 */     return tz;
/*     */   }
/*     */ 
/*     */   protected TimeZone readConfiguration(String configuration)
/*     */   {
/* 479 */     int mph = 3600000;
/*     */ 
/* 483 */     configuration = configuration.substring("STZ:".length());
/* 484 */     Vector v = StringUtils.parseArray(configuration, ',', '^');
/* 485 */     int size = v.size();
/* 486 */     String[] argStrings = new String[size];
/* 487 */     v.copyInto(argStrings);
/* 488 */     if ((size == 1) || (size == 2))
/*     */     {
/* 490 */       float offset = Float.valueOf(argStrings[0]).floatValue();
/*     */       SimpleTimeZone stz;
/* 491 */       TimeZone tz = stz = new SimpleTimeZone((int)(offset * 3600000.0F), "");
/* 492 */       if (size == 2)
/*     */       {
/* 494 */         stz.setStartYear(Integer.parseInt(argStrings[1]));
/*     */       }
/*     */     }
/* 497 */     else if ((size == 9) || (size == 10))
/*     */     {
/* 499 */       int[] args = new int[size];
/*     */       try
/*     */       {
/* 503 */         args[0] = (int)(Float.valueOf(argStrings[0]).floatValue() * 3600000.0F);
/*     */ 
/* 505 */         args[1] = Integer.parseInt(argStrings[1]);
/* 506 */         args[2] = Integer.parseInt(argStrings[2]);
/* 507 */         args[3] = Integer.parseInt(argStrings[3]);
/* 508 */         args[4] = (int)(Float.valueOf(argStrings[4]).floatValue() * 3600000.0F);
/*     */ 
/* 510 */         args[5] = Integer.parseInt(argStrings[5]);
/* 511 */         args[6] = Integer.parseInt(argStrings[6]);
/* 512 */         args[7] = Integer.parseInt(argStrings[7]);
/* 513 */         args[8] = (int)(Float.valueOf(argStrings[8]).floatValue() * 3600000.0F);
/*     */         SimpleTimeZone stz;
/* 515 */         TimeZone tz = stz = new SimpleTimeZone(args[0], "", args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
/*     */ 
/* 517 */         if (size == 10)
/*     */         {
/* 519 */           stz.setStartYear(Integer.parseInt(argStrings[9]));
/*     */         }
/*     */       }
/*     */       catch (NumberFormatException e)
/*     */       {
/* 524 */         return null;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 529 */       return null;
/*     */     }
/*     */     TimeZone tz;
/*     */     SimpleTimeZone stz;
/* 532 */     return tz;
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/* 538 */     TimeZoneFormat tzf = new TimeZoneFormat();
/* 539 */     tzf.m_timeZones = this.m_timeZones;
/* 540 */     return tzf;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 545 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84494 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TimeZoneFormat
 * JD-Core Version:    0.5.4
 */