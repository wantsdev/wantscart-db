package com.wantscart.jade.core;

/**
 * User: chuang.zhang
 * Date: 15/10/7
 * Time: 11:33
 */
public class Condition {

//    public static final Option OPTION_AND = new AndOption();
//
//    public static final Option OPTION_OR = new OrOption();
//
//    private List<Entry> conditions;
//
//    private int limit;
//
//    private int offset;
//
//    private boolean desc;
//
//    private String orderBy;
//
//    public Condition() {
//        this.conditions = new LinkedList<Entry>();
//        this.desc = true;
//    }
//
//    public Condition addCondition(String key, Object value) {
//        if (conditions.size() > 0) {
//            conditions.add(OPTION_AND);
//        }
//        conditions.add(new Pair(key, value));
//        return this;
//    }
//
//    public Condition addOrCondition(String key, Object value) {
//        if (conditions.size() > 0) {
//            conditions.add(OPTION_OR);
//        }
//        conditions.add(new Pair(key, value));
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        for(Entry condition : conditions){
//            sb.append(condition.toString());
//        }
//        if(StringUtils.isNotBlank(orderBy)){
//            sb.append(" ORDER BY ").append(orderBy);
//        }
//        if(offset > 0){
//            sb.append(" OFFSET ").append(offset);
//        }
//        if(limit > 0){
//            sb.append(" LIMIT ").append(limit);
//        }
//        return sb.toString();
//    }
//
//    public Condition setLimit(int limit) {
//        this.limit = limit;
//        return this;
//    }
//
//    public int getLimit() {
//        return limit;
//    }
//
//    public int getOffset() {
//        return offset;
//    }
//
//    public Condition setOffset(int offset) {
//        this.offset = offset;
//        return this;
//    }
//
//    public boolean desc() {
//        return desc;
//    }
//
//    public Condition setAsc() {
//        this.desc = false;
//        return this;
//    }
//
//    public String getOrderBy() {
//        return orderBy;
//    }
//
//    public Condition orderBy(String orderBy) {
//        this.orderBy = orderBy;
//        return this;
//    }
//
//
//    public abstract static class Entry {
//        @Override
//        public abstract String toString();
//    }
//
//    public static class Pair extends Entry {
//
//        private String key;
//
//        private Value val;
//
//        public Pair(String key, Object val) {
//            if (!(val instanceof Value)) {
//                val = new EqualValue(val);
//            }
//            this.key = key;
//            this.val = (Value) val;
//        }
//
//        public String getKey() {
//            return key;
//        }
//
//        public void setKey(String key) {
//            this.key = key;
//        }
//
//        public Value getVal() {
//            return val;
//        }
//
//        public void setVal(Value val) {
//            this.val = val;
//        }
//
//        @Override
//        public String toString() {
//            StringBuilder sb = new StringBuilder();
//            sb.append(key).append(val.toString());
//            return sb.toString();
//        }
//    }
//
//    public static class Option extends Entry {
//
//        private String val;
//
//        public String getVal() {
//            return val;
//        }
//
//        public void setVal(String val) {
//            this.val = val;
//        }
//
//        public String toString() {
//            return " " + val;
//        }
//    }
//
//    public static class AndOption extends Option {
//
//        public AndOption() {
//            setVal(" AND ");
//        }
//    }
//
//    public static class OrOption extends Option {
//
//        public OrOption() {
//            setVal(" OR ");
//        }
//    }
//
//    public static class Value {
//
//        private String operator;
//
//        private Object val;
//
//        public Value(String operator, Object val) {
//            this.operator = operator;
//            this.val = val;
//        }
//
//        public Object getVal() {
//            return val;
//        }
//
//        public void setVal(Object val) {
//            this.val = val;
//        }
//
//        public String getOperator() {
//            return operator;
//        }
//
//        @Override
//        public String toString() {
//            return
//        }
//    }
//
//    public static class EqualValue extends Value {
//
//        public EqualValue(Object val) {
//            super("=", val);
//        }
//    }
//
//    public static class LikeValue extends Value {
//
//        public LikeValue(Object val) {
//            super("LIKE", "%" + val.toString() + "%");
//        }
//    }
//
//    public static class MoreValue extends Value {
//
//        public MoreValue(Object val) {
//            super(">", val);
//        }
//    }
//
//    public static class LessValue extends Value {
//
//        public LessValue(Object val) {
//            super("<", val);
//        }
//    }
//
//    public static class InValue extends Value {
//
//        public InValue(Object val) {
//            super("IN", val);
//        }
//    }
}
