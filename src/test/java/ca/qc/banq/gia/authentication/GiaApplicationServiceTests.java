package ca.qc.banq.gia.authentication;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

//@SpringBootTest
class GiaApplicationServiceTests {

    @Test
    void contextLoads() {
        var str = " ";

        assertFalse(StringUtils.isNotEmpty(str));

    }

}
