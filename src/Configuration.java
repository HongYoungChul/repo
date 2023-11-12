import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {
    private static final String CONFIG_FILE_PATH = "server_info.dat"; // 설정 파일 경로, 필요에 따라 수정
    private String host;
    private int port;

    public Configuration() {
        readConfiguration();
    }

    private void readConfiguration() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("host=")) {
                    host = line.substring("host=".length());
                } else if (line.startsWith("port=")) {
                    port = Integer.parseInt(line.substring("port=".length()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (host == null || port == 0) {
            throw new RuntimeException("잘못된 설정");
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
