# 自测记录
## 可复测判据
  1. **运行中终止。并且再次启动可以启动。**（常用场景）
  2. 运行中终止，启动下一项可以启动。
  3. 运行完毕后，再次启动可以启动。
  4. **同时选择多个测试项运行。**（常用场景）

## 功能自测试记录
  ModuleFreqSwitchSynSignalAd
  不下程序，采数中终止，再重新运行，可以成功采数。 【1 clear】
  不下程序，采数中终止，再运行AD校正同步信号测试，成功采数。【2 clear】

  ModuleFreqSwitchSynSignalDa
  不下程序，完成跑完流程，重新运行，成功采数。【3 clear】
  不下程序，完整跑完流程，成功重新运行，终止后运行另一个测试项（ModuleCalSynSignalDa），成功采数。 【2 4 clear】

  ModuleCalSynSignalDa
  不下程序，采数中终止，再重新运行，可以成功采数。 【1 clear】

  ModuleCalSynSignalAd
  下程序，测试到一半终止，不下程序复测，成功采数。【1 clear】
  复选ModuleCalSynSignalAd和ModuleFreqSwitchSynSignalAd，先跑前者。 【4 clear】

  ModuleSequenceStabilityAd
  不下程序，测试到一半终止，再复测，成功采数。【1 clear】
  不下程序，注入error，终止，再复测，成功运行。【1 clear】

  ModulePpsSynAd
  不下程序，测试到一半终止，再复测，成功截图。【1 clear】

  DaForTest
  下载程序，测试到一半终止，再复测不下载程序，成功采数。【1 clear】
  
  IsolationAd
  下载程序，测试到一半终止，再复测不下载程序，成功采数。【1 clear】
  
  PowerAndLinearityAd
  下载程序，测试到一半终止，再复测不下载程序，成功采数。【1 clear】
  
# 代码不一致性备注
  针对vivado相关测试项，可实现断点获取vivado shell句柄
  测试类继承BaseHandler
  注释掉BufferWriter，并修改名称
  当前应用的测试项有：ModuleFreqSwitchSynSignalAd，ModuleFreqSwitchSynSignalDa

  Process错误流读取存在冗余，正确写法也在：ModuleFreqSwitchSynSignalAd，ModuleFreqSwitchSynSignalDa

  采数ILA编号设置为变量，在配置文件中编辑
  只在下面实现：ModuleCalSynSignalDa DA校正同步信号

  *error打印，只在ModuleSequenceStabilityAd实现。有必要全部实现。*
  
# Bug修复记录
## 已修复

`2024.04.27`
  - DA频率切换校正测试：
    - 不下程序，采数第一组，只采到process0和process3
    > DA1和DA2的下载器顺序接反 

`2024.04.27`
  - DA频率切换校正测试：
    - 下程序，采数，采出来多个文件只有1k大小。
    - 打印的info：ila armed 4个reader都是ila4？
    > 重启Main，不下程序再跑，恢复正常

## 未修复
- DBF
  - <font style=color:red> 单独下程序，调通，确保好用  </font>
  - <font style=color:red> 最后一个采数不全问题</font>
  - <font style=color:red> AD稳定性终止bug</font>
  - <font style=color:red> 移植程序</font>
- union
  - <font style=color:red> 推功率完善</font>
  - <font style=color:red> 测幅相完善</font>

# 用户使用提示
- 复选测试项，终止一次只终止一项，过3s再点下一次。
- 复选测试项，如果选中下载FPGA，那么每个测试项都会下载，建议下载程序时单跑一次。