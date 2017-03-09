package inforStatistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

class billFeature                                            // 共计   维特征
{   
	// 基本信息
	public int    bankCount = 0;                            // 银行数    1	
	public int    billCount  = 0;                           // 账单记录数 1			
	
	// 欠款模型变量
	public float  totalOwing = 0f;                           
	public float  totalBacking = 0f; 
	public float  totalBackSubOwe = 0f;
	public int    totalBackSubOweFlag = 0;
	
	public float  perBankOwing = 0f;                         // 平均每个银行下欠款
	public float  perTermOwing = 0f;                         // 平均每期欠款数
	public float  owingBankMax = 0f;                         // 欠款最多的银行金额
	public float  owingPerMax = 0f;                          // 欠款最多的银行下平均每期拖欠多少钱
		
	public int    withoutOweCount = 0;                       // 欠款单为0的期数
	public float  withoutOweRatio = 0f;                      // 欠款单为0的期数占比
	
	public int    owingBanks = 0;                            // 尚未还清欠款的银行数
	public float  owingBanksRatio = 0;                       // 尚未还清的银行比例
	
	public int    balanceNegativeCount = 0;                  // 本期账单余额为零的期数和比例
	public float  balanceNegativeRatio = 0f;
		
	// 目前主要在这个维度上做文章！！！	
	 /*
	  * 还款意愿 ： 还款多过欠款(还款能力强)：期数、期数占比、金额总数、金额占比			     
			         轻度拖欠：期数、金额
	                                 重度拖欠：期数、期数占比、金额总数、金额占欠款总数的比例
	                                拖欠笔数：  轻度拖欠笔数 + 重度拖笔数                 // 共性向量			     			   		    			    
	  */
	
	public  float [] moreBacking;
	public  float [] lightOwing;
	public  float [] heavyOwing ;
	public  int  delayCount = 0;
				
	// 逾期模型变量：当前逾期金额，当前逾期总金额，当前逾期总期数   
	
	public int    curOverdueCount = 0;                       // 当前逾期总期数 
	public float  curOverdueCountRatio = 0f;                 // 当前逾期期数占总期数比例
	
	public float  curOverdueMoney = 0f;
	public float  curOverdueMoneySum = 0f;
		
	public int    returnLeastCount = 0;	
	public float  returnLeastRatio = 0f;                     // 总的正常还款比例
			
	// 资产情况： 信用额度的均值、信用额度的下调比例(次数)、当前信用额度、当前信用额度是否提升   
				
	public int   creditDescCount = 0;                       // 信用下调次数
	public float creditDescRatio = 0f;                      // 信用额度下调比例             
		
	public int   creditToZero = 0;                          // 信用额度突变为零的次数
	
	//消费情况: 主要考察用户在逾期期间消费分情况，消费能力 和 偿还能力的均衡 
	public float consumeSumMoney = 0f;                      // 总消费金额(根据本期账单金额计算)
	public int   consumeSumCount = 0;                       // 消费总笔数	
	public int   consumeAverage = 0;                        // 平均每个银行向消费笔数
	public float consumePerCost = 0f;                       // 平均每笔消费的花费水平
		
	// 循环利息的情况
	public float  cycleRateSum = 0f;                        // 循环利息的总数
	public int    cycleRateCount = 0;                       // 循环利息大于0的期数(说明为按时还款的辅助参考)
	
	//ATM取现情况: 主要考察用户取现的信用     2		
	public int    preBorrowZeroCount = 0;                   // 预借现金额度为0的次数
				
	//信用透支情况: 信用透支比例   2	
	public int     useableZeroCount = 0;                    // 可用余额为0的总数
	public double  useableZeroCountRatio = 0f;              // 可用余额为0的期数比例
	
	//账户年限
	public long creditYearsAverage = 0L;                   // 信用卡平均年限 和 最长年限,最短年限      
	public long creditYearsLongest = 0L;
	public long creditYearsShortest = 6*1000000000L; 
	
	// 放款前期数、放款后笔数、放款前已知期数
	public long beforeLoan = 0L;
	public long afterLoan = 0L;
	public long beforeLoanKonwn = 0L;
	
	public float  [] beforeLoanBill;                        // 贷款前/后:欠款总额、欠额、还款比例、重度拖欠的笔数、金额
	public float  [] afterLoanBill;                         
	public long   loanUseTime = 0L;                         // 贷款使用时间
	public float  perOwingAfterLoaning = 0L;                // 贷款后平均欠款  = 贷款后欠款数 / 贷款使用时间
	public int    withoutOwingBeforeLoan = 0;               // 贷款前是否已经还清银行
	public float  backRatio = 0f;                           // 整体还款比例 
	public int    banksMoreThanFour = 0;                    // 银行数目大于4?
	public ArrayList<Long> billTimeList  = new ArrayList<>(); // 用于计算最后一期时间
	
	
	public int [] bankDistribution ;                        // 银行分布情况 
	
	public static int length = 51 + 15 + 15;                     // 信用卡账单维数
	
	public static int moreLength = 4;                       // 还款意愿的维数
	public static int lightLength = 2;
	public static int heavyLength = 4;
	
	public  static int  beforeLength = 5;                   // 贷款前后的维数
	public  static int  afterLength = 5;
	
	public  static int  bankLength = 15;
				
	public billFeature()
	{		
		moreBacking = new float[moreLength];
		for(int i=0;i<moreBacking.length;i++)
			moreBacking[i] = 0f;
		
		lightOwing = new float[lightLength];
		for(int i=0;i<lightOwing.length;i++)
			lightOwing[i] = 0f;
		
		heavyOwing = new float[heavyLength];
		for(int i=0;i<heavyOwing.length;i++)
			heavyOwing[i] = 0f;	
		
		beforeLoanBill =  new float[beforeLength];
		for(int i=0;i<beforeLength;i++)
			beforeLoanBill[i] = 0f;
		
		afterLoanBill =  new float[afterLength];
		for(int i=0;i<afterLength;i++)
			afterLoanBill[i] = 0f;
		
		bankDistribution = new int [bankLength];
		for(int i =0;i<bankLength;i++)
			bankDistribution[i] = 0;
	}
	public  String fetureTitle()
	{
		String title = new String();
		title += "UserID" +","+ new billFeature().combineFetureTitle();						
		return title;
	}
	public  String combineFetureTitle()
	{			
		String title = new String();
		
		// 账户账单基本信息 ：2
		title +="bankCount,billCount,";
		
		// 欠款模型：  12
		title +=  "totalOwing,totalBacking,totalBackSubOwe,totalBackSubOweFlag,"
				+ "perBankOwing,perTermOwing,owingBankMax,owingPerMax,"		
				+ "withoutOweCount,withoutOweRatio,owingBanks,owingBanksRatio,"
				+ "balanceNegativeCount,balanceNegativeRatio,";
		
		// 还款意愿模型 ：11
		title += "moreCount,moreRatio,moreSum,moreSumRatio,"
				+"lightCount,lightSum,"
				+"heavyCount,heavyRatio,heavySum,heavySumRatio,"
				+ "delayCount,";
				
		// 逾期模型 ： 6
		title +=  "curOverdueCount,curOverdueCountRatio,"
				+ "curOverdueMoney,curOverdueMoneySum,"
				+ "returnLeastCount,returnLeastRatio,";		 
		
		// 信用额度模型：3
		title += "creditDescCount,creditDescRatio,creditToZero,";
		
		// 消费模型 ：4
		title += "consumeSumMoney,consumeSumCount,consumeAverage,consumePerCost,";
	
		// 循环利息: 2
		title += "cycleRateSum,cycleRateCount,";
		 
		// 取现信息 ：1
		title += "preBorrowZeroCount,";
		
		// 信用透支: 2
		title += "useableZeroCount,useableZeroCountRatio,";
		
		// 放款前后 ：2
		title += "beforeLoan,afterLoan,beforeLoanKonwn,";
		
		// 账户年限 ：3
		title += "creditYearsAverage,creditYearsLongest,creditYearsShortest,";
						
		// 以放款时间为截断点,统计贷款前后的信用卡参量
		for(int  i=0;i<beforeLength;i++)
			title += "beforeLoanBill"+(i+1)+",";
		for(int i =0;i<afterLength;i++)
			title += "afterLoanBill"+ (i+1)+",";
		title += "loanUseTime,perOwingAfterLoaning,withoutOwingBeforeLoan,backRatio,banksMoreThanFour,";
		
		
		for(int i =0;i<bankLength;i++)
			title += "bankDistribute"+i+",";
		
		return title;
	}
	public String toString()
	{
		String title = new String();
		
		// 基本信息：2
	    title += bankCount+","+billCount+",";
		
		// 欠款模型：14
		title +=  totalOwing+","+totalBacking+","+totalBackSubOwe+","+totalBackSubOweFlag+","
				+ perBankOwing+","+perTermOwing+","+owingBankMax+","+owingPerMax+","
				+ withoutOweCount+","+withoutOweRatio+","+owingBanks+","+owingBanksRatio+","
				+ balanceNegativeCount+","+balanceNegativeRatio+",";
		
		// 还款意愿： 12
		for(int i=0;i<moreLength;i++)
			title += moreBacking[i]+",";
		for(int i=0;i<lightLength;i++)
			title += lightOwing[i]+",";
		for(int i=0;i<heavyLength;i++)
			title += heavyOwing[i]+",";
		title += delayCount+",";
		
		// 逾期模型 ：6
		title +=  curOverdueCount+","+curOverdueCountRatio+","
				+ curOverdueMoney+","+curOverdueMoneySum+","
				+ returnLeastCount+","+returnLeastRatio+",";	 
		
		// 信用额度模型：
		title += creditDescCount+","+creditDescRatio+","+ creditToZero+",";
						
		// 消费模型 ：
		title +=  consumeSumMoney+","+consumeSumCount+","+consumeAverage+","+consumePerCost+",";
				
		// 循环利息: 
		title += cycleRateSum+","+cycleRateCount+",";
		 
		// 取现信息 ：
		title += preBorrowZeroCount+",";
		
		// 信用透支: 
		title += useableZeroCount+","+useableZeroCountRatio+",";
		
		// 放款前后的笔数
		title += beforeLoan+","+afterLoan+","+beforeLoanKonwn+",";
		
		// 账户年限 ：
		title += creditYearsAverage+","+creditYearsLongest+","+creditYearsShortest+",";
		
		for(int  i=0;i<beforeLength;i++)
			title += beforeLoanBill[i]+",";
		for(int i =0;i<afterLength;i++)
			title += afterLoanBill[i]+",";
		title += loanUseTime+","+perOwingAfterLoaning+","+withoutOwingBeforeLoan+","+backRatio+","+banksMoreThanFour+",";
			
		for(int i =0;i<bankLength;i++)
			title += bankDistribution[i]+",";
		
		return title;		
	}
	public static String defaultMissing(String replaceCode)                                  // 缺失值填充
	{
		String missing = new String();
		for(int i=0;i<length;i++)
			missing += replaceCode+",";
		return missing;		
	}	
}
public class billRecord
{		
	
	
	public static  ArrayList<Integer> getMainBank()
	{
	    ArrayList<Integer> mainBanks  = new  ArrayList<Integer>();
		mainBanks.add(7);mainBanks.add(14);mainBanks.add(4);
		mainBanks.add(16);mainBanks.add(6);mainBanks.add(3);
		mainBanks.add(10);mainBanks.add(11);mainBanks.add(8);
		mainBanks.add(2);mainBanks.add(13);mainBanks.add(9);
		mainBanks.add(15);mainBanks.add(5);			
		return mainBanks;
	}
	
	    // 获取用户的放款时间	
	public static HashMap<Long,Long> loanTime(String loanTimeFile) throws IOException    // 放款时间
		{		
			HashMap<Long,Long> loanTimeMap = new HashMap<Long,Long>();
			
			File file=new File(loanTimeFile);	                                                                   
			BufferedReader reader = null;         
			reader = new BufferedReader(new FileReader(file));  
			String tempString = null;
			while((tempString = reader.readLine())!=null)
			{
				 String [] temp = tempString.split(",");
				 loanTimeMap.put(Long.valueOf(temp[0]), (Long.valueOf(temp[1])-5800000000L)/86400);			 
			}
			reader.close();
			return loanTimeMap;
		}
		
	public static HashMap<Long,billFeature> record(String billDetailFile,String billFeatureFile,String loanTimeFile) throws IOException  //获取信用卡账单记录特征
	{
		HashMap<Long,Long> loanTimeMap = loanTime(loanTimeFile);
		
		HashMap<String,ArrayList<billDetail>>  billDetailList = new  HashMap<String,ArrayList<billDetail>> ();      //每个用户对应的银行记录清单
		
		HashMap<Long,billFeature> billFeatureList = new HashMap<Long,billFeature>();
        File file=new File(billDetailFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
	   
		File file2=new File(billFeatureFile);                                                                 
		FileWriter fw = null;
        BufferedWriter writer = null;
        fw = new FileWriter(file2);
        writer = new BufferedWriter(fw);
								    
		while((tempString = reader.readLine())!=null)
		{
			 String [] temp = tempString.split(",");
			 ArrayList<billDetail> tempList;
			 
			 if((tempList = billDetailList.get(temp[0]))==null)
				{
				    tempList = new ArrayList<billDetail>();                                                   
				}
	 
	      tempList.add(new billDetail(temp[0],Long.valueOf(temp[1]),Integer.valueOf(temp[2]),Float.valueOf(temp[3]),Float.valueOf(temp[4]),
					 Float.valueOf(temp[5]),Float.valueOf(temp[6]),Float.valueOf(temp[7]),Integer.valueOf(temp[8]),Float.valueOf(temp[9]), 
					 Float.valueOf(temp[10]),Float.valueOf(temp[11]),Float.valueOf(temp[12]),Float.valueOf(temp[13]),Integer.valueOf(temp[14])));
	      
			
	      billDetailList.put(temp[0], tempList); 
		  //billDetailList.put("-1", tempList);                                // 缺失用户使用平均值填充
		}
		reader.close();	
				
		writer.write(new billFeature().fetureTitle()+"\n");
		writer.flush();
		
		Iterator iter = billDetailList.entrySet().iterator();               // 遍历有银行记录的用户的交易清单
		while (iter.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			ArrayList<billDetail> val = (ArrayList<billDetail>) entry.getValue();
						
			billFeature  feature = getFeatureList(val,loanTimeMap);                                      //银行记录提取的特征	
			billFeatureList.put(Long.valueOf(key), feature);	
			writer.write(key+","+feature.toString()+"\n");                            
		}		
		writer.flush();
		writer.close();	
		return billFeatureList;
     }
	
	public static billFeature  getFeatureList(ArrayList<billDetail> billList,HashMap<Long,Long>loanTimeMap)  throws IOException             // 一个用户的各个银行下的账单清单
	{
		billFeature resultBill =  new billFeature();
		billFeature perBankBill = new billFeature();
		
		 ArrayList<Integer> bankDistribution = getMainBank();                                              // 银行分布情况
		 
		ArrayList<Float> useYears =  new ArrayList<Float>();                                              // 每个账户的使用年限		
		HashMap<Integer,ArrayList<billDetail>> billMap = new HashMap<Integer,ArrayList<billDetail>>();    // 分银行统计属性			
						
		Iterator<billDetail> it = billList.iterator();
	    while(it.hasNext())
	    {	    	                                                    
	    	billDetail tempDetail = it.next();  
	    	
	    	if(bankIndex(bankDistribution,tempDetail.bankID) == -1)
	    		resultBill.bankDistribution[14] ++;	
	    	else
	    		resultBill.bankDistribution[bankIndex(bankDistribution,tempDetail.bankID)] ++;	       // 每个银行下的账单的分布情况 
	    	
	    	ArrayList<billDetail> billListOfEachBank;                                                  // 一个银行的信用账单记录
	    	if((billListOfEachBank = billMap.get(tempDetail.bankID)) == null)
	    		billListOfEachBank = new ArrayList<billDetail>();
	    	billListOfEachBank.add(tempDetail);
	    	billMap.put(tempDetail.bankID, billListOfEachBank);	    	
	    }	
	    
	    int recordCount = billList.size();                                                            // 信用卡记录数
		resultBill.billCount = billList.size();	
	    int bankCount = billMap.size();                                                               // 银行数目(账户数目)
	    resultBill.bankCount =  billMap.size();	   	 
	   
	    ArrayList<Float> owingBankList = new  ArrayList<Float>();                                     // 每个银行账户下的欠款数
	    ArrayList<Float> owingPerList = new  ArrayList<Float>();   
	   
	    Iterator iter = billMap.entrySet().iterator();	
		while (iter.hasNext()) 
		   {					 
			 Map.Entry entry = (Map.Entry) iter.next();
			 Integer key = (Integer) entry.getKey();
			 ArrayList<billDetail> value = (ArrayList<billDetail>)entry.getValue();
		 
			 for( billDetail  temp : value)
				 resultBill.billTimeList.add(temp.time);                                    // 账单时间列表			 
			 Collections.sort(resultBill.billTimeList);
			 
			 
			 perBankBill = getPerBank(key,value,loanTimeMap);                               // 每个银行下的信用账单
			 
			//ArrayList<billDetail> cleanAgain = BillResort.cleanAndfill(value);
			//perBankBill = getPerBank(key,cleanAgain,loanTimeMap);                          // 二次去重后的数据
			 
			 for(int i=0;i<billFeature.bankLength;i++)
				 resultBill.bankDistribution[i] =  resultBill.bankDistribution[i] >= 1 ? 1 : 0;
			 			 			 
			 owingBankList.add(perBankBill.totalBacking - perBankBill.totalOwing);          // 尚未还清的银行数	
			 owingPerList.add(perBankBill.owingPerMax);                                     // 每期欠款最多金额
			 
			 resultBill.totalOwing += perBankBill.totalOwing;
			 resultBill.totalBacking += perBankBill.totalBacking;                           // 总共欠款和总共的还款数
			 
			 resultBill.withoutOweCount  +=  perBankBill.withoutOweCount;                   // 不欠款的期数之和
			
			 // 还款意愿情况：轻度拖欠、重度拖欠和多还钱的情况
			 resultBill.moreBacking[0] += perBankBill.moreBacking[0];
			 resultBill.moreBacking[2] += perBankBill.moreBacking[2];			 
			 resultBill.lightOwing[0] += perBankBill.lightOwing[0];
			 resultBill.lightOwing[1] += perBankBill.lightOwing[1];			 
			 resultBill.heavyOwing[0] += perBankBill.heavyOwing[0];
			 resultBill.heavyOwing[2] += perBankBill.heavyOwing[2];  			 
			 resultBill.delayCount    += perBankBill.delayCount;                             // 拖欠期数
			 resultBill.balanceNegativeCount += perBankBill.balanceNegativeCount ;           // 其那款还清的次数
			 
			 // 逾期情况：当前逾期金额、当前逾期总期数、当前逾期总金额、最低还款笔数
			 resultBill.curOverdueMoney  +=  perBankBill.curOverdueMoney;                 // 所有账户下的当前逾期金额之和				    
			 resultBill.curOverdueCount += perBankBill.curOverdueCount;                   // 所有账户下的逾期期数之和
			 resultBill.curOverdueMoneySum += perBankBill.curOverdueMoneySum;             // 所有账户下的逾期总金额			 			                                             			
			 resultBill.returnLeastCount +=  perBankBill.returnLeastCount;                // 正常还款的期数之和
			 
			 // 信用额度情况 ：信用额度下降次数、变成0的期数
			 resultBill.creditDescCount += perBankBill.creditDescCount;	                  // 信用额度下调的期数之和					 			
			 resultBill.creditToZero += perBankBill.creditToZero;                         // 信用额度从正值突然变成零的期数
			 
			 // 消费情况：消费总金额和消费笔数，计算消费水平
	         resultBill.consumeSumMoney +=  perBankBill.consumeSumMoney;
			 resultBill.consumeSumCount += perBankBill.consumeSumCount;                   // 平均消费水平和逾期内消费水平				                  			
				
			 // 循环利息：循环利息总数和循环利息不为零的期数
			 resultBill.cycleRateSum   += perBankBill.cycleRateSum;	
			 resultBill.cycleRateCount +=  perBankBill.cycleRateCount;                   // 循环利息的期数
	 
			 // 预借现金情况:预借现金为0的期数
			 resultBill.preBorrowZeroCount += perBankBill.preBorrowZeroCount;            // 预借现金额度为0的期数
					
			 // 可用余额为零的期数
			 resultBill.useableZeroCount += perBankBill.useableZeroCount;                // 可用余额等于0的期数	
			 
			 // 放款前后的期数
			 resultBill.beforeLoan += perBankBill.beforeLoan;						    
			 resultBill.afterLoan += perBankBill.afterLoan;                                 // 放款前后的笔数
			 resultBill.beforeLoanKonwn += perBankBill.beforeLoanKonwn;                     // 已知时间戳(非零)在放款之前的笔数
			    
			 if(resultBill.creditYearsLongest < perBankBill.creditYearsLongest)
				 resultBill.creditYearsLongest = perBankBill.creditYearsLongest;          // 最长账户年限
			 if(resultBill.creditYearsShortest > perBankBill.creditYearsShortest)
				 resultBill.creditYearsShortest = perBankBill.creditYearsShortest;        // 最短账户年限
			 useYears.add((float) perBankBill.creditYearsAverage);	
			 
			 resultBill.beforeLoanBill[0] += perBankBill.beforeLoanBill[0];
			 resultBill.beforeLoanBill[1] += perBankBill.beforeLoanBill[1];
			 resultBill.beforeLoanBill[2] += perBankBill.beforeLoanBill[2];
			 resultBill.beforeLoanBill[3] += perBankBill.beforeLoanBill[3];
			 
			 resultBill.afterLoanBill[0] += perBankBill.afterLoanBill[0];
			 resultBill.afterLoanBill[1] += perBankBill.afterLoanBill[1];
			 resultBill.afterLoanBill[2] += perBankBill.afterLoanBill[2];
			 resultBill.afterLoanBill[3] += perBankBill.afterLoanBill[3];
			 
		   }
		
	     /* 欠款/还款模型	*/	
		resultBill.totalBackSubOwe = resultBill.totalOwing - resultBill.totalBacking ;        // 到目前还欠银行金额
		resultBill.totalBackSubOweFlag = resultBill.totalBackSubOwe >= 0 ?  1 : 0;            // 用户欠钱还是用户存款为正？		
		resultBill.perBankOwing = resultBill.totalBackSubOwe / bankCount;                     // 平均每个银行欠款均值
		resultBill.perTermOwing = resultBill.totalBackSubOwe / recordCount;                   // 平均每期欠款数均值
		
		resultBill.owingBankMax = maxAbsElement(owingBankList);
		resultBill.owingPerMax = maxAbsElement(owingPerList);                                   // 最多欠某个银行多少钱，在一个银行下最多每期拖欠金额
				
		resultBill.withoutOweRatio = (float) resultBill.withoutOweCount / recordCount;        // 不欠钱的期数比例
		resultBill.owingBanks = owingElement(owingBankList);                                  // 尚未还清欠款的银行数量	
		resultBill.owingBanksRatio = (float)resultBill.owingBanks / bankCount;                // 尚未还清欠款的银行占比
		resultBill.balanceNegativeRatio = (float) resultBill.balanceNegativeCount / recordCount;     // 还清欠款的比例
		
		/*  还款意愿模型  */
		resultBill.moreBacking[1] = (float) resultBill.moreBacking[0] / recordCount;         // 多还款的期数,金额占总欠款的占比
		resultBill.moreBacking[3] = resultBill.totalOwing <= 0 ? 
															-1 : (float) resultBill.moreBacking[2] / resultBill.totalOwing;
		
		resultBill.heavyOwing[1] = (float) resultBill.heavyOwing[0] / recordCount;           // 重度拖欠期数，金额占总欠款的占比
		resultBill.heavyOwing[3] = resultBill.totalOwing <= 0 ? 
															-1 : (float) resultBill.heavyOwing[2] / resultBill.totalOwing;
		
		/*  逾期模型   */
		resultBill.curOverdueCountRatio = (float) resultBill.curOverdueCount / recordCount;   // 逾期的比例
		resultBill.returnLeastRatio = (float) resultBill.returnLeastCount / recordCount;      // 正常还款比例
				
		/* 信用额度模型   */		
		resultBill.creditDescRatio = (float) resultBill.creditDescCount / recordCount;        // 信用下调次数比例 		                                                         
		//resultBill.creditImproveFlag = resultBill.creditImproveFlag > bankCount * 0.7 ? 1 : 0;   // 超过 70% 的银行信用下降，则整体信用下降   
		     			
		/* 消费模型  */
		resultBill.consumeAverage = resultBill.consumeSumCount / bankCount;                      // 平均每个银行下的消费笔数
		resultBill.consumePerCost = resultBill.consumeSumCount == 0 ? -1 : resultBill.consumeSumMoney / resultBill.consumeSumCount;     // 平均每笔的消费水平
				
		resultBill.useableZeroCountRatio = (float)resultBill.useableZeroCount / recordCount;      // 可用余额为0的期数占总期数的比例
		
		/* 账户年限模型 ：最长、最短以及平均年限 */            // 是给用户账户定义标签  ？？？
		float avgYears = 0;
		for(int i=0;i<useYears.size();i++)
			avgYears += useYears.get(i);
		resultBill.creditYearsAverage = (long) (avgYears / useYears.size());                      // 账户平均使用年限		
				
		/* 贷款前后的请情况对比 */
		resultBill.beforeLoanBill[4] = resultBill.beforeLoanBill[0] <= 0 ?  
				         10f:(resultBill.beforeLoanBill[0] - resultBill.beforeLoanBill[1]) / resultBill.beforeLoanBill[0];
		
		resultBill.afterLoanBill[4] = resultBill.afterLoanBill[0] <= 0 ?  
		                 10f:(resultBill.afterLoanBill[0] - resultBill.afterLoanBill[1]) / resultBill.afterLoanBill[0];
		
		resultBill.loanUseTime =  resultBill.billTimeList.get( resultBill.billTimeList.size()-1) - loanTimeMap.get(Long.valueOf(billList.get(0).userID)) <= 0 ?
								-1 : resultBill.billTimeList.get(resultBill.billTimeList.size()-1) - loanTimeMap.get(Long.valueOf(billList.get(0).userID));
		// 贷款使用时间 (最后一笔账单时间 - 放款时间 )
		
		// 贷款之后平均每个月欠银行多少钱
		resultBill.perOwingAfterLoaning = resultBill.loanUseTime == -1 ? -1 : resultBill.totalBackSubOwe / resultBill.loanUseTime * 30f;
		
		resultBill.withoutOwingBeforeLoan = resultBill.beforeLoanBill[1] <= 0 ? 1 : 0;         // 贷款前是否还清了当前信用卡的借款
		resultBill.backRatio = resultBill.totalOwing <= 0 ? 10f : resultBill.totalBacking / resultBill.totalOwing;   // 全局还款比例
		resultBill.banksMoreThanFour = resultBill.bankCount >= 4 ?  1 : 0;                     // 银行数目  >=4?
		
		return resultBill;
	}
	public static billFeature getPerBank(int bankId,ArrayList<billDetail> billList,HashMap<Long,Long> loanTimeMap) throws IOException
	{				
		long userKey  = Long.valueOf(billList.get(0).userID);
		long loanTime = loanTimeMap.get(userKey);                                                  // 用户放款时间
						
		 ArrayList<Long> timeSeries =  new ArrayList<Long>();                                      // 账单时期
		// ArrayList<Long> heavyTimeStampList = new ArrayList<Long> ();                            // 重度拖欠时间戳清单
		
		billFeature billOfThisBank = new billFeature();		
		billOfThisBank.billCount = billList.size();	
		
		for(int i=0;i<billList.size();i++)
		{
			billDetail eachDetail = billList.get(i);
					
			billOfThisBank.totalOwing += eachDetail.lastBill;
			billOfThisBank.totalBacking += eachDetail.lastReturn;                               // 在该银行下欠款和还款累加
						
			if(eachDetail.lastBill <= 0 )
				billOfThisBank.withoutOweCount ++;                                              // 不欠钱的期数
			
			// 还款意愿 : 多还款、轻度拖欠、重度拖欠、拖欠笔数 (有效还款，0-0 除外)
			if(eachDetail.lastBill != 0 && eachDetail.lastReturn != 0 && eachDetail.lastBill <= eachDetail.lastReturn)
				{
					billOfThisBank.moreBacking[0] ++;                                               // 还款多于欠款的期数
					billOfThisBank.moreBacking[2] +=  eachDetail.lastReturn - eachDetail.lastBill; // 多还的金额					
				}
			if(eachDetail.lastBill > 0 && eachDetail.lastReturn > 0 && eachDetail.lastBill > eachDetail.lastReturn )
				{
					billOfThisBank.lightOwing[0] ++;                                               // 轻度拖欠期数
					billOfThisBank.lightOwing[1] += eachDetail.lastBill - eachDetail.lastReturn;  // 轻度拖欠的金额总数					
				}
			if( (eachDetail.lastBill > 0 && eachDetail.lastReturn <= 0) || 
					                                  (eachDetail.lastBill == 0 && eachDetail.lastReturn < 0) )
				{				
					billOfThisBank.heavyOwing[0] ++;                                              // 重度拖欠的期数
					billOfThisBank.heavyOwing[2] += eachDetail.lastBill - eachDetail.lastReturn;  // 重度拖欠的金额	
					
					if(eachDetail.time <= loanTime)                                              // 还款前后的重度拖欠情况
						{
							billOfThisBank.beforeLoanBill[2] ++;
							billOfThisBank.beforeLoanBill[3] += (eachDetail.lastBill - eachDetail.lastReturn);							
						}
					else if(eachDetail.time > loanTime)
					{
						billOfThisBank.afterLoanBill[2] ++;
						billOfThisBank.afterLoanBill[3] += (eachDetail.lastBill - eachDetail.lastReturn);						
					}
				}
			
			if(eachDetail.thisBillRest <= 0)
				billOfThisBank.balanceNegativeCount ++;                                         // 欠款还清的次数
			
			billOfThisBank.consumeSumCount += eachDetail.consume;                               // 在银行下的消费总笔数
			billOfThisBank.consumeSumMoney += eachDetail.thisBill;                              // 消费总金额(即本期账单之和)
		
			billOfThisBank.cycleRateSum += eachDetail.cycleInterest;
			if(eachDetail.cycleInterest > 0)
				billOfThisBank.cycleRateCount ++ ;                                              // 循环利息大于0期数累加
							
			if(eachDetail.borrowMoney <= 0)
				billOfThisBank.preBorrowZeroCount ++;                                           // 预借现金额度为0的期数
					
			if(eachDetail.useableMoney == 0)
				billOfThisBank.useableZeroCount ++;                                             // 可用余额等于0的期数
			
			timeSeries.add(eachDetail.time);                                                    // 放款时间先后的期数			
			if(eachDetail.time <= loanTime)
				{
					billOfThisBank.beforeLoan ++;
					billOfThisBank.beforeLoanBill[0] += billOfThisBank.totalOwing;              // 贷款前总共欠款、还欠多少					
					billOfThisBank.beforeLoanBill[1] += billOfThisBank.totalOwing - billOfThisBank.totalBacking;					 
				}
			else if(eachDetail.time > loanTime)
				{
					billOfThisBank.afterLoan ++;                                               // 贷款之后的账单数
					billOfThisBank.afterLoanBill[0] += billOfThisBank.totalOwing;              // 贷款后共欠款、还欠多少					
					billOfThisBank.afterLoanBill[1] += billOfThisBank.totalOwing - billOfThisBank.totalBacking;
				}
			if(eachDetail.time <= loanTime && eachDetail.time != 0)
				billOfThisBank.beforeLoanKonwn ++;
		}
		
		billOfThisBank.owingPerMax = (billOfThisBank.totalOwing - billOfThisBank.totalBacking) / billList.size();  // 该银行平均每期欠款，求欠的最多的银行和期数金额         		
		billOfThisBank.delayCount = (int)billOfThisBank.lightOwing[0] + (int)billOfThisBank.heavyOwing[0];  // 拖欠期数(轻度拖欠和重度拖欠)
		
		// 计算有问题!!使用两重循环搜索上期账单!!
		for(int i=0;i<billList.size()-1;i++)                                                    // 考察相邻两期记录
		{
			billDetail preDetail  = billList.get(i);
			billDetail curDetail  = billList.get(i+1);                              		    // 前一笔账单和当前账单
			
			if(curDetail.lastReturn < preDetail.curLeastReturn)                                 // 上期还款  < 上期最低还款 ， 逾期
			{								
				billOfThisBank.curOverdueMoneySum += (preDetail.curLeastReturn - curDetail.lastReturn);  //逾期期间总共欠银行总金额
				billOfThisBank.curOverdueCount ++;	                                            // 该银行下的历史逾期笔数累加 						
			}
			else 
				billOfThisBank.returnLeastCount ++;                                            // 正常还款期数
			
			// 相邻两期的信用额度变化情况
			if(curDetail.credit < preDetail.credit)
				billOfThisBank.creditDescCount ++;                                             // 信用额度下调次数
			if(curDetail.credit == 0 && preDetail.credit > 0)
				billOfThisBank.creditToZero ++;                                                // 信用额度突然变成0
		}
						
		int i = billList.size()-1;
		for(;i>=1;i--)                                                                         // 最后一笔逾期金额(当前逾期金额)
		{ 
			if(billList.get(i).lastReturn < billList.get(i-1).curLeastReturn )
			{
				billOfThisBank.curOverdueMoney =  billList.get(i-1).curLeastReturn - billList.get(i).lastReturn;   
				break;
			}
		}				
		if(i == 0) 
			billOfThisBank.curOverdueMoney = 0;                                               // 没有逾期的记录，当前逾期金额为0   
		                                          									
		// 对用户的时间戳排序，如果用户的时间戳全是未知，以笔数来论账户的年限
				        
		/*Collections.sort(timeSeries);
		
		if(timeSeries.size() == 1)                                                    // 只有一条记录      
		    billOfThisBank.creditYearsAverage = billOfThisBank.creditYearsLongest =  billOfThisBank.creditYearsShortest
                         = 30L;     				
		else if(timeSeries.get(timeSeries.size()-1) == 0)                             //  时间戳全是未知				
			billOfThisBank.creditYearsAverage = billOfThisBank.creditYearsLongest =  billOfThisBank.creditYearsShortest
			                                              = timeSeries.size()*30L;    // 按照每月30天计算
		else
		{
			int zeroNotZero = 0;
			for(;zeroNotZero<timeSeries.size();zeroNotZero++)                         //  确知的最早的账单时间
				{
					if(timeSeries.get(zeroNotZero) != 0)
						break;
				}
			if(zeroNotZero <= timeSeries.size()/2)                                           // 至少一半的账单带有时间戳
				billOfThisBank.creditYearsAverage = billOfThisBank.creditYearsLongest  =  billOfThisBank.creditYearsShortest
				                                            = timeSeries.get(timeSeries.size()-1) - timeSeries.get(zeroNotZero);
			else                                                                           
				// 缺失的时间戳太多
			   billOfThisBank.creditYearsAverage = billOfThisBank.creditYearsLongest  = billOfThisBank.creditYearsShortest
			         	                                    = timeSeries.size()*30L;
		}	
		*/
		//billOfThisBank.creditYearsAverage = billOfThisBank.creditYearsLongest  = billOfThisBank.creditYearsShortest
		
		Collections.sort(timeSeries);
		if(timeSeries.get(timeSeries.size()-1) == 0)				
			billOfThisBank.creditYearsAverage = billOfThisBank.creditYearsLongest = billOfThisBank.creditYearsShortest 
					                                                              = timeSeries.size()*30L;     // 按照每月30天计算
		else
			billOfThisBank.creditYearsAverage = billOfThisBank.creditYearsLongest  = 
					     billOfThisBank.creditYearsShortest	= timeSeries.get(timeSeries.size()-1) -timeSeries.get(0);                                                                //= timeSeries.size()*30L;
		return billOfThisBank;		
	}
	
 	public static float maxAbsElement(ArrayList<Float> list)              // 绝对值最大的数，正负号有含义的，用户欠银行 还是 银行欠用户
	{
		if(list.size() == 1)
			return list.get(0);
		else
			{
				Collections.sort(list);
				if( Math.abs(list.get(0)) > Math.abs(list.get(list.size()-1)) )
					return list.get(0);
				else
					return list.get(list.size()-1);
			}		
	}
	public static int mostConsume(ArrayList<Integer> list)
	{
		int max = list.get(0);
		for(int i =0;i<list.size();i++)
			if(max < list.get(i))
				max = list.get(i);
		return max;
	}
	public static int owingElement(ArrayList<Float> list)                // 未还清欠款的银行数量
	{
		int owingCount = 0 ;
		for(int i =0;i<list.size();i++)
			if(list.get(i) < 0)
				owingCount ++;
		return owingCount;
	}
	public static long  minInetrval(ArrayList<Long> list)
	 {
		 ArrayList<Long> timeList = new ArrayList<>();
		 Collections.sort(list);
		 if(list.size() == 1)
			 return  -1;
		 else
		 {
			 for(int i = 0;i<list.size()-1;i++)
			 {
				 if(list.get(i) != 0 && list.get(i+1) != 0)
					 timeList.add(list.get(i+1) - list.get(i));
			 }
		 }
		 if(timeList.size() == 0)
			 return -1;
		 else 
			 return timeList.get(0);
	 }
	public static int bankIndex(ArrayList<Integer> mainBanks, Integer bankID)      //对应哪个银行
	 {
		 for(int i=0;i<mainBanks.size();i++)
			 {
			 	if(bankID == mainBanks.get(i) )
			 		return i;	
			 }
		return -1;		 
	 }	
}
