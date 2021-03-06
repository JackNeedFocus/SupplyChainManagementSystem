package com.jack.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jack.admin.model.RespBean;
import com.jack.admin.model.SaleCount;
import com.jack.admin.pojo.SaleList;
import com.jack.admin.pojo.SaleListGoods;
import com.jack.admin.pojo.User;
import com.jack.admin.query.SaleListGoodsQuery;
import com.jack.admin.query.SaleListQuery;
import com.jack.admin.service.ISaleListService;
import com.jack.admin.service.IUserService;
import com.jack.admin.utils.DateUtil;
import com.jack.admin.utils.MathUtil;
import com.jack.admin.utils.PageResultUtil;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售单表 前端控制器
 * </p>
 *
 * @author Jack
 * @since 2021-05-13
 */
@Controller
@RequestMapping("/sale")
public class SaleListController {
    @Resource
    private ISaleListService saleListService;

    @Resource
    private IUserService userService;

    /**
     * 销售出库主页
     * @return
     */
    @RequestMapping("index")
    public String index(Model model){
        model.addAttribute("saleNumber",saleListService.getNextSaleNumber());
        return "sale/sale";
    }

    @RequestMapping("save")
    @ResponseBody
    public RespBean save(SaleList saleList, String goodsJson, Principal principal){
        saleList.setUserId(userService.findUserByUserName(principal.getName()).getId());
        Gson gson = new Gson();
        List<SaleListGoods> slgList = gson.fromJson(goodsJson, new TypeToken<List<SaleListGoods>>(){}.getType());
        saleListService.saveSaleList(saleList, slgList);
        return RespBean.success("商品销售出库成功");
    }

    @RequestMapping("searchPage")
    public String searchPage(){
        return "sale/sale_search";
    }

    @RequestMapping("list")
    @ResponseBody
    public Map<String, Object> salelist(SaleListQuery saleListQuery){
        return saleListService.saveList(saleListQuery);
    }

    @RequestMapping("countSale")
    @ResponseBody
    public Map<String, Object> countSale(SaleListQuery saleListQuery){
        return saleListService.countSale(saleListQuery);
    }

    @RequestMapping("countSaleByDay")
    @ResponseBody
    public Map<String, Object> countDaySale(String begin, String end){
        Map<String, Object> result = new HashMap<String, Object>();
        List<SaleCount> saleCounts =  new ArrayList<SaleCount>();

        List<Map<String,Object>> list = saleListService.countDaySale(begin, end);

        List<String> dates = DateUtil.getRangeDates(begin,end);
        for(String date : dates){
            SaleCount saleCount = new SaleCount();
            saleCount.setDate(date);
            boolean flag = true;// true表示某一天无销售额
            for(Map<String, Object> map : list){
                String dd = map.get("saleDate").toString().substring(0, 10);
                //
                if(date.equals(dd)){ // 当前存在销售额
                    saleCount.setAmountCost(MathUtil.format2Bit(Float.parseFloat(map.get("amountCost").toString())));
                    saleCount.setAmountSale(MathUtil.format2Bit(Float.parseFloat(map.get("amountSale").toString())));
                    saleCount.setAmountProfit(MathUtil.format2Bit(saleCount.getAmountSale()-saleCount.getAmountCost()));
                    flag = false;
                }
            }
            if(flag) {
                saleCount.setAmountProfit(0F);
                saleCount.setAmountSale(0F);
                saleCount.setAmountCost(0F);
            }
            saleCounts.add(saleCount);
        }

        result.put("count",saleCounts.size());
        result.put("data",saleCounts);
        result.put("code",0);
        result.put("msg","");
        return result;

    }

    @RequestMapping("countSaleByMonth")
    @ResponseBody
    public Map<String, Object> countMonthSale(String begin, String end){
        Map<String, Object> result = new HashMap<String, Object>();
        List<SaleCount> saleCounts =  new ArrayList<SaleCount>();

        List<Map<String,Object>> list = saleListService.countMonthSale(begin, end);

        List<String> dates = DateUtil.getRangeMonth(begin,end);
        for(String date : dates){
            SaleCount saleCount = new SaleCount();
            saleCount.setDate(date);
            boolean flag = true;// true表示某一天无销售额
            for(Map<String, Object> map : list){
                String dd = map.get("saleDate").toString().substring(0, 7);
                //
                if(date.equals(dd)){ // 当前存在销售额
                    saleCount.setAmountCost(MathUtil.format2Bit(Float.parseFloat(map.get("amountCost").toString())));
                    saleCount.setAmountSale(MathUtil.format2Bit(Float.parseFloat(map.get("amountSale").toString())));
                    saleCount.setAmountProfit(MathUtil.format2Bit(saleCount.getAmountSale()-saleCount.getAmountCost()));
                    flag = false;
                }
            }
            if(flag) {
                saleCount.setAmountProfit(0F);
                saleCount.setAmountSale(0F);
                saleCount.setAmountCost(0F);
            }
            saleCounts.add(saleCount);
        }

        result.put("count",saleCounts.size());
        result.put("data",saleCounts);
        result.put("code",0);
        result.put("msg","");
        return result;

    }
}
