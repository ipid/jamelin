package me.ipid.jamelin.entity.sa;

import me.ipid.jamelin.exception.CompileExceptions.OutOfLimitException;

import java.util.HashMap;
import java.util.Map;

public class SATypeFactory {
    public static final int MAX_TYPE_ID = 1048576;
    private static SATypeFactory instance;

    // 类似 C 语言，在 Promela 中所有表达式的类型都会被提升为 int 的类型
    // 因此，这一类型会在生成 IL 表达式时大量使用，故使用单例模式将其缓存
    public static SAPrimitiveType promelaInt;
    private int unusedTypeId;
    private Map<Integer, Integer> unsignedTypeId;

    public SATypeFactory() {
        this.unusedTypeId = 1;
        this.unsignedTypeId = new HashMap<>();
    }

    public static SATypeFactory getInstance() {
        if (instance == null) {
            instance = new SATypeFactory();
        }

        return instance;
    }

    public static Map<String, SAPromelaType> getPrimitiveTypeMap() {
        Map<String, SAPromelaType> result = new HashMap<>();

        // TODO: 实例化 SAPrimitiveType
        result.put("bit", PrimitiveTypesLib.bit_t);
        result.put("bool", PrimitiveTypesLib.bool_t);
        result.put("byte", PrimitiveTypesLib.byte_t);
        result.put("chan", PrimitiveTypesLib.chan_t);
        result.put("short", PrimitiveTypesLib.short_t);
        result.put("int", PrimitiveTypesLib.int_t);
        result.put("mtype", PrimitiveTypesLib.mtype_t);
        result.put("pid", PrimitiveTypesLib.pid_t);

        return result;
    }

    public static int allocTypeId() {
        return getInstance().inner_allocTypeId();
    }

    /**
     * 对于每一个 bitLen，返回一个全局唯一的 typeId。
     * 函数保证对相同的 bitLen 返回相同的 id。
     *
     * @param bitLen 表示变量长度是几个位
     * @return typeId
     */
    public static int allocTypeIdForUnsigned(int bitLen) {
        return getInstance().inner_allocTypeIdForUnsigned(bitLen);
    }

    private int inner_allocTypeId() {
        if (unusedTypeId > MAX_TYPE_ID) {
            throw new OutOfLimitException("用户自定义类型的数量超出了限制（" + MAX_TYPE_ID + " 个）");
        }

        int result = unusedTypeId;
        unusedTypeId++;

        return result;
    }

    private int inner_allocTypeIdForUnsigned(int bitLen) {
        Integer oldTypeIdRaw = unsignedTypeId.get(bitLen);
        if (oldTypeIdRaw != null) {
            return oldTypeIdRaw;
        }

        int result = inner_allocTypeId();
        unsignedTypeId.put(bitLen, result);
        return result;
    }

    public static class PrimitiveTypesLib {
        public static final SAPrimitiveType
                bit_t = new SAPrimitiveType("bit", false, 1, -1),
                bool_t = new SAPrimitiveType("bool", false, 1, -2),
                byte_t = new SAPrimitiveType("byte", false, 8, -3),
                chan_t = new SAPrimitiveType("chan", false, 8, -4),
                short_t = new SAPrimitiveType("short", true, 16, -5),
                int_t = new SAPrimitiveType("int", true, 32, -6),
                mtype_t = new SAPrimitiveType("mtype", false, 8, -7),
                pid_t = new SAPrimitiveType("pid", false, 8, -8);
    }
}
