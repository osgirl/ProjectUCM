/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.EventQueue;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import javax.swing.Box;
/*     */ import javax.swing.BoxLayout;
/*     */ import javax.swing.JComponent;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class SwingPromptUser
/*     */   implements PromptUser, Runnable, ActionListener
/*     */ {
/*     */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   public boolean m_isQuiet;
/*     */   public int m_lineLength;
/*     */   public int m_textHeight;
/*     */   public JFrame m_frame;
/*     */   public JComponent m_promptBox;
/*     */   public CustomLabel m_inputLabel;
/*     */   public JTextField m_inputBox;
/*     */   public CustomLabel m_statusLabel;
/*     */ 
/*     */   public SwingPromptUser()
/*     */   {
/*  51 */     this.m_lineLength = 80;
/*  52 */     this.m_textHeight = 80;
/*     */   }
/*     */ 
/*     */   public String prompt(int type, String label, String defValue, Object data, String explaination)
/*     */   {
/*  63 */     IdcMessage labelMsg = LocaleUtils.parseMessage(label);
/*  64 */     IdcMessage explainationMsg = null;
/*  65 */     if (explaination != null)
/*     */     {
/*  67 */       explainationMsg = LocaleUtils.parseMessage(explaination);
/*     */     }
/*  69 */     return prompt(type, labelMsg, defValue, data, explainationMsg);
/*     */   }
/*     */ 
/*     */   public String prompt(int type, IdcMessage label, String defValue, Object data, IdcMessage explaination)
/*     */   {
/*  75 */     checkInit();
/*  76 */     this.m_inputLabel.setText(LocaleResources.localizeMessage(null, label, null).toString());
/*  77 */     if (defValue == null)
/*     */     {
/*  79 */       defValue = "";
/*     */     }
/*  81 */     this.m_inputBox.setText(defValue);
/*  82 */     synchronized (this)
/*     */     {
/*     */       try
/*     */       {
/*  86 */         super.wait();
/*     */       }
/*     */       catch (InterruptedException ignore)
/*     */       {
/*  90 */         Report.debug(null, "Interrupted while waiting on swing", ignore);
/*     */       }
/*     */     }
/*  93 */     return this.m_inputBox.getText();
/*     */   }
/*     */ 
/*     */   public String trimStringMid(String msg)
/*     */   {
/* 102 */     return msg;
/*     */   }
/*     */ 
/*     */   public void setLineLength(int width)
/*     */   {
/* 112 */     this.m_lineLength = width;
/*     */   }
/*     */ 
/*     */   public int getLineLength()
/*     */   {
/* 117 */     return this.m_lineLength;
/*     */   }
/*     */ 
/*     */   public void setScreenHeight(int height)
/*     */   {
/* 128 */     this.m_textHeight = height;
/*     */   }
/*     */ 
/*     */   public int getScreenHeight()
/*     */   {
/* 133 */     return this.m_textHeight;
/*     */   }
/*     */ 
/*     */   public boolean getQuiet()
/*     */   {
/* 140 */     return this.m_isQuiet;
/*     */   }
/*     */ 
/*     */   public boolean setQuiet(boolean newValue)
/*     */   {
/* 148 */     boolean oldValue = this.m_isQuiet;
/* 149 */     this.m_isQuiet = newValue;
/* 150 */     return oldValue;
/*     */   }
/*     */ 
/*     */   public void outputMessage(String text)
/*     */   {
/* 160 */     checkInit();
/*     */ 
/* 162 */     this.m_statusLabel.setText(text);
/*     */   }
/*     */ 
/*     */   public void updateMessage(String text)
/*     */   {
/* 172 */     checkInit();
/*     */ 
/* 174 */     this.m_statusLabel.setText(text);
/*     */   }
/*     */ 
/*     */   public void finalizeOutput()
/*     */   {
/* 182 */     checkInit();
/*     */   }
/*     */ 
/*     */   public void checkInit()
/*     */   {
/* 188 */     if (this.m_frame != null)
/*     */     {
/* 190 */       return;
/*     */     }
/* 192 */     this.m_frame = new JFrame();
/* 193 */     this.m_frame.setDefaultCloseOperation(3);
/* 194 */     this.m_frame.setTitle("AJK");
/* 195 */     EventQueue.invokeLater(this);
/*     */ 
/* 198 */     this.m_frame.setLocation(0, 0);
/* 199 */     BoxLayout layout = new BoxLayout(this.m_frame.getContentPane(), 1);
/* 200 */     this.m_frame.getContentPane().setLayout(layout);
/*     */ 
/* 202 */     this.m_frame.add(this.m_promptBox = new Box(0));
/* 203 */     this.m_promptBox.add(this.m_inputLabel = new CustomLabel());
/* 204 */     this.m_promptBox.add(this.m_inputBox = new JTextField());
/* 205 */     this.m_inputBox.setPreferredSize(new Dimension(this.m_lineLength * 15, 15));
/* 206 */     this.m_inputBox.addActionListener(this);
/*     */ 
/* 210 */     this.m_frame.add(this.m_statusLabel = new CustomLabel());
/*     */ 
/* 213 */     this.m_frame.pack();
/*     */   }
/*     */ 
/*     */   public synchronized void actionPerformed(ActionEvent event)
/*     */   {
/* 218 */     super.notify();
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 223 */     this.m_frame.setVisible(true);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 228 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.SwingPromptUser
 * JD-Core Version:    0.5.4
 */