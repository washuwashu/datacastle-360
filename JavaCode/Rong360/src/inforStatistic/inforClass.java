package inforStatistic;
//用户的基本属性 userInfo : 用户id,性别,职业,教育程度,婚姻状态,户口类型
class userInfo
{
	
	public String userID;
	public String sex;
	public String job;
	public String education;
	public String marriage;
	public String family;
	public  userInfo(String _id,String _sex,String _job,String _edu,String _marri,String _fam)
	{
		userID = _id;
		sex = _sex;
		job = _job;
		education = _edu;
		marriage = _marri;
		family = _fam;
	}
	public  String toString()
	{
		String  feture = userID+","+sex+","+job+","+education+","+marriage+","+family+",";
		return  feture;
	}	
	public static String combineFeatureTitle()
	{
		String  feture = "userID"+","+"sex"+","+"job"+","+"education"+","+"marriage"+","+ "family"+",";
		return  feture;
	}
}

//银行流水记录  bankDetail :  用户id,时间戳,交易类型,交易金额,工资收入标记
class bankDetail implements Comparable<bankDetail>
{
	 public String userID;
	 public long time;
	 public int commerceType;
	 public float commerceSum;
	 public int salaryLabel;
	 public bankDetail(String id,long ti,int comType,float comSum,int slabel)
	 {
		 userID = id;
		 time = ti;
		// time = ti == 0 ? ti : (ti - 5800000000L)/86400;		 
		 commerceType = comType;
		 commerceSum = comSum;
		 salaryLabel = slabel;
	 }
	 public int compareTo(bankDetail bdt) 
	 {
	      return (int) (this.time-bdt.time);
	 }
	 
}

//用户浏览行为  browseHistory : 用户id,时间戳,浏览行为数据,浏览子行为编号 
class browseHistory   implements Comparable<browseHistory>
{  
	public String userID;
	public long time;
	public int browseMain;
	public int browseChild;
	public browseHistory(String id,long ti,int bMain,int bChild)
	{
		userID = id;
		time = ti;
		browseMain = bMain;
		browseChild = bChild;
	}
	public int compareTo(browseHistory bdt) 
	 {
	      return (int) (this.time-bdt.time);
	 }
	public String toString()
	{
		String result = userID+","+time+","+browseMain+","+browseChild;
		return result;
	}
}

//信用卡账单记录bill_detail ：
//      用户id,账单时间戳,银行id,上期账单金额,上期还款金额,信用卡额度,本期账单余额,本期账单最低还款额,消费笔数,本期账单金额,调整金额,循环利息,可用金额,预借现金额度,还款状态
class billDetail implements Comparable<billDetail>
{
	public String   userID;    				     // 用户id
	public long 	time;    					 //账单时间戳
	public int 		bankID;						 //银行id
	public float 	lastBill;                    //上期账单金额
	public float 	lastReturn;                  //上期还款金额
	public float 	credit;                     //信用卡额度
	public float 	thisBillRest;               //本期账单余额
	public float 	curLeastReturn;             //本期账单最低还款额
	public int 		consume;                    //消费笔数
	public float 	thisBill;                   //本期账单金额
	public float 	adjustMoney;                //调整金额
	public float 	cycleInterest;              //循环利息
	public float 	useableMoney;               //可用金额
	public float 	borrowMoney;				 //预借现金额度
	public int 		returnState;				//还款状态
	
	public billDetail(String id,long ti,int bid,float preBill,float prereturn,float cre,float balance,float lreturn,
								int conTimes,float curBill,float adjMoney,float cycleInt,float useMoney,float borrow,int state)
	{
		userID = id;
	    
	    if(ti == 0)
	    	time = ti;
	    else
	    	time = (ti - 5800000000L)/86400; 
	    	//time = ti - 5800000000L;
		
	    bankID = bid;
		lastBill = preBill;
		lastReturn = prereturn;
		credit = cre;
		
		thisBillRest = balance;
		curLeastReturn = lreturn;
		consume = conTimes;
		thisBill = curBill;
		adjustMoney = adjMoney;
		cycleInterest = cycleInt;
		useableMoney = useMoney;
		borrowMoney = borrow;
		returnState = state;
	}
	
	public String toString()
	{
		String billString = userID +"	"+time+"	"+bankID+"	"+lastBill+"	"+lastReturn+"	"+credit+"	"+thisBillRest+"	"+
				curLeastReturn+"	"+consume+"	"+thisBill+"	"+adjustMoney+"	"+cycleInterest+"	"+useableMoney+"	"+
				borrowMoney+"	"+returnState;
		return billString;
	}
	public int compareTo(billDetail bdt) 
	 {
	      return (int) (this.time-bdt.time);
	 }
}
//放款时间信息loan_time :  用户id,放款时间
class  loanTime
{ 
	public String userID;
	public long time;
	public loanTime(String id,long ti)
	{
		userID = id;
		time = ti;
	}	
}

//逾期行为的记录类 overdue : 用户id,样本标签
class overdue
{
	public String userID;
	public int label;
	public overdue(String id,int lab)
	{
		userID = id;
		label = lab;
	}
}



public class inforClass 
{

}
