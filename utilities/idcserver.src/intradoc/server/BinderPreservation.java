/*     */ package intradoc.server;
/*     */ 
/*     */ import java.util.HashSet;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class BinderPreservation
/*     */ {
/*     */   protected Set m_preserves;
/*     */   protected Set m_preserveLocals;
/*     */   protected Set m_preserveResultSets;
/*     */ 
/*     */   public BinderPreservation()
/*     */   {
/*  26 */     this.m_preserves = null;
/*  27 */     this.m_preserveLocals = null;
/*  28 */     this.m_preserveResultSets = null;
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/*  33 */     BinderPreservation bp = new BinderPreservation();
/*  34 */     bp.m_preserves = new HashSet();
/*  35 */     if (this.m_preserves != null)
/*     */     {
/*  37 */       bp.m_preserves.addAll(this.m_preserves);
/*     */     }
/*  39 */     bp.m_preserveLocals = new HashSet();
/*  40 */     if (this.m_preserveLocals != null)
/*     */     {
/*  42 */       bp.m_preserveLocals.addAll(this.m_preserveLocals);
/*     */     }
/*  44 */     bp.m_preserveResultSets = new HashSet();
/*  45 */     if (this.m_preserveResultSets != null)
/*     */     {
/*  47 */       bp.m_preserveResultSets.addAll(this.m_preserveResultSets);
/*     */     }
/*  49 */     return bp;
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  54 */     this.m_preserves = null;
/*  55 */     this.m_preserveLocals = null;
/*  56 */     this.m_preserveResultSets = null;
/*     */   }
/*     */ 
/*     */   public Set createSet(String strSet)
/*     */   {
/*  61 */     Set set = new HashSet();
/*  62 */     if ((strSet != null) && (strSet.length() != 0))
/*     */     {
/*  64 */       String[] strElements = strSet.split(",");
/*  65 */       for (int i = 0; i < strElements.length; ++i)
/*     */       {
/*  67 */         set.add(strElements[i]);
/*     */       }
/*     */     }
/*  70 */     return set;
/*     */   }
/*     */ 
/*     */   public void setPreserves(String strPreserves)
/*     */   {
/*  75 */     this.m_preserves = createSet(strPreserves);
/*     */   }
/*     */ 
/*     */   public Set getPreserves() {
/*  79 */     return this.m_preserves;
/*     */   }
/*     */ 
/*     */   public void setPreserveLocals(String strPreserveLocals)
/*     */   {
/*  84 */     this.m_preserveLocals = createSet(strPreserveLocals);
/*     */   }
/*     */ 
/*     */   public Set getPreserveLocals() {
/*  88 */     return this.m_preserveLocals;
/*     */   }
/*     */ 
/*     */   public void setPreserveResultSets(String strPreserveResultSets)
/*     */   {
/*  93 */     this.m_preserveResultSets = createSet(strPreserveResultSets);
/*     */   }
/*     */ 
/*     */   public Set getPreserveResultSets() {
/*  97 */     return this.m_preserveResultSets;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 103 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.BinderPreservation
 * JD-Core Version:    0.5.4
 */