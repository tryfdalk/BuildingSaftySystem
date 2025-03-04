package com.project.BuildingSaftySystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class XMLHandler {

    private String content;
    private String folderPath;
    private String fileName;

    public XMLHandler(String content1){
        this.content = content1;
        this.folderPath = "/home/serveruser/SavedXMLs";
        //this.folderPath = "/home/trifon/SavedXMLs";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        this.fileName = formattedNow + ".xml";
        System.out.println(fileName);
    }

    public void SaveFile(){
        try{
            Path folder = Paths.get(folderPath);
            if(!Files.exists(folder)){
                Files.createDirectories(folder);
            }

            Path filePath = folder.resolve(fileName);
            Files.write(filePath, content.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
