package com.relic.numapp;

import com.relic.numapp.utils.RandomNumber;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceTest {

    @Test
    public void compareIntegerAndString() {
        int limit = 2000000;

        List<String> strs = new ArrayList<String>(limit);
        List<Integer> nums = new ArrayList<Integer>(limit);
        for (int i = 0; i < limit; i++) {
            String x = RandomNumber.getNextNumber();
            strs.add(x);
            nums.add(Integer.parseInt(x));
        }

        Map<String, String> strMap = new HashMap<String, String>();
        Map<Integer, Integer> numMap = new HashMap<Integer, Integer>();

        long beg, end;
        beg = System.currentTimeMillis();
        for (Integer item : nums) {
            numMap.put(item, item);
        }
        end = System.currentTimeMillis();
        System.out.println("Integer time spent: " + (end - beg) / 1000.0);

        numMap = new HashMap<Integer, Integer>();
        beg = System.currentTimeMillis();
        for (String item : strs) {
            int x = Integer.parseInt(item);
            numMap.put(x, x);
        }
        end = System.currentTimeMillis();
        System.out.println("String->Integer by parseInt default size time spent: " + (end - beg) / 1000.0);

        numMap = new HashMap<Integer, Integer>(limit);
        beg = System.currentTimeMillis();
        for (String item : strs) {
            int x = Integer.parseInt(item);
            numMap.put(x, x);
        }
        end = System.currentTimeMillis();
        System.out.println("String->Integer by parseInt predefined size time spent: " + (end - beg) / 1000.0);

        numMap = new HashMap<Integer, Integer>(limit);
        beg = System.currentTimeMillis();
        for (String item : strs) {
            Integer x = new Integer(item);
            numMap.put(x, x);
        }
        end = System.currentTimeMillis();
        System.out.println("String->Integer by new time spent: " + (end - beg) / 1000.0);

        /*beg = System.currentTimeMillis();
        for (String item : strs) {
            int x = Integer.decode(item);
            numMap.put(x, x);
        }
        end = System.currentTimeMillis();
        System.out.println("String->Integer by decode time spent: " + (end - beg) / 1000.0);
*/
        beg = System.currentTimeMillis();
        strMap = new HashMap<String, String>();
        for (String item : strs) {
            strMap.put(item, item);
        }
        end = System.currentTimeMillis();
        System.out.println("String by default time spent: " + (end - beg) / 1000.0);

        beg = System.currentTimeMillis();
        strMap = new HashMap<String, String>(limit);
        for (String item : strs) {
            strMap.put(item, item);
        }
        end = System.currentTimeMillis();
        System.out.println("String by predefined size time spent: " + (end - beg) / 1000.0);

    }

    @Test
    public void compareAutoBoxingAndExplicit() {
        int limit = 1000000;

        int[] arrays = new int[limit];
        for (int i = 0; i < limit; i++) {
            String x = RandomNumber.getNextNumber();
            arrays[i] = Integer.parseInt(x);
        }

        List<Integer> integers = new ArrayList<Integer>(limit);

        long beg, end;
        beg = System.currentTimeMillis();
        for (int item : arrays) {
            integers.add(item);
        }
        end = System.currentTimeMillis();
        System.out.println("Autoboxing time spent: " + (end - beg) / 1000.0);

        beg = System.currentTimeMillis();
        for (int item : arrays) {
            integers.add(new Integer(item));
        }
        end = System.currentTimeMillis();
        System.out.println("Explicite time spent: " + (end - beg) / 1000.0);
    }

    @Test
    public void checkIntegerAddress() {
        Integer a = new Integer(1234);
        Integer b = new Integer(1234);

        System.out.println("a: " + a.hashCode() + " b: " + b.hashCode());

        //Result: hashCode is the same, but they are two different objects!
    }
}