package helper;

import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/3/1
 * Time: 下午3:22
 */
public class ExcelUtils {

    public void createExcel(String templateFileName, Map<String, Object> beanParams, String resultFileName)
            throws InvalidFormatException, IOException {
        XLSTransformer transformer = new XLSTransformer();
        InputStream is = this.getClass().getResourceAsStream(templateFileName);
        Workbook workbook = transformer.transformXLS(is, beanParams);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(resultFileName));
        workbook.write(os);
        is.close();
        os.flush();
        os.close();
    }

}
