package inforStatistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FeatureGet 
{
	public static void main(String [] argv) throws IOException
	{
		
		String trainPath = "D:/SLaughter_code/RawData/";
		String testPath = "D:/SLaughter_code/RawData/";
		
		String  CharacterFilePath = "D:/SLaughter_code/CharacterFile/";
		  
		// 合并同一笔工资收入、非工资收入和支出记录
		Data_clean.combineBankRecord(trainPath+"bank_detail_train.txt",CharacterFilePath+"combine_bank_detail_train.txt");
		Data_clean.combineBankRecord(testPath+"bank_detail_test.txt",CharacterFilePath+"combine_bank_detail_test.txt");
		
		// 训练集 && 测试集:账单记录、浏览行为记录、银行流水记录去除重复数据
		Data_clean.deleteRepeatRecord(trainPath+"bill_detail_train.txt",CharacterFilePath+"clean_bill_detail_train.txt");		
		Data_clean.deleteRepeatRecord(testPath+"bill_detail_test.txt",CharacterFilePath+"clean_bill_detail_test.txt");	
	
		
		String tempFilePath = "D:/SLaughter_code/RawData/GenerateFile/";
		
		
		
		String   TrainResultPath = "D:/SLaughter_code/CharacterFile/";
		String   GenerateFilePath ="D:/SLaughter_code/GenerateFile/";
		
		File file2=new File(TrainResultPath+"onlineTrain.csv");                                     // 写入训练集合并特征 
		FileWriter fw =  new FileWriter(file2);
	    BufferedWriter  writer = new BufferedWriter(fw);
	    
	    int  TopKCount = 30;
	    int  timeFeatureCount = 11;
	    String missingCode = "NA";    // -1
	    	    
		HashMap<Long,bankFeature2> bankFeatureList = bankRecord2.record(CharacterFilePath+"combine_bank_detail_train.txt", 
				                                                  CharacterFilePath+"bankFeature.txt");				
		
		//Data_resort.sortBrowseByTime(dataPath+"browse_history_train.txt",dataPath+"sorted_browse_history_train.txt");//历史浏览行为需要按照时间进行排序
	    HashMap<Long,browseFeature>  browseFeatureList = browseRecord.record(trainPath+"browse_history_train.txt", 
	    		                                                             CharacterFilePath+"browserFeature.txt",
	    		                                                             GenerateFilePath+"sortDistribution.csv",
	    		                                                             GenerateFilePath+"loan_time_train.csv",TopKCount);
	      
	   // 信用卡账单记录重复数据需要进行清洗,使用去除冗余后的数据！！
		HashMap<Long,billFeature> billFeatureList = billRecord.record(CharacterFilePath+"clean_bill_detail_train.txt",
				                                                      CharacterFilePath+"billFeature.txt",
																	  GenerateFilePath+"loan_time_train.csv");		
		
		HashMap<Long,String> timeFeatureList  = longestOverdue.timeFeature(CharacterFilePath+"clean_bill_detail_train.txt", 
				                                                           CharacterFilePath+"timeFeature.txt");
		
		String timeTitle = new String();
		String missingTimeFeature = new String();
		for(int i=0;i<timeFeatureCount;i++)
			 {
				timeTitle += "time"+i+",";
				missingTimeFeature += missingCode+",";
			 }	
		
		HashMap<Long,userInfo>    userFeatureList = FeatureGet.userRecord(trainPath+"user_info_train.txt");
		HashMap<Long,String>      overdueList =  FeatureGet.overdueRecord(GenerateFilePath+"overdue_train.csv");
					
	//	String inforIntact = "hasBankRecord"+","+"hasBrowseRecord"+","+"hasBillRecord"+","+"overdueFlag\n";;     //信息的完整度
		String inforIntact = "NoBank,"+"YesBank,"+"NoBrowse,"+"YesBrowse,"+"NoBill,"+"YesBill,"+"combineCode,"+"label\n";     //信息的完整度
		String title = userInfo.combineFeatureTitle()+bankFeature2.combineFeatureTitle()+
				               browseFeature.combineFetureTitle(TopKCount)+
				               new billFeature().combineFetureTitle()+timeTitle+inforIntact;
		writer.write(title);		                                                           
		writer.flush();
		
		Iterator iter = userFeatureList.entrySet().iterator();
		while (iter.hasNext()) 
		   {
			 String combineFeture = new String();
			 String inforMissing = new String();                               //信息缺失程度  yes*3 表示完整的用户记录
			 String  hotcode = new String();
			 
			 Map.Entry entry = (Map.Entry) iter.next();
			 Long key = (Long) entry.getKey();					
			
			 combineFeture += userFeatureList.get(key);
			
			 // 银行流水记录属性
			 if(bankFeatureList.containsKey(key))                             //银行流水记录特征：如果用户有该条记录，则使用统计出的银行流水记录特征;否则使用通用特征
			 {
				 combineFeture += bankFeatureList.get(key);
				 inforMissing += "1,0,";
				 hotcode  += "1";
			 }
			 else
			 {
				// combineFeture += bankFeatureList.get((long)(-1));
				 
				 combineFeture += bankFeature2.defaultMissing(missingCode);
				 inforMissing += "0,1,";
				 hotcode  += "0";
			 }
			 
			 // 浏览行为属性
			 if(browseFeatureList.containsKey(key))                          // 浏览行为记录特征：如果用户有该条记录，则使用统计出的浏览行为特征;否则使用通用特征
			 {
				 combineFeture += browseFeatureList.get(key); 
				 inforMissing += "1,0,";
				 hotcode  += "1";
			 }
			 else
			 {
				 //combineFeture += browseFeatureList.get((long)-2);
				 
				 combineFeture += new browseFeature(TopKCount).defaultMissing(missingCode);
				 inforMissing += "0,1,";
				 hotcode  += "0";
			 }
			 
			 // 用户信用卡账单属性
			 if(billFeatureList.containsKey(key))                            // 信用卡账单记录特征：如果用户有该条记录，则使用统计出的信用卡账单记录特征;否则使用通用特征
				{
				 combineFeture += billFeatureList.get(key);
				 inforMissing += "1,0,";
				 hotcode  += "1,";
				}
			 else
				{
				 //combineFeture += billFeatureList.get((long)-1);           // 
				
				 combineFeture +=  billFeature.defaultMissing(missingCode);
				 inforMissing += "0,1,";
				 hotcode  += "0,";
				}
			 // 用户时间属性
			 if(timeFeatureList.containsKey(key))                           // 时间属性			 
				 combineFeture += timeFeatureList.get(key);
			 else
				 combineFeture += missingTimeFeature;
			 
			 combineFeture += (inforMissing+hotcode+overdueList.get(key));
	
			 writer.write(combineFeture+"\n");
		   }
		writer.flush();
		writer.close();

		//测试集数据的特征,如果测试集上的用户拥有银行流水记录 或者 浏览行为记录 或者 信用卡记录,则使用统计出的特征;否则使用训练集上的用户通用特征!!!
		
		
		File file3 = new File(TrainResultPath+"onlineTest.csv");                                                                       
		FileWriter fw3 =  new FileWriter(file3);
	    BufferedWriter  writer3 = new BufferedWriter(fw3);
		
	  
	    //银行流水记录和浏览行为不进行数据去冗余,但是流水记录需要合并同一笔相同类型的交易???
		HashMap<Long,bankFeature2> testbankFeatureList = bankRecord2.record(CharacterFilePath+"combine_bank_detail_test.txt", 
				                                                            CharacterFilePath+"testbankFeature.txt");
		
	   HashMap<Long,browseFeature> testbrowseFeatureList = browseRecord.record(testPath+"browse_history_test.txt", 
			                                                                   CharacterFilePath+"testbrowseTestFeture.txt",
	    		                                                               GenerateFilePath+"sortDistribution.csv",
	    		                                                               GenerateFilePath+"loan_time_test.csv",
	    		                                                               TopKCount);
	   		                                                                
	   
	    
	    //由于训练集和测试集中含有一定比例的重复数据,因此使用清洗掉重复数据的干净训练集和测试集!!!!   
	    HashMap<Long,billFeature> testbillFeatureList = billRecord.record(CharacterFilePath+"clean_bill_detail_test.txt", 
	    		                                                          CharacterFilePath+"testbillFeature.txt",
															              GenerateFilePath+"loan_time_test.csv");
	    
		HashMap<Long,String> testtimeFeatureList  = longestOverdue.timeFeature(CharacterFilePath+"clean_bill_detail_test.txt",
				                                                               CharacterFilePath+"timeFeature.txt");

		HashMap<Long,userInfo> testuserFeatureList = FeatureGet.userRecord(testPath+"user_info_test.txt");		
		
		String inforIntactTest = "NoBank,"+"YesBank,"+"NoBrowse,"+"YesBrowse,"+"NoBill,"+"YesBill,"+"combineCode\n";     //信息的完整度
		
		String title2 = userInfo.combineFeatureTitle()+bankFeature2.combineFeatureTitle()+browseFeature.combineFetureTitle(TopKCount)+
														new billFeature().combineFetureTitle()+timeTitle+inforIntactTest;
		writer3.write(title2);                                                        
		writer3.flush();
		
		Iterator iter2 = testuserFeatureList.entrySet().iterator();
		while (iter2.hasNext()) 
		   {
			 String testcombineFeture = new String();
			 String testinforMissing = new String();
			 String testhotCode = new String();
			 Map.Entry entry = (Map.Entry) iter2.next();
			 Long key = (Long) entry.getKey();
		
			 testcombineFeture += testuserFeatureList.get(key);
			 
			 if(testbankFeatureList.containsKey(key))               //银行流水记录特征：如果测试集上的用户有该条记录，则使用统计出的银行流水记录特征;否则使用训练集上的通用特征
			 {
				 testcombineFeture += testbankFeatureList.get(key);
				 testinforMissing  += "1,0,";
				 testhotCode += "1";
			 }
			 else
			 {
				// testcombineFeture += bankFeatureList.get((long)(-1));
				 
				 testcombineFeture += bankFeature2.defaultMissing(missingCode);
				 testinforMissing  += "0,1,";
				 testhotCode += "0";
			 }
			 
			 
			 if(testbrowseFeatureList.containsKey(key))
			 {
				 testcombineFeture += testbrowseFeatureList.get(key); 
				 testinforMissing  += "1,0,";
				 testhotCode += "1";
			 }
			 else
			 {
				// testcombineFeture += browseFeatureList.get((long)-2);
				
				 testcombineFeture += new browseFeature(TopKCount).defaultMissing(missingCode);
				 testinforMissing  += "0,1,";
				 testhotCode += "0";
			 }
			 
			 
			 if(testbillFeatureList.containsKey(key))
			 {
				 testcombineFeture += testbillFeatureList.get(key);
				 testinforMissing  += "1,0,";
				 testhotCode += "1,";
			 }				 
			 else
			 {
				 //testcombineFeture += billFeatureList.get((long)-1);
				
				 testcombineFeture += billFeature.defaultMissing(missingCode);
				 testinforMissing  += "0,1,";
				 testhotCode += "0,";
			 }
			 if(testtimeFeatureList.containsKey(key))
					testcombineFeture += testtimeFeatureList.get(key);
				else
					testcombineFeture += missingTimeFeature;
			 
			 testcombineFeture += (testinforMissing+testhotCode);
			  // +list[(int)(Math.random()*2)]);               
			 			                                             
			 
			 writer3.write(testcombineFeture+"\n");
		   }
		writer3.flush();
		writer3.close();	
	}
	
	
	public static HashMap<Long,userInfo> userRecord(String userFile) throws IOException
	{
		 HashMap<Long,userInfo> userFeatureList = new HashMap<Long,userInfo>();		
		 File file=new File(userFile);	                                                                   
		 BufferedReader reader = null;         
		 reader = new BufferedReader(new FileReader(file));  
		 String tempString = null;
		 		
		 while( (tempString = reader.readLine())!=null) 
		   {	
				 String [] temp = tempString.split(",");  
				 userFeatureList.put(Long.valueOf(temp[0]), new userInfo(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5]));					
		   }
		reader.close();
		return userFeatureList;
	}
	public static HashMap<Long,String>  overdueRecord(String overdueFile) throws IOException
	{
		 HashMap<Long,String> overdueList = new HashMap<Long,String>();		
		 File file=new File(overdueFile);	                                                                   
		 BufferedReader reader = null;         
		 reader = new BufferedReader(new FileReader(file));  
		 String tempString = null;
		 
		 String regex = "\\s+";
		 
		 while( (tempString = reader.readLine())!=null) 
		   {	 
			 	String str = tempString.replaceAll(regex, " ");
				String [] temp = str.split(",");  
				
				 String label = null;
				 if(Integer.valueOf(temp[1]) == 0) 
					 label = "0";
				 else 
					 label = "1";
				 overdueList.put(Long.valueOf(temp[0]),label);				
		   }
		reader.close();
		return overdueList;
	}
}
