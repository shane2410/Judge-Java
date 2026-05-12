package com.app.ui;

import java.util.ArrayList;
import java.util.List;

import com.app.dao.CodeSubmissionDAO;
import com.app.dao.EvaluationResultDAO;
import com.app.dao.ProblemDAO;
import com.app.dao.TestCaseDAO;
import com.app.entity.CodeSubmission;
import com.app.entity.EvaluationResult;
import com.app.entity.Problem;
import com.app.entity.TestCase;
import com.app.executor.CodeExecutionService;
import com.app.service.AiService;
import com.app.service.DeepSeekService;
import com.app.service.LocalOcrService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private TextArea problemArea;
    private TableView<TestCase> testCaseTable;
    private TextArea codeArea;
    private ComboBox<String> langCombo;
    private TextArea consoleArea;

    // --- Kiến Trúc Hybrid AI ---
    private LocalOcrService ocrService; // Dùng VietOCR3 (Offline) chuyên để đọc ảnh OCR
    private DeepSeekService aiService;  // Dùng DeepSeek chuyên cho Logic & Code
    private CodeExecutionService executionService;
    private ProblemDAO problemDAO;
    private TestCaseDAO testCaseDAO;
    private CodeSubmissionDAO codeSubmissionDAO;
    private EvaluationResultDAO evaluationResultDAO;

    private Problem currentProblem;

    @Override
    public void start(Stage primaryStage) {
        // Init Services
        ocrService = new LocalOcrService(); // Khởi tạo VietOCR3 Offline
        aiService = new DeepSeekService();  // Khởi tạo DeepSeek (Logic)
        executionService = new CodeExecutionService();
        problemDAO = new ProblemDAO();
        testCaseDAO = new TestCaseDAO();
        codeSubmissionDAO = new CodeSubmissionDAO();
        evaluationResultDAO = new EvaluationResultDAO();

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
        btnUploadImg.setOnAction(e -> handleUploadImage(primaryStage));

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
            // Lưu CodeSubmission vào DB
            CodeSubmission submission = new CodeSubmission();
            submission.setProblemId(currentProblem.getId());
            submission.setSourceCode(code);
            submission.setLanguage(lang);
            int subId = codeSubmissionDAO.addSubmission(submission);
            submission.setId(subId);

            for (TestCase tc : testCases) {
                // Evaluation Process giới hạn 2000 ms (2 giây) TLE
                EvaluationResult res = executionService.evaluateSubmission(code, lang, tc, 2000);
                res.setSubmissionId(subId);
                
                // Lưu Result vào DB
                evaluationResultDAO.addResult(res);
                
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

    private void handleUploadImage(Stage stage) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Chọn ảnh đề bài");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            log("Đang phân tích ảnh: " + selectedFile.getName() + " ...");
            new Thread(() -> {
                try {
                    // 1. Quét OCR Offline bằng VietOCR3
                    String noisyText = ocrService.extractTextFromFile(selectedFile);
                    
                    if (noisyText == null || noisyText.trim().isEmpty()) {
                        Platform.runLater(() -> log("Cảnh báo: VietOCR3 không tìm thấy chữ nào."));
                        return;
                    }

                    Platform.runLater(() -> log("Đang dùng DeepSeek để sửa lỗi font và định dạng..."));
                    
                    // 2. Dùng DeepSeek để "mông má" lại văn bản cho đẹp
                    String cleanText = aiService.refineOcrText(noisyText);

                    Platform.runLater(() -> {
                        problemArea.setText(cleanText);
                        log("Đã trích xuất và tinh chỉnh văn bản thành công!");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> log("Lỗi OCR (VietOCR3): " + ex.getMessage()));
                }
            }).start();
        }
    }

    private void log(String msg) {
        consoleArea.appendText(msg + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
