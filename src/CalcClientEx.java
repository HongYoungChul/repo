import java.io.*;
import java.net.*;
import java.util.*;

public class CalcClientEx {

    public static void main(String[] args) {
        Configuration config = new Configuration(); //외부 설정 파일을 참조하여 서버 접속 할 수 있도록 Configuration Class 참조

        //in, out 버퍼와 Socket 통신을 위한 변수 설정
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);

        try {
            socket = new Socket(config.getHost(), config.getPort());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (true) {  // 연속으로 사용자 입력을 받을 수 있도록 루프
                System.out.print("계산식을 입력하세요 (예: add 24 42)>> ");
                String outputMessage = scanner.nextLine();

                if (outputMessage.equalsIgnoreCase("bye")) {
                    out.write(outputMessage + "\n");
                    out.flush();
                    break;
                } else {
                    out.write(outputMessage + "\n");
                    out.flush();

                    String response = in.readLine();
                    if (response.startsWith("ERR")) {      // ERR 헤더로 response 받는 경우 에러 처리
                        handleErrorMessage(response);
                    } else if (response.startsWith("ANS")) {    // ANS 헤더로 response 받는 경우 계산 결과 출력
                        handleAnswerMessage(response);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.out.println("서버와 통신 중 오류가 발생했습니다.");
            }
        }
    }

    private static void handleAnswerMessage(String answerMessage) {
        String[] parts = answerMessage.split(" ");
        if (parts.length == 2) {
            int result = Integer.parseInt(parts[1]);
            System.out.println("계산 결과: " + result);
        }
    }

    private static void handleErrorMessage(String errorMessage) {
        String[] parts = errorMessage.split(" ");
        if (parts.length == 2) {
            int errorCode = Integer.parseInt(parts[1]);
            switch (errorCode) {
                case 1:
                    System.out.println("오류: 잘못된 메소드입니다.");
                    break;
                case 2:
                    System.out.println("오류: 올바른 형식의 피연산자 수가 아닙니다.");
                    break;
                case 3:
                    System.out.println("오류: 0으로 나눌 수 없습니다.");
                    break;
                default:
                    System.out.println("오류: 기타 오류가 발생했습니다.");
            }
        }
    }
}
