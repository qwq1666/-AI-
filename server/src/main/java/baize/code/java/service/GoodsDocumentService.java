package baize.code.java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.entity.GoodsDocument;

import java.util.List;

public interface GoodsDocumentService extends IService<GoodsDocument> {
    List<GoodsDocument> getListByGoodsId(Integer id);

}