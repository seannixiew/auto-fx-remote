package cn.handler.base;

import java.io.BufferedWriter;

public class BaseHandler {

    /**
     * 针对vivado相关测试项，可实现断点获取vivado shell句柄
     * 测试类继承BaseHandler
     * 注释掉BufferWriter，并修改名称
     * 当前应用的测试项有：ModuleFreqSwitchSynSignalAd，ModuleFreqSwitchSynSignalDa
     * 题外话：Process错误流读取存在冗余，正确写法也在上面两项
     */

    public BufferedWriter processOutput0;

    public BufferedWriter processOutput1;

    public BufferedWriter processOutput2;

    public BufferedWriter processOutput3;

}
