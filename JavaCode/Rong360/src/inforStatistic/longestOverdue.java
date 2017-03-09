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

public class longestOverdue 
{
	
	public static void main(String [] argv) throws IOException
	{
		String NewdataPath = "C:/Users/Liuxiang/Desktop/Credit/Data/train/LabeledTrain/";   
		String testDataPath ="C:/Users/Liuxiang/Desktop/Credit/Data/test/";
		String trainDataPath = "C:/Users/Liuxiang/Desktop/Credit/Data/train/";
		
	// 分离出账户清单
		
	  //   GBPerBank(NewdataPath+"Good_New_bill_detail.txt", NewdataPath+"Good_perBillBank.txt");
	  //   GBPerBank(NewdataPath+"Bad_New_bill_detail.txt", NewdataPath+"Bad_perBillBank.txt");
	  //   GBPerBank(trainDataPath+"clean_bill_detail_train.txt", trainDataPath+"Train_perBillBank.txt");
	  //   GBPerBank(testDataPath+"clean_bill_detail_test.txt", testDataPath+"Test_perBillBank.txt");
		
		// 计算每个用户在每个银行下的最长逾期时间
		 
		billPerBank(NewdataPath+"Good_New_bill_detail.txt",NewdataPath+"goodPerBankLongestOverdue.csv");
		billPerBank(NewdataPath+"Bad_New_bill_detail.txt",NewdataPath+"badPerBankLongestOverdue.csv");					
	}
	// 信用卡时间维度特征提取
	public static HashMap<Long,String> timeFeature(String billFile,String timeFeatureFile)throws IOException
	{
		ArrayList<String> resultString = billPerBank(billFile,timeFeatureFile);
				
		HashMap<Long,ArrayList<String>> userMap = new HashMap<Long,ArrayList<String>>();
		HashMap<Long,String> timeFeatureMap = new HashMap<Long,String>();
		
		for(String tempString : resultString)
		{
			 ArrayList<String>  tempList;
			 String [] temp = tempString.split(",");
			 if( (tempList = userMap.get(Long.valueOf(temp[0])) ) == null)            // 每个用户在每个银行下的信用卡账单数据统计
				 tempList = new ArrayList<String>();
			 tempList.add(tempString);
			 userMap.put(Long.valueOf(temp[0]),tempList);			 
		}
		// 每个用户在每个银行下的特征
		Iterator iter = userMap.entrySet().iterator();			   
		while (iter.hasNext()) 
			 {	
				// zombie账户清单、一行多卡标志位、最多持有卡数、共持有卡数、账单开始时间戳、重点时间戳和时间间隔、小于最低还款时间间隔
			   	 ArrayList<Long> deadCard = new  ArrayList<Long>();
			   	 ArrayList<Long> cardsPerBankFlag = new  ArrayList<Long>();
				 ArrayList<Long> cardsPerBankCount= new  ArrayList<Long>();
				 ArrayList<Long> startTime = new   ArrayList<Long>();
				 ArrayList<Long> finishTime = new  ArrayList<Long>();
				 ArrayList<Long> differTime = new  ArrayList<Long>();
				 ArrayList<Long> ovedueTime = new  ArrayList<Long>();
			   	 
				 Map.Entry entry = (Map.Entry) iter.next();
				 long key = (long) entry.getKey();				 
				 ArrayList<String> value = (ArrayList<String>)entry.getValue();
				 for(int i = 0;i<value.size();i++)
				 {
					 String [] temp = value.get(i).split(",");
					 deadCard.add(Long.valueOf(temp[2]));
					 cardsPerBankFlag.add(Long.valueOf(temp[3]));
					 cardsPerBankCount.add(Long.valueOf(temp[4]));
					 startTime.add(Long.valueOf(temp[5]));
					 finishTime.add(Long.valueOf(temp[6]));
					 differTime.add(Long.valueOf(temp[7]));
					 ovedueTime.add(Long.valueOf(temp[8]));
				 }
				 
				long deadCardFlag = 0;
				long blankAccount = 0;                         // 空账户数量
				float blankAccountRatio = 0;                   // 空账户的比例
								
				for(int  i=0;i<deadCard.size();i++) 
				{
					if(deadCard.get(i) == 0)                  // 是否存在空账户
						{
							deadCardFlag = 1;
							break;
						}
				}
				for(int  i=0;i<deadCard.size();i++) 
					blankAccount += deadCard.get(i);
				blankAccountRatio += deadCard.size() == 0 ? 0 : (float)blankAccount / deadCard.size();
								
				long cardsPerBankFlags = 0;
				long cardsTotal = 0;                          // 总的信用卡数
				for(int  i=0;i<cardsPerBankFlag.size();i++)   // 是否存在一行多卡情况
				{
					if(cardsPerBankFlag.get(i) == 1) 
						{
							cardsPerBankFlags = 1;
							break;
						}
				}
				for(int i =0 ;i<cardsPerBankCount.size();i++)
					cardsTotal += cardsPerBankCount.get(i);
				 
				Collections.sort(cardsPerBankCount);
				Collections.sort(startTime);                     // 起始时间戳序列
				Collections.sort(finishTime);                    // 截止时间戳序列
				Collections.sort(differTime);                    // 时间间隔序列
				Collections.sort(ovedueTime);
				
				//long maxInterval = finishTime.get(finishTime.size()-1) - startTime.get(0);
				//long minInterval = finishTime.get(0) - startTime.get(startTime.size()-1);  // 最大、最小时间间隔
				
				/*
				 *  空账户标志、空账户数量、空账户比例、一行多卡标志、最多持卡数、总共持卡数量、最早的开卡时间、已知开卡时间、最短卡龄、最长卡龄
				 */
				String timeString = deadCardFlag+","+			           						
									blankAccount+","+
									blankAccountRatio+","+									
									cardsPerBankFlags+","+
				                    cardsPerBankCount.get(cardsPerBankCount.size()-1)+","+
						            cardsTotal+","+				                    
				                    //(startTime.get(startTime.size()-1)   < 0 ? "NA":startTime.get(startTime.size()-1))+","+
				                   // (finishTime.get(finishTime.size()-1) < 0 ? "NA":finishTime.get(finishTime.size()-1))+","+
				                    startTime.get(startTime.size()-1)+","+
				                    finishTime.get(finishTime.size()-1)+","+
				                    differTime.get(0)+","+
				                    differTime.get(differTime.size()-1)+","+             				                    
				                    ovedueTime.get(ovedueTime.size()-1)+",";
				
				timeFeatureMap.put(key, timeString);
			 }
			return timeFeatureMap;			
	}
	 // 根据账单清单上下文获取用户的缺失时间的起点,如果用户没有一个时间点,则默认为全局最早的点
	public static HashMap<Long,Long>  defaultTimeStart(String billFile)throws IOException
	{
		HashMap<Long,Long> billStartMap = new  HashMap<Long,Long>();
		HashMap<Long,ArrayList<Long>> billTimeList = new HashMap<Long,ArrayList<Long>>();    // 每个用户的账单时间序列
		
		File file=new File(billFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
		while((tempString = reader.readLine())!=null)
		{
			 ArrayList<Long> tempList ;		
			 String [] temp = tempString.split(",");
			 Long userID = Long.valueOf(temp[1]);
			 if( (tempList = billTimeList.get(userID))== null)
				 tempList = new ArrayList<Long>();
			 tempList.add((Long.valueOf(temp[1])- 5800000000L)/86400); 
			 billTimeList.put(userID,tempList);			 
		}
		
		ArrayList<Long> unknownUser = new  ArrayList<Long> ();              // 未知用户的起始时间用全局最早时间来代替
		ArrayList<Long> knownUser = new  ArrayList<Long> ();  
		ArrayList<Long> knownTime = new  ArrayList<Long> ();
		
		Iterator iter = billTimeList.entrySet().iterator();               // 遍历有银行记录的用户的交易清单
		while (iter.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) iter.next();
			long user = (long) entry.getKey();
			ArrayList<Long> timeList = (ArrayList<Long>) entry.getValue();
			Collections.sort(timeList);		
			int index = 0;
			for(;index<timeList.size();index++)
			{
				if(timeList.get(index) > 0)                           
					{
						billStartMap.put(user, timeList.get(index));
						knownUser.add(user);
						break;
					}
			}	
			if(index == timeList.size())
				unknownUser.add(user);                              // 时间戳全是0的用户
		}
		
		for(int i=0;i<knownUser.size();i++)
		{
			long userId = knownUser.get(i);
			knownTime.add(billStartMap.get(userId));
		}
		
		Collections.sort(knownTime);
		for(int i=0;i<unknownUser.size();i++)
			billStartMap.put(unknownUser.get(i),knownTime.get(knownTime.size()/2));   // 未知用户使用全局最早时间填充
		billStartMap.put(-1L,knownTime.get(knownTime.size()/2));
		
		return billStartMap;
	}
	public static ArrayList<String>  billPerBank(String billFile,String billPerBankFile)throws IOException
	{
       HashMap<String,ArrayList<billDetail>>  billDetailList = new  HashMap<String,ArrayList<billDetail>> ();      //每个用户对应的银行记录清单		
       ArrayList<String> resultString = new ArrayList<String>();
      
      // HashMap<Long,Long>  defaultTimeStart = defaultTimeStart(billFile);          // 获取确实的时间起点
       
		File file=new File(billFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
	   
		File file2=new File(billPerBankFile);                                                                 
		FileWriter fw = null;
        BufferedWriter writer = null;
        fw = new FileWriter(file2);
        writer = new BufferedWriter(fw);
								    
		while((tempString = reader.readLine())!=null)
		{
			 String [] temp = tempString.split(",");
			 ArrayList<billDetail> tempList;
			 
			 if((tempList = billDetailList.get(temp[0]))==null)				
				    tempList = new ArrayList<billDetail>();                                                   				
	 
	      tempList.add(new billDetail(temp[0],Long.valueOf(temp[1]),Integer.valueOf(temp[2]),Float.valueOf(temp[3]),Float.valueOf(temp[4]),
					 Float.valueOf(temp[5]),Float.valueOf(temp[6]),Float.valueOf(temp[7]),Integer.valueOf(temp[8]),Float.valueOf(temp[9]), 
					 Float.valueOf(temp[10]),Float.valueOf(temp[11]),Float.valueOf(temp[12]),Float.valueOf(temp[13]),Integer.valueOf(temp[14])));
		  billDetailList.put(temp[0], tempList); 		
		}		
		reader.close();	
		
		String tilte = "userID,bankID,deadCard,cardsPerBankFlag,cardsPerBankCount,startTime,"
				      + "finishTime,timeDiffer,longestOverdue\n";
	     writer.write(tilte);
	     
		Iterator iter = billDetailList.entrySet().iterator();               // 遍历有银行记录的用户的交易清单
		while (iter.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			ArrayList<billDetail> val = (ArrayList<billDetail>) entry.getValue();
			HashMap<Integer,ArrayList<billDetail>> billMap = new HashMap<Integer,ArrayList<billDetail>>();    // 分银行统计属性							
			
			// 不分银行的统计量   时间聚合和时间串联！！！！
			
			
			
			
			Iterator<billDetail> it = val.iterator(); 
			while(it.hasNext()) 
	       {	    	                                                    
				billDetail tempDetail = it.next();  	    	
				ArrayList<billDetail> billListOfEachBank;                                      // 一个银行的信用账单记录
				if((billListOfEachBank = billMap.get(tempDetail.bankID)) == null)
					billListOfEachBank = new ArrayList<billDetail>();
	    		billListOfEachBank.add(tempDetail);
	    		billMap.put(tempDetail.bankID, billListOfEachBank);
	       }  
		
		   Iterator iter2 = billMap.entrySet().iterator();			   
		   while (iter2.hasNext()) 
			   {	
			   	 String timeFeature = new String();                                           // 时间戳特征
			   	 
				 Map.Entry entry2 = (Map.Entry) iter2.next();
				 Integer key2 = (Integer) entry2.getKey();
				 
				 ArrayList<billDetail> value = (ArrayList<billDetail>)entry2.getValue();
				 
				 // 账单去重以及时间戳补齐后再进行统计!!!!
				 
				
				 					 
				 long deadCard = deadBank(key2,value);                   // 僵尸账户
				 
				 long cardsPerBankFlag = cardsPerBankFlags(key2,value);  // 是否一行多卡现象？
				 
				 long cardsPerBankCount = cardsPerBankCounts(key2,value); // 在这个银行下的信用卡数量
				 
				 long longestOverdue = getPerBank(key2,value);
				  
				 timeFeature += key+","+key2+","+deadCard+","+cardsPerBankFlag+","+cardsPerBankCount+",";
				 
				 for(Long time : timeStamp(key2,value))                   // 获取确知的时间起点         
						 timeFeature += time+",";   
				 timeFeature +=longestOverdue;
				 
				 resultString.add(timeFeature);                           // 返回用户所有银行下的时间属性
				 
				 writer.write(timeFeature+"\n");
			   }
			writer.flush();
		}		
		writer.close();	
		return resultString;
	}
	 public static ArrayList<billDetail> cleanRepeatBill(ArrayList<billDetail> billList)
	 {
		 return null;
	 }
	//  统计每张银行下的信用卡记录时，需要清洗去重、时间戳的补齐，离群点的处理
	public static ArrayList<String>  GBPerBank(String billFile,String billPerBankFile)throws IOException
	{
	   // 每个用户对应的银行记录清单	
       HashMap<String,ArrayList<billDetail>>  billDetailList = new  HashMap<String,ArrayList<billDetail>> ();     	
       ArrayList<String> resultString = new ArrayList<String>();
       
		File file=new File(billFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
	   
		File file2=new File(billPerBankFile);                                                                 
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
		}		
		reader.close();	
		     
		Iterator iter = billDetailList.entrySet().iterator();               // 遍历有银行记录的用户的交易清单
		while (iter.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			ArrayList<billDetail> val = (ArrayList<billDetail>) entry.getValue();
			HashMap<Integer,ArrayList<billDetail>> billMap = new HashMap<Integer,ArrayList<billDetail>>();    // 分银行统计属性							
			
			Iterator<billDetail> it = val.iterator(); 
			while(it.hasNext()) 
	       {	    	                                                    
				billDetail tempDetail = it.next();  	    	
				ArrayList<billDetail> billListOfEachBank;                                      // 一个银行的信用账单记录
				if((billListOfEachBank = billMap.get(tempDetail.bankID)) == null)
					billListOfEachBank = new ArrayList<billDetail>();
	    		billListOfEachBank.add(tempDetail);
	    		billMap.put(tempDetail.bankID, billListOfEachBank);
	       }  
		
		   Iterator iter2 = billMap.entrySet().iterator();			   
		   while (iter2.hasNext()) 
			   {				   	 			   	 
				 Map.Entry entry2 = (Map.Entry) iter2.next();
				 Integer key2 = (Integer) entry2.getKey();				 
				 ArrayList<billDetail> value = (ArrayList<billDetail>)entry2.getValue();
				 					 
				 for(int i=0;i<value.size();i++)                                            // 返回用户所有银行下的时间属性				 
					 writer.write(value.get(i)+"\n");
			   }
			writer.flush();
		}		
		writer.close();	
		return resultString;
	}
	
	//  第一步 : 计算该账户(银行)是否为"僵尸账户"
	public static long deadBank(int bankId,ArrayList<billDetail> billList )             // 判断该银行是否为“僵尸"账户，没有欠款和还款行为
	{
		int blankCount = 0;
		for(int i=0;i<billList.size();i++)
		{
			billDetail curBillDetail  = billList.get(i);
			if(curBillDetail.lastBill == 0 && curBillDetail.lastReturn == 0 && curBillDetail.thisBillRest == 0 &&
			   curBillDetail.consume == 0 && curBillDetail.thisBill == 0 && curBillDetail.adjustMoney == 0 &&
			   curBillDetail.cycleInterest == 0 && curBillDetail.borrowMoney == 0 && curBillDetail.useableMoney == 0)
				 blankCount++;
		}		
		if(blankCount == billList.size())						
			return 0;							
	   else
		    return 1;                                                                     // 僵尸账户标志为0，活跃账户标志为1
	}
	
	// 第二步 ： 计算账户(银行)下是否有多张信用卡
	public static long cardsPerBankFlags(int bankId,ArrayList<billDetail> billList)      // 用户在一家银行下有多少张信用卡，分别对每张卡的记录进行计算	
	{
		if(billList.size() == 1)
			return 0;                                                                    // 只有一条信用账单记录，只有一张卡
		
		for(int i=0;i<billList.size()-1;i++) 
		{
			billDetail preDetail  = billList.get(i);
			billDetail curDetail  = billList.get(i+1);
			// 相邻还款间隔应该大于 >= 28,因此一个时间戳附近多笔账单，则视为一行多卡的情况	
			if( curDetail.time != 0 && preDetail.time != 0 && (curDetail.time - preDetail.time)  < 26)	// 时间间隔有误差 27可能为结账周期 						
					return 1;				
		}		
		return 0;
	}
	
	// 第三步 ： 计算该账户(银行)下的信用卡数，恶意申请信用的用户
	public static long cardsPerBankCounts(int bankId,ArrayList<billDetail> billList)    // 计算该用户在该银行下有几张卡???
	{
		int cardsCount = 0;
		
		// 粗糙的方案：检查用户在同一个人时间点收到几笔来自该银行的不同金额的账单
		if( cardsPerBankFlags(bankId,billList) == 0)
			return 1;                                                                   // 只有一张卡，相邻账单间隔 >=28	
		int i = 0;
		for(;i<billList.size()-1;i++)
			{				
				billDetail preDetail = billList.get(i);
				if(preDetail.time != 0)                                                 // 已知账单时间戳不为0
				{
					for(int j=i+1;j<billList.size();j++)
					{
						billDetail curDetail  = billList.get(j);
						if( curDetail.time != 0 && (curDetail.time - preDetail.time) >= 28)
							return (j-i);                                                     // 最远的账单距离, 返回卡的数量
					}
				}					
			}
		if(i == billList.size()-1)
			return 1;
		return 1;
	}	
    // 第四步： 获取该银行下用户的账单起始点和终点，目前存在的问题是：存在一行多卡，每一张信用卡的账单如何梳理出来， 中间还夹杂很多噪声！
	public static ArrayList<Long> timeStamp(int bankId,ArrayList<billDetail> billList)  
	{		
		 Long startTime = 0L;                                                // 填充一张信用卡的用户最早记录时间		 
		 int  knownIndex = 0;
		// Long userID = Long.valueOf(billList.get(0).userID);                // 用户ID编号
		 
		 long cardsPerBank = cardsPerBankCounts(bankId,billList);
	     if(cardsPerBank == 1)
	     {
	    	 for(int i=0;i<billList.size();i++)                              // 找到该用户已知的时间点	    
	    	 {
	    		 if(billList.get(i).time != 0)
	    		 {
	    			 startTime = billList.get(i).time;
	    			 knownIndex = i;
	    			 break;
	    		 }
	    	 }
	     }
	     
	     // 银行卡时间往前递推
	     if( cardsPerBank == 1 && knownIndex != billList.size() && knownIndex != 0 )  // 一张信用卡的账户账单起始时间确定
	     {
	    	 for(int i=0;i<knownIndex;i++)	     	     
	    		 billList.get(i).time = startTime -(knownIndex-i)*30;
	     }
	     	   
		 ArrayList<Long> timeStampList = new  ArrayList<Long>();                       
		 ArrayList<Long> timeSeries = new  ArrayList<Long> ();
		 
		 for(int i=0;i<billList.size();i++)		 
			 timeSeries.add(billList.get(i).time);
		 Collections.sort(timeSeries);                                                  // 时间戳排序
		 
		// for(int i=0;i<timeSeries.size();i++)
			// System.out.println(timeSeries.get(i));
			
		 /// BUG!!!
		 if(timeSeries.get((timeSeries.size()-1)) == 0)                                 // 时间戳全部为0,
			 {			 				 	 
			 	timeStampList.add(-1L);                                                 // 没有时间戳历史的用户用全局的来代替
			 	timeStampList.add(-timeSeries.size()*30L);			 	
			 }
		 else
			 {				
			    int indexNotZero = 0;
			 	for(;indexNotZero<timeSeries.size();indexNotZero++)                   // 从最早的一笔确知账单时间倒退，找到银行账户的最早起点
			 		{			 			
			 			if(timeSeries.get(indexNotZero) != 0L)			 				
			 					break;			 				
			 		}			 	
			 				 	
			    timeStampList.add(timeSeries.get(indexNotZero) - indexNotZero * 30L);
			 	timeStampList.add(timeSeries.get(timeSeries.size()-1));			 	
			 }
		 
		 Long timeDiffer = Math.abs(timeStampList.get(1) - timeStampList.get(0));
		 timeStampList.add(timeDiffer);                                                // 时间差
		 
	    return timeStampList;	  
	}		
	// 第四步 ： 获取用户在该银行下最长的违约时间
	public static long getPerBank(int bankId,ArrayList<billDetail> billList ) 
	{		 
		if(billList.size() <= 1)
			return -1;                                                                     // 只有一条记录，无法知道逾期情况
						
		ArrayList<Long> timeSeries =  new  ArrayList<Long> ();
		ArrayList<Long> timeList   =  new ArrayList<Long>();                               // 账单时期
		ArrayList<Long> timeDiffer =  new ArrayList<Long>();
						
		for(int i=0;i<billList.size()-1;i++)                                               // 考察相邻两期记录
		{
			billDetail preDetail  = billList.get(i);
			billDetail curDetail  = billList.get(i+1);                              	   // 前一笔账单和当前账单
			
			if(curDetail.lastReturn != 0 && curDetail.lastBill != 0 && curDetail.lastReturn < preDetail.curLeastReturn)      // 上一期逾期，则把上期时间戳作为逾期起点		
				timeSeries.add(preDetail.time);									
			else 			
				timeSeries.add(-1L);					                                  // 正常还款期数										
		}
		timeSeries.add(-1L);                                                                   
									
		for(int i=0;i<timeSeries.size();i++)
		{
			long timeNow = timeSeries.get(i);			
			if(timeNow != -1)			
				timeList.add(timeNow);
			else
			{
				int index = 0;
				for(;index<timeList.size();index++)
					if(timeList.get(index)!= 0)                                            // 最早的时间戳
						break;
				if(index == timeList.size())                       
					timeDiffer.add(timeList.size()*30L);
				else
					timeDiffer.add( timeList.get(timeList.size()-1) - timeList.get(index));
				timeList.clear();					
			}
		}				
		Collections.sort(timeDiffer);
		return timeDiffer.get(timeDiffer.size()-1);						
	}	
	// 第六步 ：用户每张卡连续还款小于最低还款的最短、最长时间跨度
	public static long getCardsOnceTime(int bankId,ArrayList<billDetail> billList)
	{	
		if( cardsPerBankFlags(bankId,billList) == 0)                                 // 用户只有一张卡
					return 1;  
		return 0;     	
	}
	// 第七步：用户还款 >= 欠款的最长和最短时间
	public static ArrayList<Long> getMaxMinInterval(int bankId,ArrayList<billDetail> billList)
	{
		 ArrayList<Long>  interval = new  ArrayList<Long>();
		if(deadBank(bankId,billList) == 0)                                           // 僵尸账户没有欠款还款时间
			{
				interval.add(0L);
				interval.add(0L);
				return interval;
			}
		
		float totalBacking = 0f;
		float totalOwing = 0f;		
		for(int i=0;i<billList.size();i++)
		{
			
			
		}
		return null;
			
	}
	 
	
}
