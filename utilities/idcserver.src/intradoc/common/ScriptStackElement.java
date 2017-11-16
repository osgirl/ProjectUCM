/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ScriptStackElement
/*     */ {
/*  29 */   public String m_elementName = null;
/*     */ 
/*  34 */   public boolean m_hasSourceScript = false;
/*     */ 
/*  37 */   public boolean m_msgOnly = false;
/*     */ 
/*  42 */   public String m_stackMsg = null;
/*     */ 
/*  45 */   public ParseLocationInfo m_location = null;
/*     */ 
/*  48 */   public ParseLocationInfo m_callFromLocation = null;
/*     */ 
/*     */   public ScriptStackElement()
/*     */   {
/*  52 */     this.m_location = new ParseLocationInfo();
/*     */   }
/*     */ 
/*     */   public void copy(ScriptStackElement e)
/*     */   {
/*  57 */     this.m_elementName = e.m_elementName;
/*  58 */     this.m_hasSourceScript = e.m_hasSourceScript;
/*  59 */     this.m_msgOnly = e.m_msgOnly;
/*  60 */     this.m_stackMsg = e.m_stackMsg;
/*  61 */     this.m_location.copy(e.m_location);
/*  62 */     if (e.m_callFromLocation != null)
/*     */     {
/*  64 */       if (this.m_callFromLocation == null)
/*     */       {
/*  66 */         this.m_callFromLocation = new ParseLocationInfo();
/*     */       }
/*  68 */       this.m_callFromLocation.copy(e.m_callFromLocation);
/*     */     }
/*     */     else
/*     */     {
/*  72 */       if (this.m_callFromLocation == null)
/*     */         return;
/*  74 */       this.m_callFromLocation.reset();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  82 */     this.m_elementName = null;
/*  83 */     this.m_hasSourceScript = false;
/*  84 */     this.m_msgOnly = false;
/*  85 */     this.m_stackMsg = null;
/*  86 */     this.m_location.reset();
/*  87 */     if (this.m_callFromLocation == null)
/*     */       return;
/*  89 */     this.m_callFromLocation.reset();
/*     */   }
/*     */ 
/*     */   public String getStringPresentation()
/*     */   {
/*  98 */     Vector v = new IdcVector();
/*  99 */     v.addElement(this.m_elementName);
/* 100 */     v.addElement("" + this.m_hasSourceScript);
/* 101 */     v.addElement("" + this.m_msgOnly);
/*     */ 
/* 103 */     if (this.m_stackMsg != null)
/*     */     {
/* 105 */       v.addElement(this.m_stackMsg);
/*     */     }
/*     */     else
/*     */     {
/* 109 */       v.addElement("");
/*     */     }
/*     */ 
/* 112 */     String locationStr = "";
/* 113 */     if (this.m_location != null)
/*     */     {
/* 115 */       locationStr = this.m_location.getStringPresentation();
/*     */     }
/* 117 */     v.addElement(locationStr);
/*     */ 
/* 119 */     String cfLocationStr = "";
/* 120 */     if (this.m_callFromLocation != null)
/*     */     {
/* 122 */       cfLocationStr = this.m_callFromLocation.getStringPresentation();
/*     */     }
/* 124 */     v.addElement(cfLocationStr);
/*     */ 
/* 126 */     return StringUtils.createString(v, ',', '^');
/*     */   }
/*     */ 
/*     */   public void buildFromString(String str)
/*     */   {
/* 131 */     Vector v = StringUtils.parseArray(str, ',', '^');
/* 132 */     int size = v.size();
/* 133 */     if (size != 6)
/*     */     {
/* 135 */       Report.trace("idcDebug", "ScriptStackElement.buildFromString: Error -  incorrect vector size " + size + ".", null);
/*     */ 
/* 137 */       return;
/*     */     }
/*     */ 
/* 140 */     this.m_elementName = ((String)v.elementAt(0));
/* 141 */     this.m_hasSourceScript = StringUtils.convertToBool((String)v.elementAt(1), false);
/* 142 */     this.m_msgOnly = StringUtils.convertToBool((String)v.elementAt(2), false);
/* 143 */     this.m_stackMsg = ((String)v.elementAt(3));
/*     */ 
/* 145 */     String locStr = (String)v.elementAt(4);
/* 146 */     if (locStr.length() > 0)
/*     */     {
/* 148 */       this.m_location.buildFromString(locStr);
/*     */     }
/*     */     else
/*     */     {
/* 152 */       this.m_location.reset();
/*     */     }
/*     */ 
/* 155 */     locStr = (String)v.elementAt(5);
/* 156 */     if (locStr.length() > 0)
/*     */     {
/* 158 */       this.m_callFromLocation = new ParseLocationInfo();
/* 159 */       this.m_callFromLocation.buildFromString(locStr);
/*     */     }
/*     */     else
/*     */     {
/* 163 */       this.m_callFromLocation = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 169 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptStackElement
 * JD-Core Version:    0.5.4
 */