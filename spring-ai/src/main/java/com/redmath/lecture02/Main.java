package com.redmath.lecture02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
public class Main {

    static{
        init();
    }

    static void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.US);
    }

    static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
