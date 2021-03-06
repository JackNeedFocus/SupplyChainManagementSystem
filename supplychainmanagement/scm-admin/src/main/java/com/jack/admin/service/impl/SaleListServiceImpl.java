package com.jack.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jack.admin.model.CountResultModel;
import com.jack.admin.model.RespBean;
import com.jack.admin.model.SaleCount;
import com.jack.admin.pojo.Goods;
import com.jack.admin.pojo.SaleList;
import com.jack.admin.mapper.SaleListMapper;
import com.jack.admin.pojo.SaleListGoods;
import com.jack.admin.query.SaleListQuery;
import com.jack.admin.service.IGoodsService;
import com.jack.admin.service.IGoodsTypeService;
import com.jack.admin.service.ISaleListGoodsService;
import com.jack.admin.service.ISaleListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.admin.utils.AssertUtil;
import com.jack.admin.utils.DateUtil;
import com.jack.admin.utils.PageResultUtil;
import com.jack.admin.utils.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售单表 服务实现类
 * </p>
 *
 * @author Jack
 * @since 2021-05-13
 */
@Service
public class SaleListServiceImpl extends ServiceImpl<SaleListMapper, SaleList> implements ISaleListService {

    @Resource
    IGoodsService goodsService;

    @Resource
    ISaleListGoodsService saleListGoodsService;

    @Resource
    IGoodsTypeService goodsTypeService;

    @Override
    public String getNextSaleNumber() {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("XS");
            stringBuffer.append(DateUtil.getCurrentDateStr());
            String saleNumber = this.baseMapper.getNextSaleNumber();
            if(null != saleNumber){
                stringBuffer.append(StringUtil.formatCode(saleNumber));
            } else{
                stringBuffer.append("0001");
            }
            return stringBuffer.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
            return "";
        }
    }

    @Override
    public void saveSaleList(SaleList saleList, List<SaleListGoods> slgList) {
        AssertUtil.isTrue(!(this.save(saleList)), "记录添加失败");
        SaleList temp = this.getOne(new QueryWrapper<SaleList>().eq("sale_number", saleList.getSaleNumber())); // 从数据库中重新取出是为了获得分配到的id
        slgList.forEach(slg -> {
            slg.setSaleListId(temp.getId());

            Goods goods = goodsService.getById(slg.getGoodsId());
            goods.setInventoryQuantity(goods.getInventoryQuantity() - slg.getNum());
            if(goods.getInventoryQuantity() == 0){
                goods.setState(1);
            } else {
                goods.setState(2);
            }
            AssertUtil.isTrue(!(goodsService.updateById(goods)), "记录添加失败");
            AssertUtil.isTrue(!(saleListGoodsService.save(slg)), "记录更新失败");
        });
    }

    @Override
    public Map<String, Object> saveList(SaleListQuery saleListQuery) {
        IPage<SaleList> page = new Page<SaleList>(saleListQuery.getPage(), saleListQuery.getLimit());
        page = this.baseMapper.saleList(page, saleListQuery);
        return PageResultUtil.getResult(page.getSize(), page.getRecords());
    }

    @Override
    public Map<String, Object> countSale(SaleListQuery saleListQuery) {
        if(null != saleListQuery.getTypeId()){
            saleListQuery.setTypeIds(goodsTypeService.queryAllSubTypeIdsByTypeId(saleListQuery.getTypeId()));
        }
        saleListQuery.setIndex((saleListQuery.getPage()-1) * saleListQuery.getLimit());
        Long count = this.baseMapper.countSaleTotal(saleListQuery);
        List<CountResultModel> list = this.baseMapper.countSaleList(saleListQuery);
        return PageResultUtil.getResult(count, list);
    }

    @Override
    public List<Map<String,Object>> countDaySale(String begin, String end) {
        return this.baseMapper.countDaySale(begin, end);
    }

    @Override
    public List<Map<String, Object>> countMonthSale(String begin, String end) {
        return this.baseMapper.countMonthSale(begin, end);
    }
}
