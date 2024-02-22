package com.nrpc.config;



import com.nrpc.serialization.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Config {
    static Properties properties;
    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);// 加载配置文件
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static int getServerPort() {
        String value = properties.getProperty("server.port");
        if(value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * 获取序列化算法
     * @return
     */
    public static Serializer.Algorithm getSerializerAlgorithm(byte serializeAlgorithm) {
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializeAlgorithm];
        if(algorithm == null) {
            return Serializer.Algorithm.Java;
        } else {
            return algorithm;
        }
    }
}
