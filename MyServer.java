import java.net.*;
import java.io.*;
import java.nio.file.*;



public class MyServer {
    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(8088)) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    threadWork(client);
                }
            }
        }
    }

    private static void threadWork(Socket client) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }
        String request = requestBuilder.toString();
        String[] splitedRequest = request.split("\r\n");
        String[] firstLine = splitedRequest[0].split("\\s+");
        String method = firstLine[0];
        String path = firstLine[1];

        Path filePath = getFilePath(path);


        if (Files.exists(filePath)) {
            String contentType = getContentType(filePath);
            sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
        } else {
            byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
            sendResponse(client, "404 Not Found", "text/html", "<h1><b>404 Not Found</b></h1>".getBytes());
        }

    }

    private static void sendResponse(Socket client, String status, String contentType, byte[] content)
            throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.0 "+status+"\r\n").getBytes());
        clientOutput.write(("ContentType: "+contentType+"\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    private static String getContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }


    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        path = "/Users/yzhu/workspace/repo/COEN317/COEN317" + path;
        return Paths.get(path);
    }
}
