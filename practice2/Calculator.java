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
    private DecimalFormat formatter = new DecimalFormat("#,##0.##########");
    
    // Color themes
    private final Color[] themes = {
        new Color(230, 230, 230), // Light
        new Color(50, 50, 50),    // Dark
        new Color(200, 230, 255)  // Blue
    };
    private int currentTheme = 0;

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
        display.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(display, BorderLayout.NORTH);

        // Create history panel
        history = new JTextArea(5, 30);
        history.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(history);
        mainPanel.add(historyScroll, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 5, 3, 3));

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
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Add keyboard listener
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
        setFocusable(true);

        // Set initial theme
        applyTheme();

        pack();
        setLocationRelativeTo(null);
        setSize(400, 600);
    }

    private void handleKeyPress(KeyEvent e) {
        char key = e.getKeyChar();
        if (Character.isDigit(key) || "+-*/.".indexOf(key) != -1) {
            processInput(String.valueOf(key));
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            processInput("=");
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            processBackspace();
        }
    }

    private void processInput(String input) {
        if (input.matches("[0-9.]")) {
            if (startNewNumber) {
                display.setText(input);
                startNewNumber = false;
            } else {
                display.setText(display.getText() + input);
            }
        } else {
            if (!startNewNumber) {
                calculate(Double.parseDouble(display.getText()));
                startNewNumber = true;
            }
            lastOperation = input;
        }
    }

    private void processBackspace() {
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
                    result += number;
                    break;
                case "-":
                    result -= number;
                    break;
                case "*":
                    result *= number;
                    break;
                case "/":
                    if (number != 0) {
                        result /= number;
                    } else {
                        throw new ArithmeticException("Division by zero");
                    }
                    break;
                case "√":
                    if (number >= 0) {
                        result = Math.sqrt(number);
                    } else {
                        throw new ArithmeticException("Invalid square root");
                    }
                    break;
                case "x²":
                    result = Math.pow(number, 2);
                    break;
                case "%":
                    result = result * (number / 100);
                    break;
                case "=":
                    result = number;
                    break;
            }
            
            // Format and update display
            display.setText(formatter.format(result));
            
            // Add to history
            String historyEntry = formatter.format(number) + " " + lastOperation + " = " + formatter.format(result);
            calculationHistory.add(historyEntry);
            updateHistory();
            
        } catch (ArithmeticException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            reset();
        }
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
        parenthesesStack.clear();
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
        
        // Update all buttons
        for (Component comp : ((JPanel)container.getComponent(0)).getComponents()) {
            if (comp instanceof JButton) {
                comp.setBackground(bgColor.brighter());
                comp.setForeground(fgColor);
            }
        }
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            
            switch (command) {
                case "Theme":
                    currentTheme = (currentTheme + 1) % themes.length;
                    applyTheme();
                    break;
                case "⌫":
                    processBackspace();
                    break;
                case "±":
                    if (!display.getText().equals("0")) {
                        if (display.getText().startsWith("-")) {
                            display.setText(display.getText().substring(1));
                        } else {
                            display.setText("-" + display.getText());
                        }
                    }
                    break;
                case "MC":
                    memory = 0;
                    break;
                case "MR":
                    display.setText(formatter.format(memory));
                    startNewNumber = false;
                    break;
                case "M+":
                    memory += Double.parseDouble(display.getText());
                    startNewNumber = true;
                    break;
                case "M-":
                    memory -= Double.parseDouble(display.getText());
                    startNewNumber = true;
                    break;
                case "MS":
                    memory = Double.parseDouble(display.getText());
                    startNewNumber = true;
                    break;
                case "C":
                    reset();
                    break;
                default:
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