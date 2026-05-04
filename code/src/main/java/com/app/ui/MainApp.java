package com.app.ui;

import com.app.dao.ProblemDAO;
import com.app.dao.TestCaseDAO;
import com.app.entity.Problem;
import com.app.entity.TestCase;
import com.app.entity.EvaluationResult;
import com.app.executor.CodeExecutionService;
import com.app.service.AiService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private TextArea problemArea;
    private TableView<TestCase> testCaseTable;
    private TextArea codeArea;
    private ComboBox<String> langCombo;
    private TextArea consoleArea;

    private AiService aiService;
    private CodeExecutionService executionService;
    private ProblemDAO problemDAO;
    private TestCaseDAO testCaseDAO;

    private Problem currentProblem;

    @Override
    public void start(Stage primaryStage) {
        // Init Services
        aiService = new AiService();
        executionService = new CodeExecutionService();
        problemDAO = new ProblemDAO();
        testCaseDAO = new TestCaseDAO();

        primaryStage.setTitle("JudgeAI - AI Assisted Programming Judge System");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 1. LEFT PANEL: Nhập Đề Bài
        VBox leftBox = new VBox(10);
        leftBox.setPrefWidth(300);
        Label lblProblem = new Label("Nội dung đề bài:");
        problemArea = new TextArea();
        problemArea.setWrapText(true);
        problemArea.setPrefRowCount(15);
        Button btnUploadImg = new Button("📸 Upload Ảnh (OCR)");
        btnUploadImg.setOnAction(e -> log("Tính năng Vision OCR: Cần đường dẫn file thực tế..."));

        Button btnGenTest = new Button("🤖 Phân tích & Sinh Testcase");
        btnGenTest.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnGenTest.setOnAction(e -> handleGenerateTestCases());

        leftBox.getChildren().addAll(lblProblem, problemArea, btnUploadImg, btnGenTest);
        root.setLeft(leftBox);

        // 2. CENTER PANEL: Bảng TestCase
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(0, 10, 0, 10));
        Label lblTestCases = new Label("Danh sách Testcase sinh ra:");
        testCaseTable = new TableView<>();
        
        TableColumn<TestCase, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        
        TableColumn<TestCase, String> colInput = new TableColumn<>("Input");
        colInput.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInputData()));
        
        TableColumn<TestCase, String> colOutput = new TableColumn<>("Expected Output");
        colOutput.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpectedOutput()));
        
        testCaseTable.getColumns().addAll(colId, colInput, colOutput);
        centerBox.getChildren().addAll(lblTestCases, testCaseTable);
        root.setCenter(centerBox);

        // 3. RIGHT PANEL: Nhập Code & Chấm
        VBox rightBox = new VBox(10);
        rightBox.setPrefWidth(350);
        Label lblCode = new Label("Mã nguồn (Submission):");
        
        HBox langBox = new HBox(10);
        langBox.getChildren().add(new Label("Ngôn ngữ: "));
        langCombo = new ComboBox<>();
        langCombo.getItems().addAll("JAVA", "CPP");
        langCombo.setValue("JAVA");
        langBox.getChildren().add(langCombo);

        Button btnGenCode = new Button("✨ AI Tự Sinh Code (AC)");
        btnGenCode.setOnAction(e -> handleGenerateCode());

        codeArea = new TextArea();
        codeArea.setPrefRowCount(20);

        Button btnRun = new Button("🚀 Chạy Thử Nghiệm (Execution Engine)");
        btnRun.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        btnRun.setOnAction(e -> handleRunExecution());

        rightBox.getChildren().addAll(lblCode, langBox, btnGenCode, codeArea, btnRun);
        root.setRight(rightBox);

        // 4. BOTTOM PANEL: Console Log
        VBox bottomBox = new VBox(5);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        bottomBox.getChildren().add(new Label("Console Log:"));
        consoleArea = new TextArea();
        consoleArea.setEditable(false);
        consoleArea.setPrefRowCount(8);
        consoleArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: green; -fx-font-family: monospace;");
        bottomBox.getChildren().add(consoleArea);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleGenerateTestCases() {
        String probText = problemArea.getText().trim();
        if (probText.isEmpty()) {
            log("Lỗi: Vui lòng nhập đề bài trước!");
            return;
        }

        log("Đang gọi AI API để phân tích đề và sinh Testcase (Đợi hệ thống xử lý...)");
        
        // Chạy ngầm tránh block giao diện
        new Thread(() -> {
            try {
                // Lưu Problem xuống DB
                Problem p = new Problem();
                p.setTitle("Problem " + System.currentTimeMillis());
                p.setDescription(probText);
                int pid = problemDAO.addProblem(p);
                p.setId(pid);
                currentProblem = p;

                String jsonResponse = aiService.generateTestCases(probText, 3);
                
                // Parse JSON array
                JsonArray array = JsonParser.parseString(jsonResponse).getAsJsonArray();
                List<TestCase> newTestCases = new ArrayList<>();
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    TestCase tc = new TestCase();
                    tc.setProblemId(pid);
                    tc.setInputData(obj.get("input").getAsString());
                    tc.setExpectedOutput(obj.get("expected_output").getAsString());
                    tc.setHidden(false);
                    // Lấy ID tự sinh từ DB sau khi add
                    int tcid = testCaseDAO.addTestCase(tc);
                    tc.setId(tcid);
                    newTestCases.add(tc);
                }

                Platform.runLater(() -> {
                    testCaseTable.getItems().clear();
                    testCaseTable.getItems().addAll(newTestCases);
                    log("Đã sinh thành công " + newTestCases.size() + " testcases và lưu vào CSDL (Problem ID: " + pid + ").");
                });

            } catch (Exception ex) {
                Platform.runLater(() -> log("Lỗi cấu hình API/Parse JSON: Bạn cần thiết lập API Key thật vào AiService (Mặc định bị ngắt). \n" + ex.getMessage()));
            }
        }).start();
    }

    private void handleGenerateCode() {
        String probText = problemArea.getText().trim();
        String lang = langCombo.getValue();
        if (probText.isEmpty()) {
            log("Lỗi: Cần đề bài để sinh code!"); return;
        }
        log("Yêu cầu AI viết mã giải thuật " + lang + " AC...");
        new Thread(() -> {
            try {
                String code = aiService.generateSampleCode(probText, lang);
                Platform.runLater(() -> {
                    codeArea.setText(code);
                    log("Đã sinh code thành công.");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> log("Lỗi: " + ex.getMessage()));
            }
        }).start();
    }

    private void handleRunExecution() {
        String code = codeArea.getText().trim();
        String lang = langCombo.getValue();
        List<TestCase> testCases = testCaseTable.getItems();

        if (code.isEmpty() || testCases.isEmpty() || currentProblem == null) {
            log("Lỗi: Phải có mã nguồn và testcase trên bảng, hoặc chưa có đề bài!");
            return;
        }

        log("\n--- BẮT ĐẦU CHẠY CHẤM ĐIỂM (EVALUATION) ---");
        new Thread(() -> {
            for (TestCase tc : testCases) {
                // Evaluation Process giới hạn 2000 ms (2 giây) TLE
                EvaluationResult res = executionService.evaluateSubmission(code, lang, tc, 2000);
                
                Platform.runLater(() -> {
                    String msg = String.format("TC #%d -> [Status: %s] | Thời gian: %d ms | Lỗi nếu có: %s",
                            tc.getId(), res.getStatus(), res.getExecutionTimeMs() == null ? 0 : res.getExecutionTimeMs(),
                            res.getErrorMessage() == null ? "Không" : res.getErrorMessage().replace("\n", " ")
                    );
                    log(msg);
                });
            }
            Platform.runLater(() -> log("--- HOÀN THÀNH CHẤM BÀI ---"));
        }).start();
    }

    private void log(String msg) {
        consoleArea.appendText(msg + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
