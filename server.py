#!/opt/homebrew/bin/python3
from http.server import HTTPServer, BaseHTTPRequestHandler


class MyServer(BaseHTTPRequestHandler):

    def do_GET(self):
        if self.path == '/':
            self.path = '/index.html'
        print(self.path)
        root_dir = "/Users/yzhu/workspace/repo/COEN317/COEN317"
        file_dir = root_dir + self.path
        print(file_dir)
        try:
            response = open(file_dir).read()
            self.send_response(200)
        except:
            response = "404 File Not Found"
            self.send_response(404)
        self.end_headers()
        self.wfile.write(bytes(response, 'utf-8'))


if __name__ == "__main__":
    httpd = HTTPServer(('localhost', 8088), MyServer)
    httpd.serve_forever()