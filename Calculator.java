import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Stack;

public class Calculator extends JFrame {
    private JTextField display;
    private JTextArea history;
    private double result = 0;
    private double memory = 0;
    private String lastOperation = "=";
    private boolean startNewNumber = true;
    private ArrayList<String> calculationHistory = new ArrayList<>();
    private Stack<Double> parenthesesStack = new Stack<>();
    private Stack<String> operatorStack = new Stack<>();
    private DecimalFormat formatter = new DecimalFormat("#,##0.##########");
    private boolean isError = false;
    private StringBuilder expression = new StringBuilder();
    private int currentTheme = 0;
    private final Color[] themes = {
        new Color(230, 230, 230), // Light
        new Color(50, 50, 50),    // Dark
        new Color(200, 230, 255)  // Blue
    };

    public Calculator() {
        setTitle("Advanced Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        // Create display
        display = new JTextField("0");
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(display, BorderLayout.NORTH);

        // Create history panel
        history = new JTextArea(5, 30);
        history.setEditable(false);
        history.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane historyScroll = new JScrollPane(history);
        mainPanel.add(historyScroll, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(7, 5, 5, 5));

        // Create buttons
        String[] buttonLabels = {
            "MC", "MR", "M+", "M-", "MS",
            "(", ")", "±", "%", "C",
            "7", "8", "9", "/", "√",
            "4", "5", "6", "*", "x²",
            "1", "2", "3", "-", "1/x",
            "0", ".", "⌫", "+", "=",
            "Theme", "Scientific", "", "", ""
        };

        for (String label : buttonLabels) {
            if (label.isEmpty()) continue;
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Add keyboard handler
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        handleKeyPress(e);
                    }
                    return false;
                }
            });

        applyTheme();
        setSize(400, 600);
        setLocationRelativeTo(null);
    }

    private void handleKeyPress(KeyEvent e) {
        char key = e.getKeyChar();
        if (Character.isDigit(key) || "+-*/.()".indexOf(key) != -1) {
            processInput(String.valueOf(key));
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            processInput("=");
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            processBackspace();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            processInput("C");
        }
    }

    private void processInput(String input) {
        if (isError) {
            if (input.equals("C")) {
                reset();
            }
            return;
        }

        if (input.matches("[0-9.]")) {
            if (startNewNumber) {
                display.setText(input);
                startNewNumber = false;
            } else {
                if (input.equals(".") && display.getText().contains(".")) {
                    return;
                }
                display.setText(display.getText() + input);
            }
            expression.append(input);
        } else if (input.equals("(")) {
            parenthesesStack.push(result);
            operatorStack.push(lastOperation);
            result = 0;
            lastOperation = "=";
            startNewNumber = true;
            expression.append(" ( ");
        } else if (input.equals(")")) {
            if (!parenthesesStack.isEmpty()) {
                calculate(Double.parseDouble(display.getText()));
                double innerResult = result;
                result = parenthesesStack.pop();
                lastOperation = operatorStack.pop();
                calculate(innerResult);
                display.setText(formatter.format(result));
                expression.append(" ) ");
            }
        } else {
            if (!startNewNumber) {
                calculate(Double.parseDouble(display.getText()));
                startNewNumber = true;
            }
            lastOperation = input;
            if (input.equals("=")) {
                addToHistory(expression.toString() + " = " + formatter.format(result));
                expression.setLength(0);
            } else if (!input.equals("C")) {
                expression.append(" ").append(input).append(" ");
            }
        }
    }

    private void processBackspace() {
        if (isError || startNewNumber) return;
        String currentText = display.getText();
        if (currentText.length() > 1) {
            display.setText(currentText.substring(0, currentText.length() - 1));
        } else {
            display.setText("0");
            startNewNumber = true;
        }
    }

    private void calculate(double number) {
        try {
            switch (lastOperation) {
                case "+":
                    result += number;// result = result + number
                    break;
                case "-":
                    result -= number;
                    break;
                case "*":
                    result *= number;
                    break;
                case "/":
                    if (number == 0) throw new ArithmeticException("Division by zero");
                    result /= number;
                    break;
                case "√":
                    if (number < 0) throw new ArithmeticException("Invalid square root");
                    result = Math.sqrt(number);
                    break;
                case "x²":
                    result = Math.pow(number, 2);
                    break;
                case "%":
                    result = result * (number / 100);
                    break;
                case "1/x":
                    if (number == 0) throw new ArithmeticException("Division by zero");
                    result = 1 / number;
                    break;
                case "=":
                    result = number;
                    break;
            }
            display.setText(formatter.format(result));
        } catch (Exception ex) {
            display.setText("Not applicable");
            isError = true;
        }
    }

    private void addToHistory(String entry) {
        calculationHistory.add(entry);
        updateHistory();
    }

    private void updateHistory() {
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, calculationHistory.size() - 5);
        for (int i = start; i < calculationHistory.size(); i++) {
            sb.append(calculationHistory.get(i)).append("\n");
        }
        history.setText(sb.toString());
    }

    private void reset() {
        result = 0;
        lastOperation = "=";
        startNewNumber = true;
        display.setText("0");
        isError = false;
        parenthesesStack.clear();
        operatorStack.clear();
        expression.setLength(0);
    }

    private void handleMemoryOperation(String operation) {
        try {
            double displayValue = Double.parseDouble(display.getText());
            switch (operation) {
                case "MC":
                    memory = 0;
                    addToHistory("Memory Cleared");
                    break;
                case "MR":
                    display.setText(formatter.format(memory));
                    startNewNumber = false;
                    addToHistory("Memory Recall: " + formatter.format(memory));
                    break;
                case "M+":
                    memory += displayValue;
                    startNewNumber = true;
                    addToHistory("Memory + " + formatter.format(displayValue));
                    break;
                case "M-":
                    memory -= displayValue;
                    startNewNumber = true;
                    addToHistory("Memory - " + formatter.format(displayValue));
                    break;
                case "MS":
                    memory = displayValue;
                    startNewNumber = true;
                    addToHistory("Memory Store: " + formatter.format(displayValue));
                    break;
            }
        } catch (Exception e) {
            display.setText("Error");
            isError = true;
        }
    }

    private void applyTheme() {
        Color bgColor = themes[currentTheme];
        Color fgColor = (currentTheme == 1) ? Color.WHITE : Color.BLACK;
        
        Container container = getContentPane();
        container.setBackground(bgColor);
        display.setBackground(bgColor);
        display.setForeground(fgColor);
        history.setBackground(bgColor);
        history.setForeground(fgColor);
        
        for (Component comp : container.getComponents()) {
            updateComponentColors(comp, bgColor, fgColor);
        }
    }

    private void updateComponentColors(Component comp, Color bgColor, Color fgColor) {
        if (comp instanceof JPanel) {
            comp.setBackground(bgColor);
            for (Component child : ((JPanel)comp).getComponents()) {
                updateComponentColors(child, bgColor, fgColor);
            }
        } else if (comp instanceof JButton) {
            comp.setBackground(bgColor.brighter());
            comp.setForeground(fgColor);
        }
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            
            if (command.matches("M[CRST+-]")) {
                handleMemoryOperation(command);
            } else if (command.equals("Theme")) {
                currentTheme = (currentTheme + 1) % themes.length;
                applyTheme();
            } else if (command.equals("C")) {
                reset();
                calculationHistory.clear();
                updateHistory();
            } else if (command.equals("±")) {
                if (!display.getText().equals("0") && !isError) {
                    if (display.getText().startsWith("-")) {
                        display.setText(display.getText().substring(1));
                    } else {
                        display.setText("-" + display.getText());
                    }
                }
            } else {
                processInput(command);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculator calc = new Calculator();
            calc.setVisible(true);
        });
    }
}