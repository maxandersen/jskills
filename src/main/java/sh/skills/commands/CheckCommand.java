package sh.skills.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sh.skills.util.Console;

import java.util.concurrent.Callable;

/**
 * Implements `skills check` — now an alias for `skills update`.
 * Kept for backward compatibility (upstream #913 merged check into update).
 */
@Command(
    name = "check",
    description = "Check for available updates (alias for 'update')",
    mixinStandardHelpOptions = true
)
public class CheckCommand implements Callable<Integer> {

    @Option(names = {"-g", "--global"}, description = "Check globally installed skills")
    private boolean global;

    @Override
    public Integer call() {
        Console.log(Console.dim("Note: 'skills check' is now an alias for 'skills update'."));
        Console.log(Console.dim("Use 'skills update' for more options (--project, --global, skill names)."));
        System.out.println();

        // Delegate to update command
        UpdateCommand update = new UpdateCommand();
        // Pass through global flag via picocli won't work here, so use the simple approach
        return new picocli.CommandLine(update).execute(
            global ? new String[]{"-g"} : new String[]{}
        );
    }
}
