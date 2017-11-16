/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcLinguisticComparatorAdapter;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserDocumentAccessFilter extends IdcLinguisticComparatorAdapter
/*     */ {
/*     */   public boolean m_isAdmin;
/*     */   public int m_appRights;
/*     */   public String[] m_groups;
/*     */   public String[] m_accounts;
/*     */   public boolean m_accessNoneAccount;
/*     */   public boolean m_accessAllAccounts;
/*     */ 
/*     */   public UserDocumentAccessFilter(Vector groups, Vector accounts, boolean isAdmin, int appRights)
/*     */   {
/*  78 */     this.m_isAdmin = isAdmin;
/*  79 */     this.m_appRights = appRights;
/*  80 */     this.m_groups = null;
/*  81 */     this.m_accounts = null;
/*  82 */     this.m_accessNoneAccount = false;
/*  83 */     this.m_accessAllAccounts = false;
/*     */ 
/*  87 */     super.init(IdcLinguisticComparatorAdapter.m_defaultRule);
/*     */ 
/*  89 */     if (groups != null)
/*     */     {
/*  91 */       this.m_groups = StringUtils.convertListToArray(groups);
/*  92 */       for (int i = 0; i < this.m_groups.length; ++i)
/*     */       {
/*  94 */         this.m_groups[i] = this.m_groups[i].toLowerCase();
/*     */       }
/*  96 */       Sort.sort(this.m_groups, 0, this.m_groups.length - 1, this);
/*     */     }
/*     */ 
/*  99 */     int naccounts = 0;
/*     */ 
/* 101 */     if (accounts != null)
/*     */     {
/* 103 */       naccounts = accounts.size();
/* 104 */       for (int i = 0; i < naccounts; ++i)
/*     */       {
/* 106 */         String acct = (String)accounts.elementAt(i);
/* 107 */         if (acct.equals("#all"))
/*     */         {
/* 109 */           this.m_accessAllAccounts = true;
/* 110 */           naccounts = 0;
/* 111 */           break;
/*     */         }
/* 113 */         if (acct.equals("#none"))
/*     */         {
/* 115 */           this.m_accessNoneAccount = true;
/* 116 */           accounts.removeElementAt(i);
/* 117 */           --naccounts;
/* 118 */           --i;
/*     */         }
/*     */         else
/*     */         {
/* 122 */           accounts.setElementAt(acct.toLowerCase(), i);
/*     */         }
/*     */       }
/*     */     }
/* 126 */     if (naccounts > 0)
/*     */     {
/* 128 */       this.m_accounts = StringUtils.convertListToArray(accounts);
/* 129 */       Sort.sort(this.m_accounts, 0, this.m_accounts.length - 1, this);
/*     */     } else {
/* 131 */       if (accounts == null)
/*     */         return;
/* 133 */       this.m_accounts = new String[0];
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isEqualSecurity(UserDocumentAccessFilter filter)
/*     */   {
/* 142 */     if ((filter.m_isAdmin == true) && (this.m_isAdmin == true))
/*     */     {
/* 144 */       return true;
/*     */     }
/* 146 */     if (filter.m_appRights != this.m_appRights)
/*     */     {
/* 148 */       return false;
/*     */     }
/* 150 */     if (filter.m_accessAllAccounts != this.m_accessAllAccounts)
/*     */     {
/* 152 */       return false;
/*     */     }
/* 154 */     if (filter.m_accessNoneAccount != this.m_accessNoneAccount)
/*     */     {
/* 156 */       return false;
/*     */     }
/* 158 */     if (!checkStringListEqual(this.m_groups, filter.m_groups))
/*     */     {
/* 160 */       return false;
/*     */     }
/*     */ 
/* 164 */     return checkStringListEqual(this.m_accounts, filter.m_accounts);
/*     */   }
/*     */ 
/*     */   public boolean checkStringListEqual(String[] list1, String[] list2)
/*     */   {
/* 174 */     if ((list1 == null) || (list2 == null))
/*     */     {
/* 176 */       return list1 == list2;
/*     */     }
/*     */ 
/* 179 */     if (list1.length != list2.length)
/*     */     {
/* 181 */       return false;
/*     */     }
/* 183 */     for (int i = 0; i < list1.length; ++i)
/*     */     {
/* 185 */       if (!list1[i].equals(list2[i]))
/*     */       {
/* 187 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 191 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean checkAccess(String group, String account)
/*     */   {
/* 200 */     int i = 0;
/*     */ 
/* 202 */     boolean hasAccess = checkAccess(group);
/* 203 */     if (!hasAccess)
/*     */     {
/* 205 */       return false;
/*     */     }
/*     */ 
/* 208 */     if (this.m_accessAllAccounts)
/*     */     {
/* 210 */       return true;
/*     */     }
/* 212 */     if ((account == null) || (account.length() == 0))
/*     */     {
/* 214 */       return this.m_accessNoneAccount;
/*     */     }
/* 216 */     if (this.m_accounts == null)
/*     */     {
/* 218 */       return false;
/*     */     }
/* 220 */     for (i = 0; i < this.m_accounts.length; ++i)
/*     */     {
/* 222 */       if (account.startsWith(this.m_accounts[i]))
/*     */       {
/* 224 */         return true;
/*     */       }
/*     */     }
/* 227 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean checkAccess(String group)
/*     */   {
/* 236 */     boolean hasAccess = false;
/* 237 */     if (this.m_groups == null)
/*     */     {
/* 239 */       if ((group == null) || (group.length() == 0))
/*     */       {
/* 241 */         return true;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 246 */       if (group == null)
/*     */       {
/* 248 */         return false;
/*     */       }
/* 250 */       for (int i = 0; i < this.m_groups.length; ++i)
/*     */       {
/* 252 */         if (!this.m_groups[i].equals(group))
/*     */           continue;
/* 254 */         hasAccess = true;
/* 255 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 259 */     return hasAccess;
/*     */   }
/*     */ 
/*     */   public int compare(Object o1, Object o2)
/*     */   {
/* 272 */     int comparison = 0;
/*     */ 
/* 274 */     String s1 = (String)o1;
/* 275 */     String s2 = (String)o2;
/*     */ 
/* 277 */     comparison = super.compare(s1, s2);
/*     */ 
/* 279 */     return comparison;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 284 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98407 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.UserDocumentAccessFilter
 * JD-Core Version:    0.5.4
 */