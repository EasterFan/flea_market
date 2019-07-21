package src.cdmodel;

import java.util.*;


/**
 * 基于用户的协同过滤算法实现部分 
 * 输入用户-->物品条目  一个用户对应多个物品
 * 举一个明显的例子： 4 个用户中，用户 A 和 B 的相似性最高，想要向 A 做推荐， K 取 2， 肉眼可见 B 是和 A 相似度最高的人， AB 是一组邻居，
 * 那么系统就会向用户 A 推荐物品 e.
 *
 * 用户ID	物品ID集合
 *   A		a b c d
 *   B		a b c e
 *   C		d f
 *   D		d h
 */

public class UserCFModel {
    public static void main(String[] args) {
        UserCF userCF = new UserCF();

        // 0. 获取所有购物车数据, 用户-物品 eg:{A:(abcd), B:(abce), C:(df), D:(dh)}
        Map<Integer,Set<Integer>> originUserItemData = userCF.getAllOriginUserItemData();
        // 用户总量
        int N = userCF.getTotalUser();
        // 当前登录的用户id - 被推荐者
        Integer recommendUser = userCF.getCurrentLoginUserId();

        // 建立用户稀疏矩阵，用于用户相似度计算【相似度矩阵】
        int[][] sparseMatrix = new int[N][N];
        // 存储每一个用户对应的不同物品总数  eg: {a:4, b:4, c:2, d:2}
        Map<Integer,Integer> userItemLength = new HashMap<>();
        // 建立 物品--用户倒排表 eg: {a=[A, B], b=[A, B], c=[A, B], d=[A, C, D], e=[B], f=[C], h=[D]}
        Map<Integer, Set<Integer>> itemUserCollection = userCF.getAllOriginItemUserData();

        Set<Integer> items = new HashSet<>();//辅助存储物品集合 eg: {a, b, c, d, e,f,h}
        Map<Integer, Integer> userID = new HashMap<>();//辅助存储每一个用户的用户ID映射 eg: {A:0, B:1, C:2, D:3}
        Map<Integer, Integer> idUser = new HashMap<>();//辅助存储每一个ID对应的用户映射 eg: {0:A, 1:B, 2:C, 3:D}

        Map<Double, Map<Integer,Integer>> recommendResultMap = null;


        // 遍历用户-物品原始数据， 提取相应数据
        Set<Integer> keySet = originUserItemData.keySet();
        for(Iterator<Integer> it = keySet.iterator();it.hasNext();){
            Integer key = it.next();
            int i = 0;
            Integer value = originUserItemData.get(key).size();
            userItemLength.put(key,value);

            items.addAll(originUserItemData.get(key));

            userID.put(key, i);//用户ID与稀疏矩阵建立对应关系
            idUser.put(i, key);
            ++i;
        }

        // 1. 计算相似度矩阵【稀疏】
        Set<Map.Entry<Integer, Set<Integer>>> entrySet = itemUserCollection.entrySet();
        Iterator<Map.Entry<Integer, Set<Integer>>> iterator = entrySet.iterator();
        while(iterator.hasNext()){
            Set<Integer> commonUsers = iterator.next().getValue();
            for (Integer user_u : commonUsers) {
                for (Integer user_v : commonUsers) {
                    if(user_u.equals(user_v)){
                        continue;
                    }
                    sparseMatrix[userID.get(user_u)][userID.get(user_v)] += 1;//计算用户u与用户v都有正反馈的物品总数
                }
            }
        }

        // 2. 计算用户之间的相似度【余弦相似性】
        int recommendUserId = userID.get(recommendUser);
        for (int j = 0;j < sparseMatrix.length; j++) {
            if(j != recommendUserId){
                System.out.println(idUser.get(recommendUserId)+"--"+idUser.get(j)+"相似度:"+sparseMatrix[recommendUserId][j]/Math.sqrt(userItemLength.get(idUser.get(recommendUserId))*userItemLength.get(idUser.get(j))));
            }
        }

        // 3. 计算指定用户recommendUser的物品推荐度, 得到推荐结果
        for(Integer item: items){//遍历每一件物品
            Set<Integer> users = itemUserCollection.get(item);//得到购买当前物品的所有用户集合
            if(!users.contains(recommendUser)){//如果被推荐用户没有购买当前物品，则进行推荐度计算
                double itemRecommendDegree = 0.0;
                for(Integer user: users){
                    itemRecommendDegree += sparseMatrix[userID.get(recommendUser)][userID.get(user)]/Math.sqrt(userItemLength.get(recommendUser)*userItemLength.get(user));//推荐度计算
                }

                Map<Integer,Integer> recommendUserItem = new HashMap<>();
                recommendUserItem.put(item,recommendUser);
                recommendResultMap.put(itemRecommendDegree, recommendUserItem);
//                System.out.println("The item "+item+" for "+recommendUser +"'s recommended degree:"+itemRecommendDegree);
            }
        }


        // 遍历结果得到最优推荐(相似度最高的一组推荐) [物品--> 用户]
        Map<Integer,Integer> bestRecommend = userCF.getFinallResult(recommendResultMap);

        // 4. 将推荐结果写入 recommend 表
        userCF.savaRecommend(bestRecommend);
    }
}
