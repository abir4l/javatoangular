package com.generator;

import com.sun.deploy.util.StringUtils;
import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Created by abiral on 12/22/17.
 */
public class Generator {


    private String genpath = "test/";
    private String path;
    private String packagePath;


    public Generator(String path) {
        this.path = path;
        this.packagePath = path.substring(path.indexOf("com"));
        this.packagePath = StringUtils.join(
                Arrays.asList(packagePath.split("/")),
                "."
        );

        try {
            printFields(this.path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//to use generator uncomment this method and run this file only

    public static void main(String[] args) {

        new Generator(JOptionPane.showInputDialog("Enter Package Path (E.g /home/java/model/entity)"));
    }


    public void printFields(String path) throws IOException {
        List<String> files = getNameList(path);
        for (String fileName : files) {

            Map<String, String> fields = getAllFields(fileName);
            createTypeScriptFiles(fileName, fields);

        }


    }

    public List<String> getNameList(String path) throws IOException {

        List<String> files = new ArrayList<>();
        for (File file : new File(path).listFiles()) files.add(file.getName().split("\\.")[0]);
        return files;

    }

    public Map<String, String> getAllFields(String fileName) {
        Map<String, String> fieldWithType = new HashMap<>();
        URL url = null;
        try {

            URL[] urls = new URL[]{};
            ClassLoader urlClassLoader = new URLClassLoader(urls);
            Class cls = urlClassLoader.loadClass(packagePath + "." + fileName); // adding filename to it
            Field[] fields = cls.getDeclaredFields();
            for (Field fld : fields) {
                if (Collection.class.isAssignableFrom(fld.getType())) {

                    System.out.println(fld.getName());
                    String type = fld.getGenericType().toString();
                    String[] typeName = type.split("\\.");
                    type = typeName[typeName.length - 1];
                    type = type.substring(0, type.length() - 1);
                    if (type.equalsIgnoreCase("integer")) {
                        type = "number";
                    } else if (type.equalsIgnoreCase("string")) {
                        type = "string";
                    }

                    type += "[]";

                    fieldWithType.put(fld.getName(), type);

                } else {
                    if (fld.getType().getSimpleName().toString().equalsIgnoreCase("integer")) {
                        fieldWithType.put(fld.getName(), "number");
                    } else if (fld.getType().getSimpleName().toString().equalsIgnoreCase("string")) {
                        fieldWithType.put(fld.getName(), "string");
                    } else
                        fieldWithType.put(fld.getName(), fld.getType().getSimpleName());

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Class Not Found");
            System.out.println(fileName);
            System.out.println(path);
            System.out.println(packagePath);
        }

        return fieldWithType;
    }

    public void createTypeScriptFiles(String fileName, Map<String, String> fields) {

        File file = new File(this.genpath + fileName.toLowerCase() + ".model.ts");
        try {
            file.createNewFile();
            String fieldData = "";
            for (Map.Entry<String, String> data : fields.entrySet()) {
                fieldData += data.getKey() + " : " + data.getValue() + ";\n";
            }
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);
            pw.print(getClassBody(fileName, fieldData));
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClassBody(String fileName, String fields) {

        String data = String.format("export class %s extends Base{\n\n%s\n}", fileName, fields);

        return data;
    }


}

