package com.tucaohui.dbwolf.xml;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;

import com.tucaohui.dbwolf.xml.DbXmlParser.DbInstanceType;
import com.tucaohui.dbwolf.xml.DbXmlParser.DbServerType;

/**
 * 配置文件解析工具.
 * 
 */
public final class XmlConfigUtil {

    private XmlConfigUtil() {
    }

    /**
     * 解析数据服务类型.
     * 
     * @param value
     * @return 数据服务类型
     */
    public static DbInstanceType parseInstanceType(final String value) {
        DbInstanceType type = null;
        if (value.equalsIgnoreCase("singler")) type = DbInstanceType.CDbInstanceSingler;
        if (value.equalsIgnoreCase("router")) type = DbInstanceType.CDbInstanceRouter;
        return type;
    }

    /**
     * 解析数据库读写类型.
     * 
     * @param value
     * @return
     */
    public static DbServerType parseServerType(String value) {
        DbServerType wrflag = null;
        if (value.equalsIgnoreCase("r")) wrflag = DbServerType.CDbRServer;
        if (value.equalsIgnoreCase("w")) wrflag = DbServerType.CDbWServer;
        if (value.equalsIgnoreCase("wr") || value.equalsIgnoreCase("rw")) wrflag = DbServerType.CDbWRServer;
        return wrflag;
    }

    /**
     * 解析时间戳，如果时间戳不符合规定格式，会抛出异常.
     * 
     * @param timeStamp 时间字符串,格式必须为"yy-MM-dd HH:mm:ss" 或 "yy-MM-dd HH:mm".
     * @return
     * @throws ParseException
     */
    public static long parseTimeStamp(String timeStamp) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = format.parse(timeStamp);
        } catch (ParseException e) {
            SimpleDateFormat optFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
            date = optFormat.parse(timeStamp);
        }
        return date.getTime();
    }

    public static int parseInt(String integer) {
        return Integer.valueOf(integer);
    }

    /**
     * 向目标数组中动态添加元素. 该方法扩展了ArrayUtils.add，实现了泛形.
     * 
     * @param array
     * @param element
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] addToArray(T[] array, T element) {
        if (array == null) {
            array = (T[]) Array.newInstance(element.getClass(), 0);
        }
        Object[] newArray = ArrayUtils.add(array, element);
        return (T[]) Arrays.copyOf(newArray, newArray.length, array.getClass());
    }
}
