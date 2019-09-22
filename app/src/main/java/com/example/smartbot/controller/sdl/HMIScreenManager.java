package com.example.smartbot.controller.sdl;

import com.smartdevicelink.proxy.rpc.Alert;

public class HMIScreenManager {
    //Singleton
    private static HMIScreenManager INSTANCE;

    public static HMIScreenManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HMIScreenManager();
        return INSTANCE;
    }

    public void showAlert(String text){
        Alert alert = new Alert();
        alert.setAlertText1(text);
        alert.setDuration(5000);
        Config.sdlManager.sendRPC(alert);
    }

    public void showTest(String text){
        Config.sdlManager.getScreenManager().beginTransaction();
        Config.sdlManager.getScreenManager().setTextField1(text);
        Config.sdlManager.getScreenManager().setTextField2("");
        Config.sdlManager.getScreenManager().commit(null);
        //sdlManager.sendRPC(new Speak(TTSChunkFactory.createSimpleTTSChunks(TEST_COMMAND_NAME)));
    }
}