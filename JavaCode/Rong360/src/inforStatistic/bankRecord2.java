package inforStatistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class bankFeature2
{
	
	public  int   salaryFlag = 0;                            // 有无工资进项
	
	//public  float salarySum = 0f;                            // 工资收入总数
	//public  int   salaryCount = 0;                           // 工资笔数
	//public  float salaryAvg = 0f;                            // 平均工资收入
	
	public  int   inputCount = 0;
	public  int   outputCount = 0;                          // 收入与支出笔数
	public  float ioRatio = 0f;                             // 支出与收入笔数比例
	
	public float  inputAvg = 0f;
	public float  outputAvg = 0f;                          // 平均收入和支出
	public float  ioAvg = 0f;                              // 平均收入与支出之差
	
	/*public long   commerceInterval = 0L;                 // 用户交易平均间隔
	public long   inputInterval = 0L;                      // 平均收入、支出时间间隔，以及之差
	public long   outputInterval = 0L;
	public long   ioIntervalDiffer = 0L;
	public int    ioFrequency = 0;                         // 收入频繁还是支出频繁? 
	*/
	
	public  float [] asset;                                // 总收入、总支出、收入-支出 、最大单笔支出
	public  float [] extra;                                // 额外收入: 总额外收入、笔数、平均数、最高额外收入、额外收入占总收入的比例
		  
	static int length = 12 ;
	static int assetLength = 4;
	static int extraLength = 4;
	
	public bankFeature2()
	{		  
	  asset = new float[assetLength];
	  for(int i=0;i<asset.length;i++)
		  asset[i] = 0;  
	  
	  extra = new float[extraLength];
	  for(int i=0;i<extra.length;i++)
		  extra[i] = 0; 	  	 				
	}
	public  String toString()
	{
		String feature  = new String();
		feature += salaryFlag+",";
		//+salarySum+","+salaryCount+","+salaryAvg+",";
		//feature += inputCount+","+outputCount+","+ioRatio+",";
		
		feature += inputAvg+","+outputAvg+","+ioAvg+",";
		
		//feature += commerceInterval+","+inputInterval+","+outputInterval+","+ioIntervalDiffer+","+ioFrequency+",";
		
		for(int i=0;i<asset.length;i++)	 feature += asset[i]+",";
		for(int i=0;i<extra.length;i++)	 feature += extra[i]+",";		
		return feature;
	}		
	public static String combineFeatureTitle()
	{
		String feature  = new String();
			
		feature += "salaryFlag," //salarySum,salaryCount,salaryAvg,"
				//+ "inputCount,outputCount,ioRatio,"
				+ "inputAvg,outputAvg,ioAvg,";
				//+ "commerceInterval,inputInterval,outputInterval,ioIntervalDiffer,ioFrequency,";
		for(int i=0;i<4;i++)	 feature += "asset"+i+",";
		for(int i=0;i<4;i++)	 feature += "extra"+i+",";
		
		return feature;
	}
	public static String featureTitle()
	{
		String feature = "userID"+","+ combineFeatureTitle();
		return feature;
	}
	public static String defaultMissing(String replaceCode)                   // 缺失值填充
	{
		String missing =  new String();
		for(int i=0;i<length;i++)
			missing += replaceCode+",";		
		return missing;
	}
	
}
public class bankRecord2
{
  public static HashMap<Long,bankFeature2> record(String bankDetailFile,String bankFeatureFile) throws IOException     //获取银行流水记录特征
	{
		HashMap<String,ArrayList<bankDetail>>  bankDetail = new  HashMap<String,ArrayList<bankDetail>> ();    //每个用户对应的银行记录清单
		
		HashMap<Long,bankFeature2> bankFeatureList = new  HashMap<Long,bankFeature2>();                         //银行流水记录特征清单

	    File file2=new File(bankFeatureFile);                                                                 //写入 21维 的银行记录特征 
		FileWriter fw = null;
        BufferedWriter writer = null;
        fw = new FileWriter(file2);
        writer = new BufferedWriter(fw);
				
		File file=new File(bankDetailFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
		
		while((tempString = reader.readLine())!=null)
		{
			 String [] temp = tempString.split(",");
			 ArrayList<bankDetail> tempList;
			 
			 if((tempList = bankDetail.get(temp[0]))==null)
				{
				    tempList = new ArrayList<bankDetail>();                                                   //新用户增加清单
				}
			 tempList.add(new bankDetail(temp[0],Long.valueOf(temp[1]),Integer.valueOf(temp[2]),
				    		                                           Float.valueOf(temp[3]),Integer.valueOf(temp[4])));
			 bankDetail.put(temp[0], tempList);                                                              //获取老用户的交易清单
		}
		reader.close();			
		writer.write(bankFeature2.featureTitle()+"\n");
		
		Iterator iter = bankDetail.entrySet().iterator();                                                  //遍历有银行记录的用户的交易清单
		while (iter.hasNext()) 
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			ArrayList<bankDetail> val = (ArrayList<bankDetail>) entry.getValue();
			
			Collections.sort(val);
			bankFeature2  feature = getFeatureList(val);                                                      //银行记录提取的特征
			bankFeatureList.put(Long.valueOf(key), feature);
			writer.write(key+","+feature.toString()+"\n");                            
		}
		writer.flush();
		writer.close();	
		return bankFeatureList;
	  }
	
	public static bankFeature2 getFeatureList(ArrayList<bankDetail> detailList)                             // 获取用户的银行记录特征
	{						                                                                   
		bankFeature2 result =  new bankFeature2();
		ArrayList<Float> extraList  = new ArrayList<Float> (); 
		
		ArrayList<Long> commerceTimeList = new ArrayList<Long>();                                        // 用户平均交易时间间隔
		ArrayList<Long> inputTimeList = new ArrayList<Long> ();		                                     // 收入和支出的时间间隔，看看用户收支频率
		ArrayList<Long> outputTimeList = new ArrayList<Long> ();	
		
		// 工资标志属性
		for( bankDetail temp : detailList)
		{
			if(temp.salaryLabel == 1)
			{
				result.salaryFlag = 1;
				break;
			}
		}    
		
		Iterator<bankDetail> it = detailList.iterator();												 // 遍历用户交易清单
		while(it.hasNext())
		{
			bankDetail temp_bankDetail =it.next();
			commerceTimeList.add(temp_bankDetail.time);                                                  // 交易时间间隔
			
			if(temp_bankDetail.commerceType == 1) 
			{
				 	result.asset[0] += temp_bankDetail.commerceSum;
				 	result.outputCount ++;                                                               // 支出笔数
				 	outputTimeList.add(temp_bankDetail.time);                                            // 支出交易时间序列
			}
			else if(temp_bankDetail.commerceType == 0)  											     // 0代表收入
				{	
					result.asset[1] += temp_bankDetail.commerceSum;                                      // 总收入
					result.inputCount ++;                                                                // 收入笔数
					inputTimeList.add(temp_bankDetail.time);                                             // 收入时间序列
					
					if(temp_bankDetail.salaryLabel == 1)  
					{	
						;//result.salarySum += temp_bankDetail.commerceSum;                                 // 工资总收入 ，标记1为工资收入					   
					    //result.salaryCount ++;
					}					
				   else if( temp_bankDetail.salaryLabel == 0)                                            // 额外收入，标记0为额外收入  
					{
					    result.extra[0] += temp_bankDetail.commerceSum;                                                             // 工资总收入
						result.extra[1] ++;                                                       
						extraList.add(temp_bankDetail.commerceSum);
					}
				}			 										
		}
	
		//result.salaryAvg = result.salarySum == 0 ? 0 : result.salarySum / result.salaryCount;        // 平均工资水平
		
		result.outputAvg = result.asset[0] == 0 ? 0 : result.asset[0] / result.outputCount;
		result.inputAvg = result.asset[1] == 0 ? 0 : result.asset[1] / result.inputCount;
		result.ioRatio = (float)result.inputCount / result.outputCount;
		result.ioAvg  = result.inputAvg - result.outputAvg ;
		
		/*
		result.commerceInterval = averageTime(commerceTimeList);                                    // 平均交易时间间隔		 
		result.inputInterval =  averageTime(inputTimeList);
		result.outputInterval = averageTime(outputTimeList);
		result.ioIntervalDiffer = result.inputInterval - result.outputInterval;
		result.ioFrequency = result.ioIntervalDiffer <= 0 ? 1 : 0;                                  // 收入间隔短为1，支出间隔短为0
		*/
		
		result.asset[2] = result.asset[1] - result.asset[0];                                        // 最大支出
		result.asset[3] = result.asset[0] == 0 ? 10e5f : result.asset[1] / result.asset[0];                                        // 收支比例
					
		result.extra[2] = average(extraList);                                                       // 额外输入的数值			
		result.extra[3] = result.asset[0] == 0 ? 0 : sum(extraList) / result.asset[0];              // 额外收入占总收入的比例
		
		return result;
	}
	public static float salaryDesc(ArrayList<Float> money)                                             // 工资以及额外收入是否下降
	{		
		if(money.size()<=1)
			return 0;
	
		float  recentMoney = money.get(money.size()-1);				
		Collections.sort(money);		
		
		if(recentMoney - money.get(money.size()/2) >= 0)                                            // 当前工资与工资中位数之差			
			return 1;
		else
			return -1;			
	}
	public static float max(ArrayList<Float> money)
	{
		if(money.size() == 0) 
			return 0;
		
		float max = money.get(0);
		for(int i=0;i<money.size()-1;i++)
		{
			if(max < money.get(i))
				max = money.get(i);
		}
		return max;
	}
	public static float sum(ArrayList<Float> money)  
	{
		float sum = 0;
		for(int i=0;i<money.size();i++)
			sum += money.get(i);
		return sum;
	}
	// 平均时间间隔计算
	public static long  averageTime(ArrayList<Long> timeList)
	{
		if(timeList.size() <= 1)
			return -1;                                                                 // 至少两笔交易才能计算时间差
		
		ArrayList<Long> timeDiffer = new ArrayList<Long>();
		for(int i=0;i<timeList.size()-1;i++)
		{
			long preTime = timeList.get(i);
			long curTime = timeList.get(i+1);
			if(preTime != 0 && curTime != 0)
				timeDiffer.add(curTime - preTime);                                       // 非零时间戳之差
		}
		long  intervalSum  = 0L;
		if(timeDiffer.size() == 0)
			return  -1;
		for(int i=0;i<timeDiffer.size();i++)		
			intervalSum += timeDiffer.get(i);	
		return intervalSum / timeDiffer.size();
	}
	public static float average(ArrayList<Float> money)                                              // 均值
	{
		if(money.size() == 0) 
			return 0;
		float sum = 0;
		for(int i=0;i<money.size();i++)
			sum += money.get(i);
		return sum/money.size();
	}
	public static float delta(ArrayList<Float> money)                                                //方差
	{
		if(money.size() == 0)
			return 0;
		float avg = average(money);
		float sum = 0;
		for(int i=0;i<money.size();i++)
			sum += (money.get(i)-avg)*(money.get(i)-avg);
		return (float) Math.sqrt(sum/money.size());
	}
	
	public static float medium(ArrayList<Float> money)                                             //中位数         
	{ 
		if(money.size() == 0)
			return -1;
		Collections.sort(money);
		
		if(money.size() %2 == 1) 
			return money.get(money.size()/2);		
		
		else
			return (money.get(money.size()/2)+money.get(money.size()/2-1))/2;
	}
	
	public static float netInput(ArrayList<Float> input,ArrayList<Float> output)                  //净收入
	{
		float sumInput = 0f,sumOutput = 0f;
		for(int i=0;i<input.size();i++)
			sumInput += input.get(i);
		for(int i=0;i<output.size();i++)
			sumOutput += output.get(i);
		return (sumInput - sumOutput);
	}
	
	
	
/*	public static HashMap<Long,Integer>  label(String labelFile)throws IOException
	{
		HashMap<Long,Integer> userLabel = new HashMap<Long,Integer>();
		
		File file=new File(labelFile);			
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		
		String tempString = null;	 	
		String regex = "\\s+";
		
		while( (tempString = reader.readLine())!=null) 
		   {			      
			   String str = tempString.replaceAll(regex, " ");			  
			   String [] temp = str.split(",");  
			   userLabel.put(Long.valueOf(temp[0]), Integer.valueOf(temp[1]));
		   }
		return userLabel;		
	}
	public static HashMap<Long,Integer>  label0(String labelFile)throws IOException
	{
		HashMap<Long,Integer> userLabel = new HashMap<Long,Integer>();
		
		File file=new File(labelFile);			
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		
		String tempString = null;	 	
		String regex = "\\s+";
		
		while( (tempString = reader.readLine())!=null) 
		   {			      
			   String str = tempString.replaceAll(regex, " ");			  
			   String [] temp = str.split(",");  
			   if(Integer.valueOf(temp[1]) == 0)
				   userLabel.put(Long.valueOf(temp[0]), Integer.valueOf(temp[1]));
		   }
		return userLabel;		
	}
	public static HashMap<Long,Integer>  label1(String labelFile)throws IOException
	{
		HashMap<Long,Integer> userLabel = new HashMap<Long,Integer>();
		
		File file=new File(labelFile);			
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		
		String tempString = null;	 	
		String regex = "\\s+";
		
		while( (tempString = reader.readLine())!=null) 
		   {			      
			   String str = tempString.replaceAll(regex, " ");			  
			   String [] temp = str.split(",");  
			   if(Integer.valueOf(temp[1]) == 1)
				   userLabel.put(Long.valueOf(temp[0]), Integer.valueOf(temp[1]));
		   }
		return userLabel;		
	}
	public static void labelRecord(HashMap<Long,Integer> userLabel,String originFile,String labeledFile)throws IOException
	{
		File file=new File(originFile);	                                                                   
		BufferedReader reader = null;         
		reader = new BufferedReader(new FileReader(file));  
		String tempString = null;
		
		File file2=new File(labeledFile);                                                        
		FileWriter fw = null;
        BufferedWriter writer = null;
        fw = new FileWriter(file2);
        writer = new BufferedWriter(fw);
        
        while((tempString = reader.readLine())!=null) 
        {       			  
			 String [] temp = tempString.split(",");
			 if(userLabel.get(Long.valueOf(temp[0])) != null)
				 {
				 	tempString += ","+userLabel.get(Long.valueOf(temp[0])); 
				 	writer.write(tempString+"\n");
				 }
			
        }
        writer.flush();  	
	}
	*/
}
