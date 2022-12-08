package com.jflove.test;

import com.jflove.tool.JJwtTool;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * @author tanjun
 * @date 2022/12/8 11:23
 * @describe
 */
@SpringBootTest(classes = Test.class)
@Import(JJwtTool.class)
@Log4j2
public class JJWTTest {
    @Autowired
    private JJwtTool jJwtTool;

    @Test
    void generalKey() throws Exception{
        log.info("-------------------{}",jJwtTool.createSecretKey(jJwtTool.getIssuer()));
    }

    @Test
    void createJwt(){
        log.info("------------------------{}",jJwtTool.createJwt("1","谭峻",null));
    }

    @Test
    void parseJwt(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwic3ViIjoi6LCt5bO7IiwiaWF0IjoxNjcwNDgxNTkzLCJpc3MiOiJqZmxvdmUuY24iLCJleHAiOjE2NzA0ODUxOTN9.l_cdkaLVeVcilXPcAVgwMV904Lic23f6i0wpWc180Hg";
        Jws<Claims> jws = jJwtTool.parseJwt(token);
        log.info("{}",jws.toString());
    }
}
