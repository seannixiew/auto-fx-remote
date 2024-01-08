package cn.model;

public class TestItemModel {

    int id;
    String testItemName;
    String handlerName;

    public TestItemModel(int id, String testItemName) {
        this.id = id;
        this.testItemName = testItemName;
    }

    public TestItemModel(int id, String testItemName, String handlerName) {
        this.id = id;
        this.testItemName = testItemName;
        this.handlerName = handlerName;
    }

    public String getName(){
        return testItemName;
    }

    public int getId() {
        return id;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    @Override
        public String toString() {
            return testItemName;
        }
    }








