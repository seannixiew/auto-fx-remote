package cn.model;

import java.util.Arrays;
import java.util.List;

public class TestItems {

    public static final TestItemModel txPowerInit=new TestItemModel(0,"功率标定","cn.handler.tx.PowerInit");

    public static final TestItemModel txEvm=new TestItemModel(1,"EVM");

    public static final TestItemModel txNpr=new TestItemModel(2,"NPR");

    public static final TestItemModel txFlatness=new TestItemModel(3,"大小功率平坦度");

    public static final TestItemModel txSuppresionOutIf=new TestItemModel(4,"中频带外抑制");

    public static final TestItemModel txNoiseOutBand=new TestItemModel(5,"带外噪声谱");

    public static final TestItemModel txConsisAmongChannels=new TestItemModel(6,"通道间幅相一致性");

    public static final TestItemModel rxP1dB=new TestItemModel(7,"P1dB");

    public static final TestItemModel rxGainAndHarmo=new TestItemModel(8,"增益及谐波");

    public static final TestItemModel rxSuppresionOutIf=new TestItemModel(9,"中频带外抑制");

    public static final TestItemModel rx3rdIntercept=new TestItemModel(10,"三阶交调");

    public static final TestItemModel rxFlatness=new TestItemModel(11,"平坦度");

    public static final TestItemModel rxConsisAmongChannels=new TestItemModel(12,"通道间幅相一致性");

    public static final TestItemModel dbfBoard=new TestItemModel(13,"DBF单板");

    public static final TestItemModel dbfMachine=new TestItemModel(14,"DBF整机");

    public static final TestItemModel tx=new TestItemModel(-1,"RF-发射");

    public static final TestItemModel rx=new TestItemModel(-1,"RF-接收");

    public static final TestItemModel dbf=new TestItemModel(-1,"DBF-联测");

    List<TestItemModel> list= Arrays.asList(txPowerInit,txEvm,txNpr,txFlatness,txSuppresionOutIf,txNoiseOutBand,txConsisAmongChannels,
            rxP1dB,rxGainAndHarmo,rxSuppresionOutIf,rx3rdIntercept,rxFlatness,rxConsisAmongChannels);

}
