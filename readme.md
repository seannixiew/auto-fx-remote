# 可复测性判据
## 判据
  1 运行中终止。并且再次启动可以启动。
  2 运行中终止，启动下一项可以启动。
  3 运行完毕后，再次启动可以启动。
  4 运行完毕后，启动下一项可以启动。
  5 同时选择多个测试项运行。
## 功能自测试记录
  ModuleFreqSwitchSynSignalAd
  不下程序，采数中终止，再重新运行，可以成功采数。 【1 clear】
  不下程序，采数中终止，再运行AD校正同步信号测试，成功采数。【2 clear】
  ModuleFreqSwitchSynSignalDa
  不下程序，完成跑完流程，重新运行，成功采数。【3 clear】
  不下程序，完整跑完流程，成功重新运行，终止后运行另一个测试项（ModuleCalSynSignalDa），成功采数。 【2 4 clear】
  ModuleCalSynSignalDa
  不下程序，采数中终止，再重新运行，可以成功采数。 【1 clear】

# 代码不一致性备注
  针对vivado相关测试项，可实现断点获取vivado shell句柄
  测试类继承BaseHandler
  注释掉BufferWriter，并修改名称
  当前应用的测试项有：ModuleFreqSwitchSynSignalAd，ModuleFreqSwitchSynSignalDa

  Process错误流读取存在冗余，正确写法也在：ModuleFreqSwitchSynSignalAd，ModuleFreqSwitchSynSignalDa

  采数ILA编号设置为变量，在配置文件中编辑
  只在下面实现：ModuleCalSynSignalDa DA校正同步信号
  
# Bug修复记录
## 已修复
  - DA频率切换校正测试：
    - 不下程序，采数第一组，只采到process0和process3
    > DA1和DA2的下载器顺序接反 

  - DA频率切换校正测试：
    - 下程序，采数，采出来多个文件只有1k大小。
    - 打印的info：ila armed 4个reader都是ila4？
    > 重启Main，不下程序再跑，恢复正常

## 未修复
