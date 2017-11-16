/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.TextHandler;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.TextEvent;
/*     */ import java.awt.event.TextListener;
/*     */ import java.lang.reflect.Field;
/*     */ import javax.swing.ComboBoxEditor;
/*     */ import javax.swing.ComboBoxModel;
/*     */ import javax.swing.DefaultComboBoxModel;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.event.EventListenerList;
/*     */ 
/*     */ public class MultiComboBox extends JComboBox
/*     */   implements TextHandler
/*     */ {
/*     */   public static final String DEFAULT_MULTIPLE_SELECTION_SEPARATOR = ", ";
/*     */   public static Class m_textListenerClass;
/*     */   protected boolean m_isMulti;
/*     */   protected boolean m_isMultiNoDups;
/*     */   protected String m_multiSeparator;
/*     */   protected EventListenerList m_textListeners;
/*     */ 
/*     */   public MultiComboBox()
/*     */   {
/*  77 */     this.m_isMulti = false;
/*  78 */     this.m_multiSeparator = ", ";
/*  79 */     init();
/*     */   }
/*     */ 
/*     */   public MultiComboBox(String separator)
/*     */   {
/*  93 */     this.m_isMulti = true;
/*  94 */     this.m_multiSeparator = ((separator != null) ? separator : ", ");
/*  95 */     init();
/*     */   }
/*     */ 
/*     */   protected void init()
/*     */   {
/* 104 */     if (m_textListenerClass == null)
/*     */     {
/* 106 */       m_textListenerClass = TextListener.class;
/*     */     }
/* 108 */     this.m_textListeners = new EventListenerList();
/* 109 */     if (this.editor instanceof MultiComboBoxEditor)
/*     */       return;
/* 111 */     this.editor = new MultiComboBoxEditor(this.editor);
/*     */   }
/*     */ 
/*     */   protected void fireTextChanged()
/*     */   {
/* 120 */     Object[] listeners = this.m_textListeners.getListeners(m_textListenerClass);
/* 121 */     TextEvent ev = null;
/* 122 */     for (int i = 0; i < listeners.length; ++i)
/*     */     {
/* 124 */       if (ev == null)
/*     */       {
/* 126 */         ev = new TextEvent(this, 900);
/*     */       }
/* 128 */       ((TextListener)listeners[i]).textValueChanged(ev);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Value createValueFromObject(Object anObject)
/*     */   {
/* 140 */     if (anObject instanceof Value)
/*     */     {
/* 142 */       return (Value)anObject;
/*     */     }
/* 144 */     if (anObject instanceof String)
/*     */     {
/* 146 */       return new Value((String)anObject);
/*     */     }
/* 148 */     if (anObject instanceof String[])
/*     */     {
/* 150 */       String[] array = (String[])(String[])anObject;
/* 151 */       return new Value(array[0], array[1]);
/*     */     }
/* 153 */     if (anObject instanceof Object[])
/*     */     {
/* 155 */       Object[] array = (Object[])(Object[])anObject;
/* 156 */       return new Value(array[0].toString(), array[1].toString());
/*     */     }
/* 158 */     if (SystemUtils.m_verbose)
/*     */     {
/* 160 */       String msg = new StringBuilder().append("non-Value item added to or removed from MultiComboBox: ").append(anObject.toString()).toString();
/* 161 */       Report.trace("applet", msg, null);
/*     */     }
/* 163 */     return new Value(anObject.toString());
/*     */   }
/*     */ 
/*     */   public void addTextListener(TextListener listener)
/*     */   {
/* 174 */     this.m_textListeners.add(m_textListenerClass, listener);
/*     */   }
/*     */ 
/*     */   public void removeTextListener(TextListener listener)
/*     */   {
/* 184 */     this.m_textListeners.remove(m_textListenerClass, listener);
/*     */   }
/*     */ 
/*     */   public boolean getMultipleSelectionMode()
/*     */   {
/* 192 */     return this.m_isMulti;
/*     */   }
/*     */ 
/*     */   public void setMultipleSectionMode(boolean isMultiple)
/*     */   {
/* 205 */     this.m_isMulti = isMultiple;
/* 206 */     if (isMultiple)
/*     */       return;
/* 208 */     setText("");
/*     */   }
/*     */ 
/*     */   public boolean getMultipleSelectionRemoveDuplicates()
/*     */   {
/* 218 */     return this.m_isMultiNoDups;
/*     */   }
/*     */ 
/*     */   public void setMultipleSelectionRemoveDuplicates(boolean removeDuplicates)
/*     */   {
/* 233 */     this.m_isMultiNoDups = removeDuplicates;
/* 234 */     if (!removeDuplicates)
/*     */       return;
/* 236 */     setText(getText());
/*     */   }
/*     */ 
/*     */   public String getMultipleSelectionSeparator()
/*     */   {
/* 246 */     return this.m_multiSeparator;
/*     */   }
/*     */ 
/*     */   public void setupNewBasicModel(String[] items)
/*     */   {
/* 258 */     int numItems = items.length;
/* 259 */     Value[] values = new Value[numItems];
/* 260 */     for (int i = numItems - 1; i >= 0; --i)
/*     */     {
/* 262 */       values[i] = new Value(items[i]);
/*     */     }
/* 264 */     setModel(new DefaultComboBoxModel(values));
/*     */   }
/*     */ 
/*     */   public void setupNewModelWithHiddens(String[][] items)
/*     */   {
/* 275 */     String[] joiners = getJoinerStrings();
/* 276 */     int numItems = items.length;
/* 277 */     Value[] values = new Value[numItems];
/* 278 */     for (int i = numItems - 1; i >= 0; --i)
/*     */     {
/* 280 */       String internalValue = items[i][0]; String displayValue = items[i][1];
/* 281 */       values[i] = new Value(internalValue, displayValue);
/* 282 */       boolean isInternalEqualToDisplayValue = internalValue.equals(displayValue);
/* 283 */       String joiner = joiners[1];
/* 284 */       String joined = createJoinedString(items[i][0], items[i][1], joiner);
/* 285 */       values[i].m_string = joined;
/*     */     }
/* 287 */     setModel(new DefaultComboBoxModel(values));
/*     */   }
/*     */ 
/*     */   public void selectItem(Value item)
/*     */   {
/* 301 */     if (item == null)
/*     */     {
/* 303 */       this.dataModel.setSelectedItem(null);
/* 304 */       return;
/*     */     }
/*     */     String text;
/*     */     String text;
/* 307 */     if (!this.m_isMulti)
/*     */     {
/* 309 */       text = item.m_internalValue;
/*     */     }
/*     */     else
/*     */     {
/* 313 */       StringBuilder str = new StringBuilder(getText());
/* 314 */       if (str.length() > 0)
/*     */       {
/* 316 */         str.append(this.m_multiSeparator);
/*     */       }
/* 318 */       str.append(item.m_internalValue);
/* 319 */       text = str.toString();
/*     */     }
/* 321 */     setText(text);
/*     */   }
/*     */ 
/*     */   public void addItem(Object anObject)
/*     */   {
/* 329 */     Value value = createValueFromObject(anObject);
/* 330 */     super.addItem(value);
/*     */   }
/*     */ 
/*     */   public void insertItemAt(Object anObject, int index)
/*     */   {
/* 337 */     Value value = createValueFromObject(anObject);
/* 338 */     super.insertItemAt(value, index);
/*     */   }
/*     */ 
/*     */   public void removeItem(Object anObject)
/*     */   {
/* 345 */     Value value = createValueFromObject(anObject);
/* 346 */     String internalValue = value.m_internalValue;
/* 347 */     int numItems = this.dataModel.getSize();
/* 348 */     for (int i = numItems - 1; i >= 0; --i)
/*     */     {
/* 350 */       Object item = this.dataModel.getElementAt(i);
/* 351 */       if (!item instanceof Value)
/*     */         continue;
/* 353 */       value = (Value)item;
/* 354 */       if (!internalValue.equals(value.m_internalValue))
/*     */         continue;
/* 356 */       removeItemAt(i);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void firePopupMenuCanceled()
/*     */   {
/* 369 */     this.dataModel.setSelectedItem(null);
/* 370 */     super.firePopupMenuCanceled();
/*     */   }
/*     */ 
/*     */   public void setPopupVisible(boolean visible)
/*     */   {
/* 383 */     super.setPopupVisible(visible);
/* 384 */     if (visible)
/*     */       return;
/* 386 */     Object item = this.dataModel.getSelectedItem();
/* 387 */     if (!item instanceof Value)
/*     */       return;
/* 389 */     Value value = (Value)item;
/* 390 */     selectItem(value);
/*     */   }
/*     */ 
/*     */   public String getText()
/*     */   {
/* 402 */     Object value = this.editor.getItem();
/* 403 */     if (value instanceof String)
/*     */     {
/* 405 */       return (String)value;
/*     */     }
/* 407 */     if (null == value)
/*     */     {
/* 409 */       return "";
/*     */     }
/* 411 */     return value.toString();
/*     */   }
/*     */ 
/*     */   public void setText(String value)
/*     */   {
/* 421 */     if ((this.m_isMultiNoDups) && (this.m_isMulti))
/*     */     {
/* 423 */       String[] values = value.split(this.m_multiSeparator);
/* 424 */       StringBuilder str = new StringBuilder();
/* 425 */       for (int i = 0; i < values.length; ++i)
/*     */       {
/* 427 */         for (int j = 0; j < i; ++j)
/*     */         {
/* 429 */           if (values[i].equals(values[j])) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 434 */         if (j < i) {
/*     */           continue;
/*     */         }
/*     */ 
/* 438 */         if (i > 0)
/*     */         {
/* 440 */           str.append(this.m_multiSeparator);
/*     */         }
/* 442 */         str.append(values[i]);
/*     */       }
/* 444 */       value = str.toString();
/*     */     }
/*     */ 
/* 447 */     this.dataModel.setSelectedItem(null);
/* 448 */     if (this.editor instanceof MultiComboBoxEditor)
/*     */     {
/* 450 */       MultiComboBoxEditor multiEditor = (MultiComboBoxEditor)this.editor;
/* 451 */       multiEditor.setText(value);
/*     */     }
/*     */     else
/*     */     {
/* 455 */       this.editor.setItem(value);
/*     */     }
/* 457 */     fireTextChanged();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getJoinerString()
/*     */   {
/* 598 */     String[] strings = getJoinerStrings();
/* 599 */     return strings[1];
/*     */   }
/*     */ 
/*     */   protected static String[] getJoinerStrings()
/*     */   {
/* 613 */     String[] fieldNames = { "m_internalIsDisplayValueJoiner", "m_internalPlusDisplayValueJoiner" };
/* 614 */     String[] joiners = { "%1", "%1 (%0)" };
/*     */     try
/*     */     {
/* 617 */       Class cl = Class.forName("intradoc.gui.GuiText");
/* 618 */       for (int i = fieldNames.length - 1; i >= 0; --i)
/*     */       {
/* 620 */         String fieldName = fieldNames[i];
/* 621 */         Field f = cl.getField(fieldName);
/* 622 */         String str = (String)f.get(null);
/*     */ 
/* 624 */         if ((str.indexOf("%0") < 0) && (str.indexOf("%1") < 0))
/*     */           continue;
/* 626 */         joiners[i] = str;
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */     }
/*     */ 
/* 634 */     return joiners;
/*     */   }
/*     */ 
/*     */   public static String createJoinedString(String internalValue, String externalValue, String joiner)
/*     */   {
/* 648 */     int length = joiner.length();
/* 649 */     StringBuilder str = new StringBuilder();
/* 650 */     int index = 0;
/*     */     while (true) {
/* 652 */       int nextIndex = joiner.indexOf(37, index);
/* 653 */       if (nextIndex < 0)
/*     */       {
/* 655 */         str.append(joiner.substring(index));
/* 656 */         break;
/*     */       }
/* 658 */       if (nextIndex > index)
/*     */       {
/* 660 */         str.append(joiner.substring(index, nextIndex));
/*     */       }
/* 662 */       if (nextIndex + 1 >= length)
/*     */       {
/* 664 */         str.append('%');
/* 665 */         break;
/*     */       }
/* 667 */       char ch = joiner.charAt(nextIndex + 1);
/* 668 */       if (ch == '0')
/*     */       {
/* 670 */         str.append(internalValue);
/*     */       }
/* 672 */       else if (ch == '1')
/*     */       {
/* 674 */         str.append(externalValue);
/*     */       }
/*     */       else
/*     */       {
/* 678 */         str.append('%');
/* 679 */         str.append(ch);
/*     */       }
/* 681 */       index = nextIndex + 2;
/*     */     }
/* 683 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 689 */     return "releaseInfo=dev,releaseRevision=$Rev: 96523 $";
/*     */   }
/*     */ 
/*     */   public class MultiComboBoxEditor
/*     */     implements ComboBoxEditor, TextHandler
/*     */   {
/*     */     public ComboBoxEditor m_realEditor;
/*     */ 
/*     */     public MultiComboBoxEditor(ComboBoxEditor wrapEditor)
/*     */     {
/* 513 */       this.m_realEditor = wrapEditor;
/*     */     }
/*     */ 
/*     */     public Component getEditorComponent()
/*     */     {
/* 519 */       return this.m_realEditor.getEditorComponent();
/*     */     }
/*     */ 
/*     */     public void setItem(Object itemObject)
/*     */     {
/* 536 */       if (MultiComboBox.this.isEditable())
/*     */       {
/* 538 */         return;
/*     */       }
/* 540 */       setText(itemObject.toString());
/*     */     }
/*     */ 
/*     */     public Object getItem()
/*     */     {
/* 548 */       return getText();
/*     */     }
/*     */ 
/*     */     public void selectAll()
/*     */     {
/* 553 */       this.m_realEditor.selectAll();
/*     */     }
/*     */ 
/*     */     public void addActionListener(ActionListener l)
/*     */     {
/* 558 */       this.m_realEditor.addActionListener(l);
/*     */     }
/*     */ 
/*     */     public void removeActionListener(ActionListener l)
/*     */     {
/* 563 */       this.m_realEditor.removeActionListener(l);
/*     */     }
/*     */ 
/*     */     public String getText()
/*     */     {
/* 573 */       return this.m_realEditor.getItem().toString();
/*     */     }
/*     */ 
/*     */     public void setText(String text)
/*     */     {
/* 583 */       this.m_realEditor.setItem(text);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class Value
/*     */   {
/*     */     public final String m_internalValue;
/*     */     public final String m_externalValue;
/*     */     public String m_string;
/*     */ 
/*     */     public Value(String value)
/*     */     {
/* 475 */       this(value, null);
/* 476 */       this.m_string = value;
/*     */     }
/*     */ 
/*     */     public Value(String internalValue, String externalValue) {
/* 480 */       this.m_internalValue = internalValue;
/* 481 */       this.m_externalValue = externalValue;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 487 */       if (this.m_string != null)
/*     */       {
/* 489 */         return this.m_string;
/*     */       }
/* 491 */       if (this.m_externalValue == null)
/*     */       {
/* 493 */         return this.m_internalValue;
/*     */       }
/* 495 */       return this.m_internalValue + " - " + this.m_externalValue;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.MultiComboBox
 * JD-Core Version:    0.5.4
 */