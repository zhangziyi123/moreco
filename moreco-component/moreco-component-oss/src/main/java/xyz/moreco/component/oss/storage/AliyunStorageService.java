package xyz.moreco.component.oss.storage;

import com.aliyun.oss.OSSClient;
import xyz.moreco.component.oss.config.OssProperties;

import java.io.InputStream;

/**
 * 阿里云实现
 *
 * @author zhangwei
 * date 2018/10/27
 * time 14:26
 */
public class AliyunStorageService extends StorageService {

    private OSSClient client;

    public AliyunStorageService(OssProperties config){
        this.config = config;
    }


    @Override
    public String getUploadToken(String bucketName) {
        return null;
    }

    @Override
    public String upload(byte[] data, String bucketName, String path) {
        return null;
    }

    @Override
    public String uploadSuffix(byte[] data, String bucketName, String suffix) {
        return null;
    }

    @Override
    public String upload(InputStream inputStream, String bucketName, String path) {
        return null;
    }

    @Override
    public String uploadSuffix(InputStream inputStream, String bucketName, String suffix) {
        return null;
    }
}
