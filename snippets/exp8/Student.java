package edu.birzeit.encs5150.exp8;

/** One student, with the same fields as one object in the JSON array. */
public class Student {

    private final int id;
    private final String name;
    private final int age;

    public Student(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return id + ". " + name + ", " + age;
    }
}
