/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcException extends Exception
/*     */   implements IdcMessageContainer, IdcExceptionInterface
/*     */ {
/*     */   public int m_errorCode;
/*     */   public IdcMessage m_message;
/*     */   public IdcAppendableFactory m_factory;
/*     */   public boolean m_isWrapper;
/*     */   public boolean m_isWrapped;
/*  45 */   public Map<String, Object> m_attributes = new HashMap();
/*     */ 
/*  50 */   public int m_protocolCode = -1;
/*     */ 
/*  55 */   protected ArrayList<Throwable> m_causeList = new ArrayList();
/*     */ 
/*     */   public IdcException()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected IdcException(Throwable t, IdcMessage msg)
/*     */   {
/*  76 */     init(t, 0, msg, null);
/*     */   }
/*     */ 
/*     */   protected IdcException(Throwable t, int errorCode, IdcMessage msg)
/*     */   {
/*  82 */     init(t, errorCode, msg, null);
/*     */   }
/*     */ 
/*     */   protected IdcException(Throwable t, int errorCode)
/*     */   {
/*  88 */     init(t, errorCode, null, null);
/*     */   }
/*     */ 
/*     */   public void init(Throwable t, int errorCode, IdcMessage msg, String msgEncoded)
/*     */   {
/*  95 */     if (t != null)
/*     */     {
/*  97 */       if ((msgEncoded == null) && (((msg == null) || (msg.isEmpty()))))
/*     */       {
/*  99 */         this.m_isWrapper = true;
/*     */       }
/* 101 */       initCause(t);
/*     */     }
/* 103 */     if ((msg != null) && (msg.m_priorThrowable != null))
/*     */     {
/* 105 */       Throwable currentCause = null;
/* 106 */       if (t != null)
/*     */       {
/* 108 */         currentCause = t.getCause();
/*     */       }
/* 110 */       if ((currentCause != msg.m_priorThrowable) && (t != msg.m_priorThrowable))
/*     */       {
/* 112 */         addCause(msg.m_priorThrowable);
/*     */ 
/* 117 */         msg.m_prior = null;
/*     */       }
/*     */     }
/* 120 */     if (errorCode != 0)
/*     */     {
/* 122 */       this.m_errorCode = errorCode;
/*     */     }
/* 124 */     if (errorCode == 0)
/*     */     {
/* 126 */       this.m_errorCode = -1;
/*     */     }
/* 128 */     this.m_message = msg;
/* 129 */     if (msgEncoded != null)
/*     */     {
/* 131 */       msg.m_msgEncoded = msgEncoded;
/*     */     }
/* 133 */     initFactory();
/*     */   }
/*     */ 
/*     */   public void initFactory()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printStackTrace(PrintStream s)
/*     */   {
/* 144 */     PrintWriter pw = new PrintWriter(s);
/* 145 */     IdcMessageUtils.printStackTrace(null, this, "", null, pw);
/* 146 */     pw.flush();
/*     */   }
/*     */ 
/*     */   public void printStackTrace(PrintWriter w)
/*     */   {
/* 152 */     IdcMessageUtils.printStackTrace(null, this, "", null, w);
/* 153 */     w.flush();
/*     */   }
/*     */ 
/*     */   public Throwable initCause(Throwable t)
/*     */   {
/* 159 */     addCause(t);
/* 160 */     if (t instanceof IdcException)
/*     */     {
/* 162 */       IdcException idce = (IdcException)t;
/* 163 */       this.m_errorCode = idce.m_errorCode;
/* 164 */       this.m_protocolCode = idce.m_protocolCode;
/*     */     }
/*     */ 
/* 167 */     return this;
/*     */   }
/*     */ 
/*     */   public Throwable addCause(Throwable t)
/*     */   {
/* 177 */     Throwable cause = getCause();
/* 178 */     if (cause == null)
/*     */     {
/* 180 */       super.initCause(t);
/*     */     }
/* 182 */     if ((cause != t) && (!this.m_causeList.contains(t)))
/*     */     {
/* 184 */       this.m_causeList.add(t);
/*     */     }
/* 186 */     return this;
/*     */   }
/*     */ 
/*     */   public List<Throwable> getCauses()
/*     */   {
/* 191 */     return this.m_causeList;
/*     */   }
/*     */ 
/*     */   public Throwable getCause()
/*     */   {
/* 197 */     Throwable cause = super.getCause();
/* 198 */     if ((cause == null) && (this.m_causeList.size() > 0))
/*     */     {
/* 200 */       cause = (Throwable)this.m_causeList.get(0);
/*     */     }
/* 202 */     return cause;
/*     */   }
/*     */ 
/*     */   public String getMessage()
/*     */   {
/* 208 */     if (this.m_message == null)
/*     */     {
/* 210 */       return super.getMessage();
/*     */     }
/*     */ 
/* 218 */     String msg = this.m_message.m_msgSimple;
/* 219 */     if (msg == null)
/*     */     {
/* 221 */       msg = this.m_message.m_msgEncoded;
/*     */     }
/* 223 */     if (this.m_message.m_msgEncoded != null)
/*     */     {
/* 225 */       return this.m_message.m_msgEncoded;
/*     */     }
/* 227 */     if ((msg == null) && (this.m_message != null))
/*     */     {
/* 229 */       IdcAppendableBase a = getIdcAppendable(this.m_message, 0);
/* 230 */       IdcMessageUtils.appendMessage(a, this.m_message, 0);
/* 231 */       msg = a.toString();
/*     */     }
/*     */ 
/* 234 */     return msg;
/*     */   }
/*     */ 
/*     */   public void setIdcMessage(IdcMessage msg)
/*     */   {
/* 239 */     this.m_message = msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage getIdcMessage()
/*     */   {
/* 244 */     return this.m_message;
/*     */   }
/*     */ 
/*     */   public IdcMessage appendAssociatedMessages(IdcMessage top)
/*     */   {
/* 249 */     return IdcMessage.appendAssociatedMessages(this.m_causeList, top);
/*     */   }
/*     */ 
/*     */   public void setContainerAttribute(String arg, Object value)
/*     */   {
/* 254 */     if (arg.equals("isWrapper"))
/*     */     {
/* 257 */       this.m_isWrapper = Boolean.parseBoolean(value.toString());
/*     */     }
/* 259 */     else if (arg.equals("isWrapped"))
/*     */     {
/* 261 */       this.m_isWrapped = Boolean.parseBoolean(value.toString());
/*     */     }
/*     */     else
/*     */     {
/* 265 */       this.m_attributes.put(arg, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object getContainerAttribute(String arg)
/*     */   {
/* 271 */     if ((this.m_isWrapper) && (arg.equals("isWrapper")))
/*     */     {
/* 273 */       return "true";
/*     */     }
/* 275 */     if ((this.m_isWrapped) && (arg.equals("isWrapped")))
/*     */     {
/* 277 */       return "true";
/*     */     }
/* 279 */     return this.m_attributes.get(arg);
/*     */   }
/*     */ 
/*     */   public void wrapIn(Throwable wrapper)
/*     */   {
/* 284 */     wrapper.initCause(this);
/* 285 */     setContainerAttribute("isWrapped", "true");
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase getIdcAppendable(Object target, int flags)
/*     */   {
/* 290 */     if (this.m_factory != null)
/*     */     {
/* 292 */       return this.m_factory.getIdcAppendable(target, flags);
/*     */     }
/* 294 */     return IdcMessageUtils.m_defaultFactory.getIdcAppendable(target, flags);
/*     */   }
/*     */ 
/*     */   public void releaseIdcAppendable(IdcAppendableBase a)
/*     */   {
/* 299 */     if (this.m_factory != null)
/*     */     {
/* 301 */       this.m_factory.releaseIdcAppendable(a);
/*     */     }
/* 303 */     IdcMessageUtils.m_defaultFactory.releaseIdcAppendable(a);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 308 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84403 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcException
 * JD-Core Version:    0.5.4
 */