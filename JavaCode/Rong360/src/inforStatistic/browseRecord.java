package inforStatistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

 class browseFeature                                                                                  
{	
	
	public  int   [] topKDistribution;                           // topK 浏览行为对的分布
	public  float [] browse;         
	
	
	// 全局  总浏览数/浏览次数/平均每次浏览个数   topK总浏览数/每次topK/topK占浏览的比重/与else的比重，其他选项/其他选项平均次数，浏览平均时间间隔/
	static  int browseLength = 9;
	static  int timeLength = 5 ;
	public HashSet<String> browsetimeList;    
	public HashSet<String> topKtimeList;                                        // 浏览的回数	
	public HashSet<String> elsetimeList;

	public HashSet<Long> browseBeforeLoanTimeList = new  HashSet<Long>();           // 放款前浏览行为时间戳列表
	public HashSet<Long> browseAfterLoanTimeList = new  HashSet<Long>();            // 放款后浏览行为时间戳列表
	

	// 第一次浏览行为发生时间、放款审核花费时间、放款前浏览次数、放款之前最近的一次浏览行为发生时间
	public long firstTime = 0L;	
	public long timeBeforeLoan = 0L;
	public long countBeforeLoan = 0L;
	public long timeRecentBeforeLoan = 0L;                                   // 放款之前最近的一次浏览行为发生时间
	public long avgInterval = 0L;                                            // 平均浏览间隔
		
	public browseFeature(int topKCount)                                     // 主行为对上的分布
	{							
		topKDistribution = new  int[topKCount];
		for(int i=0;i<topKCount;i++)
			topKDistribution[i] = 0;
		
		browse = new  float[browseLength];
		for(int i=0;i<browse.length;i++)
			browse[i] = 0;
		
		browsetimeList = new HashSet<String>();
	    topKtimeList = new HashSet<String>();
	    elsetimeList = new HashSet<String>();	   
	}
	public browseFeature(int topKCount,int unknown)
	{
		topKDistribution = new  int[topKCount+2];
		for(int i=0;i<topKCount+2;i++)
			topKDistribution[i] = unknown;
						
		browse = new  float[9];
		for(int i=0;i<browse.length;i++)
			browse[i] = unknown;
		
		browsetimeList = new HashSet<String>();
	    topKtimeList = new HashSet<String>();
	    elsetimeList = new HashSet<String>();	 
	}
	
	public static String fetureTitle(int topKDistributionSize)
	{
		String title = new String();
		title += "UserID"+",";
					
		for(int i=0;i<topKDistributionSize;i++)	
			title += "top"+(i+1)+",";
		
		for(int i=0;i<browseLength;i++)
			title += "browse"+(i+1)+",";
		
		title +=  "firstTime,timeBeforeLoan,countBeforeLoan,timeRecentBeforeLoan,avgInterval,";
		
		return title;
	}
	
	public static String combineFetureTitle(int topKDistributionSize)
	{
		String title = new String();		
					
		for(int i=0;i<topKDistributionSize;i++)	
			title += "top"+(i+1)+",";	
		
		for(int i=0;i<browseLength;i++)
			title += "browse"+(i+1)+",";
		
		title +=  "firstTime,timeBeforeLoan,countBeforeLoan,timeRecentBeforeLoan,avgInterval,";
		
		return title;
	}
	public String toString()
	{
		String title = new String();
		
		for(int i=0;i<topKDistribution.length;i++)	
			title += topKDistribution[i]+",";
		
		for(int i=0;i<9;i++)
			title += browse[i]+",";	
				
		title +=  firstTime+","+timeBeforeLoan+","+countBeforeLoan+","+timeRecentBeforeLoan+","+avgInterval+",";		
		return title;
	}
	public String defaultMissing(String replaceCode)
	{
		String missing = new String();		
		for(int i=0;i<topKDistribution.length;i++)	
			missing += replaceCode+",";
	
		for(int i=0;i<browseLength+timeLength;i++)
			missing += replaceCode+",";	
		return missing;
	}
}

 public class browseRecord
 {
	 public static float AvgInterval(ArrayList<Long> timeArray)                                              
		{
		   Collections.sort(timeArray);
		   HashSet<Long> timeCount = new HashSet<Long>();
		   if(timeArray.size() <= 1)                                                                   // 平均时间间隔
				return -1;
		   
			float sum = 0;						
			for(int i=0;i<timeArray.size()-1;i++)
				{				
					if( timeArray.get(i+1) != timeArray.get(i) && timeArray.get(i)!= 0 )               //未知时间戳不参与计算                                     //两次同类型浏览行为的平均间隔
					{						
						    timeCount.add(timeArray.get(i));
							sum += Math.abs(timeArray.get(i+1) - timeArray.get(i));												
					}
				}
			if(timeCount.size() == 0)                                                                   //只有一回浏览行为，-1
				return -1;
			return sum/timeCount.size();
		}	
	 public static float MaxInterval(ArrayList<Long> timeArray)                                             
		{
		  Collections.sort(timeArray);
				  
		  if(timeArray.size() <= 1)                                                                     //最长时间间隔
				return -1;		
		  
		  ArrayList<Long> interval = new  ArrayList<Long>();
		 		  
		  for(int i=0;i<timeArray.size()-1;i++)
				{			  		
			  		if(timeArray.get(i) != 0 && timeArray.get(i+1) != timeArray.get(i)) 			  			
			  			interval.add( Math.abs(timeArray.get(i+1)-timeArray.get(i)));
				}
		
		  Collections.sort(interval);		 
		  return  interval.size() == 0 ? -1 :  interval.get(interval.size()-1);                                                      //如果用户只有一回浏览记录，即最大值为零，返回-1
		}
	 public static float MinInterval(ArrayList<Long> timeArray)                                             
		{				
		 Collections.sort(timeArray);		 
		 
		 if(timeArray.size() <= 1)                                                                       //最短两笔时间间隔
				return -1;
		 ArrayList<Long> interval = new  ArrayList<Long>();
					 		    
		for(int i=0;i<timeArray.size()-1;i++)
				{					
				    if(timeArray.get(i) != 0 && timeArray.get(i+1) != timeArray.get(i) )                                                            //未知时间戳不参与计算													                                                   
						interval.add( Math.abs(timeArray.get(i+1)-timeArray.get(i)));           //最小时间间隔必须是不同时刻之差											
				}
		ArrayList<Long> noZeroInterval = new ArrayList<Long>();
		for(int i=0;i<interval.size();i++)
		{
			if(interval.get(i)!=0) 
				noZeroInterval.add(interval.get(i));
		}
		
		Collections.sort(noZeroInterval);
		
		return noZeroInterval.size() == 0 ? -1 : noZeroInterval.get(0);                           //如果用户只有一回浏览记录，即最小值为零，返回-1    			 
		}
	 public static ArrayList<String> topKBrowser (String distributionFile,int toKCount) throws IOException         //获取topK的浏览行为对
	 {
		 ArrayList<String> topKBehavoirs = new ArrayList<String>();
		 File file=new File(distributionFile);	                                                                   
		 BufferedReader reader = null;         
		 reader = new BufferedReader(new FileReader(file));  
		 String tempString = null;
		 int count = 0;
		 while( (tempString = reader.readLine())!=null  &&  count < toKCount)
		   {	
				 String [] temp = tempString.split(",");  
				 topKBehavoirs.add(temp[0]+","+temp[1]);                                                        
				 count++;
		   }
		 reader.close();
		 return topKBehavoirs;
	 }
	 public static  HashMap<Long,browseFeature> record(String browseDetailFile,String browseFeatureFile,
			                                           String distributionFile, String loanTimeFile,int topKCount) throws IOException 	                                        	 																													//获取浏览行为记录特征
	 {
		 	HashMap<Long,browseFeature> userBrowseMap = new HashMap<Long,browseFeature>();		 			 	
		 	ArrayList<String> mainBehaviors = topKBrowser(distributionFile,topKCount);  //主要的topKCount个浏览行为对(统计后大约31个  10万+)
			HashSet<String> BehaviorSet = new HashSet<String>(mainBehaviors);
			
		 	HashMap<Long,Long> loanTimeMap = loanTime(loanTimeFile);                      // 放款时间		 	
		 	HashSet<String> userList = new HashSet<String>();
		 	
		    File file2=new File(browseFeatureFile);                                       //写入 浏览记录特征 
			FileWriter fw = null;
	        BufferedWriter writer = null;
	        fw = new FileWriter(file2);
	        writer = new BufferedWriter(fw);
					
			File file=new File(browseDetailFile);	                                      //用户浏览行为主要分布，排序后的文件                                                                   
			BufferedReader reader = null;         
			reader = new BufferedReader(new FileReader(file));  
			
			writer.write(browseFeature.fetureTitle(topKCount)+"\n");
			writer.flush();
			
			String tempString = null;			
			while((tempString = reader.readLine())!=null)                                 //用户ID清单
			{
				 String [] temp0 = tempString.split(",");
				 userList.add(temp0[0]);
			}
			
			Iterator<String> it = userList.iterator();
			while(it.hasNext())				
				userBrowseMap.put(Long.valueOf(it.next()), new browseFeature(topKCount));   //初始化每个用户的特征
																			
			reader = new BufferedReader(new FileReader(file));  
			while((tempString = reader.readLine())!=null) 
			{						
				String [] temp = tempString.split(",");                                    // userID+time+main+sub								
				String behavior = temp[2]+","+temp[3];                                     // 主要浏览行为:主+次，键(key)
				
				int topindex = topIndex(mainBehaviors,behavior);                           // 对应第几个topK浏览行为												
				Long browseKey = Long.valueOf(temp[0]);                                    // 用户ID作为 键												
				
				userBrowseMap.get(browseKey).browse[0] ++;                                 // 总浏览个数
				userBrowseMap.get(browseKey).browsetimeList.add(temp[1]);                  // 浏览的时间
				
				if( topindex != -1 && topindex < topKCount-1)                              // topKCount
				{						
					userBrowseMap.get(browseKey).browse[3] ++;                                   
					userBrowseMap.get(browseKey).topKDistribution[topindex]++; 
					userBrowseMap.get(browseKey).topKtimeList.add(temp[1]);                // 用户浏览topK的时刻记录													                          					
				}
				else
				{	
					userBrowseMap.get(browseKey).browse[7] ++;
					userBrowseMap.get(browseKey).topKDistribution[topKCount-1]++;
					userBrowseMap.get(browseKey).elsetimeList.add(temp[1]);                // 用户浏览其他行为的时刻					
				}
				
				// 浏览时间戳转换一下
				Long  browseTime =  Long.valueOf(temp[1]) == 0 ? 0 : (Long.valueOf(temp[1])-5800000000L)/86400;
				if(loanTimeMap.get(browseKey) > browseTime)
					{					    						
						userBrowseMap.get(browseKey).browseBeforeLoanTimeList.add(browseTime);  // 放款之前浏览行为发生时间序列        
					}
				else if(loanTimeMap.get(browseKey) <= browseTime)
				    {										 
					   userBrowseMap.get(browseKey).browseAfterLoanTimeList.add(browseTime);   // 放款之后浏览行为发生时间序列     
				    }								
			}
			reader.close();
									
			Iterator<String> it2 = userList.iterator();                                   // 遍历有浏览行为记录的用户的浏览清单
			while(it2.hasNext())
			{	
				long user = Long.valueOf(it2.next());                                     // 键是long型						     	
				userBrowseMap.get(user).browse[1] = userBrowseMap.get(user).browsetimeList.size();
				userBrowseMap.get(user).browse[2] = userBrowseMap.get(user).browse[0] / userBrowseMap.get(user).browse[1];
								
				userBrowseMap.get(user).browse[4] = userBrowseMap.get(user).browse[3] / userBrowseMap.get(user).topKtimeList.size();
				
				userBrowseMap.get(user).browse[8] = userBrowseMap.get(user).elsetimeList.size() == 0 ?
												0 : userBrowseMap.get(user).browse[7] / userBrowseMap.get(user).elsetimeList.size();
				
				userBrowseMap.get(user).browse[5] = userBrowseMap.get(user).browse[3] / userBrowseMap.get(user).browse[0];
				userBrowseMap.get(user).browse[6] = userBrowseMap.get(user).browse[7] == 0 ?
						                       -1 : userBrowseMap.get(user).browse[3] / userBrowseMap.get(user).browse[7];		
				
				// 放款前的浏览时间序列
				ArrayList <Long> beforeLoanArray = new ArrayList <Long>(userBrowseMap.get(user).browseBeforeLoanTimeList);
				Collections.sort(beforeLoanArray);
				if(beforeLoanArray.size() == 0)
					userBrowseMap.get(user).firstTime = -1L;
				else
					userBrowseMap.get(user).firstTime = beforeLoanArray.get(0);
				
				// 用户申请贷款花费的时间和浏览次数(与信用成反比)
				if(userBrowseMap.get(user).firstTime == -1L)
					{
						userBrowseMap.get(user).timeBeforeLoan = -1L;
						userBrowseMap.get(user).countBeforeLoan = -1L;
					}
				else
					{
						userBrowseMap.get(user).timeBeforeLoan = loanTimeMap.get(user) - userBrowseMap.get(user).firstTime;
						userBrowseMap.get(user).countBeforeLoan = userBrowseMap.get(user).browseBeforeLoanTimeList.size();
					}														
				 
				 // 放款之前最近的一次浏览行为发生的时间
				 if(beforeLoanArray.size() != 0)
					 userBrowseMap.get(user).timeRecentBeforeLoan = 
					                                    loanTimeMap.get(user) - beforeLoanArray.get(beforeLoanArray.size()-1);
				 else
					 userBrowseMap.get(user).timeRecentBeforeLoan = -1L;
				 
				 // 用户浏览行为平均间隔
				 userBrowseMap.get(user).avgInterval = (long)AvgInterval(beforeLoanArray);
			}  		
									
			Iterator iter = userBrowseMap.entrySet().iterator();
			 while (iter.hasNext()) 
			   {  
				 Map.Entry entry = (Map.Entry) iter.next();
				 Long key = (Long) entry.getKey();				 
				 browseFeature val = (browseFeature) entry.getValue();
				 writer.write(key+","+val.toString()+"\n");
			   }			 
			reader.close();
			writer.close();	
			return userBrowseMap;
	 }
	 /*
	 public static browseFeture getFetureList(ArrayList<browseHistory> detailList,ArrayList<String> mainBehaviors,int topKCount,int subCount)  
		{   
		     //获取用户的浏览历史特征 : 在TopK和其他 主+次 浏览行为、11种子行为上的分布,
		     browseFeture result =  new browseFeture(topKCount,subCount);
		     int topKBrowseTimes = 0;
		    
		     HashSet<Long> totalTimes = new HashSet<Long>();
		     HashSet<Long> differTime = new HashSet<Long>();                                                         //浏览topK记录的次数
		     
			Iterator<browseHistory> it = detailList.iterator();												         
			while(it.hasNext())
			{	
				browseHistory temp_browseDetail = it.next();
				totalTimes.add(temp_browseDetail.time);
				String  behavior = temp_browseDetail.browseMain+"	"+ temp_browseDetail.browseChild;                //主、次浏览
				
				int topindex;
				if((topindex = topIndex(mainBehaviors,behavior)) != -1)                                              //第k个主浏览行为次数累加
				{					
					differTime.add(temp_browseDetail.time);				
					result.topKDistribution[topindex]++;                                                             //topK浏览记录对应次数累加
					topKBrowseTimes++;					
				}
				else
					result.elseDistribution++;
			   result.subBehavior[temp_browseDetail.browseChild-1]++;  		  					
			}
			result.browserPerTime = (totalTimes.size() == 0) ? 0 : detailList.size()/totalTimes.size();               //平均每次浏览记录数
			result.browserTopKPerTime = (differTime.size() == 0) ? 0 : topKBrowseTimes/differTime.size();             //平均每次浏览topK记录的次数
			return result;
		}
		*/
	 public  static int topIndex(ArrayList<String> mainBehaviors,String topString)                             //对应哪个 topK 浏览记录和子记录
	 {
		 for(int i=0;i<mainBehaviors.size();i++)
			 {
			 	if(topString.compareTo(mainBehaviors.get(i))==0)
			 		return i;	
			 }
		return -1;		 
	 }	
	 public static float  sum(int [] distribution, int begin,int end)
	 {
		 float sum = 0f;
		 for(int i=begin;i<end;i++)
			 sum += distribution[i];
		 return sum;
	 }
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
 }
 
