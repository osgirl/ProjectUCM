/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import intradoc.util.IdcExceptionInterface;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.text.ParseException;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ParseStringException extends ParseException
/*     */   implements IdcExceptionInterface
/*     */ {
/*     */   public ParseStringLocation m_parseLocation;
/*     */   public ServiceException m_helper;
/*     */ 
/*     */   public ParseStringException()
/*     */   {
/*  44 */     super("", 0);
/*  45 */     this.m_helper = new ServiceException((Throwable)null);
/*  46 */     this.m_parseLocation = null;
/*     */   }
/*     */ 
/*     */   public ParseStringException(String msg)
/*     */   {
/*  51 */     super(msg, 0);
/*  52 */     this.m_helper = new ServiceException(msg);
/*  53 */     this.m_parseLocation = null;
/*     */   }
/*     */ 
/*     */   public ParseStringException(ParseStringLocation parseLocation, String msg)
/*     */   {
/*  58 */     super(msg, (parseLocation != null) ? parseLocation.determineErrorIndex() : 0);
/*  59 */     this.m_helper = new ServiceException(msg);
/*  60 */     this.m_parseLocation = parseLocation;
/*     */   }
/*     */ 
/*     */   public ParseStringException(String msg, int index)
/*     */   {
/*  65 */     super(msg, index);
/*  66 */     this.m_helper = new ServiceException(msg);
/*  67 */     this.m_parseLocation = new ParseStringLocation();
/*  68 */     this.m_parseLocation.setErrorState(index, -1);
/*     */   }
/*     */ 
/*     */   public void init(Throwable t, int errorCode, IdcMessage msg, String msgEncoded)
/*     */   {
/*  73 */     this.m_helper.init(t, errorCode, msg, msgEncoded);
/*     */   }
/*     */ 
/*     */   public Throwable initCause(Throwable t)
/*     */   {
/*  79 */     this.m_helper.initCause(t);
/*  80 */     return this;
/*     */   }
/*     */ 
/*     */   public Throwable getCause()
/*     */   {
/*  86 */     return this.m_helper.getCause();
/*     */   }
/*     */ 
/*     */   public Throwable addCause(Throwable t)
/*     */   {
/*  91 */     this.m_helper.addCause(t);
/*  92 */     return this;
/*     */   }
/*     */ 
/*     */   public List<Throwable> getCauses()
/*     */   {
/*  97 */     return this.m_helper.getCauses();
/*     */   }
/*     */ 
/*     */   public void setIdcMessage(IdcMessage msg)
/*     */   {
/* 102 */     this.m_helper.setIdcMessage(msg);
/*     */   }
/*     */ 
/*     */   public IdcMessage getIdcMessage()
/*     */   {
/* 107 */     return this.m_helper.getIdcMessage();
/*     */   }
/*     */ 
/*     */   public void setContainerAttribute(String arg, Object value)
/*     */   {
/* 112 */     this.m_helper.setContainerAttribute(arg, value);
/*     */   }
/*     */ 
/*     */   public Object getContainerAttribute(String arg)
/*     */   {
/* 117 */     return this.m_helper.getContainerAttribute(arg);
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase getIdcAppendable(Object target, int flags)
/*     */   {
/* 122 */     return this.m_helper.getIdcAppendable(target, flags);
/*     */   }
/*     */ 
/*     */   public void releaseIdcAppendable(IdcAppendableBase a)
/*     */   {
/* 127 */     this.m_helper.releaseIdcAppendable(a);
/*     */   }
/*     */ 
/*     */   public void wrapIn(Throwable wrapper)
/*     */   {
/* 132 */     wrapper.initCause(this);
/* 133 */     setContainerAttribute("isWrapped", "true");
/*     */   }
/*     */ 
/*     */   public String getMessage()
/*     */   {
/* 139 */     return this.m_helper.getMessage();
/*     */   }
/*     */ 
/*     */   public void printStackTrace()
/*     */   {
/* 145 */     this.m_helper.printStackTrace();
/*     */   }
/*     */ 
/*     */   public void printStackTrace(PrintStream s)
/*     */   {
/* 151 */     this.m_helper.printStackTrace(s);
/*     */   }
/*     */ 
/*     */   public void printStackTrace(PrintWriter w)
/*     */   {
/* 157 */     this.m_helper.printStackTrace(w);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 162 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78445 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ParseStringException
 * JD-Core Version:    0.5.4
 */