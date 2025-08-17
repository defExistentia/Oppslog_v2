package org.opslog;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main {
    public static void main(String[] args){
        System.out.println("Starting application backend");
        Quarkus.run(args);
    }
}