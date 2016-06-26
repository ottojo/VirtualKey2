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

    @Override
    public String toString(){
        /*String result = this.get(this.size() - 1).toString();
        for(int i = this.size() - 2; i < this.size(); i++){
            result += "." + this.get(this.size() - i);
        }*/
        String result = "";
        for(Byte b:this){
            result += b.toString() + ".";
        }

        return result.substring(0, result.length()-2);
    }
}
