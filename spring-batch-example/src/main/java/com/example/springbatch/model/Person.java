package com.example.springbatch.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Person {

    private int id;
    private String name;
    private String age;
    private String address;
}
