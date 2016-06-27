package com.jonasotto.virtualkey2;

import java.util.ArrayList;

/**
 * Created by jonas on 23.06.16.
 */
public class IPAddress  extends ArrayList<Byte>{

    public IPAddress(byte[] byteArray){
        for(byte b:byteArray){
            this.add(b);
        }
    }

    public IPAddress(String ipString){
        String[] elements = ipString.split("[.]");
        for(int i = 0; i < 4; i++){
            byte currentByte = (byte) Integer.parseInt(elements[3-i]);
            this.add(currentByte);
        }
    }

    @Override
    public String toString(){
        String result = "";
        for(int i = 0; i < this.size(); i++){
            result += Integer.toString(unsignedToBytes(this.get(this.size() - i - 1))) + ".";
        }
        return result.substring(0, result.length()-1);
    }
    private static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }


}
