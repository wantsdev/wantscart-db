package cn.techwolf.jade.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import cn.techwolf.jade.datasource.router.DateRouter;
import cn.techwolf.jade.datasource.router.DirectRouter;
import cn.techwolf.jade.datasource.router.HashRouter;
import cn.techwolf.jade.datasource.router.HexHashRouter;
import cn.techwolf.jade.datasource.router.RangeRouter;
import cn.techwolf.jade.datasource.router.RoundRouter;
import cn.techwolf.jade.datasource.router.StringHashCodeRouter;

/**
 * 负责从配置项生成对应的路由对象。
 * 
 * @author han.liao
 */
public class RouterLoader {

    // 输出日志
    protected static final Log logger = LogFactory.getLog(RouterLoader.class);

    // 路由的名称
    public static final String DIRECT = "direct";

    public static final String ROUND = "round";

    public static final String RANGE = "range";

    public static final String HASH = "hash";

    public static final String HEX_HASH = "hex-hash";

    public static final String HASHCODE = "hashcode";

    public static final String DATE = "date-hash";

    public static Router fromXML(Element element) {

        for (Element child : XMLUtils.getChildren(element)) {

            // <by-column>
            String column = XMLUtils.getChildText(child, "by-column");

            // <partitions>
            String partitions = XMLUtils.getChildText(child, "partitions");

            // <target-pattern>
            String pattern = XMLUtils.getChildText(child, "target-pattern");

            if (logger.isDebugEnabled()) {
                logger.debug("try to create router " + child.getTagName());
            }

            if (HASH.equalsIgnoreCase(child.getTagName())) {

                return createHashRouter(column, pattern, partitions);

            } else if (RANGE.equalsIgnoreCase(child.getTagName())) {

                return createRangeRouter(column, pattern);

            } else if (DIRECT.equalsIgnoreCase(child.getTagName())) {

                return createDirectRouter(column, pattern);

            } else if (ROUND.equalsIgnoreCase(child.getTagName())) {

                return createRoundRouter(pattern, partitions);

            } else if (HEX_HASH.equalsIgnoreCase(child.getTagName())) {

                return createHexHashRouter(column, pattern, partitions);

            } else if (HASHCODE.equalsIgnoreCase(child.getTagName())) {

                return createStringHashCodeRouter(column, pattern, partitions);

            } else if (DATE.equalsIgnoreCase(child.getTagName())) {
                String excludeNow = child.getAttribute("excludeNow");
                if (excludeNow == null || excludeNow.length() == 0) {
                    return createDateRouter(column, pattern, partitions, false);
                } else {
                    return createDateRouter(column, pattern, partitions,
                            Boolean.valueOf(excludeNow));
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("router '" + child.getTagName() + "' is not supported");
                }
            }
        }

        return null;
    }

    private static HashRouter createHashRouter(String column, String pattern, String partitions) {

        // 检查所需的属性
        if (column == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hash' must have 'by-column' property.");
            }

            return null;
        }

        if (partitions == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hash' must have 'partitions' property.");
            }

            return null;
        }

        if (pattern == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hash' must have 'target-pattern' property.");
            }

            return null;
        }

        try {
            int count = Integer.parseInt(partitions);

            // 输出日志
            if (logger.isDebugEnabled()) {
                logger.debug("Creating router 'hash' [by-column = " + column + ", partitions = "
                        + count + ", target-pattern = " + pattern + ']');
            }

            return new HashRouter(column, pattern, count);

        } catch (NumberFormatException e) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hash' property 'partitions' must be number.");
            }

            return null;
        }
    }

    private static RoundRouter createRoundRouter(String pattern, String partitions) {

        if (partitions == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'round' must have 'partitions' property.");
            }

            return null;
        }

        if (pattern == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'round' must have 'target-pattern' property.");
            }

            return null;
        }

        try {
            int count = Integer.parseInt(partitions);

            // 输出日志
            if (logger.isDebugEnabled()) {
                logger.debug("Creating router 'round' [partitions = " + count
                        + ", target-pattern = " + pattern + ']');
            }

            return new RoundRouter(pattern, count);

        } catch (NumberFormatException e) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'round' property 'partitions' must be number.");
            }

            return null;
        }
    }

    private static DirectRouter createDirectRouter(String column, String pattern) {

        if (pattern == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'direct' must have 'target-pattern' property.");
            }

            return null;
        }

        // 输出日志
        if (logger.isDebugEnabled()) {
            if (column != null) {
                logger.debug("Creating router 'direct' [by-column = " + column
                        + ", target-pattern = " + pattern + ']');
            } else {
                logger.debug("Creating router 'direct' [target-pattern = " + pattern + ']');
            }
        }

        return new DirectRouter(column, pattern);
    }

    private static RangeRouter createRangeRouter(String column, String pattern) {

        // 检查所需的属性
        if (column == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'range' must have 'by-column' property.");
            }

            return null;
        }

        if (pattern == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'range' must have 'target-pattern' property.");
            }

            return null;
        }

        // 输出日志
        if (logger.isDebugEnabled()) {
            logger.debug("Creating router 'range' [by-column = " + column + ", target-pattern = "
                    + pattern + ']');
        }

        return new RangeRouter(column, pattern);
    }

    private static HexHashRouter createHexHashRouter(String column, String pattern,
            String partitions) {

        // 检查所需的属性
        if (column == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hex-hash' must have 'by-column' property.");
            }

            return null;
        }

        if (partitions == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hex-hash' must have 'partitions' property.");
            }

            return null;
        }

        if (pattern == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hex-hash' must have 'target-pattern' property.");
            }

            return null;
        }

        try {
            int count = Integer.parseInt(partitions);

            // 输出日志
            if (logger.isDebugEnabled()) {
                logger.debug("Creating router 'hex-hash' [by-column = " + column
                        + ", partitions = " + count + ", target-pattern = " + pattern + ']');
            }

            return new HexHashRouter(column, pattern, count);

        } catch (NumberFormatException e) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hex-hash' property 'partitions' must be number.");
            }

            return null;
        }
    }

    private static StringHashCodeRouter createStringHashCodeRouter(String column, String pattern,
            String partitions) {

        // 检查所需的属性
        if (column == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hashcode' must have 'by-column' property.");
            }

            return null;
        }

        if (partitions == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hashcode' must have 'partitions' property.");
            }

            return null;
        }

        if (pattern == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hashcode' must have 'target-pattern' property.");
            }

            return null;
        }

        try {
            int count = Integer.parseInt(partitions);

            // 输出日志
            if (logger.isDebugEnabled()) {
                logger.debug("Creating router 'hashcode' [by-column = " + column
                        + ", partitions = " + count + ", target-pattern = " + pattern + ']');
            }

            return new StringHashCodeRouter(column, pattern, count);

        } catch (NumberFormatException e) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'hashcode' property 'partitions' must be number.");
            }

            return null;
        }
    }

    private static DateRouter createDateRouter(String column, String pattern, String partitions,
            boolean excludeNow) {

        // 输出日志
        if (logger.isDebugEnabled()) {
            logger.debug("try to creating router 'date' [by-column = " + column + ", partitions = "
                    + partitions + ", target-pattern = " + pattern + ']');
        }

        if (partitions == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'date' must have 'partitions' property.");
            }

            return null;
        }

        if (pattern == null) {

            // 输出日志
            if (logger.isErrorEnabled()) {
                logger.error("Router 'date' must have 'target-pattern' property.");
            }

            return null;
        }

        // 输出日志
        if (logger.isDebugEnabled()) {
            logger.debug("Creating router 'date' [by-column = " + column + ", partitions = "
                    + partitions + ", target-pattern = " + pattern + ']');
        }

        return new DateRouter(column, pattern, partitions, excludeNow);

    }
}
