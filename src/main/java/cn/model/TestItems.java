package cn.model;

import java.util.Arrays;
import java.util.List;

// TODO: 2024/1/11 handlerName大量未映射
public class TestItems {

    /** 射频组 */

    public static final TestItemModel tx=new TestItemModel(-1,"RF-发射");

    public static final TestItemModel rx=new TestItemModel(-1,"RF-接收");

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

    /** DBF组 */

    public static final TestItemModel ad=new TestItemModel(-1,"AD板");

    public static final TestItemModel da=new TestItemModel(-1,"DA板");

    public static final TestItemModel ad40FuncAndPerformance=new TestItemModel(13,"AD功率线性度测试（40路）","cn.handler.ad.PowerAndLinearityAd");

    public static final TestItemModel adTestDAFuncAndPerformance=new TestItemModel(14,"测试DA接口功能及性能测试","cn.handler.ad.DaForTest");

    public static final TestItemModel ad40Isolation=new TestItemModel(15,"AD隔离度测试（40路）","cn.handler.ad.IsolationAd");

    public static final TestItemModel adInModuleSequenceStability=new TestItemModel(17,"整机-AD时序稳定性","cn.handler.ad.ModuleSequenceStabilityAd");

    public static final TestItemModel adInModuleCalSynSignal=new TestItemModel(18,"整机-AD校正同步信号测试","cn.handler.ad.ModuleCalSynSignalAd");

    public static final TestItemModel adInModuleConsistency=new TestItemModel(19,"整机-AD通道间一致性","cn.handler.ad.ModuleConsistencyAd");

    public static final TestItemModel adInModulePpsSyn=new TestItemModel(20,"整机-PPS板间同步","cn.handler.ad.ModulePpsSynAd");

    public static final TestItemModel adInModuleFreqSwitchSynSignal=new TestItemModel(25,"整机-AD频率切换同步信号测试","cn.handler.ad.ModuleFreqSwitchSynSignalAd");

    public static final TestItemModel da40FuncAndPerformanceAndIso=new TestItemModel(21,"40路DA功能及性能、隔离度测试");

    public static final TestItemModel daTestADFuncAndPerformance=new TestItemModel(22,"测试AD接口功能及性能测试");

    public static final TestItemModel da40Power=new TestItemModel(23,"40路DA输出功率测试");

    public static final TestItemModel da40Consistency=new TestItemModel(24,"40路DA一致性测试");

    public static final TestItemModel daInModuleCalSynSignal=new TestItemModel(25,"整机-DA校正同步信号测试","cn.handler.da.ModuleCalSynSignalDa");

    public static final TestItemModel daInModuleFreqSwitchSynSignal=new TestItemModel(25,"整机-DA频率切换同步信号测试","cn.handler.da.ModuleFreqSwitchSynSignalDa");

    /** 系统组 */

    public static final TestItemModel dbf=new TestItemModel(-1,"系统联测");

    public static final TestItemModel daAndRf=new TestItemModel(25,"DA板+RF");

    public static final TestItemModel adAndRf=new TestItemModel(26,"AD板+RF");

    public static final TestItemModel dbfAndRfTxPower=new TestItemModel(27,"DBF+RF-发射功率","cn.handler.union.PowerTx");

    public static final TestItemModel dbfAndRfTxConsistency=new TestItemModel(28,"DBF+RF-发射一致性","cn.handler.union.ConsistencyTx");

    public static final TestItemModel dbfAndRfRxConsistency=new TestItemModel(29,"DBF+RF-接收一致性","cn.handler.union.ConsistencyRx");


    /***********************************************************************************************/



//    List<TestItemModel> list= Arrays.asList();

}
