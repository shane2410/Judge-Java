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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
    private Spinner<Integer> testCaseCountSpinner;
    private TextArea consoleArea;

    // --- Kiến Trúc Hybrid AI ---
    private LocalOcrService ocrService; // Dùng VietOCR3 (Offline) chuyên để đọc ảnh OCR
    private DeepSeekService aiService;  // Dùng DeepSeek chuyên cho Logic & Code
    private CodeExecutionService executionService;
    private ProblemDAO problemDAO;
    private TestCaseDAO testCaseDAO;
    private CodeSubmissionDAO codeSubmissionDAO;
    private EvaluationResultDAO evaluationResultDAO;

    // --- History UI components ---
    private TableView<Problem> historyProblemTable;
    private TableView<TestCase> historyTestCaseTable;
    private TableView<CodeSubmission> historySubmissionTable;
    private TableView<EvaluationResult> historyResultTable;

    private Problem currentProblem;
    private TabPane tabPane;
    private Tab judgeTab;

    @Override
    public void start(Stage primaryStage) {
        // Init Services
        ocrService = new LocalOcrService();
        aiService = new DeepSeekService();
        executionService = new CodeExecutionService();
        problemDAO = new ProblemDAO();
        testCaseDAO = new TestCaseDAO();
        codeSubmissionDAO = new CodeSubmissionDAO();
        evaluationResultDAO = new EvaluationResultDAO();

        primaryStage.setTitle("JudgeAI - AI Assisted Programming Judge System");

        tabPane = new TabPane();

        // --- TAB 1: Judge (original layout) ---
        judgeTab = new Tab("Judge");
        judgeTab.setClosable(false);
        judgeTab.setContent(buildJudgePanel(primaryStage));

        // --- TAB 2: History ---
        Tab historyTab = new Tab("History");
        historyTab.setClosable(false);
        historyTab.setContent(buildHistoryPanel());
        historyTab.setOnSelectionChanged(e -> {
            if (historyTab.isSelected()) refreshHistoryData();
        });

        tabPane.getTabs().addAll(judgeTab, historyTab);

        Scene scene = new Scene(tabPane, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private BorderPane buildJudgePanel(Stage primaryStage) {
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

        HBox tcCountBox = new HBox(10);
        tcCountBox.getChildren().add(new Label("Số lượng testcase:"));
        testCaseCountSpinner = new Spinner<>();
        testCaseCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 8));
        testCaseCountSpinner.setEditable(true);
        testCaseCountSpinner.setPrefWidth(70);
        tcCountBox.getChildren().add(testCaseCountSpinner);

        Button btnGenTest = new Button("🤖 Phân tích & Sinh Testcase");
        btnGenTest.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnGenTest.setOnAction(e -> handleGenerateTestCases());

        leftBox.getChildren().addAll(lblProblem, problemArea, btnUploadImg, tcCountBox, btnGenTest);
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

        return root;
    }

    private BorderPane buildHistoryPanel() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // --- Top toolbar ---
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refreshHistoryData());
        Button btnDeleteProblem = new Button("Delete Selected Problem");
        btnDeleteProblem.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        btnDeleteProblem.setOnAction(e -> handleDeleteSelected());
        Button btnLoadToJudge = new Button("🚀 Load to Judge");
        btnLoadToJudge.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnLoadToJudge.setOnAction(e -> handleLoadProblemToJudge());
        toolbar.getChildren().addAll(btnRefresh, btnDeleteProblem, btnLoadToJudge);
        root.setTop(toolbar);

        // --- LEFT: Problems table ---
        VBox leftBox = new VBox(10);
        leftBox.setPrefWidth(350);
        Label lblProblems = new Label("Problems:");
        historyProblemTable = new TableView<>();

        TableColumn<Problem, String> colPId = new TableColumn<>("ID");
        colPId.setPrefWidth(40);
        colPId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

        TableColumn<Problem, String> colPTitle = new TableColumn<>("Title");
        colPTitle.setPrefWidth(120);
        colPTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Problem, String> colPDate = new TableColumn<>("Created");
        colPDate.setPrefWidth(150);
        colPDate.setCellValueFactory(data -> {
            java.sql.Timestamp ts = data.getValue().getCreatedAt();
            return new SimpleStringProperty(ts != null ? ts.toString() : "");
        });

        historyProblemTable.getColumns().addAll(colPId, colPTitle, colPDate);
        historyProblemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onProblemSelected(newVal);
        });
        leftBox.getChildren().addAll(lblProblems, historyProblemTable);
        root.setLeft(leftBox);

        // --- RIGHT: Detail area with tabs for TestCases, Submissions, Results ---
        TabPane detailTabPane = new TabPane();

        // TestCases tab
        Tab tcTab = new Tab("Test Cases");
        tcTab.setClosable(false);
        historyTestCaseTable = new TableView<>();
        TableColumn<TestCase, String> colTcId = new TableColumn<>("ID");
        colTcId.setPrefWidth(40);
        colTcId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        TableColumn<TestCase, String> colTcInput = new TableColumn<>("Input");
        colTcInput.setPrefWidth(200);
        colTcInput.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInputData()));
        TableColumn<TestCase, String> colTcOutput = new TableColumn<>("Expected Output");
        colTcOutput.setPrefWidth(200);
        colTcOutput.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpectedOutput()));
        TableColumn<TestCase, String> colTcHidden = new TableColumn<>("Hidden");
        colTcHidden.setPrefWidth(60);
        colTcHidden.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isHidden() ? "Yes" : "No"));
        historyTestCaseTable.getColumns().addAll(colTcId, colTcInput, colTcOutput, colTcHidden);
        tcTab.setContent(historyTestCaseTable);

        // Submissions tab
        Tab subTab = new Tab("Submissions");
        subTab.setClosable(false);
        historySubmissionTable = new TableView<>();
        TableColumn<CodeSubmission, String> colSubId = new TableColumn<>("ID");
        colSubId.setPrefWidth(40);
        colSubId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        TableColumn<CodeSubmission, String> colSubLang = new TableColumn<>("Language");
        colSubLang.setPrefWidth(80);
        colSubLang.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLanguage()));
        TableColumn<CodeSubmission, String> colSubVerdict = new TableColumn<>("Expected Verdict");
        colSubVerdict.setPrefWidth(100);
        colSubVerdict.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpectedVerdict()));
        TableColumn<CodeSubmission, String> colSubDate = new TableColumn<>("Created");
        colSubDate.setPrefWidth(150);
        colSubDate.setCellValueFactory(data -> {
            java.sql.Timestamp ts = data.getValue().getCreatedAt();
            return new SimpleStringProperty(ts != null ? ts.toString() : "");
        });
        historySubmissionTable.getColumns().addAll(colSubId, colSubLang, colSubVerdict, colSubDate);
        historySubmissionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onSubmissionSelected(newVal);
        });
        subTab.setContent(historySubmissionTable);

        // Evaluation Results tab
        Tab resTab = new Tab("Evaluation Results");
        resTab.setClosable(false);
        historyResultTable = new TableView<>();
        TableColumn<EvaluationResult, String> colResId = new TableColumn<>("ID");
        colResId.setPrefWidth(40);
        colResId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        TableColumn<EvaluationResult, String> colResStatus = new TableColumn<>("Status");
        colResStatus.setPrefWidth(70);
        colResStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        TableColumn<EvaluationResult, String> colResTime = new TableColumn<>("Time (ms)");
        colResTime.setPrefWidth(80);
        colResTime.setCellValueFactory(data -> {
            Integer t = data.getValue().getExecutionTimeMs();
            return new SimpleStringProperty(t != null ? t.toString() : "-");
        });
        TableColumn<EvaluationResult, String> colResTcId = new TableColumn<>("TC ID");
        colResTcId.setPrefWidth(50);
        colResTcId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTestcaseId())));
        TableColumn<EvaluationResult, String> colResOutput = new TableColumn<>("Actual Output");
        colResOutput.setPrefWidth(200);
        colResOutput.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getActualOutput() != null ? data.getValue().getActualOutput() : ""));
        TableColumn<EvaluationResult, String> colResError = new TableColumn<>("Error");
        colResError.setPrefWidth(200);
        colResError.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getErrorMessage() != null ? data.getValue().getErrorMessage() : ""));
        historyResultTable.getColumns().addAll(colResId, colResStatus, colResTime, colResTcId, colResOutput, colResError);
        resTab.setContent(historyResultTable);

        detailTabPane.getTabs().addAll(tcTab, subTab, resTab);
        root.setCenter(detailTabPane);

        return root;
    }

    private void refreshHistoryData() {
        new Thread(() -> {
            List<Problem> problems = problemDAO.getAllProblems();
            Platform.runLater(() -> {
                historyProblemTable.getItems().clear();
                historyProblemTable.getItems().addAll(problems);
                historyTestCaseTable.getItems().clear();
                historySubmissionTable.getItems().clear();
                historyResultTable.getItems().clear();
                log("History refreshed: " + problems.size() + " problem(s) loaded.");
            });
        }).start();
    }

    private void onProblemSelected(Problem p) {
        new Thread(() -> {
            List<TestCase> tcs = testCaseDAO.getTestCasesByProblemId(p.getId());
            List<CodeSubmission> subs = codeSubmissionDAO.getSubmissionsByProblemId(p.getId());
            Platform.runLater(() -> {
                historyTestCaseTable.getItems().clear();
                historyTestCaseTable.getItems().addAll(tcs);
                historySubmissionTable.getItems().clear();
                historySubmissionTable.getItems().addAll(subs);
                historyResultTable.getItems().clear();
            });
        }).start();
    }

    private void onSubmissionSelected(CodeSubmission sub) {
        new Thread(() -> {
            List<EvaluationResult> results = evaluationResultDAO.getResultsBySubmissionId(sub.getId());
            Platform.runLater(() -> {
                historyResultTable.getItems().clear();
                historyResultTable.getItems().addAll(results);
            });
        }).start();
    }

    private void handleDeleteSelected() {
        Problem selected = historyProblemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log("Please select a problem to delete.");
            return;
        }
        problemDAO.deleteProblem(selected.getId());
        log("Deleted problem ID=" + selected.getId());
        refreshHistoryData();
    }

    private void handleLoadProblemToJudge() {
        Problem selected = historyProblemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log("Vui lòng chọn một Problem từ bảng để nạp sang trang Judge.");
            return;
        }

        // 1. Chuyển sang Tab Judge
        tabPane.getSelectionModel().select(judgeTab);

        // 2. Set current problem và text đề bài
        currentProblem = selected;
        problemArea.setText(selected.getDescription() != null ? selected.getDescription() : "");

        // 3. Lấy testcases và code submissions ngầm định (tránh lag giao diện)
        new Thread(() -> {
            List<TestCase> tcs = testCaseDAO.getTestCasesByProblemId(selected.getId());
            List<CodeSubmission> subs = codeSubmissionDAO.getSubmissionsByProblemId(selected.getId());
            
            Platform.runLater(() -> {
                // Đổ dữ liệu TestCases
                testCaseTable.getItems().clear();
                testCaseTable.getItems().addAll(tcs);
                
                // Đổ dữ liệu Code Submission mới nhất
                if (subs != null && !subs.isEmpty()) {
                    CodeSubmission latestSub = subs.get(subs.size() - 1);
                    codeArea.setText(latestSub.getSourceCode() != null ? latestSub.getSourceCode() : "");
                    if (latestSub.getLanguage() != null) {
                        langCombo.setValue(latestSub.getLanguage());
                    }
                } else {
                    codeArea.clear(); // Xóa code cũ nếu bài này chưa từng nộp
                }
                
                log("Đã nạp bài tập (ID=" + selected.getId() + ") sang trang Judge thành công!");
            });
        }).start();
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

                String jsonResponse = aiService.generateTestCases(probText, testCaseCountSpinner.getValue());
                
                Platform.runLater(() -> log("[DEBUG] Raw API response:\n" + jsonResponse.substring(0, Math.min(300, jsonResponse.length()))));

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
                Platform.runLater(() -> log("Lỗi API/Parse: " + ex.getMessage()));
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
            List<EvaluationResult> tempResults = new ArrayList<>();
            boolean allPassed = true;

            for (TestCase tc : testCases) {
                // Evaluation Process giới hạn 2000 ms (2 giây) TLE
                EvaluationResult res = executionService.evaluateSubmission(code, lang, tc, 2000);
                tempResults.add(res);
                
                if (!"AC".equals(res.getStatus())) {
                    allPassed = false;
                }
                
                Platform.runLater(() -> {
                    String msg = String.format("TC #%d -> [Status: %s] | Thời gian: %d ms | Lỗi nếu có: %s",
                            tc.getId(), res.getStatus(), res.getExecutionTimeMs() == null ? 0 : res.getExecutionTimeMs(),
                            res.getErrorMessage() == null ? "Không" : res.getErrorMessage().replace("\n", " ")
                    );
                    log(msg);
                });
            }

            // Chỉ lưu lại Solution (CodeSubmission) nếu tất cả các testcase đều Pass (AC)
            if (allPassed) {
                CodeSubmission submission = new CodeSubmission();
                submission.setProblemId(currentProblem.getId());
                submission.setSourceCode(code);
                submission.setLanguage(lang);
                submission.setExpectedVerdict("AC");
                int subId = codeSubmissionDAO.addSubmission(submission);
                
                for (EvaluationResult res : tempResults) {
                    res.setSubmissionId(subId);
                    evaluationResultDAO.addResult(res);
                }
                
                Platform.runLater(() -> log("--- Giải thuật chính xác. Đã lưu Solution vào Lịch Sử! ---"));
            } else {
                Platform.runLater(() -> log("--- BÀI LÀM CHƯA HOÀN THIỆN ---"));
            }
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

                    Platform.runLater(() -> {
                        problemArea.setText(noisyText);
                        log("OCR thành công! Đang dùng AI để tinh chỉnh...");
                    });
                    
                    // 2. Dùng DeepSeek để làm sạch văn bản
                    try {
                        String cleanText = aiService.refineOcrText(noisyText);
                        if (cleanText != null && !cleanText.trim().isEmpty()) {
                            Platform.runLater(() -> {
                                problemArea.setText(cleanText);
                                log("Đã tinh chỉnh văn bản thành công!");
                            });
                        }
                    } catch (Exception refineEx) {
                        Platform.runLater(() -> log("Không thể tinh chỉnh (dùng bản thô): " + refineEx.getMessage()));
                    }
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
