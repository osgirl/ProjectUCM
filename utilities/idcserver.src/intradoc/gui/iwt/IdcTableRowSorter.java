/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.data.FieldInfo;
/*     */ import java.text.ParseException;
/*     */ import java.util.Comparator;
/*     */ import java.util.Date;
/*     */ import javax.swing.table.TableModel;
/*     */ import javax.swing.table.TableRowSorter;
/*     */ 
/*     */ public class IdcTableRowSorter extends TableRowSorter
/*     */ {
/*     */   public UdlPanel m_panel;
/*     */ 
/*     */   public IdcTableRowSorter(TableModel m)
/*     */   {
/*  36 */     super(m);
/*     */   }
/*     */ 
/*     */   public void init(UdlPanel p)
/*     */   {
/*  41 */     this.m_panel = p;
/*     */   }
/*     */ 
/*     */   public Comparator getComparator(int column)
/*     */   {
/*  47 */     FieldInfo fi = this.m_panel.getColumnInfo(column);
/*  48 */     IdcComparator cmp = this.m_panel.getComparator(fi.m_name);
/*  49 */     if (cmp == null)
/*     */     {
/*  51 */       if (fi.m_type == 3)
/*     */       {
/*  53 */         cmp = new Object()
/*     */         {
/*     */           public int compare(Object o1, Object o2)
/*     */           {
/*  57 */             String s1 = (String)o1;
/*  58 */             String s2 = (String)o2;
/*     */             try
/*     */             {
/*  61 */               Long l1 = Long.valueOf(Long.parseLong(s1));
/*  62 */               Long l2 = Long.valueOf(Long.parseLong(s2));
/*  63 */               return l1.compareTo(l2);
/*     */             }
/*     */             catch (NumberFormatException e) {
/*     */             }
/*  67 */             return s1.toLowerCase().compareTo(s2.toLowerCase());
/*     */           }
/*     */ 
/*     */         };
/*     */       }
/*  72 */       else if ((fi.m_type == 5) && (this.m_panel.m_dateFormat != null))
/*     */       {
/*  74 */         cmp = new Object()
/*     */         {
/*     */           public int compare(Object o1, Object o2)
/*     */           {
/*  78 */             String s1 = (String)o1;
/*  79 */             String s2 = (String)o2;
/*     */             try
/*     */             {
/*  82 */               Date d1 = IdcTableRowSorter.this.m_panel.m_dateFormat.parseDate(s1);
/*  83 */               Date d2 = IdcTableRowSorter.this.m_panel.m_dateFormat.parseDate(s2);
/*  84 */               return d1.compareTo(d2);
/*     */             }
/*     */             catch (ParseException e) {
/*     */             }
/*  88 */             return s1.toLowerCase().compareTo(s2.toLowerCase());
/*     */           }
/*     */ 
/*     */         };
/*     */       }
/*     */       else
/*     */       {
/*  95 */         cmp = new IdcComparator()
/*     */         {
/*     */           public int compare(Object o1, Object o2)
/*     */           {
/*  99 */             String s1 = (String)o1;
/* 100 */             String s2 = (String)o2;
/* 101 */             return s1.toLowerCase().compareTo(s2.toLowerCase());
/*     */           }
/*     */         };
/*     */       }
/*     */     }
/*     */ 
/* 107 */     return cmp;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 112 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.IdcTableRowSorter
 * JD-Core Version:    0.5.4
 */