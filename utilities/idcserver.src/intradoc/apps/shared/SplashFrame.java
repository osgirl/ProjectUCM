/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.GuiStyles;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.SystemColor;
/*     */ import java.awt.Toolkit;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.Box;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JProgressBar;
/*     */ 
/*     */ public class SplashFrame extends JFrame
/*     */   implements ReportProgress
/*     */ {
/*     */   protected JProgressBar m_progressBar;
/*     */   protected WindowHelper m_windowHelper;
/*     */   protected CustomLabel m_progressText;
/*     */ 
/*     */   public SplashFrame()
/*     */   {
/*  49 */     setTitle("Content Server");
/*     */ 
/*  51 */     this.m_windowHelper = new WindowHelper();
/*  52 */     this.m_windowHelper.m_exitOnClose = false;
/*  53 */     this.m_windowHelper.attachToWindow(this, null, null);
/*     */ 
/*  56 */     Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
/*  57 */     setLocation((d.width - 300) / 2, (d.height - 100) / 2);
/*  58 */     setResizable(false);
/*     */ 
/*  61 */     setBackground(SystemColor.control);
/*  62 */     setForeground(SystemColor.controlText);
/*     */ 
/*  65 */     JPanel p = new PanePanel();
/*  66 */     p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
/*  67 */     setContentPane(p);
/*  68 */     Box box = Box.createVerticalBox();
/*  69 */     p.add(box);
/*     */ 
/*  72 */     p = new PanePanel();
/*  73 */     this.m_progressText = new CustomLabel(" ", 0);
/*  74 */     GuiStyles.setCustomStyle(this.m_progressText, 2);
/*  75 */     p.add(this.m_progressText);
/*  76 */     box.add(p);
/*     */ 
/*  79 */     p = new PanePanel();
/*  80 */     this.m_progressBar = new JProgressBar(0, 100);
/*  81 */     this.m_progressBar.setPreferredSize(new Dimension(300, 20));
/*  82 */     p.add(this.m_progressBar);
/*  83 */     box.add(p);
/*     */ 
/*  85 */     pack();
/*     */   }
/*     */ 
/*     */   public void setCloseOnExit(boolean close)
/*     */   {
/*  90 */     this.m_windowHelper.m_exitOnClose = close;
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/*  95 */     this.m_progressText.setText(msg);
/*  96 */     this.m_progressBar.setValue((int)amtDone);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 101 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.SplashFrame
 * JD-Core Version:    0.5.4
 */