import cn.hutool.json.JSONUtil;
import com.jflove.ResponseHeadDTO;
import com.jflove.share.Application;
import com.jflove.share.api.INetdiskShare;
import com.jflove.share.dto.NetdiskShareDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: tanjun
 * @date: 2023/2/14 9:49 AM
 * @desc:
 */
@SpringBootTest(classes = Application.class)
@Log4j2
public class TestFunction {

    @Autowired
    private INetdiskShare iNetdiskShare;

    @Test
    void getDirectory(){
        ResponseHeadDTO<NetdiskShareDTO> dto =  iNetdiskShare.getDirectory("9c119087-cb77-42b7-ab38-12edb9d455e3",null);
        log.info("{}", JSONUtil.toJsonStr(dto));
    }
}
