package hays.co.uk.search;

public class QueryBuilderFactory {
	private static QueryBuilderFactory queryFactory = null;
	private QueryBuilderFactory(){
		
	}
	public static QueryBuilderFactory getInstance() {
		// TODO Auto-generated method stub
		if(queryFactory==null)
			queryFactory = new QueryBuilderFactory();
		return queryFactory;
	}
	public AbstractHaysQueryBuilder getQueryBuilder(boolean isFuzzy,boolean isOnlyJobTitle,String thes_name,String alertprofileid,
			String level,String pIsAdvanceSearch,boolean pRelaxFullTextCountry,String pRadius){
		if(isFuzzy){
			return new FuzzyHaysQueryBuilder(isFuzzy,isOnlyJobTitle,thes_name,alertprofileid,level,pIsAdvanceSearch,pRelaxFullTextCountry,pRadius);
		}
		else if(isOnlyJobTitle)
		{
			return new IsOnlyJobTitleQueryBuilder(isFuzzy,isOnlyJobTitle,thes_name,alertprofileid);
		}
		return new SimpleHaysQueryBuilder(isFuzzy,isOnlyJobTitle,thes_name,alertprofileid);
	}
}
