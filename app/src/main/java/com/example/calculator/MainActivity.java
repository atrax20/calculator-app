package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    
    private EditText display;
    private TextView historyDisplay;
    private ListView historyList;
    private ArrayAdapter<String> historyAdapter;
    private ArrayList<String> calculationHistory;
    
    private String currentInput = "";
    private String currentOperator = "";
    private double firstValue = Double.NaN;
    private boolean isNewCalculation = true;
    private DecimalFormat decimalFormat = new DecimalFormat("#.##########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupNumberButtons();
        setupOperatorButtons();
        setupActionButtons();
        setupHistory();
    }

    private void initializeViews() {
        display = findViewById(R.id.display);
        historyDisplay = findViewById(R.id.history_display);
        historyList = findViewById(R.id.history_list);
    }

    private void setupHistory() {
        calculationHistory = new ArrayList<>();
        historyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, calculationHistory);
        historyList.setAdapter(historyAdapter);
        
        findViewById(R.id.btn_clear_history).setOnClickListener(v -> clearHistory());
        
        historyList.setOnItemClickListener((parent, view, position, id) -> {
            String historyItem = calculationHistory.get(position);
            String[] parts = historyItem.split(" = ");
            if (parts.length == 2) {
                currentInput = parts[1];
                updateDisplay();
                updateHistoryDisplay(historyItem);
            }
        });
    }

    private void setupNumberButtons() {
        int[] numberButtonIds = {
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(v -> {
                Button button = (Button) v;
                String number = button.getText().toString();
                
                if (isNewCalculation) {
                    currentInput = number;
                    isNewCalculation = false;
                } else {
                    currentInput += number;
                }
                updateDisplay();
            });
        }
    }

    private void setupOperatorButtons() {
        findViewById(R.id.btn_add).setOnClickListener(v -> setOperator("+"));
        findViewById(R.id.btn_subtract).setOnClickListener(v -> setOperator("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> setOperator("×"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> setOperator("÷"));
        findViewById(R.id.btn_percent).setOnClickListener(v -> calculatePercentage());
        findViewById(R.id.btn_equals).setOnClickListener(v -> calculateResult());
    }

    private void setupActionButtons() {
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearCalculator());
        findViewById(R.id.btn_backspace).setOnClickListener(v -> backspace());
        findViewById(R.id.btn_decimal).setOnClickListener(v -> addDecimalPoint());
    }

    private void setOperator(String operator) {
        if (!currentInput.isEmpty()) {
            if (!Double.isNaN(firstValue)) {
                calculateResult();
            } else {
                firstValue = Double.parseDouble(currentInput);
            }
            currentOperator = operator;
            updateHistoryDisplay(formatNumber(firstValue) + " " + currentOperator);
            currentInput = "";
            isNewCalculation = true;
        }
    }

    private void calculatePercentage() {
        if (!currentInput.isEmpty()) {
            double value = Double.parseDouble(currentInput);
            double percentage;
            
            if (!Double.isNaN(firstValue) && !currentOperator.isEmpty()) {
                percentage = firstValue * value / 100;
            } else {
                percentage = value / 100;
            }
            
            if (percentage == (long) percentage) {
                currentInput = String.valueOf((long) percentage);
            } else {
                currentInput = decimalFormat.format(percentage);
            }
            
            updateDisplay();
            
            if (!Double.isNaN(firstValue) && !currentOperator.isEmpty()) {
                String historyEntry = formatNumber(firstValue) + " " + currentOperator + " " + 
                                   formatNumber(value) + "% = " + formatNumber(Double.parseDouble(currentInput));
                addToHistory(historyEntry);
            } else {
                String historyEntry = formatNumber(value) + "% = " + formatNumber(Double.parseDouble(currentInput));
                addToHistory(historyEntry);
            }
            
            isNewCalculation = true;
        }
    }

    private void calculateResult() {
        if (!Double.isNaN(firstValue) && !currentInput.isEmpty() && !currentOperator.isEmpty()) {
            double secondValue = Double.parseDouble(currentInput);
            double result = 0;
            String operation = formatNumber(firstValue) + " " + currentOperator + " " + formatNumber(secondValue);

            switch (currentOperator) {
                case "+":
                    result = firstValue + secondValue;
                    break;
                case "-":
                    result = firstValue - secondValue;
                    break;
                case "×":
                    result = firstValue * secondValue;
                    break;
                case "÷":
                    if (secondValue != 0) {
                        result = firstValue / secondValue;
                    } else {
                        display.setText("Error");
                        clearCalculator();
                        return;
                    }
                    break;
            }

            String formattedResult = formatNumber(result);
            currentInput = formattedResult;
            
            String historyEntry = operation + " = " + formattedResult;
            addToHistory(historyEntry);
            updateHistoryDisplay(historyEntry);
            
            firstValue = result;
            currentOperator = "";
            isNewCalculation = true;
            updateDisplay();
        }
    }

    private void addDecimalPoint() {
        if (isNewCalculation) {
            currentInput = "0.";
            isNewCalculation = false;
        } else if (!currentInput.contains(".")) {
            currentInput += ".";
        }
        updateDisplay();
    }

    private void backspace() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if (currentInput.isEmpty()) {
                currentInput = "0";
                isNewCalculation = true;
            }
            updateDisplay();
        }
    }

    private void clearCalculator() {
        currentInput = "";
        currentOperator = "";
        firstValue = Double.NaN;
        isNewCalculation = true;
        display.setText("0");
        historyDisplay.setText("");
    }

    private void clearHistory() {
        calculationHistory.clear();
        historyAdapter.notifyDataSetChanged();
        historyDisplay.setText("");
    }

    private void addToHistory(String entry) {
        calculationHistory.add(0, entry);
        if (calculationHistory.size() > 50) {
            calculationHistory.remove(calculationHistory.size() - 1);
        }
        historyAdapter.notifyDataSetChanged();
    }

    private void updateHistoryDisplay(String text) {
        historyDisplay.setText(text);
    }

    private void updateDisplay() {
        display.setText(currentInput.isEmpty() ? "0" : currentInput);
    }

    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        } else {
            return decimalFormat.format(number);
        }
    }
}
