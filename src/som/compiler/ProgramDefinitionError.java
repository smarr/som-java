package som.compiler;

public class ProgramDefinitionError extends Exception {
  private static final long serialVersionUID = -2555195397219550779L;

  public ProgramDefinitionError(final String message) {
    super(message);
  }

  @Override
  public String toString() {
    return "ProgramDefinitionError: " + getMessage();
  }
}
