package baize.code.java.service;

import baize.code.java.common.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.entity.GoodsDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoodsDocumentService extends IService<GoodsDocument> {
    List<GoodsDocument> getListByGoodsId(Integer id);
    //实现文件的上传
    Result<?> upload(MultipartFile file, Integer goodsId);
    //根据文档ID删除文档
    Result<?> delete(Integer id);
}