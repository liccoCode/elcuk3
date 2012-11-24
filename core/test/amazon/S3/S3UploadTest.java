package amazon.S3;

import helper.S3;
import org.jets3t.service.S3Service;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.security.AWSCredentials;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 11/24/12
 * Time: 9:54 PM
 */
public class S3UploadTest extends UnitTest {
    AWSCredentials credential;
    S3Service service;

    @Before
    public void setUp() throws ServiceException {
        credential = (AWSCredentials) AWSCredentials.load("elcuk2", new File("conf/s3.enc"));
        service = new RestS3Service(credential);
        S3.init();
    }

    @Test
    public void buckets() throws ServiceException {
        S3Bucket[] buckets = service.listAllBuckets();
        System.out.println("How many buckets to I have in S3? " + buckets.length);
    }

    @Test
    public void upload() {
        S3.I().upload("conf", new File("conf/application.conf"));
    }

    @Test
    public void download() {
        File file = S3.I().download("conf", "s3.enc");
        System.out.println(file.getParent());
    }
}
