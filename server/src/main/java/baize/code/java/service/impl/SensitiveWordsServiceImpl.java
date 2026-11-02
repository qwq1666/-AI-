package baize.code.java.service.impl;

import baize.code.java.service.SensitiveWordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import baize.code.java.entity.SensitiveWords;
import baize.code.java.mapper.SensitiveWordsMapper;
import org.springframework.stereotype.Service;

@Service
public class SensitiveWordsServiceImpl extends ServiceImpl<SensitiveWordsMapper, SensitiveWords> implements SensitiveWordsService {
}