package inforStatistic;
/*
 * CleanData类 作用： 清洗数据，去掉训练集和测试集中的冗余数据！
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Data_clean 
{
	//public static long repeatBills = 0L;
	//public static long relativeCount = 0L;
	public static HashSet<String> goodRepeat  = new HashSet<String>();
	
	public static void main(String [] argv)throws IOException
	{
		String dataPath = "C:/Users/Liuxiang/Desktop/Credit/Data/train/";
		String testPath = "C:/Users/Liuxiang/Desktop/Credit/Data/test/";
		
		String NewdataPath = "C:/Users/Liuxiang/Desktop/Credit/Data/train/LabeledTrain/";   
		
		// 训练集:账单记录、浏览行为记录、银行流水记录去除重复数据
		// deleteRepeatRecord(dataPath+"bill_detail_train.txt",dataPath+"clean_bill_detail_train.txt");
		// deleteRepeatRecord(dataPath+"browse_history_train.txt",dataPath+"clean_browse_history_train.txt");
		// deleteRepeatRecord(dataPath+"bank_detail_train.txt",dataPath+"clean_bank_detail_train.txt");
		
		
		// 测试集： 账单记录、浏览行为记录、流水记录去除重复数据
		// deleteRepeatRecord(testPath+"bill_detail_test.txt",testPath+"clean_bill_detail_test.txt");	
		// deleteRepeatRecord(testPath+"browse_history_test.txt",testPath+"clean_browse_history_test.txt");
		// deleteRepeatRecord(testPath+"bank_detail_test.txt", testPath+"clean_bank_detail_test.txt");
		
		// 合并同一笔工资收入、非工资收入和支出记录
		// combineBankRecord(dataPath+"bank_detail_train.txt",dataPath+"combine_bank_detail_train.txt");
		// combineBankRecord(testPath+"bank_detail_test.txt",testPath+"combine_bank_detail_test.txt");
		
		// 按照每个银行罗列用户的清单
		// billPerBank(dataPath+"clean_bill_detail_train.txt",dataPath+"perBillBank.txt");
		// billPerBank(testPath+"clean_bill_detail_test.txt",testPath+"perBillBank.txt");
		  
	    // billPerBank(NewdataPath+"Good_New_bill_detail.txt", NewdataPath+"Good_perBillBank1.txt");		     
	     //billPerBank(NewdataPath+"Bad_New_bill_detail.txt", NewdataPath+"Bad_perBillBank1.txt");
	     
	    // billPerBank(dataPath+"clean_bill_detail_train.txt", NewdataPath+"cleanAgain_Train.txt");
	    // billPerBank(testPath+"clean_bill_detail_test.txt", NewdataPath+"cleanAgain_Test.txt");
	}
	
	public static void  billPerBank(String billFile,String billPerBankFile)throws IOException
	{
        HashMap<String,ArrayList<billDetail>>  billDetailList = new  HashMap<String,ArrayList<billDetail>> ();      //每个用户对应的银行记录清单
				       
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
		
		Iterator iter = billDetailList.entrySet().iterator();                 // 遍历有银行记录的用户的交易清单
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
					ArrayList<billDetail> billListOfEachBank;                                                   // 一个银行的信用账单记录
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
				 	ArrayList<billDetail> clean_value = cleanRepeatBill(BillResort.cleanAndfill(value));
				 	//BillResort.cleanAndfill(value);
				 			  // 二次清洗账单数据后的账单时间戳填充  								 	
				 	for(int i=0;i<clean_value.size();i++)					 
				 		writer.write(clean_value.get(i).toString()+"\n");								 
			   }
			writer.flush();
		}		
		writer.close();		
	}
	

	//  函数功能 ：去除冗余账单数据
	public static ArrayList<billDetail> cleanRepeatBill(ArrayList<billDetail> billList)
	{
		ArrayList<billDetail> resultBillList = new ArrayList<billDetail>();
		HashSet<String> billSet = new HashSet<String>();
		for(int i=0;i<billList.size();i++)
		{
			if( ! billSet.contains(billList.get(i).toString()))
			{
				billSet.add(billList.get(i).toString());
				resultBillList.add(billList.get(i));
			}							
		}
		return resultBillList;
	}
	// 函数功能 ：去掉Zombie账户
	public static int zombieBank(int bankId,ArrayList<billDetail> billList)
	{
		int blankCount = 0;
		for(int i=0;i<billList.size();i++)                                 // 判断是否为Zombie账户，从来没有过欠款、还款和消费行为
		{
			billDetail curBillDetail  = billList.get(i);
			if(curBillDetail.lastBill == 0 && curBillDetail.lastReturn == 0 && curBillDetail.thisBillRest == 0 &&
			   curBillDetail.consume == 0 && curBillDetail.thisBill == 0 && curBillDetail.adjustMoney ==0 &&
			   curBillDetail.cycleInterest == 0 && curBillDetail.borrowMoney == 0 && curBillDetail.useableMoney == 0)
				 blankCount++;
		}
		if(blankCount == billList.size())						   
			return 0;
		else
			return 1;
		
	}
	// 函数功能 ： 合并同一笔工资收入、非工资收入和支出记录
	public static void combineBankRecord(String bankRecordFile,String combinebankRecordFile)throws IOException
	{
		//HashSet<String>   Set_UserID_Time_BankID = new HashSet<String>();
		//ArrayList<String> List_UserID_Time_BankID = new ArrayList<String>();
		
		File file = new File(bankRecordFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
		
		File file2 = new File(combinebankRecordFile);                                                        
		FileWriter fw = null;
        BufferedWriter writer = null;
        fw = new FileWriter(file2);
        writer = new BufferedWriter(fw);
        
        ArrayList<String> sameTimeComs = new ArrayList<String>();  
        String curKey = new String();
        String saveKey = "init"; 
        while( (tempString = reader.readLine()) !=null) 
        {  
        	//String  outputCombine = null;
        	//String  inputCombine = null;
        	//String  otherInoutCombine = null;                // 合并同一时间点下的同种类型的交易 : 支出、收入和额外收入        	
        	float  outputSum = 0f;
        	float  salaryInputSum = 0f;        	
        	float  elseInputSum = 0f;                          // 支出、收入和额外收入
        	       	
        	String [] temp = tempString.split(",");
        	curKey = temp[0] + "," + temp[1];                  // 用户ID+时间做键         	        	       	
        	       	
        	if(Long.valueOf(temp[1]) == 0L)
        		writer.write(tempString + "\n");               // 时间戳未知的记录保留，不做处理        	
        	else
        	{    
        		if(curKey.compareTo(saveKey) == 0 || (saveKey == "init"))
        		{
        			sameTimeComs.add(tempString); 
        			saveKey = curKey ;
        		}       		       		
        		else
        		{
        			String [] temp2  = new String [5];
        			for(int index=0;index<sameTimeComs.size(); index++)
        			{
        				String record = sameTimeComs.get(index);
        				temp2 = record.split(",");
				   				    
        				if( Integer.valueOf(temp2[2]) == 1 && Integer.valueOf(temp2[4]) == 0 )       // 支出
        					outputSum += Float.valueOf(temp2[3]);
        				else if( Integer.valueOf(temp2[2]) == 0 && Integer.valueOf(temp2[4]) == 1 )  // 工资收入
        					salaryInputSum += Float.valueOf(temp2[3]);
        				else if( Integer.valueOf(temp2[2]) == 0 && Integer.valueOf(temp2[4]) == 0)   // 其他收入
        					elseInputSum += Float.valueOf(temp2[3]);
        			}				
        			if( outputSum != 0f)
        				writer.write(temp2[0]+","+temp2[1]+",1,"+outputSum+",0"+"\n");
        			if( salaryInputSum != 0f)
        				writer.write(temp2[0]+","+temp2[1]+",0,"+salaryInputSum+",1"+"\n");
        			if( elseInputSum != 0f)
        				writer.write(temp2[0]+","+temp2[1]+",0,"+elseInputSum+",0"+"\n");
				
        			sameTimeComs.clear();
        			sameTimeComs.add(tempString);                                                  // 新的记录 ：该用户下一时刻点或者新用户的记录
        		}
        		saveKey = tempString.split(",")[0]+","+tempString.split(",")[1];
        	}
			 writer.flush(); 			 			
        }
        reader.close();
		writer.close();
	}
    // 函数功能 ： 清洗重复数据，目前只有信用卡账单数据需要清洗，银行流水记录和浏览行为不需要清洗！
	public static void deleteRepeatRecord(String billTrainFile,String cleanBillFile)throws IOException
	{
		HashSet<String>   uniqueRecordList = new HashSet<String>();
				
		File file = new File(billTrainFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
		
		File file2 = new File(cleanBillFile);                                                        
		FileWriter fw = null;
        BufferedWriter writer = null;
        fw = new FileWriter(file2);
        writer = new BufferedWriter(fw);
        
        while((tempString = reader.readLine())!=null) 
        {       			  
			 String [] temp = tempString.split(",");
			 String key = new String();
			 for(int i=0;i<temp.length;i++)
			 {
			 // key  = new billDetail(temp[0],Long.valueOf(temp[1]),Integer.valueOf(temp[2]),Float.valueOf(temp[3]),Float.valueOf(temp[4]),
					//	 Float.valueOf(temp[5]),Float.valueOf(temp[6]),Float.valueOf(temp[7]),Integer.valueOf(temp[8]),Float.valueOf(temp[9]), 
					//	 Float.valueOf(temp[10]),Float.valueOf(temp[11]),Float.valueOf(temp[12]),Float.valueOf(temp[13]),Integer.valueOf(temp[14])).toString();
				 
				key += temp[i]+",";
				//System.out.println(tempString+"\n");				
			 } 
			
        	  // key = tempString;   // 一条完整记录做键                           // 去重不不彻底??? why???
				
			 if(! uniqueRecordList.contains(key))          // 时间未知的数据保留，不好去冗余
			 {
				 uniqueRecordList.add(key);				                  
				 writer.write(tempString+"\n");            // 保持原来数据的相对时间顺序
			 }					 
			 writer.flush();
        }              
        reader.close();
        writer.close();
	}
	
}
