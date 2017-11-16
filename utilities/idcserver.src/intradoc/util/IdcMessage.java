/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcMessage
/*     */   implements Cloneable
/*     */ {
/*     */   public static final int F_EXTRA_ASSERTIONS = 1;
/*     */   protected static GenericTracingCallback m_defaultTraceCallback;
/*     */   protected static int m_defaultFlags;
/*     */   public GenericTracingCallback m_traceCallback;
/*     */   public int m_flags;
/*     */   public boolean m_isFinalizedMsg;
/*     */   public IdcMessage m_prior;
/*     */   public Throwable m_throwable;
/*     */   public Throwable m_priorThrowable;
/*     */   public String m_stringKey;
/*     */   public Object[] m_args;
/*     */   public String m_msgSimple;
/*     */   public String m_msgEncoded;
/*     */   public String m_msgLocalized;
/*     */ 
/*     */   public IdcMessage()
/*     */   {
/* 146 */     initWarnings();
/*     */   }
/*     */ 
/*     */   public IdcMessage(String key, Object[] args)
/*     */   {
/* 152 */     this(m_defaultTraceCallback, m_defaultFlags, null, null, null, null, null, key, args);
/* 153 */     initWarnings();
/*     */   }
/*     */ 
/*     */   public IdcMessage(IdcMessage prior, String key, Object[] args)
/*     */   {
/* 159 */     this(m_defaultTraceCallback, m_defaultFlags, null, prior, null, null, null, key, args);
/* 160 */     initWarnings();
/*     */   }
/*     */ 
/*     */   public IdcMessage(Throwable t)
/*     */   {
/* 166 */     this(m_defaultTraceCallback, m_defaultFlags, t, null, null, null, null, null, (Object[])null);
/* 167 */     initWarnings();
/*     */   }
/*     */ 
/*     */   protected IdcMessage createMessageListFromThrowable(Throwable t)
/*     */   {
/* 172 */     IdcMessage newmsg = null;
/* 173 */     if (t instanceof IdcMessageContainer)
/*     */     {
/* 175 */       IdcMessageContainer mc = (IdcMessageContainer)t;
/* 176 */       Object attr = mc.getContainerAttribute("isWrapper");
/* 177 */       if (!"true".equals(attr))
/*     */       {
/* 179 */         IdcMessage internalMsg = mc.getIdcMessage();
/* 180 */         if (internalMsg != null)
/*     */         {
/* 182 */           newmsg = internalMsg.makeClone();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 192 */     if (t instanceof InvocationTargetException)
/*     */     {
/* 194 */       InvocationTargetException invocationError = (InvocationTargetException)t;
/* 195 */       t = invocationError.getTargetException();
/*     */     }
/*     */ 
/* 198 */     if (newmsg == null)
/*     */     {
/* 200 */       newmsg = createMessageFromThrowable(t);
/*     */     }
/*     */ 
/* 203 */     Class cl = t.getClass();
/* 204 */     List associatedExceptions = null;
/*     */     try
/*     */     {
/* 207 */       Method me = cl.getMethod("getCauses", new Class[0]);
/* 208 */       Object obj = me.invoke(t, new Object[0]);
/* 209 */       if (obj instanceof List)
/*     */       {
/* 211 */         associatedExceptions = (List)obj;
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 216 */       if ((ignore instanceof ClassCastException) && 
/* 218 */         ((this.m_flags & 0x1) != 0))
/*     */       {
/* 220 */         report(7, "getCauses() on object of type " + cl.getName() + " doesn't return a List.", ignore);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 228 */     if (associatedExceptions == null)
/*     */     {
/* 230 */       Throwable cause = t.getCause();
/* 231 */       if (cause != null)
/*     */       {
/* 233 */         associatedExceptions = new ArrayList();
/* 234 */         associatedExceptions.add(cause);
/*     */       }
/*     */     }
/*     */ 
/* 238 */     if (associatedExceptions != null)
/*     */     {
/* 240 */       IdcMessage curmsg = newmsg;
/* 241 */       for (int i = 0; i < associatedExceptions.size(); ++i)
/*     */       {
/* 243 */         Throwable t2 = (Throwable)associatedExceptions.get(i);
/* 244 */         if (curmsg == null)
/*     */         {
/* 246 */           newmsg = curmsg = createMessageListFromThrowable(t2);
/*     */         }
/*     */         else
/*     */         {
/* 250 */           boolean isWrapped = false;
/* 251 */           if (t2 instanceof IdcMessageContainer)
/*     */           {
/* 253 */             isWrapped = ((IdcMessageContainer)t2).getContainerAttribute("isWrapped") != null;
/*     */           }
/*     */ 
/* 256 */           if (isWrapped)
/*     */           {
/* 258 */             IdcMessage tmp = createMessageListFromThrowable(t2);
/* 259 */             curmsg.copyFrom(tmp);
/*     */           }
/*     */           else
/*     */           {
/* 263 */             curmsg = curmsg.walkToEnd();
/* 264 */             curmsg.m_prior = createMessageListFromThrowable(t2);
/*     */           }
/* 266 */           curmsg = curmsg.m_prior;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 271 */     if (newmsg == null)
/*     */     {
/* 276 */       newmsg = new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, "syExceptionType", new Object[] { t.getClass().getName() });
/*     */ 
/* 280 */       assert (showEmptyMessageWarning(t, "IdcMessage(Throwable)"));
/* 281 */       return newmsg;
/*     */     }
/* 283 */     return newmsg;
/*     */   }
/*     */ 
/*     */   public IdcMessage(Throwable prior, String key, Object[] args)
/*     */   {
/* 289 */     this(m_defaultTraceCallback, m_defaultFlags, null, null, prior, null, null, key, args);
/* 290 */     initWarnings();
/*     */   }
/*     */ 
/*     */   public IdcMessage(GenericTracingCallback callback, int flags, Throwable thisThrowable, IdcMessage priorMsg, Throwable priorThrowable, String msgEncoded, String msgSimple, String key, Object[] args)
/*     */   {
/* 299 */     this.m_traceCallback = callback;
/* 300 */     this.m_flags = flags;
/*     */ 
/* 302 */     this.m_prior = priorMsg;
/* 303 */     if (thisThrowable != null)
/*     */     {
/* 305 */       IdcMessage newmsg = createMessageListFromThrowable(thisThrowable);
/* 306 */       copyFrom(newmsg);
/*     */     }
/* 308 */     if (priorThrowable != null)
/*     */     {
/* 310 */       this.m_prior = createMessageListFromThrowable(priorThrowable);
/* 311 */       this.m_priorThrowable = priorThrowable;
/* 312 */       this.m_prior.m_traceCallback = callback;
/* 313 */       this.m_prior.m_flags = flags;
/*     */     }
/*     */ 
/* 316 */     if (this.m_traceCallback == null)
/*     */     {
/* 318 */       this.m_traceCallback = m_defaultTraceCallback;
/* 319 */       report(4, null, new AssertionError("IdcMessage constructor should have a tracing callback."));
/*     */     }
/*     */ 
/* 326 */     if (msgEncoded != null)
/*     */     {
/* 328 */       this.m_msgEncoded = msgEncoded;
/*     */     }
/* 330 */     if (msgSimple != null)
/*     */     {
/* 332 */       this.m_msgSimple = msgSimple;
/*     */     }
/* 334 */     if (key != null)
/*     */     {
/* 336 */       this.m_stringKey = key;
/*     */     }
/* 338 */     if (args == null)
/*     */       return;
/* 340 */     this.m_args = args;
/*     */   }
/*     */ 
/*     */   public void initWarnings()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected IdcMessage createMessageFromThrowable(Throwable t)
/*     */   {
/*     */     try
/*     */     {
/* 362 */       if (t == null)
/*     */       {
/* 365 */         return new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, "syServiceNullPointer", (Object[])null);
/*     */       }
/*     */ 
/* 370 */       IdcMessage msg = null;
/* 371 */       if (t instanceof InvocationTargetException)
/*     */       {
/* 375 */         InvocationTargetException invocationError = (InvocationTargetException)t;
/* 376 */         t = invocationError.getTargetException();
/*     */ 
/* 378 */         assert (showInvocationExceptionWarning(invocationError));
/*     */       }
/* 380 */       if ((t instanceof NullPointerException) && (t.getMessage() == null))
/*     */       {
/* 383 */         msg = new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, "syNullPointerException", (Object[])null);
/*     */       }
/* 388 */       else if (t instanceof RuntimeException)
/*     */       {
/* 390 */         String errMsg = t.getMessage();
/* 391 */         if (errMsg != null)
/*     */         {
/* 393 */           if (!errMsg.startsWith("!"))
/*     */           {
/* 395 */             errMsg = t.getClass().getName() + ": " + errMsg;
/* 396 */             msg = new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, "syServiceRuntime", new Object[] { errMsg });
/*     */           }
/*     */           else
/*     */           {
/* 403 */             msg = new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, errMsg, null, null, (Object[])null);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 412 */         if (t instanceof IdcMessageContainer)
/*     */         {
/* 414 */           IdcMessageContainer container = (IdcMessageContainer)t;
/* 415 */           Object attr = container.getContainerAttribute("isWrapper");
/* 416 */           if ("true".equals(attr))
/*     */           {
/* 418 */             return null;
/*     */           }
/* 420 */           msg = container.getIdcMessage();
/*     */         }
/* 422 */         if (msg == null)
/*     */         {
/* 424 */           String enc = t.getMessage();
/* 425 */           if (enc != null)
/*     */           {
/* 427 */             msg = new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, null, (Object[])null);
/*     */ 
/* 432 */             if (!enc.startsWith("!"))
/*     */             {
/* 434 */               String className = t.getClass().getName();
/* 435 */               if ((!className.startsWith("intradoc")) && (!className.startsWith("idc")))
/*     */               {
/* 440 */                 enc = t.toString();
/* 441 */                 StringBuffer buffer = new StringBuffer();
/* 442 */                 buffer.append("!syJavaExceptionWrapper,");
/*     */ 
/* 446 */                 int encLength = enc.length();
/* 447 */                 for (int i = 0; i < encLength; ++i)
/*     */                 {
/* 449 */                   char ch = enc.charAt(i);
/* 450 */                   if ((ch == '\\') || (ch == '!') || (ch == ','))
/*     */                   {
/* 452 */                     buffer.append('\\');
/*     */                   }
/* 454 */                   buffer.append(ch);
/*     */                 }
/*     */ 
/* 467 */                 enc = buffer.toString();
/*     */               }
/*     */             }
/* 470 */             msg.m_msgEncoded = enc;
/*     */           }
/*     */         }
/* 473 */         if (msg == null)
/*     */         {
/* 476 */           msg = new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, "syExceptionType", new Object[] { t.getClass().getName() });
/*     */ 
/* 480 */           assert (showEmptyMessageWarning(t, "IdcMessage.createMessageFromThrowable()"));
/*     */         }
/*     */       }
/* 483 */       return msg;
/*     */     }
/*     */     catch (Throwable errorProcessingError)
/*     */     {
/* 488 */       report(6, "Unexpected throwable while processing errors.", errorProcessingError);
/*     */     }
/* 490 */     return new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, "syUnknownSystemError", (Object[])null);
/*     */   }
/*     */ 
/*     */   public void setPrior(IdcMessage msg)
/*     */   {
/* 503 */     walkToEnd().m_prior = msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage walkToEnd()
/*     */   {
/* 508 */     return walkToEnd(new HashMap());
/*     */   }
/*     */ 
/*     */   protected IdcMessage walkToEnd(Map map)
/*     */   {
/* 513 */     if (this.m_prior == null)
/*     */     {
/* 515 */       return this;
/*     */     }
/* 517 */     if (map.get(this) != null)
/*     */     {
/* 519 */       report(6, "Recursive message list found.", new AssertionError());
/*     */ 
/* 521 */       this.m_prior = null;
/* 522 */       return this;
/*     */     }
/* 524 */     return this.m_prior.walkToEnd(map);
/*     */   }
/*     */ 
/*     */   protected boolean showEmptyMessageWarning(Throwable t, String functionName)
/*     */   {
/* 529 */     if ((this.m_flags & 0x1) != 0)
/*     */     {
/* 531 */       report(7, "A Throwable without a message was found in " + functionName, t);
/*     */     }
/*     */ 
/* 534 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean showInvocationExceptionWarning(Throwable t)
/*     */   {
/* 539 */     if ((this.m_flags & 0x1) != 0)
/*     */     {
/* 541 */       report(7, "Unwrapped invocation exception being reported.", t);
/*     */     }
/*     */ 
/* 544 */     return true;
/*     */   }
/*     */ 
/*     */   public static IdcMessage appendAssociatedMessages(List list, IdcMessage top)
/*     */   {
/* 549 */     IdcMessage cur = top;
/* 550 */     int length = list.size();
/* 551 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 553 */       Object o = list.get(i);
/* 554 */       if (!o instanceof Throwable)
/*     */         continue;
/* 556 */       Throwable t = (Throwable)o;
/* 557 */       IdcMessage tmp = new IdcMessage(top.m_traceCallback, top.m_flags, t, null, null, null, null, null, (Object[])null);
/*     */ 
/* 561 */       if (cur == null)
/*     */       {
/* 563 */         cur = tmp;
/*     */       }
/*     */       else
/*     */       {
/* 567 */         cur.m_prior = tmp;
/* 568 */         cur = tmp;
/*     */       }
/*     */     }
/*     */ 
/* 572 */     return cur;
/*     */   }
/*     */ 
/*     */   public void appendTo(StringBuffer a) throws IOException
/*     */   {
/* 577 */     a.append("key: ");
/* 578 */     a.append(this.m_stringKey);
/* 579 */     a.append("\nargs: \n");
/* 580 */     if (this.m_args != null)
/*     */     {
/* 582 */       for (int i = 0; i < this.m_args.length; ++i)
/*     */       {
/* 584 */         a.append("\t");
/* 585 */         a.append(this.m_args[i].toString());
/* 586 */         a.append("\n");
/*     */       }
/*     */     }
/* 589 */     a.append("msgSimple: ");
/* 590 */     a.append(this.m_msgSimple);
/* 591 */     a.append("\nencoded: ");
/* 592 */     a.append(this.m_msgEncoded);
/* 593 */     a.append("\nlocalized: " + this.m_msgLocalized);
/* 594 */     a.append("\n");
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 600 */     StringBuffer buf = new StringBuffer();
/* 601 */     IdcMessage tmp = this;
/* 602 */     IdcMessage first = null;
/*     */     try
/*     */     {
/* 605 */       while (tmp != null)
/*     */       {
/* 607 */         tmp.appendTo(buf);
/* 608 */         if ((tmp == tmp.m_prior) || (tmp == first))
/*     */         {
/* 610 */           throw new AssertionError("!$Self-referencing message stack detected.");
/*     */         }
/* 612 */         if (first == null)
/*     */         {
/* 614 */           first = this;
/*     */         }
/* 616 */         tmp = tmp.m_prior;
/* 617 */         if (tmp == null)
/*     */           continue;
/* 619 */         buf.append("prior: ");
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 625 */       throw new AssertionError(e);
/*     */     }
/* 627 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public IdcMessage makeClone()
/*     */   {
/* 633 */     IdcMessage msg = new IdcMessage(this.m_traceCallback, this.m_flags, null, null, null, null, null, null, (Object[])null);
/*     */ 
/* 637 */     msg.copyFrom(this);
/* 638 */     return msg;
/*     */   }
/*     */ 
/*     */   public void copyFrom(IdcMessage sourceMessage)
/*     */   {
/* 643 */     this.m_traceCallback = sourceMessage.m_traceCallback;
/* 644 */     this.m_flags = sourceMessage.m_flags;
/* 645 */     this.m_stringKey = sourceMessage.m_stringKey;
/* 646 */     this.m_prior = sourceMessage.m_prior;
/* 647 */     this.m_priorThrowable = sourceMessage.m_priorThrowable;
/* 648 */     this.m_throwable = sourceMessage.m_throwable;
/* 649 */     if (sourceMessage.m_args != null)
/*     */     {
/* 652 */       this.m_args = new Object[sourceMessage.m_args.length];
/* 653 */       System.arraycopy(sourceMessage.m_args, 0, this.m_args, 0, sourceMessage.m_args.length);
/*     */     }
/* 655 */     this.m_msgSimple = sourceMessage.m_msgSimple;
/* 656 */     this.m_msgEncoded = sourceMessage.m_msgEncoded;
/* 657 */     this.m_msgLocalized = sourceMessage.m_msgLocalized;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 662 */     return (this.m_prior == null) && (this.m_throwable == null) && (this.m_stringKey == null) && (this.m_msgSimple == null) && (this.m_msgEncoded == null) && (this.m_msgLocalized == null);
/*     */   }
/*     */ 
/*     */   protected void report(int level, Object arg0, Object arg1)
/*     */   {
/* 672 */     if (this.m_traceCallback == null)
/*     */     {
/* 674 */       if (arg1 == null)
/*     */       {
/* 676 */         System.err.println("IdcMessage: " + arg0);
/*     */       }
/* 678 */       else if (arg0 != null)
/*     */       {
/* 680 */         System.err.println("IdcMessage: " + arg0 + " " + arg1);
/*     */       }
/* 682 */       if ((arg1 != null) && (arg1 instanceof Throwable))
/*     */       {
/* 684 */         ((Throwable)arg1).printStackTrace();
/*     */       }
/* 686 */       return;
/*     */     }
/*     */     Object[] args;
/* 689 */     if ((arg0 != null) && (arg1 != null))
/*     */     {
/* 691 */       Object[] args = new Object[2];
/* 692 */       args[0] = arg0;
/* 693 */       args[1] = arg1;
/*     */     }
/*     */     else
/*     */     {
/* 697 */       args = new Object[1];
/* 698 */       if (arg0 != null)
/*     */       {
/* 700 */         args[0] = arg0;
/*     */       }
/*     */       else
/*     */       {
/* 704 */         args[0] = arg1;
/*     */       }
/*     */     }
/* 707 */     this.m_traceCallback.report(level, args);
/*     */   }
/*     */ 
/*     */   public boolean equals(Object o2)
/*     */   {
/* 713 */     if ((o2 != null) && (o2 instanceof IdcMessage))
/*     */     {
/* 722 */       String s1 = toString();
/* 723 */       String s2 = o2.toString();
/* 724 */       return s1.equals(s2);
/*     */     }
/* 726 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 731 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89534 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcMessage
 * JD-Core Version:    0.5.4
 */