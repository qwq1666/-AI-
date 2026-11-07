package baize.code.java.controller;

import baize.code.java.code.ResultCode;
import baize.code.java.common.Result;
import baize.code.java.service.GoodsDocumentService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/goodsDocument")
@RequiredArgsConstructor
public class GoodsDocumentController {
    @Resource
    private GoodsDocumentService goodsDocumentService;
    /**
     * TODO:001文档上传
     * @param file 文档
     * @param goodsId 商品id
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result<?> upload(@RequestBody MultipartFile file, Integer goodsId) {
        Result<?> upload = goodsDocumentService.upload(file, goodsId);
        return Result.success(ResultCode.ADD_SUCCESS,upload.getData());
    }

    /**
     * TODO:002 删除文档
     * @param id 文档id
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam Integer id) {
        Result<?> delete = goodsDocumentService.delete(id);
        return Result.success(ResultCode.DELETE_SUCCESS,delete.getData());
    }
}
