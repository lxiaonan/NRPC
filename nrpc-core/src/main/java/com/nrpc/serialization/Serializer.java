package com.nrpc.serialization;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 *   @description: 序列化接口 包含jdk、hessian、Gson、kryo、xml
 *  @author xiaonan
 *  @date 2024/2/18
 */
public interface Serializer {
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    <T> byte[] serialize(T object);

    enum Algorithm implements Serializer {
        Java {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("反序列化失败", e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(object);
                    return baos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化失败", e);
                }
            }
        },
        Json {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                // 使用能够进行类转换的序列化器
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
                String json = new String(bytes, StandardCharsets.UTF_8);
                return gson.fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
                String json = gson.toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        },

        Hessian {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
                    return (T) input.readObject();
                } catch (IOException e) {
                    throw new RuntimeException("Hessian反序列化失败", e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                // 创建一个 ByteArrayOutputStream 对象 bos，用于存储序列化后的字节数据。
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                // 创建一个 Hessian2Output 对象 output，并将其输出流设置为上面创建的 ByteArrayOutputStream 对象 bos
                Hessian2Output output = new Hessian2Output(bos);
                try {
                    // 将目标对象 object 进行序列化操作。这会将 object 转换为 Hessian 二进制格式并写入到 bos 中
                    output.writeObject(object);
                    // 在写入完对象后，我们手动刷新输出流，确保所有数据都被写入 bos 中
                    output.getBytesOutputStream().flush();
                    // 在输出完整消息后，调用 completeMessage 方法来完成消息的写入
                    output.completeMessage();
                    output.close();
                } catch (IOException e) {
                    throw new RuntimeException("Hessian序列化失败", e);
                }

                return bos.toByteArray();
            }
        },

        Xml {
            // 需要在序列化类上 加 @XmlRootElement 注释
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {

                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    return clazz.cast(unmarshaller.unmarshal(inputStream));
                } catch (JAXBException e) {
                    throw new RuntimeException("xml反序列化失败", e);
                }

            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
                    Marshaller marshaller = jaxbContext.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    marshaller.marshal(object, outputStream);
                    return outputStream.toByteArray();
                } catch (JAXBException e) {
                    throw new RuntimeException("xml序列化失败", e);
                }
            }
        },
        Kryo {
            // 在这里进行一些配置，如注册类、设置默认序列化器等
            private final ThreadLocal<Kryo> KTL = ThreadLocal.withInitial(Kryo::new);

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Input input = null;
                try {
                    Kryo kryo = KTL.get();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                    input = new Input(byteArrayInputStream);
                    return kryo.readObject(input,clazz);
                } catch (Exception e) {
                    throw new RuntimeException("kryo反序列化失败",e);
                } finally {
                    if (input != null) {
                        input.close();
                    }
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                Output output = null;
                try {
                    Kryo kryo = KTL.get();
                    // 注册该类型
                    kryo.register(object.getClass());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    output = new Output(byteArrayOutputStream);
                    kryo.writeClassAndObject(output, object);
                    return output.toBytes();
                } catch (Exception e) {
                    throw new RuntimeException("kryo序列化失败",e);
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }
            }
        }
    }

    /**
     * gson有个bug，就是不能将java类转换成json
     * Java类转换成json
     */
    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>>{

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                String className = jsonElement.getAsString();
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
        }
    }
}
