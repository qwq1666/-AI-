package baize.code.java.service.impl;

import baize.code.java.code.ResultCode;
import baize.code.java.common.Result;
import baize.code.java.service.GoodsDocumentService;
import baize.code.java.utils.FileToDocuments;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import baize.code.java.entity.GoodsDocument;
import baize.code.java.mapper.GoodsDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static baize.code.java.code.DocumentCode.FILE_ID;
import static baize.code.java.code.DocumentCode.GOODS_ID;

@Service
@RequiredArgsConstructor
public class GoodsDocumentServiceImpl extends ServiceImpl<GoodsDocumentMapper, GoodsDocument> implements GoodsDocumentService {

    private final MilvusVectorStore vectorStore;
    private final FileToDocuments fileToDocuments;

    @Override
    public List<GoodsDocument> getListByGoodsId(Integer id) {
        return lambdaQuery().eq(GoodsDocument::getGoodsId, id).list();
    }

    @Override
    public Result<?> upload(MultipartFile file, Integer goodsId) {
        List<Document> documents = fileToDocuments.handle(file);
        System.out.println(documents);
        GoodsDocument goodsDocument = GoodsDocument.builder().name(file.getOriginalFilename())
                .goodsId(goodsId)
                .build();
        if(!save(goodsDocument)){
            return Result.error(ResultCode.UPLOAD_DOCUMENT_ERROR);
        }
        documents.forEach(d -> {
            d.getMetadata().put(FILE_ID,goodsDocument.getId().toString());
            d.getMetadata().put(GOODS_ID,goodsId.toString());
        });
        for(int i=0;i<documents.size();i+=10){
            int endIndex = Math.min(i+10,documents.size());
            List<Document> batch = documents.subList(i,endIndex);
            vectorStore.add(batch);
        }

        return Result.success(ResultCode.ADD_SUCCESS,goodsDocument);
    }

    @Override
    public Result<?> delete(Integer id) {
        GoodsDocument fileInfo = getById(id);
        if(fileInfo==null){
            return Result.error(ResultCode.DELETE_DOCUMENT_ERROR);
        }
        if(!removeById(id)){
            return Result.error(ResultCode.DELETE_DOCUMENT_ERROR);
        }
        vectorStore.delete(new Filter.Expression(Filter.ExpressionType.EQ,new Filter.Key(FILE_ID),new Filter.Value(id.toString())));
        return Result.success(ResultCode.DELETE_SUCCESS);
    }

}
