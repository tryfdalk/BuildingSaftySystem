package com.project.BuildingSaftySystem;

import DijkstraAlgorithm.Dijkstra;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


@Controller
//@RequestMapping("/api")
//@RestController
public class BuildingSaftySystemController {
    @PostMapping("/upload")
    public ResponseEntity<String> uploadXml(@RequestParam("file") MultipartFile file) {
        try {
            // Read the contents of the XML file
            String xmlContent = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Log the XML content to the console (or process it as needed)
            System.out.println("Received XML File:\n");
            //System.out.println(xmlContent);

            Dijkstra dj = new Dijkstra(xmlContent);
            dj.createGraph();
            JSONObject jsonObj = dj.createJSON();

            XMLHandler obj = new XMLHandler(xmlContent);
            obj.SaveFile();

            // Respond to the client with a success message
            return ResponseEntity.ok("XML File received and processed successfully!");
        } catch (Exception e) {
            // Handle any errors
            e.printStackTrace();
            return ResponseEntity.status(400).body("Error processing the file: " + e.getMessage());
        }
    }
}
