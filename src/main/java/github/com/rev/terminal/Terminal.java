package github.com.rev.terminal;

import github.com.rev.GlfwManager;
import github.com.rev.WindowedProgram;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class Terminal {

    private final PrintWriter printWriter = new PrintWriter(System.out);
    private final Scanner scanner = new Scanner(System.in);
    private boolean stop;

    private final GlfwManager manager = GlfwManager.instance();
    private final Map<String, Collection<WindowedProgram>> programOptions = new HashMap<>();

    public void addOption(String name, Collection<WindowedProgram> programs) {
        programOptions.put(name, programs);
    }

    public void start() {
        printWelcome();
        printOptions();
        printWriter.flush();

        while (!stop) {
            String input = scanner.nextLine();
            processInput(input);
            printWriter.flush();
        }

    }

    private void processInput(String input) {
        if ("help".equals(input)) {
            printOptions();
            return;
        }

        if ("quit".equals(input) || "q".equals(input) || "exit".equals(input)) {
            printWriter.println("Bye");
            stop = true;
            return;
        }

        if (programOptions.containsKey(input)) {
            try {
                execute(input);
                return;
            } finally {
                stop = true;
            }
        }

        printWriter.printf("Invalid option%n%n");
        printOptions();
    }

    private void execute(String input) {
        Collection<WindowedProgram> options = programOptions.get(input);
        options.forEach(manager::addWindowedProgram);
        manager.run();
    }

    private void printWelcome() {
        printWriter.printf("Welcome to the LWJGL playground!%n%n");
    }

    private void printOptions() {
        printWriter.printf("Type an option to run:%n%n");
        programOptions.keySet().forEach(option -> printWriter.printf("%s%n", option));
        printWriter.printf("%nInput: ");
    }


}
