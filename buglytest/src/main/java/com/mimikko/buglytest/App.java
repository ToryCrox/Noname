package com.mimikko.buglytest;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * @author xutao
 * @date 2018/6/13
 */

public class App extends TinkerApplication {

    public App(){
        super(ShareConstants.TINKER_ENABLE_ALL, "com.mimikko.buglytest.AppLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }
}
