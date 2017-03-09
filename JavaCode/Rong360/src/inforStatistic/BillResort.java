package inforStatistic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class BillResort 
{
	// 二次清洗数据和填充未知时间戳（相对时间与绝对时间）
	
	public static  ArrayList<billDetail>  allUnknownTime(ArrayList<billDetail> ZeroBill)
	{
		ArrayList<billDetail> cleanBillList =  new  ArrayList<billDetail>();                // 清洗并填充后的账单作为结果返回
		
		ArrayList<billDetail>  beginList = new ArrayList<billDetail>();			
		for(int i = 0;i<ZeroBill.size();i++)
			{
				int preFlag = 0,nextFlag = 0;                                               // 有相邻账单
				for(int j = 0;j<ZeroBill.size();j++)
				{
					if( i != j && ZeroBill.get(i).lastBill == ZeroBill.get(j).thisBillRest) // 当前账单有上期账单，
						preFlag = 1;
					if(i != j && ZeroBill.get(i).thisBillRest == ZeroBill.get(j).lastBill)  // 当前账单有下期账单；
						nextFlag = 1;
				}
				if(preFlag == 0 && nextFlag == 1 )                                          // 找相对初始账单（只有下期账单没有上期账单）
					beginList.add(ZeroBill.get(i));
			}
					
		ArrayList<billDetail> usedBill =  new ArrayList<billDetail>();                      // 用过的账单
		while(beginList.size() != 0)                                                        // 相对初始账单列表还有期数
		{					
			for(int i = 0;i<ZeroBill.size();i++)
			{				
				if(ZeroBill.get(i).lastBill == beginList.get(0).thisBillRest)              // 当前相对初始账单找到了下一期账单
				{
					ZeroBill.get(i).time = beginList.get(0).time + 30L;                    // 下期账单的时间由上期来决定						
					beginList.add(ZeroBill.get(i));                                        // 找到的账单排队作为新的相对初始账单寻找下一期账单   				
					break;
				}					
			}
			
			ZeroBill.remove(beginList.get(0));		
			usedBill.add(beginList.get(0));                      		                   // 当前相对初始账单添加到已使用的账单中
			beginList.remove(0);                                                           // 当前初始账单从相对初始账单队首移除				
		}   
		HashSet<billDetail> combineBill = new HashSet<billDetail>();
		combineBill.addAll(usedBill);
		combineBill.addAll(ZeroBill);	                                                   // ZeroBill中还剩余离群点
					
		cleanBillList.addAll(combineBill);
		Collections.sort(cleanBillList);
		return cleanBillList;
	}
	public static  ArrayList<billDetail>  allKnownTime(ArrayList<billDetail> billList)
	{
		HashMap<Long,Integer> timeStampCount =  new HashMap<Long,Integer>();	      		
		HashSet<Long> uniqueTime = new HashSet<Long>();	// 	唯一时间戳列表	
		
		ArrayList<billDetail> uniqueTimeBill = new ArrayList<billDetail>();
		ArrayList<billDetail> repeatTimeBill = new ArrayList<billDetail>();               // 重复时间戳账单时间戳需要修正
		
		for(billDetail temp : billList)
		{
			if(timeStampCount.get(temp.time) == null)
				timeStampCount.put(temp.time, 1);
			else
				timeStampCount.put(temp.time, timeStampCount.get(temp.time)+1);
		}
		
		for (Map.Entry<Long,Integer> entry : timeStampCount.entrySet()) 
		{  
			  if(entry.getValue() == 1 )
				  uniqueTime.add(entry.getKey());				 
	     } 
		for(billDetail temp : billList)                                               // 具有唯一时间戳的账单和重复时间戳的账单
		{
			if(uniqueTime.contains(temp.time))
				uniqueTimeBill.add(temp);
			else
				repeatTimeBill.add(temp);
		}
									
		// 重复时间戳账单遍历单一时间戳，如果两期的 本期账单余额和本期最低还款相等，则将重复时间戳账单的时间戳修正为单一时间戳账单的时间戳						
		Iterator<billDetail> it1 = repeatTimeBill.iterator();
		while(it1.hasNext())
		{
			billDetail repeat =  it1.next();
			Iterator<billDetail> it2 = uniqueTimeBill.iterator();				 
			while(it2.hasNext())
			{
				billDetail unique = it2.next();
				if(unique.thisBillRest == repeat.thisBillRest && unique.curLeastReturn == repeat.curLeastReturn)
				{
					repeat.time = unique.time;                                         		
					uniqueTimeBill.add(repeat);						
					it1.remove();
					break;
				}
			}
		}
		
		Collections.sort(repeatTimeBill);
		Collections.sort(uniqueTimeBill);			
			
		boolean  findFlag = true;
		while(findFlag)
		{
			int index = 0;
			for(;index<uniqueTimeBill.size();index++)
			{
				billDetail unique = uniqueTimeBill.get(index);
				Iterator<billDetail> it3 = repeatTimeBill.iterator();
				while(it3.hasNext())
				{
					billDetail repeat =  it3.next();
					if(repeat.lastBill == unique.thisBillRest)                 // 重复账单的本期账单余额是非重复时间戳账单的上期账单金额
					{
						repeat.time = unique.time + 30L;
						uniqueTimeBill.add(repeat);	
						it3.remove();
						break;
					}	
					else if(repeat.thisBillRest == unique.lastBill)           // 重复账单的本期账单余额是非重复时间戳账单的上期账单金额
				    {
					 	repeat.time = unique.time - 30L;	
					 	uniqueTimeBill.add(repeat);
					 	it3.remove();
					 	break;
				    }
				}
			}
			if(index == uniqueTimeBill.size())
				findFlag = false;
		}
		
		ArrayList<billDetail> cleanBillList =  new  ArrayList<billDetail>();                // 清洗并填充后的账单作为结果返回
		cleanBillList.addAll(uniqueTimeBill);
		cleanBillList.addAll(repeatTimeBill);
				
		Collections.sort(cleanBillList);		
		return cleanBillList;
	}
	public static ArrayList<billDetail>   cleanAndfill(ArrayList<billDetail> billList)
	{		
		//HashSet<billDetail> cleanBill = new HashSet<billDetail>(billList);                 				
		ArrayList<billDetail> resortBill = new ArrayList<billDetail> (billList);           // 按照时间排序账单数数据
		Collections.sort(resortBill); 
		
		ArrayList<billDetail> cleanBillList =  new  ArrayList<billDetail>();                // 清洗并填充后的账单作为结果返回
		
		ArrayList<billDetail> ZeroBill    = new ArrayList<billDetail>();
		ArrayList<billDetail> NotZeroBill = new ArrayList<billDetail>();                    // 非零时间戳和零时间戳账单列表	
				
		for(billDetail temp : resortBill)         
		{
			if(temp.time == 0L)
				ZeroBill.add(temp);
			else
				NotZeroBill.add(temp);
		}
		
		// 时间戳全部未知的用户只能够找到相对时间
		if(ZeroBill.size() == billList.size())                                                  // 时间戳全部未知为0								 
			return 	allUnknownTime(ZeroBill);									
		 // 全部是绝对时间戳，需要对重复时间戳进行调整
		else if(NotZeroBill.size() == billList.size())                                    				
			return  allKnownTime(NotZeroBill);	
		// 非零时间戳与未知时间戳混合账单
		else
		{						
			/*
			 Iterator<billDetail> it1 = ZeroBill.iterator();                  // 先去重未知时间戳的重复账单，但是重复账单仍然有作用的 
			while(it1.hasNext())
				{  
					billDetail zero = it1.next(); 		  
					for(billDetail Notzero : NotZeroBill)
					    {
						   // 判定重复账单的条件
					    	if(zero.lastBill == Notzero.lastBill && zero.lastReturn == Notzero.lastReturn && 
					    	   zero.credit == Notzero.credit &&
					    	   zero.curLeastReturn == Notzero.curLeastReturn && zero.thisBillRest == Notzero.thisBillRest)		    			  		    				                                                   
					    			{		
					    				Notzero.consume = Math.max(zero.consume,Notzero.consume);  // 消费笔数进行修正					    				
					    				break;
					    			 }
					    }
				}
			*/
			
			NotZeroBill = allKnownTime(NotZeroBill);                                              //  先修正费零时间戳账单
			
			// 某些时间戳未知的账单可以根据已知时间戳的账单进行绝对时间戳的补齐，目的是扩大贷款后的数据
			ArrayList<billDetail> calculateFromKnownBill = new ArrayList<billDetail> ();          // 从绝对时间戳中推知的未知时间戳序列
			for(int i=0;i<ZeroBill.size();i++)
				{					
					for(int j = 0;j<NotZeroBill.size();j++)
						{												
							if(ZeroBill.get(i).lastBill ==  NotZeroBill.get(j).thisBillRest)      // 未知时间戳账单时已知时间戳账单的下一期
								{
									ZeroBill.get(i).time = NotZeroBill.get(j).time + 30L;
									calculateFromKnownBill.add(ZeroBill.get(i));
								}
							if(ZeroBill.get(i).thisBillRest ==  NotZeroBill.get(j).lastBill)      // 未知时间戳账单时已知时间戳账单的上一期
								{
									ZeroBill.get(i).time = NotZeroBill.get(j).time - 30L;
									calculateFromKnownBill.add(ZeroBill.get(i));
								}
						}
					}
			
			// 根据已知的时间戳能够倒推出某些时间戳未知的账单的绝对时间     
			while(calculateFromKnownBill.size() != 0)                                                
			{					
				for(int i = 0;i<ZeroBill.size();i++)
				{				
					if(ZeroBill.get(i).lastBill == calculateFromKnownBill.get(0).thisBillRest)  // 找到了下一期账单
					{
						ZeroBill.get(i).time = calculateFromKnownBill.get(0).time + 30L;        // 下期账单的时间由上期来决定						
						calculateFromKnownBill.add(ZeroBill.get(i));                            // 找到的账单排队作为新的相对初始账单寻找下一期账单   												
						break;
					}
					if(ZeroBill.get(i).thisBillRest == calculateFromKnownBill.get(0).lastBill)  // 找到了上一期账单
					{
						ZeroBill.get(i).time = calculateFromKnownBill.get(0).time - 30L;        // 下期账单的时间由上期来决定						
						calculateFromKnownBill.add(ZeroBill.get(i));                            // 找到的账单排队作为新的相对初始账单寻找下一期账单   												
						break;
					}
				}
				ZeroBill.remove(calculateFromKnownBill.get(0));				
				NotZeroBill.add(calculateFromKnownBill.get(0));                      		   // 加入到绝对时间戳账单列表中
				calculateFromKnownBill.remove(0);                                              // 当前已知时间戳账单移除				
			} 
											
			// 仍然不能填充时间戳的信用卡账单,只能填充一些相对时间
			ArrayList<billDetail>  cannotFillBill = new ArrayList<billDetail>();
			for(billDetail  temp : ZeroBill )
			{
				if(temp.time == 0L)
					cannotFillBill.add(temp);
			}                          
			ArrayList<billDetail>  beginList = new ArrayList<billDetail>();
			
			for(int i = 0;i<cannotFillBill.size();i++)
				{
					int preFlag = 0,nextFlag = 0;                                                           // 有相邻账单的标志
					for(int j = 0;j<cannotFillBill.size();j++)
					{
						if( i != j && cannotFillBill.get(i).lastBill == cannotFillBill.get(j).thisBillRest) // 当前账单有上期账单，
							preFlag = 1;
						if(i != j && cannotFillBill.get(i).thisBillRest == cannotFillBill.get(j).lastBill)  // 当前账单有下期账单；
							nextFlag = 1;
					}
					if(preFlag == 0 && nextFlag == 1 )                                    // 找相对初始账单
						beginList.add(cannotFillBill.get(i));
				}
						
			ArrayList<billDetail> usedBill =  new ArrayList<billDetail>();               // 用过的账单
			while(beginList.size() != 0)                                                 // 相对初始账单列表还有期数
			{					
				for(int i = 0;i<cannotFillBill.size();i++)
				{				
					if(cannotFillBill.get(i).lastBill == beginList.get(0).thisBillRest)  // 当前相对初始账单找到了下一期账单
					{
						cannotFillBill.get(i).time = beginList.get(0).time + 30L;        // 下期账单的时间由上期来决定						
						beginList.add(cannotFillBill.get(i));                            // 找到的账单排队作为新的相对初始账单寻找下一期账单   												
						break;
					}					
				}
				
				cannotFillBill.remove(beginList.get(0));
				usedBill.add(beginList.get(0));                      		            // 当前相对初始账单添加到已使用的账单中
				beginList.remove(0);                                                    // 当前初始账单从相对初始账单队首移除				
			} 
			
			// 混合中的未知时间戳账单相对时间填充
			HashSet<billDetail> combineBill = new HashSet<billDetail>();
			combineBill.addAll(usedBill);
			combineBill.addAll(cannotFillBill);	                                       // ZeroBill中还剩余离群点	
			combineBill.addAll(NotZeroBill);
			
			cleanBillList.addAll(combineBill);																
		}			
		Collections.sort(cleanBillList);
		return cleanBillList;		       			
	}
	
}
