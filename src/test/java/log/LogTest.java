package log;

import com.dnk.smart.logging.LoggerFactory;
import org.junit.Test;

public class LogTest {

    @Test
    public void caller() throws Exception {
//        LoggerFactory.TCP_EXECUTE.logger("hello {}", "world");
//        LoggerFactory.TCP_EXECUTE.logger("hello {},{}", "world", "bye!");
        LoggerFactory.TCP_EXECUTE.logger("hello {},{},{},{}", "world", "I", "am", "coming.");
    }

    @Test
    public void original() throws Exception {

        org.slf4j.LoggerFactory.getLogger("test").info("hello {},{}", "world", "bye!");
    }
}
