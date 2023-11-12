import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 연산 처리를 위해 interface 형태를 구성하고 각 사칙연산 class에서 implement를 통해 연산 기능을 구현
interface Operation {
    int calculate(int op1, int op2);
}

// 덧셈
class AdditionOperation implements Operation {
    @Override
    public int calculate(int op1, int op2) {
        return op1 + op2;
    }
}

// 뺄셈
class SubtractionOperation implements Operation {
    @Override
    public int calculate(int op1, int op2) {
        return op1 - op2;
    }
}

// 곱셈
class MultiplicationOperation implements Operation {
    @Override
    public int calculate(int op1, int op2) {
        return op1 * op2;
    }
}

// 나눗셈, 0으로 나누는 경우 예외 처리
class DivisionOperation implements Operation {
    @Override
    public int calculate(int op1, int op2) {
        if (op2 == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return op1 / op2;
    }
}

public class CalcServerEx {

    private static final int THREAD_POOL_SIZE = 10; // 최대 동시 처리 스레드 수

    //연산자를 메소드로 처리하고(프로토콜 형식) operand를 전달
    public static String calc(Operation operation, int op1, int op2) {
        return Integer.toString(operation.calculate(op1, op2));
    }

    public static void main(String[] args) {
        Configuration config = new Configuration(); //외부 설정값을 참조하여 서버 연결을 설정하기 위해 별도의 class로 구현

        ServerSocket serverSocket = null;
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            serverSocket = new ServerSocket(config.getPort());
            System.out.println("서버가 시작되었습니다.");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("클라이언트가 연결되었습니다.");

                // 클라이언트 연결을 처리하는 스레드 생성 및 실행
                executorService.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage());
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 스레드 풀 종료
            executorService.shutdown();
        }
    }
}

class ClientHandler implements Runnable {

    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        BufferedWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while (true) {  // 클라이언트와 소켓 연결이 끊기더라도 종료되지 않도록
                String inputMessage = in.readLine();
                if (inputMessage == null || inputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("클라이언트에서 연결을 종료하였음");
                    break;
                } else {
                    StringTokenizer st = new StringTokenizer(inputMessage, " ");    //공백을 이용하여 tokenize
                    if (st.countTokens() != 3) {
                        sendErrorMessage(out, 2); // Invalid # operands 오류 처리, 오류 코드: 2
                    } else {
                        String operator = st.nextToken();
                        int operand1 = Integer.parseInt(st.nextToken());
                        int operand2 = Integer.parseInt(st.nextToken());

                        Operation operation;
                        switch (operator) {     // 각 case를 method로 판단, 처리
                            case "add": case "ADD":
                                operation = new AdditionOperation();
                                break;
                            case "sub": case "SUB":
                                operation = new SubtractionOperation();
                                break;
                            case "mul": case "MUL":
                                operation = new MultiplicationOperation();
                                break;
                            case "div": case "DIV":
                                operation = new DivisionOperation();
                                break;
                            default:
                                sendErrorMessage(out, 1); // Invalid method 오류 처리, 오류 코드: 1
                                continue;
                        }

                        try {
                            out.write("ANS " + CalcServerEx.calc(operation, operand1, operand2) + "\n");
                        } catch (ArithmeticException e) {
                            sendErrorMessage(out, 3); // Division by zero 오류 처리, 오류 코드: 3
                            continue;
                        }
                    }
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("클라이언트와 통신 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                if (clientSocket != null && !clientSocket.isClosed())
                    clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendErrorMessage(BufferedWriter out, int errorCode) throws IOException {
        out.write("ERR " + errorCode + "\n");   // 오류 발생 시 response header ERR과 에러 코드 전송
        out.flush();
    }
}
