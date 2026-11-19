package edu.birzeit.encs5150.exp8;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Turns JSON text into Student objects, and a new student into JSON. */
public class StudentJsonParser {

    // [ {"id": 1, "name": "…", "age": 21}, … ]  ->  a List of Students
    public static List<Student> fromJsonArray(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            students.add(fromJson(array.getJSONObject(i)));
        }
        return students;
    }

    // {"id": 1, "name": "…", "age": 21}  ->  one Student
    public static Student fromJson(JSONObject object) throws JSONException {
        return new Student(object.getInt("id"), object.getString("name"), object.getInt("age"));
    }

    // name and age  ->  {"name": "…", "age": 21}. The server chooses the id.
    public static JSONObject toJson(String name, int age) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("age", age);
        return object;
    }
}
