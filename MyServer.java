import java.net.*;
import java.io.*;
import java.nio.file.*;



public class MyServer {
    public static void main(String[] args) throws Exception {
        String root_path;
        int port;
        String[] parsedArgs = parseCommandLineArgs(args);

        root_path = parsedArgs[0];
        port = Integer.parseInt(parsedArgs[1]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    threadWork(client, root_path);
                }
            }
        }
    }


    private static String[] parseCommandLineArgs(String[] args){
        String root_path=" ", port=" ";
        for (int i=0; i < args.length; i++){
            if (args[i].equals("-document_root")){
                root_path = args[i+1];
            }
            else if (args[i].equals("-port")) {
                port = args[i+1];
            }
        }
        return new String[]{root_path, port};
    }
    private static void threadWork(Socket client, String root_path) throws IOException {
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

        Path filePath = getFilePath(path, root_path);


        if (Files.exists(filePath)) {
            String contentType = getContentType(filePath);
            sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
        } else {
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


    private static Path getFilePath(String path, String root_path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        String abs_path = root_path + path;
        return Paths.get(abs_path);
    }
}
