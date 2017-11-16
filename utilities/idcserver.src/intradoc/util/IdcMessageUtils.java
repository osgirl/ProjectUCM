/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.PrintWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public class IdcMessageUtils
/*     */   implements IdcMessageFactoryInterface
/*     */ {
/*     */   public static boolean m_isInit;
/*     */   public static int m_flags;
/*     */   public static BasicFormatter m_utcOdbcDateFormat;
/*  37 */   public static int m_maxParentStackLookBackCount = 15;
/*     */   public static IdcAppendableFactory m_defaultFactory;
/*     */   public static GenericTracingCallback m_traceCallback;
/*     */   public static final int F_USE_LEGACY_OBJECTS = 1;
/*     */   public static final int F_ESCAPE = 2;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  47 */     if (m_isInit)
/*     */     {
/*  49 */       return;
/*     */     }
/*     */ 
/*  52 */     init(null, null, null);
/*     */   }
/*     */ 
/*     */   public static synchronized void init(BasicFormatter utcOdbcFormat, IdcAppendableFactory factory, GenericTracingCallback callback)
/*     */   {
/*  60 */     if ((m_isInit) && (utcOdbcFormat == null) && (factory == null) && (callback == null))
/*     */     {
/*  63 */       return;
/*     */     }
/*     */ 
/*  66 */     IdcMessage.m_defaultFlags = m_flags;
/*     */ 
/*  68 */     if ((m_utcOdbcDateFormat == null) && (utcOdbcFormat == null))
/*     */     {
/*  70 */       m_utcOdbcDateFormat = new DateBasicFormatter();
/*     */     }
/*  72 */     if (utcOdbcFormat != null)
/*     */     {
/*  74 */       m_utcOdbcDateFormat = utcOdbcFormat;
/*     */     }
/*     */ 
/*  77 */     if ((m_defaultFactory == null) && (factory == null))
/*     */     {
/*  79 */       m_defaultFactory = new IdcSimpleAppendableFactory();
/*     */     }
/*  81 */     else if (factory != null)
/*     */     {
/*  83 */       m_defaultFactory = factory;
/*     */     }
/*     */ 
/*  86 */     if ((m_traceCallback == null) && (callback == null))
/*     */     {
/*  88 */       IdcMessage.m_defaultTraceCallback = IdcMessageUtils.m_traceCallback = new SimpleTracingCallback();
/*     */     }
/*  90 */     else if (callback != null)
/*     */     {
/*  92 */       IdcMessage.m_defaultTraceCallback = IdcMessageUtils.m_traceCallback = callback;
/*     */     }
/*  94 */     m_isInit = true;
/*     */   }
/*     */ 
/*     */   public static IdcMessage parseMessage(IdcMessageFactoryInterface factory, CharSequence seq)
/*     */   {
/*  99 */     return (IdcMessage)decodeMessageInternal(factory, seq, 0);
/*     */   }
/*     */ 
/*     */   public static IdcAppendableBase appendMessage(IdcAppendableBase a, IdcMessage msg, int flags)
/*     */   {
/* 105 */     return appendMessage(a, msg, flags, 0);
/*     */   }
/*     */ 
/*     */   protected static IdcAppendableBase appendMessage(IdcAppendableBase a, IdcMessage msg, int flags, int escapeDepth)
/*     */   {
/* 111 */     while (msg != null)
/*     */     {
/* 113 */       if (msg.m_msgEncoded != null)
/*     */       {
/* 115 */         if (!msg.m_msgEncoded.startsWith("!"))
/*     */         {
/* 120 */           appendUnencodedMessage(a, msg.m_msgEncoded, flags, escapeDepth + 1);
/*     */         }
/*     */         else
/*     */         {
/* 124 */           appendEscaped(a, msg.m_msgEncoded, flags, escapeDepth);
/*     */         }
/*     */       }
/* 127 */       else if (msg.m_stringKey != null)
/*     */       {
/* 129 */         appendMessage(a, msg.m_stringKey, msg.m_args, flags, escapeDepth);
/*     */       }
/* 131 */       else if ((msg.m_msgLocalized != null) || (msg.m_msgSimple != null))
/*     */       {
/* 134 */         String tmp = msg.m_msgLocalized;
/* 135 */         if (tmp == null)
/*     */         {
/* 137 */           tmp = msg.m_msgSimple;
/*     */         }
/* 139 */         appendUnencodedMessage(a, tmp, flags, escapeDepth + 1);
/*     */       }
/*     */       else
/*     */       {
/* 144 */         appendMessage(a, null, flags);
/*     */       }
/* 146 */       msg = msg.m_prior;
/*     */     }
/* 148 */     return a;
/*     */   }
/*     */ 
/*     */   public static IdcAppendableBase appendUnencodedMessage(IdcAppendableBase a, String msg, int flags)
/*     */   {
/* 154 */     return appendUnencodedMessage(a, msg, flags, 1);
/*     */   }
/*     */ 
/*     */   protected static IdcAppendableBase appendUnencodedMessage(IdcAppendableBase a, String msg, int flags, int escapeDepth)
/*     */   {
/* 160 */     a.append("!$");
/* 161 */     appendEscaped(a, msg, flags, escapeDepth);
/* 162 */     return a;
/*     */   }
/*     */ 
/*     */   protected static IdcAppendableBase appendMessage(IdcAppendableBase a, String key, Object[] args, int flags, int escapeDepth)
/*     */   {
/* 168 */     if (key == null)
/*     */     {
/* 170 */       key = "syNullPointerException";
/* 171 */       if ((m_flags & 0x1) != 0)
/*     */       {
/* 173 */         throw new AssertionError("!$Found a null message key.");
/*     */       }
/*     */     }
/* 176 */     if (key.length() == 0)
/*     */     {
/* 178 */       m_traceCallback.report(6, new Object[] { "LocaleUtils.appendMessage() called with empty key." });
/*     */ 
/* 180 */       return a;
/*     */     }
/* 182 */     if (key.charAt(0) == '!')
/*     */     {
/* 184 */       init();
/* 185 */       m_traceCallback.report(6, new Object[] { "LocaleUtils.appendMessage() called with encoded message instead of a key: " + key });
/*     */     }
/*     */     else
/*     */     {
/* 191 */       a.append('!');
/*     */     }
/* 193 */     a.append(key);
/* 194 */     for (int i = 0; (args != null) && (i < args.length); ++i)
/*     */     {
/* 196 */       appendEscaped(a, ",", flags, escapeDepth);
/* 197 */       String arg = null;
/* 198 */       if (args[i] == null)
/*     */       {
/* 200 */         arg = "(null)";
/*     */       }
/* 202 */       else if (args[i] instanceof Date)
/*     */       {
/* 204 */         init();
/*     */         try
/*     */         {
/* 210 */           m_utcOdbcDateFormat.format(a, args[i], null, 0);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 214 */           throw new AssertionError(e);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*     */         int j;
/*     */         Iterator i$;
/* 217 */         if (args[i] instanceof Collection)
/*     */         {
/* 219 */           Collection argCollection = (Collection)args[i];
/* 220 */           j = 0;
/* 221 */           for (i$ = argCollection.iterator(); i$.hasNext(); ) { Object item = i$.next();
/*     */ 
/* 223 */             if (j++ > 0)
/*     */             {
/* 225 */               appendEscaped(a, ",", flags, escapeDepth + 1);
/*     */             }
/* 227 */             if (item instanceof IdcMessage)
/*     */             {
/* 230 */               appendMessage(a, (IdcMessage)item, flags, escapeDepth + 1);
/*     */             }
/*     */             else
/*     */             {
/* 234 */               appendEscaped(a, item.toString(), flags, escapeDepth + 1);
/*     */             } }
/*     */ 
/*     */         }
/* 238 */         else if (args[i] instanceof Object[])
/*     */         {
/* 241 */           Object[] argAsArray = (Object[])(Object[])args[i];
/* 242 */           for (int j = 0; j < argAsArray.length; ++j)
/*     */           {
/* 244 */             if (j > 0)
/*     */             {
/* 246 */               appendEscaped(a, ",", flags, escapeDepth + 1);
/*     */             }
/* 248 */             Object item = argAsArray[j];
/* 249 */             if (item instanceof IdcMessage)
/*     */             {
/* 251 */               appendMessage(a, (IdcMessage)item, flags, escapeDepth + 1);
/*     */             }
/*     */             else
/*     */             {
/* 255 */               appendEscaped(a, item.toString(), flags, escapeDepth + 1);
/*     */             }
/*     */           }
/*     */         }
/* 259 */         else if (args[i] instanceof IdcMessage)
/*     */         {
/* 261 */           appendMessage(a, (IdcMessage)args[i], flags, escapeDepth + 1);
/*     */         }
/*     */         else
/*     */         {
/* 265 */           arg = args[i].toString();
/*     */         }
/*     */       }
/* 267 */       if (arg == null)
/*     */         continue;
/* 269 */       appendEscaped(a, arg, flags, escapeDepth + 1);
/*     */     }
/*     */ 
/* 272 */     return a;
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc()
/*     */   {
/* 277 */     IdcMessage msg = new IdcMessage(m_traceCallback, m_flags, null, null, null, null, null, null, (Object[])null);
/*     */ 
/* 281 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage()
/*     */   {
/* 286 */     return lc();
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(String key, Object[] args)
/*     */   {
/* 291 */     IdcMessage msg = new IdcMessage(m_traceCallback, m_flags, null, null, null, null, null, key, args);
/*     */ 
/* 295 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(String key, Object[] args)
/*     */   {
/* 300 */     return lc(key, args);
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(IdcMessage prior, String key, Object[] args)
/*     */   {
/* 305 */     IdcMessage msg = new IdcMessage(m_traceCallback, m_flags, null, prior, null, null, null, key, args);
/*     */ 
/* 309 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(IdcMessage prior, String key, Object[] args)
/*     */   {
/* 314 */     return lc(prior, key, args);
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(Throwable t)
/*     */   {
/* 319 */     IdcMessage msg = new IdcMessage(m_traceCallback, m_flags, t, null, null, null, null, null, (Object[])null);
/*     */ 
/* 323 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(Throwable t)
/*     */   {
/* 328 */     return lc(t);
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(Throwable prior, String key, Object[] args)
/*     */   {
/* 333 */     IdcMessage msg = new IdcMessage(m_traceCallback, m_flags, null, null, prior, null, null, key, args);
/*     */ 
/* 337 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(Throwable prior, String key, Object[] args)
/*     */   {
/* 342 */     return lc(prior, key, args);
/*     */   }
/*     */ 
/*     */   public static Object decodeMessageInternal(IdcMessageFactoryInterface factory, CharSequence msg, int flags)
/*     */   {
/* 347 */     init();
/* 348 */     if ((m_flags & 0x1) != 0)
/*     */     {
/* 350 */       m_traceCallback.report(7, new Object[] { "decoding " + msg });
/*     */     }
/*     */ 
/* 353 */     ArrayList msgList = null;
/* 354 */     IdcMessage topMsg = factory.newIdcMessage();
/* 355 */     Object msgObj = null;
/* 356 */     if ((flags & 0x1) != 0)
/*     */     {
/* 358 */       msgObj = msgList = new ArrayList();
/*     */     }
/*     */     else
/*     */     {
/* 362 */       msgObj = topMsg;
/*     */     }
/* 364 */     if ((msg == null) || (msg.length() == 0))
/*     */     {
/* 366 */       if (msgList != null)
/*     */       {
/* 368 */         return msgList;
/*     */       }
/* 370 */       if (msg == null)
/*     */       {
/* 372 */         topMsg.m_stringKey = "syNullPointerException";
/* 373 */         if ((m_flags & 0x1) != 0)
/*     */         {
/* 375 */           throw new NullPointerException();
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 380 */         topMsg.m_msgLocalized = "";
/*     */       }
/* 382 */       return topMsg;
/*     */     }
/* 384 */     char firstChar = msg.charAt(0);
/* 385 */     if (firstChar != '!')
/*     */     {
/* 387 */       init();
/* 388 */       IdcAppendableBase warning = m_defaultFactory.getIdcAppendable(msg, 0);
/* 389 */       warning.append("Illegal message string '");
/* 390 */       appendEscaped(warning, msg, 0, 1);
/* 391 */       warning.append("'.");
/* 392 */       if ((m_flags & 0x1) != 0)
/*     */       {
/* 394 */         AssertionError e = new AssertionError("!$" + warning.toString());
/* 395 */         init();
/* 396 */         m_traceCallback.report(7, new Object[] { e });
/* 397 */         throw e;
/*     */       }
/* 399 */       init();
/* 400 */       m_traceCallback.report(6, new Object[] { warning.toString() });
/* 401 */       warning = null;
/* 402 */       msg = "!$" + msg;
/*     */     }
/*     */ 
/* 405 */     ArrayList msgArgs = null;
/* 406 */     int length = msg.length();
/* 407 */     char state = 'I';
/* 408 */     init();
/* 409 */     IdcAppendableBase buf = m_defaultFactory.getIdcAppendable(msg, 4096);
/*     */ 
/* 411 */     int bufLength = 0;
/*     */ 
/* 417 */     for (int i = 1; i < length; ++i)
/*     */     {
/* 419 */       char c = msg.charAt(i);
/* 420 */       switch (state)
/*     */       {
/*     */       case 'I':
/* 423 */         buf = reset(m_defaultFactory, buf);
/* 424 */         bufLength = 0;
/* 425 */         if (c == '$')
/*     */         {
/* 427 */           state = 'L';
/*     */         }
/* 430 */         else if (c == '\\')
/*     */         {
/* 432 */           state = 'm';
/* 433 */           msgArgs = new ArrayList();
/*     */         }
/*     */         else
/*     */         {
/* 438 */           state = 'M';
/* 439 */           msgArgs = new ArrayList();
/* 440 */           --i;
/* 441 */         }break;
/*     */       case 'P':
/* 444 */         if (c == ')')
/*     */         {
/* 446 */           state = 'M';
/* 447 */           --i;
/*     */         }
/*     */         else
/*     */         {
/* 451 */           buf.append(c);
/* 452 */           ++bufLength;
/*     */         }
/* 454 */         break;
/*     */       case 'm':
/* 456 */         if (c == 's')
/*     */         {
/* 458 */           c = '$';
/*     */         }
/* 460 */         buf.append(c);
/* 461 */         ++bufLength;
/* 462 */         state = 'M';
/*     */ 
/* 464 */         if (i + 1 != length)
/*     */           continue;
/* 466 */         msgArgs.add(getTrimmedString(buf, 0, -1));
/* 467 */         buf = reset(m_defaultFactory, buf);
/* 468 */         bufLength = 0;
/* 469 */         msgObj = addMessageArgs(factory, msgObj, msgArgs); break;
/*     */       case 'M':
/* 473 */         if ((c == '(') && (msgArgs.size() == 0))
/*     */         {
/* 475 */           buf.append(c);
/* 476 */           ++bufLength;
/* 477 */           state = 'P';
/*     */         }
/* 479 */         else if (c == ',')
/*     */         {
/* 481 */           msgArgs.add(getTrimmedString(buf, 0, -1));
/* 482 */           buf = reset(m_defaultFactory, buf);
/* 483 */           bufLength = 0;
/* 484 */           if (i + 1 != length)
/*     */             continue;
/* 486 */           msgArgs.add("");
/* 487 */           msgObj = addMessageArgs(factory, msgObj, msgArgs);
/*     */         }
/* 490 */         else if ((c == '!') || (i + 1 == length) || ((c == '\n') && (msgArgs.size() == 0)))
/*     */         {
/* 492 */           if ((c != '!') && (c != '\n'))
/*     */           {
/* 494 */             buf.append(c);
/* 495 */             bufLength = 0;
/*     */           }
/* 497 */           msgArgs.add(getTrimmedString(buf, 0, -1));
/* 498 */           buf = reset(m_defaultFactory, buf);
/* 499 */           bufLength = 0;
/* 500 */           msgObj = addMessageArgs(factory, msgObj, msgArgs);
/*     */ 
/* 502 */           if (c == '\n')
/*     */           {
/* 509 */             buf.append(c);
/* 510 */             ++bufLength;
/* 511 */             state = 'L';
/*     */           }
/*     */           else
/*     */           {
/* 515 */             state = 'I';
/*     */           }
/*     */         }
/* 518 */         else if (c == '\\')
/*     */         {
/* 520 */           state = 'm';
/*     */         }
/*     */         else
/*     */         {
/* 524 */           buf.append(c);
/* 525 */           ++bufLength;
/*     */         }
/* 527 */         break;
/*     */       case 'l':
/* 529 */         buf.append(c);
/* 530 */         ++bufLength;
/* 531 */         state = 'L';
/* 532 */         break;
/*     */       case 'L':
/* 534 */         if ((c == '!') || (i + 1 == length))
/*     */         {
/* 536 */           if (c != '!')
/*     */           {
/* 538 */             buf.append(c);
/* 539 */             ++bufLength;
/*     */           }
/* 541 */           if (bufLength > 0)
/*     */           {
/* 543 */             msgObj = addMessageArgs(factory, msgObj, buf.toString());
/*     */           }
/*     */ 
/* 546 */           state = 'I';
/*     */         }
/* 548 */         else if (c == '\\')
/*     */         {
/* 550 */           state = 'l';
/*     */         }
/*     */         else
/*     */         {
/* 554 */           buf.append(c);
/* 555 */           ++bufLength;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 560 */     m_defaultFactory.releaseIdcAppendable(buf);
/* 561 */     if (msgList != null)
/*     */     {
/* 563 */       IdcVector v = new IdcVector(msgList);
/* 564 */       return v;
/*     */     }
/* 566 */     if ((m_flags & 0x1) != 0)
/*     */     {
/* 568 */       init();
/* 569 */       m_traceCallback.report(7, new Object[] { "decoded to \n" + topMsg.m_prior });
/*     */     }
/* 571 */     return topMsg.m_prior;
/*     */   }
/*     */ 
/*     */   protected static IdcAppendableBase reset(IdcAppendableFactory factory, IdcAppendableBase buf)
/*     */   {
/* 576 */     if (!buf.truncate(0))
/*     */     {
/* 578 */       return factory.getIdcAppendable(null, 0);
/*     */     }
/* 580 */     return buf;
/*     */   }
/*     */ 
/*     */   protected static String getTrimmedString(IdcAppendableBase appendable, int start, int end)
/*     */   {
/* 585 */     String str = appendable.toString();
/* 586 */     if (end == -1)
/*     */     {
/* 588 */       end = str.length();
/*     */     }
/* 590 */     while ((start < end) && (str.charAt(start) <= ' '))
/*     */     {
/* 592 */       ++start;
/*     */     }
/* 594 */     while ((start < end) && (str.charAt(end - 1) <= ' '))
/*     */     {
/* 596 */       --end;
/*     */     }
/* 598 */     return str.substring(start, end);
/*     */   }
/*     */ 
/*     */   protected static Object addMessageArgs(IdcMessageFactoryInterface factory, Object msg, Object msgArgs)
/*     */   {
/* 603 */     List msgList = null;
/*     */ 
/* 605 */     String key = null;
/* 606 */     String[] args = null;
/* 607 */     if (msgArgs instanceof List)
/*     */     {
/* 609 */       key = (String)((List)msgArgs).get(0);
/* 610 */       args = new String[((List)msgArgs).size() - 1];
/* 611 */       for (int j = 0; j < args.length; ++j)
/*     */       {
/* 613 */         args[j] = ((String)((List)msgArgs).get(j + 1));
/*     */       }
/*     */     }
/* 616 */     if (msg instanceof List)
/*     */     {
/* 618 */       msgList = (List)msg;
/* 619 */       if (msgArgs instanceof String)
/*     */       {
/* 621 */         msgList.add(msgArgs);
/*     */       }
/*     */       else
/*     */       {
/* 625 */         Object[] objs = new Object[2];
/* 626 */         objs[0] = key;
/* 627 */         objs[1] = args;
/* 628 */         msgList.add(objs);
/*     */       }
/* 630 */       return msgList;
/*     */     }
/*     */     IdcMessage idcMsg;
/*     */     IdcMessage idcMsg;
/* 632 */     if (msgArgs instanceof List)
/*     */     {
/* 634 */       idcMsg = factory.newIdcMessage(key, (Object[])args);
/*     */     }
/*     */     else
/*     */     {
/* 638 */       idcMsg = factory.newIdcMessage();
/* 639 */       idcMsg.m_msgSimple = ((String)msgArgs);
/*     */     }
/* 641 */     ((IdcMessage)msg).m_prior = idcMsg;
/* 642 */     return idcMsg;
/*     */   }
/*     */ 
/*     */   public static IdcAppendableBase appendEscaped(IdcAppendableBase a, CharSequence arg, int flags)
/*     */   {
/* 648 */     return appendEscaped(a, arg, flags, 1);
/*     */   }
/*     */ 
/*     */   protected static IdcAppendableBase appendEscaped(IdcAppendableBase a, CharSequence arg, int flags, int escapeDepth)
/*     */   {
/* 654 */     if (escapeDepth == 0)
/*     */     {
/* 656 */       a.append(arg);
/* 657 */       return a;
/*     */     }
/*     */ 
/* 660 */     int length = arg.length();
/* 661 */     int end = length - 1;
/* 662 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 664 */       char c = arg.charAt(i);
/*     */ 
/* 668 */       boolean isEndDollar = (c == '$') && (i == end);
/* 669 */       boolean isSpecial = (isEndDollar) || (c == '!') || (c == ',') || (c == '\\');
/* 670 */       if (isSpecial)
/*     */       {
/* 672 */         for (int j = escapeDepth; j > 0; --j)
/*     */         {
/* 674 */           a.append('\\');
/*     */         }
/* 676 */         if (isEndDollar)
/*     */         {
/* 678 */           c = 's';
/*     */         }
/* 680 */         a.append(c);
/*     */       }
/*     */       else
/*     */       {
/* 684 */         a.append(c);
/*     */       }
/*     */     }
/* 687 */     return a;
/*     */   }
/*     */ 
/*     */   public static void printStackTrace(StackTraceElement[] parentTrace, Throwable t, String prefix, String causeText, PrintWriter w)
/*     */   {
/* 693 */     if (prefix == null)
/*     */     {
/* 695 */       prefix = "";
/*     */     }
/* 697 */     if (parentTrace != null)
/*     */     {
/* 699 */       w.print(prefix);
/* 700 */       w.print(causeText);
/*     */     }
/* 702 */     int parentLength = (parentTrace == null) ? 0 : parentTrace.length;
/* 703 */     int adjustTraceEndDiff = 0;
/* 704 */     if (parentLength > m_maxParentStackLookBackCount)
/*     */     {
/* 706 */       adjustTraceEndDiff = parentLength - m_maxParentStackLookBackCount;
/* 707 */       parentLength = m_maxParentStackLookBackCount;
/*     */     }
/* 709 */     w.println(t.toString());
/* 710 */     if (t instanceof IdcExceptionInterface)
/*     */     {
/* 713 */       Object scriptStackObj = ((IdcExceptionInterface)t).getContainerAttribute("scriptstack");
/* 714 */       if (scriptStackObj != null)
/*     */       {
/* 716 */         w.println("*ScriptStack " + scriptStackObj);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 722 */     StackTraceElement[] trace = t.getStackTrace();
/* 723 */     int traceEnd = trace.length - 1 - adjustTraceEndDiff;
/* 724 */     int originalTraceEnd = traceEnd;
/* 725 */     int parentEnd = parentLength - 1;
/* 726 */     while ((traceEnd >= 0) && (parentEnd >= 0))
/*     */     {
/* 728 */       StackTraceElement elem = trace[traceEnd];
/* 729 */       if ((parentTrace[parentEnd] != elem) && (!parentTrace[parentEnd].equals(elem)))
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/* 734 */       --traceEnd;
/* 735 */       --parentEnd;
/*     */     }
/* 737 */     if (originalTraceEnd == traceEnd)
/*     */     {
/* 739 */       traceEnd = trace.length - 1;
/*     */     }
/*     */ 
/* 742 */     for (int i = 0; i <= traceEnd; ++i)
/*     */     {
/* 744 */       StackTraceElement elem = trace[i];
/* 745 */       w.print(prefix);
/* 746 */       w.print("        at ");
/* 747 */       w.println(elem.toString());
/*     */     }
/* 749 */     int more = trace.length - 1 - traceEnd;
/* 750 */     if (more > 0)
/*     */     {
/* 752 */       w.print(prefix);
/* 753 */       w.print("        ... ");
/* 754 */       w.print(more);
/* 755 */       w.println(" more");
/*     */     }
/* 757 */     w.flush();
/*     */ 
/* 762 */     causeText = "Caused by: ";
/* 763 */     if (t instanceof IdcExceptionInterface)
/*     */     {
/* 765 */       List causes = ((IdcExceptionInterface)t).getCauses();
/* 766 */       int size = causes.size();
/* 767 */       if ((size > 1) && (parentTrace != null))
/*     */       {
/* 769 */         prefix = prefix + "  ";
/*     */       }
/* 771 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 773 */         Throwable cause = (Throwable)causes.get(i);
/* 774 */         printStackTrace(trace, cause, prefix, causeText, w);
/* 775 */         if (i != 0)
/*     */           continue;
/* 777 */         causeText = "      and: ";
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 783 */       Throwable cause = t.getCause();
/* 784 */       if (cause == null)
/*     */         return;
/* 786 */       printStackTrace(trace, cause, prefix, "Caused by: ", w);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 793 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87111 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcMessageUtils
 * JD-Core Version:    0.5.4
 */