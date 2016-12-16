package ru.kpfu.group11501.airhockey.net;

import java.util.HashMap;

/**
 * @author Oleg Shatin
 *         11-501
 *         enum decribes convention about a list of net methods-commands from server to client and from client to server
 *         this enum can allow call method by name of item using reflection
 */
public enum NetMethod {

    //for client and for server
    updatePuckDirection("UPD", new Class[]{Double.class, Double.class}, Object.class),
    updateGameScore("UGS", new Class[]{Integer.class, Integer.class}, Object.class),

    //messages for client
    updateOpponentMalletDirection("UOD", new Class[]{Double.class, Double.class}, Client.class),
    setGameResult("SGR", new Class[]{Integer.class, Integer.class}, Client.class),
    gameStartsInTime("GST", new Class[]{Long.class}, Client.class),
    opponentIsReady("OIR", null, Client.class),
    opponentLeftGame("OLG", null, Client.class),

    //messages for server
    updateClientMalletDirection("UCD", new Class[]{Double.class, Double.class}, Server.class),
    clientIsReady("CIR", null, Server.class),
    clientAsksGame("CAG", null, Server.class),
    clientLeavesGame("CLG", null, Server.class),
    ;
    //Hash map to get method by code
    private static HashMap<String, NetMethod> codeMap;
    static {
        codeMap = new HashMap<>(NetMethod.values().length);
        for (NetMethod method  : NetMethod.values()){
            codeMap.put(method.code, method);
        }
    }
    public static NetMethod getMethod(String code){
        return codeMap.get(code);
    }
    
    /**
     * @code - net code - 3 characters, that will be sent to receiver.
     * @argsClasses - list of classes that will be arguments in this method
     * @implementer - a class that should to implement this method
     * */
    private String code;
    private Class[] argsClasses;
    private Class implementer;

    NetMethod(String code, Class[] argsClasses, Class implementer) {
        this.code = code;
        this.argsClasses = argsClasses;
        this.implementer = implementer;
    }

    public String getCode() {
        return code;
    }

    public Class[] getArgsClasses() {
        return argsClasses;
    }

    public Class getImplementer() {
        return implementer;
    }
    
}
