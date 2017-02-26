package com.handysolutions.casey.functional;

public class Holder<T> {
    private T value;
    public Holder() {
    }
    public Holder(T value) {
        this.value = value;
    }
    public void set(T val){
        value=val;
    }
    public T get(){
        return value;
    }
    public boolean isPresent(){
        return value!=null;
    }
}