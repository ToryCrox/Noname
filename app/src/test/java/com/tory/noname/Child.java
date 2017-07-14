package com.tory.noname;

class Parent {

    int x = 10;

    public Parent() {
        System.out.println("Parent x=" + x);
        add(2);
    }

    void add(int y) {
        x += y;
    }
}


public class Child extends Parent {
    int x = 9;

    public Child() {
        System.out.println("Child x=" + x);
    }

    void add(int y) {
        System.out.println("Child.add x=" + x);
        x += y;
    }


    public static void main(String[] args) {
        Parent p = new Child();
        System.out.println(p.x);
    }
}