/*    */ package intradoc.provider;
/*    */ 
/*    */ import intradoc.common.NumberUtils;
/*    */ import intradoc.common.StringUtils;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class LdapGroupPrefix
/*    */ {
/*    */   public int m_allowedDepth;
/*    */   public boolean m_useFullGroupName;
/*    */   public String m_value;
/*    */   public String m_type;
/*    */ 
/*    */   public LdapGroupPrefix()
/*    */   {
/* 35 */     this.m_allowedDepth = 0;
/* 36 */     this.m_useFullGroupName = true;
/* 37 */     this.m_value = "";
/* 38 */     this.m_type = "role";
/*    */   }
/*    */ 
/*    */   public LdapGroupPrefix(String group, String type)
/*    */   {
/* 43 */     this.m_allowedDepth = 0;
/* 44 */     this.m_useFullGroupName = true;
/* 45 */     this.m_value = group;
/* 46 */     this.m_type = type;
/*    */ 
/* 48 */     Vector array = StringUtils.parseArrayEx(group, ',', '^', true);
/* 49 */     this.m_value = StringUtils.createStringRemoveEmpty(array, ',', '^');
/*    */ 
/* 51 */     int index = group.indexOf("[");
/* 52 */     if (index >= 0)
/*    */     {
/* 54 */       String params = this.m_value.substring(index + 1);
/* 55 */       this.m_value = this.m_value.substring(0, index);
/* 56 */       index = params.indexOf("]");
/* 57 */       if (index >= 0)
/*    */       {
/* 59 */         StringBuffer depth = new StringBuffer();
/* 60 */         params = params.substring(0, index);
/* 61 */         char[] ch = new char[params.length()];
/* 62 */         params.getChars(0, params.length(), ch, 0);
/*    */ 
/* 64 */         for (int i = 0; i < ch.length; ++i)
/*    */         {
/* 66 */           if (ch[i] == '*')
/*    */           {
/* 68 */             this.m_useFullGroupName = false;
/*    */           } else {
/* 70 */             if (!Character.isDigit(ch[i]))
/*    */               continue;
/* 72 */             depth.append(ch[i]);
/*    */           }
/*    */         }
/* 75 */         this.m_allowedDepth = NumberUtils.parseInteger(depth.toString(), 0);
/*    */       }
/*    */     }
/* 78 */     if (this.m_allowedDepth >= 0)
/*    */       return;
/* 80 */     this.m_allowedDepth = 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 86 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.LdapGroupPrefix
 * JD-Core Version:    0.5.4
 */