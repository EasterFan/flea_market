package src.cdmodel;

import src.dbHandle.RecommendHandle;
import src.dbHandle.ShopCartHandle;
import src.dbHandle.UserHandle;
import src.vo.Shoppingcart;

import java.util.*;

/**
 * 协同过滤的功能工具集
 */

public class UserCF {
    public UserCF() {
    }


    private ShopCartHandle shopCartHandle = new ShopCartHandle();
    private RecommendHandle recommendHandle = new RecommendHandle();
    private UserHandle userHandle = new UserHandle();

    // 获取当前用户总数
    public int getTotalUser(){

        try {
            return userHandle.findTotalNum();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 获取所有购物车数据 - 用户-物品映射  eg:{A:(abcd), B:(abce), C:(df), D:(dh)}
    public Map<Integer,Set<Integer>> getAllOriginUserItemData(){
        List<Shoppingcart> originShoppingcartData = null;

        try {
            originShoppingcartData = shopCartHandle.findAllShopcartGoods();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Set<Integer> userIdSet = new HashSet<>();
        Map<Integer,Set<Integer>> userAndItemResult = new HashMap<>();

        for (Shoppingcart shoppingcartItem : originShoppingcartData){
            userIdSet.add(shoppingcartItem.getUserId());
        }

        for (Iterator it = userIdSet.iterator();it.hasNext();){
            Integer userId = (Integer) it.next();

            Set<Integer> itemIdSet = null;
            for (Shoppingcart shoppingcartItem : originShoppingcartData){
                if (shoppingcartItem.getUserId() == userId)
                    itemIdSet.add(shoppingcartItem.getGoodsId());
            }
            userAndItemResult.put(userId,itemIdSet);
        }

        return userAndItemResult;
    }

    // 获取所有购物车数据 - 物品-用户映射  eg: {a=[A, B], b=[A, B], c=[A, B], d=[A, C, D], e=[B], f=[C], h=[D]}
    public Map<Integer,Set<Integer>> getAllOriginItemUserData(){
        List<Shoppingcart> originShoppingcartData = null;

        try {
            originShoppingcartData = shopCartHandle.findAllShopcartGoods();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Set<Integer> itemIdSet = new HashSet<>();
        Map<Integer,Set<Integer>> itemAndUsermResult = new HashMap<>();

        for (Shoppingcart shoppingcartItem : originShoppingcartData){
            itemIdSet.add(shoppingcartItem.getGoodsId());
        }

        for (Iterator it = itemIdSet.iterator();it.hasNext();){
            Integer itemId = (Integer) it.next();
            Set<Integer> userSet = null;


            for (Shoppingcart shoppingcartItem : originShoppingcartData){
                if (itemId.equals(shoppingcartItem.getGoodsId()))
                userSet.add(shoppingcartItem.getUserId());
            }
            itemAndUsermResult.put((Integer) it.next(),userSet);
        }

        return itemAndUsermResult;
    }


    // 获取当前用户 id
    public Integer getCurrentLoginUserId(){
        return 10;
    }


    // 获取推荐 Map 结果集中最优解
    public Map<Integer,Integer> getFinallResult(Map<Double, Map<Integer,Integer>> recommendResultMap){
        Set<Double> doubleSet = new HashSet<>();

        Set<Double> keySet = recommendResultMap.keySet();
        for (Iterator it = keySet.iterator(); it.hasNext();){
            // 可能发生精度损失找不到key
            doubleSet.add((Double) it.next());
        }

        Double maxDouble = Collections.max(doubleSet);

        return recommendResultMap.get(maxDouble);
    }


    // 推荐结果写入数据表
   public void savaRecommend(Map<Integer,Integer> recommendMap){

        Set<Integer> keyset = recommendMap.keySet();
        for (Iterator it = keyset.iterator(); it.hasNext();){
            Integer recommendItemId = (Integer) it.next();
            Integer recommendUserId = recommendMap.get(recommendItemId);

            try {
                recommendHandle.saveRecommendRecord(recommendItemId, recommendUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
   }

}
