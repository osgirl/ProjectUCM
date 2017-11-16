/*     */ package intradoc.gui;
/*     */ 
/*     */ import java.awt.Dialog;
/*     */ import java.awt.SystemColor;
/*     */ import java.lang.reflect.Method;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ 
/*     */ public class CustomDialog extends JDialog
/*     */ {
/*     */   public CustomDialog(JFrame frame)
/*     */   {
/*  35 */     super(frame, true);
/*  36 */     setModalityType("DOCUMENT_MODAL");
/*  37 */     setBackground(SystemColor.control);
/*  38 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomDialog(Dialog frame)
/*     */   {
/*  43 */     super(frame, true);
/*  44 */     setModalityType("DOCUMENT_MODAL");
/*  45 */     setBackground(SystemColor.control);
/*  46 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomDialog(JFrame frame, boolean modal)
/*     */   {
/*  51 */     super(frame, modal);
/*  52 */     if (modal)
/*     */     {
/*  54 */       setModalityType("DOCUMENT_MODAL");
/*     */     }
/*  56 */     setBackground(SystemColor.control);
/*  57 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomDialog(Dialog frame, boolean modal)
/*     */   {
/*  62 */     super(frame, modal);
/*  63 */     if (modal)
/*     */     {
/*  65 */       setModalityType("DOCUMENT_MODAL");
/*     */     }
/*  67 */     setBackground(SystemColor.control);
/*  68 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomDialog(JFrame frame, String title)
/*     */   {
/*  73 */     super(frame, title, true);
/*  74 */     setModalityType("DOCUMENT_MODAL");
/*  75 */     setBackground(SystemColor.control);
/*  76 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomDialog(Dialog frame, String title)
/*     */   {
/*  81 */     super(frame, title, true);
/*  82 */     setModalityType("DOCUMENT_MODAL");
/*  83 */     setBackground(SystemColor.control);
/*  84 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomDialog(JFrame frame, String title, boolean modal)
/*     */   {
/*  89 */     super(frame, title, modal);
/*  90 */     if (modal)
/*     */     {
/*  92 */       setModalityType("DOCUMENT_MODAL");
/*     */     }
/*  94 */     setBackground(SystemColor.control);
/*  95 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomDialog(Dialog frame, String title, boolean modal)
/*     */   {
/* 100 */     super(frame, title, modal);
/* 101 */     if (modal)
/*     */     {
/* 103 */       setModalityType("DOCUMENT_MODAL");
/*     */     }
/* 105 */     setBackground(SystemColor.control);
/* 106 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public void setModalityType(String modalityType)
/*     */   {
/*     */     try
/*     */     {
/* 113 */       Class dialogClass = super.getClass();
/* 114 */       Class modalityTypeClass = Class.forName("java.awt.Dialog$ModalityType");
/* 115 */       Enum e = Enum.valueOf(modalityTypeClass, modalityType);
/* 116 */       Method m = dialogClass.getMethod("setModalityType", new Class[] { modalityTypeClass });
/* 117 */       m.invoke(this, new Object[] { e });
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 121 */       if (modalityType.equals("MODELESS"))
/*     */       {
/* 123 */         setModal(false);
/*     */       }
/*     */       else
/*     */       {
/* 127 */         setModal(true);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 134 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79248 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomDialog
 * JD-Core Version:    0.5.4
 */