package com.project.BuildingSaftySystem;

import DijkstraAlgorithm.Dijkstra;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<Resource> uploadXml(@RequestParam("file") MultipartFile file) {
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

            byte[] jsonBytes = jsonObj.toString(4).getBytes(StandardCharsets.UTF_8);
            ByteArrayResource resource = new ByteArrayResource(jsonBytes);

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "output.json");

            return ResponseEntity.ok().headers(headers).contentLength(jsonBytes.length).body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
