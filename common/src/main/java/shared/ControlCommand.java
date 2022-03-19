package shared;

public class ControlCommand extends AbstractCommand {
    public enum Type{
        DELETE_FILE, OPEN_FOLDER, GO_TO_UPPER_FOLDER, RENAME_FILE, CREATE_FOLDER
    }

    private Type command;
    private String fileName;

    public ControlCommand(Type command, String fileName) {
        this.command = command;
        this.fileName = fileName;
    }

    public Type getCommand(){
        return this.command;
    }

    public String getFileName() {
        return fileName;
    }
}
