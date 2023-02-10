import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.notebook.Application;
import com.jflove.notebook.mapper.ShareLinkMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author: tanjun
 * @date: 2023/2/10 2:34 PM
 * @desc:
 */
@SpringBootTest(classes = Application.class)
@Log4j2
public class Test {
    @Autowired
    private ShareLinkMapper mapper;

    @org.junit.jupiter.api.Test
    void a(){
        List s = mapper.selectList(new LambdaQueryWrapper<>());
        log.info(s.size());
    }
}
