package com.example.mcard.FunctionalInterfaces;

public interface NetworkConnection
{
    boolean checkNetwork() throws InterruptedException;
    default void networkListenerRegister() { }
    default void networkListenerUnregister() { }
}
