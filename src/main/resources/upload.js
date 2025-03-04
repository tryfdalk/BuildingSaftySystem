function uploadFile() {

    console.log("uploadFile function is running!");

    let fileInput = document.getElementById("fileInput");
    let message = document.getElementById("message");

    if (fileInput.files.length === 0) {
        message.innerHTML = "Please select an XML file.";
        return;
    }

    let file = fileInput.files[0];
    let formData = new FormData();
    formData.append("file", file);

    fetch("http://155.207.201.113:8080/upload", {  // Replace with actual server IP
        method: "POST",
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error processing file");
        }
        return response.blob();  // Get the file as a blob
    })
    .then(blob => {
        let downloadUrl = URL.createObjectURL(blob);
        let downloadLink = document.createElement("a");
        downloadLink.href = downloadUrl;
        downloadLink.download = "output.json";
        downloadLink.innerHTML = "Download JSON Result";
        message.innerHTML = "File processed successfully! ";
        message.appendChild(downloadLink);
    })
    .catch(error => {
        message.innerHTML = "Error: " + error.message;
    });
}

