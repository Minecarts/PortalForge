package com.minecarts.portalforge.portal;


public class UnknownPortal extends BasePortal {

    @Override
    public void onTouch(){
        System.out.println("Touched unknown portal");
    }
    
    public void onPortal(){
        System.out.println("Stood in unknown portal");
    }
}
