package cn.controllers.root;

import cn.utils.ControllersManager;

public class RootController {

    public RootController(){
        //初始化FXML时保存当前Controller实例
        ControllersManager.CONTROLLERS.put(this.getClass().getSimpleName(), this);
        System.out.println(this.getClass().getSimpleName()+"已被创建。");
    }
}
